package com.sirolf2009.necromancy.multipart.part;

import com.sirolf2009.necromancy.multipart.math.PartTransform;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;

import java.util.Objects;

/**
 * Named socket on a parent bodypart. Transform is full TRS relative to the chosen bind space on the parent.
 */
public record AttachmentPoint(ResourceLocation socketId, PartTransform socketTransform, SocketBindSpace bindSpace,
                              ResourceLocation childPartId, int priority) {

    public AttachmentPoint(ResourceLocation socketId, PartTransform socketTransform, SocketBindSpace bindSpace,
                           ResourceLocation childPartId, int priority) {
        this.socketId = Objects.requireNonNull(socketId);
        this.socketTransform = socketTransform.copy();
        this.bindSpace = Objects.requireNonNull(bindSpace);
        this.childPartId = childPartId;
        this.priority = priority;
    }

    public AttachmentPoint(ResourceLocation socketId, PartTransform socketTransform, SocketBindSpace bindSpace,
                           ResourceLocation childPartId) {
        this(socketId, socketTransform, bindSpace, childPartId, 0);
    }

    /** Legacy factory: yaw-only rotation in simulation space. */
    public static AttachmentPoint simulationYawDegrees(ResourceLocation socketId, Vec3 localOffset, float yawDegrees,
                                                       ResourceLocation childPartId) {
        return new AttachmentPoint(socketId, PartTransform.fromTranslationYawDegrees(localOffset, yawDegrees),
            SocketBindSpace.SIMULATION, childPartId, 0);
    }

    public boolean hasChild() {
        return childPartId != null;
    }

    public AttachmentPoint withChild(ResourceLocation newChildId) {
        return new AttachmentPoint(socketId, socketTransform, bindSpace, newChildId, priority);
    }

    public AttachmentPoint withoutChild() {
        return new AttachmentPoint(socketId, socketTransform, bindSpace, null, priority);
    }
}
