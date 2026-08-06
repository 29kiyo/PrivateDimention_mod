package dev.keiragi.privatedimension;

import com.google.gson.*;
import java.io.*;
import java.nio.file.*;
import dev.keiragi.privatedimension.util.Lang;

public class ModConfig {
    public String worldName          = "private_dimension";
    public int    plotSize           = 48;
    public int    plotHeight         = 46;
    public int    plotSpacing        = 128;
    public int    plotFloorY         = 64;
    public int    pullEntityLimit    = 10;
    public double pullEntityRadius   = 3.0;
    public boolean borderEnforcement = true;
    public String  plotBypassTag      = "pd_free";
    public String  structureFile      = "plot48x48.nbt";
    public int     cooldownSeconds    = 2;

    /** "auto" = クライアントの言語を自動検出。それ以外は "en" / "ja" のように固定言語コードを指定 */
    public String language = "auto";

    private transient Path configPath;

    public void setConfigPath(Path path) { this.configPath = path; }

    public void load() {
        if (configPath == null) return;
        if (!Files.exists(configPath)) { save(); Lang.init(configPath.getParent().resolve("lang")); return; }
        try (Reader r = Files.newBufferedReader(configPath)) {
            ModConfig loaded = new Gson().fromJson(r, ModConfig.class);
            if (loaded != null) copyFrom(loaded);
            Lang.init(configPath.getParent().resolve("lang"));
        } catch (Exception e) {
            PrivateDimensionMod.LOGGER.warn("Failed to load config: {}", e.getMessage());
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
            PrivateDimensionMod.LOGGER.warn("Failed to save config: {}", e.getMessage());
        }
    }

    private void copyFrom(ModConfig o) {
        worldName = o.worldName; plotSize = o.plotSize; plotHeight = o.plotHeight;
        plotSpacing = o.plotSpacing;
        plotFloorY = o.plotFloorY; pullEntityLimit = o.pullEntityLimit;
        pullEntityRadius = o.pullEntityRadius; borderEnforcement = o.borderEnforcement;
        if (o.plotBypassTag != null) plotBypassTag = o.plotBypassTag;
        if (o.structureFile != null) structureFile = o.structureFile;
        if (o.language != null) language = o.language;
        cooldownSeconds = o.cooldownSeconds;
    }
}
