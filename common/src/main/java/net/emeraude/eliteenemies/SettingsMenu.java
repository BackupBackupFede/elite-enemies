package net.emeraude.eliteenemies;

import net.emeraude.eliteenemies.compat.Click;
import net.emeraude.eliteenemies.compat.ClickRelayMenu;
import net.emeraude.eliteenemies.core.Config;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.Locale;

/**
 * {@code /eliteenemies settings}: the main switches in a vanilla chest, so that no client mod is
 * needed. Each setting is an icon in the middle row with a pane under it; a click on either
 * switches it (or moves to the next value) and saves the config.
 *
 * <p>The chest only displays: its slots hold copies, every click is intercepted before vanilla sees
 * it, and the client is resynced afterwards because a vanilla client predicts the move it asked for.
 */
public final class SettingsMenu extends ClickRelayMenu {

    private static final int ROWS = 3, SIZE = ROWS * 9, INFO = 4, FIRST = 9;

    private static final double[] CHANCES = {0.5, 1, 2, 5, 10};
    private static final String[] ANNOUNCE = {"NONE", "LEGENDARY", "RARE", "UNCOMMON", "COMMON"};
    private static final int[] GAPS = {0, 16, 24, 32, 48};

    /** The settings, left to right. */
    public enum Setting {
        ENABLED("minecraft:nether_star", "Elites appear", "Master switch"),
        CHANCE("minecraft:rabbit_foot", "Chance", "Per eligible mob spawn"),
        ANNOUNCE("minecraft:bell", "Announced in chat", "From this rarity up"),
        SOUND("minecraft:note_block", "Sound", "With the announcement"),
        BEAM("minecraft:beacon", "Beam", "Particle column when one appears"),
        BOSS_BAR("minecraft:dragon_head", "Boss bar", "Name and health, nearby"),
        GLOW("minecraft:glowstone_dust", "Glow", "Seen through walls"),
        HEIGHT_GAP("minecraft:ladder", "Max height gap", "To the nearest player"),
        SPAWNERS("minecraft:spawner", "From spawners", "Off: no farming elites");

        final String icon, title, help;

        Setting(String icon, String title, String help) {
            this.icon = icon;
            this.title = title;
            this.help = help;
        }
    }

    private final ServerPlayer viewer;
    private final SimpleContainer display;
    private final ResourceKey<Level> openedIn;

    public SettingsMenu(int id, ServerPlayer viewer) {
        this(id, viewer, new SimpleContainer(SIZE));
    }

    private SettingsMenu(int id, ServerPlayer viewer, SimpleContainer display) {
        super(MenuType.GENERIC_9x3, id, viewer.getInventory(), display, ROWS);
        this.viewer = viewer;
        this.display = display;
        this.openedIn = viewer.level().dimension();
        draw();
    }

    public static void open(ServerPlayer player) {
        player.openMenu(new SimpleMenuProvider((id, inventory, p) -> new SettingsMenu(id, player),
            Component.literal("Elite Enemies - settings")));
    }

    /** The slot to click to change {@code setting}: its icon. The pane under it works too. */
    public static int slotOf(Setting setting) {
        return FIRST + setting.ordinal();
    }

    public ItemStack shown(int slot) {
        return display.getItem(slot);
    }

    /** What the setting is now, as shown on its pane. Null for a switch that is off. */
    static String value(Setting setting, Config c) {
        return switch (setting) {
            case ENABLED -> c.enabled ? "On" : null;
            case CHANCE -> (c.chancePercent == Math.rint(c.chancePercent) ? String.valueOf((int) c.chancePercent) : String.valueOf(c.chancePercent)) + " %";
            case ANNOUNCE -> c.announceThreshold() == null ? "Never" : c.announceThreshold().name().charAt(0)
                + c.announceThreshold().name().substring(1).toLowerCase(Locale.ROOT) + " and up";
            case SOUND -> c.sound ? "On" : null;
            case BEAM -> c.beam ? "On" : null;
            case BOSS_BAR -> c.bossBar ? "On" : null;
            case GLOW -> c.glow ? "On" : null;
            case HEIGHT_GAP -> c.maxHeightGap <= 0 ? "No limit" : c.maxHeightGap + " blocks";
            case SPAWNERS -> c.allowSpawners ? "On" : null;
        };
    }

