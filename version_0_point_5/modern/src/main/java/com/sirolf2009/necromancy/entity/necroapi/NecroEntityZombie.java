package com.sirolf2009.necromancy.entity.necroapi;

import com.sirolf2009.necromancy.api.BodyPartLocation;
import com.sirolf2009.necromancy.api.NecroEntityBiped;
import com.sirolf2009.necromancy.item.NecromancyItems;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/** 1:1 port of {@code NecroEntityZombie}. */
public class NecroEntityZombie extends NecroEntityBiped {
    public NecroEntityZombie() {
        super("Zombie");
        headItem  = new ItemStack(Items.ZOMBIE_HEAD);
        torsoItem = new ItemStack(NecromancyItems.bodyPart("Zombie Torso"));
        armItem   = new ItemStack(NecromancyItems.bodyPart("Zombie Arm"));
        legItem   = new ItemStack(NecromancyItems.bodyPart("Zombie Legs"));
        texture   = ResourceLocation.parse("minecraft:textures/entity/zombie/zombie.png");
        textureHeight = 64;
        hasHead = true;
    }
    @Override public void initRecipes() { initDefaultRecipes(Items.ROTTEN_FLESH); }
    @Override public void setAttributes(LivingEntity m, BodyPartLocation loc) {
        switch (loc) {
            case Head     -> addAttributeMods(m, "Head", 1, 1, 0, 0, 1);
            case Torso    -> addAttributeMods(m, "Torso", 2, 0, 0, 0, 0);
            case ArmLeft  -> addAttributeMods(m, "ArmL", 0.5, 0, 0, 0, 0.5);
            case ArmRight -> addAttributeMods(m, "ArmR", 0.5, 0, 0, 0, 0.5);
            case Legs     -> addAttributeMods(m, "Legs", 1, 0, 3, 3, 0);
        }
    }
}
