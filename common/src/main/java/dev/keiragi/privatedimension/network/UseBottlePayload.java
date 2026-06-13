package dev.keiragi.privatedimension.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record UseBottlePayload() implements CustomPacketPayload {
    public static final ResourceLocation ID_LOC = ResourceLocation.fromNamespaceAndPath("privatedimension", "use_bottle");
    public static final Type<UseBottlePayload> TYPE = new Type<>(ID_LOC);
    public static final StreamCodec<FriendlyByteBuf, UseBottlePayload> CODEC =
        StreamCodec.unit(new UseBottlePayload());

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }
}
