package com.sirolf2009.necromancy.multipart.damage;

import com.sirolf2009.necromancy.multipart.TransformHierarchy;
import com.sirolf2009.necromancy.multipart.part.BodyPartNode;

/**
 * Rolling aggregate for UI / vanilla bridge — root {@link net.minecraft.world.entity.LivingEntity} health should mirror
 * this when multipart damage mode is enabled (implementations choose policy).
 *
 * <p><strong>Sever / dissection:</strong> gameplay detach pipelines remain conceptual-only until surgical tooling lands — see ideas.md (idea&nbsp;2) backlog.</p>
 */
public final class MultipartHealthAggregate {

    private MultipartHealthAggregate() {}

    public static float totalCurrent(TransformHierarchy h) {
        float s = 0f;
        for (BodyPartNode n : h.nodes()) {
            s += n.damageState().currentHealth();
        }
        return s;
    }

    public static float totalMax(TransformHierarchy h) {
        float s = 0f;
        for (BodyPartNode n : h.nodes()) {
            s += n.damageState().maxHealth();
        }
        return s;
    }

    public static float fraction(TransformHierarchy h) {
        float max = totalMax(h);
        return max <= 1e-6f ? 1f : totalCurrent(h) / max;
    }
}
