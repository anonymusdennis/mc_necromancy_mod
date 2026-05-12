package com.sirolf2009.necromancy.api;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;

/**
 * Quadruped {@link NecroEntityBase} -- Cow / Pig / Sheep / Wolf body shape.
 *
 * <p>Direct port of {@code com.sirolf2009.necroapi.NecroEntityQuadruped}.  The
 * {@link #size} field is the leg length in pixels (12 = cow, 8 = pig, etc).
 *
 * <p>The quadruped torso is rendered with a +90&deg; X rotation -- this is the
 * crucial bit that makes a cow body look like a cow body (lying horizontal
 * over four legs) rather than a vertical "torso" that looks like a head.
 * The legacy mod baked that rotation into {@code setRotationAngles} for the
 * Torso location; we do the same here.
 */
public abstract class NecroEntityQuadruped extends NecroEntityBase {

    /** Leg height in pixels (default 12, cow-sized). */
    public int size;

    protected NecroEntityQuadruped(String mobName, int size) {
        super(mobName);
        this.size = size;
    }

    @Override
    public BodyPart[] initHead() {
        BodyPart head = new BodyPart(this, 0, 0).addBox(-4F, -4F, -4F, 8, 8, 8);
        return new BodyPart[] { head };
    }

    @Override
    public BodyPart[] initTorso() {
        float[] headPos     = { 4.0F, 12F - size, -14.0F };
        float[] armLeftPos  = { -1.0F, 12.0F, -10.0F };
        float[] armRightPos = {  5.0F, 12.0F, -10.0F };
        BodyPart torso = new BodyPart(this, armLeftPos, armRightPos, headPos, 28, 8);
        torso.addBox(-1F, -12F, -12F, 10, 16, 8);
        return new BodyPart[] { torso };
    }

    @Override
    public BodyPart[] initArmLeft() {
        BodyPart armLeft = new BodyPart(this, 0, 16).mirror();
        armLeft.addBox(0F, 0F, -1F, 4, size, 4);
        return new BodyPart[] { armLeft };
    }

    @Override
    public BodyPart[] initArmRight() {
        BodyPart armRight = new BodyPart(this, 0, 16);
        armRight.addBox(0F, 0F, -1F, 4, size, 4);
        return new BodyPart[] { armRight };
    }

    @Override
    public BodyPart[] initLegs() {
        float[] torsoPos = { -4F, 4F, 0F };
        BodyPart legLeft  = new BodyPart(this, torsoPos, 0, 16).mirror();
        legLeft.addBox(-2F, 0F, -2F, 4, size, 4).setPos(-3F, 22F - size, 3F);
        BodyPart legRight = new BodyPart(this, torsoPos, 0, 16);
        legRight.addBox(-2F, 0F, -2F, 4, size, 4).setPos( 3F, 22F - size, 3F);
        return new BodyPart[] { legLeft, legRight };
    }

    /**
     * Quadruped animation: head pitches with the look vector, torso flat-lays
     * (xRot = +&pi;/2), arms (cow has none normally) swing out of phase with
     * the leg cycle, and the four-legged walk cycle is the standard
     * cosine-pair from the legacy code.
     */
    @Override
    public void setAnim(LivingEntity minion, ModelPart[] parts, BodyPartLocation loc,
                        float limbSwing, float limbSwingAmount, float ageInTicks,
                        float netHeadYaw, float headPitch) {
        switch (loc) {
            case Head -> {
                // Legacy used `par5` (pitch) for both X and Y on quadrupeds; we
                // apply it the same way to preserve the original look-aim feel.
                float pitchRad = headPitch * Mth.DEG_TO_RAD;
                for (ModelPart p : parts) { p.xRot = pitchRad; p.yRot = pitchRad; }
            }
            case Torso -> {
                // Critical: rotate cow body 90 degrees so it lies flat across
                // the legs rather than standing up like a humanoid torso.
                if (parts.length > 0) {
                    parts[0].xRot = Mth.HALF_PI;
                }
            }
            case ArmLeft -> {
                if (parts.length > 0) {
                    parts[0].xRot = Mth.cos(limbSwing * 0.6662F + Mth.PI) * 1.4F * limbSwingAmount;
                    parts[0].zRot = 0F;
                }
            }
            case ArmRight -> {
                if (parts.length > 0) {
                    parts[0].xRot = Mth.cos(limbSwing * 0.6662F) * 1.4F * limbSwingAmount;
                    parts[0].zRot = 0F;
                }
            }
            case Legs -> {
                if (parts.length >= 2) {
                    parts[0].xRot = Mth.cos(limbSwing * 0.6662F)          * 1.4F * limbSwingAmount;
                    parts[1].xRot = Mth.cos(limbSwing * 0.6662F + Mth.PI) * 1.4F * limbSwingAmount;
                    parts[0].yRot = 0F;
                    parts[1].yRot = 0F;
                }
            }
        }
    }

    /**
     * Legacy preRender: when the entity has the "extra" sneaking flag set,
     * the saddle/torso is tipped forward by an extra 90 degrees and shifted.
     * We preserve the call shape (no-op in normal play) so future overrides
     * (e.g. for saddle states) can hook in.
     */
    @Override
    public void preRender(LivingEntity minion, PoseStack pose, BodyPartLocation location) {
        if (minion != null && location == BodyPartLocation.Torso && minion.isShiftKeyDown()) {
            pose.mulPose(com.mojang.math.Axis.XP.rotationDegrees(-90F));
            pose.translate(0F, -0.5F, 1F);
        }
    }
}
