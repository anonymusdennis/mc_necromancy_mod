package com.sirolf2009.necromancy.entity.necroapi;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import com.sirolf2009.necroapi.BodyPartLocation;
import com.sirolf2009.necromancy.item.ItemBodyPart;
import com.sirolf2009.necroapi.NecroEntityBiped;

public class NecroEntityZombie extends NecroEntityBiped
{
    public NecroEntityZombie()
    {
        super("Zombie");
        headItem = new ItemStack(Items.SKULL, 1, 2);
        torsoItem = ItemBodyPart.getItemStackFromName("Zombie Torso", 1);
        armItem = ItemBodyPart.getItemStackFromName("Zombie Arm", 1);
        legItem = ItemBodyPart.getItemStackFromName("Zombie Legs", 1);
        texture = new ResourceLocation("textures/entity/zombie/zombie.png");
        textureHeight = 64;
    }
    @Override public void initRecipes() { initDefaultRecipes(Items.ROTTEN_FLESH); }
    @Override public void setAttributes(EntityLivingBase minion, BodyPartLocation loc) {
        if (loc == BodyPartLocation.Head) addAttributeMods(minion, "H", 1D, 1D, 0D, 0D, 1D);
        else if (loc == BodyPartLocation.Torso) addAttributeMods(minion, "T", 2D, 0D, 0D, 0D, 0D);
        else if (loc == BodyPartLocation.ArmLeft || loc == BodyPartLocation.ArmRight) addAttributeMods(minion, "A", 0.5D, 0D, 0D, 0D, 0.5D);
        else if (loc == BodyPartLocation.Legs) addAttributeMods(minion, "L", 1D, 0D, 3D, 3D, 0D);
    }
}
