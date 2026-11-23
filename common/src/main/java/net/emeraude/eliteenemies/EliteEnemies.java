package net.emeraude.eliteenemies;

import net.emeraude.eliteenemies.core.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Path;

/**
 * Loader-agnostic entry point — TIER A (server-side) logic goes here.
 *
 * <p>Both loader modules compile this source in directly and call {@link #init(Path)}. Anything
 * that runs on a dedicated server belongs in this class or below it; nothing here may touch
 * client-only Minecraft classes, or a dedicated server will crash on class load.
 */
public final class EliteEnemies {

    public static final String MOD_ID = "eliteenemies";
    public static final Logger LOGGER = LoggerFactory.getLogger("Elite Enemies");

    private static boolean initialized = false;
    private static Path configDir = Path.of("config");
    private static Config config = new Config();

    private EliteEnemies() {}

    /** Called once from each loader's entry point, as early as possible, with the loader's config folder. */
    public static synchronized void init(Path configDir) {
        if (initialized) return;
        initialized = true;
        EliteEnemies.configDir = configDir;
        reloadConfig();
        LOGGER.info("{} loaded", MOD_ID);
    }

    public static Config config() {
        return config;
    }

    /** Re-reads {@code config/eliteenemies.json}. A broken file keeps the previous settings. */
    public static String reloadConfig() {
        try {
            config = Config.load(configDir.resolve(MOD_ID + ".json"));
            return null;
        } catch (IOException e) {
            LOGGER.error("Could not read the config, keeping the previous settings", e);
            return e.getMessage();
        }
    }

    /** Writes the current settings back; returns why it failed, or null. */
    public static String saveConfig() {
        try {
            config.save(configDir.resolve(MOD_ID + ".json"));
            return null;
        } catch (IOException e) {
            LOGGER.error("Could not save the config", e);
            return e.getMessage();
        }
    }

    /** For the self-test only. */
    static void useConfig(Config replacement) {
        config = replacement;
    }
}
