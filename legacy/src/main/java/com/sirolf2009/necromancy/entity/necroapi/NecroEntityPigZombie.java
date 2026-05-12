package com.sirolf2009.necromancy.entity.necroapi;
import net.minecraft.entity.EntityLivingBase; import net.minecraft.init.Items; import net.minecraft.item.ItemStack; import net.minecraft.util.ResourceLocation;
import com.sirolf2009.necroapi.*; import com.sirolf2009.necromancy.item.ItemBodyPart;
public class NecroEntityPigZombie extends NecroEntityBiped {
    public NecroEntityPigZombie() { super("PigZombie");
        headItem=ItemBodyPart.getItemStackFromName("Pigzombie Head",1); torsoItem=ItemBodyPart.getItemStackFromName("Pigzombie Torso",1); armItem=ItemBodyPart.getItemStackFromName("Pigzombie Arm",1); legItem=ItemBodyPart.getItemStackFromName("Pigzombie Legs",1);
        texture=new ResourceLocation("textures/entity/zombie_pigman.png"); textureHeight=64; }
    @Override public void initRecipes() { initDefaultRecipes(Items.GOLD_NUGGET); }
    @Override public void setAttributes(EntityLivingBase m, BodyPartLocation l) {
        if(l==BodyPartLocation.Head) addAttributeMods(m,"H",1D,2D,0D,0D,2D);
        else if(l==BodyPartLocation.Torso) addAttributeMods(m,"T",2D,0D,1D,0D,0D);
        else if(l==BodyPartLocation.ArmLeft||l==BodyPartLocation.ArmRight) addAttributeMods(m,"A",0D,0D,0D,0D,1.5D);
        else if(l==BodyPartLocation.Legs) addAttributeMods(m,"L",1D,0D,0D,1D,0D); }
}
