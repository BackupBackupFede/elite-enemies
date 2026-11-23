package net.emeraude.eliteenemies;

import net.emeraude.eliteenemies.compat.Compat;
import net.emeraude.eliteenemies.core.Config;
import net.emeraude.eliteenemies.core.Loot;
import net.emeraude.eliteenemies.core.Messages;
import net.emeraude.eliteenemies.core.Rarity;
import net.emeraude.eliteenemies.core.SpawnGate;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextColor;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TridentItem;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Scoreboard;

import java.util.List;
import java.util.Set;

/**
 * The rule: rare mobs become elites — stronger, geared, announced — and drop their gear.
 *
 * <p>An elite is an ordinary vanilla mob with attribute modifiers, equipment, a custom name and a
 * few entity tags, so a vanilla client sees all of it and the state survives a restart with no
 * data of our own.
 */
public final class Elites {

    static final String TAG = EliteEnemies.MOD_ID;
    private static final String RARITY_TAG = TAG + ".";
    private static final String UNTIL_TAG = TAG + ".until.";
    private static final String MODIFIER = EliteEnemies.MOD_ID + ":elite";
    /** Above 1, vanilla always drops the piece, undamaged, whoever the killer. */
    private static final float ALWAYS_DROP = 2.0F;

    private static final SpawnGate GATE = new SpawnGate();

    private Elites() {}

    // --- reading -------------------------------------------------------------------------------

    /** The mob's rarity, or null when it is not an elite. */
    public static Rarity rarityOf(Mob mob) {
        Set<String> tags = Compat.tags(mob);
        if (!tags.contains(TAG)) return null;
        for (Rarity rarity : Rarity.values()) if (tags.contains(RARITY_TAG + rarity.id())) return rarity;
        return null;
    }

    /** An elite does not despawn during its first minutes (the "persist" setting). */
    public static boolean protectedFromDespawn(Mob mob) {
        if (!(mob.level() instanceof ServerLevel level) || level.getDifficulty() == Difficulty.PEACEFUL) return false;
        for (String tag : Compat.tags(mob)) {
            if (!tag.startsWith(UNTIL_TAG)) continue;
            try {
                return level.getGameTime() < Long.parseLong(tag.substring(UNTIL_TAG.length()));
            } catch (NumberFormatException e) {
                return false;
            }
        }
        return false;
    }

    // --- spawning ------------------------------------------------------------------------------

    /** Spawn reasons that count as a mob appearing on its own. */
    private static final Set<String> NATURAL = Set.of("NATURAL", "CHUNK_GENERATION", "STRUCTURE", "PATROL");
    private static final Set<String> SPAWNERS = Set.of("SPAWNER", "TRIAL_SPAWNER");

    /** Called for every mob that has just entered the world, with why it spawned. */
    public static void onSpawn(Mob mob, String reason, ServerLevel level) {
        Config config = EliteEnemies.config();
        if (!config.enabled || mob.isPassenger() || !mob.isAlive()) return;
        if (!NATURAL.contains(reason) && !(config.allowSpawners && SPAWNERS.contains(reason))) return;
        if (!config.mobs.contains(String.valueOf(BuiltInRegistries.ENTITY_TYPE.getKey(mob.getType())))) return;

        long now = level.getGameTime();
        String dimension = Compat.dimensionId(level);
        if (!GATE.open(now, dimension, mob.getX(), mob.getZ(), config.protectedRadius)) return;
        if (level.getRandom().nextDouble() * 100.0 >= config.chancePercent) return;
        if (config.maxHeightGap > 0) {
            Player nearest = level.getNearestPlayer(mob, 128.0);
            if (nearest == null || Math.abs(nearest.getY() - mob.getY()) > config.maxHeightGap) return;
        }
        int[] weights = config.weights();
        int total = Rarity.total(weights);
        if (total <= 0) return;
        Rarity rarity = Rarity.pick(weights, level.getRandom().nextInt(total));

        make(mob, rarity, level);
        GATE.record(now, dimension, mob.getX(), mob.getZ(), config.cooldownSeconds * 20, config.protectedMinutes * 1200);
    }

