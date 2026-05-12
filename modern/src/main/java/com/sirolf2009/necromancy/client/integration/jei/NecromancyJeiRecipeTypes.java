package com.sirolf2009.necromancy.client.integration.jei;

import com.sirolf2009.necromancy.Reference;
import mezz.jei.api.recipe.RecipeType;

public final class NecromancyJeiRecipeTypes {

    public static final RecipeType<SewingJeiRecipe> SEWING =
        RecipeType.create(Reference.MOD_ID, "sewing", SewingJeiRecipe.class);

    public static final RecipeType<GuideJeiRecipe> GUIDE =
        RecipeType.create(Reference.MOD_ID, "guide", GuideJeiRecipe.class);

    private NecromancyJeiRecipeTypes() {}
}
