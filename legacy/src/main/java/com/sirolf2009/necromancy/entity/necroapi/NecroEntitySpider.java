package com.sirolf2009.necromancy.entity.necroapi;
import net.minecraft.entity.EntityLivingBase; import net.minecraft.init.Items; import net.minecraft.item.ItemStack; import net.minecraft.util.ResourceLocation;
import com.sirolf2009.necroapi.*; import com.sirolf2009.necromancy.item.ItemBodyPart;
public class NecroEntitySpider extends NecroEntityQuadruped {
    public NecroEntitySpider() { super("Spider",8);
        headItem=ItemBodyPart.getItemStackFromName("Spider Head",1); torsoItem=ItemBodyPart.getItemStackFromName("Spider Torso",1); legItem=ItemBodyPart.getItemStackFromName("Spider Legs",1);
        hasArms=false; texture=new ResourceLocation("textures/entity/spider/spider.png"); textureWidth=64; textureHeight=32; }
    @Override public void initRecipes() { initDefaultRecipes(Items.STRING, Items.STRING, Items.STRING, Items.STRING); }
    @Override public void setAttributes(EntityLivingBase m, BodyPartLocation l) {
        if(l==BodyPartLocation.Head) addAttributeMods(m,"H",0D,1D,0D,0D,2D);
        else if(l==BodyPartLocation.Torso) addAttributeMods(m,"T",2D,0D,0D,0D,0D);
        else if(l==BodyPartLocation.Legs) addAttributeMods(m,"L",0D,0D,0D,3D,0D); }
}
