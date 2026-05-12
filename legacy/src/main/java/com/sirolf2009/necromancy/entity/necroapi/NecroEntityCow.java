package com.sirolf2009.necromancy.entity.necroapi;
import net.minecraft.entity.EntityLivingBase; import net.minecraft.init.Items; import net.minecraft.util.ResourceLocation;
import com.sirolf2009.necroapi.*; import com.sirolf2009.necroapi.ISaddleAble; import com.sirolf2009.necromancy.item.ItemBodyPart;
public class NecroEntityCow extends NecroEntityQuadruped implements ISaddleAble {
    public NecroEntityCow() { super("Cow",14);
        headItem=ItemBodyPart.getItemStackFromName("Cow Head",1); torsoItem=ItemBodyPart.getItemStackFromName("Cow Torso",1); armItem=ItemBodyPart.getItemStackFromName("Cow Arm",1); legItem=ItemBodyPart.getItemStackFromName("Cow Legs",1);
        texture=new ResourceLocation("textures/entity/cow/cow.png"); }
    @Override public void initRecipes() { initDefaultRecipes(Items.BEEF); }
    @Override public void setAttributes(EntityLivingBase m, BodyPartLocation l) { addAttributeMods(m,"C",1D,0D,1D,0D,0D); }
    @Override public float riderHeight() { return 1.0F; }
}
