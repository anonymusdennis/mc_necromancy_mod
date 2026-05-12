package com.sirolf2009.necromancy.entity.necroapi;
import net.minecraft.entity.EntityLivingBase; import net.minecraft.init.Items; import net.minecraft.item.ItemStack; import net.minecraft.util.ResourceLocation;
import com.sirolf2009.necroapi.*; import com.sirolf2009.necromancy.item.ItemBodyPart;
public class NecroEntityEnderman extends NecroEntityBiped {
    public NecroEntityEnderman() { super("Enderman");
        headItem=ItemBodyPart.getItemStackFromName("Enderman Head",1); torsoItem=ItemBodyPart.getItemStackFromName("Enderman Torso",1); armItem=ItemBodyPart.getItemStackFromName("Enderman Arm",1); legItem=ItemBodyPart.getItemStackFromName("Enderman Legs",1);
        texture=new ResourceLocation("textures/entity/enderman/enderman.png"); textureHeight=64; }
    @Override public void initRecipes() { initDefaultRecipes(new ItemStack(Items.DYE,1,0)); }
    @Override public void setAttributes(EntityLivingBase m, BodyPartLocation l) {
        if(l==BodyPartLocation.Head) addAttributeMods(m,"H",2D,5D,0D,2D,0D);
        else if(l==BodyPartLocation.Torso) addAttributeMods(m,"T",3D,0D,0D,0D,0D);
        else if(l==BodyPartLocation.ArmLeft||l==BodyPartLocation.ArmRight) addAttributeMods(m,"A",0D,0D,0D,0D,2D);
        else if(l==BodyPartLocation.Legs) addAttributeMods(m,"L",0D,0D,0D,5D,0D); }
}
