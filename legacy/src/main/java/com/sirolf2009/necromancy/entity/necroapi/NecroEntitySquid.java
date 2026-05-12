package com.sirolf2009.necromancy.entity.necroapi;
import net.minecraft.entity.EntityLivingBase; import net.minecraft.init.Items; import net.minecraft.util.ResourceLocation;
import com.sirolf2009.necroapi.*; import com.sirolf2009.necromancy.item.ItemBodyPart;
public class NecroEntitySquid extends NecroEntityBiped {
    public NecroEntitySquid() { super("Squid");
        headItem=ItemBodyPart.getItemStackFromName("Squid Head",1); torsoItem=ItemBodyPart.getItemStackFromName("Squid Torso",1); legItem=ItemBodyPart.getItemStackFromName("Squid Legs",1);
        hasArms=false; texture=new ResourceLocation("textures/entity/squid.png"); }
    @Override public void initRecipes() { initDefaultRecipes(new net.minecraft.item.ItemStack(Items.DYE,1,0)); }
    @Override public void setAttributes(EntityLivingBase m, BodyPartLocation l) { addAttributeMods(m,"Sq",1D,2D,0D,0D,0D); }
}
