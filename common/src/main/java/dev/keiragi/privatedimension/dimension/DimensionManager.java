package dev.keiragi.privatedimension.dimension;

import dev.keiragi.privatedimension.PrivateDimensionMod;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import net.minecraft.world.level.storage.LevelResource;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Optional;

public class DimensionManager {

    public static final ResourceLocation DIMENSION_ID =
        ResourceLocation.fromNamespaceAndPath(PrivateDimensionMod.MOD_ID, "private_dimension");

    public static final ResourceKey<Level> DIMENSION_KEY =
        ResourceKey.create(Registries.DIMENSION, DIMENSION_ID);

    private final PrivateDimensionMod mod;
    private MinecraftServer server;

    public DimensionManager(PrivateDimensionMod mod) { this.mod = mod; }

    public void onServerStart(MinecraftServer server) {
        this.server = server;
        ServerLevel dim = getPrivateDimension();
        if (dim != null) {
            PrivateDimensionMod.LOGGER.info("プライベート次元ワールドを確認しました: {}", DIMENSION_ID);
        } else {
            PrivateDimensionMod.LOGGER.warn("プライベート次元が見つかりません。データパック確認が必要です。");
        }
    }

    public ServerLevel getPrivateDimension() {
        return server == null ? null : server.getLevel(DIMENSION_KEY);
    }

    public boolean isPrivateDimension(ServerLevel level) {
        return level != null && level.dimension().equals(DIMENSION_KEY);
    }

    public void placeStructure(ServerLevel level, BlockPos origin) {
        try {
            ensureNbtExtracted(level);
            StructureTemplateManager stm = level.getServer().getStructureManager();
            ResourceLocation structId = ResourceLocation.fromNamespaceAndPath(PrivateDimensionMod.MOD_ID, "plot48x48");
            Optional<StructureTemplate> opt = stm.get(structId);
            if (opt.isEmpty()) {
                PrivateDimensionMod.LOGGER.error("構造物 {} が見つかりません！", structId);
                return;
            }
            StructurePlaceSettings settings = new StructurePlaceSettings();
            opt.get().placeInWorld(level, origin, origin, settings, level.random, 2);
            PrivateDimensionMod.LOGGER.info("構造物配置完了: {}", origin);
        } catch (Exception e) {
            PrivateDimensionMod.LOGGER.error("構造物配置失敗: {}", e.getMessage());
        }
    }

    private void ensureNbtExtracted(ServerLevel level) throws IOException {
        Path structDir = level.getServer().getWorldPath(LevelResource.ROOT)
            .resolve("generated")
            .resolve(PrivateDimensionMod.MOD_ID)
            .resolve("structures");
        Files.createDirectories(structDir);
        Path dest = structDir.resolve("plot48x48.nbt");

        try (InputStream in = DimensionManager.class.getResourceAsStream("/plot48x48.nbt")) {
            if (in == null) { PrivateDimensionMod.LOGGER.error("plot48x48.nbt リソースなし！"); return; }
            byte[] bytes = in.readAllBytes();
            if (!Files.exists(dest) || !md5Matches(dest, bytes)) {
                Files.write(dest, bytes);
                PrivateDimensionMod.LOGGER.info("plot48x48.nbt を展開しました。");
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
