package com.sirolf2009.necromancy.entity.necroapi;
import net.minecraft.entity.Entity; import net.minecraft.entity.EntityLivingBase; import net.minecraft.init.Items; import net.minecraft.util.ResourceLocation;
import com.sirolf2009.necroapi.*; import com.sirolf2009.necromancy.entity.EntityMinion; import com.sirolf2009.necromancy.item.ItemBodyPart;
public class NecroEntityIsaac extends NecroEntityBiped {
    public NecroEntityIsaac() { super("Isaac");
        headItem=ItemBodyPart.getItemStackFromName("Isaac Head",1); torsoItem=ItemBodyPart.getItemStackFromName("Isaac Torso",1); armItem=ItemBodyPart.getItemStackFromName("Isaac Arm",1); legItem=ItemBodyPart.getItemStackFromName("Isaac Legs",1);
        texture=new ResourceLocation("necromancy:textures/entity/isaac.png"); textureHeight=64; hasHead=true; hasArms=true; hasLegs=true; }
    @Override public void initRecipes() { initDefaultRecipes(Items.GHAST_TEAR); }
    @Override public void setAttributes(EntityLivingBase m, BodyPartLocation l) {
        if(l==BodyPartLocation.Head) addAttributeMods(m,"H",3D,5D,0D,3D,5D);
        else if(l==BodyPartLocation.Torso) addAttributeMods(m,"T",5D,0D,2D,0D,0D);
        else if(l==BodyPartLocation.ArmLeft||l==BodyPartLocation.ArmRight) addAttributeMods(m,"A",0D,0D,0D,0D,3D);
        else if(l==BodyPartLocation.Legs) addAttributeMods(m,"L",2D,0D,0D,5D,0D); }
}
