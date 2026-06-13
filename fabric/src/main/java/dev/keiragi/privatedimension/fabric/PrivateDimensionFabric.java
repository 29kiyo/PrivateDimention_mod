package dev.keiragi.privatedimension.fabric;

import dev.keiragi.privatedimension.CommonEventHandler;
import dev.keiragi.privatedimension.PrivateDimensionMod;
import dev.keiragi.privatedimension.item.DimensionBottleItem;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTabs;
import dev.keiragi.privatedimension.registry.ModItems;
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
    private final Map<UUID, Boolean> cooldownActive = new HashMap<>();
    @Override
    public void onInitialize() {
        // アイテム登録
        Registry.register(
            BuiltInRegistries.ITEM,
            ResourceLocation.fromNamespaceAndPath("privatedimension", "dimension_bottle"),
            ModItems.createDimensionBottle()
        );

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

        // アイテム使用
        ItemEvents.USE.register((world, player, hand) -> {
            PrivateDimensionMod.LOGGER.info("ItemEvents.USE fired: isClientSide={}", world.isClientSide());
            if (world.isClientSide() || !(player instanceof ServerPlayer sp)) {
                return InteractionResult.PASS;
            }
            ItemStack stack = player.getItemInHand(hand);
            if (stack.getItem() instanceof DimensionBottleItem) {
                eventHandler.onItemUse(sp, stack);
                return InteractionResult.SUCCESS;
            }
            return InteractionResult.PASS;
        });

        // アイテム使用
        ItemEvents.USE.register((world, player, hand) -> {
            PrivateDimensionMod.LOGGER.info("ItemEvents.USE fired: isClientSide={}", world.isClientSide());
            if (world.isClientSide() || !(player instanceof ServerPlayer sp)) {
                return InteractionResult.PASS;
            }
            ItemStack stack = player.getItemInHand(hand);
            if (stack.getItem() instanceof DimensionBottleItem) {
                eventHandler.onItemUse(sp, stack);
                return InteractionResult.SUCCESS;
            }
            return InteractionResult.PASS;
        });

        // アイテム使用
        ItemEvents.USE.register((world, player, hand) -> {
            PrivateDimensionMod.LOGGER.info("ItemEvents.USE fired: isClientSide={}", world.isClientSide());
            if (world.isClientSide() || !(player instanceof ServerPlayer sp)) {
                return InteractionResult.PASS;
            }
            ItemStack stack = player.getItemInHand(hand);
            if (stack.getItem() instanceof DimensionBottleItem) {
                eventHandler.onItemUse(sp, stack);
                return InteractionResult.SUCCESS;
            }
            return InteractionResult.PASS;
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

                // アイテムクールダウン監視で右クリック検知
                ItemStack mainHand = player.getMainHandItem();
                if (DimensionBottleItem.isDimensionBottle(mainHand)) {
                    float cd = player.getCooldowns().getCooldownPercent(mainHand, 0f);
                    Boolean hadCooldown = cooldownActive.get(uid);
                    boolean hasCooldown = cd > 0f;
                    if (hadCooldown != null && !hadCooldown && hasCooldown) {
                        PrivateDimensionMod.LOGGER.info("Bottle cooldown detected for {}", player.getName().getString());
                        eventHandler.onItemUse(player, mainHand);
                    }
                    cooldownActive.put(uid, hasCooldown);
                }


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
