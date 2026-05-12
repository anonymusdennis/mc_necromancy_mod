package com.sirolf2009.necromancy.multipart.editor.session;

import org.jetbrains.annotations.Nullable;

/**
 * Result surface for authoritative multipart edits — callers gate UX messaging via {@link #reasonCode()}.
 */
public record MultipartTopologyEditResult(boolean ok, @Nullable String reasonCode) {

    public static final String RC_NOT_MULTIPART = "not_multipart";
    public static final String RC_WRONG_DIMENSION = "wrong_dimension";
    public static final String RC_LOCK_HELD_BY_OTHER = "lock_held_by_other";
    public static final String RC_LOCK_REQUIRED = "lock_required";
    public static final String RC_WRONG_PLAYER = "wrong_player";

    public static MultipartTopologyEditResult success() {
        return new MultipartTopologyEditResult(true, null);
    }

    public static MultipartTopologyEditResult deny(String reason) {
        return new MultipartTopologyEditResult(false, reason);
    }

    public boolean deniedByLockHeldElsewhere() {
        return RC_LOCK_HELD_BY_OTHER.equals(reasonCode());
    }
}