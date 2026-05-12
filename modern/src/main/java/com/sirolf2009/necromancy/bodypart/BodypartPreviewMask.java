package com.sirolf2009.necromancy.bodypart;

/** Synced preview visibility bits for {@link com.sirolf2009.necromancy.entity.EntityBodypartPreview}. */
public final class BodypartPreviewMask {

    public static final int SOCKET_MARKERS = 2;
    public static final int PIVOT_MARKERS = 4;
    public static final int MESH = 8;
    public static final int COLLISION_OUTLINE = 16;

    /** Default: markers + mesh + collision outline enabled for dev preview. */
    public static final int DEFAULT_ALL =
        SOCKET_MARKERS | PIVOT_MARKERS | MESH | COLLISION_OUTLINE;

    private BodypartPreviewMask() {}
}
