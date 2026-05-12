package com.sirolf2009.necromancy.client;

import com.sirolf2009.necromancy.Reference;
import com.sirolf2009.necromancy.client.particle.NecroFXParticle;
import com.sirolf2009.necromancy.block.NecromancyBlocks;
import com.sirolf2009.necromancy.client.model.IsaacHeadModel;
import com.sirolf2009.necromancy.client.model.IsaacNormalModel;
import com.sirolf2009.necromancy.client.model.IsaacSeveredModel;
import com.sirolf2009.necromancy.client.model.NightCrawlerModel;
import com.sirolf2009.necromancy.client.model.ScytheModel;
import com.sirolf2009.necromancy.client.model.TeddyModel;
import com.sirolf2009.necromancy.client.renderer.blockentity.AltarBlockEntityRenderer;
import com.sirolf2009.necromancy.client.renderer.RenderIsaac;
import com.sirolf2009.necromancy.client.renderer.RenderMinion;
import com.sirolf2009.necromancy.client.renderer.RenderNightCrawler;
import com.sirolf2009.necromancy.client.renderer.RenderTear;
import com.sirolf2009.necromancy.client.renderer.RenderTeddy;
import com.sirolf2009.necromancy.client.screen.ScreenAltar;
import com.sirolf2009.necromancy.client.screen.ScreenNecronomicon;
import com.sirolf2009.necromancy.client.screen.ScreenSewing;
import com.sirolf2009.necromancy.entity.NecromancyEntities;
import com.sirolf2009.necromancy.inventory.NecromancyMenus;
import com.sirolf2009.necromancy.particle.NecromancyParticles;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;

/**
 * Client-side bootstrap.  Subscribed to the MOD bus on the {@link Dist#CLIENT}
 * side.  Registers our screens with the menu types and exposes a static
 * helper for opening the Necronomicon screen on right-click.
 */
@EventBusSubscriber(modid = Reference.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class NecromancyClient {

    private NecromancyClient() {}

    @SubscribeEvent
    public static void registerScreens(RegisterMenuScreensEvent event) {
        event.register(NecromancyMenus.ALTAR.get(),  ScreenAltar::new);
        event.register(NecromancyMenus.SEWING.get(), ScreenSewing::new);
    }

    @SubscribeEvent
    public static void registerEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(NecromancyEntities.MINION.get(),         RenderMinion::new);
        event.registerEntityRenderer(NecromancyEntities.TEDDY.get(),          RenderTeddy::new);
        event.registerEntityRenderer(NecromancyEntities.NIGHT_CRAWLER.get(),  RenderNightCrawler::new);
        event.registerEntityRenderer(NecromancyEntities.ISAAC_NORMAL.get(),   RenderIsaac.Normal::new);
        event.registerEntityRenderer(NecromancyEntities.ISAAC_BLOOD.get(),    RenderIsaac.Blood::new);
        event.registerEntityRenderer(NecromancyEntities.ISAAC_HEAD.get(),     RenderIsaac.Head::new);
        event.registerEntityRenderer(NecromancyEntities.ISAAC_BODY.get(),     RenderIsaac.Body::new);
        event.registerEntityRenderer(NecromancyEntities.TEAR.get(),           RenderTear.Tear::new);
        event.registerEntityRenderer(NecromancyEntities.TEAR_BLOOD.get(),     RenderTear.TearBlood::new);

        // Block-entity renderers.  The altar uses its BER to draw the
        // floating preview minion above the block.
        event.registerBlockEntityRenderer(NecromancyBlocks.ALTAR_BE.get(), AltarBlockEntityRenderer::new);
    }

    @SubscribeEvent
    public static void registerParticles(RegisterParticleProvidersEvent event) {
        event.registerSpriteSet(NecromancyParticles.NECRO_FX.get(), NecroFXParticle.Provider::new);
    }

    /**
     * Registers the layer definitions backing the 3D scythe item models.  The
     * actual baking of the {@link net.minecraft.client.model.geom.ModelPart}
     * happens inside the BEWLR on first resource-manager reload.
     */
    @SubscribeEvent
    public static void registerLayers(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(ScytheModel.LAYER_BLOOD,        ScytheModel::createBloodLayer);
        event.registerLayerDefinition(ScytheModel.LAYER_BONE,         ScytheModel::createBoneLayer);
        event.registerLayerDefinition(NightCrawlerModel.LAYER,        NightCrawlerModel::createBodyLayer);
        event.registerLayerDefinition(TeddyModel.LAYER,               TeddyModel::createBodyLayer);
        event.registerLayerDefinition(IsaacNormalModel.LAYER,         IsaacNormalModel::createBodyLayer);
        event.registerLayerDefinition(IsaacHeadModel.LAYER,           IsaacHeadModel::createBodyLayer);
        event.registerLayerDefinition(IsaacSeveredModel.LAYER,        IsaacSeveredModel::createBodyLayer);
    }

    /** Called from the (client side branch of) ItemNecronomicon.use. */
    public static void openNecronomiconScreen() {
        Minecraft mc = Minecraft.getInstance();
        mc.setScreen(new ScreenNecronomicon());
    }
}
