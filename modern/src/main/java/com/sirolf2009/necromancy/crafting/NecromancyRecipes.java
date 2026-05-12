package com.sirolf2009.necromancy.crafting;

import com.sirolf2009.necromancy.Reference;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Placeholder registry kept for forward-compat: lets future data-driven
 * sewing recipes register via JSON if desired without changing the
 * {@link com.sirolf2009.necromancy.Necromancy} bootstrap.
 *
 * <p>The current port keeps sewing recipes runtime-only via
 * {@link CraftingManagerSewing}, mirroring the 1.7.10 mod 1:1.
 */
public final class NecromancyRecipes {

    public static final DeferredRegister<RecipeType<?>>       RECIPE_TYPES =
        DeferredRegister.create(Registries.RECIPE_TYPE,       Reference.MOD_ID);

    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS =
        DeferredRegister.create(Registries.RECIPE_SERIALIZER, Reference.MOD_ID);

    private NecromancyRecipes() {}
}
