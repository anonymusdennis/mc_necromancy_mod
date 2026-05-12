package com.sirolf2009.necromancy.multipart.editor;



import com.sirolf2009.necromancy.multipart.TransformHierarchy;

import com.sirolf2009.necromancy.multipart.part.AttachmentPoint;

import com.sirolf2009.necromancy.multipart.part.BodyPartNode;

import net.minecraft.resources.ResourceLocation;

import net.minecraft.world.phys.Vec3;



import java.util.ArrayList;

import java.util.List;



/**

 * Stable operations for in-world editors or network handlers—mutate graphs without touching entity classes directly.

 * Heavy batches should wrap callers with {@link TransformHierarchy#beginEditBatch()} (these helpers open batches where

 * they perform multiple hierarchy mutations). Pair sustained surgery edits that bypass topology transactions with

 * {@link com.sirolf2009.necromancy.multipart.broadphase.MultipartBroadphaseEditBatch}.

 */

public final class MultipartEditorHooks {



    private MultipartEditorHooks() {}



    /** Removes child reference from a socket; subtree stays in {@link TransformHierarchy} for loot/export hooks unless caller removes it. */

    public static void clearSocket(TransformHierarchy hierarchy, ResourceLocation parentPartId, ResourceLocation socketId) {

        try (var __ = hierarchy.beginEditBatch()) {

            BodyPartNode parent = hierarchy.get(parentPartId);

            if (parent == null) return;

            List<AttachmentPoint> snapshot = new ArrayList<>(parent.attachmentPointsView());

            parent.clearAttachmentPoints();

            for (AttachmentPoint ap : snapshot) {

                if (ap.socketId().equals(socketId)) {

                    ResourceLocation childId = ap.childPartId();

                    if (childId != null) {

                        BodyPartNode child = hierarchy.get(childId);

                        if (child != null) {

                            child.setAttachedToParent(false);

                        }

                    }

                    parent.addAttachmentPoint(ap.withoutChild());

                } else {

                    parent.addAttachmentPoint(ap);

                }

            }

            hierarchy.markSubtreeDirty(parentPartId);

        }

    }



    /** Register {@code child} and assign it to the named socket on {@code parentPartId}. */

    public static void attachToSocket(TransformHierarchy hierarchy, ResourceLocation parentPartId,

                                      ResourceLocation socketId, BodyPartNode child) {

        try (var __ = hierarchy.beginEditBatch()) {

            hierarchy.registerChild(child, parentPartId);

            BodyPartNode parent = hierarchy.get(parentPartId);

            if (parent == null) return;

            List<AttachmentPoint> snapshot = new ArrayList<>(parent.attachmentPointsView());

            boolean found = false;

            parent.clearAttachmentPoints();

            for (AttachmentPoint ap : snapshot) {

                if (ap.socketId().equals(socketId)) {

                    parent.addAttachmentPoint(ap.withChild(child.id()));

                    found = true;

                } else {

                    parent.addAttachmentPoint(ap);

                }

            }

            if (!found) {

                parent.addAttachmentPoint(AttachmentPoint.simulationYawDegrees(socketId, Vec3.ZERO, 0f, child.id()));

            }

            child.setAttachedToParent(true);

        }

    }



    /** Former multipart root becomes a child under {@code newParentId} / {@code socketId}. */

    public static void reparentFormerRootAsSocketChild(TransformHierarchy hierarchy, ResourceLocation formerRootId,

                                                       ResourceLocation newParentId, ResourceLocation socketId) {

        BodyPartNode node = hierarchy.get(formerRootId);

        if (node == null) return;

        try (var __ = hierarchy.beginEditBatch()) {

            hierarchy.removeRoot(formerRootId);

            attachToSocket(hierarchy, newParentId, socketId, node);

        }

    }

}

