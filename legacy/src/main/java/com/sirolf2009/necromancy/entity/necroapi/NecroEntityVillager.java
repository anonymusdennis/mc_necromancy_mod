package com.sirolf2009.necromancy.entity.necroapi;
import net.minecraft.entity.EntityLivingBase; import net.minecraft.init.Items; import net.minecraft.util.ResourceLocation;
import com.sirolf2009.necroapi.*; import com.sirolf2009.necromancy.item.ItemBodyPart;
public class NecroEntityVillager extends NecroEntityBiped {
    public NecroEntityVillager() { super("Villager");
        headItem=ItemBodyPart.getItemStackFromName("Villager Head",1); torsoItem=ItemBodyPart.getItemStackFromName("Villager Torso",1); armItem=ItemBodyPart.getItemStackFromName("Villager Arm",1); legItem=ItemBodyPart.getItemStackFromName("Villager Legs",1);
        texture=new ResourceLocation("textures/entity/villager/villager.png"); textureHeight=64; }
    @Override public void initRecipes() { initDefaultRecipes(Items.EMERALD); }
    @Override public void setAttributes(EntityLivingBase m, BodyPartLocation l) { addAttributeMods(m,"V",1D,2D,0D,1D,0D); }
}
