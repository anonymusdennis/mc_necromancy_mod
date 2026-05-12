package com.sirolf2009.necromancy.entity.necroapi;
import net.minecraft.entity.EntityLivingBase; import net.minecraft.init.Items; import net.minecraft.item.ItemStack; import net.minecraft.util.ResourceLocation;
import com.sirolf2009.necroapi.*; import com.sirolf2009.necromancy.item.ItemBodyPart;
public class NecroEntityCreeper extends NecroEntityBiped {
    public NecroEntityCreeper() { super("Creeper");
        torsoItem=ItemBodyPart.getItemStackFromName("Creeper Torso",1); legItem=ItemBodyPart.getItemStackFromName("Creeper Legs",1);
        hasHead=false; hasArms=false; texture=new ResourceLocation("textures/entity/creeper/creeper.png"); }
    @Override public void initRecipes() { initDefaultRecipes(Items.GUNPOWDER, Items.GUNPOWDER, Items.GUNPOWDER, Items.GUNPOWDER); }
    @Override public void setAttributes(EntityLivingBase m, BodyPartLocation l) {
        if(l==BodyPartLocation.Torso) addAttributeMods(m,"T",3D,0D,0D,0D,0D);
        else if(l==BodyPartLocation.Legs) addAttributeMods(m,"L",0D,0D,0D,2D,0D); }
}
