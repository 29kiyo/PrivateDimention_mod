package dev.keiragi.privatedimension;

import com.google.gson.*;
import java.io.*;
import java.nio.file.*;

public class ModConfig {
    public String worldName          = "private_dimension";
    public int    plotSize           = 48;
    public int    plotSpacing        = 128;
    public int    plotFloorY         = 64;
    public int    pullEntityLimit    = 10;
    public double pullEntityRadius   = 3.0;
    public boolean borderEnforcement = true;

    public String msgDimensionEnter  = "別世界の空間へ移動しました。";
    public String msgDimensionReturn = "元の世界へ戻りました。";
    public String msgBorderForced    = "[Private Dimension] プロットの外には出られません！";
    public String msgGiveItem        = "[Private Dimension] アイテムを付与しました。";
    public String msgNoPermission    = "この操作を行う権限がありません。";

    private Path configPath;

    public void setConfigPath(Path path) { this.configPath = path; }

    public void load() {
        if (configPath == null) return;
        if (!Files.exists(configPath)) { save(); return; }
        try (Reader r = Files.newBufferedReader(configPath)) {
            ModConfig loaded = new Gson().fromJson(r, ModConfig.class);
            if (loaded != null) copyFrom(loaded);
        } catch (Exception e) {
            PrivateDimensionMod.LOGGER.warn("設定読み込み失敗: {}", e.getMessage());
        }
    }

    public void save() {
        if (configPath == null) return;
        try {
            Files.createDirectories(configPath.getParent());
            try (Writer w = Files.newBufferedWriter(configPath)) {
                new GsonBuilder().setPrettyPrinting().create().toJson(this, w);
            }
        } catch (Exception e) {
            PrivateDimensionMod.LOGGER.warn("設定保存失敗: {}", e.getMessage());
        }
    }

    private void copyFrom(ModConfig o) {
        worldName = o.worldName; plotSize = o.plotSize; plotSpacing = o.plotSpacing;
        plotFloorY = o.plotFloorY; pullEntityLimit = o.pullEntityLimit;
        pullEntityRadius = o.pullEntityRadius; borderEnforcement = o.borderEnforcement;
        msgDimensionEnter = o.msgDimensionEnter; msgDimensionReturn = o.msgDimensionReturn;
        msgBorderForced = o.msgBorderForced; msgGiveItem = o.msgGiveItem;
        msgNoPermission = o.msgNoPermission;
    }
}
