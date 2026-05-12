package com.sirolf2009.necromancy.entity;

import com.sirolf2009.necromancy.api.BodyPartLocation;
import com.sirolf2009.necromancy.api.LocomotionProfile;
import com.sirolf2009.necromancy.api.NecroEntityBase;
import com.sirolf2009.necromancy.api.VoiceProfile;

import java.util.EnumMap;
import java.util.Map;

/**
 * Server-side, value-object snapshot of how a minion is currently assembled
 * and how that assembly drives movement and voice.  Computed by
 * {@link #resolve(EntityMinion)} at most once per tick (the caller is
 * expected to memo).
 *
 * <p>Two layers:
 * <ul>
 *     <li>Structural -- the five {@link NecroEntityBase} adapters per slot,
 *         plus the resolved {@link Mode} (which body part is doing the work
 *         of "the legs").</li>
 *     <li>Active profile -- the {@link LocomotionProfile} the runtime should
 *         actually use right now (already adjusted for arms-as-legs etc.),
 *         and the {@link VoiceProfile} sourced from torso/head.</li>
 * </ul>
 *
 * <p>Resolution rules per the v0.5 plan:
 * <ol>
 *     <li>legs present -> {@link Mode#STANDING}, profile = legs.locomotion()</li>
 *     <li>no legs but torso + at least one arm -> {@link Mode#ARMS_AS_LEGS},
 *         profile = {@code LocomotionProfile.armsAsLegs(armAdapter.locomotion())}</li>
 *     <li>otherwise -> {@link Mode#STATIC} with {@link LocomotionProfile#STATIC}</li>
 * </ol>
 */
public record MinionAssembly(
        NecroEntityBase head,
        NecroEntityBase torso,
        NecroEntityBase armLeft,
        NecroEntityBase armRight,
        NecroEntityBase legs,
        Mode mode,
        LocomotionProfile profile,
        VoiceProfile voice) {

    /** Structural stance of the minion -- which slot(s) are providing locomotion. */
    public enum Mode {
        /** Legs adapter is present; legs.locomotion() drives behaviour. */
        STANDING,
        /** No legs but torso + arm(s); arms walk-cycle as legs. */
        ARMS_AS_LEGS,
        /** Nothing connected can move (head only, or no body parts at all). */
        STATIC
    }

    /** Convenience: an EnumMap view of the five adapters keyed by slot. */
    public EnumMap<BodyPartLocation, NecroEntityBase> adapterMap() {
        EnumMap<BodyPartLocation, NecroEntityBase> map = new EnumMap<>(BodyPartLocation.class);
        if (head     != null) map.put(BodyPartLocation.Head,     head);
        if (torso    != null) map.put(BodyPartLocation.Torso,    torso);
        if (armLeft  != null) map.put(BodyPartLocation.ArmLeft,  armLeft);
        if (armRight != null) map.put(BodyPartLocation.ArmRight, armRight);
        if (legs     != null) map.put(BodyPartLocation.Legs,     legs);
        return map;
    }

    /** {@code true} when a head adapter is present — brain-dead AI policy (plan F04) disables hostile melee/targeting without it. */
    public boolean hasFunctionalBrain() {
        return head != null;
    }

    /** {@code true} when the structural mode allows the minion to walk at all. */
    public boolean canWalk() {
        return mode != Mode.STATIC && profile.type() != LocomotionProfile.LocomotionType.STATIC;
    }

    // ---------------------------------------------------------------- --
    //                              RESOLVER
    // ---------------------------------------------------------------- --

    /** Resolve the live assembly state from the minion's current body-part data. */
    public static MinionAssembly resolve(EntityMinion minion) {
        return fromAdapters(
            minion.getBodyPart(BodyPartLocation.Head),
            minion.getBodyPart(BodyPartLocation.Torso),
            minion.getBodyPart(BodyPartLocation.ArmLeft),
            minion.getBodyPart(BodyPartLocation.ArmRight),
            minion.getBodyPart(BodyPartLocation.Legs));
    }

    /**
     * Build an assembly from raw adapters (e.g. from the altar preview before
     * any minion exists).  Applies the same resolution rules as
     * {@link #resolve(EntityMinion)}.
     */
    public static MinionAssembly fromAdapters(
            NecroEntityBase head, NecroEntityBase torso,
            NecroEntityBase armLeft, NecroEntityBase armRight,
            NecroEntityBase legs) {
        Mode mode;
        LocomotionProfile profile;

        if (legs != null) {
            mode    = Mode.STANDING;
            profile = legs.locomotion();
        } else if (torso != null && (armLeft != null || armRight != null)) {
            mode    = Mode.ARMS_AS_LEGS;
            NecroEntityBase armSrc = armLeft != null ? armLeft : armRight;
            profile = LocomotionProfile.armsAsLegs(armSrc.locomotion());
        } else {
            mode    = Mode.STATIC;
            profile = LocomotionProfile.STATIC;
        }

        // Voice prefers torso, then head, else default zombie.
        VoiceProfile voice;
        if (torso != null)        voice = torso.voice();
        else if (head != null)    voice = head.voice();
        else                       voice = VoiceProfile.ZOMBIE;

        return new MinionAssembly(head, torso, armLeft, armRight, legs, mode, profile, voice);
    }

    /** Empty assembly -- no parts at all. */
    public static MinionAssembly empty() {
        return new MinionAssembly(null, null, null, null, null,
            Mode.STATIC, LocomotionProfile.STATIC, VoiceProfile.ZOMBIE);
    }

    // ---------------------------------------------------------------- --
    //                         CHANGE-DETECTION
    // ---------------------------------------------------------------- --

    /**
     * Cheap "did anything change?" used by {@code EntityMinion} to decide
     * whether to re-resolve and re-attach features.  Compares adapter
     * identity (==), not equality, since adapters are static singletons.
     */
    public boolean structurallySameAs(MinionAssembly other) {
        if (other == null) return false;
        return head     == other.head
            && torso    == other.torso
            && armLeft  == other.armLeft
            && armRight == other.armRight
            && legs     == other.legs;
    }

    /** Bridge for callers that still want the old EnumMap shape. */
    public Map<BodyPartLocation, NecroEntityBase> asMap() { return adapterMap(); }
}
