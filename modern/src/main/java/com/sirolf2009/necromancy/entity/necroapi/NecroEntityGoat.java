package com.sirolf2009.necromancy.entity.necroapi;

import com.sirolf2009.necromancy.api.BodyPart;
import com.sirolf2009.necromancy.api.BodyPartLocation;
import com.sirolf2009.necromancy.api.LocomotionProfile;
import com.sirolf2009.necromancy.api.NecroEntityBase;
import com.sirolf2009.necromancy.api.VoiceProfile;
import com.sirolf2009.necromancy.client.model.BorrowedGeometry;
import com.sirolf2009.necromancy.item.NecromancyItems;
import net.minecraft.client.model.GoatModel;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.List;

public class NecroEntityGoat extends NecroEntityBase {

    private LayerDefinition layer;

    public NecroEntityGoat() {
        super("Goat");
        hasArms = false;
        textureWidth = 64;
        textureHeight = 64;
        texture = ResourceLocation.parse("minecraft:textures/entity/goat/goat.png");
        headItem = new ItemStack(NecromancyItems.bodyPart("Goat Head"));
        torsoItem = new ItemStack(NecromancyItems.bodyPart("Goat Torso"));
        armItem = ItemStack.EMPTY;
        legItem = new ItemStack(NecromancyItems.bodyPart("Goat Legs"));
        initRecipes();
    }

    private LayerDefinition layer() {
        if (layer == null) {
            layer = GoatModel.createBodyLayer();
        }
        return layer;
    }

    @Override
    public void initRecipes() {
        initDefaultRecipes(Items.WHEAT);
    }

    @Override
    public LocomotionProfile locomotion() {
        return LocomotionProfile.hop(0.52F, 24, SoundEvents.GOAT_STEP);
    }

    @Override
    public VoiceProfile voice() {
        return VoiceProfile.GOAT;
    }

    @Override
    public BodyPart[] initHead() {
        return BorrowedGeometry.concat(
            BorrowedGeometry.borrow(this, layer(), "head"),
            BorrowedGeometry.borrow(this, layer(), "head/left_horn"),
            BorrowedGeometry.borrow(this, layer(), "head/right_horn"),
            BorrowedGeometry.borrow(this, layer(), "head/nose"));
    }

    @Override
    public BodyPart[] initTorso() {
        BodyPart[] t = BorrowedGeometry.borrow(this, layer(), "body");
        if (t.length > 0) {
            t[0].headPos = new float[] { -1F, -14F, -10F };
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
            "left_hind_leg", "right_hind_leg", "left_front_leg", "right_front_leg"));
        if (legs.length > 0) {
            legs[0].torsoPos = new float[] { 0F, 12F, 0F };
        }
        return legs;
    }

    @Override
    public void setAttributes(LivingEntity m, BodyPartLocation loc) {
        switch (loc) {
            case Head -> addAttributeMods(m, "Head", 0.45, 1, 0, 0.15, 0);
            case Torso -> addAttributeMods(m, "Torso", 0.85, 0, 0.1, 0.22, 0);
            case Legs -> addAttributeMods(m, "Legs", 0.35, 0, 1.8, 3F, 0);
            default -> { }
        }
    }
}
