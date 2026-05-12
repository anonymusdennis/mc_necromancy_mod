package com.sirolf2009.necromancy.block.entity;

import com.sirolf2009.necromancy.block.NecromancyBlocks;
import com.sirolf2009.necromancy.bodypart.BodyPartConfigManager;
import com.sirolf2009.necromancy.bodypart.BodyPartItemIds;
import com.sirolf2009.necromancy.bodypart.BodypartDefinitionIo;
import com.sirolf2009.necromancy.bodypart.BodypartDefinitionJson;
import com.sirolf2009.necromancy.bodypart.BodypartPreviewMask;
import com.sirolf2009.necromancy.entity.EntityBodypartPreview;
import com.sirolf2009.necromancy.entity.NecromancyEntities;
import com.sirolf2009.necromancy.inventory.ContainerBodypartDev;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.nio.charset.StandardCharsets;
import java.util.Objects;

public class BlockEntityBodypartDev extends BlockEntity implements Container, MenuProvider {

    public static final int SLOT_PART = 0;
    private final NonNullList<ItemStack> items = NonNullList.withSize(1, ItemStack.EMPTY);

    private byte[] draftUtf8 = BodypartDefinitionIo.toJson(new BodypartDefinitionJson()).getBytes(StandardCharsets.UTF_8);
    private int previewVisibilityMask = BodypartPreviewMask.DEFAULT_ALL;
    private int previewEntityId;

    public BlockEntityBodypartDev(BlockPos pos, BlockState state) {
        super(NecromancyBlocks.BODYPART_DEV_BE.get(), pos, state);
    }

    public static void tickServer(Level level, BlockPos pos, BlockState state, BlockEntityBodypartDev be) {
        if (level.isClientSide) return;
        be.tickPreview(level, state);
    }

    private void tickPreview(Level level, BlockState state) {
        Direction dir = state.getValue(com.sirolf2009.necromancy.block.BlockBodypartDev.FACING);
        float previewYaw = dir.toYRot();
        ItemStack stack = getItem(SLOT_PART);
        if (stack.isEmpty() || !BodyPartItemIds.isBodyPartStack(stack)) {
            removePreview(level);
            return;
        }
        var id = BodyPartItemIds.partId(stack.getItem());
        if (id == null) {
            removePreview(level);
            return;
        }
        Vec3 spot = previewSpot(state);
        Entity existing = previewEntityId != 0 ? level.getEntity(previewEntityId) : null;
        EntityBodypartPreview previewMob;
        if (existing instanceof EntityBodypartPreview alive && alive.isAlive()) {
            previewMob = alive;
        } else {
            removePreview(level);
            previewMob = new EntityBodypartPreview(NecromancyEntities.BODYPART_PREVIEW.get(), level);
            previewMob.setPos(spot.x, spot.y, spot.z);
            previewMob.setYRot(previewYaw);
            previewMob.setYHeadRot(previewYaw);
            previewMob.setPartId(id);
            previewMob.setPreviewVisibilityMask(previewVisibilityMask);
            previewMob.setDevBlockPacked(worldPosition.asLong());
            level.addFreshEntity(previewMob);
            previewEntityId = previewMob.getId();
            return;
        }
        if (previewMob.getYRot() != previewYaw || previewMob.getYHeadRot() != previewYaw) {
            previewMob.setYRot(previewYaw);
            previewMob.setYHeadRot(previewYaw);
        }
        if (previewMob.distanceToSqr(spot) > 1e-6) {
            previewMob.setPos(spot.x, spot.y, spot.z);
        }
        if (!Objects.equals(id, previewMob.getPartIdRl())) {
            previewMob.setPartId(id);
        }
        if (previewMob.getPreviewVisibilityMask() != previewVisibilityMask) {
            previewMob.setPreviewVisibilityMask(previewVisibilityMask);
        }
        long packed = worldPosition.asLong();
        if (previewMob.getDevBlockPacked() != packed) {
            previewMob.setDevBlockPacked(packed);
        }
    }

    private Vec3 previewSpot(BlockState state) {
        Direction dir = state.getValue(com.sirolf2009.necromancy.block.BlockBodypartDev.FACING);
        return Vec3.atBottomCenterOf(worldPosition.relative(dir))
            .add(dir.getStepX() * 1.15, 1.1, dir.getStepZ() * 1.15);
    }

    private void removePreview(Level level) {
        if (previewEntityId != 0) {
            Entity e = level.getEntity(previewEntityId);
            if (e != null) e.discard();
            previewEntityId = 0;
        }
    }

    public String getDraftJson() {
        return new String(draftUtf8, StandardCharsets.UTF_8);
    }

