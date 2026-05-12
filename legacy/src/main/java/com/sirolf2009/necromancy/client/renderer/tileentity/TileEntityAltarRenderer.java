package com.sirolf2009.necromancy.client.renderer.tileentity;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import com.sirolf2009.necromancy.entity.EntityMinion;
import com.sirolf2009.necromancy.tileentity.TileEntityAltar;

@SideOnly(Side.CLIENT)
public class TileEntityAltarRenderer extends TileEntitySpecialRenderer<TileEntityAltar>
{
    @Override
    public void render(TileEntityAltar te, double x, double y, double z, float partialTicks, int destroyStage, float alpha)
    {
        EntityMinion preview = te.getPreviewEntity();
        if (preview == null || preview.getBodyParts() == null) return;

        GlStateManager.pushMatrix();
        GlStateManager.translate((float) x + 0.5F, (float) y + 1.5F, (float) z + 0.5F);
        GlStateManager.scale(0.75F, 0.75F, 0.75F);
        GlStateManager.rotate(180F, 0F, 1F, 0F);

        net.minecraft.client.Minecraft.getMinecraft().getRenderManager()
                .renderEntity(preview, 0, 0, 0, 0, partialTicks, false);

        GlStateManager.popMatrix();
    }
}
