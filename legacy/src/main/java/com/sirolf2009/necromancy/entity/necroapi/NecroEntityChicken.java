package com.sirolf2009.necromancy.entity.necroapi;
import net.minecraft.entity.EntityLivingBase; import net.minecraft.init.Items; import net.minecraft.util.ResourceLocation;
import com.sirolf2009.necroapi.*; import com.sirolf2009.necromancy.item.ItemBodyPart;
public class NecroEntityChicken extends NecroEntityBiped {
    public NecroEntityChicken() { super("Chicken");
        headItem=ItemBodyPart.getItemStackFromName("Chicken Head",1); torsoItem=ItemBodyPart.getItemStackFromName("Chicken Torso",1); armItem=ItemBodyPart.getItemStackFromName("Chicken Arm",1); legItem=ItemBodyPart.getItemStackFromName("Chicken Legs",1);
        texture=new ResourceLocation("textures/entity/chicken.png"); }
    @Override public void initRecipes() { initDefaultRecipes(Items.FEATHER); }
    @Override public void setAttributes(EntityLivingBase m, BodyPartLocation l) { addAttributeMods(m,"Ch",0D,1D,0D,0.5D,0D); }
}
