package com.sirolf2009.necromancy.network.payload;

import com.sirolf2009.necromancy.Reference;
import com.sirolf2009.necromancy.block.entity.BlockEntityBodypartDev;
import com.sirolf2009.necromancy.item.ItemDevKnob;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * Client → server: give the player a {@link ItemDevKnob} linked to the specified dev block in the given mode.
 */
public record DevKnobGivePayload(BlockPos pos, int mode, int socketIndex) implements CustomPacketPayload {

    /** Maximum squared distance (blocks²) from the block entity to accept the packet. */
    private static final double MAX_DISTANCE_SQ = 128.0;

    public static final Type<DevKnobGivePayload> TYPE = new Type<>(Reference.rl("dev_knob_give"));

    public static final StreamCodec<RegistryFriendlyByteBuf, DevKnobGivePayload> STREAM_CODEC =
        StreamCodec.composite(
            BlockPos.STREAM_CODEC, DevKnobGivePayload::pos,
            ByteBufCodecs.VAR_INT, DevKnobGivePayload::mode,
            ByteBufCodecs.VAR_INT, DevKnobGivePayload::socketIndex,
            DevKnobGivePayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(DevKnobGivePayload msg, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            if (!(ctx.player() instanceof ServerPlayer player)) return;
            if (!player.getAbilities().instabuild && !player.hasPermissions(2)) return;
            if (player.distanceToSqr(net.minecraft.world.phys.Vec3.atCenterOf(msg.pos)) > MAX_DISTANCE_SQ) return;
            if (!(player.level().getBlockEntity(msg.pos) instanceof BlockEntityBodypartDev)) return;
            int mode = Math.max(0, Math.min(msg.mode(), ItemDevKnob.MODE_MAX));
            int socketIdx = Math.max(0, Math.min(msg.socketIndex(), ItemDevKnob.SOCKET_KNOB_COUNT - 1));
            ItemStack knob = ItemDevKnob.create(mode, msg.pos, socketIdx);
            if (!player.addItem(knob)) {
                player.drop(knob, false);
            }
        });
    }
}
