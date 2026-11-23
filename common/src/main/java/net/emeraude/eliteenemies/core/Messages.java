package net.emeraude.eliteenemies.core;

/**
 * Chat text, English only. The mob's own name stays a vanilla translation, filled in by the
 * client.
 */
public final class Messages {

    private Messages() {}

    public static String rarityName(Rarity rarity) {
        return switch (rarity) {
            case COMMON -> "Common";
            case UNCOMMON -> "Uncommon";
            case RARE -> "Rare";
            case LEGENDARY -> "Legendary";
        };
    }

    /** What comes before the mob's name: "☠ Legendary elite nearby: ". */
    public static String announcePrefix(Rarity rarity) {
        return "☠ " + rarityName(rarity) + " elite nearby: ";
    }

    /** What comes after the mob's name: " (45 blocks)". */
    public static String announceSuffix(int distance) {
        return " (" + distance + " blocks)";
    }
}
