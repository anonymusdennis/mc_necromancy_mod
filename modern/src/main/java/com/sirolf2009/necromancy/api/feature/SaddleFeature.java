package com.sirolf2009.necromancy.api.feature;

import com.sirolf2009.necromancy.api.BodyPartLocation;
import com.sirolf2009.necromancy.api.ISaddleAble;
import com.sirolf2009.necromancy.entity.EntityMinion;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/**
 * Reference implementation of a {@link PartFeature}: lets a saddleable torso
 * adapter wear a vanilla saddle.
 *
 * <p>Adapters that implement {@link ISaddleAble} (e.g. cow, pig, spider, squid)
 * register an instance of this feature for the {@link BodyPartLocation#Torso}
 * slot.  When attached, it:
 * <ul>
 *     <li>accepts a held vanilla {@link Items#SADDLE saddle} on right-click and
 *         marks the minion saddled;</li>
 *     <li>accepts vanilla shears (placeholder for "remove saddle") to clear
 *         the flag and drop the saddle item;</li>
 *     <li>otherwise mounts the player when right-clicked while already
 *         saddled (so the minion behaves like a vanilla pig under saddle).</li>
 * </ul>
 *
 * <p>The saddle texture overlay is still drawn by {@link com.sirolf2009.necromancy.client.renderer.MinionAssembler}
 * which queries {@link ISaddleAble#getSaddleTexture()} when {@code isSaddled()}
 * returns true.  This feature only handles the gameplay side.
 */
public final class SaddleFeature implements PartFeature {

    /** Single shared instance; the registry deduplicates by id. */
    public static final SaddleFeature INSTANCE =
        FeatureRegistry.register(new SaddleFeature());

    private SaddleFeature() {}

    @Override public String id() { return "necromancy:saddle"; }

    @Override
    public InteractionResult onPlayerInteract(EntityMinion minion, BodyPartLocation slot,
                                              Player player, InteractionHand hand) {
        if (slot != BodyPartLocation.Torso) return InteractionResult.PASS;
        // Only saddleable torsos are affected.
        if (!(minion.getAssembly().torso() instanceof ISaddleAble)) return InteractionResult.PASS;

        ItemStack held = player.getItemInHand(hand);
        if (!minion.isSaddled() && held.is(Items.SADDLE)) {
            minion.setSaddled(true);
            if (!player.getAbilities().instabuild) held.shrink(1);
            return InteractionResult.sidedSuccess(minion.level().isClientSide);
        }
        if (minion.isSaddled() && held.is(Items.SHEARS)) {
            minion.setSaddled(false);
            if (!minion.level().isClientSide) {
                minion.spawnAtLocation(new ItemStack(Items.SADDLE));
            }
            return InteractionResult.sidedSuccess(minion.level().isClientSide);
        }
        if (minion.isSaddled() && held.isEmpty() && !player.isShiftKeyDown()) {
            // Mount.  Vanilla rideability gating still applies (pose, water...).
            if (!minion.level().isClientSide && minion.isOwnedBy(player)) {
                player.startRiding(minion);
            }
            return InteractionResult.sidedSuccess(minion.level().isClientSide);
        }
        return InteractionResult.PASS;
    }
}
