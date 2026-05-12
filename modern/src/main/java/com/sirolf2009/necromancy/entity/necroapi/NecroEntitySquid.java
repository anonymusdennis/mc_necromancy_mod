package com.sirolf2009.necromancy.entity.necroapi;

import com.sirolf2009.necromancy.api.BodyPart;
import com.sirolf2009.necromancy.api.BodyPartLocation;
import com.sirolf2009.necromancy.api.ISaddleAble;
import com.sirolf2009.necromancy.api.LocomotionProfile;
import com.sirolf2009.necromancy.api.NecroEntityBase;
import com.sirolf2009.necromancy.api.VoiceProfile;
import com.sirolf2009.necromancy.item.NecromancyItems;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/**
 * 1:1 port of the legacy {@code NecroEntitySquid}.
 *
 * <p>Squid parts use the vanilla 64x32 squid texture.  The "head" is a 12x16x12
 * cube from the top of the squid texture (the squid has no separate head, so
 * the entire body cube is recycled as the head).  The "legs" are eight 16x2x2
 * tentacles fanned out in a fixed splay.  Tentacle animation matches the
 * vanilla {@link net.minecraft.client.model.SquidModel}.
 */
public class NecroEntitySquid extends NecroEntityBase implements ISaddleAble {

    public NecroEntitySquid() {
        super("Squid");
        headItem  = new ItemStack(NecromancyItems.bodyPart("Squid Head"));
        torsoItem = new ItemStack(NecromancyItems.bodyPart("Squid Torso"));
        legItem   = new ItemStack(NecromancyItems.bodyPart("Squid Legs"));
        texture   = ResourceLocation.parse("minecraft:textures/entity/squid/squid.png");
        textureWidth  = 64;
        textureHeight = 32;
        hasArms = false;
    }

    @Override public void initRecipes() { initDefaultRecipes(Items.INK_SAC); }
    @Override public ResourceLocation getSaddleTexture() { return com.sirolf2009.necromancy.Reference.TEXTURE_SADDLE_SQUID; }
    @Override public LocomotionProfile locomotion() { return LocomotionProfile.SWIM_DEFAULT; }
    @Override public VoiceProfile voice() { return VoiceProfile.SQUID; }

    /** The squid mantle.  Legacy shifts it +8 along Y at construction. */
    @Override
    public BodyPart[] initHead() {
        BodyPart squidBody = new BodyPart(this, 0, 0);
        squidBody.addBox(-6F, -20F, -6F, 12, 16, 12);
        squidBody.pose = PartPose.offset(0F, 8F, 0F);
        return new BodyPart[] { squidBody };
    }

    /** Reuses the squid mantle for the torso.  Wears the same texture region. */
    @Override
    public BodyPart[] initTorso() {
        float[] armLeftPos  = { -6F, -4F, 0F };
        float[] armRightPos = { 10F, -4F, 0F };
        float[] headPos     = {  4F, -8F, 0F };
        BodyPart squidBody = new BodyPart(this, armLeftPos, armRightPos, headPos, 0, 0);
        squidBody.addBox(-2F, -12F, -5F, 12, 16, 12);
        squidBody.pose = PartPose.offset(0F, 8F, 0F);
        return new BodyPart[] { squidBody };
    }

    /** Eight tentacles -- four on each side, sharing texOffs (18,0). */
    @Override
    public BodyPart[] initLegs() {
        float[] torsoPos = { -4F, 6F, 3F };
        BodyPart[] legs = new BodyPart[8];
        legs[0] = new BodyPart(this, torsoPos, 18, 0); legs[0].addBox(-15F, -1F, -1F, 16, 2, 2).setPos(-4F, 15F,  2F);
        legs[1] = new BodyPart(this, torsoPos, 18, 0); legs[1].addBox(-1F,  -1F, -1F, 16, 2, 2).setPos( 4F, 15F,  2F);
        legs[2] = new BodyPart(this, torsoPos, 18, 0); legs[2].addBox(-15F, -1F, -1F, 16, 2, 2).setPos(-4F, 15F,  1F);
        legs[3] = new BodyPart(this, torsoPos, 18, 0); legs[3].addBox(-1F,  -1F, -1F, 16, 2, 2).setPos( 4F, 15F,  1F);
        legs[4] = new BodyPart(this, torsoPos, 18, 0); legs[4].addBox(-15F, -1F, -1F, 16, 2, 2).setPos(-4F, 15F,  0F);
        legs[5] = new BodyPart(this, torsoPos, 18, 0); legs[5].addBox(-1F,  -1F, -1F, 16, 2, 2).setPos( 4F, 15F,  0F);
        legs[6] = new BodyPart(this, torsoPos, 18, 0); legs[6].addBox(-15F, -1F, -1F, 16, 2, 2).setPos(-4F, 15F, -1F);
        legs[7] = new BodyPart(this, torsoPos, 18, 0); legs[7].addBox(-1F,  -1F, -1F, 16, 2, 2).setPos( 4F, 15F, -1F);
        return legs;
    }

