package com.sirolf2009.necromancy.api.feature;

import com.sirolf2009.necromancy.api.BodyPartLocation;
import com.sirolf2009.necromancy.entity.EntityMinion;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;

import java.util.List;

/**
 * Enderman skull flavour: when the minion takes damage it occasionally short-hop
 * teleports away from trouble (vanilla enderman sound + {@link EntityMinion#randomTeleport}).
 */
public final class EnderHeadBlinkTeleportFeature implements PartFeature {

    public static final EnderHeadBlinkTeleportFeature INSTANCE =
        FeatureRegistry.register(new EnderHeadBlinkTeleportFeature());

    private EnderHeadBlinkTeleportFeature() {}

    @Override
    public String id() {
        return "necromancy:ender_head_blink";
    }

    @Override
    public void onHurt(EntityMinion minion, BodyPartLocation slot, DamageSource source, float amount) {
        if (amount <= 0 || minion.level().isClientSide()) return;
        if (minion.getRandom().nextFloat() > 0.14F) return;
        var rng = minion.getRandom();
        for (int i = 0; i < 48; i++) {
            double x = minion.getX() + (rng.nextDouble() - 0.5) * 28;
            double y = minion.getY() + rng.nextInt(12) - 4;
            double z = minion.getZ() + (rng.nextDouble() - 0.5) * 28;
            if (minion.randomTeleport(x, y, z, true)) {
                minion.resetFallDistance();
                minion.playSound(SoundEvents.ENDERMAN_TELEPORT, 1F, 1F);
                break;
            }
        }
    }

    @Override
    public void appendTooltip(BodyPartLocation slot, List<Component> lines) {
        lines.add(Component.translatable("necromancy.feature.ender_head_blink.tooltip"));
    }
}
