package com.sirolf2009.necromancy.entity.necroapi;

import com.sirolf2009.necromancy.api.BodyPart;
import com.sirolf2009.necromancy.api.BodyPartLocation;
import com.sirolf2009.necromancy.api.ISkull;
import com.sirolf2009.necromancy.api.LocomotionProfile;
import com.sirolf2009.necromancy.api.NecroEntityBase;
import com.sirolf2009.necromancy.api.VoiceProfile;
import com.sirolf2009.necromancy.item.NecromancyItems;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/** 1:1 port of {@code NecroEntityIronGolem}. */
public class NecroEntityIronGolem extends NecroEntityBase implements ISkull {
    public NecroEntityIronGolem() {
        super("IronGolem");
        headItem  = new ItemStack(NecromancyItems.bodyPart("IronGolem Head"));
        torsoItem = new ItemStack(NecromancyItems.bodyPart("IronGolem Torso"));
        armItem   = new ItemStack(NecromancyItems.bodyPart("IronGolem Arm"));
        legItem   = new ItemStack(NecromancyItems.bodyPart("IronGolem Legs"));
        texture   = ResourceLocation.parse("minecraft:textures/entity/iron_golem/iron_golem.png");
        textureHeight = 128; textureWidth = 128;
        skullItem = new ItemStack(NecromancyItems.bodyPart("IronGolem Head"));
    }
    @Override public void initRecipes() { initDefaultRecipes(Items.PUMPKIN, Items.IRON_BLOCK, Items.IRON_BLOCK, Items.IRON_BLOCK); }
    @Override public LocomotionProfile locomotion() { return LocomotionProfile.walk(0.55F, SoundEvents.IRON_GOLEM_STEP, 1.0F, 1.0F); }
    @Override public VoiceProfile voice() { return VoiceProfile.IRON_GOLEM; }
    @Override public ResourceLocation getSkullTexture() { return texture; }
    @Override public String getSkullName() { return "Iron Golem"; }
    @Override public BodyPart[] initHead() {
        BodyPart h = new BodyPart(this, 0, 0); h.setPos(0, 0, -2);
        h.cubes.texOffs(0, 0).addBox(-4F, -6F, -5.5F, 8, 10, 8);
        h.cubes.texOffs(24, 0).addBox(-1F, 1F, -7.5F, 2, 4, 2);
        return new BodyPart[] { h };
    }
    @Override public BodyPart[] initTorso() {
        float[] headPos     = {  8F, -7F, 2F };
        float[] armLeftPos  = { -9F,  0F, 0F };
        float[] armRightPos = { 13F,  0F, 0F };
        BodyPart b = new BodyPart(this, armLeftPos, armRightPos, headPos, 0, 0);
        b.setPos(0F, -7F, 0F);
        b.cubes.texOffs(0, 40).addBox(-5F, 4F, -6F, 18, 12, 11);
        b.cubes.texOffs(0, 70).addBox(-0.5F, 16F, -3F, 9, 5, 6,
            new net.minecraft.client.model.geom.builders.CubeDeformation(0.5F));
        return new BodyPart[] { b };
    }
    @Override public BodyPart[] initLegs() {
        float[] torsoPos = { -4F, -4F, 0F };
        BodyPart ll = new BodyPart(this, torsoPos, 0, 22); ll.setPos(-4F, 11F, 0F); ll.cubes.texOffs(37, 0).addBox(-3.5F, -3F, -3F, 6, 16, 5);
        BodyPart rl = new BodyPart(this, torsoPos, 0, 22); rl.setPos( 5F, 11F, 0F); rl.cubes.texOffs(60, 0).addBox(-3.5F, -3F, -3F, 6, 16, 5);
        return new BodyPart[] { ll, rl };
    }
    @Override public BodyPart[] initArmLeft() {
        BodyPart la = new BodyPart(this, 0, 0); la.setPos(0F, -7F, 0F);
        la.cubes.texOffs(60, 58).addBox(0F, 2F, -3F, 4, 30, 6);
        return new BodyPart[] { la };
    }
    @Override public BodyPart[] initArmRight() {
        BodyPart ra = new BodyPart(this, 0, 0); ra.setPos(0F, -7F, 0F);
        ra.cubes.texOffs(60, 21).addBox(0F, 2F, -3F, 4, 30, 6);
        return new BodyPart[] { ra };
    }
    @Override public void setAttributes(LivingEntity m, BodyPartLocation loc) {
        switch (loc) {
            case Head     -> addAttributeMods(m, "Head", 1, 1, 2, 0, 0.5);
            case Torso    -> addAttributeMods(m, "Torso", 5, 0, 2, 0, 0);
            case ArmLeft  -> addAttributeMods(m, "ArmL", 1, 0, 1, 0, 1.5);
            case ArmRight -> addAttributeMods(m, "ArmR", 1, 0, 1, 0, 1.5);
            case Legs     -> addAttributeMods(m, "Legs", 4, 0, 3, 1, 0);
        }
    }
}
