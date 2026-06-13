package dev.keiragi.privatedimension.fabric;

import dev.keiragi.privatedimension.item.DimensionBottleItem;
import dev.keiragi.privatedimension.fabric.network.UseBottlePayload;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.item.ItemStack;

@Environment(EnvType.CLIENT)
public class PrivateDimensionFabricClient implements ClientModInitializer {

    private boolean wasRightClickDown = false;

    @Override
    public void onInitializeClient() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null || client.level == null) return;
            LocalPlayer player = client.player;

            boolean isRightClickDown = client.options.useKey.isDown();

            if (isRightClickDown && !wasRightClickDown) {
                ItemStack stack = player.getMainHandItem();
                if (DimensionBottleItem.isDimensionBottle(stack)) {
                    ClientPlayNetworking.send(new UseBottlePayload());
                }
            }
            wasRightClickDown = isRightClickDown;
        });
    }
}
