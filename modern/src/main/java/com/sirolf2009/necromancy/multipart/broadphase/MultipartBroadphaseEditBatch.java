package com.sirolf2009.necromancy.multipart.broadphase;

import net.minecraft.world.level.Level;

/**
 * Explicit staging hooks for operation-table / surgery tooling outside multipart topology transactions.
 * Topology edits routed through {@link com.sirolf2009.necromancy.multipart.editor.session.MultipartServerTopologyEditService}
 * already bracket deferred publishes automatically.
 */
public final class MultipartBroadphaseEditBatch {

    private MultipartBroadphaseEditBatch() {
    }

    public static void begin(Level level) {
        MultipartBroadphaseHooks.beginDeferredBroadphasePublish(level);
    }

    public static void end(Level level) {
        MultipartBroadphaseHooks.endDeferredBroadphasePublish(level);
    }
}
