package dev.keiragi.privatedimension.forge;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import dev.keiragi.privatedimension.CommonEventHandler;
import dev.keiragi.privatedimension.PrivateDimensionMod;
import dev.keiragi.privatedimension.item.DimensionBottleItem;
import dev.keiragi.privatedimension.manager.PlayerDataManager;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.UUID;

/**
 * /pd コマンド (Forge)
 */
public class ForgeCommandHandler {

    static void register(PrivateDimensionMod mod, CommonEventHandler eventHandler,
                         CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
            Commands.literal("pd")
                .then(Commands.literal("give")
                    .requires(src -> src.hasPermission(2))
                    .executes(ctx -> giveSelf(ctx, mod))
                    .then(Commands.argument("player", StringArgumentType.word())
                        .executes(ctx -> givePlayer(ctx, mod))))
                .then(Commands.literal("reload")
                    .requires(src -> src.hasPermission(2))
                    .executes(ctx -> reload(ctx, mod)))
                .then(Commands.literal("info")
                    .executes(ctx -> info(ctx, mod)))
        );
        dispatcher.register(
            Commands.literal("privatedim")
                .redirect(dispatcher.getRoot().getChild("pd"))
        );
    }

    private static int giveSelf(CommandContext<CommandSourceStack> ctx, PrivateDimensionMod mod) {
        ServerPlayer player;
        try { player = ctx.getSource().getPlayerOrException(); }
        catch (Exception e) {
            ctx.getSource().sendFailure(Component.literal("プレイヤーとして実行してください。"));
            return 0;
        }
        player.getInventory().add(DimensionBottleItem.createItem());
        ctx.getSource().sendSuccess(() ->
            Component.literal("§a[PrivateDimension] アイテムを付与しました。"), false);
        return 1;
    }

    private static int givePlayer(CommandContext<CommandSourceStack> ctx, PrivateDimensionMod mod) {
        String name = StringArgumentType.getString(ctx, "player");
        ServerPlayer target = ctx.getSource().getServer().getPlayerList().getPlayerByName(name);
        if (target == null) {
            ctx.getSource().sendFailure(Component.literal("§cプレイヤーが見つかりません: " + name));
            return 0;
        }
        target.getInventory().add(DimensionBottleItem.createItem());
        ctx.getSource().sendSuccess(() ->
            Component.literal("§a[PrivateDimension] " + target.getName().getString() + " にアイテムを付与しました。"), false);
        return 1;
    }

    private static int reload(CommandContext<CommandSourceStack> ctx, PrivateDimensionMod mod) {
        mod.getConfig().load();
        ctx.getSource().sendSuccess(() ->
            Component.literal("§a[PrivateDimension] 設定をリロードしました。"), false);
        return 1;
    }

    private static int info(CommandContext<CommandSourceStack> ctx, PrivateDimensionMod mod) {
        ServerPlayer player;
        try { player = ctx.getSource().getPlayerOrException(); }
        catch (Exception e) {
            ctx.getSource().sendFailure(Component.literal("プレイヤーとして実行してください。"));
            return 0;
        }
        PlayerDataManager pdm = mod.getPlayerDataManager();
        UUID uid = player.getUUID();
        if (pdm.hasPlot(uid)) {
            int id = pdm.getPlotId(uid);
            double[] pos = pdm.getPlotPos(uid);
            player.sendSystemMessage(Component.literal("§b[PrivateDimension] プロットID: §f" + id));
            if (pos != null) {
                player.sendSystemMessage(Component.literal(
                    String.format("§b次元内最終座標: §f%.1f, %.1f, %.1f", pos[0], pos[1], pos[2])));
            }
        } else {
            player.sendSystemMessage(Component.literal("§b[PrivateDimension] まだプロットを持っていません。"));
        }
        return 1;
    }
}
