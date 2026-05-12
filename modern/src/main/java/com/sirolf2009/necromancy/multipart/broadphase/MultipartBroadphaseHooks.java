package com.sirolf2009.necromancy.multipart.broadphase;

import com.sirolf2009.necromancy.multipart.RootMobEntity;
import net.minecraft.world.level.Level;

/** Opt-in wiring from {@link RootMobEntity#multipartTick()} or custom tick pipelines. */
public final class MultipartBroadphaseHooks {

    private MultipartBroadphaseHooks() {
    }

    /** Structural publish after each multipart entity tick (slot edits land here). */
    public static void afterMultipartTick(RootMobEntity root) {
        if (!root.multipartBroadphaseAutoPublish()) {
            return;
        }
        MultipartBroadphaseRegistry.get(root.asMultipartRoot().level()).updateFromRoot(root);
    }

    /**
     * Explicit frame boundary: freezes broad-phase data for readers (render/debug/network summaries). Prefer invoking
     * once per level tick after entities finish multipart simulation — {@link MultipartBroadphaseTickSubscriber}
     * registers this automatically.
     */
    public static void publishReadSnapshot(Level level) {
        MultipartBroadphaseRegistry.publishReadSnapshot(level);
    }

    /**
     * Opens a deferred spatial aggregation scope (nested-safe). Live {@linkplain MultipartBroadphaseWorld#spatialIndex() spatial inserts}
     * for edited roots are staged until the matching {@link #endDeferredBroadphasePublish(Level)} on the outermost scope,
     * while snapshots merge staged slots so queries stay conservative during editing.
     */
    public static void beginDeferredBroadphasePublish(Level level) {
        MultipartBroadphaseRegistry.get(level).beginDeferredBroadphasePublish();
    }

    /** Ends one deferred scope; flushes staged inserts when the nesting depth reaches zero. */
    public static void endDeferredBroadphasePublish(Level level) {
        MultipartBroadphaseRegistry.get(level).endDeferredBroadphasePublish();
    }
}
