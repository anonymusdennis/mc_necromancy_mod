package com.sirolf2009.necromancy.multipart.animation;

/**
 * Ordered evaluation phases for render-only bone overlays ({@linkplain com.sirolf2009.necromancy.multipart.TransformHierarchy}
 * applies layers after authoritative simulation poses). Earlier phases feed later ones; {@link #IK_OVERRIDE} and
 * {@link #TEMPORARY_EFFECT} are positioned for full-body IK solves and hit-flinch / VFX offsets respectively.
 */
public enum RenderLayerPhase {
    /** Weighted procedural offsets (locomotion noise, breathing, look-at prep). */
    PROCEDURAL,
    /** Additive animation clips / cosmetic rigs stacked before IK. */
    ADDITIVE_OVERLAY,
    /** Aim toward IK targets (hands/feet); blends toward absolute local-space solutions. */
    IK_OVERRIDE,
    /** Hit reactions, editor preview flashes — applied last before per-node editor overlay merge. */
    TEMPORARY_EFFECT
}
