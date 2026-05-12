package com.sirolf2009.necromancy.block.entity;

import com.sirolf2009.necromancy.api.BodyPartLocation;
import com.sirolf2009.necromancy.api.NecroEntityBase;
import com.sirolf2009.necromancy.api.NecroEntityRegistry;
import com.sirolf2009.necromancy.block.NecromancyBlocks;
import com.sirolf2009.necromancy.bodypart.BodyPartConfigGate;
import com.sirolf2009.necromancy.bodypart.BodyPartItemIds;
import com.sirolf2009.necromancy.entity.EntityMinion;
import com.sirolf2009.necromancy.inventory.ContainerAltar;
import com.sirolf2009.necromancy.item.NecromancyItems;
import net.minecraft.core.BlockPos;
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
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Block entity for {@link com.sirolf2009.necromancy.block.BlockAltar}.
 *
 * <p>Holds the 7 inventory slots that match the legacy mod 1:1:
 * <pre>
 *   0 -> Jar of Blood
 *   1 -> Soul in a Jar
 *   2 -> Head body part
 *   3 -> Torso body part
 *   4 -> Legs body part
 *   5 -> Right Arm body part
 *   6 -> Left  Arm body part
 * </pre>
 *
 * <p>The block entity is also a {@link MenuProvider} so right-clicking the
 * altar opens its container.  When the player shift-right-clicks, the altar
 * runs {@link #spawn(Player)} which performs the summon ritual and consumes
 * one of every slot.
 *
 * <p>The "preview minion" feature from the legacy mod (the small floating
 * minion above the altar with the parts you currently have selected) is
 * resolved by the BE renderer using {@link #getPreviewParts()}.
 */
public class BlockEntityAltar extends BlockEntity implements Container, MenuProvider {

    public static final int SIZE = 7;
    private final NonNullList<ItemStack> items = NonNullList.withSize(SIZE, ItemStack.EMPTY);

    /** Cached body-part info for the preview minion (keyed by location). */
    private final java.util.EnumMap<BodyPartLocation, NecroEntityBase> previewMobs =
        new java.util.EnumMap<>(BodyPartLocation.class);
    private final ItemStack[] previousParts = new ItemStack[5];

    public BlockEntityAltar(BlockPos pos, BlockState state) {
        super(NecromancyBlocks.ALTAR_BE.get(), pos, state);
    }

    // ---------------------------------------------------------------- Container
    @Override public int getContainerSize() { return SIZE; }
    @Override public boolean isEmpty()      { for (ItemStack s : items) if (!s.isEmpty()) return false; return true; }
    @Override public ItemStack getItem(int s) { return items.get(s); }
    @Override public ItemStack removeItem(int s, int n) { return ContainerHelper.removeItem(items, s, n); }
    @Override public ItemStack removeItemNoUpdate(int s) { return ContainerHelper.takeItem(items, s); }
    @Override public void setItem(int s, ItemStack stk) {
        items.set(s, stk);
        if (stk.getCount() > getMaxStackSize()) stk.setCount(getMaxStackSize());
        setChanged();
    }
    @Override public boolean stillValid(Player p)      { return Container.stillValidBlockEntity(this, p); }
    @Override public void clearContent()                { items.clear(); }

    // ---------------------------------------------------------------- MenuProvider
    @Override public Component getDisplayName() { return Component.translatable("container.necromancy.altar"); }
    @Override public AbstractContainerMenu createMenu(int id, Inventory inv, Player p) {
        return new ContainerAltar(id, inv, this);
    }

    // ---------------------------------------------------------------- Save/load
    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider lookup) {
        super.loadAdditional(tag, lookup);
        items.clear();
        ContainerHelper.loadAllItems(tag, items, lookup);
    }
    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider lookup) {
        super.saveAdditional(tag, lookup);
        ContainerHelper.saveAllItems(tag, items, lookup);
    }
    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider lookup) {
        CompoundTag tag = new CompoundTag();
        ContainerHelper.saveAllItems(tag, items, lookup);
        return tag;
    }
    @Override
    public @Nullable Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    // ---------------------------------------------------------------- Summon logic

    /** Identical to legacy {@code TileEntityAltar.canSpawn()}. */
    public boolean canSpawn() {
        ItemStack jar  = getItem(0);
        ItemStack soul = getItem(1);
        return !jar.isEmpty() && jar.is(NecromancyItems.JAR_OF_BLOOD.get())
            && !soul.isEmpty() && soul.is(NecromancyItems.SOUL_IN_A_JAR.get());
    }

    /** Non-empty bodypart stacks must have validated bodypart JSON on disk (see {@link BodyPartConfigGate}). */
    public boolean partsConfiguredForSpawn() {
        for (int i = 2; i <= 6; i++) {
            ItemStack s = getItem(i);
            if (!s.isEmpty() && !BodyPartConfigGate.allows(s)) {
                return false;
            }
        }
        return true;
    }

    /** Human-readable list of altar slots that currently fail {@link BodyPartConfigGate} (for chat). */
    public Component summarizeBlockedBodyParts() {
        List<String> segments = new ArrayList<>();
        BodyPartLocation[] order = {
            BodyPartLocation.Head, BodyPartLocation.Torso, BodyPartLocation.Legs,
            BodyPartLocation.ArmRight, BodyPartLocation.ArmLeft
        };
        int[] slots = {2, 3, 4, 5, 6};
        for (int i = 0; i < order.length; i++) {
            ItemStack s = getItem(slots[i]);
            if (s.isEmpty() || BodyPartConfigGate.allows(s)) continue;
            var id = BodyPartItemIds.partId(s.getItem());
            String idStr = id != null ? id.toString() : s.getDescriptionId();
            String detail = switch (BodyPartConfigGate.reason(s)) {
                case NOT_VALIDATED -> "not_validated";
                case MISSING_JSON -> "missing_json";
                default -> "blocked";
            };
            segments.add(order[i].name() + "=" + idStr + "[" + detail + "]");
        }
        return Component.literal(String.join(", ", segments));
    }

    /**
     * Performs the summoning ritual: instantiates an {@link EntityMinion} with
     * the body parts currently in slots 2-6, places it on top of the altar and
     * decrements one of every input.  Mirrors legacy {@code TileEntityAltar.spawn}.
     */
    public void spawn(Player user) {
        if (level == null || level.isClientSide) return;

        if (!partsConfiguredForSpawn()) {
            if (user != null) {
                user.displayClientMessage(
                    Component.translatable("message.necromancy.altar.parts_unconfigured", summarizeBlockedBodyParts()), true);
            }
            return;
        }

        EntityMinion minion = new EntityMinion(
            com.sirolf2009.necromancy.entity.NecromancyEntities.MINION.get(), level);
        minion.setPos(worldPosition.getX() + 0.5, worldPosition.getY() + 1.0, worldPosition.getZ() + 0.5);

        // Apply body part labels (these drive the renderer + attribute mods).
        applyPart(minion, BodyPartLocation.Head,     getItem(2));
        applyPart(minion, BodyPartLocation.Torso,    getItem(3));
        applyPart(minion, BodyPartLocation.Legs,     getItem(4));
        applyPart(minion, BodyPartLocation.ArmRight, getItem(5));
        applyPart(minion, BodyPartLocation.ArmLeft,  getItem(6));

        // Apply attribute scaling per body part.
        for (BodyPartLocation loc : BodyPartLocation.values()) {
            NecroEntityBase mob = matchMob(slotForLocation(loc), loc);
            if (mob != null) mob.setAttributes(minion, loc);
        }

        if (user != null) minion.tame(user);
        level.addFreshEntity(minion);

        if (!user.getAbilities().instabuild) {
            for (int i = 0; i < SIZE; i++) removeItem(i, 1);
        }
        // Track minion count via persistent player data
        var data = user.getPersistentData();
        data.putInt("necromancy_minions", data.getInt("necromancy_minions") + 1);

        level.playSound(null, worldPosition,
            net.minecraft.sounds.SoundEvents.PORTAL_TRIGGER, net.minecraft.sounds.SoundSource.BLOCKS,
            1F, 1F);
    }

    private ItemStack slotForLocation(BodyPartLocation loc) {
        return switch (loc) {
            case Head     -> getItem(2);
            case Torso    -> getItem(3);
            case Legs     -> getItem(4);
            case ArmRight -> getItem(5);
            case ArmLeft  -> getItem(6);
        };
    }

    /**
     * Finds the {@link NecroEntityBase} whose body part item matches {@code stack}
     * for the given {@code location}.  Returns null if there is no match.
     */
    public static NecroEntityBase matchMob(ItemStack stack, BodyPartLocation location) {
        if (stack.isEmpty()) return null;
        for (NecroEntityBase mob : NecroEntityRegistry.registeredEntities.values()) {
            ItemStack candidate = switch (location) {
                case Head     -> mob.headItem;
                case Torso    -> mob.torsoItem;
                case ArmLeft, ArmRight -> mob.armItem;
                case Legs     -> mob.legItem;
            };
            if (!candidate.isEmpty() && ItemStack.isSameItem(candidate, stack)) return mob;
        }
        return null;
    }

    private void applyPart(EntityMinion minion, BodyPartLocation loc, ItemStack stack) {
        NecroEntityBase mob = matchMob(stack, loc);
        if (mob != null) {
            minion.setBodyPartName(loc, mob.mobName);
        } else {
            minion.setBodyPartName(loc, "");
        }
    }

    // ---------------------------------------------------------------- Preview rendering helpers

    /** Returns true if the parts in slots 2-6 changed since the last query. */
    public boolean refreshPreview() {
        boolean changed = false;
        for (int i = 0; i < 5; i++) {
            ItemStack now  = getItem(i + 2);
            ItemStack prev = previousParts[i];
            if (prev == null && now.isEmpty()) continue;
            if (prev == null || !ItemStack.matches(prev, now)) {
                previousParts[i] = now.copy();
                changed = true;
            }
        }
        return changed;
    }

    public java.util.EnumMap<BodyPartLocation, NecroEntityBase> getPreviewParts() {
        previewMobs.clear();
        previewMobs.put(BodyPartLocation.Head,     matchMob(getItem(2), BodyPartLocation.Head));
        previewMobs.put(BodyPartLocation.Torso,    matchMob(getItem(3), BodyPartLocation.Torso));
        previewMobs.put(BodyPartLocation.Legs,     matchMob(getItem(4), BodyPartLocation.Legs));
        previewMobs.put(BodyPartLocation.ArmRight, matchMob(getItem(5), BodyPartLocation.ArmRight));
        previewMobs.put(BodyPartLocation.ArmLeft,  matchMob(getItem(6), BodyPartLocation.ArmLeft));
        previewMobs.values().removeIf(java.util.Objects::isNull);
        return previewMobs;
    }

    public static void clientTick(Level level, BlockPos pos, BlockState state, BlockEntityAltar be) {
        // The renderer will pull data from the BE on demand.  No tick work required.
    }
}
