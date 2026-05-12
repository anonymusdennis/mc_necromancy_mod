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
 *   <li>{@link #MODE_ATTACH_POS} – primary attachment ox/oy/oz</li>
 *   <li>{@link #MODE_ATTACH_ROT} – primary attachment euler yaw/pitch/roll (degrees)</li>
 * </ul>
 */
public final class ItemDevKnob extends Item {

    public static final int MODE_HITBOX_POS   = 0;
    public static final int MODE_HITBOX_SIZE  = 1;
    public static final int MODE_VISUAL_OFFSET = 2;
    public static final int MODE_VISUAL_ROT   = 3;
    public static final int MODE_ATTACH_POS   = 4;
    public static final int MODE_ATTACH_ROT   = 5;
    public static final int MODE_MAX          = MODE_ATTACH_ROT;

    private static final String TAG_MODE = "DevKnobMode";
    private static final String TAG_BX   = "DevKnobBlockX";
    private static final String TAG_BY   = "DevKnobBlockY";
    private static final String TAG_BZ   = "DevKnobBlockZ";

    public ItemDevKnob() {
        super(new Item.Properties().stacksTo(1));
    }

    /** Create a knob item for the given mode linked to a specific dev block. */
    public static ItemStack create(int mode, BlockPos blockPos) {
        ItemStack stack = new ItemStack(NecromancyItems.DEV_KNOB.get());
        CompoundTag tag = new CompoundTag();
        tag.putInt(TAG_MODE, mode);
        tag.putInt(TAG_BX, blockPos.getX());
        tag.putInt(TAG_BY, blockPos.getY());
        tag.putInt(TAG_BZ, blockPos.getZ());
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

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext ctx, List<Component> tooltip, TooltipFlag flag) {
        int mode = getMode(stack);
        BlockPos pos = getBlockPos(stack);
        tooltip.add(Component.translatable("item.necromancy.dev_knob.mode." + mode));
        tooltip.add(Component.literal("Block: " + pos.toShortString()));
        tooltip.add(Component.translatable("item.necromancy.dev_knob.hint"));
    }
}
