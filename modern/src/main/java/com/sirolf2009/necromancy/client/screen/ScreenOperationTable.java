package com.sirolf2009.necromancy.client.screen;

import com.sirolf2009.necromancy.inventory.ContainerOperationTable;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class ScreenOperationTable extends AbstractContainerScreen<ContainerOperationTable> {

    public ScreenOperationTable(ContainerOperationTable menu, Inventory inv, Component title) {
        super(menu, inv, title);
        imageWidth = 176;
        imageHeight = 166;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;
        graphics.fill(x, y, x + imageWidth, y + imageHeight, 0xFF202026);
        graphics.renderOutline(x, y, imageWidth, imageHeight, 0xFF606068);
    }
}
