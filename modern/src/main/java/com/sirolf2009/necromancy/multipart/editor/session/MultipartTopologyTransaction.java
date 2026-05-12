package com.sirolf2009.necromancy.multipart.editor.session;

import net.minecraft.server.level.ServerLevel;

/**
 * RAII helper for nested multipart topology transactions — {@linkplain AutoCloseable#close()} rolls back unless
 * {@link #commit()} ran.
 * <p>
 * Opening a transaction begins a deferred multipart broad-phase publish on the server dimension so repeated topology
 * edits coalesce into one spatial flush when the outer transaction commits or rolls back ({@link MultipartServerTopologyEditService}).
 */
public final class MultipartTopologyTransaction implements AutoCloseable {

    private final ServerLevel level;
    private final int entityId;
    private boolean finished;

    MultipartTopologyTransaction(ServerLevel level, int entityId) {
        this.level = level;
        this.entityId = entityId;
    }

    /** Commits the outermost nested depth reached during this session segment (drops rollback snapshot). */
    public void commit() {
        MultipartServerTopologyEditService.commitTransaction(level, entityId);
        finished = true;
    }

    /** Restores the snapshot captured at transaction entry (full unwind for current nesting model). */
    public void rollback() {
        MultipartServerTopologyEditService.rollbackTransaction(level, entityId);
        finished = true;
    }

    @Override
    public void close() {
        if (!finished) {
            rollback();
        }
    }
}
