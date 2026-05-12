package com.sirolf2009.necromancy.multipart.network.payload;

import com.sirolf2009.necromancy.Reference;
import com.sirolf2009.necromancy.multipart.RootMobEntity;
import com.sirolf2009.necromancy.multipart.telemetry.MultipartTelemetry;
import com.sirolf2009.necromancy.multipart.network.binary.MultipartSchemaVersions;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.Entity;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * Server → client: batched per-part HP / flags / sever events. Blob schema {@value MultipartSchemaVersions#DAMAGE_SYNC_V1}
 * — {@link com.sirolf2009.necromancy.multipart.network.binary.MultipartDamageDeltaBinary}.
 */
public record MultipartDamageSyncPayload(int entityId, long sequence, byte[] packedDamage) implements CustomPacketPayload {

    public static final Type<MultipartDamageSyncPayload> TYPE = new Type<>(Reference.rl("multipart_damage_sync"));

    public static final StreamCodec<ByteBuf, MultipartDamageSyncPayload> STREAM_CODEC =
        StreamCodec.composite(
            ByteBufCodecs.INT, MultipartDamageSyncPayload::entityId,
            ByteBufCodecs.VAR_LONG, MultipartDamageSyncPayload::sequence,
            ByteBufCodecs.BYTE_ARRAY, MultipartDamageSyncPayload::packedDamage,
            MultipartDamageSyncPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(MultipartDamageSyncPayload msg, IPayloadContext ctx) {
        MultipartTelemetry.recordNetReceivedDamageSync(msg.entityId(), msg.packedDamage());
        ctx.enqueueWork(() -> {
            Entity e = ctx.player() == null ? null : ctx.player().level().getEntity(msg.entityId());
            if (!(e instanceof RootMobEntity r)) return;
            r.multipartConsumeDamageSync(msg);
        });
    }
}
