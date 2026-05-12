package com.sirolf2009.necromancy.api.feature;

import com.sirolf2009.necromancy.api.BodyPartLocation;
import com.sirolf2009.necromancy.entity.EntityMinion;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

import java.util.List;

/**
 * A pluggable per-bodypart ability.
 *
 * <p>Each {@link com.sirolf2009.necromancy.api.NecroEntityBase} adapter can
 * declare a list of features per slot via
 * {@link com.sirolf2009.necromancy.api.NecroEntityBase#features(BodyPartLocation)}.
 * The runtime ({@link EntityMinion}) attaches those features to the slot when
 * the adapter is bolted on, ticks them server-side, and routes player
 * interaction / attacks through them.
 *
 * <p>Lifecycle:
 * <pre>
 *   slot adapter changes
 *      -&gt; old features detached  (onDetach)
 *      -&gt; new features attached  (onAttach)
 *   every server tick
 *      -&gt; serverTick on every attached feature
 *   player right-clicks the minion
 *      -&gt; onPlayerInteract is called for every attached feature
 *         until one returns a non-PASS result
 *   minion lands a melee attack
 *      -&gt; onAttack is called for every attached feature
 *   minion takes damage (after {@link EntityMinion#hurt} succeeds)
 *      -&gt; onHurt is called for every attached feature
 *   tooltip / JEI rendering of the body part item
 *      -&gt; appendTooltip
 * </pre>
 *
 * <p>All hooks default to no-ops so feature implementations only override
 * what they care about.
 */
public interface PartFeature {

    /** Globally unique id, e.g. {@code "necromancy:saddle"}.  Used for registry lookup. */
    String id();

    /** Called when this feature has been bolted onto the given slot. */
    default void onAttach(EntityMinion minion, BodyPartLocation slot) {}

    /** Called when the slot's adapter has just been replaced or removed. */
    default void onDetach(EntityMinion minion, BodyPartLocation slot) {}

    /** Called every server tick while the feature is attached. */
    default void serverTick(EntityMinion minion, BodyPartLocation slot) {}

    /**
     * Called when the player right-clicks the minion.  Return any non-PASS
     * value to short-circuit further handling.
     */
    default InteractionResult onPlayerInteract(EntityMinion minion, BodyPartLocation slot,
                                               Player player, InteractionHand hand) {
        return InteractionResult.PASS;
    }

    /** Called after the minion successfully damages a target with melee. */
    default void onAttack(EntityMinion minion, BodyPartLocation slot, LivingEntity target) {}

    /**
     * Called on the server after {@link net.minecraft.world.entity.LivingEntity#hurt}
     * returns {@code true}.  Use for reactive abilities (short-range teleport,
     * thorns-like feedback, …).
     */
    default void onHurt(EntityMinion minion, BodyPartLocation slot, DamageSource source, float amount) {}

    /**
     * Append lines to a body-part item's tooltip / JEI page.  Called from the
     * client only; safe to read static config.
     */
    default void appendTooltip(BodyPartLocation slot, List<Component> lines) {}
}
