package com.sirolf2009.necromancy.multipart.broadphase;

import com.sirolf2009.necromancy.multipart.telemetry.MultipartTelemetry;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Optional;
import java.util.WeakHashMap;

/**
 * Weak per-level worlds — multipart entities hold strong refs to their {@link Level}.
 * <p>
 * Query-facing APIs should prefer {@link #readSnapshotIfPresent(Level)} / {@link MultipartWorldReadFrame} after
 * {@link #publishReadSnapshot(Level)} runs for the tick (see {@link MultipartBroadphaseTickSubscriber}).
 * Per-level {@link MultipartBroadphaseWorld#configureQueryAcceleration(MultipartBroadphaseQueryStrategy, MultipartBroadphaseInstrumentation)}
 * swaps {@link MultipartBroadphaseQueryStrategy} implementations without migrating spatial tiers.
 */
public final class MultipartBroadphaseRegistry {

    private static final Map<Level, MultipartBroadphaseWorld> WORLDS = new WeakHashMap<>();
    private static final double DEFAULT_CELL_SIZE = 4.0;

    private MultipartBroadphaseRegistry() {
    }

    public static MultipartBroadphaseWorld get(Level level) {
        return WORLDS.computeIfAbsent(level, MultipartBroadphaseRegistry::createDefault);
    }

    /** Does not create an empty world entry (unlike {@link #get(Level)}). */
    public static @Nullable MultipartBroadphaseWorld getIfPresent(Level level) {
        return WORLDS.get(level);
    }

    /**
     * Publishes an immutable broad-phase snapshot cheaply when nothing changed since the last publish.
     * Intended call site: end-of-level-tick boundary ({@link MultipartBroadphaseTickSubscriber}).
     */
    public static void publishReadSnapshot(Level level) {
        MultipartBroadphaseWorld w = WORLDS.get(level);
        if (w != null) {
            MultipartTelemetry.recordBroadphaseTickPublishInvocation(level);
            w.publishQuerySnapshot();
        }
    }

    /** Cached publish only — skips rebuilding while the simulation thread has not marked the spatial structure dirty. */
    public static Optional<MultipartBroadphaseSnapshot> peekPublishedBroadphaseSnapshot(Level level) {
        MultipartBroadphaseWorld w = getIfPresent(level);
        return w == null ? Optional.empty() : w.peekPublishedSnapshot();
    }

    /**
     * Snapshot suitable for readers off the tick thread; rebuilds when multipart edits landed since the last publish.
     */
    public static Optional<MultipartBroadphaseSnapshot> readSnapshotIfPresent(Level level) {
        MultipartBroadphaseWorld w = getIfPresent(level);
        return w == null ? Optional.empty() : Optional.of(w.readSnapshot());
    }

    private static MultipartBroadphaseWorld createDefault(Level level) {
        return new MultipartBroadphaseWorld(level, DEFAULT_CELL_SIZE,
            MultipartBroadphaseSpatialPolicy.DEFAULT,
            new MultipartInternalBroadphase.LinearScan(),
            MultipartTelemetry.broadphaseHooksOrNoop());
    }
}
