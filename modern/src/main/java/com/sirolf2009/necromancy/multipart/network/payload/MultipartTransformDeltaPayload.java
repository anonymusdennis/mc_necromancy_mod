package com.sirolf2009.necromancy.multipart.network.payload;

import com.sirolf2009.necromancy.Reference;
import com.sirolf2009.necromancy.multipart.RootMobEntity;
import com.sirolf2009.necromancy.multipart.network.binary.MultipartSchemaVersions;
import com.sirolf2009.necromancy.multipart.telemetry.MultipartTelemetry;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.Entity;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * Server → client: sparse transform blobs using schema {@value MultipartSchemaVersions#TRANSFORM_DELTA_V1} —
 * {@link com.sirolf2009.necromancy.multipart.network.binary.MultipartTransformDeltaBinary}. Revision guards stale data.
 */
public record MultipartTransformDeltaPayload(int entityId, long transformDirtyRevision, byte[] packedOps) implements CustomPacketPayload {

    public static final Type<MultipartTransformDeltaPayload> TYPE = new Type<>(Reference.rl("multipart_transform_delta"));

    public static final StreamCodec<ByteBuf, MultipartTransformDeltaPayload> STREAM_CODEC =
        StreamCodec.composite(
            ByteBufCodecs.INT, MultipartTransformDeltaPayload::entityId,
            ByteBufCodecs.VAR_LONG, MultipartTransformDeltaPayload::transformDirtyRevision,
            ByteBufCodecs.BYTE_ARRAY, MultipartTransformDeltaPayload::packedOps,
            MultipartTransformDeltaPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(MultipartTransformDeltaPayload msg, IPayloadContext ctx) {
        MultipartTelemetry.recordNetReceivedTransformDelta(msg.entityId(), msg.packedOps());
        ctx.enqueueWork(() -> {
            Entity e = ctx.player() == null ? null : ctx.player().level().getEntity(msg.entityId());
            if (!(e instanceof RootMobEntity r)) return;
            r.multipartConsumeTransformDelta(msg);
        });
    }
}
