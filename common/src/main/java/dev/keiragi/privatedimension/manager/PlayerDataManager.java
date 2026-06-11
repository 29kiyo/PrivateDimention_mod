package dev.keiragi.privatedimension.manager;

import com.google.gson.*;
import dev.keiragi.privatedimension.PrivateDimensionMod;
import dev.keiragi.privatedimension.util.IdUtils;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec3;

import java.io.*;
import java.nio.file.*;
import java.util.*;

public class PlayerDataManager {
    private final PrivateDimensionMod mod;
    private Path dataFile;

    private final Map<UUID, Integer>    plotIdCache     = new HashMap<>();
    private final Map<UUID, double[]>   plotPosCache    = new HashMap<>();
    private final Map<UUID, ReturnPos>  returnLocCache  = new HashMap<>();
    private int nextPlotId = 0;
    private JsonObject root = new JsonObject();

    public PlayerDataManager(PrivateDimensionMod mod) { this.mod = mod; }

    public void setDataPath(Path path) { this.dataFile = path; load(); }

    private void load() {
        if (dataFile == null || !Files.exists(dataFile)) return;
        try (Reader r = Files.newBufferedReader(dataFile)) {
            root = new Gson().fromJson(r, JsonObject.class);
            if (root == null) root = new JsonObject();
            nextPlotId = root.has("nextPlotId") ? root.get("nextPlotId").getAsInt() : 0;
        } catch (Exception e) {
            PrivateDimensionMod.LOGGER.warn("playerdata.json 読み込み失敗: {}", e.getMessage());
            root = new JsonObject();
        }
    }

    public void saveAll() {
        for (var e : plotIdCache.entrySet())
            getOrCreate(e.getKey().toString()).addProperty("plotId", e.getValue());
        for (var e : plotPosCache.entrySet()) {
            double[] p = e.getValue();
            JsonObject o = new JsonObject();
            o.addProperty("x", p[0]); o.addProperty("y", p[1]); o.addProperty("z", p[2]);
            getOrCreate(e.getKey().toString()).add("plotPos", o);
        }
        for (var e : returnLocCache.entrySet()) {
            ReturnPos rp = e.getValue();
            JsonObject o = new JsonObject();
            o.addProperty("world", rp.worldKey); o.addProperty("x", rp.x);
            o.addProperty("y", rp.y); o.addProperty("z", rp.z);
            o.addProperty("yaw", rp.yaw); o.addProperty("pitch", rp.pitch);
            getOrCreate(e.getKey().toString()).add("returnLoc", o);
        }
        root.addProperty("nextPlotId", nextPlotId);
        trySave();
    }

    private JsonObject getOrCreate(String uuid) {
        if (!root.has(uuid)) root.add(uuid, new JsonObject());
        return root.getAsJsonObject(uuid);
    }

    private void trySave() {
        if (dataFile == null) return;
        try {
            Files.createDirectories(dataFile.getParent());
            try (Writer w = Files.newBufferedWriter(dataFile)) {
                new GsonBuilder().setPrettyPrinting().create().toJson(root, w);
            }
        } catch (Exception e) {
            PrivateDimensionMod.LOGGER.warn("playerdata.json 保存失敗: {}", e.getMessage());
        }
    }

    public boolean hasPlot(UUID uuid) {
        if (plotIdCache.containsKey(uuid)) return plotIdCache.get(uuid) >= 0;
        JsonObject p = root.has(uuid.toString()) ? root.getAsJsonObject(uuid.toString()) : null;
        if (p != null && p.has("plotId")) {
            int id = p.get("plotId").getAsInt(); plotIdCache.put(uuid, id); return id >= 0;
        }
        return false;
    }

    public int getPlotId(UUID uuid) {
        if (plotIdCache.containsKey(uuid)) return plotIdCache.get(uuid);
        JsonObject p = root.has(uuid.toString()) ? root.getAsJsonObject(uuid.toString()) : null;
        int id = (p != null && p.has("plotId")) ? p.get("plotId").getAsInt() : -1;
        plotIdCache.put(uuid, id); return id;
    }

    public void setPlotId(UUID uuid, int id) {
        plotIdCache.put(uuid, id);
        getOrCreate(uuid.toString()).addProperty("plotId", id);
        trySave();
    }

    public int getNextPlotId() { return nextPlotId++; }

    public double[] getPlotPos(UUID uuid) {
        if (plotPosCache.containsKey(uuid)) return plotPosCache.get(uuid);
        JsonObject p = root.has(uuid.toString()) ? root.getAsJsonObject(uuid.toString()) : null;
        if (p != null && p.has("plotPos")) {
            JsonObject pp = p.getAsJsonObject("plotPos");
            double[] pos = { pp.get("x").getAsDouble(), pp.get("y").getAsDouble(), pp.get("z").getAsDouble() };
            plotPosCache.put(uuid, pos); return pos;
        }
        return null;
    }

    public void setPlotPos(UUID uuid, double x, double y, double z) {
        plotPosCache.put(uuid, new double[]{x, y, z});
        // クラッシュ時にも座標が失われないよう即座に永続化する
        // テレポート時のみ呼ばれるため I/O 負荷は問題なし
        JsonObject obj = getOrCreate(uuid.toString());
        JsonObject pos = new JsonObject();
        pos.addProperty("x", x); pos.addProperty("y", y); pos.addProperty("z", z);
        obj.add("plotPos", pos);
        trySave();
    }
    public void setPlotPosFromVec3(UUID uuid, Vec3 pos) { setPlotPos(uuid, pos.x, pos.y, pos.z); }

    public ReturnPos getReturnLocation(UUID uuid) { return returnLocCache.get(uuid); }

    public void setReturnLocation(UUID uuid, ServerLevel level, Vec3 pos, float yaw, float pitch) {
        returnLocCache.put(uuid, new ReturnPos(
            level.dimension().registry().toString(), pos.x, pos.y, pos.z, yaw, pitch));
    }

    public void clearReturnLocation(UUID uuid) { returnLocCache.remove(uuid); }

    public static class ReturnPos {
        public final String worldKey;
        public final double x, y, z;
        public final float yaw, pitch;

        public ReturnPos(String worldKey, double x, double y, double z, float yaw, float pitch) {
            this.worldKey = worldKey; this.x = x; this.y = y; this.z = z;
            this.yaw = yaw; this.pitch = pitch;
        }

        public Vec3 toVec3() { return new Vec3(x, y, z); }

        public ServerLevel resolveLevel(MinecraftServer server) {
            try {
                String[] _parts = worldKey.split(":", 2);
                Object rl = IdUtils.createId(_parts[0], _parts[1]);
                for (ServerLevel level : server.getAllLevels())
                    if (level.dimension().registry().equals(rl)) return level;
            } catch (Exception ignored) {}
            return server.overworld();
        }
    }
}
