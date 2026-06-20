package dev.keiragi.privatedimension.registry;

import dev.keiragi.privatedimension.item.DimensionBottleItem;
import net.minecraft.world.item.Item;

public class ModItems {
    public static DimensionBottleItem DIMENSION_BOTTLE;

    /**
     * 呼び出し側(Fabric/NeoForge)が id を設定済みの Item.Properties を渡すこと。
     * setId() されていない Properties を渡すと、登録時に
     * "Item id not set" の NullPointerException になる。
     */
    public static DimensionBottleItem createDimensionBottle(Item.Properties properties) {
        DIMENSION_BOTTLE = new DimensionBottleItem(properties.stacksTo(1).fireResistant());
        return DIMENSION_BOTTLE;
    }
}
