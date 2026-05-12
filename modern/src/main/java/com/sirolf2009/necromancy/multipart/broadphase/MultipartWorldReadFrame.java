package com.sirolf2009.necromancy.multipart.broadphase;

import net.minecraft.world.level.Level;

import java.util.Optional;

/**
 * Lightweight immutable pairing of {@linkplain MultipartBroadphaseSnapshot#sequence() snapshot generation} with the
 * broad-phase payload itself. Render/async tooling can retain this record across threads while simulation advances.
 */
public record MultipartWorldReadFrame(long sequence, MultipartBroadphaseSnapshot broadphase) {

    /** Forces a publish when the spatial structure changed since the last boundary (may allocate). */
    public static Optional<MultipartWorldReadFrame> captureBroadphase(Level level) {
        return MultipartBroadphaseRegistry.readSnapshotIfPresent(level).map(s -> new MultipartWorldReadFrame(s.sequence(), s));
    }

    /** Uses the latest publish only; empty when no world exists or nothing published yet. */
    public static Optional<MultipartWorldReadFrame> peekBroadphase(Level level) {
        return MultipartBroadphaseRegistry.peekPublishedBroadphaseSnapshot(level)
            .map(s -> new MultipartWorldReadFrame(s.sequence(), s));
    }
}
