package com.sirolf2009.necromancy.item;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;

/**
 * Scythe weapon -- has two tiers: BLOOD (cheap, lifesteal) and BONE (expensive,
 * straight damage).  Always tries to capture the target's soul on the killing
 * blow when the wielder carries a glass bottle.
 *
 * <p>Direct behavioural port of the legacy {@code ItemScythe}.  The legacy
 * BLOOD tier had attack damage 2 and bone tier 4 + base sword damage; we use
 * the same numbers via a custom {@link Tier}.
 */
public class ItemScythe extends SwordItem {

    /** Cheap, regular scythe (iron-tier mining + 2 damage bonus). */
    public static final Tier TIER_BLOOD = new Tier() {
        @Override public int   getUses()              { return 666; }
        @Override public float getSpeed()             { return 7F; }
        @Override public float getAttackDamageBonus() { return 2F; }
        @Override public int   getEnchantmentValue()  { return 9; }
        @Override public TagKey<Block> getIncorrectBlocksForDrops() { return BlockTags.INCORRECT_FOR_IRON_TOOL; }
        @Override public net.minecraft.world.item.crafting.Ingredient getRepairIngredient() {
            return net.minecraft.world.item.crafting.Ingredient.of(Items.IRON_INGOT);
        }
    };

    /** Heavy bone-tier scythe (diamond mining + 4 damage bonus). */
    public static final Tier TIER_BONE = new Tier() {
        @Override public int   getUses()              { return 666; }
        @Override public float getSpeed()             { return 7F; }
        @Override public float getAttackDamageBonus() { return 4F; }
        @Override public int   getEnchantmentValue()  { return 9; }
        @Override public TagKey<Block> getIncorrectBlocksForDrops() { return BlockTags.INCORRECT_FOR_DIAMOND_TOOL; }
        @Override public net.minecraft.world.item.crafting.Ingredient getRepairIngredient() {
            return net.minecraft.world.item.crafting.Ingredient.of(Items.BONE);
        }
    };

    private final boolean isBone;

    public ItemScythe(Tier tier, boolean isBone) {
        super(tier, new Properties().attributes(SwordItem.createAttributes(tier, 3, -2.4F)));
        this.isBone = isBone;
    }

    public boolean isBone() { return isBone; }

    /**
     * Hooks our custom {@link com.sirolf2009.necromancy.client.renderer.ScytheItemRenderer}
     * (a 3D BEWLR) into both scythe tiers.  Vanilla still loads the JSON model
     * for tooltips and missing-resource fallback, but the actual draw goes
     * through the BEWLR.
     */
    @Override
    public void initializeClient(java.util.function.Consumer<net.neoforged.neoforge.client.extensions.common.IClientItemExtensions> consumer) {
        consumer.accept(new net.neoforged.neoforge.client.extensions.common.IClientItemExtensions() {
            private com.sirolf2009.necromancy.client.renderer.ScytheItemRenderer renderer;
            @Override
            public net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer getCustomRenderer() {
                // The cast is safe -- ScytheItemRenderer extends BEWLR.
                if (renderer == null) {
                    var mc = net.minecraft.client.Minecraft.getInstance();
                    renderer = new com.sirolf2009.necromancy.client.renderer.ScytheItemRenderer(
                        mc.getBlockEntityRenderDispatcher(), mc.getEntityModels());
                    renderer.onResourceManagerReload(mc.getResourceManager());
                }
                return renderer;
            }
        });
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        // Damage the scythe.
        stack.hurtAndBreak(1, attacker, net.minecraft.world.entity.EquipmentSlot.MAINHAND);

        // Lifesteal for the cheap blood tier (legacy behaviour).
        if (!isBone && attacker instanceof Player p && target.getHealth() > 0) {
            p.heal(1F);
        }

        // Capture the soul on the killing blow if the player has a glass bottle.
        if (target.getHealth() <= 0 && attacker instanceof Player player) {
            int slot = player.getInventory().findSlotMatchingItem(new ItemStack(Items.GLASS_BOTTLE));
            if (slot != -1) {
                player.getInventory().removeItem(slot, 1);
                player.getInventory().add(new ItemStack(NecromancyItems.SOUL_IN_A_JAR.get()));

                if (target.level().isClientSide) {
                    var rng = target.getRandom();
                    for (int i = 0; i < 30; i++) {
                        target.level().addParticle(ParticleTypes.SOUL,
                            target.getX(), target.getY(), target.getZ(),
                            rng.nextDouble() / 36.0,
                            rng.nextDouble() / 36.0,
                            rng.nextDouble() / 36.0);
                    }
                }
                target.setDeltaMovement(0, 10000, 0);
            }
        }
        return super.hurtEnemy(stack, target, attacker);
    }
}
