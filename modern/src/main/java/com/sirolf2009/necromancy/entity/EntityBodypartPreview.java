package com.sirolf2009.necromancy.entity;

import com.sirolf2009.necromancy.bodypart.BodypartPreviewMask;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

/** Client-visualised anchor for the bodypart dev editor; hitbox outline is drawn by {@link com.sirolf2009.necromancy.client.renderer.BodypartPreviewRenderer}. */
public class EntityBodypartPreview extends Mob {

    private static final EntityDataAccessor<String> DATA_PART_ID =
        SynchedEntityData.defineId(EntityBodypartPreview.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<Integer> DATA_VISIBILITY =
        SynchedEntityData.defineId(EntityBodypartPreview.class, EntityDataSerializers.INT);
    /** Packed {@link net.minecraft.core.BlockPos} of the bodypart dev block (client reads draft from its BE). */
    private static final EntityDataAccessor<Long> DATA_DEV_BLOCK =
        SynchedEntityData.defineId(EntityBodypartPreview.class, EntityDataSerializers.LONG);

    public EntityBodypartPreview(EntityType<? extends EntityBodypartPreview> type, Level level) {
        super(type, level);
        setNoAi(true);
        setInvisible(false);
        setInvulnerable(true);
        setNoGravity(true);
        noPhysics = true;
        setPersistenceRequired();
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_PART_ID, "");
        builder.define(DATA_VISIBILITY, BodypartPreviewMask.DEFAULT_ALL);
        builder.define(DATA_DEV_BLOCK, 0L);
    }

    @Override
    protected void registerGoals() {}

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    public boolean isPickable() {
        return false;
    }

    @Override
    public void tick() {
        setDeltaMovement(0, 0, 0);
        super.tick();
    }

    @Override
    public void aiStep() {
        // Static anchor: skip vanilla limb swing / walk animation that bobbles Y each tick.
    }

    public void setPartId(@Nullable ResourceLocation id) {
        entityData.set(DATA_PART_ID, id == null ? "" : id.toString());
    }

    public @Nullable ResourceLocation getPartIdRl() {
        String s = entityData.get(DATA_PART_ID);
        return s.isEmpty() ? null : ResourceLocation.parse(s);
    }

    public void setPreviewVisibilityMask(int mask) {
        entityData.set(DATA_VISIBILITY, mask);
    }

    public int getPreviewVisibilityMask() {
        return entityData.get(DATA_VISIBILITY);
    }

    public void setDevBlockPacked(long packedPos) {
        entityData.set(DATA_DEV_BLOCK, packedPos);
    }

    public long getDevBlockPacked() {
        return entityData.get(DATA_DEV_BLOCK);
    }
}
