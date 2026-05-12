package com.sirolf2009.necromancy.multipart.editor.session;

import com.sirolf2009.necromancy.multipart.TransformHierarchy;

/**
 * Ordered deterministic topology mutation intended for future operation-log / OT workflows — reversible edits replay both ways.
 */
public interface MultipartRecordedTopologyOp {

    /** Monotonic sequence supplied by {@link MultipartTopologyOperationJournal} once appended (implementation-dependent). */
    default long sequenceHint() {
        return -1L;
    }

    /** Applies forward against authoritative hierarchy state (server simulation graph). */
    void applyForward(TransformHierarchy hierarchy);

    /** Undoes {@link #applyForward}; symmetric implementations unlock deterministic rollback beyond coarse snapshots. */
    void applyBackward(TransformHierarchy hierarchy);
}
