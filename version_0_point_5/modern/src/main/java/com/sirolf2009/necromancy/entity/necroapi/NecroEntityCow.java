package com.sirolf2009.necromancy.entity.necroapi;

import com.sirolf2009.necromancy.api.BodyPart;
import com.sirolf2009.necromancy.api.BodyPartLocation;
import com.sirolf2009.necromancy.api.ISaddleAble;
import com.sirolf2009.necromancy.api.NecroEntityQuadruped;
import com.sirolf2009.necromancy.item.NecromancyItems;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/**
 * 1:1 port of the legacy {@code NecroEntityCow}.
 *
 * <p>The cow's body is 12&times;18&times;10 (not the generic quadruped's
 * 10&times;16&times;8), with an extra udder cube and two horn cubes on the
 * head -- so we override {@link #initHead()}, {@link #initTorso()} and
 * {@link #initLegs()} from {@link NecroEntityQuadruped} to match the layout
 * of the vanilla 64&times;32 cow texture exactly.
 */
public class NecroEntityCow extends NecroEntityQuadruped implements ISaddleAble {

    public NecroEntityCow()                 { this("Cow", 12); }
    public NecroEntityCow(String n, int s)  {
        super(n, s);
        headItem  = new ItemStack(NecromancyItems.bodyPart("Cow Head"));
        torsoItem = new ItemStack(NecromancyItems.bodyPart("Cow Torso"));
        armItem   = new ItemStack(NecromancyItems.bodyPart("Cow Arm"));
        legItem   = new ItemStack(NecromancyItems.bodyPart("Cow Legs"));
        texture   = ResourceLocation.parse("minecraft:textures/entity/cow/cow.png");
    }

    @Override public void initRecipes() { initDefaultRecipes(Items.BEEF); }
    @Override public ResourceLocation getSaddleTexture() { return com.sirolf2009.necromancy.Reference.TEXTURE_SADDLE_COW; }

    /** Cow head: 8x8x6 face + two 1x3x1 horns at texOffs (22,0). */
    @Override
    public BodyPart[] initHead() {
        BodyPart head = new BodyPart(this, 0, 0);
        head.cubes.texOffs(0, 0).addBox(-4F, -4F, -4F, 8, 8, 6);
        head.cubes.texOffs(22, 0).addBox(-5F, -5F, -4F, 1, 3, 1);  // left horn
        head.cubes.texOffs(22, 0).addBox( 4F, -5F, -4F, 1, 3, 1);  // right horn
        return new BodyPart[] { head };
    }

    /** Cow body: 12x18x10 main + 4x6x1 udder, both pulled from the cow texture. */
    @Override
    public BodyPart[] initTorso() {
        float[] headPos     = {  4.0F, 16F - size, -14.0F };
        float[] armLeftPos  = { -1.0F, 12.0F, -10.0F };
        float[] armRightPos = {  5.0F, 12.0F, -10.0F };
        BodyPart body = new BodyPart(this, armLeftPos, armRightPos, headPos, 18, 4);
        body.cubes.texOffs(18, 4).addBox(-2F, -12F, -12F, 12, 18, 10);
        body.cubes.texOffs(52, 0).addBox( 2F,   2F, -13F,  4,  6,  1);  // udder
        return new BodyPart[] { body };
    }

    /** Cow legs: pivot at leg-top, slightly different anchor than the generic. */
    @Override
    public BodyPart[] initLegs() {
        float[] torsoPos = { -4F, -2F, 0F };
        BodyPart legLeft  = new BodyPart(this, torsoPos, 0, 16).mirror();
        legLeft.addBox(-2F, 0F, -2F, 4, size, 4).setPos(-4F, 22F - size, 2F);
        BodyPart legRight = new BodyPart(this, torsoPos, 0, 16);
        legRight.addBox(-2F, 0F, -2F, 4, size, 4).setPos( 4F, 22F - size, 2F);
        return new BodyPart[] { legLeft, legRight };
    }

    @Override
    public void setAttributes(LivingEntity m, BodyPartLocation loc) {
        switch (loc) {
            case Head     -> addAttributeMods(m, "Head", 0.5, 1, 0, 0, 0);
            case Torso    -> addAttributeMods(m, "Torso", 1, 0, 0, 0, 0);
            case ArmLeft  -> addAttributeMods(m, "ArmL", 0.25, 0, 0, 0, 0.25);
            case ArmRight -> addAttributeMods(m, "ArmR", 0.25, 0, 0, 0, 0.25);
            case Legs     -> addAttributeMods(m, "Legs", 0.25, 0, 1, 3, 0);
        }
    }
}
