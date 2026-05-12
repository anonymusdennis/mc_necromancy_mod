package com.sirolf2009.necromancy.entity;

import java.util.List;
import java.util.UUID;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IRangedAttackMob;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIAttackMelee;
import net.minecraft.entity.ai.EntityAIFollowOwner;
import net.minecraft.entity.ai.EntityAIHurtByTarget;
import net.minecraft.entity.ai.EntityAILeapAtTarget;
import net.minecraft.entity.ai.EntityAILookIdle;
import net.minecraft.entity.ai.EntityAIOwnerHurtByTarget;
import net.minecraft.entity.ai.EntityAIOwnerHurtTarget;
import net.minecraft.entity.ai.EntityAISit;
import net.minecraft.entity.ai.EntityAISwimming;
import net.minecraft.entity.ai.EntityAITempt;
import net.minecraft.entity.ai.EntityAIWanderAvoidWater;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.passive.EntityTameable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;

import com.sirolf2009.necroapi.BodyPart;
import com.sirolf2009.necroapi.BodyPartLocation;
import com.sirolf2009.necroapi.ISaddleAble;
import com.sirolf2009.necroapi.NecroEntityBase;
import com.sirolf2009.necroapi.NecroEntityRegistry;
import com.sirolf2009.necromancy.client.model.ModelMinion;
import com.sirolf2009.necromancy.item.ItemGeneric;

public class EntityMinion extends EntityTameable implements IRangedAttackMob
{
    private static final DataParameter<String> PART_HEAD =
            EntityDataManager.createKey(EntityMinion.class, DataSerializers.STRING);
    private static final DataParameter<String> PART_TORSO =
            EntityDataManager.createKey(EntityMinion.class, DataSerializers.STRING);
    private static final DataParameter<String> PART_ARM_LEFT =
            EntityDataManager.createKey(EntityMinion.class, DataSerializers.STRING);
    private static final DataParameter<String> PART_ARM_RIGHT =
            EntityDataManager.createKey(EntityMinion.class, DataSerializers.STRING);
    private static final DataParameter<String> PART_LEGS =
            EntityDataManager.createKey(EntityMinion.class, DataSerializers.STRING);
    private static final DataParameter<Byte> SADDLED =
            EntityDataManager.createKey(EntityMinion.class, DataSerializers.BYTE);
    private static final DataParameter<Byte> AGGRESSIVE =
            EntityDataManager.createKey(EntityMinion.class, DataSerializers.BYTE);

    protected BodyPart[] head, torso, armLeft, armRight, leg;
    protected ModelMinion model;
    private int attackTimer;
    private int rangedAttackTimer;

    public EntityMinion(World world)
    {
        super(world);
        model = new ModelMinion();
        getNavigator().setAvoidWater(true);
        setSize(0.6F, 1.8F);

        tasks.addTask(1, new EntityAISwimming(this));
        tasks.addTask(2, new EntityAISit(this));
        tasks.addTask(3, new EntityAILeapAtTarget(this, 0.4F));
        tasks.addTask(4, new EntityAIAttackMelee(this, 1.0D, true));
        tasks.addTask(6, new EntityAIWanderAvoidWater(this, 1.0D));
        tasks.addTask(7, new EntityAITempt(this, 1.2D, ItemGeneric.getItemStackFromName("Brain on a Stick").getItem(), false));
        tasks.addTask(8, new EntityAIFollowOwner(this, 1.0D, 10.0F, 2.0F));
        tasks.addTask(9, new EntityAIWatchClosest(this, EntityPlayer.class, 8.0F));
        tasks.addTask(10, new EntityAILookIdle(this));
        targetTasks.addTask(1, new EntityAIOwnerHurtByTarget(this));
        targetTasks.addTask(2, new EntityAIOwnerHurtTarget(this));
        targetTasks.addTask(3, new EntityAIHurtByTarget(this, true));
    }

    public EntityMinion(World world, BodyPart[][] bodypart, String ownerName)
    {
        this(world);
        setBodyParts(bodypart);
        setTamed(true);
        EntityPlayer owner = world.getPlayerEntityByName(ownerName);
        if (owner != null) setOwnerId(owner.getUniqueID());
    }

    @Override
    protected void entityInit()
    {
        super.entityInit();
        dataManager.register(PART_HEAD, "UNDEFINED");
        dataManager.register(PART_TORSO, "UNDEFINED");
        dataManager.register(PART_ARM_LEFT, "UNDEFINED");
        dataManager.register(PART_ARM_RIGHT, "UNDEFINED");
        dataManager.register(PART_LEGS, "UNDEFINED");
        dataManager.register(SADDLED, (byte) 0);
        dataManager.register(AGGRESSIVE, (byte) 0);
    }

