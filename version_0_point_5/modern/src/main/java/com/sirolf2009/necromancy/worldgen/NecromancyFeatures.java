package com.sirolf2009.necromancy.worldgen;

import com.sirolf2009.necromancy.Reference;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

/**
 * {@link DeferredRegister} for our world-gen {@link Feature}s.
 *
 * <p>The {@link NetherChaliceFeature} alone is registered here.  The cemetery
 * structure was originally a custom village component; we ship a JSON Jigsaw
 * structure for it instead (see {@code data/necromancy/worldgen/structure}).
 */
public final class NecromancyFeatures {

    public static final DeferredRegister<Feature<?>> FEATURES =
        DeferredRegister.create(Registries.FEATURE, Reference.MOD_ID);

    public static final Supplier<Feature<NoneFeatureConfiguration>> NETHER_CHALICE =
        FEATURES.register("nether_chalice",
            () -> new NetherChaliceFeature(NoneFeatureConfiguration.CODEC));

    private NecromancyFeatures() {}
}
