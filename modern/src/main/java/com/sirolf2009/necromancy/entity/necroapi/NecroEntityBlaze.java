package com.sirolf2009.necromancy.entity.necroapi;

import com.sirolf2009.necromancy.api.BodyPart;
import com.sirolf2009.necromancy.api.BodyPartLocation;
import com.sirolf2009.necromancy.api.LocomotionProfile;
import com.sirolf2009.necromancy.api.NecroEntityBase;
import com.sirolf2009.necromancy.api.VoiceProfile;
import com.sirolf2009.necromancy.api.feature.BlazeTorsoFireGuardFeature;
import com.sirolf2009.necromancy.api.feature.PartFeature;
import com.sirolf2009.necromancy.client.model.BorrowedGeometry;
import com.sirolf2009.necromancy.item.NecromancyItems;
import net.minecraft.client.model.BlazeModel;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.List;

public class NecroEntityBlaze extends NecroEntityBase {

    private LayerDefinition layer;

    public NecroEntityBlaze() {
        super("Blaze");
        hasArms = false;
        hasLegs = false;
        textureWidth = 64;
        textureHeight = 32;
        texture = ResourceLocation.parse("minecraft:textures/entity/blaze.png");
        headItem = new ItemStack(NecromancyItems.bodyPart("Blaze Head"));
        torsoItem = new ItemStack(NecromancyItems.bodyPart("Blaze Torso"));
        armItem = ItemStack.EMPTY;
        legItem = ItemStack.EMPTY;
        initRecipes();
    }

    private LayerDefinition layer() {
        if (layer == null) {
            layer = BlazeModel.createBodyLayer();
        }
        return layer;
    }

    @Override
    public void initRecipes() {
        initDefaultRecipes(Items.BLAZE_ROD);
    }

    @Override
    public LocomotionProfile locomotion() {
        return LocomotionProfile.STATIC;
    }

    @Override
    public VoiceProfile voice() {
        return VoiceProfile.BLAZE;
    }

    @Override
    public List<PartFeature> features(BodyPartLocation location) {
        List<PartFeature> out = new ArrayList<>(super.features(location));
        if (location == BodyPartLocation.Torso) {
            out.add(BlazeTorsoFireGuardFeature.INSTANCE);
        }
        return out;
    }

    @Override
    public BodyPart[] initHead() {
        return BorrowedGeometry.borrow(this, layer(), "head");
    }

    @Override
    public BodyPart[] initTorso() {
        List<String> rods = new ArrayList<>();
        for (int i = 0; i < 12; i++) {
            rods.add("part" + i);
        }
        BodyPart[] rodsParts = BorrowedGeometry.borrowAll(this, layer(), rods);
        if (rodsParts.length > 0) {
            rodsParts[0].headPos = new float[] { 0F, -12F, 0F };
        }
        return rodsParts;
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
        return new BodyPart[0];
    }

    @Override
    public void setAttributes(LivingEntity m, BodyPartLocation loc) {
        switch (loc) {
            case Head -> addAttributeMods(m, "Head", 0.5, 1, 0, 0, 0.35);
            case Torso -> addAttributeMods(m, "Torso", 1.1, 0, 0, 0.1, 0.25);
            default -> { }
        }
    }
}
