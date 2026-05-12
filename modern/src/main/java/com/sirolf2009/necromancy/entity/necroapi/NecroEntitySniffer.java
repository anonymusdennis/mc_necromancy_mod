package com.sirolf2009.necromancy.entity.necroapi;

import com.sirolf2009.necromancy.api.BodyPart;
import com.sirolf2009.necromancy.api.BodyPartLocation;
import com.sirolf2009.necromancy.api.LocomotionProfile;
import com.sirolf2009.necromancy.api.NecroEntityBase;
import com.sirolf2009.necromancy.api.VoiceProfile;
import com.sirolf2009.necromancy.client.model.BorrowedGeometry;
import com.sirolf2009.necromancy.item.NecromancyItems;
import net.minecraft.client.model.SnifferModel;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.List;

public class NecroEntitySniffer extends NecroEntityBase {

    private LayerDefinition layer;

    public NecroEntitySniffer() {
        super("Sniffer");
        hasArms = false;
        textureWidth = 192;
        textureHeight = 192;
        texture = ResourceLocation.parse("minecraft:textures/entity/sniffer/sniffer.png");
        headItem = new ItemStack(NecromancyItems.bodyPart("Sniffer Head"));
        torsoItem = new ItemStack(NecromancyItems.bodyPart("Sniffer Torso"));
        armItem = ItemStack.EMPTY;
        legItem = new ItemStack(NecromancyItems.bodyPart("Sniffer Legs"));
        initRecipes();
    }

    private LayerDefinition layer() {
        if (layer == null) {
            layer = SnifferModel.createBodyLayer();
        }
        return layer;
    }

    @Override
    public void initRecipes() {
        initDefaultRecipes(Items.TORCHFLOWER_SEEDS);
    }

    @Override
    public LocomotionProfile locomotion() {
        return LocomotionProfile.walk(0.32F, SoundEvents.SNIFFER_STEP);
    }

    @Override
    public VoiceProfile voice() {
        return VoiceProfile.SNIFFER;
    }

    @Override
    public BodyPart[] initHead() {
        return BorrowedGeometry.concat(
            BorrowedGeometry.borrow(this, layer(), "root/bone/body/head"),
            BorrowedGeometry.borrow(this, layer(), "root/bone/body/head/left_ear"),
            BorrowedGeometry.borrow(this, layer(), "root/bone/body/head/right_ear"),
            BorrowedGeometry.borrow(this, layer(), "root/bone/body/head/nose"),
            BorrowedGeometry.borrow(this, layer(), "root/bone/body/head/lower_beak"));
    }

    @Override
    public BodyPart[] initTorso() {
        BodyPart[] t = BorrowedGeometry.borrow(this, layer(), "root/bone/body");
        if (t.length > 0) {
            t[0].headPos = new float[] { 0F, -8F, -28F };
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
            "root/bone/right_front_leg", "root/bone/right_mid_leg", "root/bone/right_hind_leg",
            "root/bone/left_front_leg", "root/bone/left_mid_leg", "root/bone/left_hind_leg"));
        if (legs.length > 0) {
            legs[0].torsoPos = new float[] { 0F, 14F, 0F };
        }
        return legs;
    }

    @Override
    public void setAttributes(LivingEntity m, BodyPartLocation loc) {
        switch (loc) {
            case Head -> addAttributeMods(m, "Head", 0.8, 1, 0, 0.1, 0);
            case Torso -> addAttributeMods(m, "Torso", 1.4, 0, 0.3, 0.12, 0);
            case Legs -> addAttributeMods(m, "Legs", 0.5, 0, 1.2, 2.4, 0);
            default -> { }
        }
    }
}
