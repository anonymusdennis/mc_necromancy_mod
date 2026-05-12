package com.sirolf2009.necromancy.entity.necroapi;

import com.sirolf2009.necromancy.Reference;
import com.sirolf2009.necromancy.api.BodyPart;
import com.sirolf2009.necromancy.api.BodyPartLocation;
import com.sirolf2009.necromancy.api.ISaddleAble;
import com.sirolf2009.necromancy.api.LocomotionProfile;
import com.sirolf2009.necromancy.api.NecroEntityBase;
import com.sirolf2009.necromancy.api.VoiceProfile;
import com.sirolf2009.necromancy.client.model.BorrowedGeometry;
import com.sirolf2009.necromancy.item.NecromancyItems;
import net.minecraft.client.model.CamelModel;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.List;

public class NecroEntityCamel extends NecroEntityBase implements ISaddleAble {

    private LayerDefinition layer;

    public NecroEntityCamel() {
        super("Camel");
        hasArms = false;
        textureWidth = 128;
        textureHeight = 128;
        texture = ResourceLocation.parse("minecraft:textures/entity/camel/camel.png");
        headItem = new ItemStack(NecromancyItems.bodyPart("Camel Head"));
        torsoItem = new ItemStack(NecromancyItems.bodyPart("Camel Torso"));
        armItem = ItemStack.EMPTY;
        legItem = new ItemStack(NecromancyItems.bodyPart("Camel Legs"));
        initRecipes();
    }

    private LayerDefinition layer() {
        if (layer == null) {
            layer = CamelModel.createBodyLayer();
        }
        return layer;
    }

    @Override
    public void initRecipes() {
        initDefaultRecipes(Items.CACTUS);
    }

    @Override
    public LocomotionProfile locomotion() {
        return LocomotionProfile.walk(0.62F, SoundEvents.CAMEL_STEP);
    }

    @Override
    public VoiceProfile voice() {
        return VoiceProfile.CAMEL;
    }

    @Override
    public ResourceLocation getSaddleTexture() {
        return Reference.TEXTURE_SADDLE_COW;
    }

    @Override
    public BodyPart[] initHead() {
        return BorrowedGeometry.concat(
            BorrowedGeometry.borrow(this, layer(), "body/head"),
            BorrowedGeometry.borrow(this, layer(), "body/head/left_ear"),
            BorrowedGeometry.borrow(this, layer(), "body/head/right_ear"));
    }

    @Override
    public BodyPart[] initTorso() {
        BodyPart[] t = BorrowedGeometry.borrow(this, layer(), "body");
        if (t.length > 0) {
            t[0].headPos = new float[] { 0F, -14F, -22F };
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
            legs[0].torsoPos = new float[] { 0F, 16F, 0F };
        }
        return legs;
    }

    @Override
    public void setAttributes(LivingEntity m, BodyPartLocation loc) {
        switch (loc) {
            case Head -> addAttributeMods(m, "Head", 0.6, 1, 0, 0.1, 0);
            case Torso -> addAttributeMods(m, "Torso", 1.2, 0, 0.2, 0.15, 0);
            case Legs -> addAttributeMods(m, "Legs", 0.45, 0, 1.5, 2.8, 0);
            default -> { }
        }
    }
}
