package com.sirolf2009.necromancy.multipart.broadphase;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Thread-safe cumulative counters for multipart broad-phase instrumentation (narrow tests, paired hooks).
 *
 * <p>For full-runtime profiling use {@link com.sirolf2009.necromancy.multipart.telemetry.MultipartTelemetry} or compose via
 * {@link MultipartBroadphaseInstrumentationChain}.
 */
public final class MultipartBroadphaseInstrumentationCounters implements MultipartBroadphaseInstrumentation {

    private final AtomicLong overlapQueries = new AtomicLong();
    private final AtomicLong overlapCandidates = new AtomicLong();
    private final AtomicLong overlapNanos = new AtomicLong();

    private final AtomicLong rayQueries = new AtomicLong();
    private final AtomicLong rayBroadCandidates = new AtomicLong();
    private final AtomicLong rayNarrowHits = new AtomicLong();
    private final AtomicLong rayBroadNanos = new AtomicLong();
    private final AtomicLong rayNarrowNanos = new AtomicLong();

    @Override
    public void recordOverlap(String scopeTag, long elapsedNanos, int candidatesEmitted) {
        overlapQueries.incrementAndGet();
        overlapCandidates.addAndGet(Math.max(0, candidatesEmitted));
        overlapNanos.addAndGet(Math.max(0L, elapsedNanos));
    }

    @Override
    public void recordRaySegment(String scopeTag, long broadphaseNanos, long narrowNanos, int broadphaseCandidates, int narrowHits) {
        rayQueries.incrementAndGet();
        rayBroadCandidates.addAndGet(Math.max(0, broadphaseCandidates));
        rayNarrowHits.addAndGet(Math.max(0, narrowHits));
        rayBroadNanos.addAndGet(Math.max(0L, broadphaseNanos));
        rayNarrowNanos.addAndGet(Math.max(0L, narrowNanos));
    }

    public long overlapQueries() {
        return overlapQueries.get();
    }

    public long overlapCandidates() {
        return overlapCandidates.get();
    }

    public long overlapNanos() {
        return overlapNanos.get();
    }

    public long rayQueries() {
        return rayQueries.get();
    }

    public long rayBroadCandidates() {
        return rayBroadCandidates.get();
    }

    public long rayNarrowHits() {
        return rayNarrowHits.get();
    }

    public long rayBroadNanos() {
        return rayBroadNanos.get();
    }

    public long rayNarrowNanos() {
        return rayNarrowNanos.get();
    }

    public void reset() {
        overlapQueries.set(0);
        overlapCandidates.set(0);
        overlapNanos.set(0);
        rayQueries.set(0);
        rayBroadCandidates.set(0);
        rayNarrowHits.set(0);
        rayBroadNanos.set(0);
        rayNarrowNanos.set(0);
    }
}
