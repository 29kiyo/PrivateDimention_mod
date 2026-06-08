package dev.keiragi.privatedimension.manager;

import dev.keiragi.privatedimension.PrivateDimensionMod;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;

public class PlotManager {
    private final PrivateDimensionMod mod;
    public PlotManager(PrivateDimensionMod mod) { this.mod = mod; }

    public int getPlotOriginZ(int plotId) {
        return plotId * mod.getConfig().plotSpacing;
    }

    public Vec3 getPlotSpawn(int plotId) {
        return new Vec3(0.5, mod.getConfig().plotFloorY + 5, getPlotOriginZ(plotId) + 0.5);
    }

    public BlockPos getPlotStructureOrigin(int plotId) {
        return new BlockPos(-24, mod.getConfig().plotFloorY - 1, getPlotOriginZ(plotId) - 24);
    }

    public boolean isInsidePlot(int plotId, double x, double y, double z) {
        int oz = getPlotOriginZ(plotId);
        int fy = mod.getConfig().plotFloorY;
        return x >= -24 && x <= 24
            && z >= (oz - 24) && z <= (oz + 24)
            && y >= (fy - 1)  && y <= (fy + 46);
    }
}
