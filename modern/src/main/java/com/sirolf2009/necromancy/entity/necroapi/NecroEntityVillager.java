package com.sirolf2009.necromancy.entity.necroapi;

import com.sirolf2009.necromancy.api.BodyPart;
import com.sirolf2009.necromancy.api.BodyPartLocation;
import com.sirolf2009.necromancy.api.NecroEntityBase;
import com.sirolf2009.necromancy.api.VoiceProfile;
import com.sirolf2009.necromancy.item.NecromancyItems;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/** 1:1 port of {@code NecroEntityVillager}. */
public class NecroEntityVillager extends NecroEntityBase {
    public NecroEntityVillager()             { this("Villager"); }
    public NecroEntityVillager(String name) {
        super(name);
        headItem  = new ItemStack(NecromancyItems.bodyPart("Villager Head"));
        torsoItem = new ItemStack(NecromancyItems.bodyPart("Villager Torso"));
        armItem   = new ItemStack(NecromancyItems.bodyPart("Villager Arm"));
        legItem   = new ItemStack(NecromancyItems.bodyPart("Villager Legs"));
        texture   = ResourceLocation.parse("minecraft:textures/entity/villager/villager.png");
        textureHeight = 64;
    }
    @Override public void initRecipes() { initDefaultRecipes(Items.BOOK); }
    @Override public VoiceProfile voice() { return VoiceProfile.VILLAGER; }
    @Override public BodyPart[] initHead() {
        BodyPart head = new BodyPart(this, 0, 0).addBox(-4F, -6F, -4F, 8, 10, 8);
        BodyPart nose = new BodyPart(this, 24, 0); nose.setPos(0F, -2F, 0F).addBox(-1F, 3F, -6F, 2, 4, 2);
        return new BodyPart[] { head, nose };
    }
    @Override public BodyPart[] initTorso() {
        float[] headPos     = { 4.0F, -4.0F, 2.0F };
        float[] armLeftPos  = { -4F,  0F,   0F };
        float[] armRightPos = {  8F,  0F,   0F };
        BodyPart torsoOuter = new BodyPart(this, armLeftPos, armRightPos, headPos, 16, 20).addBox(0F, 0F, -1F, 8, 12, 6);
        BodyPart torsoRobe  = new BodyPart(this, armLeftPos, armRightPos, headPos, 0, 38);
        torsoRobe.cubes.addBox(0F, 0F, -1F, 8, 18, 6,
            new net.minecraft.client.model.geom.builders.CubeDeformation(0.5F));
        return new BodyPart[] { torsoOuter, torsoRobe };
    }
    @Override public BodyPart[] initArmLeft() {
        BodyPart la = new BodyPart(this, 44, 22);
        la.cubes.addBox(0F, -2F, -2F, 4, 8, 4);
        la.cubes.addBox(4F, 2F, -2F, 4, 4, 4);
        la.setPos(0F, 2F, 0F);
        return new BodyPart[] { la };
    }
    @Override public BodyPart[] initArmRight() {
        BodyPart ra = new BodyPart(this, 44, 22);
        ra.setPos(0F, 2F, 0F);
        ra.cubes.addBox(0F, -2F, -2F, 4, 8, 4);
        ra.cubes.addBox(-4F, 2F, -2F, 4, 4, 4);
        return new BodyPart[] { ra };
    }
    @Override public BodyPart[] initLegs() {
        float[] torsoPos = { -4F, 0F, -2F };
        BodyPart rl = new BodyPart(this, torsoPos, 0, 22).addBox(-2F, 0F, -2F, 4, 12, 4); rl.setPos(-2F, 12F, 0F);
        BodyPart ll = new BodyPart(this, torsoPos, 0, 22).addBox(-2F, 0F, -2F, 4, 12, 4); ll.setPos( 2F, 12F, 0F);
        return new BodyPart[] { rl, ll };
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
