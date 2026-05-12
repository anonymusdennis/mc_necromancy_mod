package com.sirolf2009.necromancy.item;

import com.sirolf2009.necromancy.entity.NecromancyEntities;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;

import java.util.List;

/**
 * "Soul Heart" spawner egg -- when right-clicked on a block, spawns a random
 * one of the three custom mobs (Isaac Normal / Teddy / Night Crawler).
 *
 * <p>Direct port of {@code ItemSpawner}.
 */
public class ItemSpawner extends Item {

    public ItemSpawner() {
        super(new Item.Properties().stacksTo(16));
    }

    @Override
    public InteractionResult useOn(UseOnContext ctx) {
        Level level  = ctx.getLevel();
        Player player = ctx.getPlayer();
        if (level.isClientSide || player == null) return InteractionResult.SUCCESS;

        BlockPos pos = ctx.getClickedPos();
        Direction dir = ctx.getClickedFace();
        BlockPos spawnPos = pos.relative(dir);
        RandomSource rng = level.getRandom();

        List<EntityType<?>> options = List.of(
            NecromancyEntities.ISAAC_NORMAL.get(),
            NecromancyEntities.TEDDY.get(),
            NecromancyEntities.NIGHT_CRAWLER.get());
        EntityType<?> type = options.get(rng.nextInt(options.size()));

        Entity entity = type.spawn((ServerLevel) level, spawnPos, MobSpawnType.SPAWN_EGG);
        if (entity != null && !player.getAbilities().instabuild) {
            ctx.getItemInHand().shrink(1);
        }
        return InteractionResult.SUCCESS;
    }
}
