package com.sirolf2009.necromancy.entity.necroapi;
import net.minecraft.entity.EntityLivingBase; import net.minecraft.init.Items; import net.minecraft.item.ItemStack; import net.minecraft.util.ResourceLocation;
import com.sirolf2009.necroapi.*; import com.sirolf2009.necromancy.item.ItemBodyPart;
public class NecroEntitySkeleton extends NecroEntityBiped {
    public NecroEntitySkeleton() { super("Skeleton");
        torsoItem = ItemBodyPart.getItemStackFromName("Skeleton Torso",1); armItem = ItemBodyPart.getItemStackFromName("Skeleton Arm",1); legItem = ItemBodyPart.getItemStackFromName("Skeleton Legs",1);
        hasHead=false; texture=new ResourceLocation("textures/entity/skeleton/skeleton.png"); textureHeight=64; }
    @Override public void initRecipes() { initDefaultRecipes(Items.BONE, Items.BONE, Items.BONE, Items.BONE); }
    @Override public void setAttributes(EntityLivingBase m, BodyPartLocation l) {
        if(l==BodyPartLocation.Torso) addAttributeMods(m,"T",1D,1D,0D,0D,0D);
        else if(l==BodyPartLocation.ArmLeft||l==BodyPartLocation.ArmRight) addAttributeMods(m,"A",0D,0D,0D,0D,1D);
        else if(l==BodyPartLocation.Legs) addAttributeMods(m,"L",0D,0D,0D,1D,0D); }
}
