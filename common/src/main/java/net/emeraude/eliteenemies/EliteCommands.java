package net.emeraude.eliteenemies;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.emeraude.eliteenemies.compat.Compat;
import net.emeraude.eliteenemies.core.Rarity;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.phys.Vec3;

import java.util.Arrays;

/**
 * {@code /eliteenemies settings}, {@code reload} and {@code spawn <rarity>}, all for operators:
 * the last one puts an elite zombie in front of you, to try the mod or a setting.
 */
public final class EliteCommands {

    private static final int OPERATOR = 2;

    private EliteCommands() {}

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        // The Commands constructor runs more than once (startup validation, /reload).
        if (dispatcher.getRoot().getChild(EliteEnemies.MOD_ID) != null) return;
        dispatcher.register(Commands.literal(EliteEnemies.MOD_ID)
            .requires(source -> Compat.hasPermission(source, OPERATOR))
            .then(Commands.literal("settings").executes(EliteCommands::settings))
            .then(Commands.literal("reload").executes(EliteCommands::reload))
            .then(Commands.literal("spawn")
                .then(Commands.argument("rarity", StringArgumentType.word())
                    .suggests((ctx, builder) -> {
                        Arrays.stream(Rarity.values()).map(Rarity::id).forEach(builder::suggest);
                        return builder.buildFuture();
                    })
                    .executes(EliteCommands::spawn))));
    }

    private static int settings(CommandContext<CommandSourceStack> ctx) {
        ServerPlayer player = ctx.getSource().getPlayer();
        if (player == null) {
            ctx.getSource().sendSystemMessage(error("Open this as a player, or edit config/eliteenemies.json."));
            return 0;
        }
        SettingsMenu.open(player);
        return 1;
    }

    private static int reload(CommandContext<CommandSourceStack> ctx) {
        String failure = EliteEnemies.reloadConfig();
        ctx.getSource().sendSystemMessage(failure == null
            ? info("Elite Enemies config reloaded.")
            : error("Config not reloaded: " + failure));
        return failure == null ? 1 : 0;
    }

    private static int spawn(CommandContext<CommandSourceStack> ctx) {
        Rarity rarity = Rarity.parse(StringArgumentType.getString(ctx, "rarity"));
        ServerPlayer player = ctx.getSource().getPlayer();
        if (rarity == null || player == null) {
            ctx.getSource().sendSystemMessage(error("Usage, as a player: /eliteenemies spawn common|uncommon|rare|legendary"));
            return 0;
        }
        ServerLevel level = (ServerLevel) player.level();
        Entity entity = Compat.create(ItemIds.entity("minecraft:zombie"), level);
        if (!(entity instanceof Mob mob)) return 0;
        Vec3 ahead = player.position().add(player.getLookAngle().multiply(4, 0, 4));
        mob.setPos(ahead.x, player.getY(), ahead.z);
        level.addFreshEntity(mob);
        Elites.make(mob, rarity, level);
        return 1;
    }

    private static Component info(String text) {
        return Component.literal(text).withStyle(ChatFormatting.GRAY);
    }

    private static Component error(String text) {
        return Component.literal(text).withStyle(ChatFormatting.RED);
    }
}
