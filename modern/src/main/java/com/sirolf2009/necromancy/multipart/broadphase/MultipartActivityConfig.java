package com.sirolf2009.necromancy.multipart.broadphase;

/**
 * Tunables for temporal coherence — squared metrics avoid sqrt per bone per tick.
 *
 * @param translationEpsilonSq Min squared delta (world metres) of simulation pose translation to count as motion.
 * @param orientationOrthoEpsilonSq Min squared orthogonality gap ({@code 1 - dot(q0,q1)^2}) for rotation-only motion.
 * @param ticksQuietBeforeIdle Consecutive quiet ticks while ACTIVE before dropping to {@link MultipartPartActivityState#IDLE}.
 * @param ticksQuietBeforeSleep Consecutive quiet ticks while IDLE before dropping to {@link MultipartPartActivityState#SLEEPING}.
 */
public record MultipartActivityConfig(
    double translationEpsilonSq,
    float orientationOrthoEpsilonSq,
    int ticksQuietBeforeIdle,
    int ticksQuietBeforeSleep
) {
    public static final MultipartActivityConfig DEFAULT = new MultipartActivityConfig(
        2.5e-5,
        2.5e-6f,
        40,
        120);

    public MultipartActivityConfig {
        if (ticksQuietBeforeIdle < 1 || ticksQuietBeforeSleep < 1) {
            throw new IllegalArgumentException("quiet thresholds must be positive");
        }
    }
}
