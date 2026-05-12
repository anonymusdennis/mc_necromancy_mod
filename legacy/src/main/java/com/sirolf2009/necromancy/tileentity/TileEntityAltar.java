package com.sirolf2009.necromancy.tileentity;

import java.util.Iterator;

import net.minecraft.entity.effect.EntityLightningBolt;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.fml.common.FMLCommonHandler;

import com.sirolf2009.necroapi.BodyPart;
import com.sirolf2009.necroapi.BodyPartLocation;
import com.sirolf2009.necroapi.NecroEntityBase;
import com.sirolf2009.necroapi.NecroEntityRegistry;
import com.sirolf2009.necromancy.Necromancy;
import com.sirolf2009.necromancy.achievement.AchievementNecromancy;
import com.sirolf2009.necromancy.entity.EntityMinion;
import com.sirolf2009.necromancy.item.ItemGeneric;

public class TileEntityAltar extends TileEntity implements IInventory, ITickable
{
    private ItemStack[] altarItemStacks = new ItemStack[7];
    private EntityMinion previewMinion;
    private ItemStack[] bodyPartsPrev = new ItemStack[5];

    public TileEntityAltar()
    {
        for (int i = 0; i < altarItemStacks.length; i++)
        {
            altarItemStacks[i] = ItemStack.EMPTY;
        }
        for (int i = 0; i < bodyPartsPrev.length; i++)
        {
            bodyPartsPrev[i] = ItemStack.EMPTY;
        }
    }

    @Override
    public void update() {}

    @Override
    public AxisAlignedBB getRenderBoundingBox()
    {
        return TileEntity.INFINITE_EXTENT_AABB;
    }

    public void spawn(EntityPlayer user)
    {
        if (!world.isRemote)
        {
            if (Necromancy.instance.maxSpawn != -1
                    && user.getEntityData().getInteger("minions") >= Necromancy.instance.maxSpawn)
            {
                user.sendMessage(new TextComponentString("<Death> Mortal fool! Thou shan't never grow that strong."));
                world.spawnEntity(new EntityLightningBolt(world, pos.getX(), pos.getY(), pos.getZ(), true));
            }
            else
            {
                BodyPart[][] types = new BodyPart[5][];
                EntityMinion model = getPreviewMinion();
                ItemStack head = getStackInSlot(2);
                ItemStack body = getStackInSlot(3);
                ItemStack leg = getStackInSlot(4);
                ItemStack armRight = getStackInSlot(5);
                ItemStack armLeft = getStackInSlot(6);

                types[0] = !head.isEmpty() ? getBodyPart(head, false) : new BodyPart[] {};
                types[1] = !body.isEmpty() ? getBodyPart(body, false) : new BodyPart[] {};
                types[2] = !armLeft.isEmpty() ? getBodyPart(armLeft, false) : new BodyPart[] {};
                types[3] = !armRight.isEmpty() ? getBodyPart(armRight, true) : new BodyPart[] {};
                types[4] = !leg.isEmpty() ? getBodyPart(leg, false) : new BodyPart[] {};

                EntityMinion spawned = new EntityMinion(world, types, user.getName());
                spawned.setPosition(pos.getX(), pos.getY() + 1, pos.getZ());
                spawned.calculateAttributes();
                world.spawnEntity(spawned);
                user.addStat(AchievementNecromancy.SpawnAchieve);
                user.sendMessage(new TextComponentString("<Minion> Your bidding?"));
                spawned.dataWatcherUpdate();
                if (model != null) spawned.getModel().updateModel(spawned, true);

                if (!user.isCreative())
                {
                    for (int x = 0; x < 7; x++) decrStackSize(x, 1);
                }
                user.getEntityData().setInteger("minions", user.getEntityData().getInteger("minions") + 1);
                spawned.playSound(spawned.world.isRemote ? null
                        : net.minecraft.util.SoundEvent.REGISTRY.getObject(
                                new net.minecraft.util.ResourceLocation("necromancy:spawn")),
                        1.0F, 1.0F);

                FMLCommonHandler.instance().getMinecraftServerInstance()
                        .getPlayerList().sendPacketToAllPlayers(getUpdatePacket());
            }
        }
    }

