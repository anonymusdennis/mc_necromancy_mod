package com.sirolf2009.necromancy.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.sirolf2009.necromancy.api.BodyPartLocation;
import com.sirolf2009.necromancy.api.NecroEntityBase;
import com.sirolf2009.necromancy.bodypart.BodyPartItemIds;
import com.sirolf2009.necromancy.entity.EntityMinion;
import com.sirolf2009.necromancy.entity.MinionAssembly;
import com.sirolf2009.necromancy.multipart.TransformHierarchy;
import com.sirolf2009.necromancy.multipart.math.PartTransform;
import com.sirolf2009.necromancy.multipart.part.BodyPartNode;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;

/**
 * Hierarchy-driven renderer for live {@link EntityMinion} entities.
 *
 * <p>For each occupied altar slot, this renderer:
 * <ol>
 *   <li>Applies the standard outer transform (lift 1.5 blocks, flip Y/X, rotate by body yaw).</li>
 *   <li>Translates within model-local space to the part's slot position (derived from the same
 *       {@code slotOffset} table used by {@link com.sirolf2009.necromancy.bodypart.MinionSkeletonBinder}).</li>
 *   <li>Retrieves the node's composed render overlay from the {@link TransformHierarchy} — this contains
 *       the procedural animation layers pushed by
 *       {@link com.sirolf2009.necromancy.bodypart.MinionBodypartAnimator}.</li>
 *   <li>Delegates to {@link MinionAssembler#renderSinglePartWithHierarchyOverlay} which draws the part
 *       in rest pose with the animation quaternion applied to the model root.</li>
 * </ol>
 *
 * <p>Falls back silently to a no-op if a slot has no corresponding hierarchy node (part not in config).
 *
 * <p><b>Coordinate math note:</b> After the outer transform the pose stack is in "model local" space.
 * A world-relative slot offset {@code (dx, dy, dz)} maps to model-local {@code (tx, ty, tz)} via:
 * <pre>
 *   tx = dx·cos(yaw) + dz·sin(yaw)
 *   ty = 1.5 − dy
 *   tz = dx·sin(yaw) − dz·cos(yaw)
 * </pre>
 * This is derived from the inverse of the outer transform
 * {@code T(0,1.5,0) · S(−1,−1,1) · R(180−yaw)} evaluated at each slot offset.
 */
public final class MinionHierarchyRenderer {

    private MinionHierarchyRenderer() {}

    /**
     * Render all occupied body-part slots using the hierarchy's current render world poses.
     *
     * <p>The pose stack must be at the entity-relative origin (entity foot = current origin)
     * when this method is called — exactly as in {@link RenderMinion#render}.
     *
     * @param minion       the entity being rendered
     * @param hierarchy    the entity's transform hierarchy (must not be empty)
     * @param assembly     current assembly snapshot (provides per-slot adapters)
     * @param bodyYaw      interpolated body yaw in degrees (Mth.lerp of yBodyRotO / yBodyRot)
     * @param partialTicks partial tick for interpolation (unused for pose; reserved)
     * @param pose         pose stack at entity-relative origin
     * @param buf          buffer source
     * @param light        packed light value
     */
    public static void render(EntityMinion minion,
                               TransformHierarchy hierarchy,
                               MinionAssembly assembly,
                               float bodyYaw,
                               float partialTicks,
                               PoseStack pose,
                               MultiBufferSource buf,
                               int light) {
        float yawRad = (float) Math.toRadians(bodyYaw);
        float cosY   = Mth.cos(yawRad);
        float sinY   = Mth.sin(yawRad);

        // Apply the shared outer transform once; each slot's inner push/pop restores this state.
        pose.pushPose();
        pose.translate(0f, 1.5f, 0f);
        pose.scale(-1f, -1f, 1f);
        pose.mulPose(Axis.YP.rotationDegrees(180f - bodyYaw));

        for (BodyPartLocation loc : BodyPartLocation.values()) {
            NecroEntityBase adapter = adapterFor(assembly, loc);
            if (adapter == null) continue;

            ResourceLocation partId = BodyPartItemIds.inferredPartId(adapter.mobName, loc);
            BodyPartNode node = hierarchy.get(partId);

            // Animation overlay — identity if no node registered.
            Quaternionf animQuat = new Quaternionf(); // identity
            if (node != null) {
                PartTransform overlay = new PartTransform();
                node.composedRenderOverlayInto(overlay);
                overlay.rotationInto(animQuat);
            }

            Vec3 slotOff = slotOffset(loc);

            // Translate to model-local slot position (see class javadoc for formula).
            float tx = (float) (slotOff.x * cosY + slotOff.z * sinY);
            float ty = (float) (1.5 - slotOff.y);
            float tz = (float) (slotOff.x * sinY - slotOff.z * cosY);

            pose.pushPose();
            pose.translate(tx, ty, tz);

            MinionAssembler.renderSinglePartWithHierarchyOverlay(
                minion, adapter, loc, animQuat, pose, buf, light);

            pose.popPose();
        }

        pose.popPose();
    }

    // -------------------------------------------------------------------------
    // Internal helpers
    // -------------------------------------------------------------------------

    /**
     * Slot offsets in entity-relative world space — mirrors
     * {@link com.sirolf2009.necromancy.bodypart.MinionSkeletonBinder} and
     * {@link com.sirolf2009.necromancy.bodypart.MinionCompositeCollision}.
     */
    private static Vec3 slotOffset(BodyPartLocation loc) {
        return switch (loc) {
            case Head     -> new Vec3(  0.00, 1.45, 0);
            case Torso    -> new Vec3(  0.00, 0.92, 0);
            case Legs     -> new Vec3(  0.00, 0.42, 0);
            case ArmLeft  -> new Vec3(-0.32,  1.05, 0);
            case ArmRight -> new Vec3( 0.32,  1.05, 0);
        };
    }

    private static NecroEntityBase adapterFor(MinionAssembly assembly, BodyPartLocation loc) {
        return switch (loc) {
            case Head     -> assembly.head();
            case Torso    -> assembly.torso();
            case Legs     -> assembly.legs();
            case ArmLeft  -> assembly.armLeft();
            case ArmRight -> assembly.armRight();
        };
    }
}
