package com.sirolf2009.necromancy.entity.necroapi;

import com.sirolf2009.necromancy.Reference;
import com.sirolf2009.necromancy.api.BodyPart;
import com.sirolf2009.necromancy.api.BodyPartLocation;
import com.sirolf2009.necromancy.api.NecroEntityBiped;
import com.sirolf2009.necromancy.item.NecromancyItems;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

/** 1:1 port of {@code NecroEntityIsaac}. */
public class NecroEntityIsaac extends NecroEntityBiped {
    public NecroEntityIsaac() {
        super("Isaac");
        headItem = new ItemStack(NecromancyItems.ISAACS_HEAD.get());
        hasTorso = false; hasArms = false; hasLegs = false;
        texture = Reference.TEXTURE_ENTITY_ISAAC_BLOOD;
        textureWidth  = 64;
        textureHeight = 32;
    }
    @Override public BodyPart[] initHead() {
        BodyPart head  = new BodyPart(this, 0, 0); head.addBox(-5F, -6F, -4F, 10, 9, 8); head.setPos(0F, 1F, 0F);
        BodyPart n1 = new BodyPart(this, 0, 0); n1.addBox(-1F, 2F, 0F, 1, 1, 1); n1.setPos(1F, 2F, -1F);
        BodyPart n2 = new BodyPart(this, 0, 0); n2.addBox(-1F, 2F, 0F, 1, 1, 1); n2.setPos(0F, 2F,  0F);
        BodyPart n3 = new BodyPart(this, 0, 0); n3.addBox(-1F, 2F, -1F, 1, 1, 1); n3.setPos(0F, 2F, 1F);
        BodyPart n4 = new BodyPart(this, 0, 0); n4.addBox(-1F, 2F, -1F, 1, 3, 1); n4.setPos(1F, 2F, 1F);
        return new BodyPart[] { head, n1, n2, n3, n4 };
    }
    @Override public void setAttributes(LivingEntity m, BodyPartLocation loc) {
        if (loc == BodyPartLocation.Head) addAttributeMods(m, "Head", 2, 1, 1, 0, 0.5);
    }
}
