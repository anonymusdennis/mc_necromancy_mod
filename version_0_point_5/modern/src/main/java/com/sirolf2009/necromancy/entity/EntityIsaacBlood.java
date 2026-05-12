package com.sirolf2009.necromancy.entity;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;
import net.minecraft.sounds.SoundEvents;

/**
 * EntityIsaacBlood -- second stage of the Isaac chain.  Stronger ranged
 * attack (red-dust tear, 6 damage) and 75 HP.  Splits into
 * {@link EntityIsaacHead} and {@link EntityIsaacBody} on death.
 */
public class EntityIsaacBlood extends EntityIsaacNormal {

    public EntityIsaacBlood(EntityType<? extends EntityIsaacBlood> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return EntityIsaacBody.createAttributes()
            .add(Attributes.MAX_HEALTH, 75.0)
            .add(Attributes.ATTACK_DAMAGE, 4.0);
    }

    @Override
    public void performRangedAttack(LivingEntity target, float distance) {
        var rl = com.sirolf2009.necromancy.Reference.rl("tear");
        var snd = BuiltInRegistries.SOUND_EVENT.get(rl);
        if (snd != null) playSound(snd, 1.0F, 1.0F / (random.nextFloat() * 0.4F + 0.8F));
        else             playSound(SoundEvents.GHAST_HURT, 1.0F, 1.0F);
        EntityTearBlood t = new EntityTearBlood(NecromancyEntities.TEAR_BLOOD.get(), level());
        t.init(this, target);
        level().addFreshEntity(t);
    }

    @Override
    public void die(DamageSource src) {
        super.die(src);
        if (!level().isClientSide && this.getClass() == EntityIsaacBlood.class) {
            EntityIsaacHead head = NecromancyEntities.ISAAC_HEAD.get().create(level());
            EntityIsaacBody body = NecromancyEntities.ISAAC_BODY.get().create(level());
            if (head != null) {
                head.moveTo(getX(), getY() + 1, getZ(), getYRot(), getXRot());
                level().addFreshEntity(head);
            }
            if (body != null) {
                body.moveTo(getX(), getY(), getZ(), getYRot(), getXRot());
                level().addFreshEntity(body);
            }
        }
    }
}
