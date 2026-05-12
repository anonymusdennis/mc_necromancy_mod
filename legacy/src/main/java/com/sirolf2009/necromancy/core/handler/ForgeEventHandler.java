package com.sirolf2009.necromancy.core.handler;

import net.minecraft.block.Block;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.monster.EntitySkeleton;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingSpawnEvent.SpecialSpawn;
import net.minecraftforge.event.entity.player.FillBucketEvent;
import net.minecraftforge.fml.common.eventhandler.Event.Result;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.ItemCraftedEvent;

import com.sirolf2009.necromancy.Necromancy;
import com.sirolf2009.necromancy.achievement.AchievementNecromancy;
import com.sirolf2009.necromancy.block.RegistryBlocksNecromancy;
import com.sirolf2009.necromancy.entity.EntityIsaacNormal;
import com.sirolf2009.necromancy.entity.EntityNightCrawler;
import com.sirolf2009.necromancy.item.ItemGeneric;
import com.sirolf2009.necromancy.item.ItemNecroSkull;
import com.sirolf2009.necromancy.item.RegistryNecromancyItems;
import com.sirolf2009.necromancy.lib.ConfigurationNecromancy;

public class ForgeEventHandler
{
    @SubscribeEvent
    public void onPotentialSpawns(SpecialSpawn event)
    {
        if (event.getEntityLiving().getClass().equals(EntityZombie.class)
                || event.getEntityLiving().getClass().equals(EntitySkeleton.class))
        {
            if (event.getWorld().rand.nextInt(ConfigurationNecromancy.rarityNightCrawlers) == 0)
            {
                event.setResult(Result.DENY);
                EntityNightCrawler enc = new EntityNightCrawler(event.getWorld());
                enc.setPosition(event.getX(), event.getY(), event.getZ());
                event.getWorld().spawnEntity(enc);
            }
            else if (event.getWorld().rand.nextInt(ConfigurationNecromancy.rarityIsaacs) == 0)
            {
                event.setResult(Result.DENY);
                EntityIsaacNormal ein = new EntityIsaacNormal(event.getWorld());
                ein.setPosition(event.getX(), event.getY(), event.getZ());
                event.getWorld().spawnEntity(ein);
            }
        }
    }

    @SubscribeEvent
    public void onLivingDeath(LivingDeathEvent event)
    {
        if (event.getEntityLiving() instanceof EntityLiving && !event.getEntityLiving().world.isRemote)
        {
            switch (event.getEntityLiving().world.rand.nextInt(100))
            {
            case 0: event.getEntityLiving().entityDropItem(new ItemStack(RegistryNecromancyItems.organs, 1, 0), 1F); break;
            case 1: event.getEntityLiving().entityDropItem(new ItemStack(RegistryNecromancyItems.organs, 1, 1), 1F); break;
            case 2: case 3: case 4: case 5:
                event.getEntityLiving().entityDropItem(new ItemStack(RegistryNecromancyItems.organs, 1, 2), 1F); break;
            case 6: event.getEntityLiving().entityDropItem(new ItemStack(RegistryNecromancyItems.organs, 1, 3), 1F); break;
            default: break;
            }
        }
    }

    @SubscribeEvent
    public void onCrafting(ItemCraftedEvent event)
    {
        ItemStack item = event.crafting;
        EntityPlayer player = event.player;
        IInventory craftMatrix = event.craftMatrix;

        if (!item.isEmpty())
        {
            if (item.getItem() == RegistryNecromancyItems.necronomicon)
            {
                player.addStat(AchievementNecromancy.NecronomiconAchieve);
            }
            else if (item.isItemEqual(ItemGeneric.getItemStackFromName("Jar of Blood")))
            {
                player.inventory.addItemStackToInventory(new ItemStack(Items.BUCKET));
            }
            else if (item.getItem() == RegistryNecromancyItems.bucketBlood)
            {
                player.inventory.addItemStackToInventory(new ItemStack(Items.GLASS_BOTTLE, 8));
            }
            else if (item.getItem() instanceof ItemBlock)
            {
                Block block = ((ItemBlock) item.getItem()).getBlock();
                if (block == RegistryBlocksNecromancy.sewing)
                {
                    player.addStat(AchievementNecromancy.SewingAchieve);
                }
                else if (block == RegistryBlocksNecromancy.skullWall && item.hasTagCompound())
                {
                    item.getTagCompound().setString("Base", craftMatrix.getStackInSlot(0).getTranslationKey());
                    item.getTagCompound().setString("Skull1", ItemNecroSkull.skullTypes != null && craftMatrix.getStackInSlot(1).getMetadata() < ItemNecroSkull.skullTypes.length
                            ? ItemNecroSkull.skullTypes[craftMatrix.getStackInSlot(1).getMetadata()] : "");
                    item.getTagCompound().setString("Skull2", ItemNecroSkull.skullTypes != null && craftMatrix.getStackInSlot(4).getMetadata() < ItemNecroSkull.skullTypes.length
                            ? ItemNecroSkull.skullTypes[craftMatrix.getStackInSlot(4).getMetadata()] : "");
                    item.getTagCompound().setString("Skull3", ItemNecroSkull.skullTypes != null && craftMatrix.getStackInSlot(5).getMetadata() < ItemNecroSkull.skullTypes.length
                            ? ItemNecroSkull.skullTypes[craftMatrix.getStackInSlot(5).getMetadata()] : "");
                }
            }
        }
    }

    @SubscribeEvent
    public void onBucketFill(FillBucketEvent event)
    {
        if (event.getTarget() != null && event.getTarget().typeOfHit == RayTraceResult.Type.BLOCK)
        {
            World world = event.getWorld();
            RayTraceResult target = event.getTarget();
            Block block = world.getBlockState(target.getBlockPos()).getBlock();

            if ((block == RegistryBlocksNecromancy.fluidBlood.getBlock()
                    || block == RegistryBlocksNecromancy.blood)
                    && world.getBlockState(target.getBlockPos()).getBlock()
                            .getMetaFromState(world.getBlockState(target.getBlockPos())) == 0)
            {
                world.setBlockToAir(target.getBlockPos());
                event.setResult(Result.ALLOW);
                event.setFilledBucket(new ItemStack(RegistryNecromancyItems.bucketBlood));
            }
        }
    }
}
