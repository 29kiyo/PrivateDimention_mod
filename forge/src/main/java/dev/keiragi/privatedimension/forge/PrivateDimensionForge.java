package dev.keiragi.privatedimension.forge;

import dev.keiragi.privatedimension.CommonEventHandler;
import dev.keiragi.privatedimension.PrivateDimensionMod;
import dev.keiragi.privatedimension.item.DimensionBottleItem;
import dev.keiragi.privatedimension.manager.PlayerDataManager;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.eventbus.api.IEventBus;

import java.nio.file.Path;
import java.util.*;

@Mod(PrivateDimensionMod.MOD_ID)
public class PrivateDimensionForge {

    private PrivateDimensionMod mod;
    private CommonEventHandler eventHandler;
    private final Map<UUID, Vec3> lastPos = new HashMap<>();

    public PrivateDimensionForge() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        modBus.addListener(this::setup);

        MinecraftForge.EVENT_BUS.register(this);

        mod = new PrivateDimensionMod();
        Path configDir = FMLPaths.CONFIGDIR.get().resolve(PrivateDimensionMod.MOD_ID);
        mod.getConfig().setConfigPath(configDir.resolve("config.json"));
        mod.init();
        eventHandler = new CommonEventHandler(mod);
    }

    private void setup(FMLCommonSetupEvent event) {
        PrivateDimensionMod.LOGGER.info("PrivateDimension Forge セットアップ完了。");
    }

    @SubscribeEvent
    public void onServerStarted(ServerStartedEvent event) {
        mod.getDimensionManager().onServerStart(event.getServer());
        mod.getPlayerDataManager().setDataPath(
            event.getServer().getWorldPath(net.minecraft.world.level.storage.LevelResource.ROOT)
                .resolve("privatedimension_playerdata.json"));
    }

    @SubscribeEvent
    public void onServerStopping(ServerStoppingEvent event) {
        mod.getPlayerDataManager().saveAll();
    }

    @SubscribeEvent
    public void onRegisterCommands(RegisterCommandsEvent event) {
        ForgeCommandHandler.register(mod, eventHandler, event.getDispatcher());
    }

    @SubscribeEvent
    public void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
        if (event.getEntity().level().isClientSide) return;
        if (!(event.getEntity() instanceof ServerPlayer sp)) return;
        ItemStack stack = event.getItemStack();
        if (DimensionBottleItem.isDimensionBottle(stack)) {
            event.setCanceled(true);
            eventHandler.onItemUse(sp, stack);
        }
    }

    @SubscribeEvent
    public void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        for (ServerPlayer player : event.getServer().getPlayerList().getPlayers()) {
            UUID uid = player.getUUID();
            Vec3 current = player.position();
            Vec3 prev = lastPos.get(uid);
            if (prev == null || !prev.equals(current)) {
                eventHandler.onPlayerMove(player, current);
                lastPos.put(uid, current);
            }
        }
    }

    @SubscribeEvent
    public void onLivingDeath(LivingDeathEvent event) {
        if (event.getEntity() instanceof ServerPlayer sp) {
            eventHandler.onPlayerDeath(sp);
        }
    }

    @SubscribeEvent
    public void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer sp)) return;
        CommonEventHandler.RespawnInfo info = eventHandler.onPlayerRespawn(sp);
        if (info != null) {
            sp.teleportTo(info.level, info.pos.x, info.pos.y, info.pos.z,
                sp.getYRot(), sp.getXRot());
        }
    }

    @SubscribeEvent
    public void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        mod.getPlayerDataManager().saveAll();
    }
}
