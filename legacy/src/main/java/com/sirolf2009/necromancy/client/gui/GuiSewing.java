package com.sirolf2009.necromancy.client.gui;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import com.sirolf2009.necromancy.tileentity.TileEntitySewing;

@SideOnly(Side.CLIENT)
public class GuiSewing extends GuiContainer
{
    private static final ResourceLocation TEXTURE = new ResourceLocation("necromancy:textures/gui/sewing.png");

    public GuiSewing(InventoryPlayer inventory, TileEntitySewing sewing)
    {
        super(new ContainerSewing(inventory, sewing));
        xSize = 176;
        ySize = 166;
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY)
    {
        mc.getTextureManager().bindTexture(TEXTURE);
        int x = (width - xSize) / 2;
        int y = (height - ySize) / 2;
        drawTexturedModalRect(x, y, 0, 0, xSize, ySize);
    }
}
