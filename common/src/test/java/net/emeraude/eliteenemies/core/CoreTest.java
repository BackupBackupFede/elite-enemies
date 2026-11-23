package net.emeraude.eliteenemies.core;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class CoreTest {

    @TempDir
    Path dir;

    // --- rarity --------------------------------------------------------------------------------

    @Test
    void everyDrawLandsOnTheRarityItsWeightCovers() {
        int[] weights = {50, 30, 9, 2};
        Map<Rarity, Integer> counts = new EnumMap<>(Rarity.class);
        for (int draw = 0; draw < Rarity.total(weights); draw++) counts.merge(Rarity.pick(weights, draw), 1, Integer::sum);
        assertEquals(50, counts.get(Rarity.COMMON));
        assertEquals(30, counts.get(Rarity.UNCOMMON));
        assertEquals(9, counts.get(Rarity.RARE));
        assertEquals(2, counts.get(Rarity.LEGENDARY));
    }

    @Test
    void zeroAndNegativeWeightsNeverWin() {
        int[] weights = {0, -5, 3, 0};
        assertEquals(3, Rarity.total(weights));
        for (int draw = 0; draw < 3; draw++) assertEquals(Rarity.RARE, Rarity.pick(weights, draw));
        assertNull(Rarity.pick(new int[]{0, 0, 0, 0}, 0));
    }

    @Test
    void higherRanksAreStrongerOnEveryStat() {
        Rarity[] r = Rarity.values();
        for (int i = 1; i < r.length; i++) {
            assertTrue(r[i].health > r[i - 1].health && r[i].armour > r[i - 1].armour && r[i].damage > r[i - 1].damage
                && r[i].speed > r[i - 1].speed && r[i].knockback > r[i - 1].knockback, r[i].name());
            assertEquals(i + 1, r[i].starText().length());
        }
        assertEquals(Rarity.RARE, Rarity.parse(" rare "));
        assertNull(Rarity.parse("NONE"));
    }

    // --- spawn gate ----------------------------------------------------------------------------

    @Test
    void cooldownBlocksEverywhereThenLifts() {
        SpawnGate gate = new SpawnGate();
        assertTrue(gate.open(0, "minecraft:overworld", 0, 0, 24));
        gate.record(0, "minecraft:overworld", 0, 0, 1200, 0);
        assertFalse(gate.open(1199, "minecraft:the_nether", 5000, 5000, 24));
        assertTrue(gate.open(1200, "minecraft:the_nether", 5000, 5000, 24));
    }

    @Test
    void protectedAreaBlocksNearbyInTheSameDimensionOnly() {
        SpawnGate gate = new SpawnGate();
        gate.record(0, "minecraft:overworld", 100, 100, 0, 12000);
        assertFalse(gate.open(10, "minecraft:overworld", 110, 110, 24), "inside the radius");
        assertTrue(gate.open(10, "minecraft:overworld", 130, 100, 24), "outside the radius");
        assertTrue(gate.open(10, "minecraft:the_nether", 100, 100, 24), "other dimension");
        assertTrue(gate.open(12000, "minecraft:overworld", 100, 100, 24), "expired");
    }

    // --- loot ----------------------------------------------------------------------------------

    @Test
    void everyPieceIsNamedEnchantedAndStaysWithinWhatAVanillaClientCanName() {
        Set<String> names = new HashSet<>();
        for (Rarity rarity : Rarity.values()) {
            assertFalse(Loot.of(rarity).isEmpty(), rarity.name());
            for (Loot.Entry entry : Loot.of(rarity)) {
                assertTrue(entry.item().startsWith("minecraft:"), entry.item());
                assertTrue(names.add(entry.name()), "duplicate name " + entry.name());
                assertFalse(entry.enchants().isEmpty(), entry.name());
                for (Loot.Enchant enchant : entry.enchants()) {
                    assertTrue(enchant.min() >= 1 && enchant.min() <= enchant.max() && enchant.max() <= 10, entry.name() + " " + enchant);
                }
            }
        }
    }

    @Test
    void picksCoverEveryPieceAndEveryLevel() {
        Set<String> seen = new HashSet<>();
        for (int draw = 0; draw < Loot.COMMON.size(); draw++) seen.add(Loot.pick(Rarity.COMMON, draw).name());
        assertEquals(Loot.COMMON.size(), seen.size());
        Loot.Enchant enchant = new Loot.Enchant("minecraft:sharpness", 8, 10);
        Set<Integer> levels = new HashSet<>();
        for (int draw = -5; draw < 20; draw++) levels.add(Loot.level(enchant, draw));
        assertEquals(Set.of(8, 9, 10), levels);
    }

    // --- messages ------------------------------------------------------------------------------

    @Test
    void announcementIsEnglish() {
        assertEquals("☠ Legendary elite nearby: ", Messages.announcePrefix(Rarity.LEGENDARY));
        assertEquals(" (45 blocks)", Messages.announceSuffix(45));
    }

    // --- config --------------------------------------------------------------------------------

    @Test
    void missingFileIsWrittenWithEveryDefault() throws IOException {
        Path file = dir.resolve("eliteenemies.json");
        Config config = Config.load(file);
        assertTrue(config.enabled && config.bossBar && config.beam && config.sound);
        assertFalse(config.allowSpawners || config.glow);
        assertEquals(Rarity.RARE, config.announceThreshold());
        assertTrue(config.mobs.contains("minecraft:zombie"));
        String written = Files.readString(file, StandardCharsets.UTF_8);
        for (String key : new String[]{"chancePercent", "cooldownSeconds", "maxHeightGap", "allowSpawners", "weightLegendary", "mobs"}) {
            assertTrue(written.contains(key), key);
        }
    }

    @Test
    void missingKeysKeepTheirDefaultAndBrokenFilesAreRefused() throws IOException {
        Path file = dir.resolve("eliteenemies.json");
        Files.writeString(file, "{ \"glow\": true, \"announceFrom\": \"NONE\", \"mobs\": [\"minecraft:zombie\"] }", StandardCharsets.UTF_8);
        Config config = Config.load(file);
        assertTrue(config.glow);
        assertNull(config.announceThreshold());
        assertEquals(1, config.mobs.size());
        assertEquals(50, config.weightCommon);

        Files.writeString(file, "{ \"glow\": ", StandardCharsets.UTF_8);
        assertThrows(IOException.class, () -> Config.load(file));
    }
}
