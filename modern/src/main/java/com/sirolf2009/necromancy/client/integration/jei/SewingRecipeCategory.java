package com.sirolf2009.necromancy.client.integration.jei;

import com.sirolf2009.necromancy.Reference;
import com.sirolf2009.necromancy.block.NecromancyBlocks;
import com.sirolf2009.necromancy.crafting.SewingRecipe;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

public class SewingRecipeCategory implements IRecipeCategory<SewingJeiRecipe> {

    private final RecipeType<SewingJeiRecipe> type;
    private final IDrawable background;
    private final IDrawable icon;

    public SewingRecipeCategory(IGuiHelper guiHelper) {
        this.type = NecromancyJeiRecipeTypes.SEWING;
        this.background = guiHelper.createDrawable(
            Reference.TEXTURES_GUI_JEI_SEWING,
            0,
            0,
            Reference.JEI_SEWING_BG_WIDTH,
            Reference.JEI_SEWING_BG_HEIGHT);
        this.icon = guiHelper.createDrawableItemLike(NecromancyBlocks.SEWING_MACHINE.get());
    }

    @Override
    public RecipeType<SewingJeiRecipe> getRecipeType() {
        return type;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("jei.necromancy.sewing.title");
    }

    @Override
    public IDrawable getBackground() {
        return background;
    }

    @Override
    public IDrawable getIcon() {
        return icon;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, SewingJeiRecipe wrapper, IFocusGroup focuses) {
        SewingRecipe recipe = wrapper.recipe();
        if (recipe.shapeless) {
            builder.setShapeless(4, 4);
        }
        for (int i = 0; i < SewingRecipe.SIZE * SewingRecipe.SIZE; i++) {
            int row = i / SewingRecipe.SIZE;
            int col = i % SewingRecipe.SIZE;
            builder.addInputSlot(col * 18 + 4, row * 18 + 4)
                .addIngredients(recipe.grid[i])
                .setStandardSlotBackground();
        }
        int gridPx = SewingRecipe.SIZE * 18 + 8;
        builder.addOutputSlot(gridPx + 12, gridPx / 2 - 18 / 2)
            .addItemStack(recipe.result)
            .setOutputSlotBackground();
    }
}
