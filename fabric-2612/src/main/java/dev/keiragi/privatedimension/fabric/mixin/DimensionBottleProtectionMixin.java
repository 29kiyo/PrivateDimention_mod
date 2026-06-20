package dev.keiragi.privatedimension.fabric.mixin;

import dev.keiragi.privatedimension.item.DimensionBottleItem;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Dimension in a Bottle がドロップされている間、爆発・炎・溶岩を含む
 * 全てのダメージソースから無条件に保護する。
 * setInvulnerable(true) だけでは爆発でアイテムが消えるケースがあったため、
 * hurtServer 自体をキャンセルすることで確実にダメージを無効化する。
 */
@Mixin(Entity.class)
public abstract class DimensionBottleProtectionMixin {

    @Inject(method = "hurtServer", at = @At("HEAD"), cancellable = true)
    private void privatedimension$cancelBottleDamage(
            ServerLevel level, DamageSource damageSource, float amount,
            CallbackInfoReturnable<Boolean> cir) {
        if ((Object) this instanceof ItemEntity itemEntity
                && DimensionBottleItem.isDimensionBottle(itemEntity.getItem())) {
            cir.setReturnValue(false);
        }
    }
}
