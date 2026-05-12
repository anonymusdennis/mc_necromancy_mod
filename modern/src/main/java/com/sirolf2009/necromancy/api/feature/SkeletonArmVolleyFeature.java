package com.sirolf2009.necromancy.api.feature;

import com.sirolf2009.necromancy.api.BodyPartLocation;
import com.sirolf2009.necromancy.entity.EntityMinion;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;

import java.util.List;

/**
 * Skeleton arm flavour: after a successful melee swing the minion also looses a
 * cheap arrow toward the victim (cooldown-gated, non-pickup).
 */
public final class SkeletonArmVolleyFeature implements PartFeature {

    public static final SkeletonArmVolleyFeature INSTANCE =
        FeatureRegistry.register(new SkeletonArmVolleyFeature());

    private static final String COOLDOWN_KEY = "necromancy_skeleton_arrow_cd";

    private SkeletonArmVolleyFeature() {}

    @Override
    public String id() {
        return "necromancy:skeleton_arm_volley";
    }

    @Override
    public void onAttack(EntityMinion minion, BodyPartLocation slot, LivingEntity target) {
        if (minion.level().isClientSide()) return;
        CompoundTag data = minion.getPersistentData();
        int now = minion.tickCount;
        if (now < data.getInt(COOLDOWN_KEY)) return;
        data.putInt(COOLDOWN_KEY, now + 22);

        Arrow arrow = new Arrow(minion.level(), minion, new ItemStack(Items.ARROW), ItemStack.EMPTY);
        arrow.pickup = AbstractArrow.Pickup.DISALLOWED;
        double dx = target.getX() - minion.getX();
        double dy = target.getEyeY() - minion.getEyeY();
        double dz = target.getZ() - minion.getZ();
        Vec3 dir = new Vec3(dx, dy, dz).normalize();
        arrow.setPos(minion.getX(), minion.getEyeY() - 0.1, minion.getZ());
        arrow.shoot(dir.x, dir.y, dir.z, 1.55F, 10F);
        minion.level().addFreshEntity(arrow);
        minion.playSound(SoundEvents.SKELETON_SHOOT, 0.9F, 1F / (minion.getRandom().nextFloat() * 0.4F + 0.8F));
    }

    @Override
    public void appendTooltip(BodyPartLocation slot, List<Component> lines) {
        lines.add(Component.translatable("necromancy.feature.skeleton_arm_volley.tooltip"));
    }
}