    public boolean canSpawn()
    {
        ItemStack slot0 = getStackInSlot(0);
        ItemStack slot1 = getStackInSlot(1);
        if (slot0.isEmpty() || slot0.getItem() != ItemGeneric.getItemStackFromName("Jar of Blood").getItem())
            return false;
        if (slot1.isEmpty() || !soulCheck()) return false;
        return true;
    }

    private BodyPart[] getBodyPart(ItemStack stack, boolean isRightArm)
    {
        EntityMinion model = getPreviewMinion();
        for (NecroEntityBase mob : NecroEntityRegistry.registeredEntities.values())
        {
            if (mob.headItem != null && stack.isItemEqual(mob.headItem))
                return mob.head == null ? mob.updateParts(model.getModel()).head : mob.head;
            if (mob.torsoItem != null && stack.isItemEqual(mob.torsoItem))
                return mob.torso == null ? mob.updateParts(model.getModel()).torso : mob.torso;
            if (mob.armItem != null && stack.isItemEqual(mob.armItem))
            {
                if (isRightArm)
                    return mob.armRight == null ? mob.updateParts(model.getModel()).armRight : mob.armRight;
                else
                    return mob.armLeft == null ? mob.updateParts(model.getModel()).armLeft : mob.armLeft;
            }
            if (mob.legItem != null && stack.isItemEqual(mob.legItem))
                return mob.legs == null ? mob.updateParts(model.getModel()).legs : mob.legs;
        }
        return null;
    }

    public boolean hasAltarChanged()
    {
        boolean changed = !areEqual(bodyPartsPrev[0], getStackInSlot(2))
                || !areEqual(bodyPartsPrev[1], getStackInSlot(3))
                || !areEqual(bodyPartsPrev[2], getStackInSlot(4))
                || !areEqual(bodyPartsPrev[3], getStackInSlot(5))
                || !areEqual(bodyPartsPrev[4], getStackInSlot(6));
        if (changed)
        {
            for (int i = 0; i < 5; i++)
            {
                ItemStack s = getStackInSlot(i + 2);
                bodyPartsPrev[i] = s.isEmpty() ? ItemStack.EMPTY : s.copy();
            }
        }
        return changed;
    }

    private boolean areEqual(ItemStack a, ItemStack b)
    {
        if (a.isEmpty() && b.isEmpty()) return true;
        if (a.isEmpty() || b.isEmpty()) return false;
        return a.isItemEqual(b);
    }

    private EntityMinion getPreviewMinion()
    {
        if (previewMinion == null && world != null)
        {
            previewMinion = new EntityMinion(world);
        }
        return previewMinion;
    }

    public EntityMinion getPreviewEntity()
    {
        EntityMinion model = getPreviewMinion();
        if (model != null && hasAltarChanged())
        {
            for (BodyPartLocation loc : BodyPartLocation.values())
            {
                int slotIndex = locToSlotIndex(loc);
                ItemStack stack = getStackInSlot(slotIndex);
                if (!stack.isEmpty() && isLegalCombo(locToString(loc), stack))
                    model.setBodyPart(loc, getBodyPart(stack, loc == BodyPartLocation.ArmRight));
                else
                    model.setBodyPart(loc, new BodyPart[] {});
            }
        }
        return model;
    }

    private int locToSlotIndex(BodyPartLocation loc)
    {
        switch (loc)
        {
        case Head: return 2;
        case Torso: return 3;
        case Legs: return 4;
        case ArmRight: return 5;
        case ArmLeft: return 6;
        default: return 2;
        }
    }

    private String locToString(BodyPartLocation loc)
    {
        switch (loc)
        {
        case Head: return "head";
        case Torso: return "body";
        case Legs: return "leg";
        case ArmLeft: case ArmRight: return "arm";
        default: return "head";
        }
    }

    private boolean isLegalCombo(String location, ItemStack stack)
    {
        if (stack.isEmpty()) return false;
        for (NecroEntityBase mob : NecroEntityRegistry.registeredEntities.values())
        {
            if ("head".equals(location) && mob.hasHead && stack.isItemEqual(mob.headItem)) return true;
            if ("body".equals(location) && mob.hasTorso && stack.isItemEqual(mob.torsoItem)) return true;
            if ("arm".equals(location) && mob.hasArms && stack.isItemEqual(mob.armItem)) return true;
            if ("leg".equals(location) && mob.hasLegs && stack.isItemEqual(mob.legItem)) return true;
        }
        return false;
    }

