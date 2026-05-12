package com.sirolf2009.necromancy.network.payload;

import com.sirolf2009.necromancy.Reference;
import com.sirolf2009.necromancy.block.entity.BlockEntityBodypartDev;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.nio.charset.StandardCharsets;

/**
 * Client → Server: requests the bodypart-dev block entity to reload its draft from the
 * on-disk config and then sends the loaded JSON back to the requesting player via
 * {@link BodypartDevLoadResponsePayload}.
 */
public record BodypartDevLoadPayload(BlockPos pos) implements CustomPacketPayload {

    public static final Type<BodypartDevLoadPayload> TYPE = new Type<>(Reference.rl("bodypart_dev_load"));

    public static final StreamCodec<RegistryFriendlyByteBuf, BodypartDevLoadPayload> STREAM_CODEC =
        StreamCodec.composite(
            BlockPos.STREAM_CODEC, BodypartDevLoadPayload::pos,
            BodypartDevLoadPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(BodypartDevLoadPayload msg, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            if (!(ctx.player() instanceof ServerPlayer player)) return;
            if (!player.getAbilities().instabuild && !player.hasPermissions(2)) return;
            if (player.distanceToSqr(net.minecraft.world.phys.Vec3.atCenterOf(msg.pos)) > 100) return;
            BlockEntity be = player.level().getBlockEntity(msg.pos);
            if (!(be instanceof BlockEntityBodypartDev dev)) return;
            // Reload draft from the on-disk config (or default stub if not yet saved).
            dev.refreshDraftForCurrentSlot();
            // Send the freshly loaded JSON back to the requesting player.
            byte[] utf8 = dev.getDraftJson().getBytes(StandardCharsets.UTF_8);
            PacketDistributor.sendToPlayer(player, new BodypartDevLoadResponsePayload(msg.pos, utf8));
        });
    }
}