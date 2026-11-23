package net.emeraude.eliteenemies.compat;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.BossEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.enchantment.Enchantment;
import org.joml.Vector3f;

import net.minecraft.ChatFormatting;
import net.minecraft.world.scores.PlayerTeam;

import java.util.Set;

/** The few calls whose shape differs between 1.21.1 and 26.x. This is the 1.21.1 side. */
@SuppressWarnings("deprecation") // Registry.wrapAsHolder: still the one call shared with 26.x
public final class Compat {

    /** The spawn reason of a spawn egg. */
    public static final String SPAWN_EGG = "SPAWN_EGG";

    private Compat() {}

    public static boolean hasPermission(CommandSourceStack source, int level) {
        return source.hasPermission(level);
    }

    public static String dimensionId(ServerLevel level) {
        return level.dimension().location().toString();
    }

    public static Set<String> tags(Entity entity) {
        return entity.getTags();
    }

    public static AttributeModifier modifier(String id, double amount, AttributeModifier.Operation operation) {
        return new AttributeModifier(ResourceLocation.parse(id), amount, operation);
    }

    /** The enchantment with this id in the server's registry, or null. */
    public static Holder<Enchantment> enchantment(ServerLevel level, String id) {
        Registry<Enchantment> registry = level.registryAccess().registryOrThrow(Registries.ENCHANTMENT);
        for (Enchantment enchantment : registry) {
            if (String.valueOf(registry.getKey(enchantment)).equals(id)) return registry.wrapAsHolder(enchantment);
        }
        return null;
    }

    public static ServerBossEvent bossEvent(Component name, BossEvent.BossBarColor colour) {
        return new ServerBossEvent(name, colour, BossEvent.BossBarOverlay.PROGRESS);
    }

    /** A coloured column of dust above x, y, z, sent to one player whatever their particle setting. */
    public static void beam(ServerLevel level, ServerPlayer player, double x, double y, double z, int rgb) {
        Vector3f colour = new Vector3f(((rgb >> 16) & 0xFF) / 255F, ((rgb >> 8) & 0xFF) / 255F, (rgb & 0xFF) / 255F);
        DustParticleOptions dust = new DustParticleOptions(colour, 3.0F);
        for (int i = 0; i < 48; i++) {
            level.sendParticles(player, dust, true, x, y + i, z, 2, 0.15, 0.4, 0.15, 0.0);
        }
    }

    /** Team colour, which is also the colour of the glowing outline. 1.21.1: a ChatFormatting. */
    public static void teamColour(PlayerTeam team, String name) {
        team.setColor(ChatFormatting.valueOf(name));
    }

    public static Entity create(EntityType<?> type, ServerLevel level) {
        return type.create(level);
    }

    /** What a spawner does: prepare the mob for this spawn reason, then add it to the world. */
    public static boolean spawnAs(ServerLevel level, Mob mob, String reason) {
        mob.finalizeSpawn(level, level.getCurrentDifficultyAt(mob.blockPosition()), MobSpawnType.valueOf(reason), null);
        return level.addFreshEntity(mob);
    }

    public static void kill(LivingEntity entity, ServerLevel level) {
        entity.kill();
    }
}
