package com.sirolf2009.necromancy.item;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * Brain on a Stick -- worn-and-tear marker that lets a player pacify minions
 * within line-of-sight (legacy "stop attacking my pet" behaviour).  Right-click
 * empty hand pulses a calming wave; the stick takes 1 damage per use.
 *
 * <p>Re-implementation note: the original 1.7.10 code did not actually attach
 * any side effect to right-clicking the stick -- the effect was triggered by
 * the {@code KeyHandlerNecro} key bind.  We move the right-click trigger here
 * so the item is functional standalone, while keeping the keybind path intact.
 */
public class ItemBrainOnAStick extends Item {

    public ItemBrainOnAStick() {
        super(new Item.Properties().stacksTo(1).durability(64));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide) {
            // Server-side: consume one durability and let the calm wave logic
            // run from EntityMinion.tick where it watches for nearby holders.
            stack.hurtAndBreak(1, player, hand == InteractionHand.MAIN_HAND
                ? net.minecraft.world.entity.EquipmentSlot.MAINHAND
                : net.minecraft.world.entity.EquipmentSlot.OFFHAND);
        }
        player.getCooldowns().addCooldown(this, 20);
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }
}
