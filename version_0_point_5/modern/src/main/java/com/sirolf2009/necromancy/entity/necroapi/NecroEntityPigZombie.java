package com.sirolf2009.necromancy.entity.necroapi;

import com.sirolf2009.necromancy.api.BodyPartLocation;
import com.sirolf2009.necromancy.api.NecroEntityBiped;
import com.sirolf2009.necromancy.item.NecromancyItems;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/**
 * 1:1 port of {@code NecroEntityPigZombie}.
 *
 * <p>The legacy mob name was "Pig Zombie" (with the space) while the body
 * parts were keyed under "Pigzombie" (no space).  We preserve this bug-for-bug
 * so existing world data and recipe lookups continue to work.
 */
public class NecroEntityPigZombie extends NecroEntityBiped {
    public NecroEntityPigZombie() {
        super("Pig Zombie");                          // legacy name preserved
        headItem  = new ItemStack(NecromancyItems.bodyPart("Pigzombie Head"));
        torsoItem = new ItemStack(NecromancyItems.bodyPart("Pigzombie Torso"));
        armItem   = new ItemStack(NecromancyItems.bodyPart("Pigzombie Arm"));
        legItem   = new ItemStack(NecromancyItems.bodyPart("Pigzombie Legs"));
        texture   = ResourceLocation.parse("minecraft:textures/entity/zombie/zombie_pigman.png");
        textureHeight = 64;
    }
    @Override public void initRecipes() { initDefaultRecipes(Items.COOKED_BEEF); }
    @Override public void setAttributes(LivingEntity m, BodyPartLocation loc) {
        switch (loc) {
            case Head     -> addAttributeMods(m, "Head", 1.5, 1, 0, 0, 0.5);
            case Torso    -> addAttributeMods(m, "Torso", 3, 0, 0, 0, 0);
            case ArmLeft  -> addAttributeMods(m, "ArmL", 0.5, 0, 0, 0, 0.75);
            case ArmRight -> addAttributeMods(m, "ArmR", 0.5, 0, 0, 0, 0.75);
            case Legs     -> addAttributeMods(m, "Legs", 1.5, 0, 3, 3, 0);
        }
    }
}
