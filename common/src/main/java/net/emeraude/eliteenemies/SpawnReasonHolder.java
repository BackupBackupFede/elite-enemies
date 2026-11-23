package net.emeraude.eliteenemies;

/** Added to every Mob by a mixin: why it was last spawned, read once. */
public interface SpawnReasonHolder {

    /** The spawn reason's name ("NATURAL", "SPAWNER"...), then forgets it; null when unknown. */
    String eliteenemies$takeReason();
}
