package com.sirolf2009.necromancy.entity.necroapi;
import net.minecraft.entity.EntityLivingBase; import net.minecraft.init.Items; import net.minecraft.util.ResourceLocation;
import com.sirolf2009.necroapi.*; import com.sirolf2009.necromancy.item.ItemBodyPart;
public class NecroEntityWitch extends NecroEntityBiped {
    public NecroEntityWitch() { super("Witch");
        headItem=ItemBodyPart.getItemStackFromName("Witch Head",1); torsoItem=ItemBodyPart.getItemStackFromName("Witch Torso",1); armItem=ItemBodyPart.getItemStackFromName("Witch Arm",1); legItem=ItemBodyPart.getItemStackFromName("Witch Legs",1);
        texture=new ResourceLocation("textures/entity/witch.png"); textureHeight=64; }
    @Override public void initRecipes() { initDefaultRecipes(Items.NETHER_WART); }
    @Override public void setAttributes(EntityLivingBase m, BodyPartLocation l) { addAttributeMods(m,"W",1D,3D,0D,1D,0.5D); }
}