    public void setDraftJson(String json) {
        draftUtf8 = json.getBytes(StandardCharsets.UTF_8);
        setChanged();
        syncToClients();
    }

    public int getPreviewVisibilityMask() {
        return previewVisibilityMask;
    }

    public void setPreviewVisibilityMask(int previewVisibilityMask) {
        this.previewVisibilityMask = previewVisibilityMask;
        setChanged();
        syncToClients();
    }

    private void syncToClients() {
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    /** Reload draft from disk / defaults when the bodypart slot changes. */
    public void refreshDraftForCurrentSlot() {
        ItemStack stack = getItem(SLOT_PART);
        if (stack.isEmpty() || !BodyPartItemIds.isBodyPartStack(stack)) {
            draftUtf8 = BodypartDefinitionIo.toJson(new BodypartDefinitionJson()).getBytes(StandardCharsets.UTF_8);
            setChanged();
            syncToClients();
            return;
        }
        var id = BodyPartItemIds.partId(stack.getItem());
        if (id == null) return;
        BodyPartConfigManager.INSTANCE.get(id).ifPresentOrElse(def -> {
            draftUtf8 = BodypartDefinitionIo.toJson(def.toJson()).getBytes(StandardCharsets.UTF_8);
        }, () -> {
            BodypartDefinitionJson stub = BodyPartConfigManager.defaultStub(id,
                ((com.sirolf2009.necromancy.item.ItemBodyPart) stack.getItem()).getLocation());
            stub.id = id.toString();
            draftUtf8 = BodypartDefinitionIo.toJson(stub).getBytes(StandardCharsets.UTF_8);
        });
        setChanged();
        syncToClients();
    }

    // ---------------------------------------------------------------- Container / Menu
    @Override public int getContainerSize() { return 1; }
    @Override public boolean isEmpty() { return items.get(SLOT_PART).isEmpty(); }
    @Override public ItemStack getItem(int s) { return items.get(s); }
    @Override public ItemStack removeItem(int s, int n) {
        ItemStack r = ContainerHelper.removeItem(items, s, n);
        if (!r.isEmpty()) {
            setChanged();
            if (s == SLOT_PART) refreshDraftForCurrentSlot();
            syncToClients();
        }
        return r;
    }
    @Override public ItemStack removeItemNoUpdate(int s) { return ContainerHelper.takeItem(items, s); }
    @Override
    public void setItem(int s, ItemStack stk) {
        items.set(s, stk);
        if (stk.getCount() > getMaxStackSize()) stk.setCount(getMaxStackSize());
        setChanged();
        if (s == SLOT_PART) refreshDraftForCurrentSlot();
        syncToClients();
    }
    @Override public boolean stillValid(Player p) { return Container.stillValidBlockEntity(this, p); }
    @Override public void clearContent() { items.clear(); }

    @Override public Component getDisplayName() {
        return Component.translatable("container.necromancy.bodypart_dev");
    }

    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inv, Player p) {
        return new ContainerBodypartDev(id, inv, this);
    }

    // ---------------------------------------------------------------- NBT
    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider lookup) {
        super.loadAdditional(tag, lookup);
        items.clear();
        ContainerHelper.loadAllItems(tag, items, lookup);
        if (tag.contains("DraftUtf8")) {
            draftUtf8 = tag.getByteArray("DraftUtf8");
        }
        previewVisibilityMask = tag.getInt("PreviewVis");
        previewEntityId = tag.getInt("PreviewEnt");
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider lookup) {
        super.saveAdditional(tag, lookup);
        ContainerHelper.saveAllItems(tag, items, lookup);
        tag.putByteArray("DraftUtf8", draftUtf8);
        tag.putInt("PreviewVis", previewVisibilityMask);
        tag.putInt("PreviewEnt", previewEntityId);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider lookup) {
        CompoundTag tag = new CompoundTag();
        ContainerHelper.saveAllItems(tag, items, lookup);
        tag.putByteArray("DraftUtf8", draftUtf8);
        tag.putInt("PreviewVis", previewVisibilityMask);
        return tag;
    }

    @Override
    public void handleUpdateTag(CompoundTag tag, HolderLookup.Provider lookup) {
        super.handleUpdateTag(tag, lookup);
        items.clear();
        ContainerHelper.loadAllItems(tag, items, lookup);
        if (tag.contains("DraftUtf8")) {
            draftUtf8 = tag.getByteArray("DraftUtf8");
        }
        previewVisibilityMask = tag.getInt("PreviewVis");
    }

    @Override
    public @Nullable Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}
