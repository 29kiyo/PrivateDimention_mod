package dev.keiragi.privatedimension.item;

import dev.keiragi.privatedimension.PrivateDimensionMod;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class DimensionBottleItem extends Item {

    public static final String ITEM_ID = "dimension_bottle";

    public DimensionBottleItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (player.getCooldowns().isOnCooldown(stack)) {
            return InteractionResult.FAIL;
        }
        if (!level.isClientSide && player instanceof net.minecraft.server.level.ServerPlayer sp) {
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
