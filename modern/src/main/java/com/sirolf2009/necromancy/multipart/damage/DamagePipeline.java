package com.sirolf2009.necromancy.multipart.damage;

import com.sirolf2009.necromancy.multipart.part.BodyPartNode;

/**
 * Evaluates incoming damage against armor layers, critical zones, and hitbox multipliers before applying to part HP.
 */
public final class DamagePipeline {

    private DamagePipeline() {}

    public static float modifyIncoming(float raw, BodyPartNode part) {
        float v = raw * part.hitbox().damageMultiplier();
        CriticalZone z = part.damageState().criticalZone() != CriticalZone.GENERIC
            ? part.damageState().criticalZone()
            : part.hitbox().criticalZone();
        v *= switch (z) {
            case HEAD -> 1.35f;
            case WEAK_POINT -> 1.5f;
            case ORGAN -> 1.25f;
            default -> 1f;
        };
        for (PartArmorLayer layer : part.damageState().armorLayersView()) {
            v = Math.max(0f, v - layer.flatReduction());
            v *= Math.max(0f, layer.damageMultiplier());
        }
        return v;
    }
}
