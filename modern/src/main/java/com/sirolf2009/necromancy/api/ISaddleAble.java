package com.sirolf2009.necromancy.api;

import net.minecraft.resources.ResourceLocation;

/**
 * Implemented by a {@link NecroEntityBase} whose body parts can be saddled
 * and ridden by the player.
 *
 * <p>Direct port of {@code com.sirolf2009.necroapi.ISaddleAble}.
 */
public interface ISaddleAble {

    /** Texture overlay drawn on top of the body to show the saddle. */
    ResourceLocation getSaddleTexture();

    /**
     * The Y-position adjustment (in world units) for the rider on this mob's
     * back, so that small minions like a pig don't levitate the rider.
     */
    default float riderHeight() { return 0.5F; }
}
