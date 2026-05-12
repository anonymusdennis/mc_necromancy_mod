package com.sirolf2009.necromancy.api;

import net.minecraft.resources.ResourceLocation;

/**
 * Implemented by a {@link NecroEntityBase} whose head can be used as a skull
 * item placed on top of the summoning altar (slot 1).
 *
 * <p>Direct port of {@code com.sirolf2009.necroapi.ISkull}.
 */
public interface ISkull {

    /** Texture used to render the skull when placed in the altar's skull slot. */
    ResourceLocation getSkullTexture();

    /** Display name shown for this skull in tooltips and creative tabs. */
    String getSkullName();
}
