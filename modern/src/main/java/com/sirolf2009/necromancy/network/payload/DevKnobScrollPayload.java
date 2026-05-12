package com.sirolf2009.necromancy.network.payload;

import com.sirolf2009.necromancy.Reference;
import com.sirolf2009.necromancy.block.entity.BlockEntityBodypartDev;
import com.sirolf2009.necromancy.bodypart.BodypartAttachmentJson;
import com.sirolf2009.necromancy.bodypart.BodypartDefinitionIo;
import com.sirolf2009.necromancy.bodypart.BodypartDefinitionJson;
import com.sirolf2009.necromancy.bodypart.BodypartHitboxJson;
import com.sirolf2009.necromancy.bodypart.BodypartVisualOffsetJson;
import com.sirolf2009.necromancy.item.ItemDevKnob;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.ArrayList;

/**
 * Client → server: apply a scalar delta to a specific field in the bodypart dev block's draft.
 * Axis: 0=X, 1=Y, 2=Z (positional) or 0=yaw, 1=pitch, 2=roll (rotational).
 */
public record DevKnobScrollPayload(BlockPos pos, int mode, int axis, double delta) implements CustomPacketPayload {

    public static final Type<DevKnobScrollPayload> TYPE = new Type<>(Reference.rl("dev_knob_scroll"));

    public static final net.minecraft.network.codec.StreamCodec<RegistryFriendlyByteBuf, DevKnobScrollPayload> STREAM_CODEC =
        new net.minecraft.network.codec.StreamCodec<>() {
            @Override
            public DevKnobScrollPayload decode(RegistryFriendlyByteBuf buf) {
                BlockPos pos = BlockPos.STREAM_CODEC.decode(buf);
                int mode = buf.readVarInt();
                int axis = buf.readVarInt();
                double delta = buf.readDouble();
                return new DevKnobScrollPayload(pos, mode, axis, delta);
            }

            @Override
            public void encode(RegistryFriendlyByteBuf buf, DevKnobScrollPayload val) {
                BlockPos.STREAM_CODEC.encode(buf, val.pos());
                buf.writeVarInt(val.mode());
                buf.writeVarInt(val.axis());
                buf.writeDouble(val.delta());
            }
        };

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(DevKnobScrollPayload msg, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            if (!(ctx.player() instanceof ServerPlayer player)) return;
            if (!player.getAbilities().instabuild && !player.hasPermissions(2)) return;
            if (player.distanceToSqr(net.minecraft.world.phys.Vec3.atCenterOf(msg.pos)) > 128) return;
            if (!(player.level().getBlockEntity(msg.pos) instanceof BlockEntityBodypartDev dev)) return;
            try {
                BodypartDefinitionJson json = BodypartDefinitionIo.fromJson(dev.getDraftJson());
                applyDelta(json, msg.mode(), msg.axis(), msg.delta());
                dev.setDraftJson(BodypartDefinitionIo.toJson(json));
            } catch (Exception ignored) {
                // Malformed draft — skip silently
            }
        });
    }

    /** Apply delta to the appropriate field of {@code json} based on mode and axis. */
    private static void applyDelta(BodypartDefinitionJson json, int mode, int axis, double delta) {
        if (json.hitbox == null) json.hitbox = new BodypartHitboxJson();
        if (json.visualOffset == null) json.visualOffset = new BodypartVisualOffsetJson();
        if (json.attachments == null) json.attachments = new ArrayList<>();

        switch (mode) {
            case ItemDevKnob.MODE_HITBOX_POS:
                if (axis == 0) json.hitbox.ox += delta;
                else if (axis == 1) json.hitbox.oy += delta;
                else json.hitbox.oz += delta;
                break;

            case ItemDevKnob.MODE_HITBOX_SIZE:
                if (axis == 0) json.hitbox.sx = Math.max(1e-4, json.hitbox.sx + delta);
                else if (axis == 1) json.hitbox.sy = Math.max(1e-4, json.hitbox.sy + delta);
                else json.hitbox.sz = Math.max(1e-4, json.hitbox.sz + delta);
                break;

            case ItemDevKnob.MODE_VISUAL_OFFSET:
                if (axis == 0) json.visualOffset.dx += delta;
                else if (axis == 1) json.visualOffset.dy += delta;
                else json.visualOffset.dz += delta;
                break;

            case ItemDevKnob.MODE_VISUAL_ROT:
                if (axis == 0) json.visualOffset.rotYawDeg += delta;
                else if (axis == 1) json.visualOffset.rotPitchDeg += delta;
                else json.visualOffset.rotRollDeg += delta;
                break;

            case ItemDevKnob.MODE_ATTACH_POS: {
                BodypartAttachmentJson at = primaryAttachment(json);
                if (axis == 0) at.ox += delta;
                else if (axis == 1) at.oy += delta;
                else at.oz += delta;
                break;
            }

            case ItemDevKnob.MODE_ATTACH_ROT: {
                BodypartAttachmentJson at = primaryAttachment(json);
                if (axis == 0) at.eulerYawDeg = nvl(at.eulerYawDeg) + delta;
                else if (axis == 1) at.eulerPitchDeg = nvl(at.eulerPitchDeg) + delta;
                else at.eulerRollDeg = nvl(at.eulerRollDeg) + delta;
                break;
            }

            default:
                break;
        }
    }

    private static BodypartAttachmentJson primaryAttachment(BodypartDefinitionJson json) {
        if (json.attachments.isEmpty()) {
            json.attachments.add(new BodypartAttachmentJson());
        }
        return json.attachments.get(0);
    }

    private static double nvl(Double v) {
        return v != null ? v : 0.0;
    }
}
