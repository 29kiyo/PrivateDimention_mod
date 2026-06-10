package dev.keiragi.privatedimension.fabric;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import dev.keiragi.privatedimension.CommonEventHandler;
import dev.keiragi.privatedimension.PrivateDimensionMod;
import dev.keiragi.privatedimension.item.DimensionBottleItem;
import dev.keiragi.privatedimension.manager.PlayerDataManager;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.UUID;

public class FabricCommandHandler {

    static void register(PrivateDimensionMod mod, CommonEventHandler eventHandler) {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            dispatcher.register(Commands.literal("pd")
                .then(Commands.literal("give")
                    .requires(src -> src.hasPermission(2))
                    .executes(ctx -> giveSelf(ctx, mod))
                    .then(Commands.argument("player", StringArgumentType.word())
                        .executes(ctx -> givePlayer(ctx, mod))))
                .then(Commands.literal("reload")
                    .requires(src -> src.hasPermission(2))
                    .executes(ctx -> reload(ctx, mod)))
                .then(Commands.literal("info")
                    .executes(ctx -> info(ctx, mod))));
            dispatcher.register(Commands.literal("privatedim")
                .redirect(dispatcher.getRoot().getChild("pd")));
        });
    }

    private static int giveSelf(CommandContext<CommandSourceStack> ctx, PrivateDimensionMod mod) {
        try {
            ServerPlayer player = ctx.getSource().getPlayerOrException();
            player.getInventory().add(DimensionBottleItem.createItem());
            ctx.getSource().sendSuccess(() -> Component.literal("§a[PD] アイテムを付与しました。"), false);
            return 1;
        } catch (Exception e) {
            ctx.getSource().sendFailure(Component.literal("プレイヤーとして実行してください。"));
            return 0;
        }
    }

    private static int givePlayer(CommandContext<CommandSourceStack> ctx, PrivateDimensionMod mod) {
        String name = StringArgumentType.getString(ctx, "player");
        ServerPlayer target = ctx.getSource().getServer().getPlayerList().getPlayerByName(name);
        if (target == null) {
            ctx.getSource().sendFailure(Component.literal("§cプレイヤーが見つかりません: " + name));
            return 0;
        }
        target.getInventory().add(DimensionBottleItem.createItem());
        ctx.getSource().sendSuccess(() -> Component.literal("§a[PD] " + target.getName().getString() + " に付与しました。"), false);
        return 1;
    }

    private static int reload(CommandContext<CommandSourceStack> ctx, PrivateDimensionMod mod) {
        mod.getConfig().load();
        ctx.getSource().sendSuccess(() -> Component.literal("§a[PD] 設定をリロードしました。"), false);
        return 1;
    }

    private static int info(CommandContext<CommandSourceStack> ctx, PrivateDimensionMod mod) {
        try {
            ServerPlayer player = ctx.getSource().getPlayerOrException();
            UUID uid = player.getUUID();
            PlayerDataManager pdm = mod.getPlayerDataManager();
            if (pdm.hasPlot(uid)) {
                int id = pdm.getPlotId(uid);
                double[] pos = pdm.getPlotPos(uid);
                player.sendSystemMessage(Component.literal("§b[PD] プロットID: §f" + id));
                if (pos != null)
                    player.sendSystemMessage(Component.literal(
                        String.format("§b次元内最終座標: §f%.1f, %.1f, %.1f", pos[0], pos[1], pos[2])));
            } else {
                player.sendSystemMessage(Component.literal("§b[PD] まだプロットを持っていません。"));
            }
            return 1;
        } catch (Exception e) {
            ctx.getSource().sendFailure(Component.literal("プレイヤーとして実行してください。"));
            return 0;
        }
    }
}
