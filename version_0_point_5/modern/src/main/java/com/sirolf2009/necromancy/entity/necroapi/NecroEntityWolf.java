package com.sirolf2009.necromancy.entity.necroapi;

import com.sirolf2009.necromancy.api.BodyPart;
import com.sirolf2009.necromancy.api.BodyPartLocation;
import com.sirolf2009.necromancy.api.NecroEntityBiped;
import com.sirolf2009.necromancy.item.NecromancyItems;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/** 1:1 port of {@code NecroEntityWolf} (head-only). */
public class NecroEntityWolf extends NecroEntityBiped {
    public NecroEntityWolf() {
        super("Wolf");
        headItem = new ItemStack(NecromancyItems.bodyPart("Wolf Head"));
        hasTorso = false; hasArms = false; hasLegs = false;
        texture = ResourceLocation.parse("minecraft:textures/entity/wolf/wolf.png");
        textureHeight = 32; textureWidth = 64;
    }
    @Override public void initRecipes() { initDefaultRecipes(Items.NAME_TAG); }
    @Override public BodyPart[] initHead() {
        BodyPart head = new BodyPart(this, 0, 0);
        head.cubes.addBox(-2F, -2.5F, 2F, 6, 6, 4);
        head.setPos(-1F, 0F, -3F);
        head.cubes.texOffs(16, 14).addBox(-2F, -4.5F, 4F, 2, 2, 1);
        head.cubes.texOffs(16, 14).addBox( 2F, -4.5F, 4F, 2, 2, 1);
        head.cubes.texOffs(0, 10).addBox(-0.5F, 0.5F, -1F, 3, 3, 4);
        return new BodyPart[] { head };
    }
    @Override public void setAttributes(LivingEntity m, BodyPartLocation loc) {
        if (loc == BodyPartLocation.Head) addAttributeMods(m, "Head", 2, 1, 1, 1, 2);
    }
}
