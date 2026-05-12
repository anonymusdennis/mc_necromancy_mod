package com.sirolf2009.necromancy.entity.necroapi;

import com.sirolf2009.necromancy.api.BodyPart;
import com.sirolf2009.necromancy.api.BodyPartLocation;
import com.sirolf2009.necromancy.api.LocomotionProfile;
import com.sirolf2009.necromancy.api.NecroEntityBase;
import com.sirolf2009.necromancy.api.VoiceProfile;
import com.sirolf2009.necromancy.client.model.BorrowedGeometry;
import com.sirolf2009.necromancy.item.NecromancyItems;
import net.minecraft.client.model.AllayModel;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/**
 * Post-1.7 mob — geometry borrowed from vanilla {@link AllayModel}.  Wing cubes live on the torso part.
 */
public class NecroEntityAllay extends NecroEntityBase {

    private LayerDefinition layer;

    public NecroEntityAllay() {
        super("Allay");
        hasLegs = false;
        textureWidth = 32;
        textureHeight = 32;
        texture = ResourceLocation.parse("minecraft:textures/entity/allay/allay.png");
        headItem = new ItemStack(NecromancyItems.bodyPart("Allay Head"));
        torsoItem = new ItemStack(NecromancyItems.bodyPart("Allay Torso"));
        armItem = new ItemStack(NecromancyItems.bodyPart("Allay Arm"));
        legItem = ItemStack.EMPTY;
        initRecipes();
    }

    private LayerDefinition layer() {
        if (layer == null) {
            layer = AllayModel.createBodyLayer();
        }
        return layer;
    }

    @Override
    public void initRecipes() {
        initDefaultRecipes(Items.AMETHYST_SHARD);
    }

    @Override
    public LocomotionProfile locomotion() {
        return LocomotionProfile.walk(0.55F, SoundEvents.GRASS_STEP);
    }

    @Override
    public VoiceProfile voice() {
        return VoiceProfile.ALLAY;
    }

    @Override
    public BodyPart[] initHead() {
        return BorrowedGeometry.borrow(this, layer(), "root/head");
    }

    @Override
    public BodyPart[] initTorso() {
        BodyPart[] t = BorrowedGeometry.borrow(this, layer(), "root/body");
        if (t.length > 0) {
            t[0].headPos = new float[] { 0F, -10F, 0F };
            t[0].armLeftPos = new float[] { -3F, 4F, 0F };
            t[0].armRightPos = new float[] { 3F, 4F, 0F };
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
        return new BodyPart[0];
    }

    @Override
    public void setAttributes(LivingEntity m, BodyPartLocation loc) {
        switch (loc) {
            case Head -> addAttributeMods(m, "Head", 0.35, 1, 0, 0.15, 0);
            case Torso -> addAttributeMods(m, "Torso", 0.6, 0, 0, 0.25, 0);
            case ArmLeft, ArmRight -> addAttributeMods(m, "Arm", 0.2, 0, 0, 0, 0.15);
            default -> { }
        }
    }
}
