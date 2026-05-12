package com.sirolf2009.necromancy.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.sirolf2009.necromancy.api.BodyPart;
import com.sirolf2009.necromancy.api.BodyPartLocation;
import com.sirolf2009.necromancy.api.ISaddleAble;
import com.sirolf2009.necromancy.api.NecroEntityBase;
import com.sirolf2009.necromancy.bodypart.BodyPartConfigManager;
import com.sirolf2009.necromancy.bodypart.BodyPartItemIds;
import com.sirolf2009.necromancy.bodypart.BodypartDefinition;
import com.sirolf2009.necromancy.bodypart.BodypartPreviewDiagnostics;
import com.sirolf2009.necromancy.client.model.MinionPartCache;
import com.sirolf2009.necromancy.entity.MinionAssembly;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;

import java.util.EnumMap;

/**
 * Stateless helper that draws an "assembled minion" -- a body composed of body
 * parts from up to five different {@link NecroEntityBase} adapters.
 *
 * <p>Rendering order, anchor offsets and per-adapter animation hooks all
 * follow the legacy {@code ModelMinion#render} flow exactly, with v0.5
 * extensions for the hybrid anchor cascade and the ARMS_AS_LEGS fallback.
 *
 * <p>Two callers:
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
     * {@code pushPose()} + global living-entity transform (translation, body
     * yaw rotation, scale).
     *
     * <p>Behaviour switches on {@link MinionAssembly#mode()}:
     * <ul>
     *     <li>{@link MinionAssembly.Mode#STANDING} -- legs (if any) -> torso ->
     *         arms + head, with the hybrid anchor cascade for missing parts.</li>
     *     <li>{@link MinionAssembly.Mode#ARMS_AS_LEGS} -- whole stack lifted by
     *         {@code profile.liftPixels()}; arms swing as legs at half
     *         amplitude; torso forced to its rest pose; head static.</li>
     *     <li>{@link MinionAssembly.Mode#STATIC} -- same as STANDING but no
     *         walk-cycle motion (handled implicitly by zero speed).</li>
     * </ul>
     */
    public static void renderAssembled(LivingEntity minion,
                                       MinionAssembly assembly,
                                       boolean saddled,
                                       float attackAnim,
                                       float walk, float speed, float age,
                                       float headYaw, float headPitch,
                                       PoseStack pose, MultiBufferSource buf, int light) {
        if (assembly == null) return;
        if (assembly.mode() == MinionAssembly.Mode.ARMS_AS_LEGS) {
            renderArmsAsLegs(minion, assembly, saddled, attackAnim, walk, speed, age,
                             headYaw, headPitch, pose, buf, light);
        } else {
            renderStanding(minion, assembly, saddled, attackAnim, walk, speed, age,
                           headYaw, headPitch, pose, buf, light);
        }
    }

    /**
     * Backwards-compatible wrapper accepting a raw EnumMap.  Used by the altar
     * block-entity renderer, which doesn't have an {@link com.sirolf2009.necromancy.entity.EntityMinion}
     * yet but still wants the same assembled-render output.
     */
    public static void renderAssembled(LivingEntity minion,
                                       EnumMap<BodyPartLocation, NecroEntityBase> adapters,
                                       boolean saddled,
                                       float attackAnim,
                                       float walk, float speed, float age,
                                       float headYaw, float headPitch,
                                       PoseStack pose, MultiBufferSource buf, int light) {
        MinionAssembly preview = MinionAssembly.fromAdapters(
            adapters.get(BodyPartLocation.Head),
            adapters.get(BodyPartLocation.Torso),
            adapters.get(BodyPartLocation.ArmLeft),
            adapters.get(BodyPartLocation.ArmRight),
            adapters.get(BodyPartLocation.Legs));
        renderAssembled(minion, preview, saddled, attackAnim, walk, speed, age,
                        headYaw, headPitch, pose, buf, light);
    }

    /**
     * Renders a single adapter limb in rest pose — bodypart dev preview / tooling parity with altar assembled renderer.
     *
     * @param visualSpec when non-null, mesh translation uses this definition's visual offset (draft/live),
     *                   and geometry is drawn from an isolated model cache so animated minions cannot smear poses here.
     */
    public static void renderSinglePartAtRest(@Nullable LivingEntity contextHost,
                                              NecroEntityBase adapter,
                                              BodyPartLocation slot,
                                              @Nullable BodypartDefinition visualSpec,
                                              PoseStack pose, MultiBufferSource buf, int light) {
        if (adapter == null) return;
        renderGroup(contextHost, adapter, slot, slot, null,
            0F, 0F, 0F, 0F, 0F, 0F, false,
            /*forceRestPose=*/true, visualSpec, /*isolatePreviewMesh=*/true, pose, buf, light);
    }

    // ------------------------------------------------------------------ --
    //                              STANDING
    // ------------------------------------------------------------------ --

    private static void renderStanding(LivingEntity minion, MinionAssembly assembly,
                                       boolean saddled, float attackAnim,
                                       float walk, float speed, float age,
                                       float headYaw, float headPitch,
                                       PoseStack pose, MultiBufferSource buf, int light) {

        NecroEntityBase legsAdapter  = assembly.legs();
        NecroEntityBase torsoAdapter = assembly.torso();
        NecroEntityBase armLAdapter  = assembly.armLeft();
        NecroEntityBase armRAdapter  = assembly.armRight();
        NecroEntityBase headAdapter  = assembly.head();

        // -- LEGS --
        renderGroup(minion, legsAdapter, BodyPartLocation.Legs, BodyPartLocation.Legs, null,
                    attackAnim, walk, speed, age, headYaw, headPitch, false,
                    /*forceRestPose=*/false, null, false, pose, buf, light);

        // Move the anchor up to where the torso attaches to the legs.
        BodyPart legAnchor = firstPart(legsAdapter == null ? null : legsAdapter.legs);
        if (legAnchor != null && legAnchor.torsoPos != null) {
            pose.translate(legAnchor.torsoPos[0] / 16F,
                           legAnchor.torsoPos[1] / 16F,
                           legAnchor.torsoPos[2] / 16F);
        }

        // -- TORSO --
        renderGroup(minion, torsoAdapter, BodyPartLocation.Torso, BodyPartLocation.Torso, null,
                    attackAnim, walk, speed, age, headYaw, headPitch, saddled,
                    /*forceRestPose=*/false, null, false, pose, buf, light);

        BodyPart torsoAnchor = firstPart(torsoAdapter == null ? null : torsoAdapter.torso);
        boolean torsoPresent = torsoAdapter != null && torsoAnchor != null;

        if (torsoPresent) {
            // Arms anchored to the torso.
            renderGroup(minion, armLAdapter, BodyPartLocation.ArmLeft, BodyPartLocation.ArmLeft,
                        torsoAnchor.armLeftPos,
                        attackAnim, walk, speed, age, headYaw, headPitch, false,
                        false, null, false, pose, buf, light);

            renderGroup(minion, armRAdapter, BodyPartLocation.ArmRight, BodyPartLocation.ArmRight,
                        torsoAnchor.armRightPos,
                        attackAnim, walk, speed, age, headYaw, headPitch, false,
                        false, null, false, pose, buf, light);

            // Head anchored to the torso.
            renderGroup(minion, headAdapter, BodyPartLocation.Head, BodyPartLocation.Head,
                        torsoAnchor.headPos,
                        0F, walk, speed, age, headYaw, headPitch, false,
                        false, null, false, pose, buf, light);
        } else {
            // No torso: hide arms (cascade rule), render head at the current
            // anchor (legs torsoPos or world origin if no legs either).
            renderGroup(minion, headAdapter, BodyPartLocation.Head, BodyPartLocation.Head, null,
                        0F, walk, speed, age, headYaw, headPitch, false,
                        false, null, false, pose, buf, light);
        }
    }

    // ------------------------------------------------------------------ --
    //                            ARMS-AS-LEGS
    // ------------------------------------------------------------------ --

    /**
     * Specialised layout for minions that have arms + torso but no legs.
     *
     * <p>Visual contract:
     * <ol>
     *     <li>Lift the entire model by {@code profile.liftPixels()} so the
     *         arm-tips touch the ground (no floating).</li>
     *     <li>Render arms with the LEGS animation hook so they perform a
     *         walk-cycle, and HALVE the swing speed so the mob plods on
     *         its hands.</li>
     *     <li>Render the torso forced to its rest pose (no quadruped
     *         flat-lay); attach point is the arm's shoulder height.</li>
     *     <li>Head static (no head tracking) on top of the torso.</li>
     * </ol>
     */
    private static void renderArmsAsLegs(LivingEntity minion, MinionAssembly assembly,
                                         boolean saddled, float attackAnim,
                                         float walk, float speed, float age,
                                         float headYaw, float headPitch,
                                         PoseStack pose, MultiBufferSource buf, int light) {

        float lift = assembly.profile().liftPixels();
        // Y is flipped by the caller's pose.scale(-1, -1, 1); negative pose-Y
        // delta = positive world-Y delta = visually lifting upward.
        if (lift > 0F) pose.translate(0F, -lift / 16F, 0F);

        NecroEntityBase torsoAdapter = assembly.torso();
        NecroEntityBase armLAdapter  = assembly.armLeft();
        NecroEntityBase armRAdapter  = assembly.armRight();
        NecroEntityBase headAdapter  = assembly.head();

        // Halved swing for "walking on hands".
        float armSpeed = speed * 0.5F;

        // Render arms first, anchored at the lifted ground.  Their animation
        // location is forced to LEGS so the adapter's walk-cycle fires.
        renderGroup(minion, armLAdapter, BodyPartLocation.ArmLeft, BodyPartLocation.Legs, null,
                    /*attackAnim=*/0F, walk, armSpeed, age, 0F, 0F, false,
                    false, null, false, pose, buf, light);
        renderGroup(minion, armRAdapter, BodyPartLocation.ArmRight, BodyPartLocation.Legs, null,
                    0F, walk, armSpeed, age, 0F, 0F, false,
                    false, null, false, pose, buf, light);

        // Translate up to the arm-shoulder so the torso sits on top of the
        // hands.  In the absence of explicit metadata we use the first arm
        // part's setPos.y as the shoulder height.
        BodyPart shoulder = firstPart(armLAdapter == null ? null : armLAdapter.armLeft);
        if (shoulder == null) shoulder = firstPart(armRAdapter == null ? null : armRAdapter.armRight);
        if (shoulder != null) {
            // Move "up" in world == positive pose-Y after the flip... but a
            // standard arm has setPos.y = +2, meaning +2 px DOWNWARD in model
            // space.  We want to translate the OPPOSITE direction so the
            // torso pivot lands at the shoulder.  Easy: copy setPos as-is.
            pose.translate(0F, shoulder.pose.y / 16F, shoulder.pose.z / 16F);
        }

        // Render torso with rest pose forced; no quadruped flat-lay even when
        // the adapter is a quadruped.
        renderGroup(minion, torsoAdapter, BodyPartLocation.Torso, BodyPartLocation.Torso, null,
                    attackAnim, walk, /*speed=*/0F, age, 0F, 0F, saddled,
                    /*forceRestPose=*/true, null, false, pose, buf, light);

        // Head: anchored to the torso headPos as in STANDING, but locked.
        BodyPart torsoAnchor = firstPart(torsoAdapter == null ? null : torsoAdapter.torso);
        if (torsoAnchor != null) {
            renderGroup(minion, headAdapter, BodyPartLocation.Head, BodyPartLocation.Head,
                        torsoAnchor.headPos,
                        0F, walk, 0F, age, 0F, 0F, false,
                        true, null, false, pose, buf, light);
        }
    }

    // ------------------------------------------------------------------ --
    //                           SHARED RENDER GROUP
    // ------------------------------------------------------------------ --

    /**
     * Render a single body-part group, applying:
     * <ul>
     *     <li>an optional pixel-offset {@code anchor} (relative to current pose)</li>
     *     <li>{@code resetPoses()} on each {@link ModelPart}</li>
     *     <li>{@link NecroEntityBase#setAnim adapter.setAnim(...)} unless
     *         {@code forceRestPose} is true</li>
     *     <li>attack-swing override for arms</li>
     *     <li>{@link NecroEntityBase#preRender adapter.preRender(...)} unless
     *         {@code forceRestPose} is true</li>
     *     <li>the model-part draw call</li>
     *     <li>{@link NecroEntityBase#postRender adapter.postRender(...)} unless
     *         {@code forceRestPose} is true</li>
     * </ul>
     *
     * <p>{@code geomLoc} selects which body-part array is fetched from the
     * cache (controls geometry).  {@code animLoc} selects which slot the
     * adapter's {@code setAnim} hook will see.  These differ for ARMS_AS_LEGS
     * mode where the arms render their own geometry but call setAnim with
     * {@code BodyPartLocation.Legs}.
     */
    private static void renderGroup(LivingEntity minion, NecroEntityBase adapter,
                                    BodyPartLocation geomLoc, BodyPartLocation animLoc,
                                    float[] anchor,
                                    float attackAnim, float walk, float speed, float age,
                                    float headYaw, float headPitch, boolean saddleOverlay,
                                    boolean forceRestPose,
                                    @Nullable BodypartDefinition visualSpec,
                                    boolean isolatePreviewMesh,
                                    PoseStack pose, MultiBufferSource buf, int light) {
        if (adapter == null) return;
        var baked = MinionPartCache.get(adapter, geomLoc, isolatePreviewMesh);
        if (baked == null) return;

        pose.pushPose();
        if (anchor != null) {
            pose.translate(anchor[0] / 16F, anchor[1] / 16F, anchor[2] / 16F);
        }
        applyVisualOffset(pose, visualSpec, adapter, geomLoc);
        if (BodypartPreviewDiagnostics.ENABLED && isolatePreviewMesh) {
            logIsolateMeshSample(baked, "beforeReset", adapter.mobName, geomLoc, isolatePreviewMesh);
        }
        baked.resetPoses();
        if (BodypartPreviewDiagnostics.ENABLED && isolatePreviewMesh) {
            logIsolateMeshSample(baked, "afterReset", adapter.mobName, geomLoc, isolatePreviewMesh);
        }
        if (!forceRestPose) {
            boolean armsAsLegsWalk = (geomLoc == BodyPartLocation.ArmLeft || geomLoc == BodyPartLocation.ArmRight)
                && animLoc == BodyPartLocation.Legs;
            if (armsAsLegsWalk) {
                adapter.setArmsAsLegsAnim(minion, baked.children(), walk, speed, age, headYaw, headPitch);
            } else {
                adapter.setAnim(minion, baked.children(), animLoc, walk, speed, age, headYaw, headPitch);
            }
        }

        // Attack swing on arms: fold the cosine-pose toward a forward strike.
        if (!forceRestPose && attackAnim > 0F
                && (geomLoc == BodyPartLocation.ArmLeft || geomLoc == BodyPartLocation.ArmRight)) {
            float swing = -2F + 1.5F * pulse(attackAnim);
            for (ModelPart p : baked.children()) p.xRot = swing;
        }

        if (!forceRestPose) adapter.preRender(minion, pose, geomLoc);
        draw(baked.root(), adapter.texture, pose, buf, light);
        if (saddleOverlay && adapter instanceof ISaddleAble sa) {
            ResourceLocation saddleTex = sa.getSaddleTexture();
            if (saddleTex != null) draw(baked.root(), saddleTex, pose, buf, light);
        }
        if (!forceRestPose) adapter.postRender(minion, pose, geomLoc);
        pose.popPose();
    }

    private static void applyVisualOffset(PoseStack pose, @Nullable BodypartDefinition visualSpec,
                                          NecroEntityBase adapter, BodyPartLocation geomLoc) {
        if (visualSpec != null) {
            pose.translate((float) visualSpec.visDx(), (float) visualSpec.visDy(), (float) visualSpec.visDz());
            return;
        }
        if (adapter == null) return;
        ResourceLocation id = BodyPartItemIds.inferredPartId(adapter.mobName, geomLoc);
        BodyPartConfigManager.INSTANCE.get(id).ifPresent(def ->
            pose.translate((float) def.visDx(), (float) def.visDy(), (float) def.visDz()));
    }

    private static void logIsolateMeshSample(MinionPartCache.Baked baked,
                                             String phase,
                                             String adapterMob,
                                             BodyPartLocation geomLoc,
                                             boolean isolatePreviewMesh) {
        ModelPart root = baked.root();
        ModelPart[] ch = baked.children();
        float x0 = ch.length > 0 ? ch[0].xRot : Float.NaN;
        float y0 = ch.length > 0 ? ch[0].yRot : Float.NaN;
        float z0 = ch.length > 0 ? ch[0].zRot : Float.NaN;
        BodypartPreviewDiagnostics.logMeshRotations(
            phase, adapterMob, geomLoc, isolatePreviewMesh,
            root.xRot, root.yRot, root.zRot,
            ch.length, x0, y0, z0);
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
