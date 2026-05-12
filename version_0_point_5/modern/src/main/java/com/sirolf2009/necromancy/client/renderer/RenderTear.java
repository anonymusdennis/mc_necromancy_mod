package com.sirolf2009.necromancy.client.renderer;

import com.sirolf2009.necromancy.entity.EntityTear;
import com.sirolf2009.necromancy.entity.EntityTearBlood;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;

/**
 * Renderers for the two tear projectiles.  Both extend
 * {@link ThrownItemRenderer} so they pick up the item-model JSON we ship at
 * {@code assets/necromancy/models/item/tear.json}.
 */
public final class RenderTear {
    private RenderTear() {}

    public static class Tear extends ThrownItemRenderer<EntityTear> {
        public Tear(EntityRendererProvider.Context ctx) { super(ctx); }
    }

    public static class TearBlood extends ThrownItemRenderer<EntityTearBlood> {
        public TearBlood(EntityRendererProvider.Context ctx) { super(ctx); }
    }
}
