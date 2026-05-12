package com.sirolf2009.necromancy.entity.necroapi;

import com.sirolf2009.necromancy.api.BodyPart;
import com.sirolf2009.necromancy.api.BodyPartLocation;
import com.sirolf2009.necromancy.api.LocomotionProfile;
import com.sirolf2009.necromancy.api.NecroEntityQuadruped;
import com.sirolf2009.necromancy.api.VoiceProfile;
import com.sirolf2009.necromancy.item.NecromancyItems;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/**
 * 1:1 port of {@code NecroEntitySheep}.
 *
 * <p>The sheep is special in vanilla: its head is 6x6x8 (longer face) and the
 * body is 8x16x6 -- so we override {@link #initHead} and {@link #initTorso}
 * with those exact dimensions to match the vanilla 64x32 sheep texture.
 */
public class NecroEntitySheep extends NecroEntityQuadruped {
    public NecroEntitySheep() {
        super("Sheep", 12);
        headItem  = new ItemStack(NecromancyItems.bodyPart("Sheep Head"));
        torsoItem = new ItemStack(NecromancyItems.bodyPart("Sheep Torso"));
        armItem   = new ItemStack(NecromancyItems.bodyPart("Sheep Arm"));
        legItem   = new ItemStack(NecromancyItems.bodyPart("Sheep Legs"));
        texture   = ResourceLocation.parse("minecraft:textures/entity/sheep/sheep.png");
    }
    @Override public void initRecipes() { initDefaultRecipes(Items.WHITE_WOOL); }
    @Override public LocomotionProfile locomotion() { return LocomotionProfile.walk(0.7F, SoundEvents.WOOL_STEP); }
    @Override public VoiceProfile voice() { return VoiceProfile.SHEEP; }

    /** Sheep face: 6x6x8 -- longer than tall and longer than wide. */
    @Override
    public BodyPart[] initHead() {
        BodyPart head = new BodyPart(this, 0, 0);
        head.addBox(-4F, -4F, -4F, 6, 6, 8);
        return new BodyPart[] { head };
    }

    /** Sheep body: 8x16x6 (narrower than cow, taller than tall). */
    @Override
    public BodyPart[] initTorso() {
        float[] headPos     = {  4.0F, 12F - size, -14.0F };
        float[] armLeftPos  = { -1.0F, 6.0F, -10.0F };
        float[] armRightPos = {  5.0F, 6.0F, -10.0F };
        BodyPart torso = new BodyPart(this, armLeftPos, armRightPos, headPos, 28, 8);
        torso.addBox(0F, -10F, -6F, 8, 16, 6);
        return new BodyPart[] { torso };
    }

    @Override public void setAttributes(LivingEntity m, BodyPartLocation loc) {
        switch (loc) {
            case Head     -> addAttributeMods(m, "Head", 0.5, 1, 0, 0, 0);
            case Torso    -> addAttributeMods(m, "Torso", 1, 0, 0, 0, 0);
            case ArmLeft  -> addAttributeMods(m, "ArmL", 0.25, 0, 0, 0, 0.25);
            case ArmRight -> addAttributeMods(m, "ArmR", 0.25, 0, 0, 0, 0.25);
            case Legs     -> addAttributeMods(m, "Legs", 0.25, 0, 1, 3, 0);
        }
    }
}
