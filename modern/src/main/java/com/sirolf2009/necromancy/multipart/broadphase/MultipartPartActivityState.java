package com.sirolf2009.necromancy.multipart.broadphase;

/**
 * Hierarchical broad-phase activity — drives fine spatial-hash contributions while union hulls stay conservative.
 *
 * <p><strong>Fine spatial slots:</strong> {@link #ACTIVE} / {@link #IDLE} emit per-part slots when collision flags allow.
 * {@link #SLEEPING} omits an entire subtree from fine indexing until motion wakes it. {@link #STATIC} skips this bone's
 * slot but still walks descendants (welded inner assemblies with animated children).
 */
public enum MultipartPartActivityState {

    /** Fully awake — immediate reaction to motion thresholds. */
    ACTIVE,

    /** Stationary within thresholds — still indexed for narrow interactions / proximity wakes. */
    IDLE,

    /** Subtree omitted from fine indexing — collapses descendant slots until motion or ancestor wake. */
    SLEEPING,

    /** Weld-style bone — no dedicated slot; descendants may still emit slots. Ignores automatic idle timers. */
    STATIC;

    /** Per-part entries in {@link com.sirolf2009.necromancy.multipart.TransformHierarchy#collectBroadphaseSlots}. */
    public boolean contributesFineBroadphaseSlots() {
        return this == ACTIVE || this == IDLE;
    }

    /** Skip recursive fine indexing under this bone (union / dormant hull still conservative elsewhere). */
    public boolean collapsesSubtreeFineIndexing() {
        return this == SLEEPING;
    }

    /** Bones governed by quiet counters / motion thresholds (STATIC is editor-/rig-driven). */
    public boolean participatesInAutomaticTransitions() {
        return this != STATIC;
    }
}
