package com.sirolf2009.necromancy.particle;

import com.sirolf2009.necromancy.Reference;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.Registries;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

/**
 * {@link DeferredRegister} for our custom particle type, the modern equivalent
 * of legacy {@code EntityNecroFX}.  The actual particle factory is wired in
 * {@link com.sirolf2009.necromancy.client.NecromancyClient} on the client.
 */
public final class NecromancyParticles {

    public static final DeferredRegister<ParticleType<?>> PARTICLES =
        DeferredRegister.create(Registries.PARTICLE_TYPE, Reference.MOD_ID);

    public static final Supplier<SimpleParticleType> NECRO_FX =
        PARTICLES.register("necro_fx", () -> new SimpleParticleType(false));

    private NecromancyParticles() {}
}
