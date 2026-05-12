package com.sirolf2009.necromancy.client.renderer;

import com.sirolf2009.necromancy.Reference;
import com.sirolf2009.necromancy.client.model.TeddyModel;
import com.sirolf2009.necromancy.entity.EntityTeddy;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

/**
 * Teddy renderer using the custom {@link TeddyModel}.  Texture is shipped
 * at {@code textures/entities/teddy.png}.
 */
public class RenderTeddy extends MobRenderer<EntityTeddy, TeddyModel> {
    public RenderTeddy(EntityRendererProvider.Context ctx) {
        super(ctx, new TeddyModel(ctx.bakeLayer(TeddyModel.LAYER)), 0.3F);
        this.shadowRadius = 0.3F;
    }

    @Override
    public ResourceLocation getTextureLocation(EntityTeddy entity) {
        return Reference.TEXTURE_ENTITY_TEDDY;
    }
}
