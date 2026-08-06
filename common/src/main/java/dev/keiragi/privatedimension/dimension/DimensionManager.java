package dev.keiragi.privatedimension.dimension;

import dev.keiragi.privatedimension.PrivateDimensionMod;
import dev.keiragi.privatedimension.util.IdUtils;
import dev.keiragi.privatedimension.util.NbtStructurePlacer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.LevelResource;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.Arrays;

public class DimensionManager {

    public static final Object DIMENSION_ID =
        IdUtils.createId(PrivateDimensionMod.MOD_ID, "private_dimension");

    @SuppressWarnings("rawtypes")
    public static final Object DIMENSION_KEY =
        IdUtils.createResourceKey(Registries.DIMENSION, DIMENSION_ID);

    private final PrivateDimensionMod mod;
    private MinecraftServer server;

    public DimensionManager(PrivateDimensionMod mod) { this.mod = mod; }

    public void onServerStart(MinecraftServer server) {
        this.server = server;
        ServerLevel dim = getPrivateDimension();
        if (dim != null) {
            PrivateDimensionMod.LOGGER.info("Private dimension world found: {}", DIMENSION_ID);
            try {
                Path structDir = getStructureDir();
                Files.createDirectories(structDir);
                PrivateDimensionMod.LOGGER.info(
                    "Place your custom .nbt structures here (then set the file name in config.json's structureFile): {}",
                    structDir.toAbsolutePath());
            } catch (Exception ignored) {}
        } else {
            PrivateDimensionMod.LOGGER.warn("Private dimension not found. Please check the datapack.");
        }
    }

    public ServerLevel getPrivateDimension() {
        return server == null ? null : server.getLevel((ResourceKey<Level>) DIMENSION_KEY);
    }

    public boolean isPrivateDimension(ServerLevel level) {
        return level != null && level.dimension().equals(DIMENSION_KEY);
    }

    private Path getStructureDir() {
        return server.getWorldPath(LevelResource.ROOT)
            .resolve("generated")
            .resolve(PrivateDimensionMod.MOD_ID)
            .resolve("structures");
    }

    public void placeStructure(ServerLevel level, BlockPos origin) {
        try {
            ensureDefaultNbtExtracted(level);
            Path structDir = getStructureDir();
            String fileName = mod.getConfig().structureFile;
            if (fileName == null || fileName.isBlank()) fileName = "plot48x48.nbt";
            Path nbtPath = structDir.resolve(fileName);
            if (!Files.exists(nbtPath)) {
                PrivateDimensionMod.LOGGER.error(
                    "NBT file not found: {} (check structureFile in config.json)", nbtPath);
                return;
            }
            boolean result = NbtStructurePlacer.place(level, origin, nbtPath);
            PrivateDimensionMod.LOGGER.info("Structure placement complete: {} placed={}", origin, result);
        } catch (Exception e) {
            PrivateDimensionMod.LOGGER.error("Structure placement failed: {}", e.getMessage(), e);
        }
    }

    private void ensureDefaultNbtExtracted(ServerLevel level) throws IOException {
        Path structDir = getStructureDir();
        Files.createDirectories(structDir);
        Path dest = structDir.resolve("plot48x48.nbt");

        try (InputStream in = DimensionManager.class.getResourceAsStream("/plot48x48.nbt")) {
            if (in == null) { PrivateDimensionMod.LOGGER.error("plot48x48.nbt resource missing!"); return; }
            byte[] bytes = in.readAllBytes();
            if (!Files.exists(dest) || !md5Matches(dest, bytes)) {
                Files.write(dest, bytes);
                PrivateDimensionMod.LOGGER.info("Extracted plot48x48.nbt.");
            }
        }
    }

    private boolean md5Matches(Path file, byte[] ref) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            return Arrays.equals(md.digest(Files.readAllBytes(file)), md.digest(ref));
        } catch (Exception e) { return false; }
    }
}