    private boolean soulCheck()
    {
        return getStackInSlot(1).getItem() == ItemGeneric.getItemStackFromName("Soul in a Jar").getItem();
    }

    @Override
    public void readFromNBT(NBTTagCompound compound)
    {
        super.readFromNBT(compound);
        NBTTagList list = compound.getTagList("Items", 10);
        altarItemStacks = new ItemStack[getSizeInventory()];
        for (int i = 0; i < altarItemStacks.length; i++)
            altarItemStacks[i] = ItemStack.EMPTY;
        for (int i = 0; i < list.tagCount(); i++)
        {
            NBTTagCompound tag = list.getCompoundTagAt(i);
            byte slot = tag.getByte("Slot");
            if (slot >= 0 && slot < altarItemStacks.length)
                altarItemStacks[slot] = new ItemStack(tag);
        }
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound)
    {
        super.writeToNBT(compound);
        NBTTagList list = new NBTTagList();
        for (int i = 0; i < altarItemStacks.length; i++)
        {
            if (!altarItemStacks[i].isEmpty())
            {
                NBTTagCompound tag = new NBTTagCompound();
                tag.setByte("Slot", (byte) i);
                altarItemStacks[i].writeToNBT(tag);
                list.appendTag(tag);
            }
        }
        compound.setTag("Items", list);
        return compound;
    }

    @Override
    public SPacketUpdateTileEntity getUpdatePacket()
    {
        return new SPacketUpdateTileEntity(pos, 0, getUpdateTag());
    }

    @Override
    public NBTTagCompound getUpdateTag()
    {
        return writeToNBT(new NBTTagCompound());
    }

    @Override
    public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt)
    {
        readFromNBT(pkt.getNbtCompound());
    }

    @Override
    public int getSizeInventory() { return altarItemStacks.length; }

    @Override
    public boolean isEmpty()
    {
        for (ItemStack s : altarItemStacks)
            if (!s.isEmpty()) return false;
        return true;
    }

    @Override
    public ItemStack getStackInSlot(int index) { return altarItemStacks[index]; }

    @Override
    public ItemStack decrStackSize(int index, int count)
    {
        if (altarItemStacks[index].isEmpty()) return ItemStack.EMPTY;
        if (altarItemStacks[index].getCount() <= count)
        {
            ItemStack old = altarItemStacks[index];
            altarItemStacks[index] = ItemStack.EMPTY;
            return old;
        }
        return altarItemStacks[index].splitStack(count);
    }

    @Override
    public ItemStack removeStackFromSlot(int index)
    {
        ItemStack old = altarItemStacks[index];
        altarItemStacks[index] = ItemStack.EMPTY;
        return old;
    }

    @Override
    public void setInventorySlotContents(int index, ItemStack stack)
    {
        altarItemStacks[index] = stack == null ? ItemStack.EMPTY : stack;
        if (!altarItemStacks[index].isEmpty() && altarItemStacks[index].getCount() > getInventoryStackLimit())
            altarItemStacks[index].setCount(getInventoryStackLimit());
    }

    @Override
    public String getName() { return "Altar"; }

    @Override
    public boolean hasCustomName() { return false; }

    @Override
    public ITextComponent getDisplayName() { return new TextComponentString(getName()); }

    @Override
    public int getInventoryStackLimit() { return 64; }

    @Override
    public boolean isUsableByPlayer(EntityPlayer player)
    {
        return world.getTileEntity(pos) == this
                && player.getDistanceSq(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5) < 64;
    }

    @Override
    public void openInventory(EntityPlayer player) {}

    @Override
    public void closeInventory(EntityPlayer player) {}

    @Override
    public boolean isItemValidForSlot(int index, ItemStack stack) { return false; }

    @Override
    public int getField(int id) { return 0; }

    @Override
    public void setField(int id, int value) {}

    @Override
    public int getFieldCount() { return 0; }

    @Override
    public void clear()
    {
        for (int i = 0; i < altarItemStacks.length; i++)
            altarItemStacks[i] = ItemStack.EMPTY;
    }
}
