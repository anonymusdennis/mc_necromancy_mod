package com.sirolf2009.necromancy.inventory;

import com.sirolf2009.necromancy.block.NecromancyBlocks;
import com.sirolf2009.necromancy.block.entity.BlockEntityAltar;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import com.sirolf2009.necromancy.api.BodyPartLocation;
import com.sirolf2009.necromancy.item.ItemBodyPart;
import com.sirolf2009.necromancy.item.NecromancyItems;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import java.util.function.Predicate;

/**
 * Container/menu for the Summoning Altar.
 *
 * <p>Slot layout matches the legacy {@code ContainerAltar} byte-for-byte:
 * <pre>
 *   0 -> Jar of Blood     (26, 40)
 *   1 -> Soul in a Jar    (134, 39)
 *   2 -> Head             (80, 19)
 *   3 -> Torso            (80, 36)
 *   4 -> Legs             (80, 53)
 *   5 -> Arm Right        (63, 36)
 *   6 -> Arm Left         (97, 36)
 *  7..33 -> Player main inventory
 *  34..42 -> Hotbar
 * </pre>
 */
public class ContainerAltar extends AbstractContainerMenu {

    private final Container altar;
    private final ContainerLevelAccess access;

    public ContainerAltar(int id, Inventory playerInv, RegistryFriendlyByteBuf buf) {
        this(id, playerInv, lookupAltar(playerInv, buf.readBlockPos()));
    }

    public ContainerAltar(int id, Inventory playerInv, BlockEntityAltar altar) {
        super(NecromancyMenus.ALTAR.get(), id);
        this.altar  = altar;
        this.access = ContainerLevelAccess.create(altar.getLevel(), altar.getBlockPos());

        // Slot 0 / 1: ritual ingredients.  Slots 2-6: body parts (with
        // location-specific filtering so users cannot drop a Head into the
        // Legs slot etc).
        addSlot(typedSlot(0,  26, 40, s -> s.is(NecromancyItems.JAR_OF_BLOOD.get())));
        addSlot(typedSlot(1, 134, 39, s -> s.is(NecromancyItems.SOUL_IN_A_JAR.get())));
        addSlot(typedSlot(2,  80, 19, bodyPartFor(BodyPartLocation.Head)));
        addSlot(typedSlot(3,  80, 36, bodyPartFor(BodyPartLocation.Torso)));
        addSlot(typedSlot(4,  80, 53, bodyPartFor(BodyPartLocation.Legs)));
        addSlot(typedSlot(5,  63, 36, bodyPartFor(BodyPartLocation.ArmRight)));
        addSlot(typedSlot(6,  97, 36, bodyPartFor(BodyPartLocation.ArmLeft)));

        // Player inventory + hotbar
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlot(new Slot(playerInv, col + row * 9 + 9, 8 + col * 18, 84 + row * 18));
            }
        }
        for (int col = 0; col < 9; col++) {
            addSlot(new Slot(playerInv, col, 8 + col * 18, 142));
        }
    }

    /** A Slot that delegates {@link Slot#mayPlace} to a {@link Predicate}. */
    private Slot typedSlot(int idx, int x, int y, Predicate<ItemStack> filter) {
        return new Slot(altar, idx, x, y) {
            @Override public boolean mayPlace(ItemStack stack) { return filter.test(stack); }
        };
    }

    /**
     * Builds a predicate matching only body-part items intended for the given
     * location.  Arm slots accept either ArmLeft- or ArmRight-tagged items
     * because adapters share a single "Arm" item per mob.
     */
    private static Predicate<ItemStack> bodyPartFor(BodyPartLocation target) {
        return stack -> {
            Item it = stack.getItem();
            if (!(it instanceof ItemBodyPart bp)) return false;
            BodyPartLocation actual = bp.getLocation();
            if (target == BodyPartLocation.ArmLeft || target == BodyPartLocation.ArmRight) {
                return actual == BodyPartLocation.ArmLeft || actual == BodyPartLocation.ArmRight;
            }
            return actual == target;
        };
    }

    private static BlockEntityAltar lookupAltar(Inventory inv, BlockPos pos) {
        BlockEntity be = inv.player.level().getBlockEntity(pos);
        if (be instanceof BlockEntityAltar a) return a;
        // Fall back to a phantom so the client constructor never crashes.
        return new BlockEntityAltar(pos, NecromancyBlocks.ALTAR.get().defaultBlockState());
    }

    public BlockEntityAltar getAltar() {
        return altar instanceof BlockEntityAltar a ? a : null;
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(access, player, NecromancyBlocks.ALTAR.get());
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = slots.get(index);
        if (!slot.hasItem()) return ItemStack.EMPTY;

        ItemStack stack = slot.getItem();
        ItemStack copy  = stack.copy();
        // Slots 0..6 -> player inventory; player inventory -> appropriate altar slot
        if (index <= 6) {
            if (!moveItemStackTo(stack, 7, 43, true)) return ItemStack.EMPTY;
        } else {
            // Default: try to put body parts into the body part slots, blood
            // into 0, soul into 1.  We let the user re-organise by hand for
            // anything else.
            if (stack.is(NecromancyItems.JAR_OF_BLOOD.get())) {
                if (!moveItemStackTo(stack, 0, 1, false)) return ItemStack.EMPTY;
            } else if (stack.is(NecromancyItems.SOUL_IN_A_JAR.get())) {
                if (!moveItemStackTo(stack, 1, 2, false)) return ItemStack.EMPTY;
            } else if (stack.getItem() instanceof ItemBodyPart bp) {
                // Route the body part to its specific location slot.
                int target = switch (bp.getLocation()) {
                    case Head     -> 2;
                    case Torso    -> 3;
                    case Legs     -> 4;
                    case ArmRight -> 5;
                    case ArmLeft  -> 6;
                };
                if (!moveItemStackTo(stack, target, target + 1, false)) {
                    // Fall back: arms can swap between left and right.
                    if (target == 5 || target == 6) {
                        int alt = target == 5 ? 6 : 5;
                        if (!moveItemStackTo(stack, alt, alt + 1, false)) return ItemStack.EMPTY;
                    } else {
                        return ItemStack.EMPTY;
                    }
                }
            } else {
                return ItemStack.EMPTY;
            }
        }

        if (stack.isEmpty()) slot.set(ItemStack.EMPTY);
        else slot.setChanged();
        if (stack.getCount() == copy.getCount()) return ItemStack.EMPTY;
        slot.onTake(player, stack);
        return copy;
    }
}
