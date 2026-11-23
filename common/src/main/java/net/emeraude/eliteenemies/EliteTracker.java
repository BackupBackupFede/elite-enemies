package net.emeraude.eliteenemies;

import net.emeraude.eliteenemies.compat.Compat;
import net.emeraude.eliteenemies.core.Rarity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.BossEvent;
import net.minecraft.world.entity.Mob;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

/**
 * One vanilla boss bar per living elite, shown to the players close to it: its name, its rarity's
 * colour and its health. This is what makes an elite findable and readable on a vanilla client.
 */
public final class EliteTracker {

    private record Tracked(Mob mob, ServerBossEvent bar) {}

    private static final Map<UUID, Tracked> TRACKED = new HashMap<>();
    private static int ticks;

    private EliteTracker() {}

    /** Starts following an elite; harmless if it is already followed. */
    public static void track(Mob mob) {
        if (!EliteEnemies.config().bossBar || TRACKED.containsKey(mob.getUUID())) return;
        Rarity rarity = Elites.rarityOf(mob);
        if (rarity == null) return;
        ServerBossEvent bar = Compat.bossEvent(mob.getDisplayName(), BossEvent.BossBarColor.valueOf(rarity.barColour));
        TRACKED.put(mob.getUUID(), new Tracked(mob, bar));
    }

    public static void tick(MinecraftServer server) {
        if (++ticks % 10 != 0 || TRACKED.isEmpty()) return;
        boolean enabled = EliteEnemies.config().bossBar;
        int range = EliteEnemies.config().bossBarRange;
        double r2 = (double) range * range;
        for (Iterator<Tracked> it = TRACKED.values().iterator(); it.hasNext(); ) {
            Tracked t = it.next();
            Mob mob = t.mob;
            if (!enabled || mob.isRemoved() || !mob.isAlive()) {
                t.bar.removeAllPlayers();
                it.remove();
                continue;
            }
            t.bar.setProgress(Math.max(0F, Math.min(1F, mob.getHealth() / mob.getMaxHealth())));
            for (ServerPlayer player : new ArrayList<>(t.bar.getPlayers())) {
                if (player.isRemoved() || player.level() != mob.level() || player.distanceToSqr(mob) > r2) t.bar.removePlayer(player);
            }
            for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                if (player.level() == mob.level() && player.distanceToSqr(mob) <= r2 && !t.bar.getPlayers().contains(player)) {
                    t.bar.addPlayer(player);
                }
            }
        }
    }

    /** For the self-test. */
    static boolean isTracked(Mob mob) {
        return TRACKED.containsKey(mob.getUUID());
    }
}
