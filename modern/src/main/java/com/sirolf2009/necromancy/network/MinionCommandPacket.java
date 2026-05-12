package com.sirolf2009.necromancy.network;

import com.sirolf2009.necromancy.Reference;
import com.sirolf2009.necromancy.entity.EntityMinion;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.Entity;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * Server-bound payload for the Necronomicon screen.  Triggered when the
 * player presses Sit/Follow or Dismiss on a tamed minion.
 *
 * <p>The legacy mod opened a server-side container for this; we use a single
 * tiny payload because there is nothing to keep open.  Op codes are encoded
 * as bytes for forward compatibility with new actions.
 */
public record MinionCommandPacket(int entityId, byte op) implements CustomPacketPayload {

    public enum Op { TOGGLE_SIT, DISMISS;
        public byte code() { return (byte) ordinal(); }
        public static Op of(byte b) { return values()[b & 0xFF]; }
    }

    public MinionCommandPacket(int entityId, Op op) { this(entityId, op.code()); }

    public static final Type<MinionCommandPacket> TYPE = new Type<>(Reference.rl("minion_command"));

    public static final StreamCodec<ByteBuf, MinionCommandPacket> STREAM_CODEC =
        StreamCodec.composite(
            ByteBufCodecs.INT, MinionCommandPacket::entityId,
            ByteBufCodecs.BYTE, MinionCommandPacket::op,
            MinionCommandPacket::new);

    @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }

    public static void handle(MinionCommandPacket msg, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            if (ctx.player() == null) return;
            Entity e = ctx.player().level().getEntity(msg.entityId());
            if (!(e instanceof EntityMinion minion)) return;
            if (!ctx.player().getUUID().equals(minion.getOwnerUUID())) return;
            switch (Op.of(msg.op())) {
                case TOGGLE_SIT -> minion.setOrderedToSit(!minion.isOrderedToSit());
                case DISMISS -> minion.discard();
            }
        });
    }
}
