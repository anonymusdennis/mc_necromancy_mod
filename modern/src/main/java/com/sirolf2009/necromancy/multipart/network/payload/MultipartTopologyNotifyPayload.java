package com.sirolf2009.necromancy.multipart.network.payload;

import com.sirolf2009.necromancy.Reference;
import com.sirolf2009.necromancy.multipart.RootMobEntity;
import com.sirolf2009.necromancy.multipart.telemetry.MultipartTelemetry;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.Entity;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * Server → client: revision ping after topology edits so clients invalidate caches / request follow-up deltas.
 */
public record MultipartTopologyNotifyPayload(int entityId, int topologyRevision, long transformDirtyRevision,
                                             long propagationSerial) implements CustomPacketPayload {

    public static final Type<MultipartTopologyNotifyPayload> TYPE = new Type<>(Reference.rl("multipart_topology_notify"));

    public static final StreamCodec<ByteBuf, MultipartTopologyNotifyPayload> STREAM_CODEC =
        StreamCodec.composite(
            ByteBufCodecs.INT, MultipartTopologyNotifyPayload::entityId,
            ByteBufCodecs.INT, MultipartTopologyNotifyPayload::topologyRevision,
            ByteBufCodecs.VAR_LONG, MultipartTopologyNotifyPayload::transformDirtyRevision,
            ByteBufCodecs.VAR_LONG, MultipartTopologyNotifyPayload::propagationSerial,
            MultipartTopologyNotifyPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(MultipartTopologyNotifyPayload msg, IPayloadContext ctx) {
        MultipartTelemetry.recordNetReceivedTopologyNotify(msg.entityId());
        ctx.enqueueWork(() -> {
            Entity e = ctx.player() == null ? null : ctx.player().level().getEntity(msg.entityId());
            if (!(e instanceof RootMobEntity r)) return;
            r.multipartConsumeTopologyNotify(msg.topologyRevision(), msg.transformDirtyRevision(), msg.propagationSerial());
        });
    }
}
