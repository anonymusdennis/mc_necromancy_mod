package com.sirolf2009.necromancy.entity.necroapi;

import com.sirolf2009.necromancy.api.BodyPart;
import com.sirolf2009.necromancy.api.BodyPartLocation;
import com.sirolf2009.necromancy.api.NecroEntityBase;
import com.sirolf2009.necromancy.item.NecromancyItems;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/**
 * 1:1 port of the legacy {@code NecroEntityCreeper}.
 *
 * <p>The creeper has its OWN distinct body shape -- biped torso/head, but four
 * stubby legs in a square pattern, and no arms.  We extend {@link NecroEntityBase}
 * directly (not {@link com.sirolf2009.necromancy.api.NecroEntityBiped}) so the
 * 4-leg geometry replaces the standard 2-leg biped layout.
 */
public class NecroEntityCreeper extends NecroEntityBase {

    public NecroEntityCreeper() {
        super("Creeper");
        headItem  = new ItemStack(Items.CREEPER_HEAD);
        torsoItem = new ItemStack(NecromancyItems.bodyPart("Creeper Torso"));
        legItem   = new ItemStack(NecromancyItems.bodyPart("Creeper Legs"));
        texture   = ResourceLocation.parse("minecraft:textures/entity/creeper/creeper.png");
        hasArms   = false;
    }

    @Override public void initRecipes() { initDefaultRecipes(Items.GUNPOWDER); }

    @Override
    public BodyPart[] initHead() {
        BodyPart head = new BodyPart(this, 0, 0).addBox(-4F, -4F, -4F, 8, 8, 8);
        return new BodyPart[] { head };
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

    /**
     * Four creeper feet, exactly mirroring the vanilla {@code CreeperModel}.
     *
     * <p>Each leg pivots at <b>its top corner</b> (the joint where it meets
     * the body), so the {@code xRot} swing is a small motion at the foot
     * rather than a giant arc -- this is what fixes "legs swinging 5
     * kilometres" the legacy mod's creeper had.
     *
     * <p>The four pivot points form a 4x8 footprint under the 8x4 body.  Box
     * coordinates are body-relative to the pivot: -2..+2 wide, 0..6 tall,
     * -2..+2 deep -- identical to vanilla.
     */
    @Override
    public BodyPart[] initLegs() {
        float[] torsoPos = { -4F, 4F, -2F };
        // legs in vanilla CreeperModel order: rightHind, leftHind, rightFront, leftFront
        BodyPart legRH = new BodyPart(this, torsoPos, 0, 16);
        legRH.addBox(-2F, 0F, -2F, 4, 6, 4).setPos(-2F, 16F,  4F);
        BodyPart legLH = new BodyPart(this, torsoPos, 0, 16).mirror();
        legLH.addBox(-2F, 0F, -2F, 4, 6, 4).setPos( 2F, 16F,  4F);
        BodyPart legRF = new BodyPart(this, torsoPos, 0, 16);
        legRF.addBox(-2F, 0F, -2F, 4, 6, 4).setPos(-2F, 16F, -4F);
        BodyPart legLF = new BodyPart(this, torsoPos, 0, 16).mirror();
        legLF.addBox(-2F, 0F, -2F, 4, 6, 4).setPos( 2F, 16F, -4F);
        return new BodyPart[] { legRH, legLH, legRF, legLF };
    }

    @Override public BodyPart[] initArmLeft()  { return new BodyPart[0]; }
    @Override public BodyPart[] initArmRight() { return new BodyPart[0]; }

    /**
     * Creeper trot animation -- a 1:1 reuse of vanilla
     * {@link net.minecraft.client.model.QuadrupedModel}'s walk cycle.  The
     * four legs animate in DIAGONAL pairs ("trot"):
     * <pre>
     *   rightHind  &amp; leftFront  -> cos(swing)
     *   leftHind   &amp; rightFront -> cos(swing + PI)
     * </pre>
     * which is exactly how a real creeper walks in vanilla Minecraft.
     */
    @Override
    public void setAnim(LivingEntity minion, ModelPart[] parts, BodyPartLocation loc,
                        float limbSwing, float limbSwingAmount, float ageInTicks,
                        float netHeadYaw, float headPitch) {
        switch (loc) {
            case Head -> {
                if (parts.length > 0) {
                    parts[0].yRot = netHeadYaw * Mth.DEG_TO_RAD;
                    parts[0].xRot = headPitch  * Mth.DEG_TO_RAD;
                }
            }
            case Legs -> {
                if (parts.length >= 4) {
                    float a =  Mth.cos(limbSwing * 0.6662F)          * 1.4F * limbSwingAmount;
                    float b =  Mth.cos(limbSwing * 0.6662F + Mth.PI) * 1.4F * limbSwingAmount;
                    parts[0].xRot = a;  // right hind
                    parts[1].xRot = b;  // left hind
                    parts[2].xRot = b;  // right front (diagonal-paired with left hind)
                    parts[3].xRot = a;  // left front  (diagonal-paired with right hind)
                    for (ModelPart p : parts) p.yRot = 0F;
                }
            }
            default -> { /* torso/arms static */ }
        }
    }

    @Override
    public void setAttributes(LivingEntity m, BodyPartLocation loc) {
        switch (loc) {
            case Head  -> addAttributeMods(m, "Head", 0.5, 1, 0, 0, 0);
            case Torso -> addAttributeMods(m, "Torso", 1, 0, 0, 0, 0);
            case Legs  -> addAttributeMods(m, "Legs", 0.25, 0, 3, 3, 0);
            default    -> {}
        }
    }
}
