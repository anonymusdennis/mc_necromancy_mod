package com.sirolf2009.necromancy.multipart.part;

import com.sirolf2009.necromancy.multipart.collision.OrientedLocalVolume;
import com.sirolf2009.necromancy.multipart.damage.CriticalZone;
import com.sirolf2009.necromancy.multipart.ecs.MultipartComponent;
import net.minecraft.world.phys.AABB;

/**
 * Hit volume for a bodypart: oriented local volume + conservative axis-aligned bounds for broad-phase.
 */
public interface HitboxComponent extends MultipartComponent {

    AABB localBounds();

    default OrientedLocalVolume localOrientedVolume() {
        return OrientedLocalVolume.fromAxisAlignedBox(localBounds());
    }

    boolean collisionEnabled();

    default float damageMultiplier() {
        return 1f;
    }

    default CriticalZone criticalZone() {
        return CriticalZone.GENERIC;
    }

    record FixedOrientedHitbox(OrientedLocalVolume oriented, boolean enabled, float damageMultiplier,
                               CriticalZone criticalZone) implements HitboxComponent {

        public FixedOrientedHitbox(OrientedLocalVolume oriented, boolean enabled) {
            this(oriented, enabled, 1f, CriticalZone.GENERIC);
        }

        @Override
        public AABB localBounds() {
            return oriented.conservativeLocalAabb();
        }

        @Override
        public OrientedLocalVolume localOrientedVolume() {
            return oriented;
        }

        @Override
        public boolean collisionEnabled() {
            return enabled;
        }

        @Override
        public float damageMultiplier() {
            return damageMultiplier;
        }

        @Override
        public CriticalZone criticalZone() {
            return criticalZone;
        }
    }

    record FixedLocalBox(AABB local, boolean enabled, float damageMultiplier, CriticalZone criticalZone)
        implements HitboxComponent {

        public FixedLocalBox(AABB local, boolean enabled) {
            this(local, enabled, 1f, CriticalZone.GENERIC);
        }

        @Override
        public AABB localBounds() {
            return local;
        }

        @Override
        public boolean collisionEnabled() {
            return enabled;
        }

        @Override
        public float damageMultiplier() {
            return damageMultiplier;
        }

        @Override
        public CriticalZone criticalZone() {
            return criticalZone;
        }
    }
}

