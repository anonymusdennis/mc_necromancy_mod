package com.sirolf2009.necromancy.multipart.broadphase;

/**
 * Fan-out broad-phase instrumentation for composing narrow counters with global telemetry.
 */
public record MultipartBroadphaseInstrumentationChain(MultipartBroadphaseInstrumentation first,
                                                      MultipartBroadphaseInstrumentation second)
    implements MultipartBroadphaseInstrumentation {

    public static MultipartBroadphaseInstrumentation of(MultipartBroadphaseInstrumentation a,
                                                        MultipartBroadphaseInstrumentation b) {
        if (a == MultipartBroadphaseInstrumentation.noop()) {
            return b;
        }
        if (b == MultipartBroadphaseInstrumentation.noop()) {
            return a;
        }
        return new MultipartBroadphaseInstrumentationChain(a, b);
    }

    @Override
    public void recordOverlap(String scopeTag, long elapsedNanos, int candidatesEmitted) {
        first.recordOverlap(scopeTag, elapsedNanos, candidatesEmitted);
        second.recordOverlap(scopeTag, elapsedNanos, candidatesEmitted);
    }

    @Override
    public void recordRaySegment(String scopeTag, long broadphaseNanos, long narrowNanos,
                                 int broadphaseCandidates, int narrowHits) {
        first.recordRaySegment(scopeTag, broadphaseNanos, narrowNanos, broadphaseCandidates, narrowHits);
        second.recordRaySegment(scopeTag, broadphaseNanos, narrowNanos, broadphaseCandidates, narrowHits);
    }
}
