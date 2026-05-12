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
 * {@code socketIndex} is used for {@link ItemDevKnob#MODE_SOCKET_POS} and {@link ItemDevKnob#MODE_SOCKET_ROT};
 * if the bodypart has fewer sockets than the requested index, the packet is a no-op.
 */
public record DevKnobScrollPayload(BlockPos pos, int mode, int axis, double delta, int socketIndex) implements CustomPacketPayload {

    /** Maximum squared distance (blocks²) from the block entity to accept the packet. */
    private static final double MAX_DISTANCE_SQ = 128.0;

    /** Minimum hitbox full-extent to prevent collapsing to zero or negative size. */
    private static final double MIN_HITBOX_EXTENT = 1e-4;

    /** Minimum visual scale to prevent inverting the mesh. */
    private static final double MIN_SCALE = 1e-4;

    public static final Type<DevKnobScrollPayload> TYPE = new Type<>(Reference.rl("dev_knob_scroll"));

    public static final net.minecraft.network.codec.StreamCodec<RegistryFriendlyByteBuf, DevKnobScrollPayload> STREAM_CODEC =
        new net.minecraft.network.codec.StreamCodec<>() {
            @Override
            public DevKnobScrollPayload decode(RegistryFriendlyByteBuf buf) {
                BlockPos pos = BlockPos.STREAM_CODEC.decode(buf);
                int mode = buf.readVarInt();
                int axis = buf.readVarInt();
                double delta = buf.readDouble();
                int socketIndex = buf.readVarInt();
                return new DevKnobScrollPayload(pos, mode, axis, delta, socketIndex);
            }

            @Override
            public void encode(RegistryFriendlyByteBuf buf, DevKnobScrollPayload val) {
                BlockPos.STREAM_CODEC.encode(buf, val.pos());
                buf.writeVarInt(val.mode());
                buf.writeVarInt(val.axis());
                buf.writeDouble(val.delta());
                buf.writeVarInt(val.socketIndex());
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
            if (player.distanceToSqr(net.minecraft.world.phys.Vec3.atCenterOf(msg.pos)) > MAX_DISTANCE_SQ) return;
            if (!(player.level().getBlockEntity(msg.pos) instanceof BlockEntityBodypartDev dev)) return;
            try {
                BodypartDefinitionJson json = BodypartDefinitionIo.fromJson(dev.getDraftJson());
                applyDelta(json, msg.mode(), msg.axis(), msg.delta(), msg.socketIndex());
                dev.setDraftJson(BodypartDefinitionIo.toJson(json));
            } catch (Exception ignored) {
                // Malformed draft — skip silently
            }
        });
    }

    /** Apply delta to the appropriate field of {@code json} based on mode, axis and socketIndex. */
    private static void applyDelta(BodypartDefinitionJson json, int mode, int axis, double delta, int socketIndex) {
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
                if (axis == 0) json.hitbox.sx = Math.max(MIN_HITBOX_EXTENT, json.hitbox.sx + delta);
                else if (axis == 1) json.hitbox.sy = Math.max(MIN_HITBOX_EXTENT, json.hitbox.sy + delta);
                else json.hitbox.sz = Math.max(MIN_HITBOX_EXTENT, json.hitbox.sz + delta);
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

            case ItemDevKnob.MODE_SCALE: {
                // Normalize 0.0 (absent in old JSON) to 1.0 before applying delta
                double curSx = json.visualOffset.scaleX > 0 ? json.visualOffset.scaleX : 1.0;
                double curSy = json.visualOffset.scaleY > 0 ? json.visualOffset.scaleY : 1.0;
                double curSz = json.visualOffset.scaleZ > 0 ? json.visualOffset.scaleZ : 1.0;
                if (axis == 0) json.visualOffset.scaleX = Math.max(MIN_SCALE, curSx + delta);
                else if (axis == 1) json.visualOffset.scaleY = Math.max(MIN_SCALE, curSy + delta);
                else json.visualOffset.scaleZ = Math.max(MIN_SCALE, curSz + delta);
                break;
            }

            case ItemDevKnob.MODE_SOCKET_POS: {
                // No-op if socket at socketIndex does not exist
                if (socketIndex >= json.attachments.size()) break;
                BodypartAttachmentJson at = json.attachments.get(socketIndex);
                if (axis == 0) at.ox += delta;
                else if (axis == 1) at.oy += delta;
                else at.oz += delta;
                break;
            }

            case ItemDevKnob.MODE_SOCKET_ROT: {
                // No-op if socket at socketIndex does not exist
                if (socketIndex >= json.attachments.size()) break;
                BodypartAttachmentJson at = json.attachments.get(socketIndex);
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
