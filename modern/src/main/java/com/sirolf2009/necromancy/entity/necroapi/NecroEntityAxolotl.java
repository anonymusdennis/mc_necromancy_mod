package com.sirolf2009.necromancy.entity.necroapi;

import com.sirolf2009.necromancy.api.BodyPart;
import com.sirolf2009.necromancy.api.BodyPartLocation;
import com.sirolf2009.necromancy.api.LocomotionProfile;
import com.sirolf2009.necromancy.api.NecroEntityBase;
import com.sirolf2009.necromancy.api.VoiceProfile;
import com.sirolf2009.necromancy.client.model.BorrowedGeometry;
import com.sirolf2009.necromancy.item.NecromancyItems;
import net.minecraft.client.model.AxolotlModel;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.List;

public class NecroEntityAxolotl extends NecroEntityBase {

    private LayerDefinition layer;

    public NecroEntityAxolotl() {
        super("Axolotl");
        hasArms = false;
        textureWidth = 64;
        textureHeight = 64;
        texture = ResourceLocation.parse("minecraft:textures/entity/axolotl/axolotl.png");
        headItem = new ItemStack(NecromancyItems.bodyPart("Axolotl Head"));
        torsoItem = new ItemStack(NecromancyItems.bodyPart("Axolotl Torso"));
        armItem = ItemStack.EMPTY;
        legItem = new ItemStack(NecromancyItems.bodyPart("Axolotl Legs"));
        initRecipes();
    }

    private LayerDefinition layer() {
        if (layer == null) {
            layer = AxolotlModel.createBodyLayer();
        }
        return layer;
    }

    @Override
    public void initRecipes() {
        initDefaultRecipes(Items.TROPICAL_FISH);
    }

    @Override
    public LocomotionProfile locomotion() {
        return LocomotionProfile.walk(0.52F, SoundEvents.GRASS_STEP);
    }

    @Override
    public VoiceProfile voice() {
        return VoiceProfile.AXOLOTL;
    }

    @Override
    public BodyPart[] initHead() {
        return BorrowedGeometry.concat(
            BorrowedGeometry.borrow(this, layer(), "body/head"),
            BorrowedGeometry.borrow(this, layer(), "body/head/top_gills"),
            BorrowedGeometry.borrow(this, layer(), "body/head/left_gills"),
            BorrowedGeometry.borrow(this, layer(), "body/head/right_gills"));
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
            "body/right_hind_leg", "body/left_hind_leg",
            "body/right_front_leg", "body/left_front_leg"));
        if (legs.length > 0) {
            legs[0].torsoPos = new float[] { 0F, 12F, 0F };
        }
        return BorrowedGeometry.concat(legs, BorrowedGeometry.borrow(this, layer(), "body/tail"));
    }

    @Override
    public void setAttributes(LivingEntity m, BodyPartLocation loc) {
        switch (loc) {
            case Head -> addAttributeMods(m, "Head", 0.5, 1, 0, 0.2, 0);
            case Torso -> addAttributeMods(m, "Torso", 0.8, 0, 0, 0.25, 0);
            case Legs -> addAttributeMods(m, "Legs", 0.35, 0, 1.5, 2.5, 0);
            default -> { }
        }
    }
}
