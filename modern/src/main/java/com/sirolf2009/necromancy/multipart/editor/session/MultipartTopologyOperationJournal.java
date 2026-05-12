package com.sirolf2009.necromancy.multipart.editor.session;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Append-only journal staged during transactional edits — placeholder spine for collaborative / OT pipelines.
 */
public final class MultipartTopologyOperationJournal {

    private final List<MultipartRecordedTopologyOp> ops = new CopyOnWriteArrayList<>();
    private long nextSeq;

    public void append(MultipartRecordedTopologyOp op) {
        nextSeq++;
        ops.add(op);
    }

    public List<MultipartRecordedTopologyOp> viewOps() {
        return List.copyOf(ops);
    }

    public long nextSequenceExclusive() {
        return nextSeq;
    }

    public void clear() {
        ops.clear();
        nextSeq = 0;
    }
}
