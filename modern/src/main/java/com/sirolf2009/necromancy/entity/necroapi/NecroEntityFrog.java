package com.sirolf2009.necromancy.entity.necroapi;

import com.sirolf2009.necromancy.api.BodyPart;
import com.sirolf2009.necromancy.api.BodyPartLocation;
import com.sirolf2009.necromancy.api.LocomotionProfile;
import com.sirolf2009.necromancy.api.NecroEntityBase;
import com.sirolf2009.necromancy.api.VoiceProfile;
import com.sirolf2009.necromancy.client.model.BorrowedGeometry;
import com.sirolf2009.necromancy.item.NecromancyItems;
import net.minecraft.client.model.FrogModel;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class NecroEntityFrog extends NecroEntityBase {

    private LayerDefinition layer;

    public NecroEntityFrog() {
        super("Frog");
        textureWidth = 48;
        textureHeight = 48;
        texture = ResourceLocation.parse("minecraft:textures/entity/frog/frog.png");
        headItem = new ItemStack(NecromancyItems.bodyPart("Frog Head"));
        torsoItem = new ItemStack(NecromancyItems.bodyPart("Frog Torso"));
        armItem = new ItemStack(NecromancyItems.bodyPart("Frog Arm"));
        legItem = new ItemStack(NecromancyItems.bodyPart("Frog Legs"));
        initRecipes();
    }

    private LayerDefinition layer() {
        if (layer == null) {
            layer = FrogModel.createBodyLayer();
        }
        return layer;
    }

    @Override
    public void initRecipes() {
        initDefaultRecipes(Items.SLIME_BALL);
    }

    @Override
    public LocomotionProfile locomotion() {
        return LocomotionProfile.hop(0.38F, 28, SoundEvents.FROG_STEP);
    }

    @Override
    public VoiceProfile voice() {
        return VoiceProfile.FROG;
    }

    @Override
    public BodyPart[] initHead() {
        return BorrowedGeometry.concat(
            BorrowedGeometry.borrow(this, layer(), "root/body/head"),
            BorrowedGeometry.borrow(this, layer(), "root/body/head/eyes/right_eye"),
            BorrowedGeometry.borrow(this, layer(), "root/body/head/eyes/left_eye"));
    }

    @Override
    public BodyPart[] initTorso() {
        BodyPart[] t = BorrowedGeometry.borrow(this, layer(), "root/body");
        if (t.length > 0) {
            t[0].headPos = new float[] { 0F, -6F, -10F };
            t[0].armLeftPos = new float[] { 5F, -2F, -8F };
            t[0].armRightPos = new float[] { -5F, -2F, -8F };
        }
        return t;
    }

    @Override
    public BodyPart[] initArmLeft() {
        return BorrowedGeometry.borrow(this, layer(), "root/body/left_arm");
    }

    @Override
    public BodyPart[] initArmRight() {
        return BorrowedGeometry.borrow(this, layer(), "root/body/right_arm");
    }

    @Override
    public BodyPart[] initLegs() {
        BodyPart[] legs = BorrowedGeometry.concat(
            BorrowedGeometry.borrow(this, layer(), "root/left_leg"),
            BorrowedGeometry.borrow(this, layer(), "root/left_leg/left_foot"),
            BorrowedGeometry.borrow(this, layer(), "root/right_leg"),
            BorrowedGeometry.borrow(this, layer(), "root/right_leg/right_foot"));
        if (legs.length > 0) {
            legs[0].torsoPos = new float[] { 0F, 10F, 0F };
        }
        return legs;
    }

    @Override
    public void setAttributes(LivingEntity m, BodyPartLocation loc) {
        switch (loc) {
            case Head -> addAttributeMods(m, "Head", 0.4, 1, 0, 0.15, 0);
            case Torso -> addAttributeMods(m, "Torso", 0.7, 0, 0, 0.25, 0);
            case ArmLeft, ArmRight -> addAttributeMods(m, "Arm", 0.25, 0, 0, 0, 0.2);
            case Legs -> addAttributeMods(m, "Legs", 0.35, 0, 1.8, 3F, 0);
            default -> { }
        }
    }
}
