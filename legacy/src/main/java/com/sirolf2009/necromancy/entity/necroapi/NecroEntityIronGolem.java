package com.sirolf2009.necromancy.entity.necroapi;
import net.minecraft.entity.EntityLivingBase; import net.minecraft.init.Items; import net.minecraft.util.ResourceLocation;
import com.sirolf2009.necroapi.*; import com.sirolf2009.necromancy.item.ItemBodyPart;
public class NecroEntityIronGolem extends NecroEntityBiped {
    public NecroEntityIronGolem() { super("IronGolem");
        headItem=ItemBodyPart.getItemStackFromName("IronGolem Head",1); torsoItem=ItemBodyPart.getItemStackFromName("IronGolem Torso",1); armItem=ItemBodyPart.getItemStackFromName("IronGolem Arm",1); legItem=ItemBodyPart.getItemStackFromName("IronGolem Legs",1);
        texture=new ResourceLocation("textures/entity/iron_golem.png"); textureWidth=128; textureHeight=128; }
    @Override public void initRecipes() { initDefaultRecipes(Items.IRON_INGOT); }
    @Override public void setAttributes(EntityLivingBase m, BodyPartLocation l) {
        if(l==BodyPartLocation.Head) addAttributeMods(m,"H",5D,0D,5D,0D,0D);
        else if(l==BodyPartLocation.Torso) addAttributeMods(m,"T",10D,0D,10D,0D,0D);
        else if(l==BodyPartLocation.ArmLeft||l==BodyPartLocation.ArmRight) addAttributeMods(m,"A",0D,0D,0D,0D,5D);
        else if(l==BodyPartLocation.Legs) addAttributeMods(m,"L",5D,0D,5D,0D,0D); }
}
