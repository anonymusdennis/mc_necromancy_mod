package com.sirolf2009.necromancy.entity.necroapi;

import com.sirolf2009.necromancy.api.BodyPart;
import com.sirolf2009.necromancy.api.BodyPartLocation;
import com.sirolf2009.necromancy.api.LocomotionProfile;
import com.sirolf2009.necromancy.api.NecroEntityBase;
import com.sirolf2009.necromancy.api.VoiceProfile;
import com.sirolf2009.necromancy.client.model.BorrowedGeometry;
import com.sirolf2009.necromancy.item.NecromancyItems;
import net.minecraft.client.model.RabbitModel;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.List;

public class NecroEntityRabbit extends NecroEntityBase {

    private LayerDefinition layer;

    public NecroEntityRabbit() {
        super("Rabbit");
        hasArms = false;
        textureWidth = 64;
        textureHeight = 32;
        texture = ResourceLocation.parse("minecraft:textures/entity/rabbit/brown.png");
        headItem = new ItemStack(NecromancyItems.bodyPart("Rabbit Head"));
        torsoItem = new ItemStack(NecromancyItems.bodyPart("Rabbit Torso"));
        armItem = ItemStack.EMPTY;
        legItem = new ItemStack(NecromancyItems.bodyPart("Rabbit Legs"));
        initRecipes();
    }

    private LayerDefinition layer() {
        if (layer == null) {
            layer = RabbitModel.createBodyLayer();
        }
        return layer;
    }

    @Override
    public void initRecipes() {
        initDefaultRecipes(Items.RABBIT_FOOT);
    }

    @Override
    public LocomotionProfile locomotion() {
        return LocomotionProfile.hop(0.42F, 22, SoundEvents.RABBIT_JUMP);
    }

    @Override
    public VoiceProfile voice() {
        return VoiceProfile.RABBIT;
    }

    @Override
    public BodyPart[] initHead() {
        return BorrowedGeometry.concat(
            BorrowedGeometry.borrow(this, layer(), "head"),
            BorrowedGeometry.borrow(this, layer(), "left_ear"),
            BorrowedGeometry.borrow(this, layer(), "right_ear"),
            BorrowedGeometry.borrow(this, layer(), "nose"));
    }

    @Override
    public BodyPart[] initTorso() {
        BodyPart[] t = BorrowedGeometry.borrow(this, layer(), "body");
        if (t.length > 0) {
            t[0].headPos = new float[] { 0F, -8F, -14F };
        }
        return t;
    }

    @Override
    public BodyPart[] initArmLeft() {
        return new BodyPart[0];
    }

    @Override
    public BodyPart[] initArmRight() {
        return new BodyPart[0];
    }

    @Override
    public BodyPart[] initLegs() {
        BodyPart[] legs = BorrowedGeometry.borrowAll(this, layer(), List.of(
            "left_haunch", "right_haunch",
            "left_front_leg", "right_front_leg",
            "left_hind_foot", "right_hind_foot"));
        if (legs.length > 0) {
            legs[0].torsoPos = new float[] { 0F, 14F, 0F };
        }
        return legs;
    }

    @Override
    public void setAttributes(LivingEntity m, BodyPartLocation loc) {
        switch (loc) {
            case Head -> addAttributeMods(m, "Head", 0.35, 1, 0, 0.2, 0);
            case Torso -> addAttributeMods(m, "Torso", 0.55, 0, 0, 0.35, 0);
            case Legs -> addAttributeMods(m, "Legs", 0.3, 0, 2.5, 3.5, 0);
            default -> { }
        }
    }
}
