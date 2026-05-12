package com.sirolf2009.necromancy.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.sirolf2009.necromancy.api.BodyPart;
import com.sirolf2009.necromancy.api.BodyPartLocation;
import com.sirolf2009.necromancy.api.ISaddleAble;
import com.sirolf2009.necromancy.api.NecroEntityBase;
import com.sirolf2009.necromancy.client.model.MinionPartCache;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;

import java.util.EnumMap;

/**
 * Stateless helper that draws an "assembled minion" -- a body composed of body
 * parts from up to five different {@link NecroEntityBase} adapters.
 *
 * <p>Rendering order, anchor offsets and per-adapter animation hooks all
 * follow the legacy {@code ModelMinion#render} flow exactly.  This class is
 * shared between:
 * <ul>
 *     <li>{@link RenderMinion}, which renders a live {@code EntityMinion}, and</li>
 *     <li>{@code AltarBlockEntityRenderer}, which previews the result of the
 *         items currently inserted into a {@code BlockAltar}.</li>
 * </ul>
 */
public final class MinionAssembler {

    private MinionAssembler() {}

    /**
     * Render the five body parts as a single assembled minion at the current
     * pose-stack position.  The caller is responsible for the outer
     * {@code pushPose()} + global transform (translation, body-yaw rotation,
     * scale).
     *
     * @param minion         a {@link LivingEntity} for adapter hooks that need
     *                       state (sneaking, saddled, ...); may be {@code null}
     *                       for the altar preview
     * @param adapters       map of body location -> adapter; missing entries
     *                       are skipped
     * @param saddled        whether to overlay the saddle texture on the torso
     * @param attackAnim     0..1 attack-swing progress, 0 = idle
     * @param walk           walk-cycle phase (radians)
     * @param speed          walk speed (0 = standing, 1 = full)
     * @param age            entity ageInTicks + partialTick
     * @param headYaw        head yaw (degrees) relative to body
     * @param headPitch      head pitch (degrees)
     */
    public static void renderAssembled(LivingEntity minion,
                                       EnumMap<BodyPartLocation, NecroEntityBase> adapters,
                                       boolean saddled,
                                       float attackAnim,
                                       float walk, float speed, float age,
                                       float headYaw, float headPitch,
                                       PoseStack pose, MultiBufferSource buf, int light) {

        NecroEntityBase legsAdapter  = adapters.get(BodyPartLocation.Legs);
        NecroEntityBase torsoAdapter = adapters.get(BodyPartLocation.Torso);
        NecroEntityBase armLAdapter  = adapters.get(BodyPartLocation.ArmLeft);
        NecroEntityBase armRAdapter  = adapters.get(BodyPartLocation.ArmRight);
        NecroEntityBase headAdapter  = adapters.get(BodyPartLocation.Head);

        // -- LEGS --
        renderGroup(minion, legsAdapter, BodyPartLocation.Legs, null, attackAnim,
                    walk, speed, age, headYaw, headPitch, false, pose, buf, light);

        // Move the anchor up to where the torso attaches to the legs.  Per the
        // legacy code this offset comes from {@code legs[0].torsoPos}.
        BodyPart legAnchor = firstPart(legsAdapter == null ? null : legsAdapter.legs);
        if (legAnchor != null && legAnchor.torsoPos != null) {
            pose.translate(legAnchor.torsoPos[0] / 16F,
                           legAnchor.torsoPos[1] / 16F,
                           legAnchor.torsoPos[2] / 16F);
        }

        // -- TORSO --
        renderGroup(minion, torsoAdapter, BodyPartLocation.Torso, null, attackAnim,
                    walk, speed, age, headYaw, headPitch, saddled, pose, buf, light);

        BodyPart torsoAnchor = firstPart(torsoAdapter == null ? null : torsoAdapter.torso);

        // -- ARM LEFT / RIGHT / HEAD all anchored to the torso --
        renderGroup(minion, armLAdapter, BodyPartLocation.ArmLeft,
                    torsoAnchor == null ? null : torsoAnchor.armLeftPos,
                    attackAnim, walk, speed, age, headYaw, headPitch, false,
                    pose, buf, light);

        renderGroup(minion, armRAdapter, BodyPartLocation.ArmRight,
                    torsoAnchor == null ? null : torsoAnchor.armRightPos,
                    attackAnim, walk, speed, age, headYaw, headPitch, false,
                    pose, buf, light);

        renderGroup(minion, headAdapter, BodyPartLocation.Head,
                    torsoAnchor == null ? null : torsoAnchor.headPos,
                    0F, walk, speed, age, headYaw, headPitch, false,
                    pose, buf, light);
    }

    /**
     * Render a single body-part group, applying:
     * <ul>
     *     <li>an optional pixel-offset {@code anchor} (relative to current pose)</li>
     *     <li>{@code resetPoses()} on each {@link ModelPart}</li>
     *     <li>{@link NecroEntityBase#setAnim adapter.setAnim(...)}</li>
     *     <li>attack-swing override for arms</li>
     *     <li>{@link NecroEntityBase#preRender adapter.preRender(...)}</li>
     *     <li>the model-part draw call</li>
     *     <li>{@link NecroEntityBase#postRender adapter.postRender(...)}</li>
     * </ul>
     */
    private static void renderGroup(LivingEntity minion, NecroEntityBase adapter,
                                    BodyPartLocation loc, float[] anchor,
                                    float attackAnim, float walk, float speed, float age,
                                    float headYaw, float headPitch, boolean saddleOverlay,
                                    PoseStack pose, MultiBufferSource buf, int light) {
        if (adapter == null) return;
        var baked = MinionPartCache.get(adapter, loc);
        if (baked == null) return;

        pose.pushPose();
        if (anchor != null) {
            pose.translate(anchor[0] / 16F, anchor[1] / 16F, anchor[2] / 16F);
        }
        baked.resetPoses();
        adapter.setAnim(minion, baked.children(), loc, walk, speed, age, headYaw, headPitch);

        // Attack swing on arms: fold the cosine-pose toward a forward strike.
        if (attackAnim > 0F && (loc == BodyPartLocation.ArmLeft || loc == BodyPartLocation.ArmRight)) {
            float swing = -2F + 1.5F * pulse(attackAnim);
            for (ModelPart p : baked.children()) p.xRot = swing;
        }

        adapter.preRender(minion, pose, loc);
        draw(baked.root(), adapter.texture, pose, buf, light);
        if (saddleOverlay && adapter instanceof ISaddleAble sa) {
            ResourceLocation saddleTex = sa.getSaddleTexture();
            if (saddleTex != null) draw(baked.root(), saddleTex, pose, buf, light);
        }
        adapter.postRender(minion, pose, loc);
        pose.popPose();
    }

    private static void draw(ModelPart root, ResourceLocation tex,
                             PoseStack pose, MultiBufferSource buf, int light) {
        if (root == null || tex == null) return;
        root.render(pose, buf.getBuffer(RenderType.entityCutoutNoCull(tex)),
                    light, OverlayTexture.NO_OVERLAY);
    }

    private static BodyPart firstPart(BodyPart[] parts) {
        return parts == null || parts.length == 0 ? null : parts[0];
    }

    /** Triangle-wave pulse for the attack anim (matches legacy calc()). */
    private static float pulse(float a) {
        float par2 = 10F;
        float par1 = a * par2;
        return (Math.abs(par1 % par2 - par2 * 0.5F) - par2 * 0.25F) / (par2 * 0.25F);
    }
}
