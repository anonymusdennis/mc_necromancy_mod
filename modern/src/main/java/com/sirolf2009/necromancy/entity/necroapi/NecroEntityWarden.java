package com.sirolf2009.necromancy.entity.necroapi;

import com.sirolf2009.necromancy.api.BodyPart;
import com.sirolf2009.necromancy.api.BodyPartLocation;
import com.sirolf2009.necromancy.api.LocomotionProfile;
import com.sirolf2009.necromancy.api.NecroEntityBase;
import com.sirolf2009.necromancy.api.VoiceProfile;
import com.sirolf2009.necromancy.client.model.BorrowedGeometry;
import com.sirolf2009.necromancy.item.NecromancyItems;
import net.minecraft.client.model.WardenModel;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.List;

public class NecroEntityWarden extends NecroEntityBase {

    private LayerDefinition layer;

    public NecroEntityWarden() {
        super("Warden");
        textureWidth = 128;
        textureHeight = 128;
        texture = ResourceLocation.parse("minecraft:textures/entity/warden/warden.png");
        headItem = new ItemStack(NecromancyItems.bodyPart("Warden Head"));
        torsoItem = new ItemStack(NecromancyItems.bodyPart("Warden Torso"));
        armItem = new ItemStack(NecromancyItems.bodyPart("Warden Arm"));
        legItem = new ItemStack(NecromancyItems.bodyPart("Warden Legs"));
        initRecipes();
    }

    private LayerDefinition layer() {
        if (layer == null) {
            layer = WardenModel.createBodyLayer();
        }
        return layer;
    }

    @Override
    public void initRecipes() {
        initDefaultRecipes(Items.ECHO_SHARD);
    }

    @Override
    public LocomotionProfile locomotion() {
        return LocomotionProfile.walk(0.42F, SoundEvents.WARDEN_STEP);
    }

    @Override
    public VoiceProfile voice() {
        return VoiceProfile.WARDEN;
    }

    @Override
    public BodyPart[] initHead() {
        return BorrowedGeometry.concat(
            BorrowedGeometry.borrow(this, layer(), "bone/body/head"),
            BorrowedGeometry.borrow(this, layer(), "bone/body/head/right_tendril"),
            BorrowedGeometry.borrow(this, layer(), "bone/body/head/left_tendril"));
    }

    @Override
    public BodyPart[] initTorso() {
        BodyPart[] t = BorrowedGeometry.concat(
            BorrowedGeometry.borrow(this, layer(), "bone/body"),
            BorrowedGeometry.borrow(this, layer(), "bone/body/right_ribcage"),
            BorrowedGeometry.borrow(this, layer(), "bone/body/left_ribcage"));
        if (t.length > 0) {
            t[0].headPos = new float[] { 0F, -22F, -4F };
            t[0].armLeftPos = new float[] { 14F, -12F, 2F };
            t[0].armRightPos = new float[] { -14F, -12F, 2F };
        }
        return t;
    }

    @Override
    public BodyPart[] initArmLeft() {
        return BorrowedGeometry.borrow(this, layer(), "bone/body/left_arm");
    }

    @Override
    public BodyPart[] initArmRight() {
        return BorrowedGeometry.borrow(this, layer(), "bone/body/right_arm");
    }

    @Override
    public BodyPart[] initLegs() {
        BodyPart[] legs = BorrowedGeometry.borrowAll(this, layer(), List.of("bone/right_leg", "bone/left_leg"));
        if (legs.length > 0) {
            legs[0].torsoPos = new float[] { 0F, 14F, 0F };
        }
        return legs;
    }

    @Override
    public void setAttributes(LivingEntity m, BodyPartLocation loc) {
        switch (loc) {
            case Head -> addAttributeMods(m, "Head", 1.2, 1, 0.5, 0.2, 0.25);
            case Torso -> addAttributeMods(m, "Torso", 2.5, 0, 1, 0.15, 0);
            case ArmLeft, ArmRight -> addAttributeMods(m, "Arm", 0.75, 0, 0, 0, 0.75);
            case Legs -> addAttributeMods(m, "Legs", 0.85, 0, 2, 2.5, 0);
            default -> { }
        }
    }
}
