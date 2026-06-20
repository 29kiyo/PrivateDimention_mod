package dev.keiragi.privatedimension.neoforge.mixin;

import dev.keiragi.privatedimension.item.DimensionBottleItem;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.item.ItemEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Dimension in a Bottle がドロップされている間、爆発・炎・溶岩を含む
 * 全てのダメージソースから無条件に保護する。
 * Entity#hurtServer は抽象メソッドで本体を持たないため、
 * 実装本体を持つ ItemEntity 側をMixin対象にする。
 */
@Mixin(ItemEntity.class)
public abstract class DimensionBottleProtectionMixin {

    @Inject(method = "hurtServer", at = @At("HEAD"), cancellable = true)
    private void privatedimension$cancelBottleDamage(
            ServerLevel level, DamageSource damageSource, float amount,
            CallbackInfoReturnable<Boolean> cir) {
        ItemEntity self = (ItemEntity) (Object) this;
        if (DimensionBottleItem.isDimensionBottle(self.getItem())) {
            cir.setReturnValue(false);
        }
    }
}
