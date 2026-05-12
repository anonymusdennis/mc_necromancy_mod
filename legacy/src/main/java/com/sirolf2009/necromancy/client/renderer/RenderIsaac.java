package com.sirolf2009.necromancy.client.renderer;

import net.minecraft.client.renderer.entity.RenderBiped;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderIsaac extends RenderBiped<EntityLivingBase>
{
    private static final ResourceLocation TEXTURE = new ResourceLocation("necromancy:textures/entity/isaac.png");

    public RenderIsaac(RenderManager manager)
    {
        super(manager, new net.minecraft.client.model.ModelBiped(), 0.5F);
    }

    @Override
    protected ResourceLocation getEntityTexture(EntityLivingBase entity) { return TEXTURE; }
}
