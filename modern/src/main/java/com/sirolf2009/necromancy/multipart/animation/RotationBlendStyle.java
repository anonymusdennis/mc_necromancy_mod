package com.sirolf2009.necromancy.multipart.animation;

/**
 * How a layer's rotation delta combines with the accumulated overlay quaternion inside {@link PartTransformLayerBlend}.
 *
 * <p>Translation/scale masking is orthogonal ({@link TransformBlendMask}); styles only affect quaternion composition order
 * for that sorted contributor.
 */
public enum RotationBlendStyle {
    /**
     * {@code qAcc = qAcc * step(w)} — matches legacy procedural stacking (locomotion twist then cosmetic multiply after).
     */
    POST_MULTIPLY,
    /**
     * {@code qAcc = step(w) * qAcc} — additive-style overlay applied before prior quaternion factors (recoil / aim bias
     * that should affect subsequent incremental twists differently).
     */
    PRE_MULTIPLY
}
