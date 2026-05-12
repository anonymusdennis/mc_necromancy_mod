package com.sirolf2009.necromancy.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;
import java.util.List;

/**
 * Goal port of {@code EntityAIScareEntities}.
 *
 * <p>When teddy is in {@link EntityTeddy.EntityState#DEFENDING} mode and a
 * hostile {@link Enemy} comes within {@code seekingRange} blocks, walks toward
 * it; if the hostile is within {@code scaringRange}, the hostile is steered
 * away to a random reachable point.
 */
public class EntityAIScareEntities extends Goal {

    private final EntityTeddy teddy;
    private final float seekingRange;
    private final float scaringRange;
    private LivingEntity target;

    public EntityAIScareEntities(EntityTeddy teddy, float seekingRange, float scaringRange) {
        this.teddy = teddy;
        this.seekingRange = seekingRange;
        this.scaringRange = scaringRange;
        setFlags(EnumSet.of(Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        if (teddy.getEntityState() != EntityTeddy.EntityState.DEFENDING) return false;
        AABB box = teddy.getBoundingBox().inflate(seekingRange);
        List<LivingEntity> hostiles = teddy.level().getEntitiesOfClass(LivingEntity.class, box,
            e -> e instanceof Enemy && e.isAlive());
        target = closest(hostiles);
        return target != null;
    }

    @Override
    public boolean canContinueToUse() {
        return target != null && target.isAlive()
            && teddy.getEntityState() == EntityTeddy.EntityState.DEFENDING
            && teddy.distanceTo(target) <= seekingRange;
    }

    @Override
    public void tick() {
        if (target == null) return;
        teddy.getNavigation().moveTo(target, 0.3);
        if (teddy.distanceTo(target) < scaringRange && target instanceof PathfinderMob mob) {
            Vec3 from = teddy.position();
            Vec3 awayPos = DefaultRandomPos.getPosAway(mob, 16, 7, from);
            if (awayPos != null) {
                PathNavigation nav = mob.getNavigation();
                nav.moveTo(nav.createPath(BlockPos.containing(awayPos), 0), 0.4);
            }
        }
    }

    private LivingEntity closest(List<LivingEntity> targets) {
        LivingEntity best = null;
        double bestDist = seekingRange + 1.0;
        for (LivingEntity e : targets) {
            double d = teddy.distanceTo(e);
            if (d < bestDist) { bestDist = d; best = e; }
        }
        return best;
    }
}
