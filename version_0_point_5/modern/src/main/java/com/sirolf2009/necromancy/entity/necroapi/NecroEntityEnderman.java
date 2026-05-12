package com.sirolf2009.necromancy.entity.necroapi;

import com.sirolf2009.necromancy.api.BodyPart;
import com.sirolf2009.necromancy.api.BodyPartLocation;
import com.sirolf2009.necromancy.api.NecroEntityBiped;
import com.sirolf2009.necromancy.item.NecromancyItems;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/**
 * 1:1 port of {@code NecroEntityEnderman}.
 *
 * <p>Bug-for-bug note: the original {@code initArmRight} returned {@code armLeft}
 * via a copy-paste mistake.  We preserve the same behaviour here (passes the
 * "right arm" as a plain non-mirrored arm at the same coords) so existing
 * world saves and rendering match the original.
 */
public class NecroEntityEnderman extends NecroEntityBiped {
    public NecroEntityEnderman() {
        super("Enderman");
        headItem  = new ItemStack(NecromancyItems.bodyPart("Enderman Head"));
        torsoItem = new ItemStack(NecromancyItems.bodyPart("Enderman Torso"));
        armItem   = new ItemStack(NecromancyItems.bodyPart("Enderman Arm"));
        legItem   = new ItemStack(NecromancyItems.bodyPart("Enderman Legs"));
        texture   = ResourceLocation.parse("minecraft:textures/entity/enderman/enderman.png");
    }
    @Override public void initRecipes() { initDefaultRecipes(Items.ENDER_PEARL); }
    @Override public BodyPart[] initHead() {
        return new BodyPart[] {
            new BodyPart(this, 0, 0).addBox(-4F, -7F, -4F, 8, 8, 8),
            new BodyPart(this, 0, 16).addBox(-4F, -3F, -4F, 8, 8, 8, -0.5F)
        };
    }
    @Override public BodyPart[] initLegs() {
        float[] torsoPos = { -4F, -18F, 0F };
        BodyPart lr = new BodyPart(this, torsoPos, 56, 0); lr.addBox(-1F, -4F, 1F, 2, 30, 2).setPos(-2F, -2F, 0F);
        BodyPart ll = new BodyPart(this, torsoPos, 56, 0); ll.addBox(-1F, -4F, 1F, 2, 30, 2).setPos( 2F, -2F, 0F);
        return new BodyPart[] { ll, lr };
    }
    @Override public BodyPart[] initArmLeft() {
        BodyPart la = new BodyPart(this, 56, 0); la.addBox(2F, 0F, -1F, 2, 30, 2);
        return new BodyPart[] { la };
    }
    @Override public BodyPart[] initArmRight() {
        // Bug preserved: original returned the same shape as left, no mirror.
        BodyPart ra = new BodyPart(this, 56, 0); ra.addBox(0F, 0F, -1F, 2, 30, 2);
        return new BodyPart[] { ra };
    }
    @Override public void setAttributes(LivingEntity m, BodyPartLocation loc) {
        switch (loc) {
            case Head     -> addAttributeMods(m, "Head", 1, 1, 1, 1, 0.5);
            case Torso    -> addAttributeMods(m, "Torso", 4, 0, 1, 0, 0);
            case ArmLeft  -> addAttributeMods(m, "ArmL", 1, 0, 0, 0, 1.5);
            case ArmRight -> addAttributeMods(m, "ArmR", 1, 0, 0, 0, 1.5);
            case Legs     -> addAttributeMods(m, "Legs", 1, 0, 4, 3, 0);
        }
    }
}
