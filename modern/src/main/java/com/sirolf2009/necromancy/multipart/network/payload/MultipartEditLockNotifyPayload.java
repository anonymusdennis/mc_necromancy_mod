package com.sirolf2009.necromancy.multipart.network.payload;

import com.sirolf2009.necromancy.Reference;
import com.sirolf2009.necromancy.multipart.network.MultipartClientEditLockCache;
import com.sirolf2009.necromancy.multipart.telemetry.MultipartTelemetry;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * Server → tracking clients: authoritative multipart topology lease visibility for UI / debug overlays.
 */
public record MultipartEditLockNotifyPayload(int entityId, boolean locked, @Nullable UUID holderUuid,
                                            long sessionGeneration) implements CustomPacketPayload {

    public static final Type<MultipartEditLockNotifyPayload> TYPE = new Type<>(Reference.rl("multipart_edit_lock"));

    public static final StreamCodec<ByteBuf, MultipartEditLockNotifyPayload> STREAM_CODEC =
        StreamCodec.of(MultipartEditLockNotifyPayload::encode, MultipartEditLockNotifyPayload::decode);

    private static void encode(ByteBuf buf, MultipartEditLockNotifyPayload msg) {
        FriendlyByteBuf f = new FriendlyByteBuf(buf);
        f.writeInt(msg.entityId());
        f.writeBoolean(msg.locked());
        if (msg.holderUuid() != null) {
            f.writeBoolean(true);
            f.writeUUID(msg.holderUuid());
        } else {
            f.writeBoolean(false);
        }
        f.writeVarLong(msg.sessionGeneration());
    }

    private static MultipartEditLockNotifyPayload decode(ByteBuf buf) {
        FriendlyByteBuf f = new FriendlyByteBuf(buf);
        int entityId = f.readInt();
        boolean locked = f.readBoolean();
        UUID holder = f.readBoolean() ? f.readUUID() : null;
        long gen = f.readVarLong();
        return new MultipartEditLockNotifyPayload(entityId, locked, holder, gen);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(MultipartEditLockNotifyPayload msg, IPayloadContext ctx) {
        MultipartTelemetry.recordNetReceivedEditLock(msg.entityId());
        ctx.enqueueWork(() ->
            MultipartClientEditLockCache.apply(msg.entityId(), msg.locked(), msg.holderUuid(), msg.sessionGeneration()));
    }
}
