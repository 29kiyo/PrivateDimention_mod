package dev.keiragi.privatedimension.registry;

import dev.keiragi.privatedimension.item.DimensionBottleItem;
import net.minecraft.world.item.Item;

public class ModItems {
    public static DimensionBottleItem DIMENSION_BOTTLE;

    public static DimensionBottleItem createDimensionBottle() {
        DIMENSION_BOTTLE = new DimensionBottleItem(new Item.Properties().stacksTo(1));
        return DIMENSION_BOTTLE;
    }
}
