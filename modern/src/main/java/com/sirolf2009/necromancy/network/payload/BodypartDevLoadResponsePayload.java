package com.sirolf2009.necromancy.network.payload;

import com.sirolf2009.necromancy.Reference;
import com.sirolf2009.necromancy.client.screen.ScreenBodypartDev;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.nio.charset.StandardCharsets;

/**
 * Server → Client: delivers the JSON that the server loaded from disk in response to a
 * {@link BodypartDevLoadPayload} request. The client handler forwards the JSON to
 * the currently open {@link ScreenBodypartDev} so it can refresh its draft and widgets.
 */
public record BodypartDevLoadResponsePayload(BlockPos pos, byte[] jsonUtf8) implements CustomPacketPayload {

    public static final Type<BodypartDevLoadResponsePayload> TYPE =
        new Type<>(Reference.rl("bodypart_dev_load_response"));

    public static final StreamCodec<RegistryFriendlyByteBuf, BodypartDevLoadResponsePayload> STREAM_CODEC =
        StreamCodec.composite(
            BlockPos.STREAM_CODEC, BodypartDevLoadResponsePayload::pos,
            ByteBufCodecs.BYTE_ARRAY, BodypartDevLoadResponsePayload::jsonUtf8,
            BodypartDevLoadResponsePayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(BodypartDevLoadResponsePayload msg, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            Minecraft mc = Minecraft.getInstance();
            if (mc.screen instanceof ScreenBodypartDev screen
                    && screen.getMenu().getDevBlockPos().equals(msg.pos)) {
                String json = new String(msg.jsonUtf8, StandardCharsets.UTF_8);
                screen.receiveLoadFromDisk(json);
            }
        });
    }
}
