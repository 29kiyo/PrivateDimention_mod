package dev.keiragi.privatedimension.neoforge;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import dev.keiragi.privatedimension.CommonEventHandler;
import dev.keiragi.privatedimension.PrivateDimensionMod;
import dev.keiragi.privatedimension.manager.PlayerDataManager;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.UUID;

public class NeoForgeCommandHandler {

    static void register(PrivateDimensionMod mod, CommonEventHandler eventHandler,
                         CommandDispatcher<CommandSourceStack> dispatcher) {
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
    }

    private static int giveSelf(CommandContext<CommandSourceStack> ctx, PrivateDimensionMod mod) {
        if (!isOp(ctx.getSource())) {
            ctx.getSource().sendFailure(net.minecraft.network.chat.Component.literal("§cこのコマンドはOP専用です。"));
            return 0;
        }
        try {
            ServerPlayer player = ctx.getSource().getPlayerOrException();
            if (dev.keiragi.privatedimension.registry.ModItems.DIMENSION_BOTTLE != null)
                player.getInventory().add(new net.minecraft.world.item.ItemStack(dev.keiragi.privatedimension.registry.ModItems.DIMENSION_BOTTLE));
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
        if (dev.keiragi.privatedimension.registry.ModItems.DIMENSION_BOTTLE != null)
            target.getInventory().add(new net.minecraft.world.item.ItemStack(dev.keiragi.privatedimension.registry.ModItems.DIMENSION_BOTTLE));
        ctx.getSource().sendSuccess(() -> Component.literal("§a[PD] " + target.getName().getString() + " に付与しました。"), false);
        return 1;
    }

    private static int reload(CommandContext<CommandSourceStack> ctx, PrivateDimensionMod mod) {
        if (!isOp(ctx.getSource())) {
            ctx.getSource().sendFailure(net.minecraft.network.chat.Component.literal("§cこのコマンドはOP専用です。"));
            return 0;
        }
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
                player.sendSystemMessage(Component.literal("§b[PD] プロットID: §f" + pdm.getPlotId(uid)));
                double[] pos = pdm.getPlotPos(uid);
                if (pos != null)
                    player.sendSystemMessage(Component.literal(
                        String.format("§b次元内座標: §f%.1f, %.1f, %.1f", pos[0], pos[1], pos[2])));
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
