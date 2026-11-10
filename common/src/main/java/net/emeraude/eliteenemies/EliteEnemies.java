package net.emeraude.eliteenemies;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Loader-agnostic entry point — TIER A (server-side) logic goes here.
 *
 * <p>Both loader modules compile this source in directly and call {@link #init()}. Anything that
 * runs on a dedicated server belongs in this class or below it; nothing here may touch client-only
 * Minecraft classes, or a dedicated server will crash on class load.
 */
public final class EliteEnemies {

    public static final String MOD_ID = "eliteenemies";
    public static final Logger LOGGER = LoggerFactory.getLogger("Elite Enemies");

    private static boolean initialized = false;

    private EliteEnemies() {}

    /** Called once from each loader's entry point, as early as possible. */
    public static synchronized void init() {
        if (initialized) return;
        initialized = true;

        LOGGER.info("{} loaded", MOD_ID);
    }
}
