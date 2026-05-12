package com.sirolf2009.necromancy.network.payload;

import com.sirolf2009.necromancy.Reference;
import com.sirolf2009.necromancy.block.entity.BlockEntityBodypartDev;
import com.sirolf2009.necromancy.bodypart.BodypartDefinitionIo;
import com.sirolf2009.necromancy.bodypart.BodyPartItemIds;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.nio.charset.StandardCharsets;

public record BodypartDevApplyPayload(BlockPos pos, byte[] draftUtf8, int previewVisibilityMask) implements CustomPacketPayload {

    public static final Type<BodypartDevApplyPayload> TYPE = new Type<>(Reference.rl("bodypart_dev_apply"));

    public static final StreamCodec<RegistryFriendlyByteBuf, BodypartDevApplyPayload> STREAM_CODEC =
        StreamCodec.composite(
            BlockPos.STREAM_CODEC, BodypartDevApplyPayload::pos,
            ByteBufCodecs.BYTE_ARRAY, BodypartDevApplyPayload::draftUtf8,
            ByteBufCodecs.VAR_INT, BodypartDevApplyPayload::previewVisibilityMask,
            BodypartDevApplyPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(BodypartDevApplyPayload msg, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            if (!(ctx.player() instanceof ServerPlayer player)) return;
            if (!player.getAbilities().instabuild && !player.hasPermissions(2)) return;
            if (player.distanceToSqr(net.minecraft.world.phys.Vec3.atCenterOf(msg.pos)) > 100) return;
            BlockEntity be = player.level().getBlockEntity(msg.pos);
            if (!(be instanceof BlockEntityBodypartDev dev)) return;
            try {
                String raw = new String(msg.draftUtf8, StandardCharsets.UTF_8);
                var json = BodypartDefinitionIo.fromJson(raw);
                var stack = dev.getItem(BlockEntityBodypartDev.SLOT_PART);
                if (stack.isEmpty()) return;
                var expected = BodyPartItemIds.partId(stack.getItem());
                if (expected == null || json.id == null || !net.minecraft.resources.ResourceLocation.parse(json.id).equals(expected)) {
                    player.displayClientMessage(Component.translatable("message.necromancy.bodypart_dev.id_mismatch"), true);
                    return;
                }
                dev.setDraftJson(raw);
                dev.setPreviewVisibilityMask(msg.previewVisibilityMask);
            } catch (Exception e) {
                player.displayClientMessage(Component.translatable("message.necromancy.bodypart_dev.bad_json"), true);
            }
        });
    }
}
