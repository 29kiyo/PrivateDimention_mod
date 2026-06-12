package dev.keiragi.privatedimension.neoforge;

import dev.keiragi.privatedimension.CommonEventHandler;
import dev.keiragi.privatedimension.PrivateDimensionMod;
import dev.keiragi.privatedimension.item.DimensionBottleItem;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Mod(PrivateDimensionMod.MOD_ID)
public class PrivateDimensionNeoForge {

    private final PrivateDimensionMod mod;
    private final CommonEventHandler eventHandler;
    private final Map<UUID, Vec3> lastPos = new HashMap<>();

    public PrivateDimensionNeoForge(IEventBus modBus, ModContainer container) {
        mod = new PrivateDimensionMod();
        mod.getConfig().setConfigPath(
            FMLPaths.CONFIGDIR.get()
                .resolve(PrivateDimensionMod.MOD_ID)
                .resolve("config.json"));
        mod.init();
        eventHandler = new CommonEventHandler(mod);

        modBus.addListener(this::setup);
        NeoForge.EVENT_BUS.register(this);

        PrivateDimensionMod.LOGGER.info("PrivateDimension (NeoForge {}) 初期化完了", mc_version());
    }

    private String mc_version() { return "1.21.9"; }

    private void setup(FMLCommonSetupEvent event) {}

    @SubscribeEvent
    public void onServerStarted(ServerStartedEvent event) {
        mod.getDimensionManager().onServerStart(event.getServer());
        mod.getPlayerDataManager().setDataPath(
            event.getServer().getWorldPath(LevelResource.ROOT)
                .resolve("privatedimension_playerdata.json"));
    }

    @SubscribeEvent
    public void onServerStopping(ServerStoppingEvent event) {
        mod.getPlayerDataManager().saveAll();
    }

    @SubscribeEvent
    public void onRegisterCommands(RegisterCommandsEvent event) {
        NeoForgeCommandHandler.register(mod, eventHandler, event.getDispatcher());
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
    public void onServerTick(ServerTickEvent.Post event) {
        for (ServerPlayer player : event.getServer().getPlayerList().getPlayers()) {
            UUID uid = player.getUUID();
            Vec3 current = player.position();
            if (!current.equals(lastPos.get(uid))) {
                eventHandler.onPlayerMove(player, current);
                lastPos.put(uid, current);
            }
        }
    }

    @SubscribeEvent
    public void onLivingDeath(LivingDeathEvent event) {
        if (event.getEntity() instanceof ServerPlayer sp) eventHandler.onPlayerDeath(sp);
    }

    @SubscribeEvent
    public void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer sp)) return;
        CommonEventHandler.RespawnInfo info = eventHandler.onPlayerRespawn(sp);
        if (info != null)
            sp.teleportTo(info.level, info.pos.x, info.pos.y, info.pos.z, java.util.Set.of(), sp.getYRot(), sp.getXRot(), false);
    }

    @SubscribeEvent
    public void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        mod.getPlayerDataManager().saveAll();
    }
}
