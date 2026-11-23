package net.emeraude.eliteenemies;

import com.mojang.authlib.GameProfile;
import net.emeraude.eliteenemies.compat.Click;
import net.emeraude.eliteenemies.compat.ClickRelayMenu;
import net.emeraude.eliteenemies.compat.Compat;
import net.emeraude.eliteenemies.core.Config;
import net.emeraude.eliteenemies.core.Loot;
import net.emeraude.eliteenemies.core.Rarity;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ClientInformation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.levelgen.Heightmap;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

/**
 * Behaviour check for a dedicated server, off unless the environment variable
 * {@code ELITEENEMIES_SELFTEST=true} is set. It spawns real mobs through the real path
 * (finalizeSpawn with a spawn reason, then addFreshEntity), so the mixins are exercised, checks
 * the outcome, and stops the server.
 *
 * <p>The witness replays the very same natural spawn with the mod switched off: if that still
 * makes an elite, the check measures nothing.
 */
public final class EliteEnemiesSelfTest {

    private static final String SWITCH = EliteEnemies.MOD_ID.toUpperCase(Locale.ROOT).replaceAll("[^A-Z0-9]", "") + "_SELFTEST";

    private static final List<Entity> SEEN = new ArrayList<>();
    private static boolean recording;

    private EliteEnemiesSelfTest() {}

    /** Called for every entity added to a level; keeps it only while a check is recording. */
    public static void saw(Entity entity) {
        if (recording) SEEN.add(entity);
    }

    public static boolean requested() {
        return "true".equalsIgnoreCase(System.getenv(SWITCH));
    }

    public static void run(MinecraftServer server) {
        List<String> failures = new ArrayList<>();
        Config original = EliteEnemies.config();
        try {
            ServerLevel level = server.overworld();
            check(failures, "commands", server.getCommands().getDispatcher().getRoot().getChild(EliteEnemies.MOD_ID) != null
                ? null : "/eliteenemies is not registered");
            check(failures, "loot resolves", lootResolves(level));
            check(failures, "natural spawn", naturalSpawn(level));
            check(failures, "drops its gear", dropsItsGear(level));
            check(failures, "damage bonus", damageBonus(level));
            check(failures, "witch and piglin hands", hands(level));
            check(failures, "cooldown", cooldown(level));
            check(failures, "spawners", spawners(level));
            check(failures, "other mobs", otherMobs(level));
            check(failures, "settings menu", settingsMenu(server, level));
            check(failures, "witness", witness(level));
        } catch (Throwable t) {
            EliteEnemies.LOGGER.error("[SELFTEST] crashed", t);
            failures.add("crash: " + t);
        } finally {
            EliteEnemies.useConfig(original);
            EliteEnemies.saveConfig();
        }
        EliteEnemies.LOGGER.info("[SELFTEST] RESULT {}", failures.isEmpty() ? "PASS" : "FAIL " + failures);
        server.halt(false);
    }

    private static void check(List<String> failures, String name, String error) {
        if (error == null) {
            EliteEnemies.LOGGER.info("[SELFTEST] {} OK", name);
        } else {
            EliteEnemies.LOGGER.error("[SELFTEST] {} FAILED: {}", name, error);
            failures.add(name + ": " + error);
        }
    }

    /** Every elite on every spawn, no cooldown, no height limit: the checks decide, not luck. */
    private static Config always() {
        Config config = new Config();
        config.chancePercent = 100;
        config.cooldownSeconds = 0;
        config.protectedMinutes = 0;
        config.maxHeightGap = 0;
        return config;
    }

    /** A mob of this type spawned for this reason at the world origin, through the real path. */
    private static Mob spawn(ServerLevel level, EntityType<?> type, String reason) {
        Elites.resetGate();
        level.getChunk(0, 0);
        Entity entity = Compat.create(type, level);
        if (!(entity instanceof Mob mob)) throw new IllegalStateException(type + " is not a mob");
        mob.setPos(0.5, level.getHeight(Heightmap.Types.MOTION_BLOCKING, 0, 0), 0.5);
        if (!Compat.spawnAs(level, mob, reason)) throw new IllegalStateException("could not add " + type);
        return mob;
    }

    /** Every piece of every rarity exists in this version, with all its enchantments. */
    private static String lootResolves(ServerLevel level) {
        for (Rarity rarity : Rarity.values()) {
            for (Loot.Entry entry : Loot.of(rarity)) {
                ItemStack stack = Elites.gear(entry, rarity, level);
                ItemEnchantments enchantments = stack.get(DataComponents.ENCHANTMENTS);
                int count = enchantments == null ? 0 : enchantments.size();
                if (count != entry.enchants().size()) return entry.name() + " has " + count + " enchantments, expected " + entry.enchants().size();
            }
        }
        return null;
    }

