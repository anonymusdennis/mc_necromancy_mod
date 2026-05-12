package com.sirolf2009.necromancy.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.core.particles.SimpleParticleType;

/**
 * Modern equivalent of legacy {@code EntityNecroFX}.
 *
 * <p>The 1.7.10 mod hand-rolled a tessellator that sampled UVs from the
 * "particles.png" sheet.  In 1.21.1 we feed the particle through the standard
 * {@link TextureSheetParticle} pipeline using the sprite set bound to the
 * {@link com.sirolf2009.necromancy.NecromancyParticles#NECRO_FX} type, which
     * is wired to {@code assets/necromancy/particles/necro_fx.json}.
 */
public class NecroFXParticle extends TextureSheetParticle {

    private final SpriteSet sprites;

    protected NecroFXParticle(ClientLevel level, double x, double y, double z,
                              double mx, double my, double mz, SpriteSet sprites) {
        super(level, x, y, z, 0, 0, 0);
        this.sprites = sprites;
        this.xd = mx * 0.01 + this.xd;
        this.yd = my * 0.01 + this.yd;
        this.zd = mz * 0.01 + this.zd;
        float gray = (float)(Math.random() * 0.5);
        this.rCol = this.gCol = this.bCol = gray;
        this.quadSize *= 0.75F;
        this.lifetime = (int) (8.0 / (Math.random() * 0.8 + 0.2) + 64);
        this.hasPhysics = false;
        this.gravity = 0.0F;
        setSpriteFromAge(sprites);
    }

    @Override
    public ParticleRenderType getRenderType() { return ParticleRenderType.PARTICLE_SHEET_OPAQUE; }

    @Override
    public void tick() {
        super.tick();
        if (this.age++ >= this.lifetime) {
            remove();
        } else {
            setSpriteFromAge(sprites);
        }
        if (this.onGround) {
            this.xd *= 0.7;
            this.zd *= 0.7;
        }
        this.xd *= 0.99999;
        this.yd *= 0.99999;
        this.zd *= 0.99999;
    }

    public static class Provider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;
        public Provider(SpriteSet sprites) { this.sprites = sprites; }
        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel level,
                                       double x, double y, double z,
                                       double mx, double my, double mz) {
            return new NecroFXParticle(level, x, y, z, mx, my, mz, sprites);
        }
    }
}
