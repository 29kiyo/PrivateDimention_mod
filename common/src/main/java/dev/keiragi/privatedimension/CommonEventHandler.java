package dev.keiragi.privatedimension;

import dev.keiragi.privatedimension.item.DimensionBottleItem;
import dev.keiragi.privatedimension.manager.PlayerDataManager;
import dev.keiragi.privatedimension.util.TeleportHandler;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import java.util.*;

public class CommonEventHandler {
    private final PrivateDimensionMod mod;
    private final TeleportHandler teleportHandler;
    private final Set<UUID> diedInDimension = Collections.synchronizedSet(new HashSet<>());
    private static final long BORDER_COOLDOWN_MS = 2000;
    private final Map<UUID, Long> borderCooldown = new HashMap<>();

    public CommonEventHandler(PrivateDimensionMod mod) {
        this.mod = mod;
        this.teleportHandler = new TeleportHandler(mod);
    }

    public TeleportHandler getTeleportHandler() { return teleportHandler; }

    public boolean onItemUse(ServerPlayer player, ItemStack stack) {
        if (!DimensionBottleItem.isDimensionBottle(stack)) return false;
        teleportHandler.handleUse(player);
        return true;
    }

    public void onPlayerMove(ServerPlayer player, Vec3 newPos) {
        if (!mod.getDimensionManager().isPrivateDimension((ServerLevel) player.level())) return;
        if (player.level().isClientSide() ? false : ((net.minecraft.server.level.ServerPlayer)player).hasPermissions(4)) return;
        UUID uid = player.getUUID();
        if (!mod.getPlayerDataManager().hasPlot(uid)) return;
        if (teleportHandler.isTeleporting(uid)) return;
        int plotId = mod.getPlayerDataManager().getPlotId(uid);
        if (mod.getPlotManager().isInsidePlot(plotId, newPos.x, newPos.y, newPos.z)) return;
        if (!mod.getConfig().borderEnforcement) return;
        long now = System.currentTimeMillis();
        Long last = borderCooldown.get(uid);
        if (last != null && now - last < BORDER_COOLDOWN_MS) return;
        borderCooldown.put(uid, now);
        player.sendSystemMessage(Component.literal("§c" + mod.getConfig().msgBorderForced));
        teleportHandler.playVfx((ServerLevel) player.level(), player.position());
        teleportHandler.gotoBaseWorld(player);
    }

    public void onPlayerDeath(ServerPlayer player) {
        if (mod.getDimensionManager().isPrivateDimension((ServerLevel) player.level()))
            diedInDimension.add(player.getUUID());
    }

    public RespawnInfo onPlayerRespawn(ServerPlayer player) {
        UUID uid = player.getUUID();
        if (!diedInDimension.remove(uid)) return null;
        PlayerDataManager pdm = mod.getPlayerDataManager();
        PlayerDataManager.ReturnPos rp = pdm.getReturnLocation(uid);
        ServerLevel dest = rp != null ? rp.resolveLevel(net.minecraft.server.MinecraftServer.getServer()) : net.minecraft.server.MinecraftServer.getServer().overworld();
        Vec3 destPos = rp != null ? rp.toVec3() : new Vec3(dest.getLevelData().getXSpawn(), dest.getLevelData().getYSpawn(), dest.getLevelData().getZSpawn());
        pdm.clearReturnLocation(uid);
        if (pdm.hasPlot(uid))
            pdm.setPlotPosFromVec3(uid, mod.getPlotManager().getPlotSpawn(pdm.getPlotId(uid)));
        return new RespawnInfo(dest, destPos);
    }

    public static class RespawnInfo {
        public final ServerLevel level;
        public final Vec3 pos;
        public RespawnInfo(ServerLevel level, Vec3 pos) { this.level = level; this.pos = pos; }
    }
}
