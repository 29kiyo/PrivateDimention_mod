package dev.keiragi.privatedimension.network;

import dev.keiragi.privatedimension.util.IdUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record UseBottlePayload() implements CustomPacketPayload {
    @SuppressWarnings("unchecked")
    public static final Type<UseBottlePayload> TYPE = new Type<>(
        (net.minecraft.resources.ResourceLocation) IdUtils.createId("privatedimension", "use_bottle")
    );
    public static final StreamCodec<FriendlyByteBuf, UseBottlePayload> CODEC =
        StreamCodec.unit(new UseBottlePayload());

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }
}
