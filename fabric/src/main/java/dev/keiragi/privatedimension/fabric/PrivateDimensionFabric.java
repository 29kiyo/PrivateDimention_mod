package dev.keiragi.privatedimension.fabric;

import dev.keiragi.privatedimension.CommonEventHandler;
import dev.keiragi.privatedimension.PrivateDimensionMod;
import dev.keiragi.privatedimension.item.DimensionBottleItem;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.nio.file.Path;
import java.util.*;

public class PrivateDimensionFabric implements ModInitializer {

    private PrivateDimensionMod mod;
    private CommonEventHandler eventHandler;

    // 移動チェック用：前tickの座標を保持
    private final Map<UUID, Vec3> lastPos = new HashMap<>();

    @Override
    public void onInitialize() {
        mod = new PrivateDimensionMod();
        mod.init();

        // 設定ファイルパスをセット（init()後にセット→再ロード）
        Path configDir = FabricLoader.getInstance().getConfigDir().resolve(PrivateDimensionMod.MOD_ID);
        mod.getConfig().setConfigPath(configDir.resolve("config.json"));
        mod.getConfig().load();

        // playerdata.json パス
        mod.getPlayerDataManager().setDataPath(
            FabricLoader.getInstance().getGameDir()
                .resolve("world")
                .resolve("privatedimension_playerdata.json"));

        eventHandler = new CommonEventHandler(mod);

        registerEvents();
        FabricCommandHandler.register(mod, eventHandler);

        PrivateDimensionMod.LOGGER.info("PrivateDimension Fabric が初期化されました。");
    }

    private void registerEvents() {
        // サーバー起動時
        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            mod.getDimensionManager().onServerStart(server);
            // playerdata パスを実際のワールドフォルダに更新
            mod.getPlayerDataManager().setDataPath(
                server.getWorldPath(net.minecraft.world.level.storage.LevelResource.ROOT)
                    .resolve("privatedimension_playerdata.json"));
        });

        // サーバー停止時
        ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
            mod.getPlayerDataManager().saveAll();
        });

        // アイテム使用
        UseItemCallback.EVENT.register((player, world, hand) -> {
            PrivateDimensionMod.LOGGER.info("UseItemCallback fired: isClientSide={}, player={}", world.isClientSide(), player.getName().getString());
            if (world.isClientSide() || !(player instanceof ServerPlayer sp)) {
                return InteractionResultHolder.pass(player.getItemInHand(hand));
            }
            ItemStack stack = player.getItemInHand(hand);
            PrivateDimensionMod.LOGGER.info("Item used: {}, isDimensionBottle={}", stack.getItem(), DimensionBottleItem.isDimensionBottle(stack));
            if (DimensionBottleItem.isDimensionBottle(stack)) {
                eventHandler.onItemUse(sp, stack);
                return InteractionResultHolder.success(stack);
            }
            return InteractionResultHolder.pass(stack);
        });

        // プレイヤー移動チェック (毎tick)
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                UUID uid = player.getUUID();
                Vec3 current = player.position();
                Vec3 prev = lastPos.get(uid);
                if (prev == null || !prev.equals(current)) {
                    eventHandler.onPlayerMove(player, current);
                    lastPos.put(uid, current);
                }
            }
        });

        // 死亡
        ServerLivingEntityEvents.AFTER_DEATH.register((entity, source) -> {
            if (entity instanceof ServerPlayer sp) {
                eventHandler.onPlayerDeath(sp);
            }
        });

        // リスポーン
        ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, newPlayer, alive) -> {
            CommonEventHandler.RespawnInfo info = eventHandler.onPlayerRespawn(newPlayer);
            if (info != null) {
                newPlayer.teleportTo(info.level, info.pos.x, info.pos.y, info.pos.z,
                    newPlayer.getYRot(), newPlayer.getXRot());
            }
        });

        // プレイヤーログアウト時にデータ保存
        net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents.DISCONNECT.register(
            (handler, server) -> mod.getPlayerDataManager().saveAll());
    }
}