    @Override
    protected void applyEntityAttributes()
    {
        super.applyEntityAttributes();
        getAttributeMap().registerAttribute(SharedMonsterAttributes.ATTACK_DAMAGE);
    }

    public void calculateAttributes()
    {
        if (getBodyParts().length > 0)
        {
            getAttributeMap().getAttributeInstance(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(20D);
            getAttributeMap().getAttributeInstance(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.1D);
            getAttributeMap().getAttributeInstance(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue(16D);
            getAttributeMap().getAttributeInstance(SharedMonsterAttributes.KNOCKBACK_RESISTANCE).setBaseValue(0.1D);
            getAttributeMap().getAttributeInstance(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(2D);

            if (head != null && head.length > 0 && head[0] != null)
                head[0].entity.setAttributes(this, BodyPartLocation.Head);
            if (torso != null && torso.length > 0 && torso[0] != null)
                torso[0].entity.setAttributes(this, BodyPartLocation.Torso);
            if (armLeft != null && armLeft.length > 0 && armLeft[0] != null)
                armLeft[0].entity.setAttributes(this, BodyPartLocation.ArmLeft);
            if (armRight != null && armRight.length > 0 && armRight[0] != null)
                armRight[0].entity.setAttributes(this, BodyPartLocation.ArmRight);
            if (leg != null && leg.length > 0 && leg[0] != null)
                leg[0].entity.setAttributes(this, BodyPartLocation.Legs);

            setHealth((float) getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).getAttributeValue());
        }
    }

    public void dataWatcherUpdate()
    {
        String[] names = getBodyPartsNames();
        if (!"UNDEFINED".equals(names[0])) dataManager.set(PART_HEAD, names[0]);
        if (!"UNDEFINED".equals(names[1])) dataManager.set(PART_TORSO, names[1]);
        if (!"UNDEFINED".equals(names[2])) dataManager.set(PART_ARM_LEFT, names[2]);
        if (!"UNDEFINED".equals(names[3])) dataManager.set(PART_ARM_RIGHT, names[3]);
        if (!"UNDEFINED".equals(names[4])) dataManager.set(PART_LEGS, names[4]);
    }

    private void syncBodyPartsFromDataManager()
    {
        head = getBodyPartFromLocation(BodyPartLocation.Head, dataManager.get(PART_HEAD));
        torso = getBodyPartFromLocation(BodyPartLocation.Torso, dataManager.get(PART_TORSO));
        armLeft = getBodyPartFromLocation(BodyPartLocation.ArmLeft, dataManager.get(PART_ARM_LEFT));
        armRight = getBodyPartFromLocation(BodyPartLocation.ArmRight, dataManager.get(PART_ARM_RIGHT));
        leg = getBodyPartFromLocation(BodyPartLocation.Legs, dataManager.get(PART_LEGS));
    }

    private BodyPart[] getBodyPartFromLocation(BodyPartLocation location, String name)
    {
        if ("UNDEFINED".equals(name)) return null;
        NecroEntityBase mob = NecroEntityRegistry.registeredEntities.get(name);
        if (mob == null)
        {
            System.err.println("Necromancy: body part '" + name + "' for location " + location + " not found");
            return null;
        }
        switch (location)
        {
        case Head:
            return mob.head == null ? mob.updateParts(model).head : mob.head;
        case Torso:
            return mob.torso == null ? mob.updateParts(model).torso : mob.torso;
        case ArmLeft:
            return mob.armLeft == null ? mob.updateParts(model).armLeft : mob.armLeft;
        case ArmRight:
            return mob.armRight == null ? mob.updateParts(model).armRight : mob.armRight;
        case Legs:
            return mob.legs == null ? mob.updateParts(model).legs : mob.legs;
        default:
            return null;
        }
    }

    @Override
    public boolean attackEntityAsMob(Entity target)
    {
        attackTimer = 10;
        world.setEntityState(this, (byte) 4);

        float damage = (float) getAttributeMap().getAttributeInstance(SharedMonsterAttributes.ATTACK_DAMAGE).getAttributeValue();

        if (head != null && head.length > 0 && head[0] != null)
            head[0].entity.attackEntityAsMob(this, BodyPartLocation.Head, target, damage);
        if (torso != null && torso.length > 0 && torso[0] != null)
            torso[0].entity.attackEntityAsMob(this, BodyPartLocation.Torso, target, damage);
        if (armLeft != null && armLeft.length > 0 && armLeft[0] != null)
            armLeft[0].entity.attackEntityAsMob(this, BodyPartLocation.ArmLeft, target, damage);
        if (armRight != null && armRight.length > 0 && armRight[0] != null)
            armRight[0].entity.attackEntityAsMob(this, BodyPartLocation.ArmRight, target, damage);
        if (leg != null && leg.length > 0 && leg[0] != null)
            leg[0].entity.attackEntityAsMob(this, BodyPartLocation.Legs, target, damage);

        EntityLivingBase owner = getOwner();
        if (owner instanceof EntityPlayer)
            return target.attackEntityFrom(DamageSource.causePlayerDamage((EntityPlayer) owner), damage);
        else
            return target.attackEntityFrom(DamageSource.causeMobDamage(this), damage);
    }

    @Override
    public boolean processInteract(EntityPlayer player, net.minecraft.util.EnumHand hand)
    {
        if (!world.isRemote)
        {
            if (!getSaddled())
            {
                ItemStack held = player.getHeldItem(hand);
                if (!held.isEmpty() && held.getItem() == Items.SADDLE)
                {
                    NecroEntityBase mob;
                    if (torso != null && torso[0] != null
                            && (mob = NecroEntityRegistry.registeredEntities.get(torso[0].name)) != null
                            && mob instanceof ISaddleAble)
                    {
                        setSaddled(true);
                        if (!player.isCreative()) held.shrink(1);
                        return true;
                    }
                    return false;
                }
            }
            else if (!isBeingRidden() && isOwner(player))
            {
                player.startRiding(this);
                return true;
            }

            if (!isBeingRidden() && isOwner(player))
            {
                setSitting(!isSitting());
                isJumping = false;
                navigator.clearPath();
                setAttackTarget(null);
                player.sendMessage(new TextComponentString("Minion is " + (isSitting() ? "staying put." : "free to move.")));
            }
            else if (!isOwner(player))
            {
                player.sendMessage(new TextComponentString("<Minion> I obey only " + getOwnerName()));
            }
        }
        return true;
    }

    private boolean isOwner(EntityPlayer player)
    {
        UUID ownerUUID = getOwnerId();
        return ownerUUID != null && ownerUUID.equals(player.getUniqueID());
    }

    private String getOwnerName()
    {
        EntityLivingBase owner = getOwner();
        return owner != null ? owner.getName() : "nobody";
    }

    @Override
    public void onUpdate()
    {
        super.onUpdate();

        List<EntityPlayer> nearby = world.getEntitiesWithinAABB(EntityPlayer.class, getEntityBoundingBox().grow(10D, 4D, 10D));
        for (EntityPlayer player : nearby)
        {
            if (getOwner() != null)
            {
                NBTTagCompound nbt = getOwner().getEntityData();
                boolean isAggressive = nbt.getBoolean("aggressive");
                String playerName = player.getName();
                if ("enemy".equals(nbt.getString(playerName)))
                    setAttackTarget(player);
                else if (nbt.getString(playerName).isEmpty() && isAggressive)
                    setAttackTarget(player);
            }
        }

        if (ticksExisted % 1000 == 0 || ticksExisted == 1)
        {
            if (!world.isRemote) dataWatcherUpdate();
            else syncBodyPartsFromDataManager();
        }

        if (ticksExisted == 1) model.updateModel(this, true);

        if (head == null) setDead();
    }

    @Override
    public void onLivingUpdate()
    {
        super.onLivingUpdate();
        if (attackTimer > 0)
        {
            attackTimer--;
        }
        else if (getAttackTarget() != null && rangedAttackTimer == 0)
        {
            attackEntityWithRangedAttack(getAttackTarget(), 0);
            rangedAttackTimer = 60;
        }

        if (rangedAttackTimer > 0) rangedAttackTimer--;
    }

    @Override
    public void handleStatusUpdate(byte status)
    {
        if (status == 4) attackTimer = 10;
        else super.handleStatusUpdate(status);
    }

    @Override
    public void attackEntityWithRangedAttack(EntityLivingBase target, float distanceFactor)
    {
        if ("Isaac".equals(getBodyPartsNames()[0]))
        {
            playSound(net.minecraft.util.SoundEvent.REGISTRY.getObject(
                    new ResourceLocation("necromancy:tear")), 1.0F, 1.0F / (getRNG().nextFloat() * 0.4F + 0.8F));
            world.spawnEntity(rand.nextInt(5) == 0
                    ? new EntityTearBlood(world, this, target)
                    : new EntityTear(world, this, target));
        }
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound compound)
    {
        super.writeEntityToNBT(compound);
        String[] names = getBodyPartsNames();
        compound.setString("head", names[0]);
        compound.setString("body", names[1]);
        compound.setString("armLeft", names[2]);
        compound.setString("armRight", names[3]);
        compound.setString("leg", names[4]);
        compound.setBoolean("Saddle", getSaddled());
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound compound)
    {
        super.readEntityFromNBT(compound);
        head = getBodyPartFromLocation(BodyPartLocation.Head, compound.getString("head"));
        torso = getBodyPartFromLocation(BodyPartLocation.Torso, compound.getString("body"));
        armLeft = getBodyPartFromLocation(BodyPartLocation.ArmLeft, compound.getString("armLeft"));
        armRight = getBodyPartFromLocation(BodyPartLocation.ArmRight, compound.getString("armRight"));
        leg = getBodyPartFromLocation(BodyPartLocation.Legs, compound.getString("leg"));
        setSaddled(compound.getBoolean("Saddle"));
        dataWatcherUpdate();
    }

    @Override
    public void onDeath(DamageSource cause)
    {
        super.onDeath(cause);
        EntityLivingBase owner = getOwner();
        if (owner != null)
            owner.getEntityData().setInteger("minions", owner.getEntityData().getInteger("minions") - 1);
    }

    @Override
    protected net.minecraft.util.SoundEvent getAmbientSound()
    {
        String headName = getBodyPartsNames()[0];
        ResourceLocation loc = new ResourceLocation("mob." + headName.toLowerCase() + ".say");
        return net.minecraft.util.SoundEvent.REGISTRY.getObject(loc);
    }

    @Override
    protected net.minecraft.util.SoundEvent getHurtSound(DamageSource source)
    {
        String headName = getBodyPartsNames()[0];
        ResourceLocation loc = new ResourceLocation("mob." + headName.toLowerCase() + ".hurt");
        return net.minecraft.util.SoundEvent.REGISTRY.getObject(loc);
    }

    @Override
    protected net.minecraft.util.SoundEvent getDeathSound()
    {
        String headName = getBodyPartsNames()[0];
        ResourceLocation loc = new ResourceLocation("mob." + headName.toLowerCase() + ".death");
        return net.minecraft.util.SoundEvent.REGISTRY.getObject(loc);
    }

    public boolean getSaddled() { return (dataManager.get(SADDLED) & 1) != 0; }

    public void setSaddled(boolean saddled)
    {
        dataManager.set(SADDLED, saddled ? (byte) 1 : (byte) 0);
    }

    @Override
    public boolean isAIDisabled() { return false; }

    public void setBodyPart(BodyPartLocation location, BodyPart[] parts)
    {
        switch (location)
        {
        case Head: head = parts; break;
        case Torso: torso = parts; break;
        case ArmLeft: armLeft = parts; break;
        case ArmRight: armRight = parts; break;
        case Legs: leg = parts; break;
        default: System.err.println("Necromancy: tried to set impossible body part location: " + location);
        }
        dataWatcherUpdate();
    }

    public void setBodyParts(BodyPart[][] parts)
    {
        head = parts[0];
        torso = parts[1];
        armLeft = parts[2];
        armRight = parts[3];
        leg = parts[4];
        dataWatcherUpdate();
    }

    public String[] getBodyPartsNames()
    {
        return new String[] {
            head != null && head.length > 0 && head[0] != null ? head[0].name : "UNDEFINED",
            torso != null && torso.length > 0 && torso[0] != null ? torso[0].name : "UNDEFINED",
            armLeft != null && armLeft.length > 0 && armLeft[0] != null ? armLeft[0].name : "UNDEFINED",
            armRight != null && armRight.length > 0 && armRight[0] != null ? armRight[0].name : "UNDEFINED",
            leg != null && leg.length > 0 && leg[0] != null ? leg[0].name : "UNDEFINED"
        };
    }

    public BodyPart[][] getBodyParts() { return new BodyPart[][] { head, torso, armLeft, armRight, leg }; }

    public ModelMinion getModel() { return model; }

    public void setModel(ModelMinion model) { this.model = model; }

    public int getAttackTimer() { return attackTimer; }
}
