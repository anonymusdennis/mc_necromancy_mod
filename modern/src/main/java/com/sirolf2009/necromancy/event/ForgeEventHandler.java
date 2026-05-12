package com.sirolf2009.necromancy.event;

import com.sirolf2009.necromancy.NecromancyConfig;
import com.sirolf2009.necromancy.block.NecromancyBlocks;
import com.sirolf2009.necromancy.bodypart.MinionCompositeCollision;
import com.sirolf2009.necromancy.entity.EntityIsaacNormal;
import com.sirolf2009.necromancy.entity.EntityMinion;
import com.sirolf2009.necromancy.entity.EntityNightCrawler;
import com.sirolf2009.necromancy.entity.NecromancyEntities;
import com.sirolf2009.necromancy.item.NecromancyItems;
import com.sirolf2009.necromancy.multipart.RootMobEntity;
import com.sirolf2009.necromancy.multipart.damage.MultipartDamageRouter;
import com.sirolf2009.necromancy.multipart.editor.session.MultipartServerTopologyEditService;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.monster.Skeleton;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.living.FinalizeSpawnEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerAboutToStartEvent;

/**
 * Game-bus event listener.  Direct port of legacy {@code ForgeEventHandler}.
 *
 * <ul>
 *     <li>{@link #onFinalizeSpawn(FinalizeSpawnEvent)} -- replace zombies and
 *         skeletons with Necromancy mobs at the configured rarities.</li>
 *     <li>{@link #onLivingDeath(LivingDeathEvent)} -- 7% chance to drop a
 *         random organ on death (matching the legacy 0..6 case branches).</li>
 *     <li>{@link #onItemCrafted(PlayerEvent.ItemCraftedEvent)} -- give back
 *         the bucket / glass bottles consumed by jar/blood-bucket recipes.</li>
 * </ul>
 *
 * <p>The legacy {@code FillBucketEvent} hook was used to right-click empty
 * buckets onto blood blocks; in 1.21.1 that flow is data-driven through the
 * fluid type's bucket item, which {@link NecromancyItems#BUCKET_BLOOD}
 * already provides.  No event hook is needed.
 */
public final class ForgeEventHandler {

    private ForgeEventHandler() {}

    @SubscribeEvent
    public static void onServerAboutToStart(ServerAboutToStartEvent event) {
        com.sirolf2009.necromancy.bodypart.BodyPartConfigManager.INSTANCE.bootServerWritesDefaultsThenReload();
    }

    @SubscribeEvent
    public static void onAttackEntity(AttackEntityEvent event) {
        if (!(event.getTarget() instanceof EntityMinion minion)) return;
        if (!(event.getEntity() instanceof Player player)) return;
        if (player.level().isClientSide()) return;
        var boxes = minion.getCompositeCollisionBoxes();
        if (boxes.isEmpty()) return;
        Vec3 eye = player.getEyePosition(1f);
        double reach = player.blockInteractionRange() + 2.5;
        Vec3 end = eye.add(player.getLookAngle().scale(reach));
        if (minion.useLegacyCollision()) {
            if (!MinionCompositeCollision.segmentHitsAny(boxes, eye, end)) {
                event.setCanceled(true);
            }
            return;
        }
        if (MultipartDamageRouter.findPartAlongSegment(minion.multipartHierarchy(), eye, end) == null) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onFinalizeSpawn(FinalizeSpawnEvent event) {
        if (event.getSpawnType() != MobSpawnType.NATURAL
            && event.getSpawnType() != MobSpawnType.CHUNK_GENERATION
            && event.getSpawnType() != MobSpawnType.STRUCTURE) {
            return;
        }
        Mob entity = event.getEntity();
        if (!(entity instanceof Zombie) && !(entity instanceof Skeleton)) return;

        Level level = entity.level();
        var rng = level.getRandom();
        BlockPos pos = entity.blockPosition();

        int crawlerRarity = NecromancyConfig.RARITY_NIGHT_CRAWLERS.get();
        int isaacRarity   = NecromancyConfig.RARITY_ISAACS.get();
        if (crawlerRarity > 0 && rng.nextInt(crawlerRarity) == 0) {
            event.setSpawnCancelled(true);
            EntityNightCrawler nc = NecromancyEntities.NIGHT_CRAWLER.get().create(level);
            if (nc != null) {
                nc.moveTo(pos, 0F, 0F);
                level.addFreshEntity(nc);
            }
        } else if (isaacRarity > 0 && rng.nextInt(isaacRarity) == 0) {
            event.setSpawnCancelled(true);
            EntityIsaacNormal ein = NecromancyEntities.ISAAC_NORMAL.get().create(level);
            if (ein != null) {
                ein.moveTo(pos, 0F, 0F);
                level.addFreshEntity(ein);
            }
        }
    }

    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        LivingEntity ent = event.getEntity();
        if (ent.level().isClientSide) return;
        if (ent instanceof RootMobEntity) {
            MultipartServerTopologyEditService.clearEntity((ServerLevel) ent.level(), ent.getId());
        }
        // Original mod rolls 0..99 once and drops 1 organ for the 7 cases below.
        // We preserve the same probabilities (1% brains, 1% heart, 4% muscle, 1% lungs).
        switch (ent.level().getRandom().nextInt(100)) {
            case 0       -> drop(ent, NecromancyItems.BRAINS.get().getDefaultInstance());
            case 1       -> drop(ent, NecromancyItems.HEART.get().getDefaultInstance());
            case 2,3,4,5 -> drop(ent, NecromancyItems.MUSCLE.get().getDefaultInstance());
            case 6       -> drop(ent, NecromancyItems.LUNGS.get().getDefaultInstance());
            default      -> { /* no organ drops */ }
        }
    }

    @SubscribeEvent
    public static void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer sp)) return;
        if (sp.level().isClientSide) return;
        MultipartServerTopologyEditService.releaseAllForPlayer(sp.serverLevel(), sp.getUUID());
    }

    private static void drop(LivingEntity ent, ItemStack stack) {
        ent.spawnAtLocation(stack);
    }

    @SubscribeEvent
    public static void onItemCrafted(PlayerEvent.ItemCraftedEvent event) {
        ItemStack item = event.getCrafting();
        Player player = event.getEntity();
        if (item.isEmpty()) return;

        if (item.is(NecromancyItems.JAR_OF_BLOOD.get())) {
            // Player paid a bucket for a jar of blood; refund the bucket like the legacy mod.
            player.getInventory().add(new ItemStack(Items.BUCKET));
        } else if (item.is(NecromancyItems.BUCKET_BLOOD.get())) {
            // Bucket-of-blood crafting -- legacy mod refunded 8 glass bottles.
            player.getInventory().add(new ItemStack(Items.GLASS_BOTTLE, 8));
        }
    }
}
