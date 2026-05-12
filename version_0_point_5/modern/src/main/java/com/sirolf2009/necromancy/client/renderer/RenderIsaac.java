package com.sirolf2009.necromancy.client.renderer;

import com.sirolf2009.necromancy.Reference;
import com.sirolf2009.necromancy.client.model.IsaacHeadModel;
import com.sirolf2009.necromancy.client.model.IsaacNormalModel;
import com.sirolf2009.necromancy.client.model.IsaacSeveredModel;
import com.sirolf2009.necromancy.entity.EntityIsaacBlood;
import com.sirolf2009.necromancy.entity.EntityIsaacBody;
import com.sirolf2009.necromancy.entity.EntityIsaacHead;
import com.sirolf2009.necromancy.entity.EntityIsaacNormal;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

/**
 * Isaac renderers.  The legacy mod hand-rolled three near-identical models
 * (Normal, Severed, Head); we ported each into its own
 * {@link com.sirolf2009.necromancy.client.model} class with the original 64x32
 * UV layout intact.
 *
 * <ul>
 *   <li>{@code EntityIsaacNormal} / {@code EntityIsaacBlood} share
 *   {@link IsaacNormalModel} (biped with a 10x9x8 head).</li>
 *   <li>{@code EntityIsaacBody} uses {@link IsaacSeveredModel} (biped with a
 *   tiny neck stub instead of a head).</li>
 *   <li>{@code EntityIsaacHead} uses {@link IsaacHeadModel} (just the head
 *   plus four trailing neck cubes).</li>
 * </ul>
 */
public final class RenderIsaac {
    private RenderIsaac() {}

    public static class Normal extends HumanoidMobRenderer<EntityIsaacNormal, IsaacNormalModel<EntityIsaacNormal>> {
        public Normal(EntityRendererProvider.Context ctx) {
            super(ctx, new IsaacNormalModel<>(ctx.bakeLayer(IsaacNormalModel.LAYER)), 0.5F);
        }
        @Override public ResourceLocation getTextureLocation(EntityIsaacNormal e) { return Reference.TEXTURE_ENTITY_ISAAC; }
    }

    public static class Blood extends HumanoidMobRenderer<EntityIsaacBlood, IsaacNormalModel<EntityIsaacBlood>> {
        public Blood(EntityRendererProvider.Context ctx) {
            super(ctx, new IsaacNormalModel<>(ctx.bakeLayer(IsaacNormalModel.LAYER)), 0.5F);
        }
        @Override public ResourceLocation getTextureLocation(EntityIsaacBlood e) { return Reference.TEXTURE_ENTITY_ISAAC_BLOOD; }
    }

    public static class Head extends MobRenderer<EntityIsaacHead, IsaacHeadModel> {
        public Head(EntityRendererProvider.Context ctx) {
            super(ctx, new IsaacHeadModel(ctx.bakeLayer(IsaacHeadModel.LAYER)), 0.3F);
        }
        @Override public ResourceLocation getTextureLocation(EntityIsaacHead e) { return Reference.TEXTURE_ENTITY_ISAAC_BLOOD; }
    }

    public static class Body extends HumanoidMobRenderer<EntityIsaacBody, IsaacSeveredModel<EntityIsaacBody>> {
        public Body(EntityRendererProvider.Context ctx) {
            super(ctx, new IsaacSeveredModel<>(ctx.bakeLayer(IsaacSeveredModel.LAYER)), 0.5F);
        }
        @Override public ResourceLocation getTextureLocation(EntityIsaacBody e) { return Reference.TEXTURE_ENTITY_ISAAC_BLOOD; }
    }
}
