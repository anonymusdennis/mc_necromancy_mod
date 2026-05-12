package com.sirolf2009.necromancy.inventory;

import com.sirolf2009.necromancy.crafting.SewingCraftMatrix;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ResultSlot;
import net.minecraft.world.item.ItemStack;

/**
 * The output slot of the sewing machine.  Reuses vanilla {@link ResultSlot}
 * mechanics for "take == consume one of every input".  When the player picks
 * up the result, we additionally drain one Bone Needle + one String from the
 * sewing machine's requirement slots.
 *
 * <p>Direct port of {@code SlotSewing}.
 */
public class SlotSewingResult extends ResultSlot {

    private final ContainerSewing menu;
    private final SewingCraftMatrix matrix;

    public SlotSewingResult(Player player, ContainerSewing menu, SewingCraftMatrix matrix,
            Container result, int slotIndex, int x, int y) {
        super(player, matrix, result, slotIndex, x, y);
        this.menu = menu;
        this.matrix = matrix;
    }

    @Override
    public void onTake(Player player, ItemStack stack) {
        // Decrement craft grid (per ResultSlot default) and chip away one of
        // each requirement.  We bypass the vanilla shrinking of crafting items
        // because the legacy mod consumed *all* matrix items, not one each.
        for (int i = 0; i < matrix.getContainerSize(); i++) {
            ItemStack s = matrix.getItem(i);
            if (!s.isEmpty()) {
                ItemStack remainder = s.getCraftingRemainingItem();
                s.shrink(1);
                matrix.setItem(i, s.isEmpty() ? remainder : s);
            }
        }
        menu.getSewing().consumeRequirements();
        // Re-evaluate the recipe in case it can be repeated.
        menu.slotsChanged(matrix);
    }
}
