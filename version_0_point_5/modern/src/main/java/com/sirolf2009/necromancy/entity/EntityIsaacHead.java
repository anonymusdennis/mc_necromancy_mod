package com.sirolf2009.necromancy.entity;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;

/**
 * EntityIsaacHead -- terminal stage of the Isaac chain.  Has 40 HP and stops
 * the resurrection cycle (death override is empty), as in
 * legacy {@code EntityIsaacHead}.
 */
public class EntityIsaacHead extends EntityIsaacBlood {

    public EntityIsaacHead(EntityType<? extends EntityIsaacHead> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return EntityIsaacBody.createAttributes()
            .add(Attributes.MAX_HEALTH, 40.0)
            .add(Attributes.ATTACK_DAMAGE, 2.0);
    }

    @Override
    public void die(DamageSource src) {
        // Bypass Blood/Normal's chained spawns: terminal stage drops normally.
        // Replicate behavior of Mob#die directly via the grandparent path.
        super.die(src);
    }
}
