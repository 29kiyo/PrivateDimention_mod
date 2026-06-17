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

    @Override
    public boolean isFoil(ItemStack stack) {
        return true;
    }

    @Override
    public boolean canBeHurtBy(net.minecraft.world.damagesource.DamageSource source) {
        // サボテン、爆発、マグマなどのあらゆるダメージを無効化（無敵化）
        return false;
    }

    @Override
    public boolean isFireResistant() {
        // ネザライトのように炎や溶岩で燃え尽きないようにする
        return true;
    }

    // 🌟 アイテム名の色を Rarity.RARE と同じ水色にする
    @Override
    public Component getName(ItemStack stack) {
        return super.getName(stack).copy().withStyle(ChatFormatting.AQUA);
    }

    // @Override なし: appendHoverText のシグネチャがバージョンにより異なる
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.empty());
        tooltip.add(Component.translatable("item.privatedimension.dimension_bottle.desc1")
            .withStyle(ChatFormatting.WHITE));
        tooltip.add(Component.translatable("item.privatedimension.dimension_bottle.desc2")
            .withStyle(ChatFormatting.WHITE));
        tooltip.add(Component.empty());
        tooltip.add(Component.translatable("item.privatedimension.dimension_bottle.quote.line1")
            .withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
        tooltip.add(Component.translatable("item.privatedimension.dimension_bottle.quote.line2")
            .withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC)); 
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
