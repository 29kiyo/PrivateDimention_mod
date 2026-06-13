package dev.keiragi.privatedimension;

import dev.keiragi.privatedimension.dimension.DimensionManager;
import dev.keiragi.privatedimension.util.TeleportHandler;
import dev.keiragi.privatedimension.manager.PlayerDataManager;
import dev.keiragi.privatedimension.manager.PlotManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PrivateDimensionMod {
    public static final String MOD_ID = "privatedimension";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    private static PrivateDimensionMod instance;
    private DimensionManager dimensionManager;
    private PlotManager plotManager;
    private PlayerDataManager playerDataManager;
    private ModConfig config = new ModConfig();
    private TeleportHandler teleportHandler;

    public static PrivateDimensionMod getInstance() { return instance; }

    public void init() {
        instance = this;
        playerDataManager = new PlayerDataManager(this);
        dimensionManager  = new DimensionManager(this);
        plotManager       = new PlotManager(this);
        teleportHandler   = new TeleportHandler(this);
        LOGGER.info("PrivateDimension Mod が有効化されました！");
    }

    public DimensionManager getDimensionManager()   { return dimensionManager; }
    public PlotManager getPlotManager()             { return plotManager; }
    public PlayerDataManager getPlayerDataManager() { return playerDataManager; }
    public ModConfig getConfig()                    { return config; }
    public TeleportHandler getTeleportHandler()      { return teleportHandler; }
}
