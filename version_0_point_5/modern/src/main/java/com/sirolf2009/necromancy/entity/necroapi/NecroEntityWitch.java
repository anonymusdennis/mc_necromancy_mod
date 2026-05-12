package com.sirolf2009.necromancy.entity.necroapi;

import com.sirolf2009.necromancy.api.BodyPartLocation;
import com.sirolf2009.necromancy.api.NecroEntityBiped;
import com.sirolf2009.necromancy.item.NecromancyItems;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/** 1:1 port of {@code NecroEntityWitch}. */
public class NecroEntityWitch extends NecroEntityVillager {
    public NecroEntityWitch() {
        super("Witch");
        headItem  = new ItemStack(NecromancyItems.bodyPart("Witch Head"));
        torsoItem = new ItemStack(NecromancyItems.bodyPart("Witch Torso"));
        armItem   = new ItemStack(NecromancyItems.bodyPart("Witch Arm"));
        legItem   = new ItemStack(NecromancyItems.bodyPart("Witch Legs"));
        texture   = ResourceLocation.parse("minecraft:textures/entity/witch.png");
        textureHeight = 128;
    }
    @Override public void initRecipes() { initDefaultRecipes(Items.POISONOUS_POTATO); }
    @Override public void setAttributes(LivingEntity m, BodyPartLocation loc) {
        switch (loc) {
            case Head     -> addAttributeMods(m, "Head", 1.5, 1, 0, 0, 0);
            case Torso    -> addAttributeMods(m, "Torso", 2, 0, 0, 0, 0);
            case ArmLeft  -> addAttributeMods(m, "ArmL", 0.5, 0, 0, 0, 0.75);
            case ArmRight -> addAttributeMods(m, "ArmR", 0.5, 0, 0, 0, 0.75);
            case Legs     -> addAttributeMods(m, "Legs", 1.5, 0, 3, 3, 0);
        }
    }
}
