package net.emeraude.eliteenemies.core;

import java.util.List;

/**
 * What an elite carries, and drops when it dies. Every piece is worn or held, so the player sees
 * the reward before the fight. Enchantments go past vanilla's limits (and combine where vanilla
 * forbids it) but never above X, the highest level a vanilla client can name.
 *
 * <p>Several names come from Mob Captains (PuckiSilver, MIT): Riot Shield, Knockback Stick, Greed,
 * Match, Cactus, Gem Magnet, Long Fall Boots, Bulletproof Vest, Apocalypse, Winter Flippers,
 * Diving Helmet, Multiplier, Ripper, Mjölnir.
 */
public final class Loot {

    /** Where the piece goes. HAND picks the main or the off hand depending on the mob. */
    public enum Slot { HEAD, CHEST, LEGS, FEET, HAND, OFFHAND }

    public record Enchant(String id, int min, int max) {}

    public record Entry(String item, String name, int colour, Slot slot, List<Enchant> enchants) {}

    private static Enchant e(String id, int min, int max) {
        return new Enchant("minecraft:" + id, min, max);
    }

    private static Entry entry(String item, String name, int colour, Slot slot, Enchant... enchants) {
        return new Entry("minecraft:" + item, name, colour, slot, List.of(enchants));
    }

    public static final List<Entry> COMMON = List.of(
        entry("shield", "Riot Shield", 0x5865F2, Slot.OFFHAND, e("unbreaking", 4, 5)),
        entry("stick", "Knockback Stick", 0xFF9D00, Slot.HAND, e("knockback", 3, 5)),
        entry("golden_axe", "Greed", 0xD99307, Slot.HAND, e("looting", 4, 5)),
        entry("leather_boots", "Featherweights", 0x00FF00, Slot.FEET, e("feather_falling", 5, 6)),
        entry("golden_chestplate", "Cactus", 0x30DE00, Slot.CHEST, e("thorns", 4, 5)),
        entry("wooden_sword", "Match", 0xC23511, Slot.HAND, e("fire_aspect", 3, 4)),
        entry("stone_axe", "Primitive Chainsaw", 0x9C8F8C, Slot.HAND, e("efficiency", 6, 7)),
        entry("golden_pickaxe", "Primitive Drill", 0xB0A8A6, Slot.HAND, e("efficiency", 6, 7)));

    public static final List<Entry> UNCOMMON = List.of(
        entry("iron_pickaxe", "Gem Magnet", 0xFF3333, Slot.HAND, e("fortune", 4, 4)),
        entry("iron_boots", "Long Fall Boots", 0x44D3E3, Slot.FEET, e("feather_falling", 6, 8)),
        entry("chainmail_chestplate", "Bulletproof Vest", 0xEDC266, Slot.CHEST, e("projectile_protection", 5, 6)),
        entry("iron_sword", "Apocalypse", 0xD90000, Slot.HAND, e("sharpness", 5, 6), e("smite", 5, 6)),
        entry("diamond_boots", "Winter Flippers", 0xABEDEB, Slot.FEET, e("frost_walker", 3, 3), e("depth_strider", 4, 4)),
        entry("turtle_helmet", "Diving Helmet", 0x21FF55, Slot.HEAD, e("respiration", 4, 5), e("aqua_affinity", 1, 1)),
        entry("crossbow", "Multiplier", 0x96FFCB, Slot.HAND, e("piercing", 5, 6), e("multishot", 1, 1)),
        entry("iron_leggings", "Sneaky Pants", 0x7F8C8D, Slot.LEGS, e("swift_sneak", 4, 5)));

    public static final List<Entry> RARE = List.of(
        entry("trident", "Ripper", 0x9B30C8, Slot.HAND, e("riptide", 4, 5), e("impaling", 6, 7)),
        entry("diamond_sword", "Vorpal Blade", 0x4FD8FF, Slot.HAND, e("sharpness", 6, 7), e("looting", 4, 4)),
        entry("bow", "Longshot", 0xC9A86B, Slot.HAND, e("power", 6, 8), e("punch", 3, 3)),
        entry("diamond_chestplate", "Bastion", 0x2E9FD8, Slot.CHEST, e("protection", 5, 6)),
        entry("diamond_helmet", "Iron Will", 0x8AB4F8, Slot.HEAD, e("protection", 5, 5), e("unbreaking", 5, 5)),
        entry("iron_axe", "Mjölnir", 0xFFF291, Slot.HAND, e("smite", 6, 8), e("sharpness", 4, 4)),
        entry("diamond_pickaxe", "Excavator", 0x5CE1E6, Slot.HAND, e("efficiency", 7, 8), e("fortune", 4, 4)));

    public static final List<Entry> LEGENDARY = List.of(
        entry("netherite_sword", "Kingslayer", 0xFFAA00, Slot.HAND, e("sharpness", 8, 10), e("fire_aspect", 3, 3), e("looting", 5, 5)),
        entry("netherite_chestplate", "Aegis", 0xFFD966, Slot.CHEST, e("protection", 7, 8), e("thorns", 4, 4)),
        entry("mace", "Earthshaker", 0xE67E22, Slot.HAND, e("density", 6, 8), e("breach", 4, 4)),
        entry("netherite_pickaxe", "World Eater", 0xF1C40F, Slot.HAND, e("efficiency", 8, 10), e("fortune", 5, 5)),
        entry("netherite_boots", "Seven-League Boots", 0xF39C12, Slot.FEET, e("feather_falling", 8, 8), e("depth_strider", 4, 4), e("soul_speed", 4, 4)),
        entry("bow", "Stormcaller", 0xFFE066, Slot.HAND, e("power", 8, 10), e("flame", 1, 1), e("infinity", 1, 1), e("mending", 1, 1)));

    private Loot() {}

    public static List<Entry> of(Rarity rarity) {
        return switch (rarity) {
            case COMMON -> COMMON;
            case UNCOMMON -> UNCOMMON;
            case RARE -> RARE;
            case LEGENDARY -> LEGENDARY;
        };
    }

    /** The piece for a draw in {@code [0, of(rarity).size())}: every piece of a rarity is equally likely. */
    public static Entry pick(Rarity rarity, int draw) {
        List<Entry> entries = of(rarity);
        return entries.get(Math.floorMod(draw, entries.size()));
    }

    /** A level for a draw in {@code [0, max - min]}. */
    public static int level(Enchant enchant, int draw) {
        return enchant.min + Math.floorMod(draw, enchant.max - enchant.min + 1);
    }
}
