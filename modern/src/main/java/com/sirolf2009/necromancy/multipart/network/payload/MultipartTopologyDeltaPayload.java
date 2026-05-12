package com.sirolf2009.necromancy.multipart.network.payload;

import com.sirolf2009.necromancy.Reference;
import com.sirolf2009.necromancy.multipart.RootMobEntity;
import com.sirolf2009.necromancy.multipart.network.binary.MultipartSchemaVersions;
import com.sirolf2009.necromancy.multipart.telemetry.MultipartTelemetry;
import com.sirolf2009.necromancy.multipart.telemetry.MultipartTelemetry;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.Entity;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * Server → client: surgical topology / attachment edits. Blob schema {@value MultipartSchemaVersions#TOPOLOGY_DELTA_V1}
 * — see {@link com.sirolf2009.necromancy.multipart.network.binary.MultipartTopologyDeltaBinary}.
 */
public record MultipartTopologyDeltaPayload(int entityId, int topologyRevision, byte[] packedTopologyOps) implements CustomPacketPayload {

    public static final Type<MultipartTopologyDeltaPayload> TYPE = new Type<>(Reference.rl("multipart_topology_delta"));

    public static final StreamCodec<ByteBuf, MultipartTopologyDeltaPayload> STREAM_CODEC =
        StreamCodec.composite(
            ByteBufCodecs.INT, MultipartTopologyDeltaPayload::entityId,
            ByteBufCodecs.INT, MultipartTopologyDeltaPayload::topologyRevision,
            ByteBufCodecs.BYTE_ARRAY, MultipartTopologyDeltaPayload::packedTopologyOps,
            MultipartTopologyDeltaPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(MultipartTopologyDeltaPayload msg, IPayloadContext ctx) {
        MultipartTelemetry.recordNetReceivedTopologyDelta(msg.entityId(), msg.packedTopologyOps());
        ctx.enqueueWork(() -> {
            Entity e = ctx.player() == null ? null : ctx.player().level().getEntity(msg.entityId());
            if (!(e instanceof RootMobEntity r)) return;
            r.multipartConsumeTopologyDelta(msg);
        });
    }
}
