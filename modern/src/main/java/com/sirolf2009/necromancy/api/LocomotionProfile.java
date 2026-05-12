package com.sirolf2009.necromancy.api;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;

/**
 * Declarative description of how a body-part adapter wants its minion to
 * move.  Returned by {@link NecroEntityBase#locomotion()} and consumed by
 * the runtime resolver in {@code com.sirolf2009.necromancy.entity.MinionAssembly}.
 *
 * <p>Conceptually two layers:
 * <ul>
 *     <li>{@link LocomotionType} -- what kind of movement this adapter
 *         contributes when used as the LEGS slot of a minion.  WALK is the
 *         common case (zombie-style ground walking); HOP is rabbit-style
 *         periodic jumps; SWIM stays put on land and meanders in water like
 *         a vanilla squid; STATIC cannot move at all (turret).</li>
 *     <li>Numerical knobs -- {@link #speedMultiplier}, {@link #hopIntervalTicks},
 *         step sound metadata, etc.  These let two WALK adapters feel
 *         different (e.g. a creeper trots faster than a slow cow).</li>
 * </ul>
 *
 * <p>The {@link #liftPixels} field is filled in by the runtime resolver when
 * an arms-as-legs fallback kicks in (a minion that has arms but no legs walks
 * on its hands and is lifted by ~10 px so the arm tips touch the ground).
 * Adapters do not normally set it themselves.
 */
public record LocomotionProfile(
        LocomotionType type,
        float          speedMultiplier,
        SoundEvent     stepSound,
        float          stepVolume,
        float          stepPitch,
        int            hopIntervalTicks,
        float          liftPixels) {

    /** What kind of movement an adapter declares for the LEGS slot. */
    public enum LocomotionType {
        /** Standard ground walk, animated by the legs adapter. */
        WALK,
        /** Periodic jumps; idle between hops. */
        HOP,
        /** Stays put on land, meanders in water. */
        SWIM,
        /** Cannot move under its own power. */
        STATIC
    }

    // ---- standard factory profiles ------------------------------------ --

    /** Default zombie-style walker -- 1x speed, generic stone step. */
    public static final LocomotionProfile WALK_DEFAULT = new LocomotionProfile(
        LocomotionType.WALK, 1.0F,
        SoundEvents.ZOMBIE_STEP, 0.15F, 1.0F,
        0, 0F);

    /** Adapter cannot walk at all (good for turret bodies). */
    public static final LocomotionProfile STATIC = new LocomotionProfile(
        LocomotionType.STATIC, 0F, null, 0F, 1F, 0, 0F);

    /** Vanilla squid: stuck on land, swims in water. */
    public static final LocomotionProfile SWIM_DEFAULT = new LocomotionProfile(
        LocomotionType.SWIM, 0.6F,
        null, 0F, 1F, 0, 0F);

    /**
     * Helper for rabbit / slime style adapters.
     *
     * @param speedMul     speed scaling (1.0 = baseline)
     * @param intervalTicks hop period in ticks (20 ticks = 1 second)
     * @param stepSound    sound to play when landing
     */
    public static LocomotionProfile hop(float speedMul, int intervalTicks, SoundEvent stepSound) {
        return new LocomotionProfile(
            LocomotionType.HOP, speedMul,
            stepSound, 0.15F, 1.0F,
            intervalTicks, 0F);
    }

    /** Helper for tuned WALK profiles. */
    public static LocomotionProfile walk(float speedMul, SoundEvent stepSound) {
        return new LocomotionProfile(
            LocomotionType.WALK, speedMul,
            stepSound, 0.15F, 1.0F,
            0, 0F);
    }

    /** Helper for tuned WALK profiles with full sound parameters. */
    public static LocomotionProfile walk(float speedMul, SoundEvent stepSound, float volume, float pitch) {
        return new LocomotionProfile(
            LocomotionType.WALK, speedMul,
            stepSound, volume, pitch,
            0, 0F);
    }

    /**
     * Build the effective profile when an adapter is being used as a fallback
     * "arms-as-legs" walker (i.e. the minion has no legs, but does have a
     * torso and at least one arm).
     *
     * <p>The contract per design:
     * <ul>
     *     <li>type collapses to WALK regardless of source type</li>
     *     <li>speed is halved (slow shuffle on hands)</li>
     *     <li>liftPixels = 10 (lifts the model so the arm-tips touch ground)</li>
     * </ul>
     */
    public static LocomotionProfile armsAsLegs(LocomotionProfile source) {
        return new LocomotionProfile(
            LocomotionType.WALK,
            source.speedMultiplier * 0.5F,
            source.stepSound,
            source.stepVolume,
            source.stepPitch,
            0,
            10F);
    }
}
