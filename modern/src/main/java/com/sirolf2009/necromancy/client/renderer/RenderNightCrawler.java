package com.sirolf2009.necromancy.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.sirolf2009.necromancy.Reference;
import com.sirolf2009.necromancy.client.model.NightCrawlerModel;
import com.sirolf2009.necromancy.entity.EntityNightCrawler;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

/**
 * NightCrawler renderer using our custom {@link NightCrawlerModel} -- the
 * legacy mod scaled the model up by 1.4 and lifted it 0.4 blocks; we apply
 * those transforms in {@link #scale} so the model occupies the same in-world
 * footprint as the original mob.
 */
public class RenderNightCrawler extends MobRenderer<EntityNightCrawler, NightCrawlerModel> {

    public RenderNightCrawler(EntityRendererProvider.Context ctx) {
        super(ctx, new NightCrawlerModel(ctx.bakeLayer(NightCrawlerModel.LAYER)), 0.5F);
    }

    @Override
    public ResourceLocation getTextureLocation(EntityNightCrawler e) {
        return Reference.TEXTURE_ENTITY_NIGHT;
    }

    @Override
    protected void scale(EntityNightCrawler entity, PoseStack pose, float partialTick) {
        pose.scale(1.4F, 1.4F, 1.4F);
        pose.translate(0F, -0.4F / 1.4F, 0F);
    }
}
