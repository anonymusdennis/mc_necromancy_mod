package com.sirolf2009.necromancy.bodypart;

import com.sirolf2009.necromancy.Reference;
import com.sirolf2009.necromancy.entity.EntityMinion;
import com.sirolf2009.necromancy.multipart.TransformHierarchy;
import com.sirolf2009.necromancy.multipart.animation.MultipartAnimationFrame;
import com.sirolf2009.necromancy.multipart.animation.RenderLayerPhase;
import com.sirolf2009.necromancy.multipart.animation.WeightedPartTransform;
import com.sirolf2009.necromancy.multipart.math.PartTransform;
import com.sirolf2009.necromancy.multipart.part.BodyPartNode;
import net.minecraft.resources.ResourceLocation;
import org.joml.Quaternionf;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Procedural animation for minion multipart hierarchies.
 *
 * <p>Called each tick from {@link EntityMinion#multipartCollectAnimationLayers} on both sides.
 * Pushes bone-local {@link WeightedPartTransform} entries (phase {@link RenderLayerPhase#PROCEDURAL})
 * into the frame for each bodypart flagged as head / arm / leg / torso.
 *
 * <p>Animation rules per flag:
 * <ul>
 *   <li><b>leg</b> — alternating X-axis rotation driven by {@code walkPos} / {@code walkSpeed}.
 *       Nodes are sorted by their simulation-local X offset to pair left vs right into 180° phase-shifted groups.</li>
 *   <li><b>arm</b> — counter-phase walk swing at ~50 % amplitude; overridden toward a forward-strike angle
 *       when {@code attackAnim > 0}.</li>
 *   <li><b>torso</b> — gentle Y-axis sway plus slight forward pitch proportional to walk speed.</li>
 *   <li><b>head</b> — head yaw (around Y) and pitch (around X) relative to body orientation.</li>
 *   <li><b>special</b> — no default animation; reserved for {@code PartFeature} overrides.</li>
 * </ul>
 */
public final class MinionBodypartAnimator {

    private static final ResourceLocation CONTRIBUTOR = Reference.rl("minion_bodypart_anim");

    /** Maximum leg swing in radians at full walk speed. */
    private static final float LEG_AMPLITUDE   = 0.8f;
    /** Arm swing amplitude (counter-phase, ~50 % of legs). */
    private static final float ARM_AMPLITUDE   = 0.4f;
    /** Torso Y-sway amplitude at full walk speed. */
    private static final float TORSO_SWAY_AMP  = 0.08f;
    /** Torso forward-pitch at full walk speed. */
    private static final float TORSO_PITCH_AMP = 0.05f;

    private MinionBodypartAnimator() {}

    /**
     * Collect animation layers for this tick and add them to {@code frame}.
     *
     * @param entity     the minion being animated
     * @param frame      animation frame to push layers into
     * @param walkPos    {@code entity.walkAnimation.position(partialTick)}
     * @param walkSpeed  {@code entity.walkAnimation.speed(partialTick)}, unclamped
     * @param headYaw    head yaw relative to body yaw (degrees)
     * @param headPitch  entity xRot (degrees, positive = looking down)
     * @param attackAnim {@code entity.getAttackAnim(partialTick)}, range [0, 1]
     */
    public static void collect(EntityMinion entity,
                                MultipartAnimationFrame frame,
                                float walkPos,
                                float walkSpeed,
                                float headYaw,
                                float headPitch,
                                float attackAnim) {
        TransformHierarchy hierarchy = entity.multipartHierarchy();
        if (hierarchy.nodes().isEmpty()) return;

        // Collect leg nodes and sort by simulation-local X for alternating phase assignment.
        List<BodyPartNode> legNodes = new ArrayList<>();
        for (BodyPartNode node : hierarchy.nodes()) {
            BodypartDefinition def = BodyPartConfigManager.INSTANCE.get(node.id()).orElse(null);
            if (def == null || def.flags() == null || !def.flags().leg) continue;
            legNodes.add(node);
        }
        legNodes.sort(Comparator.comparingDouble(n -> n.simulationLocalTransform().translation().x));

        // Even indices (left-side / negative X) get phase 0; odd indices get phase π.
        Map<ResourceLocation, Float> legPhases = new HashMap<>();
        for (int i = 0; i < legNodes.size(); i++) {
            legPhases.put(legNodes.get(i).id(), (i % 2 == 0) ? 0f : (float) Math.PI);
        }

        float normalizedSpeed = Math.min(1f, Math.abs(walkSpeed) * 8f);

        for (BodyPartNode node : hierarchy.nodes()) {
            BodypartDefinition def = BodyPartConfigManager.INSTANCE.get(node.id()).orElse(null);
            if (def == null || def.flags() == null) continue;
            BodypartFlagsJson flags = def.flags();

            PartTransform delta = PartTransform.identity();
            boolean hasContrib = false;

            if (flags.leg) {
                float phase = legPhases.getOrDefault(node.id(), 0f);
                float angle = (float) Math.sin(walkPos + phase) * LEG_AMPLITUDE * normalizedSpeed;
                delta.setRotation(new Quaternionf().rotateX(angle));
                hasContrib = true;

            } else if (flags.arm) {
                // Swing counter-phase to legs.
                float baseAngle = (float) Math.sin(-walkPos + Math.PI) * ARM_AMPLITUDE * normalizedSpeed;
                if (attackAnim > 0f) {
                    // Lerp toward a forward-reach strike angle based on attack progress.
                    float strikeAngle = -2f + 1.5f * attackPulse(attackAnim);
                    baseAngle = baseAngle + (strikeAngle - baseAngle) * attackAnim;
                }
                delta.setRotation(new Quaternionf().rotateX(baseAngle));
                hasContrib = true;

            } else if (flags.torso) {
                float sway     = (float) Math.sin(walkPos * 2.0) * TORSO_SWAY_AMP * normalizedSpeed;
                float pitchFwd = normalizedSpeed * TORSO_PITCH_AMP;
                delta.setRotation(new Quaternionf().rotateY(sway).rotateX(pitchFwd));
                hasContrib = true;

            } else if (flags.head) {
                float yawR   = (float) Math.toRadians(headYaw);
                float pitchR = (float) Math.toRadians(headPitch);
                delta.setRotation(new Quaternionf().rotateY(yawR).rotateX(pitchR));
                hasContrib = true;
            }
            // flags.special: no default animation — reserved for PartFeature overrides.

            if (hasContrib) {
                frame.addLayer(node.id(),
                    new WeightedPartTransform(CONTRIBUTOR, RenderLayerPhase.PROCEDURAL, 0, 1f, delta));
            }
        }
    }

    /** Triangle-wave pulse for attack animation (mirrors legacy {@code MinionAssembler.pulse}). */
    private static float attackPulse(float a) {
        float range  = 10f;
        float t      = a * range;
        return (Math.abs(t % range - range * 0.5f) - range * 0.25f) / (range * 0.25f);
    }
}
