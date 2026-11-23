package net.emeraude.eliteenemies.core;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * When an elite may appear: not during the global cooldown after the last one, and not inside the
 * protected area around a recent one. Times are game ticks. Kept in memory only: a restart simply
 * lifts the limits, which is harmless.
 */
public final class SpawnGate {

    private record Zone(String dimension, double x, double z, long until) {}

    private final List<Zone> zones = new ArrayList<>();
    private long cooldownUntil = Long.MIN_VALUE;

    /** True when nothing forbids an elite at this place and time. */
    public boolean open(long now, String dimension, double x, double z, int radius) {
        if (now < cooldownUntil) return false;
        double r2 = (double) radius * radius;
        for (Iterator<Zone> it = zones.iterator(); it.hasNext(); ) {
            Zone zone = it.next();
            if (now >= zone.until) {
                it.remove();
                continue;
            }
            if (zone.dimension.equals(dimension)) {
                double dx = zone.x - x, dz = zone.z - z;
                if (dx * dx + dz * dz <= r2) return false;
            }
        }
        return true;
    }

    /** Records an elite that just appeared. */
    public void record(long now, String dimension, double x, double z, int cooldownTicks, int protectedTicks) {
        cooldownUntil = now + Math.max(0, cooldownTicks);
        if (protectedTicks > 0) zones.add(new Zone(dimension, x, z, now + protectedTicks));
    }

    public void reset() {
        zones.clear();
        cooldownUntil = Long.MIN_VALUE;
    }
}
