package com.sirolf2009.necromancy.item;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraftforge.common.util.EnumHelper;

import com.sirolf2009.necromancy.Necromancy;

public class ItemScythe extends ItemSword
{
    public static final ToolMaterial toolScythe = EnumHelper.addToolMaterial("BLOODSCYTHE", 0, 666, 7F, 2F, 9);
    public static final ToolMaterial toolScytheBone = EnumHelper.addToolMaterial("BLOODSCYTHEBONE", 0, 666, 7F, 4F, 9);

    public ItemScythe(ToolMaterial material)
    {
        super(material);
        setCreativeTab(Necromancy.tabNecromancy);
    }

    @Override
    public boolean hitEntity(ItemStack stack, EntityLivingBase target, EntityLivingBase attacker)
    {
        stack.damageItem(1, attacker);
        if (target.getHealth() <= 0 && attacker instanceof EntityPlayer)
        {
            EntityPlayer player = (EntityPlayer) attacker;
            if (player.inventory.consumeInventoryItem(Items.GLASS_BOTTLE))
            {
                player.inventory.addItemStackToInventory(ItemGeneric.getItemStackFromName("Soul in a Jar"));
                if (target.world.isRemote)
                {
                    for (int i = 0; i < 30; i++)
                    {
                        Necromancy.proxy.spawnParticle("skull", target.posX, target.posY, target.posZ,
                                target.getRNG().nextDouble() / 360 * 10,
                                target.getRNG().nextDouble() / 360 * 10,
                                target.getRNG().nextDouble() / 360 * 10);
                    }
                }
                target.motionY = 10000;
            }
        }
        return super.hitEntity(stack, target, attacker);
    }
}