    private static String naturalSpawn(ServerLevel level) {
        EliteEnemies.useConfig(always());
        Mob mob = spawn(level, ItemIds.entity("minecraft:skeleton"), "NATURAL");
        try {
            Rarity rarity = Elites.rarityOf(mob);
            if (rarity == null) return "a natural skeleton at 100 % did not become an elite";
            if (mob.getMaxHealth() <= 20.0F) return "max health still " + mob.getMaxHealth();
            if (mob.getHealth() < mob.getMaxHealth()) return "not healed to its new max";
            if (mob.getCustomName() == null || !mob.getCustomName().getString().startsWith(rarity.starText())) return "no star name";
            if (!Elites.protectedFromDespawn(mob)) return "not protected from despawning";
            if (!EliteTracker.isTracked(mob)) return "no boss bar";
            if (!carriesGear(mob)) return "carries no elite gear";
            return null;
        } finally {
            mob.discard();
        }
    }

    private static boolean carriesGear(Mob mob) {
        Set<String> names = gearNames();
        for (ItemStack stack : List.of(mob.getMainHandItem(), mob.getOffhandItem(),
            mob.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.HEAD), mob.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.CHEST),
            mob.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.LEGS), mob.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.FEET))) {
            if (stack.has(DataComponents.CUSTOM_NAME) && names.contains(stack.getHoverName().getString())) return true;
        }
        return false;
    }

    private static Set<String> gearNames() {
        Set<String> names = new HashSet<>();
        for (Rarity rarity : Rarity.values()) for (Loot.Entry entry : Loot.of(rarity)) names.add(entry.name());
        return names;
    }

    /** Killed by anything, an elite drops its piece, undamaged. */
    private static String dropsItsGear(ServerLevel level) {
        EliteEnemies.useConfig(always());
        Mob mob = spawn(level, ItemIds.entity("minecraft:zombie"), "NATURAL");
        if (Elites.rarityOf(mob) == null) return "no elite to kill";
        if (mob.isBaby()) return "a baby elite would drop nothing";
        String carried = describeGear(mob);
        // Since 1.21.9 there are no spawn chunks: the origin is not entity-ticking, so a search
        // around the mob finds nothing. Record what enters the world while it dies instead.
        SEEN.clear();
        recording = true;
        try {
            Compat.kill(mob, level);
        } finally {
            recording = false;
        }
        Set<String> names = gearNames();
        List<ItemEntity> found = new ArrayList<>();
        for (Entity entity : SEEN) if (entity instanceof ItemEntity item) found.add(item);
        found.forEach(Entity::discard);
        for (ItemEntity item : found) {
            ItemStack stack = item.getItem();
            if (stack.has(DataComponents.CUSTOM_NAME) && names.contains(stack.getHoverName().getString())) {
                boolean intact = !stack.isDamageableItem() || stack.getDamageValue() == 0;
                return intact ? null : stack.getHoverName().getString() + " dropped damaged";
            }
        }
        return "nothing named dropped (carried " + carried + "; dead " + mob.isDeadOrDying() + ", removed " + mob.isRemoved()
            + ", health " + mob.getHealth() + ", " + found.size() + " items: "
            + found.stream().map(i -> i.getItem().getHoverName().getString()).toList() + ")";
    }

    private static String describeGear(Mob mob) {
        List<String> parts = new ArrayList<>();
        for (net.minecraft.world.entity.EquipmentSlot slot : net.minecraft.world.entity.EquipmentSlot.values()) {
            ItemStack stack = mob.getItemBySlot(slot);
            if (!stack.isEmpty()) parts.add(slot.getName() + "=" + stack.getHoverName().getString());
        }
        return String.join(", ", parts);
    }

    /** A legendary zombie gets +9 damage (3 base x 3.0), flat: the weapon it holds is not multiplied. */
    private static String damageBonus(ServerLevel level) {
        Config config = always();
        config.weightCommon = config.weightUncommon = config.weightRare = 0;
        EliteEnemies.useConfig(config);
        Mob mob = spawn(level, ItemIds.entity("minecraft:zombie"), "NATURAL");
        try {
            if (Elites.rarityOf(mob) != Rarity.LEGENDARY) return "not a legendary: " + Elites.rarityOf(mob);
            var attack = mob.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_DAMAGE);
            for (var modifier : attack.getModifiers()) {
                if (!String.valueOf(modifier.id()).equals(EliteEnemies.MOD_ID + ":elite")) continue;
                if (modifier.operation() != net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADD_VALUE) return "bonus is " + modifier.operation();
                return Math.abs(modifier.amount() - 9.0) < 1e-6 ? null : "bonus is " + modifier.amount() + ", expected 9";
            }
            return "no damage bonus";
        } finally {
            mob.discard();
        }
    }

    /** Whatever the piece, a witch never holds it in her main hand and a piglin never in its off hand. */
    private static String hands(ServerLevel level) {
        EliteEnemies.useConfig(always());
        Set<String> names = gearNames();
        for (int i = 0; i < 40; i++) {
            for (String type : new String[]{"minecraft:witch", "minecraft:piglin"}) {
                Mob mob = spawn(level, ItemIds.entity(type), "NATURAL");
                try {
                    if (Elites.rarityOf(mob) == null) return type + " did not become an elite";
                    ItemStack wrong = type.endsWith("witch") ? mob.getMainHandItem() : mob.getOffhandItem();
                    if (wrong.has(DataComponents.CUSTOM_NAME) && names.contains(wrong.getHoverName().getString())) {
                        return type + " holds " + wrong.getHoverName().getString() + " in the wrong hand";
                    }
                    if (!carriesGear(mob)) return type + " carries no gear";
                } finally {
                    mob.discard();
                }
            }
        }
        return null;
    }

    /** With the default 60 s cooldown, a second spawn right after the first is a plain mob. */
    private static String cooldown(ServerLevel level) {
        Config config = always();
        config.cooldownSeconds = 60;
        EliteEnemies.useConfig(config);
        Mob first = spawn(level, ItemIds.entity("minecraft:zombie"), "NATURAL");
        Entity entity = Compat.create(ItemIds.entity("minecraft:zombie"), level);
        Mob second = (Mob) entity;
        second.setPos(first.getX() + 200, first.getY(), first.getZ());
        Compat.spawnAs(level, second, "NATURAL");
        try {
            if (Elites.rarityOf(first) == null) return "the first one is not an elite";
            return Elites.rarityOf(second) == null ? null : "the second one is an elite during the cooldown";
        } finally {
            first.discard();
            second.discard();
        }
    }

    /** Spawners are off by default, and on when allowed. */
    private static String spawners(ServerLevel level) {
        EliteEnemies.useConfig(always());
        Mob blocked = spawn(level, ItemIds.entity("minecraft:zombie"), "SPAWNER");
        boolean blockedIsElite = Elites.rarityOf(blocked) != null;
        blocked.discard();
        if (blockedIsElite) return "a spawner zombie became an elite with spawners off";
        Config allow = always();
        allow.allowSpawners = true;
        EliteEnemies.useConfig(allow);
        Mob allowed = spawn(level, ItemIds.entity("minecraft:zombie"), "SPAWNER");
        boolean allowedIsElite = Elites.rarityOf(allowed) != null;
        allowed.discard();
        if (!allowedIsElite) return "a spawner zombie did not become an elite with spawners on";
        EliteEnemies.useConfig(always());
        Mob egg = spawn(level, ItemIds.entity("minecraft:zombie"), Compat.SPAWN_EGG);
        boolean eggIsElite = Elites.rarityOf(egg) != null;
        egg.discard();
        return eggIsElite ? "a spawn-egg zombie became an elite" : null;
    }

    /** A pig is not on the list. */
    private static String otherMobs(ServerLevel level) {
        EliteEnemies.useConfig(always());
        Mob pig = spawn(level, ItemIds.entity("minecraft:pig"), "NATURAL");
        boolean elite = Elites.rarityOf(pig) != null;
        pig.discard();
        return elite ? "a pig became an elite" : null;
    }

    /** A click on the master switch turns it off and saves; a second click turns it back on. */
    private static String settingsMenu(MinecraftServer server, ServerLevel level) {
        EliteEnemies.useConfig(always());
        ServerPlayer player = player(server, level);
        SettingsMenu menu = new SettingsMenu(1, player);
        int slot = SettingsMenu.slotOf(SettingsMenu.Setting.ENABLED);
        ClickRelayMenu.click(menu, slot, 0, Click.PICKUP, player);
        if (EliteEnemies.config().enabled) return "the click did not switch the mod off";
        if (!menu.shown(slot + 9).getHoverName().getString().equals("Off")) return "the pane does not say Off";
        ClickRelayMenu.click(menu, slot + 9, 0, Click.PICKUP, player);
        if (!EliteEnemies.config().enabled) return "the pane click did not switch it back on";
        int chance = SettingsMenu.slotOf(SettingsMenu.Setting.CHANCE);
        ClickRelayMenu.click(menu, chance, 0, Click.PICKUP, player);
        if (EliteEnemies.config().chancePercent != 0.5) return "chance after 100 % should wrap to 0.5 %, is " + EliteEnemies.config().chancePercent;
        if (!player.containerMenu.getCarried().isEmpty()) return "an icon stuck to the cursor";
        return null;
    }

    /** The same natural spawn as above, mod off: no elite, or the checks measure nothing. */
    private static String witness(ServerLevel level) {
        Config off = always();
        off.enabled = false;
        EliteEnemies.useConfig(off);
        Mob mob = spawn(level, ItemIds.entity("minecraft:skeleton"), "NATURAL");
        boolean elite = Elites.rarityOf(mob) != null;
        mob.discard();
        return elite ? "mod off, a natural skeleton still became an elite" : null;
    }

    /**
     * A player with a connection that has no channel. Vanilla sends packets on the way, and an
     * unopened Connection simply queues them instead of throwing.
     */
    private static ServerPlayer player(MinecraftServer server, ServerLevel level) {
        GameProfile profile = new GameProfile(UUID.randomUUID(), "selftest");
        ServerPlayer player = new ServerPlayer(server, level, profile, ClientInformation.createDefault());
        new ServerGamePacketListenerImpl(server, new Connection(PacketFlow.SERVERBOUND), player, CommonListenerCookie.createInitial(profile, false));
        player.getInventory().clearContent();
        return player;
    }
}
