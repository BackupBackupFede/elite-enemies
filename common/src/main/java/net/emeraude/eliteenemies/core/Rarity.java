package net.emeraude.eliteenemies.core;

/**
 * The four ranks of an elite. Stat bonuses follow Mob Captains (PuckiSilver, MIT), whose balance
 * players already know: health and damage are multiplied on the total, armour and knockback
 * resistance are added.
 *
 * <p>Colours are named after Minecraft's {@code ChatFormatting} constants and boss-bar colours,
 * as strings, so this class stays free of Minecraft.
 */
public enum Rarity {
    //          stars health armour speed damage knockback colour         bar       beam
    COMMON(    1,    0.5,   2,     0.15, 0.7,   0.03,     "GRAY",         "WHITE",  0xFFFFFF),
    UNCOMMON(  2,    0.9,   4,     0.25, 1.2,   0.10,     "GREEN",        "GREEN",  0x55FF55),
    RARE(      3,    1.8,   8,     0.40, 2.1,   0.50,     "LIGHT_PURPLE", "PURPLE", 0xB400FF),
    LEGENDARY( 4,    2.7,   15,    0.60, 3.0,   1.00,     "GOLD",         "YELLOW", 0xFFAA00);

    public final int stars;
    /** Added as "multiplied total": 0.5 means 1.5 times the health. */
    public final double health;
    public final double armour;
    public final double speed;
    public final double damage;
    public final double knockback;
    public final String chatColour;
    public final String barColour;
    public final int beamRgb;

    Rarity(int stars, double health, double armour, double speed, double damage, double knockback,
           String chatColour, String barColour, int beamRgb) {
        this.stars = stars;
        this.health = health;
        this.armour = armour;
        this.speed = speed;
        this.damage = damage;
        this.knockback = knockback;
        this.chatColour = chatColour;
        this.barColour = barColour;
        this.beamRgb = beamRgb;
    }

    /** "★★★" for a rare: what shows above the mob, readable in every language. */
    public String starText() {
        return "★".repeat(stars);
    }

    /** Lower-case name, used in entity tags and the config. */
    public String id() {
        return name().toLowerCase(java.util.Locale.ROOT);
    }

    /** The rarity with this name, any case, or null. */
    public static Rarity parse(String name) {
        if (name == null) return null;
        for (Rarity rarity : values()) if (rarity.name().equalsIgnoreCase(name.trim())) return rarity;
        return null;
    }

    /**
     * Picks a rarity from weights, given a draw in {@code [0, total)}. Weights are in rarity order;
     * a zero or negative weight never wins. Returns null when every weight is zero.
     */
    public static Rarity pick(int[] weights, int draw) {
        int left = draw;
        for (Rarity rarity : values()) {
            int weight = Math.max(0, weights[rarity.ordinal()]);
            if (left < weight) return rarity;
            left -= weight;
        }
        return null;
    }

    public static int total(int[] weights) {
        int total = 0;
        for (int weight : weights) total += Math.max(0, weight);
        return total;
    }
}
