package com.sirolf2009.necromancy.api;

/**
 * Identifies where on a minion a {@link BodyPart} sits.
 *
 * <p>Direct port of the legacy {@code com.sirolf2009.necroapi.BodyPartLocation}
 * enum; the names are preserved verbatim so existing data files and recipes
 * still work.
 */
public enum BodyPartLocation {
    Head,
    Torso,
    ArmLeft,
    ArmRight,
    Legs;

    /**
     * Returns the canonical "part name" used throughout the original mod
     * (e.g. {@code ArmLeft} and {@code ArmRight} both map to {@code "Arm"}).
     * This is what {@code ItemBodyPart} stored in its damage-meta name list.
     */
    public String partKey() {
        return switch (this) {
            case Head     -> "Head";
            case Torso    -> "Torso";
            case ArmLeft, ArmRight -> "Arm";
            case Legs     -> "Legs";
        };
    }
}
