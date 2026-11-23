package net.emeraude.eliteenemies.core;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Server configuration, read from {@code config/eliteenemies.json}. Missing keys keep their
 * default. Most switches are also set from {@code /eliteenemies settings}.
 */
public final class Config {

    /** Master switch. */
    public boolean enabled = true;
    /** Chance, in percent, that an eligible mob spawn becomes an elite (outside the cooldown). */
    public double chancePercent = 1.0;
    /** After an elite appears, no other one anywhere for this long. */
    public int cooldownSeconds = 60;
    /** After an elite appears, no other one within this many blocks... */
    public int protectedRadius = 24;
    /** ...for this long. */
    public int protectedMinutes = 10;
    /** An elite does not despawn for this long after it appears, so players can reach it. */
    public int persistMinutes = 5;
    /**
     * Only becomes an elite when the nearest player is at most this many blocks above or below
     * (no more elites at the bottom of a cave nobody can reach). 0 turns the check off.
     */
    public int maxHeightGap = 24;
    /** Mobs from spawners and trial spawners may become elites (off: no farming elites). */
    public boolean allowSpawners = false;

    /** Rarity weights, in order: common, uncommon, rare, legendary. */
    public int weightCommon = 50;
    public int weightUncommon = 30;
    public int weightRare = 9;
    public int weightLegendary = 2;

    /** Announce in chat from this rarity up (COMMON, UNCOMMON, RARE, LEGENDARY), or NONE. */
    public String announceFrom = "RARE";
    /** Players within this many blocks get the announcement, the sound and the beam. */
    public int announceRadius = 128;
    public boolean sound = true;
    /** A short column of particles above the elite when it appears. */
    public boolean beam = true;
    /** Elites glow through walls, in the colour of their rarity. */
    public boolean glow = false;
    /** A boss bar with the elite's name and health, for players close to it. */
    public boolean bossBar = true;
    public int bossBarRange = 32;

    /** Entity types that may become elites (ids, "minecraft:zombie"). Mods' mobs can be added. */
    public List<String> mobs = new ArrayList<>(DEFAULT_MOBS);

    /** The 24 types of Mob Captains, plus the skeleton family it reached through a tag. */
    public static final List<String> DEFAULT_MOBS = List.of(
        "minecraft:skeleton", "minecraft:stray", "minecraft:wither_skeleton", "minecraft:bogged",
        "minecraft:zombie", "minecraft:zombie_villager", "minecraft:drowned", "minecraft:husk",
        "minecraft:creeper", "minecraft:blaze", "minecraft:enderman", "minecraft:guardian",
        "minecraft:shulker", "minecraft:silverfish", "minecraft:spider", "minecraft:cave_spider",
        "minecraft:evoker", "minecraft:vindicator", "minecraft:witch", "minecraft:zombified_piglin",
        "minecraft:piglin", "minecraft:piglin_brute", "minecraft:magma_cube", "minecraft:slime",
        "minecraft:hoglin", "minecraft:zoglin", "minecraft:ravager");

    public int[] weights() {
        return new int[]{weightCommon, weightUncommon, weightRare, weightLegendary};
    }

    /** The lowest rarity announced, or null for none. */
    public Rarity announceThreshold() {
        return Rarity.parse(announceFrom);
    }

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    /** Reads the file, writing it with every default filled in when it is missing or incomplete. */
    public static Config load(Path file) throws IOException {
        Config config = null;
        if (Files.exists(file)) {
            try {
                config = GSON.fromJson(Files.readString(file, StandardCharsets.UTF_8), Config.class);
            } catch (JsonParseException e) {
                throw new IOException("unreadable " + file + ": " + e.getMessage(), e);
            }
        }
        if (config == null) config = new Config();
        if (config.mobs == null) config.mobs = new ArrayList<>(DEFAULT_MOBS);
        if (config.announceFrom == null) config.announceFrom = "RARE";
        config.save(file);
        return config;
    }

    public void save(Path file) throws IOException {
        Files.createDirectories(file.toAbsolutePath().getParent());
        Files.writeString(file, GSON.toJson(this), StandardCharsets.UTF_8);
    }
}
