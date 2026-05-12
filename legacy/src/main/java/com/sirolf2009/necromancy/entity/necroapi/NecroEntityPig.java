package com.sirolf2009.necromancy.entity.necroapi;
import net.minecraft.entity.EntityLivingBase; import net.minecraft.init.Items; import net.minecraft.util.ResourceLocation;
import com.sirolf2009.necroapi.*; import com.sirolf2009.necroapi.ISaddleAble; import com.sirolf2009.necromancy.item.ItemBodyPart;
public class NecroEntityPig extends NecroEntityQuadruped implements ISaddleAble {
    public NecroEntityPig() { super("Pig",12);
        headItem=ItemBodyPart.getItemStackFromName("Pig Head",1); torsoItem=ItemBodyPart.getItemStackFromName("Pig Torso",1); armItem=ItemBodyPart.getItemStackFromName("Pig Arm",1); legItem=ItemBodyPart.getItemStackFromName("Pig Legs",1);
        texture=new ResourceLocation("textures/entity/pig/pig.png"); }
    @Override public void initRecipes() { initDefaultRecipes(Items.PORKCHOP); }
    @Override public void setAttributes(EntityLivingBase m, BodyPartLocation l) { addAttributeMods(m,"P",0.5D,0D,0D,0.5D,0D); }
    @Override public float riderHeight() { return 0.8F; }
}
