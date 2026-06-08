package dev.keiragi.privatedimension.item;

import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.ItemLore;

import java.util.List;
import java.util.Optional;

public class DimensionBottleItem {

    public static final String ITEM_ID = "dimension_in_a_bottle";
    public static final String NBT_KEY = "privatedimension_item_id";

    public static ItemStack createItem() {
        ItemStack stack = new ItemStack(Items.LINGERING_POTION);

        stack.set(DataComponents.CUSTOM_NAME,
            Component.literal("Dimension in a Bottle")
                .withStyle(Style.EMPTY.withColor(0x40BFFF).withItalic(false)));

        stack.set(DataComponents.LORE, new ItemLore(List.of(
            Component.empty(),
            Component.literal("使用すると、別世界の空間へと移動する。")
                .withStyle(Style.EMPTY.withColor(0xFFFFFF).withItalic(false)),
            Component.literal("次元内で再び使用すると、元の世界へ戻る。")
                .withStyle(Style.EMPTY.withColor(0xFFFFFF).withItalic(false)),
            Component.empty(),
            Component.literal("\"この小さな丸い瓶の中には、")
                .withStyle(Style.EMPTY.withColor(0xAAAAAA).withItalic(false)),
            Component.literal(" どういうわけか別の世界が詰まっている\"")
                .withStyle(Style.EMPTY.withColor(0xAAAAAA).withItalic(false))
        )));

        stack.set(DataComponents.POTION_CONTENTS,
            new PotionContents(Optional.empty(), Optional.of(0x40BFFF), List.of(), Optional.empty()));

        stack.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);

        CompoundTag tag = new CompoundTag();
        tag.putString(NBT_KEY, ITEM_ID);
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));

        return stack;
    }

    public static boolean isDimensionBottle(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;
        if (!stack.is(Items.LINGERING_POTION)) return false;
        CustomData cd = stack.get(DataComponents.CUSTOM_DATA);
        if (cd == null) return false;
        return ITEM_ID.equals(cd.copyTag().getString(NBT_KEY));
    }
}
