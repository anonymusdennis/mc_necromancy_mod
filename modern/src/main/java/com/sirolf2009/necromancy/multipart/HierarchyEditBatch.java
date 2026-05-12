package com.sirolf2009.necromancy.multipart;

/**
 * Deferred invalidation scope for {@link TransformHierarchy}: topology bumps and transform dirtiness coalesce until
 * {@link #close()} (try-with-resources commits). Structural mutations apply immediately and are not undone if you abort
 * batching early — only revision counters are deferred.
 */
public final class HierarchyEditBatch implements AutoCloseable {

    private final TransformHierarchy owner;
    private boolean closed;

    HierarchyEditBatch(TransformHierarchy owner) {
        this.owner = owner;
    }

    /** Ends this batch level and flushes coalesced invalidations when the nesting depth returns to zero. */
    @Override
    public void close() {
        if (closed) {
            return;
        }
        closed = true;
        owner.leaveEditBatch();
    }
}
