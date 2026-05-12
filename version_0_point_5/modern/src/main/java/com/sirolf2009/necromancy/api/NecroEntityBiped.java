package com.sirolf2009.necromancy.api;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;

/**
 * Biped (humanoid) {@link NecroEntityBase} -- Zombie/Skeleton/Villager body
 * shape.  The geometry is the standard 8x12x4 torso, two 4x12x4 arms, two
 * 4x12x4 legs and an 8x8x8 head.
 *
 * <p>Direct port of {@code com.sirolf2009.necroapi.NecroEntityBiped}, including
 * the legacy {@code setRotationAngles} animation that swings arms and legs in
 * step with {@link LivingEntity#walkAnimation}.
 */
public abstract class NecroEntityBiped extends NecroEntityBase {

    protected NecroEntityBiped(String mobName) {
        super(mobName);
    }

    @Override
    public BodyPart[] initHead() {
        BodyPart head = new BodyPart(this, 0, 0).addBox(-4F, -4F, -4F, 8, 8, 8);
        BodyPart hat  = new BodyPart(this, 32, 0).addBox(-4F, -4F, -4F, 8, 8, 8, 0.5F);
        return new BodyPart[] { head, hat };
    }

    @Override
    public BodyPart[] initTorso() {
        float[] headPos     = { 4.0F, -4.0F, 2.0F };
        float[] armLeftPos  = { -4F,  0.0F, 2.0F };
        float[] armRightPos = {  8F,  0.0F, 2.0F };
        BodyPart torso = new BodyPart(this, armLeftPos, armRightPos, headPos, 16, 16);
        torso.addBox(0F, 0F, 0F, 8, 12, 4);
        return new BodyPart[] { torso };
    }

    @Override
    public BodyPart[] initArmLeft() {
        BodyPart armLeft = new BodyPart(this, 40, 16).mirror();
        armLeft.addBox(0F, 0F, -2F, 4, 12, 4);
        return new BodyPart[] { armLeft };
    }

    @Override
    public BodyPart[] initArmRight() {
        BodyPart armRight = new BodyPart(this, 40, 16);
        armRight.addBox(0F, 0F, -2F, 4, 12, 4);
        return new BodyPart[] { armRight };
    }

    @Override
    public BodyPart[] initLegs() {
        float[] torsoPos = { -4F, -2F, -2F };
        BodyPart legLeft  = new BodyPart(this, torsoPos, 0, 16).mirror();
        legLeft.addBox(-4F, -2F, -2F, 4, 12, 4).setPos(0F, 12F, 0F);
        BodyPart legRight = new BodyPart(this, torsoPos, 0, 16);
        legRight.addBox(0F, -2F, -2F, 4, 12, 4).setPos(0F, 12F, 0F);
        return new BodyPart[] { legLeft, legRight };
    }

    /**
     * Standard biped walk animation -- legs swing forward/back, arms swing in
     * counter-phase, head rotates to follow the look vector.  Mirrors the
     * legacy {@code setRotationAngles} for biped mobs exactly.
     */
    @Override
    public void setAnim(LivingEntity minion, ModelPart[] parts, BodyPartLocation loc,
                        float limbSwing, float limbSwingAmount, float ageInTicks,
                        float netHeadYaw, float headPitch) {
        switch (loc) {
            case Head -> {
                float yawRad   = netHeadYaw * Mth.DEG_TO_RAD;
                float pitchRad = headPitch  * Mth.DEG_TO_RAD;
                for (ModelPart p : parts) { p.yRot = yawRad; p.xRot = pitchRad; }
            }
            case ArmLeft -> {
                if (parts.length > 0) {
                    parts[0].xRot = Mth.cos(limbSwing * 0.6662F) * 2F * limbSwingAmount * 0.5F;
                    parts[0].zRot = 0F;
                }
            }
            case ArmRight -> {
                if (parts.length > 0) {
                    parts[0].xRot = Mth.cos(limbSwing * 0.6662F + Mth.PI) * 2F * limbSwingAmount * 0.5F;
                    parts[0].zRot = 0F;
                }
            }
            case Legs -> {
                if (parts.length >= 2) {
                    parts[0].xRot = Mth.cos(limbSwing * 0.6662F)             * 1.4F * limbSwingAmount;
                    parts[1].xRot = Mth.cos(limbSwing * 0.6662F + Mth.PI)    * 1.4F * limbSwingAmount;
                    parts[0].yRot = 0F;
                    parts[1].yRot = 0F;
                }
            }
            case Torso -> { /* biped torso is static */ }
        }
    }

    /** Bipeds need no extra matrix transform. */
    @Override
    public void preRender(LivingEntity minion, PoseStack pose, BodyPartLocation location) { }
}
