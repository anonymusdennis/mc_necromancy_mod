package com.sirolf2009.necromancy.entity.necroapi;

import com.sirolf2009.necromancy.api.BodyPart;
import com.sirolf2009.necromancy.api.BodyPartLocation;
import com.sirolf2009.necromancy.api.ISaddleAble;
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
 * 1:1 port of {@code NecroEntityPig}.
 *
 * <p>The pig's body uses the generic quadruped torso (10x16x8) but its head
 * needs a 4x3x1 snout cube on top of the standard 8x8x8 face -- so we
 * override {@link #initHead} to add the snout from texOffs (16,16).
 */
public class NecroEntityPig extends NecroEntityQuadruped implements ISaddleAble {
    public NecroEntityPig() {
        super("Pig", 6);
        headItem  = new ItemStack(NecromancyItems.bodyPart("Pig Head"));
        torsoItem = new ItemStack(NecromancyItems.bodyPart("Pig Torso"));
        armItem   = new ItemStack(NecromancyItems.bodyPart("Pig Arm"));
        legItem   = new ItemStack(NecromancyItems.bodyPart("Pig Legs"));
        texture   = ResourceLocation.parse("minecraft:textures/entity/pig/pig.png");
    }
    @Override public void initRecipes() { initDefaultRecipes(Items.PORKCHOP); }
    @Override public ResourceLocation getSaddleTexture() { return ResourceLocation.parse("minecraft:textures/entity/pig/pig_saddle.png"); }
    @Override public LocomotionProfile locomotion() { return LocomotionProfile.walk(0.85F, SoundEvents.PIG_STEP); }
    @Override public VoiceProfile voice() { return VoiceProfile.PIG; }

    /** Pig head: 8x8x8 face with a 4x3x1 snout poking out the front. */
    @Override
    public BodyPart[] initHead() {
        BodyPart head = new BodyPart(this, 0, 0);
        head.addBox(-4F, -4F, -4F, 8, 8, 8);
        BodyPart snout = new BodyPart(this, 16, 16);
        snout.addBox(-2F, 0F, -5F, 4, 3, 1);
        return new BodyPart[] { head, snout };
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
