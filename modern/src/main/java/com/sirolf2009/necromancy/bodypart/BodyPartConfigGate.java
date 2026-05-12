package com.sirolf2009.necromancy.bodypart;



import com.sirolf2009.necromancy.item.ItemBodyPart;

import net.minecraft.world.item.ItemStack;



/**

 * Gates crafting/spawning/trading/item-use flows until bodypart JSON exists <strong>and</strong> is {@linkplain BodypartDefinition#validated() validated}

 * (author-approved via bodypart dev Save).

 *

 * <p><strong>Callsites (audit):</strong>

 * <ul>

 *     <li>{@link com.sirolf2009.necromancy.item.ItemBodyPart#use} + hover tooltips.</li>

 *     <li>{@link com.sirolf2009.necromancy.block.entity.BlockEntityAltar#partsConfiguredForSpawn()}.</li>

 *     <li>{@link com.sirolf2009.necromancy.block.BlockAltar} shift-click ritual — creative skips ingredient consumption in {@link com.sirolf2009.necromancy.block.entity.BlockEntityAltar#spawn}, never skips validation.</li>

 *     <li>{@link com.sirolf2009.necromancy.crafting.CraftingManagerSewing#findMatching}.</li>

 *     <li>{@link com.sirolf2009.necromancy.event.NecroVillagerTrades} — librarian trades omit unvalidated parts silently.</li>

 * </ul>

 */

public final class BodyPartConfigGate {



    private BodyPartConfigGate() {}



    /** Why {@link #allows(ItemStack)} failed — diagnostics only (altar/item UX). */

    public enum Reason {

        OK,

        MISSING_JSON,

        NOT_VALIDATED

    }



    /** Bodypart stacks must override disk definition that is {@linkplain BodypartDefinition#validated() validated}. */

    public static boolean allows(ItemStack stack) {

        return reason(stack) == Reason.OK;

    }



    public static boolean allows(ItemBodyPart item) {

        return reason(item) == Reason.OK;

    }



    public static Reason reason(ItemStack stack) {

        if (stack.isEmpty()) return Reason.OK;

        if (!(stack.getItem() instanceof ItemBodyPart)) return Reason.OK;

        var id = BodyPartItemIds.partId(stack.getItem());

        if (id == null) return Reason.MISSING_JSON;

        return BodyPartConfigManager.INSTANCE.get(id)

            .map(def -> def.validated() ? Reason.OK : Reason.NOT_VALIDATED)

            .orElse(Reason.MISSING_JSON);

    }



    public static Reason reason(ItemBodyPart item) {

        var id = BodyPartItemIds.partId(item);

        if (id == null) return Reason.MISSING_JSON;

        return BodyPartConfigManager.INSTANCE.get(id)

            .map(def -> def.validated() ? Reason.OK : Reason.NOT_VALIDATED)

            .orElse(Reason.MISSING_JSON);

    }

}

