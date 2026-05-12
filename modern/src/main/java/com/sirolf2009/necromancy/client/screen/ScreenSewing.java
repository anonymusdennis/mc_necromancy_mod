package com.sirolf2009.necromancy.client.screen;

import com.sirolf2009.necromancy.Reference;
import com.sirolf2009.necromancy.inventory.ContainerSewing;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

/**
 * Sewing Machine screen -- blits the legacy {@code sewinggui.png}.
 *
 * <p>The image is 176x166 like the legacy GUI; the 4x4 grid sits at (8,8) on
 * the texture with 18-pixel cell spacing, the needle/string slots at (95,17)
 * and (95,54), and the result slot at (145,35).
 */
public class ScreenSewing extends AbstractContainerScreen<ContainerSewing> {

    public ScreenSewing(ContainerSewing menu, Inventory inv, Component title) {
        super(menu, inv, title);
        this.imageWidth  = 176;
        this.imageHeight = 166;
        this.inventoryLabelY = this.imageHeight - 94;
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        renderBackground(g, mouseX, mouseY, partialTick);
        super.render(g, mouseX, mouseY, partialTick);
        renderTooltip(g, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics g, float partialTick, int mouseX, int mouseY) {
        int x = (this.width  - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;
        g.blit(Reference.TEXTURES_GUI_SEWING, x, y, 0, 0, this.imageWidth, this.imageHeight);
    }
}
