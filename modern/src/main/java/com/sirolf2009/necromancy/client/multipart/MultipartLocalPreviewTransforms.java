package com.sirolf2009.necromancy.client.multipart;

import com.sirolf2009.necromancy.multipart.math.PartTransform;
import com.sirolf2009.necromancy.multipart.part.BodyPartNode;

/**
 * Client-only cosmetic previews — writes the render overlay lane without touching simulation locals (no replication).
 */
public final class MultipartLocalPreviewTransforms {

    private MultipartLocalPreviewTransforms() {}

    public static void setRenderPreview(BodyPartNode node, PartTransform previewLocal) {
        node.renderOverlayTransform().set(previewLocal);
    }

    public static void clearRenderPreview(BodyPartNode node) {
        node.renderOverlayTransform().setToIdentity();
    }
}
