package com.sirolf2009.necromancy.client.integration.jei;

import com.sirolf2009.necromancy.Reference;
import com.sirolf2009.necromancy.item.NecromancyItems;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

public class GuideRecipeCategory implements IRecipeCategory<GuideJeiRecipe> {

    private static final int TEXT_X = 10;
    private static final int TEXT_Y = 50;
    private static final int TEXT_WIDTH = Reference.JEI_GUIDE_BG_WIDTH - 20;
    private static final int TEXT_COLOR = 0xFF404040;

    private final RecipeType<GuideJeiRecipe> type;
    private final IDrawable background;
    private final IDrawable icon;

    public GuideRecipeCategory(IGuiHelper guiHelper) {
        this.type = NecromancyJeiRecipeTypes.GUIDE;
        this.background = guiHelper.createDrawable(
            Reference.TEXTURES_GUI_JEI_GUIDE,
            0,
            0,
            Reference.JEI_GUIDE_BG_WIDTH,
            Reference.JEI_GUIDE_BG_HEIGHT);
        this.icon = guiHelper.createDrawableItemLike(NecromancyItems.NECRONOMICON.get());
    }

    @Override
    public RecipeType<GuideJeiRecipe> getRecipeType() {
        return type;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("jei.necromancy.guide.title");
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
    public void setRecipe(IRecipeLayoutBuilder builder, GuideJeiRecipe recipe, IFocusGroup focuses) {
        builder.addInputSlot(10, 22)
            .addItemStack(recipe.stack())
            .setStandardSlotBackground();
    }

    @Override
    public void draw(GuideJeiRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics,
        double mouseX, double mouseY) {
        Minecraft mc = Minecraft.getInstance();
        guiGraphics.drawWordWrap(
            mc.font,
            NecromancyGuideTexts.describe(recipe.stack()),
            TEXT_X,
            TEXT_Y,
            TEXT_WIDTH,
            TEXT_COLOR);
    }
}
