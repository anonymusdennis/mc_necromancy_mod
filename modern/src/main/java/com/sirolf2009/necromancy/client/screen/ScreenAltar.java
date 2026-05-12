package com.sirolf2009.necromancy.client.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import com.sirolf2009.necromancy.Reference;
import com.sirolf2009.necromancy.inventory.ContainerAltar;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

/**
 * Altar GUI screen.  Just blits the legacy {@code altargui.png} background.
 * The block entity renderer ({@code BlockEntityAltarRenderer}) handles the
 * floating preview minion, so this screen is mostly bookkeeping.
 */
public class ScreenAltar extends AbstractContainerScreen<ContainerAltar> {

    public ScreenAltar(ContainerAltar menu, Inventory inv, Component title) {
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
        g.blit(Reference.TEXTURES_GUI_ALTAR, x, y, 0, 0, this.imageWidth, this.imageHeight);
    }
}
