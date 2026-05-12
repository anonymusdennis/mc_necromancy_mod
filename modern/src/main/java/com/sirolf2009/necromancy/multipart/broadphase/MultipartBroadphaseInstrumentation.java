package com.sirolf2009.necromancy.multipart.broadphase;

/**
 * Opt-in profiling hooks for multipart broad-phase overlap and ray segment queries (editor picking, AI traces, debug HUD).
 *
 * <p>Production worlds typically wire {@link com.sirolf2009.necromancy.multipart.telemetry.MultipartTelemetry} through
 * {@link MultipartBroadphaseRegistry}; compose narrow tests with {@link MultipartBroadphaseInstrumentationChain}.
 */
public interface MultipartBroadphaseInstrumentation {

    /** Scope tags are opaque (e.g. {@code "overlap"}, {@code "ray.segment"}); implementations may aggregate or ignore them. */
    default void recordOverlap(String scopeTag, long elapsedNanos, int candidatesEmitted) {
    }

    /** {@code broadphaseNanos} covers spatial/chunk candidate collection; {@code narrowNanos} covers narrow-phase refinement. */
    default void recordRaySegment(String scopeTag, long broadphaseNanos, long narrowNanos, int broadphaseCandidates, int narrowHits) {
    }

    static MultipartBroadphaseInstrumentation noop() {
        return Noop.INSTANCE;
    }

    enum Noop implements MultipartBroadphaseInstrumentation {
        INSTANCE
    }
}
