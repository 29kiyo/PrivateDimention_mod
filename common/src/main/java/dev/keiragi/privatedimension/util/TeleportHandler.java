package dev.keiragi.privatedimension.util;

import dev.keiragi.privatedimension.PrivateDimensionMod;
import dev.keiragi.privatedimension.manager.PlayerDataManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustColorTransitionOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;

public class TeleportHandler {
    private final PrivateDimensionMod mod;
    private final Set<UUID> teleporting = Collections.synchronizedSet(new HashSet<>());

    private static volatile boolean transitionChecked = false;
    private static Constructor<?> transitionCtor = null;
    private static int transitionCtorParamCount = 0;
    private static Object doNothingCallback = null;
    private static Method teleportCrossDimMethod = null;

    public TeleportHandler(PrivateDimensionMod mod) { this.mod = mod; }
    public boolean isTeleporting(UUID uid) { return teleporting.contains(uid); }

    public void handleUse(ServerPlayer player) {
        if (!teleporting.add(player.getUUID())) return;
        ServerLevel pd = mod.getDimensionManager().getPrivateDimension();
        if (pd == null) {
            release(player.getUUID());
            player.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                "§c[PrivateDimension] 次元ワールドが準備できていません。"));
            return;
        }
        try {
            if (mod.getDimensionManager().isPrivateDimension((ServerLevel) player.level()))
                gotoBaseWorld(player);
            else
                gotoPrivate(player, collectBringEntities(player));
        } catch (Exception e) {
            release(player.getUUID());
            PrivateDimensionMod.LOGGER.error("handleUse 例外: {}", e.getMessage(), e);
        }
    }

    private void gotoPrivate(ServerPlayer player, List<Entity> bring) {
        PlayerDataManager pdm = mod.getPlayerDataManager();
        UUID uid = player.getUUID();
        pdm.setReturnLocation(uid, (ServerLevel) player.level(), player.position(), player.getYRot(), player.getXRot());
        playVfx((ServerLevel) player.level(), player.position());
        addBlindness(player);
        if (pdm.hasPlot(uid)) gotoMyPlot(player, bring);
        else                   claimPlot(player, bring);
    }

    private void gotoMyPlot(ServerPlayer player, List<Entity> bring) {
        PlayerDataManager pdm = mod.getPlayerDataManager();
        UUID uid = player.getUUID();
        ServerLevel pd = mod.getDimensionManager().getPrivateDimension();
        int plotId = pdm.getPlotId(uid);
        BlockPos structOrigin = mod.getPlotManager().getPlotStructureOrigin(plotId);
        pd.getChunk(structOrigin);
        boolean needsStructure = pd.isEmptyBlock(structOrigin) || pd.isEmptyBlock(structOrigin.above(1));
        if (needsStructure) {
            PrivateDimensionMod.LOGGER.warn("プロット{}の構造物が見つかりません。再配置します。", plotId);
            pd.getChunk(BlockPos.containing(mod.getPlotManager().getPlotSpawn(plotId)));
            mod.getDimensionManager().placeStructure(pd, structOrigin);
        }
        double[] saved = pdm.getPlotPos(uid);
        Vec3 dest = saved != null ? new Vec3(saved[0], saved[1], saved[2]) : mod.getPlotManager().getPlotSpawn(plotId);
        teleportTo(player, pd, dest);
        pullEntities(pd, dest, bring);
        playVfx(pd, dest);
        release(uid);
    }

    private void claimPlot(ServerPlayer player, List<Entity> bring) {
        PlayerDataManager pdm = mod.getPlayerDataManager();
        UUID uid = player.getUUID();
        int plotId = pdm.getNextPlotId();
        pdm.setPlotId(uid, plotId);
        ServerLevel pd = mod.getDimensionManager().getPrivateDimension();
        BlockPos origin = mod.getPlotManager().getPlotStructureOrigin(plotId);
        Vec3 spawn = mod.getPlotManager().getPlotSpawn(plotId);
        pd.getChunk(BlockPos.containing(spawn));
        pd.getChunk(origin);
        mod.getDimensionManager().placeStructure(pd, origin);
        addSlowFalling(player);
        teleportTo(player, pd, spawn);
        pdm.setPlotPosFromVec3(uid, spawn);
        pullEntities(pd, spawn, bring);
        playVfx(pd, spawn);
        release(uid);
    }

    public void gotoBaseWorld(ServerPlayer player) {
        UUID uid = player.getUUID();
        teleporting.add(uid);
        List<Entity> bring = collectBringEntities(player);
        Vec3 cur = player.position();
        mod.getPlayerDataManager().setPlotPos(uid, cur.x, cur.y, cur.z);
        PlayerDataManager.ReturnPos rp = mod.getPlayerDataManager().getReturnLocation(uid);
        ServerLevel dest = (rp != null) ? rp.resolveLevel(player.level().getServer()) : player.level().getServer().overworld();
        Vec3 destPos = (rp != null) ? rp.toVec3() : new Vec3(0, 64, 0);
        playVfx((ServerLevel) player.level(), cur);
        addBlindness(player);
        teleportTo(player, dest, destPos);
        pullEntities(dest, destPos, bring);
        mod.getPlayerDataManager().clearReturnLocation(uid);
        playVfx(dest, destPos);
        release(uid);
    }

    private void teleportTo(ServerPlayer p, ServerLevel level, Vec3 pos) {
        p.teleportTo(level, pos.x, pos.y, pos.z, Set.of(), p.getYRot(), p.getXRot(), false);
    }

    private List<Entity> collectBringEntities(ServerPlayer player) {
        if (!player.isShiftKeyDown()) return Collections.emptyList();
        double r = mod.getConfig().pullEntityRadius;
        int lim = mod.getConfig().pullEntityLimit;
        List<Entity> res = new ArrayList<>();
        AABB box = player.getBoundingBox().inflate(r);
        for (Entity e : ((ServerLevel) player.level()).getEntitiesOfClass(Entity.class, box)) {
            if (e instanceof Monster || e instanceof Player
             || e instanceof net.minecraft.world.entity.item.ItemEntity
             || e instanceof net.minecraft.world.entity.decoration.ArmorStand) continue;
            res.add(e);
            if (res.size() >= lim) break;
        }
        return res;
    }

    private void pullEntities(ServerLevel dest, Vec3 pos, List<Entity> entities) {
        for (Entity e : entities) {
            if (e instanceof ServerPlayer sp) {
                sp.teleportTo(dest, pos.x, pos.y, pos.z, Set.of(), sp.getYRot(), sp.getXRot(), false);
            } else if (e.level() == dest) {
                e.teleportTo(pos.x, pos.y, pos.z);
            } else {
                if (!tryTeleportCrossDimension(e, dest, pos)) {
                    PrivateDimensionMod.LOGGER.warn("クロスディメンション転送失敗: {}", e.getType());
                }
            }
        }
    }

    private static synchronized void initTransitionApi() {
        if (transitionChecked) return;
        transitionChecked = true;
        try {
            Class<?> ttClass = Class.forName("net.minecraft.world.level.portal.TeleportTransition");
            Field f = ttClass.getField("DO_NOTHING");
            doNothingCallback = f.get(null);

            // 優先度: 6引数 > 7引数 > 9引数
            Constructor<?> best = null;
            int bestCount = Integer.MAX_VALUE;
            for (Constructor<?> ctor : ttClass.getConstructors()) {
                Class<?>[] params = ctor.getParameterTypes();
                if (params.length >= 5
                        && params[0] == ServerLevel.class
                        && params[1] == Vec3.class
                        && params[2] == Vec3.class
                        && params.length < bestCount) {
                    best = ctor;
                    bestCount = params.length;
                }
            }
            transitionCtor = best;
            transitionCtorParamCount = bestCount;

            // teleportCrossDimension または teleport メソッドを探す
            for (Method m : Entity.class.getMethods()) {
                if ((m.getName().equals("teleportCrossDimension") || m.getName().equals("teleport"))
                        && m.getParameterCount() == 1) {
                    teleportCrossDimMethod = m;
                    if (m.getName().equals("teleportCrossDimension")) break; // 優先
                }
            }
            PrivateDimensionMod.LOGGER.info("TeleportTransition API: ctor={}params, method={}",
                transitionCtorParamCount, teleportCrossDimMethod != null ? teleportCrossDimMethod.getName() : "null");
        } catch (Exception e) {
            PrivateDimensionMod.LOGGER.warn("TeleportTransition API初期化失敗: {}", e.getMessage());
        }
    }

    private boolean tryTeleportCrossDimension(Entity entity, ServerLevel dest, Vec3 pos) {
        initTransitionApi();
        if (transitionCtor == null || teleportCrossDimMethod == null || doNothingCallback == null) {
            return false;
        }
        try {
            Object transition;
            switch (transitionCtorParamCount) {
                case 6 -> // (ServerLevel, Vec3, Vec3, float, float, PostTeleportTransition)
                    transition = transitionCtor.newInstance(
                        dest, pos, Vec3.ZERO, entity.getYRot(), entity.getXRot(),
                        doNothingCallback);
                case 7 -> // (ServerLevel, Vec3, Vec3, float, float, Set, PostTeleportTransition)
                    transition = transitionCtor.newInstance(
                        dest, pos, Vec3.ZERO, entity.getYRot(), entity.getXRot(),
                        Set.of(), doNothingCallback);
                case 9 -> // (ServerLevel, Vec3, Vec3, float, float, boolean, boolean, Set, PostTeleportTransition)
                    transition = transitionCtor.newInstance(
                        dest, pos, Vec3.ZERO, entity.getYRot(), entity.getXRot(),
                        false, false, Set.of(), doNothingCallback);
                default -> {
                    PrivateDimensionMod.LOGGER.warn("未知のTeleportTransition引数数: {}", transitionCtorParamCount);
                    return false;
                }
            }
            teleportCrossDimMethod.invoke(entity, transition);
            return true;
        } catch (Exception e) {
            PrivateDimensionMod.LOGGER.warn("teleportCrossDimension失敗: {}", e.getMessage());
            return false;
        }
    }

    private void addBlindness(ServerPlayer p) {
        p.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 40, 0, true, false));
    }

    private void addSlowFalling(ServerPlayer p) {
        p.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 20, 0, true, false));
    }

    public void playVfx(ServerLevel level, Vec3 pos) {
        Vec3 c = pos.add(0, 1, 0);
        level.sendParticles(ParticleTypes.GLOW, c.x, c.y, c.z, 50, 0.2, 0.5, 0.2, 1.0);
        level.sendParticles(
            new DustColorTransitionOptions(0x0000B2FF, 0x0099FFFF, 1.0f),
            c.x, c.y, c.z, 100, 0.2, 0.5, 0.2, 1.0);
        level.playSound(null, pos.x, pos.y, pos.z, SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 2f, 0.8f);
        level.playSound(null, pos.x, pos.y, pos.z, SoundEvents.ALLAY_ITEM_TAKEN, SoundSource.PLAYERS, 2f, 0.8f);
        level.playSound(null, pos.x, pos.y, pos.z, SoundEvents.AMETHYST_CLUSTER_BREAK, SoundSource.BLOCKS, 2f, 1.2f);
    }

    private void release(UUID uid) { teleporting.remove(uid); }
}
