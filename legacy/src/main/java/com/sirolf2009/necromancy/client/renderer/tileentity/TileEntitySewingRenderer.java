package com.sirolf2009.necromancy.client.renderer.tileentity;

import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import com.sirolf2009.necromancy.tileentity.TileEntitySewing;

@SideOnly(Side.CLIENT)
public class TileEntitySewingRenderer extends TileEntitySpecialRenderer<TileEntitySewing>
{
    @Override
    public void render(TileEntitySewing te, double x, double y, double z, float partialTicks, int destroyStage, float alpha)
    {
        // Sewing machine model rendering - placeholder for custom model
    }
}
