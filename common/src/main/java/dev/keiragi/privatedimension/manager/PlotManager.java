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
        int half = mod.getConfig().plotSize / 2;
        return new BlockPos(-half, mod.getConfig().plotFloorY - 1, getPlotOriginZ(plotId) - half);
    }

    public boolean isInsidePlot(int plotId, double x, double y, double z) {
        int half = mod.getConfig().plotSize / 2;
        int oz = getPlotOriginZ(plotId);
        int fy = mod.getConfig().plotFloorY;
        return x >= -half && x <= half
            && z >= (oz - half) && z <= (oz + half)
            && y >= (fy - 1)  && y <= (fy + mod.getConfig().plotHeight);
    }
}
