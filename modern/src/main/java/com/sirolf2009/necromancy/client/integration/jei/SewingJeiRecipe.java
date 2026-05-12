package com.sirolf2009.necromancy.client.integration.jei;

import com.sirolf2009.necromancy.crafting.SewingRecipe;

/** Wrapper so JEI can index programmatic sewing recipes without a vanilla JSON codec. */
public record SewingJeiRecipe(SewingRecipe recipe) {}
