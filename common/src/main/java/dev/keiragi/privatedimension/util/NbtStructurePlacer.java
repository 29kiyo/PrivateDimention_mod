package dev.keiragi.privatedimension.util;

import dev.keiragi.privatedimension.PrivateDimensionMod;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * NBTファイルを直接パースしてブロックを配置するユーティリティ。
 * StructureTemplateManagerを使わないため全バージョンで動作する。
 */
public class NbtStructurePlacer {

    public static boolean place(ServerLevel level, BlockPos origin, Path nbtPath) {
        try {
            if (!Files.exists(nbtPath)) {
                PrivateDimensionMod.LOGGER.error("NBTファイルが存在しません: {}", nbtPath);
                return false;
            }

            CompoundTag root;
            try (InputStream is = Files.newInputStream(nbtPath)) {
                root = NbtIo.readCompressed(is, NbtAccounter.unlimitedHeap());
            }

            // ブロックレジストリをlevel経由で取得（全バージョン共通）
            Object blockRegistry = level.registryAccess()
                .lookupOrThrow(net.minecraft.core.registries.Registries.BLOCK);

            ListTag paletteTag = root.getList("palette").orElse(new ListTag());
            List<BlockState> palette = new ArrayList<>();
            for (int i = 0; i < paletteTag.size(); i++) {
                CompoundTag entry = paletteTag.getCompound(i).orElse(new CompoundTag());
                String name = entry.getString("Name").orElse("");
                if (name.isEmpty()) {
                    palette.add(Blocks.AIR.defaultBlockState());
                    continue;
                }
                try {
                    int colon = name.indexOf(':');
                    String namespace = colon >= 0 ? name.substring(0, colon) : "minecraft";
                    String path     = colon >= 0 ? name.substring(colon + 1) : name;
                    // IdUtils経由でIdentifier/ResourceLocationを吸収
                    Object block = IdUtils.registryGetValue(blockRegistry, namespace, path);
                    if (!(block instanceof Block) || (block == Blocks.AIR && !name.contains("air"))) {
                        palette.add(Blocks.AIR.defaultBlockState());
                    } else {
                        BlockState state = ((Block) block).defaultBlockState();
                        if (entry.contains("Properties")) {
                            CompoundTag props = entry.getCompound("Properties").orElse(new CompoundTag());
                            for (String key : props.keySet()) {
                                String val = props.getString(key).orElse("");
                                if (!val.isEmpty()) {
                                    state = applyProperty(state, key, val);
                                }
                            }
                        }
                        palette.add(state);
                    }
                } catch (Exception e) {
                    PrivateDimensionMod.LOGGER.warn("ブロック解析失敗: {} -> {}", name, e.getMessage());
                    palette.add(Blocks.AIR.defaultBlockState());
                }
            }

            ListTag blocks = root.getList("blocks").orElse(new ListTag());
            int placed = 0;
            for (int i = 0; i < blocks.size(); i++) {
                CompoundTag blockEntry = blocks.getCompound(i).orElse(new CompoundTag());
                int stateIdx = blockEntry.getInt("state").orElse(0);
                ListTag posTag = blockEntry.getList("pos").orElse(new ListTag());
                int x = posTag.getInt(0).orElse(0);
                int y = posTag.getInt(1).orElse(0);
                int z = posTag.getInt(2).orElse(0);

                if (stateIdx < 0 || stateIdx >= palette.size()) continue;
                BlockState state = palette.get(stateIdx);

                BlockPos target = origin.offset(x, y, z);
                level.setBlock(target, state, Block.UPDATE_ALL);
                placed++;
            }

            PrivateDimensionMod.LOGGER.info("NbtStructurePlacer: {}ブロック配置完了 at {}", placed, origin);
            return placed > 0;

        } catch (Exception e) {
            PrivateDimensionMod.LOGGER.error("NbtStructurePlacer失敗: {}", e.getMessage(), e);
            return false;
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static BlockState applyProperty(BlockState state, String key, String value) {
        try {
            for (net.minecraft.world.level.block.state.properties.Property<?> prop :
                    state.getProperties()) {
                if (prop.getName().equals(key)) {
                    java.util.Optional<?> parsed = prop.getValue(value);
                    if (parsed.isPresent()) {
                        state = state.setValue(
                                (net.minecraft.world.level.block.state.properties.Property) prop,
                                (Comparable) parsed.get());
                    }
                    break;
                }
            }
        } catch (Exception ignored) {}
        return state;
    }
}
