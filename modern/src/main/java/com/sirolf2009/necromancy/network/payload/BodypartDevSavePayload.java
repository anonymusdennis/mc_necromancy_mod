package com.sirolf2009.necromancy.network.payload;

import com.sirolf2009.necromancy.Reference;
import com.sirolf2009.necromancy.block.entity.BlockEntityBodypartDev;
import com.sirolf2009.necromancy.bodypart.BodyPartConfigManager;
import com.sirolf2009.necromancy.bodypart.BodyPartItemIds;
import com.sirolf2009.necromancy.bodypart.BodypartDefinitionIo;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.io.IOException;

public record BodypartDevSavePayload(BlockPos pos) implements CustomPacketPayload {

    public static final Type<BodypartDevSavePayload> TYPE = new Type<>(Reference.rl("bodypart_dev_save"));

    public static final StreamCodec<RegistryFriendlyByteBuf, BodypartDevSavePayload> STREAM_CODEC =
        StreamCodec.composite(
            BlockPos.STREAM_CODEC, BodypartDevSavePayload::pos,
            BodypartDevSavePayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(BodypartDevSavePayload msg, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            if (!(ctx.player() instanceof ServerPlayer player)) return;
            if (!player.getAbilities().instabuild && !player.hasPermissions(2)) return;
            if (player.distanceToSqr(net.minecraft.world.phys.Vec3.atCenterOf(msg.pos)) > 100) return;
            BlockEntity be = player.level().getBlockEntity(msg.pos);
            if (!(be instanceof BlockEntityBodypartDev dev)) return;
            var stack = dev.getItem(BlockEntityBodypartDev.SLOT_PART);
            if (stack.isEmpty()) {
                player.displayClientMessage(Component.translatable("message.necromancy.bodypart_dev.need_stack"), true);
                return;
            }
            var id = BodyPartItemIds.partId(stack.getItem());
            if (id == null) return;
            try {
                var json = BodypartDefinitionIo.fromJson(dev.getDraftJson());
                json.id = id.toString();
                json.validated = Boolean.TRUE;
                BodyPartConfigManager.INSTANCE.saveDefinitionOverwrite(id, json);
                BodyPartConfigManager.INSTANCE.reloadFromDisk();
                player.displayClientMessage(Component.translatable("message.necromancy.bodypart_dev.saved"), true);
            } catch (IOException e) {
                player.displayClientMessage(Component.translatable("message.necromancy.bodypart_dev.save_fail"), true);
            }
        });
    }
}
