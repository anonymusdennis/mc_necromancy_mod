package com.sirolf2009.necromancy.bodypart;

/**
 * Client-held bodypart JSON draft while the dev console screen is open — lets preview/render/dimensions update before Apply syncs the block entity.
 * Cleared when the screen closes. Safe on dedicated server (never written there).
 */
public final class BodypartDevLiveDraft {

    private static final long UNSET = Long.MIN_VALUE;
    /** Returned by {@link #livePreviewMask(long)} when no dev GUI is open for that block. */
    public static final int NO_LIVE_MASK = Integer.MIN_VALUE;

    private static volatile long packedDevPos = UNSET;
    private static volatile String utf8Json = "";
    private static volatile int livePreviewMask = BodypartPreviewMask.DEFAULT_ALL;

    private BodypartDevLiveDraft() {}

    /** {@code packed} is {@link net.minecraft.core.BlockPos#asLong()}. */
    public static void update(long packed, String json, int previewMask) {
        packedDevPos = packed;
        utf8Json = json != null ? json : "";
        livePreviewMask = previewMask;
    }

    public static void clear() {
        packedDevPos = UNSET;
        utf8Json = "";
        livePreviewMask = BodypartPreviewMask.DEFAULT_ALL;
    }

    public static boolean matches(long packedBlockPos) {
        return packedDevPos == packedBlockPos && !utf8Json.isEmpty();
    }

    public static String getUtf8OrEmpty(long packedBlockPos) {
        return packedDevPos == packedBlockPos ? utf8Json : "";
    }

    /**
     * Preview visibility driven by the open editor (immediate feedback).
     * Returns {@link #NO_LIVE_MASK} when this block does not have the dev UI open.
     */
    public static int livePreviewMask(long packedBlockPos) {
        if (packedDevPos != packedBlockPos) return NO_LIVE_MASK;
        return livePreviewMask;
    }
}
