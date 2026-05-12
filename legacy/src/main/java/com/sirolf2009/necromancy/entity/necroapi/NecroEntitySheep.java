package com.sirolf2009.necromancy.entity.necroapi;
import net.minecraft.entity.EntityLivingBase; import net.minecraft.init.Items; import net.minecraft.util.ResourceLocation;
import com.sirolf2009.necroapi.*; import com.sirolf2009.necromancy.item.ItemBodyPart;
public class NecroEntitySheep extends NecroEntityQuadruped {
    public NecroEntitySheep() { super("Sheep",12);
        headItem=ItemBodyPart.getItemStackFromName("Sheep Head",1); torsoItem=ItemBodyPart.getItemStackFromName("Sheep Torso",1); armItem=ItemBodyPart.getItemStackFromName("Sheep Arm",1); legItem=ItemBodyPart.getItemStackFromName("Sheep Legs",1);
        texture=new ResourceLocation("textures/entity/sheep/sheep.png"); }
    @Override public void initRecipes() { initDefaultRecipes(Items.WOOL); }
    @Override public void setAttributes(EntityLivingBase m, BodyPartLocation l) { addAttributeMods(m,"S",1D,0D,1D,0D,0D); }
}
