package com.sirolf2009.necromancy.entity.ai;

import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;

/**
 * Like {@link net.minecraft.world.entity.ai.goal.RandomSwimmingGoal} but tuned for
 * minions whose locomotion type is {@code SWIM}: only fires when the minion is
 * actually in water, picks a target that stays in water, and keeps the
 * underlying movement controller speed slow.
 *
 * <p>Used by {@link com.sirolf2009.necromancy.entity.EntityMinion} when the
 * resolved {@link com.sirolf2009.necromancy.entity.MinionAssembly} says the
 * minion should swim (e.g. squid legs on land = static, in water = wander).
 */
public class SwimRandomGoal extends RandomStrollGoal {

    public SwimRandomGoal(PathfinderMob mob, double speed) {
        super(mob, speed, 80, false);
    }

    @Override
    public boolean canUse() {
        return mob.isInWater() && super.canUse();
    }

    @Override
    public boolean canContinueToUse() {
        return mob.isInWater() && super.canContinueToUse();
    }

    @Override
    protected @Nullable Vec3 getPosition() {
        // Use the same default random position picker; the mob's path navigator
        // will refuse to leave water for swim entities by default.
        return DefaultRandomPos.getPos(mob, 10, 7);
    }
}
