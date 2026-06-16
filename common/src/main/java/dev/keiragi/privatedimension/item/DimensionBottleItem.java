package dev.keiragi.privatedimension.item;

import dev.keiragi.privatedimension.PrivateDimensionMod;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

public class DimensionBottleItem extends Item {
    public static final String ITEM_ID = "dimension_bottle";

    public DimensionBottleItem(Properties properties) {
        super(properties);
    }

    public static boolean isDimensionBottle(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;
        return stack.getItem() instanceof DimensionBottleItem;
    }

    public static ItemStack createItem() {
        return dev.keiragi.privatedimension.registry.ModItems.DIMENSION_BOTTLE != null
            ? new ItemStack(dev.keiragi.privatedimension.registry.ModItems.DIMENSION_BOTTLE)
            : ItemStack.EMPTY;
    }

    /** エンチャントグリッターを表示 */
    @Override
    public boolean isFoil(ItemStack stack) {
        return true;
    }

    @Override
    public Component getName(ItemStack stack) {
        return Component.translatable("item.privatedimension.dimension_bottle")
            .withStyle(ChatFormatting.AQUA);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.literal(""));
        tooltip.add(Component.translatable("item.privatedimension.dimension_bottle.desc1")
            .withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("item.privatedimension.dimension_bottle.desc2")
            .withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.literal(""));
        tooltip.add(Component.literal("\"この小さな丸い瓶の中には、")
            .withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.ITALIC));
        tooltip.add(Component.literal(" どういうわけか別の世界が詰まっている\"")
            .withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.ITALIC));
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (player.getCooldowns().isOnCooldown(stack)) {
            return InteractionResult.FAIL;
        }
        if (!level.isClientSide() && player instanceof net.minecraft.server.level.ServerPlayer sp) {
            PrivateDimensionMod mod = PrivateDimensionMod.getInstance();
            if (mod != null) {
                mod.getTeleportHandler().handleUse(sp);
                int cooldownTicks = mod.getConfig().cooldownSeconds * 20;
                player.getCooldowns().addCooldown(stack, cooldownTicks);
            }
        }
        return InteractionResult.SUCCESS;
    }
}
