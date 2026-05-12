package com.sirolf2009.necromancy.entity.necroapi;

import com.sirolf2009.necromancy.api.BodyPart;
import com.sirolf2009.necromancy.api.BodyPartLocation;
import com.sirolf2009.necromancy.api.NecroEntityBase;
import com.sirolf2009.necromancy.item.NecromancyItems;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/** 1:1 port of {@code NecroEntityChicken}. */
public class NecroEntityChicken extends NecroEntityBase {
    public NecroEntityChicken() {
        super("Chicken");
        headItem  = new ItemStack(NecromancyItems.bodyPart("Chicken Head"));
        torsoItem = new ItemStack(NecromancyItems.bodyPart("Chicken Torso"));
        armItem   = new ItemStack(NecromancyItems.bodyPart("Chicken Arm"));
        legItem   = new ItemStack(NecromancyItems.bodyPart("Chicken Legs"));
        texture   = ResourceLocation.parse("minecraft:textures/entity/chicken/temperate_chicken.png");
    }
    @Override public void initRecipes() { initDefaultRecipes(Items.CHICKEN); }
    @Override public BodyPart[] initHead() {
        return new BodyPart[] {
            new BodyPart(this, 0, 0).addBox(-2F, -2F, -2F, 4, 6, 3),
            new BodyPart(this, 14, 0).addBox(-2F, 0F, -4F, 4, 2, 2),
            new BodyPart(this, 14, 4).addBox(-1F, 2F, -3F, 2, 2, 2)
        };
    }
    @Override public BodyPart[] initTorso() {
        float[] headPos     = { 4.0F, 4.0F, -2.0F };
        float[] armLeftPos  = { -3F,  6.0F,  2.0F };
        float[] armRightPos = {  7F,  6.0F,  2.0F };
        BodyPart torso = new BodyPart(this, armLeftPos, armRightPos, headPos, 0, 9);
        torso.addBox(1F, -2F, -12F, 6, 8, 6);
        return new BodyPart[] { torso };
    }
    @Override public BodyPart[] initLegs() {
        float[] torsoPos = { -3F, 8F, 0F };
        BodyPart rl = new BodyPart(this, torsoPos, 26, 0); rl.addBox(-1.5F, -1F, -1F, 3, 5, 3).setPos(0F, 19F, 0F);
        BodyPart ll = new BodyPart(this, torsoPos, 26, 0); ll.addBox( 0.5F, -1F, -1F, 3, 5, 3).setPos(0F, 19F, 0F);
        return new BodyPart[] { ll, rl };
    }
    @Override public BodyPart[] initArmLeft() {
        BodyPart lw = new BodyPart(this, 24, 13); lw.addBox(3F, 0F, -3F, 1, 4, 6);
        return new BodyPart[] { lw };
    }
    @Override public BodyPart[] initArmRight() {
        BodyPart rw = new BodyPart(this, 24, 13); rw.addBox(0F, 0F, -3F, 1, 4, 6);
        return new BodyPart[] { rw };
    }
    @Override public void setAttributes(LivingEntity m, BodyPartLocation loc) {
        switch (loc) {
            case Head     -> addAttributeMods(m, "Head", 0.5, 1, 0, 0, 0.25);
            case Torso    -> addAttributeMods(m, "Torso", 0.25, 0, 0, 0, 0);
            case ArmLeft  -> addAttributeMods(m, "ArmL", 0.1, 0, 0, 0, 0);
            case ArmRight -> addAttributeMods(m, "ArmR", 0.1, 0, 0, 0, 0);
            case Legs     -> addAttributeMods(m, "Legs", 0.1, 0, 0, 0.5, 0);
        }
    }
}
