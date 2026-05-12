package com.sirolf2009.necromancy.api.feature;

import com.sirolf2009.necromancy.api.BodyPartLocation;
import com.sirolf2009.necromancy.entity.EntityMinion;
import net.minecraft.network.chat.Component;

import java.util.List;

/** Blaze torso flavour: constantly extinguish fire so the stitched torso burns away slower. */
public final class BlazeTorsoFireGuardFeature implements PartFeature {

    public static final BlazeTorsoFireGuardFeature INSTANCE =
        FeatureRegistry.register(new BlazeTorsoFireGuardFeature());

    private BlazeTorsoFireGuardFeature() {}

    @Override
    public String id() {
        return "necromancy:blaze_torso_fire_guard";
    }

    @Override
    public void serverTick(EntityMinion minion, BodyPartLocation slot) {
        if (minion.isOnFire()) minion.clearFire();
    }

    @Override
    public void appendTooltip(BodyPartLocation slot, List<Component> lines) {
        lines.add(Component.translatable("necromancy.feature.blaze_torso_fire_guard.tooltip"));
    }
}
