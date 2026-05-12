package com.sirolf2009.necromancy.entity.necroapi;
import net.minecraft.entity.EntityLivingBase; import net.minecraft.init.Items; import net.minecraft.util.ResourceLocation;
import com.sirolf2009.necroapi.*; import com.sirolf2009.necromancy.item.ItemBodyPart;
public class NecroEntityWolf extends NecroEntityQuadruped {
    public NecroEntityWolf() { super("Wolf",10);
        headItem=ItemBodyPart.getItemStackFromName("Wolf Head",1);
        hasTorso=false; hasArms=false; hasLegs=false;
        texture=new ResourceLocation("textures/entity/wolf/wolf.png"); }
    @Override public void initRecipes() { initDefaultRecipes(Items.BONE); }
    @Override public void setAttributes(EntityLivingBase m, BodyPartLocation l) { if(l==BodyPartLocation.Head) addAttributeMods(m,"H",0D,3D,0D,2D,3D); }
}
