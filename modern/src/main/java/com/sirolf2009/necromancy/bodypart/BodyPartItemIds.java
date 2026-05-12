package com.sirolf2009.necromancy.bodypart;

import com.sirolf2009.necromancy.api.BodyPartLocation;
import com.sirolf2009.necromancy.item.ItemBodyPart;
import com.sirolf2009.necromancy.item.NecromancyItems;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.Locale;

/** Maps stacks/items/adapters to stable bodypart config ids (same as item registry names). */
public final class BodyPartItemIds {

    private BodyPartItemIds() {}

    public static ResourceLocation partId(Item item) {
        return BuiltInRegistries.ITEM.getKey(item);
    }

    public static ResourceLocation partId(ItemStack stack) {
        return stack.isEmpty() ? null : partId(stack.getItem());
    }

    /**
     * Heuristic used before registry Bootstrap resolves DeferredRegisters —
     * matches {@link NecromancyItems#addParts} convention ({@code zombie_arm}, …).
     */
    public static ResourceLocation inferredPartId(String mobName, BodyPartLocation loc) {
        String pk = loc.partKey();
        if (loc == BodyPartLocation.ArmLeft || loc == BodyPartLocation.ArmRight) {
            pk = "Arm";
        }
        String path = mobName.toLowerCase(Locale.ROOT) + "_" + pk.toLowerCase(Locale.ROOT);
        return ResourceLocation.fromNamespaceAndPath(com.sirolf2009.necromancy.Reference.MOD_ID, path);
    }

    public static boolean isBodyPartStack(ItemStack stack) {
        return !stack.isEmpty() && stack.getItem() instanceof ItemBodyPart;
    }
}