    /** Turns the mob into an elite of this rarity, wherever it came from. */
    public static void make(Mob mob, Rarity rarity, ServerLevel level) {
        Config config = EliteEnemies.config();
        // A baby drops nothing in 26.x: the rule says elites drop their gear, so elites are adults.
        if (mob.isBaby()) mob.setBaby(false);

        boost(mob, Attributes.MAX_HEALTH, rarity.health, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
        boost(mob, Attributes.ARMOR, rarity.armour, AttributeModifier.Operation.ADD_VALUE);
        boost(mob, Attributes.MOVEMENT_SPEED, rarity.speed, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
        // Damage is a flat bonus on the mob's own base damage, not a multiplier: a multiplier would
        // also multiply the weapon the elite holds (a legendary zombie with a netherite sword would
        // one-shot a player in full netherite).
        AttributeInstance attack = mob.getAttribute(Attributes.ATTACK_DAMAGE);
        if (attack != null) {
            boost(mob, Attributes.ATTACK_DAMAGE, attack.getBaseValue() * rarity.damage, AttributeModifier.Operation.ADD_VALUE);
        }
        boost(mob, Attributes.KNOCKBACK_RESISTANCE, rarity.knockback, AttributeModifier.Operation.ADD_VALUE);
        mob.setHealth(mob.getMaxHealth());

        Loot.Entry entry = Loot.pick(rarity, level.getRandom().nextInt(Loot.of(rarity).size()));
        ItemStack stack = gear(entry, rarity, level);
        EquipmentSlot slot = slotFor(entry.slot(), stack, mob);
        mob.setItemSlot(slot, stack);
        mob.setDropChance(slot, ALWAYS_DROP);

        mob.setCustomName(Component.literal(rarity.starText() + " ").append(mob.getType().getDescription())
            .withStyle(colour(rarity)));

        mob.addTag(TAG);
        mob.addTag(RARITY_TAG + rarity.id());
        mob.addTag(UNTIL_TAG + (level.getGameTime() + config.persistMinutes * 1200L));

        if (config.glow) glow(mob, rarity, level);
        announce(mob, rarity, level, config);
        EliteTracker.track(mob);
        EliteEnemies.LOGGER.debug("{} elite {} at {}", rarity.id(), mob.getType().getDescriptionId(), mob.blockPosition());
    }

    private static void boost(Mob mob, Holder<Attribute> attribute, double amount, AttributeModifier.Operation operation) {
        AttributeInstance instance = mob.getAttribute(attribute);
        if (instance == null) return; // a creeper has no attack damage, for one
        AttributeModifier modifier = Compat.modifier(MODIFIER, amount, operation);
        if (instance.getModifier(modifier.id()) == null) instance.addPermanentModifier(modifier);
    }

    /** The piece, named, coloured, enchanted past vanilla's limits and marked with the rarity's stars. */
    static ItemStack gear(Loot.Entry entry, Rarity rarity, ServerLevel level) {
        ItemStack stack = ItemIds.stack(entry.item(), 1);
        stack.set(DataComponents.CUSTOM_NAME, Component.literal(entry.name())
            .withStyle(s -> s.withItalic(false).withColor(TextColor.fromRgb(entry.colour()))));
        stack.set(DataComponents.LORE, new ItemLore(List.of(
            Component.literal(rarity.starText()).withStyle(s -> s.withItalic(false).withColor(colour(rarity))))));
        for (Loot.Enchant enchant : entry.enchants()) {
            Holder<Enchantment> holder = Compat.enchantment(level, enchant.id());
            if (holder == null) {
                EliteEnemies.LOGGER.warn("Unknown enchantment {} on {}", enchant.id(), entry.name());
                continue;
            }
            stack.enchant(holder, Loot.level(enchant, level.getRandom().nextInt(enchant.max() - enchant.min() + 1)));
        }
        return stack;
    }

    /**
     * Armour goes where it is worn. A held piece goes in the main hand, unless the mob already
     * shoots with what it holds and the piece would not: then the off hand, so a skeleton keeps
     * its bow while carrying its sword.
     */
    static EquipmentSlot slotFor(Loot.Slot slot, ItemStack stack, Mob mob) {
        String type = String.valueOf(BuiltInRegistries.ENTITY_TYPE.getKey(mob.getType()));
        return switch (slot) {
            case HEAD -> EquipmentSlot.HEAD;
            case CHEST -> EquipmentSlot.CHEST;
            case LEGS -> EquipmentSlot.LEGS;
            case FEET -> EquipmentSlot.FEET;
            case OFFHAND, HAND -> {
                // A witch overwrites her main hand with every potion she drinks: the piece would vanish.
                if (type.equals("minecraft:witch")) yield EquipmentSlot.OFFHAND;
                // A piglin drops whatever is in its off hand to admire gold: the piece for one ingot.
                if (type.equals("minecraft:piglin")) yield EquipmentSlot.MAINHAND;
                if (slot == Loot.Slot.OFFHAND) yield EquipmentSlot.OFFHAND;
                yield ranged(mob.getMainHandItem()) && !ranged(stack) ? EquipmentSlot.OFFHAND : EquipmentSlot.MAINHAND;
            }
        };
    }

    private static boolean ranged(ItemStack stack) {
        return stack.getItem() instanceof BowItem || stack.getItem() instanceof CrossbowItem || stack.getItem() instanceof TridentItem;
    }

    static ChatFormatting colour(Rarity rarity) {
        return ChatFormatting.valueOf(rarity.chatColour);
    }

    /** Glowing through walls, in the rarity's colour: vanilla takes the outline colour from the team. */
    private static void glow(Mob mob, Rarity rarity, ServerLevel level) {
        mob.addEffect(new MobEffectInstance(MobEffects.GLOWING, MobEffectInstance.INFINITE_DURATION, 0, false, false));
        Scoreboard scoreboard = level.getServer().getScoreboard();
        String name = EliteEnemies.MOD_ID + "_" + rarity.id();
        PlayerTeam team = scoreboard.getPlayerTeam(name);
        if (team == null) {
            team = scoreboard.addPlayerTeam(name);
            Compat.teamColour(team, rarity.chatColour);
        }
        scoreboard.addPlayerToTeam(mob.getScoreboardName(), team);
    }

    /** Chat line, sound and beam for the players around. */
    private static void announce(Mob mob, Rarity rarity, ServerLevel level, Config config) {
        Rarity threshold = config.announceThreshold();
        boolean chat = threshold != null && rarity.ordinal() >= threshold.ordinal();
        if (!chat && !config.beam) return;
        double r2 = (double) config.announceRadius * config.announceRadius;
        for (ServerPlayer player : level.players()) {
            double d2 = player.distanceToSqr(mob);
            if (d2 > r2) continue;
            if (chat) {
                MutableComponent line = Component.literal(Messages.announcePrefix(rarity)).withStyle(colour(rarity))
                    .append(mob.getType().getDescription().copy().withStyle(ChatFormatting.WHITE))
                    .append(Component.literal(Messages.announceSuffix((int) Math.sqrt(d2))).withStyle(ChatFormatting.GRAY));
                player.sendSystemMessage(line);
                if (config.sound) {
                    level.playSound((Player) null, player.getX(), player.getY(), player.getZ(),
                        SoundEvents.WITHER_SPAWN, SoundSource.HOSTILE, 0.4F, 1.8F);
                }
            }
            if (config.beam) Compat.beam(level, player, mob.getX(), mob.getY() + 1, mob.getZ(), rarity.beamRgb);
        }
    }

    /** For the self-test: forget the cooldowns. */
    static void resetGate() {
        GATE.reset();
    }
}
