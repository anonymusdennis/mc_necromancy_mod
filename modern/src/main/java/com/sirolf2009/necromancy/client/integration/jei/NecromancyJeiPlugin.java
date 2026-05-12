package com.sirolf2009.necromancy.client.integration.jei;

import com.sirolf2009.necromancy.Reference;
import com.sirolf2009.necromancy.block.NecromancyBlocks;
import com.sirolf2009.necromancy.crafting.CraftingManagerSewing;
import com.sirolf2009.necromancy.item.NecromancyItems;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@JeiPlugin
public class NecromancyJeiPlugin implements IModPlugin {

    private static final ResourceLocation UID =
        ResourceLocation.fromNamespaceAndPath(Reference.MOD_ID, "jei_plugin");

    @Override
    public ResourceLocation getPluginUid() {
        return UID;
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        var gui = registration.getJeiHelpers().getGuiHelper();
        registration.addRecipeCategories(new SewingRecipeCategory(gui));
        registration.addRecipeCategories(new GuideRecipeCategory(gui));
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        List<SewingJeiRecipe> recipes = CraftingManagerSewing.getRecipes().stream()
            .map(SewingJeiRecipe::new)
            .toList();
        registration.addRecipes(NecromancyJeiRecipeTypes.SEWING, recipes);

        List<GuideJeiRecipe> guides = new ArrayList<>();
        for (Item item : BuiltInRegistries.ITEM) {
            ResourceLocation id = BuiltInRegistries.ITEM.getKey(item);
            if (id != null && Reference.MOD_ID.equals(id.getNamespace())) {
                guides.add(new GuideJeiRecipe(new ItemStack(item)));
            }
        }
        guides.sort(Comparator.comparing(r -> BuiltInRegistries.ITEM.getKey(r.stack().getItem()).getPath()));
        registration.addRecipes(NecromancyJeiRecipeTypes.GUIDE, guides);
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addRecipeCatalyst(new ItemStack(NecromancyBlocks.SEWING_MACHINE.get()), NecromancyJeiRecipeTypes.SEWING);
        registration.addRecipeCatalyst(new ItemStack(NecromancyItems.NECRONOMICON.get()), NecromancyJeiRecipeTypes.GUIDE);
        registration.addRecipeCatalyst(new ItemStack(NecromancyBlocks.ALTAR.get()), NecromancyJeiRecipeTypes.GUIDE);
    }
}