    @Override public BodyPart[] initArmLeft()  { return new BodyPart[0]; }
    @Override public BodyPart[] initArmRight() { return new BodyPart[0]; }

    /**
     * Squid tentacle wiggle.  Splays the tentacles outward, then layers a
     * sinusoidal sway whose phase rotates around all eight tentacles, so they
     * undulate in a wave -- matches the legacy code line-for-line.
     */
    @Override
    public void setAnim(LivingEntity minion, ModelPart[] parts, BodyPartLocation loc,
                        float limbSwing, float limbSwingAmount, float ageInTicks,
                        float netHeadYaw, float headPitch) {
        if (loc != BodyPartLocation.Legs || parts.length < 8) return;

        float quarter = Mth.PI / 4F;
        parts[0].zRot = -quarter;
        parts[1].zRot =  quarter;
        parts[2].zRot = -quarter * 0.74F;
        parts[3].zRot =  quarter * 0.74F;
        parts[4].zRot = -quarter * 0.74F;
        parts[5].zRot =  quarter * 0.74F;
        parts[6].zRot = -quarter;
        parts[7].zRot =  quarter;

        float fan = 0.3926991F;
        parts[0].yRot =  fan * 2F;
        parts[1].yRot = -fan * 2F;
        parts[2].yRot =  fan;
        parts[3].yRot = -fan;
        parts[4].yRot = -fan;
        parts[5].yRot =  fan;
        parts[6].yRot = -fan * 2F;
        parts[7].yRot =  fan * 2F;

        float swing = limbSwing * 0.6662F;
        float yA = -(Mth.cos(swing * 2F)              * 0.4F) * limbSwingAmount;
        float yB = -(Mth.cos(swing * 2F + Mth.PI)     * 0.4F) * limbSwingAmount;
        float yC = -(Mth.cos(swing * 2F + Mth.HALF_PI)* 0.4F) * limbSwingAmount;
        float yD = -(Mth.cos(swing * 2F + Mth.PI * 1.5F) * 0.4F) * limbSwingAmount;
        float zA = Math.abs(Mth.sin(swing)              * 0.4F) * limbSwingAmount;
        float zB = Math.abs(Mth.sin(swing + Mth.PI)     * 0.4F) * limbSwingAmount;
        float zC = Math.abs(Mth.sin(swing + Mth.HALF_PI)* 0.4F) * limbSwingAmount;
        float zD = Math.abs(Mth.sin(swing + Mth.PI * 1.5F) * 0.4F) * limbSwingAmount;

        parts[0].yRot += yA; parts[1].yRot += -yA;
        parts[2].yRot += yB; parts[3].yRot += -yB;
        parts[4].yRot += yC; parts[5].yRot += -yC;
        parts[6].yRot += yD; parts[7].yRot += -yD;

        parts[0].zRot += zA; parts[1].zRot += -zA;
        parts[2].zRot += zB; parts[3].zRot += -zB;
        parts[4].zRot += zC; parts[5].zRot += -zC;
        parts[6].zRot += zD; parts[7].zRot += -zD;
    }

    @Override public void setAttributes(LivingEntity m, BodyPartLocation loc) {
        switch (loc) {
            case Head  -> addAttributeMods(m, "Head", 0.5, 1, 0, 0, 0.5);
            case Torso -> addAttributeMods(m, "Torso", 2, 0, 0, 0, 0);
            case Legs  -> addAttributeMods(m, "Legs", 0.5, 0, 1, 2, 0.5);
            default    -> {}
        }
    }
}
