package dev.keiragi.privatedimension.fabric;

import dev.keiragi.privatedimension.CommonEventHandler;
import dev.keiragi.privatedimension.PrivateDimensionMod;
import dev.keiragi.privatedimension.item.DimensionBottleItem;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.interaction.v1.UseItemCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PrivateDimensionFabric implements ModInitializer {

    private PrivateDimensionMod mod;
    private CommonEventHandler eventHandler;
    private final Map<UUID, Vec3> lastPos = new HashMap<>();

    @Override
    public void onInitialize() {
        mod = new PrivateDimensionMod();
        mod.getConfig().setConfigPath(
            FabricLoader.getInstance().getConfigDir()
                .resolve(PrivateDimensionMod.MOD_ID)
                .resolve("config.json"));
        mod.init();
        eventHandler = new CommonEventHandler(mod);

        registerEvents();
        FabricCommandHandler.register(mod, eventHandler);
        PrivateDimensionMod.LOGGER.info("PrivateDimension (Fabric 1.21.8) 初期化完了");
    }

    private void registerEvents() {
        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            mod.getDimensionManager().onServerStart(server);
            mod.getPlayerDataManager().setDataPath(
                server.getWorldPath(LevelResource.ROOT)
                    .resolve("privatedimension_playerdata.json"));
        });

        ServerLifecycleEvents.SERVER_STOPPING.register(server ->
            mod.getPlayerDataManager().saveAll());

        UseItemCallback.EVENT.register((player, world, hand) -> {
            if (world.isClientSide || !(player instanceof ServerPlayer sp))
                return InteractionResultHolder.pass(player.getItemInHand(hand));
            ItemStack stack = player.getItemInHand(hand);
            if (DimensionBottleItem.isDimensionBottle(stack)) {
                eventHandler.onItemUse(sp, stack);
                return InteractionResultHolder.success(stack);
            }
            return InteractionResultHolder.pass(stack);
        });

        ServerTickEvents.END_SERVER_TICK.register(server -> {
            for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                UUID uid = player.getUUID();
                Vec3 current = player.position();
                if (!current.equals(lastPos.get(uid))) {
                    eventHandler.onPlayerMove(player, current);
                    lastPos.put(uid, current);
                }
            }
        });

        ServerLivingEntityEvents.AFTER_DEATH.register((entity, source) -> {
            if (entity instanceof ServerPlayer sp) eventHandler.onPlayerDeath(sp);
        });

        ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, newPlayer, alive) -> {
            CommonEventHandler.RespawnInfo info = eventHandler.onPlayerRespawn(newPlayer);
            if (info != null)
                newPlayer.teleportTo(info.level, info.pos.x, info.pos.y, info.pos.z,
                    newPlayer.getYRot(), newPlayer.getXRot());
        });

        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) ->
            mod.getPlayerDataManager().saveAll());
    }
}
