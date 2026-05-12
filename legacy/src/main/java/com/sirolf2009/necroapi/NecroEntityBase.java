package com.sirolf2009.necroapi;

import net.minecraft.client.model.ModelBase;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import com.sirolf2009.necromancy.entity.EntityMinion;
import com.sirolf2009.necromancy.item.RegistryNecromancyItems;

/**
 * The base class for all necro mob body-part definitions.
 */
public abstract class NecroEntityBase
{
    public String mobName;
    public ResourceLocation texture;
    public ItemStack headItem;
    public ItemStack torsoItem;
    public ItemStack armItem;
    public ItemStack legItem;
    public Object[] headRecipe;
    public Object[] torsoRecipe;
    public Object[] armRecipe;
    public Object[] legRecipe;
    public boolean hasHead = true;
    public boolean hasTorso = true;
    public boolean hasArms = true;
    public boolean hasLegs = true;
    public Item organs;
    public int textureWidth = 64;
    public int textureHeight = 32;
    protected boolean isNecromancyInstalled;
    public BodyPart[] head;
    public BodyPart[] torso;
    public BodyPart[] armLeft;
    public BodyPart[] armRight;
    public BodyPart[] legs;

    private boolean modelInit;

    public NecroEntityBase(String mobName)
    {
        this.mobName = mobName;
        try
        {
            Class.forName("com.sirolf2009.necromancy.Necromancy");
            isNecromancyInstalled = true;
            organs = RegistryNecromancyItems.organs;
            initRecipes();
        }
        catch (ClassNotFoundException e)
        {
            isNecromancyInstalled = false;
        }
        modelInit = false;
    }

    public void initRecipes() {}

    public void initDefaultRecipes(Object... items)
    {
        Object headItem = null, torsoItem = null, armItem = null, legItem = null;
        if (items.length == 1)
        {
            headItem = torsoItem = armItem = legItem = items[0];
        }
        else
        {
            headItem = items[0];
            torsoItem = items[1];
            armItem = items[2];
            legItem = items[3];
        }
        headRecipe = new Object[] { "SSSS", "SBFS", "SEES", 'S', new ItemStack(organs, 1, 4),
                'E', Items.SPIDER_EYE, 'F', headItem, 'B', new ItemStack(organs, 1, 0) };
        torsoRecipe = new Object[] { " LL ", "BHUB", "LEEL", "BLLB", 'L', new ItemStack(organs, 1, 4),
                'E', torsoItem, 'H', new ItemStack(organs, 1, 1),
                'U', new ItemStack(organs, 1, 3), 'B', Items.BONE };
        armRecipe = new Object[] { "LLLL", "BMEB", "LLLL", 'L', new ItemStack(organs, 1, 4),
                'E', armItem, 'M', new ItemStack(organs, 1, 2), 'B', Items.BONE };
        legRecipe = new Object[] { "LBBL", "LMML", "LEEL", "LBBL", 'L', new ItemStack(organs, 1, 4),
                'E', legItem, 'M', new ItemStack(organs, 1, 2), 'B', Items.BONE };
    }

    public BodyPart[] initHead(ModelBase model) { return null; }
    public BodyPart[] initTorso(ModelBase model) { return null; }
    public BodyPart[] initLegs(ModelBase model) { return null; }
    public BodyPart[] initArmLeft(ModelBase model) { return null; }
    public BodyPart[] initArmRight(ModelBase model) { return null; }

    public NecroEntityBase updateParts(ModelBase model)
    {
        if (!modelInit)
        {
            head = initHead(model);
            torso = initTorso(model);
            armLeft = initArmLeft(model);
            armRight = initArmRight(model);
            legs = initLegs(model);
            modelInit = true;
        }
        return this;
    }

    public void setAttributes(EntityLivingBase minion, BodyPartLocation location)
    {
        addAttributeMods(minion, "default", 2D, 1D, 0D, 2D, 2D);
    }

    protected void addAttributeMods(EntityLivingBase entity, String bodyPart, double health, double followRange,
            double knockBackResistance, double movementSpeed, double attackDamage)
    {
        if (health != 0D)
            entity.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH)
                    .applyModifier(new AttributeModifier(bodyPart + "FPMod", health, 1));
        if (followRange != 0D)
            entity.getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE)
                    .applyModifier(new AttributeModifier(bodyPart + "FRMod", followRange, 1));
        if (knockBackResistance != 0D)
            entity.getEntityAttribute(SharedMonsterAttributes.KNOCKBACK_RESISTANCE)
                    .applyModifier(new AttributeModifier(bodyPart + "KBRMod", knockBackResistance, 1));
        if (movementSpeed != 0D)
            entity.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED)
                    .applyModifier(new AttributeModifier(bodyPart + "MOVMod", movementSpeed, 1));
        if (attackDamage != 0D)
            entity.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE)
                    .applyModifier(new AttributeModifier(bodyPart + "DMGMod", attackDamage, 1));
    }

    public void setRotationAngles(float par1, float par2, float par3, float par4, float par5, float par6,
            Entity entity, BodyPart[] part, BodyPartLocation location) {}

    public void preRender(Entity entity, BodyPart[] parts, BodyPartLocation location, ModelBase model) {}

    public void postRender(Entity entity, BodyPart[] parts, BodyPartLocation location, ModelBase model) {}

    public void attackEntityAsMob(EntityMinion minion, BodyPartLocation location, Entity target, float damage) {}
}