    /** Switches the setting, or moves it to its next value. */
    static void change(Setting setting, Config c) {
        switch (setting) {
            case ENABLED -> c.enabled = !c.enabled;
            case CHANCE -> c.chancePercent = next(CHANCES, c.chancePercent);
            case ANNOUNCE -> {
                String now = c.announceThreshold() == null ? "NONE" : c.announceThreshold().name();
                int i = List.of(ANNOUNCE).indexOf(now);
                c.announceFrom = ANNOUNCE[(i + 1) % ANNOUNCE.length];
            }
            case SOUND -> c.sound = !c.sound;
            case BEAM -> c.beam = !c.beam;
            case BOSS_BAR -> c.bossBar = !c.bossBar;
            case GLOW -> c.glow = !c.glow;
            case HEIGHT_GAP -> {
                int i = 0;
                while (i < GAPS.length && GAPS[i] <= c.maxHeightGap) i++;
                c.maxHeightGap = GAPS[i % GAPS.length];
            }
            case SPAWNERS -> c.allowSpawners = !c.allowSpawners;
        }
    }

    private static double next(double[] values, double now) {
        for (double value : values) if (value > now + 1e-9) return value;
        return values[0];
    }

    private void draw() {
        for (int i = 0; i < SIZE; i++) display.setItem(i, ItemStack.EMPTY);
        display.setItem(INFO, icon(ItemIds.stack("minecraft:book", 1), "Elite Enemies", ChatFormatting.GOLD,
            List.of("Rare mobs become elites: stronger, geared,", "announced, and they drop their gear.",
                "Click a setting to change it.", "More in config/eliteenemies.json")));
        Config c = EliteEnemies.config();
        for (Setting setting : Setting.values()) {
            String value = value(setting, c);
            boolean toggle = switch (setting) {
                case CHANCE, ANNOUNCE, HEIGHT_GAP -> false;
                default -> true;
            };
            String shown = value == null ? "Off" : value;
            int slot = slotOf(setting);
            display.setItem(slot, icon(ItemIds.stack(setting.icon, 1), setting.title, ChatFormatting.WHITE, List.of(setting.help, shown)));
            String pane = !toggle ? "minecraft:yellow_stained_glass_pane"
                : value == null ? "minecraft:red_stained_glass_pane" : "minecraft:lime_stained_glass_pane";
            ChatFormatting colour = !toggle ? ChatFormatting.YELLOW : value == null ? ChatFormatting.RED : ChatFormatting.GREEN;
            display.setItem(slot + 9, icon(ItemIds.stack(pane, 1), shown, colour, List.of(setting.title)));
        }
    }

    @Override
    protected void onClick(int slot, int button, Click click, Player player) {
        try {
            if (player == viewer && click == Click.PICKUP && (button == 0 || button == 1)) {
                for (Setting setting : Setting.values()) {
                    if (slot != slotOf(setting) && slot != slotOf(setting) + 9) continue;
                    change(setting, EliteEnemies.config());
                    String failure = EliteEnemies.saveConfig();
                    EliteEnemies.LOGGER.info("{} set {} to {}", viewer.getName().getString(), setting.title,
                        value(setting, EliteEnemies.config()) == null ? "Off" : value(setting, EliteEnemies.config()));
                    if (failure != null) viewer.sendSystemMessage(Component.literal("Not saved: " + failure).withStyle(ChatFormatting.RED));
                    draw();
                }
            }
        } finally {
            // Undo whatever the client predicted: nothing on the cursor, every slot as the server has it.
            if (viewer.containerMenu == this) {
                setCarried(ItemStack.EMPTY);
                sendAllDataToRemote();
            }
            viewer.inventoryMenu.sendAllDataToRemote();
        }
    }

    @Override
    public ItemStack quickMoveStack(Player player, int slot) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean canTakeItemForPickAll(ItemStack carried, Slot slot) {
        return false;
    }

    @Override
    public boolean canDragTo(Slot slot) {
        return false;
    }

    /** Closed by vanilla's per-tick check on death, on a dimension change and on disconnect. */
    @Override
    public boolean stillValid(Player player) {
        return player == viewer && viewer.isAlive() && !viewer.isRemoved() && viewer.level().dimension() == openedIn;
    }

    private static ItemStack icon(ItemStack stack, String name, ChatFormatting colour, List<String> lore) {
        stack.set(DataComponents.CUSTOM_NAME, Component.literal(name).withStyle(s -> s.withItalic(false).withColor(colour)));
        stack.set(DataComponents.LORE, new ItemLore(lore.stream()
            .map(line -> (Component) Component.literal(line).withStyle(s -> s.withItalic(false).withColor(ChatFormatting.GRAY)))
            .toList()));
        return stack;
    }
}
