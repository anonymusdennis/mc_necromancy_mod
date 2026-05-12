package com.sirolf2009.necromancy.item;

import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;

import java.util.List;

/**
 * Developer tuning knob: when held outside any GUI and the player scrolls, adjusts one parameter group
 * in the linked bodypart dev block's draft. The active axis (X/Y/Z or yaw/pitch/roll) is determined by
 * the player's view direction; modifier keys control step size.
 *
 * <p>Modes:
 * <ul>
 *   <li>{@link #MODE_HITBOX_POS} – hitbox center ox/oy/oz</li>
 *   <li>{@link #MODE_HITBOX_SIZE} – hitbox full extents sx/sy/sz</li>
 *   <li>{@link #MODE_VISUAL_OFFSET} – visual offset dx/dy/dz</li>
 *   <li>{@link #MODE_VISUAL_ROT} – visual rotation yaw/pitch/roll (degrees)</li>
 *   <li>{@link #MODE_ATTACH_POS} – primary attachment ox/oy/oz (socket 0)</li>
 *   <li>{@link #MODE_ATTACH_ROT} – primary attachment euler yaw/pitch/roll (socket 0)</li>
 *   <li>{@link #MODE_SCALE} – visual scale scaleX/scaleY/scaleZ</li>
 *   <li>{@link #MODE_SOCKET_POS} – attachment socket ox/oy/oz, socket selected by {@link #getSocketIndex}</li>
 *   <li>{@link #MODE_SOCKET_ROT} – attachment socket euler yaw/pitch/roll, socket selected by {@link #getSocketIndex}</li>
 * </ul>
 */
public final class ItemDevKnob extends Item {

    public static final int MODE_HITBOX_POS    = 0;
    public static final int MODE_HITBOX_SIZE   = 1;
    public static final int MODE_VISUAL_OFFSET = 2;
    public static final int MODE_VISUAL_ROT    = 3;
    public static final int MODE_ATTACH_POS    = 4;
    public static final int MODE_ATTACH_ROT    = 5;
    public static final int MODE_SCALE         = 6;
    public static final int MODE_SOCKET_POS    = 7;
    public static final int MODE_SOCKET_ROT    = 8;
    public static final int MODE_MAX           = MODE_SOCKET_ROT;

    /** Number of indexed socket knobs available (one per socket slot 0–8). */
    public static final int SOCKET_KNOB_COUNT = 9;

    private static final String TAG_MODE       = "DevKnobMode";
    private static final String TAG_BX         = "DevKnobBlockX";
    private static final String TAG_BY         = "DevKnobBlockY";
    private static final String TAG_BZ         = "DevKnobBlockZ";
    private static final String TAG_SOCKET_IDX = "DevKnobSocketIdx";

    public ItemDevKnob() {
        super(new Item.Properties().stacksTo(1));
    }

    /** Create a knob item for the given mode linked to a specific dev block (socket index 0). */
    public static ItemStack create(int mode, BlockPos blockPos) {
        return create(mode, blockPos, 0);
    }

    /** Create a knob item for the given mode and socket index linked to a specific dev block. */
    public static ItemStack create(int mode, BlockPos blockPos, int socketIndex) {
        ItemStack stack = new ItemStack(NecromancyItems.DEV_KNOB.get());
        CompoundTag tag = new CompoundTag();
        tag.putInt(TAG_MODE, mode);
        tag.putInt(TAG_BX, blockPos.getX());
        tag.putInt(TAG_BY, blockPos.getY());
        tag.putInt(TAG_BZ, blockPos.getZ());
        tag.putInt(TAG_SOCKET_IDX, socketIndex);
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
        return stack;
    }

    public static int getMode(ItemStack stack) {
        CustomData cd = stack.get(DataComponents.CUSTOM_DATA);
        return cd != null ? cd.copyTag().getInt(TAG_MODE) : MODE_HITBOX_POS;
    }

    public static BlockPos getBlockPos(ItemStack stack) {
        CustomData cd = stack.get(DataComponents.CUSTOM_DATA);
        if (cd == null) return BlockPos.ZERO;
        CompoundTag tag = cd.copyTag();
        return new BlockPos(tag.getInt(TAG_BX), tag.getInt(TAG_BY), tag.getInt(TAG_BZ));
    }

    /** Returns the socket index (0-based) stored in this knob, used for {@link #MODE_SOCKET_POS}/{@link #MODE_SOCKET_ROT}. */
    public static int getSocketIndex(ItemStack stack) {
        CustomData cd = stack.get(DataComponents.CUSTOM_DATA);
        return cd != null ? cd.copyTag().getInt(TAG_SOCKET_IDX) : 0;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext ctx, List<Component> tooltip, TooltipFlag flag) {
        int mode = getMode(stack);
        BlockPos pos = getBlockPos(stack);
        int socketIdx = getSocketIndex(stack);
        if (mode == MODE_SOCKET_POS || mode == MODE_SOCKET_ROT) {
            tooltip.add(Component.translatable("item.necromancy.dev_knob.mode." + mode, socketIdx));
        } else {
            tooltip.add(Component.translatable("item.necromancy.dev_knob.mode." + mode));
        }
        tooltip.add(Component.literal("Block: " + pos.toShortString()));
        tooltip.add(Component.translatable("item.necromancy.dev_knob.hint"));
    }
}
