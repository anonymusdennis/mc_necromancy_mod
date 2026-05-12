package com.sirolf2009.necromancy.core.proxy;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import com.sirolf2009.necromancy.client.renderer.RenderIsaac;
import com.sirolf2009.necromancy.client.renderer.RenderMinion;
import com.sirolf2009.necromancy.client.renderer.RenderNightCrawler;
import com.sirolf2009.necromancy.client.renderer.RenderTeddy;
import com.sirolf2009.necromancy.client.renderer.tileentity.TileEntityAltarRenderer;
import com.sirolf2009.necromancy.client.renderer.tileentity.TileEntitySewingRenderer;
import com.sirolf2009.necromancy.entity.EntityIsaacBlood;
import com.sirolf2009.necromancy.entity.EntityIsaacBody;
import com.sirolf2009.necromancy.entity.EntityIsaacHead;
import com.sirolf2009.necromancy.entity.EntityIsaacNormal;
import com.sirolf2009.necromancy.entity.EntityMinion;
import com.sirolf2009.necromancy.entity.EntityNightCrawler;
import com.sirolf2009.necromancy.entity.EntityTeddy;
import com.sirolf2009.necromancy.tileentity.TileEntityAltar;
import com.sirolf2009.necromancy.tileentity.TileEntitySewing;

@SideOnly(Side.CLIENT)
public class ClientProxy extends CommonProxy
{
    @Override
    public void init()
    {
        RenderingRegistry.registerEntityRenderingHandler(EntityMinion.class, RenderMinion::new);
        RenderingRegistry.registerEntityRenderingHandler(EntityIsaacNormal.class, RenderIsaac::new);
        RenderingRegistry.registerEntityRenderingHandler(EntityIsaacBlood.class, RenderIsaac::new);
        RenderingRegistry.registerEntityRenderingHandler(EntityIsaacHead.class, RenderIsaac::new);
        RenderingRegistry.registerEntityRenderingHandler(EntityIsaacBody.class, RenderIsaac::new);
        RenderingRegistry.registerEntityRenderingHandler(EntityNightCrawler.class, RenderNightCrawler::new);
        RenderingRegistry.registerEntityRenderingHandler(EntityTeddy.class, RenderTeddy::new);

        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityAltar.class, new TileEntityAltarRenderer());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntitySewing.class, new TileEntitySewingRenderer());
    }

    @Override
    public int addArmour(String path)
    {
        return RenderingRegistry.addNewArmourRendererPrefix(path);
    }

    @Override
    public void spawnParticle(String name, double posX, double posY, double posZ,
            double motionX, double motionY, double motionZ)
    {
        Minecraft.getMinecraft().effectRenderer.addEffect(
                new com.sirolf2009.necromancy.entity.EntityNecroFX(
                        Minecraft.getMinecraft().world, posX, posY, posZ, motionX, motionY, motionZ));
    }

    @Override
    public void bindTexture(ResourceLocation location)
    {
        Minecraft.getMinecraft().getTextureManager().bindTexture(location);
    }
}
