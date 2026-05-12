package com.sirolf2009.necromancy.multipart.damage;

/**
 * Flat armor layer applied after critical modifiers (ordered evaluation lives in {@link DamagePipeline}).
 *
 * @param flatReduction subtractive before multiplier
 * @param damageMultiplier multiplicative factor
 */
public record PartArmorLayer(float flatReduction, float damageMultiplier, String layerId) {

    public static PartArmorLayer multiplierOnly(float mult, String layerId) {
        return new PartArmorLayer(0f, mult, layerId);
    }
}
