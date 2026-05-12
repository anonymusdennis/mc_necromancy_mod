package com.sirolf2009.necromancy.entity;

import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

/**
 * EntityTearBlood -- "reddust" tear used by {@link EntityIsaacBlood}.
 *
 * <p>Direct port of legacy {@code EntityTearBlood}: 6 damage and red-dust
 * particles instead of water splash.
 */
public class EntityTearBlood extends EntityTear {
    @SuppressWarnings("unchecked")
    public EntityTearBlood(EntityType<? extends EntityTearBlood> type, Level level) {
        super((EntityType<? extends EntityTear>) type, level);
        damage = 6F;
        particle = new DustParticleOptions(new Vector3f(1.0F, 0.0F, 0.0F), 1.0F);
    }
    @Override
    protected net.minecraft.world.item.Item getDefaultItem() {
        return com.sirolf2009.necromancy.item.NecromancyItems.TEAR_BLOOD.get();
    }
}
