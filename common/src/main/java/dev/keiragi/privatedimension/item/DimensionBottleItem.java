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

    // 🌟 アイテム名の色を Rarity.RARE と同じ水色にする
    @Override
    public Component getName(ItemStack stack) {
        return super.getName(stack).copy().withStyle(ChatFormatting.AQUA);
    }

    // @Override なし: appendHoverText のシグネチャがバージョンにより異なる
    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
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
    public net.minecraft.world.InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        
        if (player.getCooldowns().isOnCooldown(stack)) {
            return net.minecraft.world.InteractionResult.FAIL;
        }
        
        if (!level.isClientSide() && player instanceof net.minecraft.server.level.ServerPlayer sp) {
            PrivateDimensionMod mod = PrivateDimensionMod.getInstance();
            if (mod != null) {
                
                // 🔊 1. コーラスフルーツのワープ音（標準の音量 1.0）
                level.playSound(
                    null, 
                    player.getX(), player.getY(), player.getZ(), 
                    net.minecraft.sounds.SoundEvents.CHORUS_FRUIT_TELEPORT, 
                    net.minecraft.sounds.SoundSource.PLAYERS, 
                    1.0F, 
                    1.0F
                );

                // 🔊 2. アメジストのチャイム音（音量を 0.4 に抑えて控えめに）
                level.playSound(
                    null, 
                    player.getX(), player.getY(), player.getZ(), 
                    net.minecraft.sounds.SoundEvents.AMETHYST_BLOCK_CHIME, 
                    net.minecraft.sounds.SoundSource.PLAYERS, 
                    0.4F, 
                    1.2F
                );

                mod.getTeleportHandler().handleUse(sp);
                int cooldownTicks = mod.getConfig().cooldownSeconds * 20;
                player.getCooldowns().addCooldown(stack, cooldownTicks);
            }
        }
        
        // 1.21.4以降は SUCCESS ではなく SUCCESS_NO_ITEM_USED などを返すか、環境に応じたResultを返します
        return net.minecraft.world.InteractionResult.SUCCESS;
    }

}
