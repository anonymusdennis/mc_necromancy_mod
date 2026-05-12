package com.sirolf2009.necromancy.entity.necroapi;
import net.minecraft.entity.EntityLivingBase; import net.minecraft.init.Items; import net.minecraft.item.ItemStack; import net.minecraft.util.ResourceLocation;
import com.sirolf2009.necroapi.*; import com.sirolf2009.necromancy.item.ItemBodyPart;
public class NecroEntityCaveSpider extends NecroEntityQuadruped {
    public NecroEntityCaveSpider() { super("CaveSpider",6);
        headItem=ItemBodyPart.getItemStackFromName("CaveSpider Head",1); torsoItem=ItemBodyPart.getItemStackFromName("CaveSpider Torso",1); legItem=ItemBodyPart.getItemStackFromName("CaveSpider Legs",1);
        hasArms=false; texture=new ResourceLocation("textures/entity/spider/cave_spider.png"); textureWidth=64; textureHeight=32; }
    @Override public void initRecipes() { initDefaultRecipes(Items.FERMENTED_SPIDER_EYE, Items.FERMENTED_SPIDER_EYE, Items.FERMENTED_SPIDER_EYE, Items.FERMENTED_SPIDER_EYE); }
    @Override public void setAttributes(EntityLivingBase m, BodyPartLocation l) {
        if(l==BodyPartLocation.Head) addAttributeMods(m,"H",0D,2D,0D,0D,3D);
        else if(l==BodyPartLocation.Torso) addAttributeMods(m,"T",1D,0D,0D,0D,0D);
        else if(l==BodyPartLocation.Legs) addAttributeMods(m,"L",0D,0D,0D,4D,0D); }
}
