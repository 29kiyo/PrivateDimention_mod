package dev.keiragi.privatedimension.fabric;

import dev.keiragi.privatedimension.CommonEventHandler;
import dev.keiragi.privatedimension.PrivateDimensionMod;
import dev.keiragi.privatedimension.item.DimensionBottleItem;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

import java.nio.file.Path;
import java.util.*;

public class PrivateDimensionFabric implements ModInitializer {

    private PrivateDimensionMod mod;
    private CommonEventHandler eventHandler;
    private final Map<UUID, Vec3> lastPos = new HashMap<>();
    private final Map<UUID, Boolean> wasUsingItem = new HashMap<>();

    @Override
    public void onInitialize() {
        mod = new PrivateDimensionMod();
        mod.init();

        Path configDir = FabricLoader.getInstance().getConfigDir().resolve(PrivateDimensionMod.MOD_ID);
        mod.getConfig().setConfigPath(configDir.resolve("config.json"));
        mod.getConfig().load();

        mod.getPlayerDataManager().setDataPath(
            FabricLoader.getInstance().getGameDir()
                .resolve("world")
                .resolve("privatedimension_playerdata.json"));

        eventHandler = new CommonEventHandler(mod);

        registerEvents();
        FabricCommandHandler.register(mod, eventHandler);

        PrivateDimensionMod.LOGGER.info("PrivateDimension (Fabric) 初期化完了");
    }

    private void registerEvents() {
        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            mod.getDimensionManager().onServerStart(server);
            mod.getPlayerDataManager().setDataPath(
                server.getWorldPath(net.minecraft.world.level.storage.LevelResource.ROOT)
                    .resolve("privatedimension_playerdata.json"));
        });

        ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
            mod.getPlayerDataManager().saveAll();
        });

        ServerTickEvents.END_SERVER_TICK.register(server -> {
            for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                UUID uid = player.getUUID();
                Vec3 current = player.position();
                Vec3 prev = lastPos.get(uid);
                if (prev == null || !prev.equals(current)) {
                    eventHandler.onPlayerMove(player, current);
                    lastPos.put(uid, current);
                }

                // アイテム使用検知: 使用中→未使用に変わった瞬間を検知
                boolean isUsing = player.isUsingItem();
                Boolean wasUsing = wasUsingItem.get(uid);
                if (wasUsing != null && wasUsing && !isUsing) {
                    ItemStack stack = player.getMainHandItem();
                    if (DimensionBottleItem.isDimensionBottle(stack)) {
                        PrivateDimensionMod.LOGGER.info("Bottle use detected for {}", player.getName().getString());
                        eventHandler.onItemUse(player, stack);
                    }
                }
                wasUsingItem.put(uid, isUsing);
            }
        });

        ServerLivingEntityEvents.AFTER_DEATH.register((entity, source) -> {
            if (entity instanceof ServerPlayer sp) {
                eventHandler.onPlayerDeath(sp);
            }
        });

        ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, newPlayer, alive) -> {
            CommonEventHandler.RespawnInfo info = eventHandler.onPlayerRespawn(newPlayer);
            if (info != null) {
                newPlayer.teleportTo(info.level, info.pos.x, info.pos.y, info.pos.z,
                    newPlayer.getYRot(), newPlayer.getXRot());
            }
        });

        net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents.DISCONNECT.register(
            (handler, server) -> mod.getPlayerDataManager().saveAll());
    }
}
