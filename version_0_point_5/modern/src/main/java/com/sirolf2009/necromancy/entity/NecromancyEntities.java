package com.sirolf2009.necromancy.entity;

import com.sirolf2009.necromancy.Reference;
import net.minecraft.core.registries.Registries;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.SpawnPlacementTypes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.common.DeferredSpawnEggItem;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

/**
 * Central {@link DeferredRegister} for every custom mob, projectile and the
 * mod's {@link SoundEvent}s.
 *
 * <p>Direct port of legacy {@code RegistryNecromancyEntities}.  Default
 * attributes and spawn placements are registered through dedicated bus
 * listeners called from {@link com.sirolf2009.necromancy.Necromancy}.
 */
public final class NecromancyEntities {

    public static final DeferredRegister<EntityType<?>> ENTITIES =
        DeferredRegister.create(Registries.ENTITY_TYPE, Reference.MOD_ID);

    public static final DeferredRegister<SoundEvent> SOUND_EVENTS =
        DeferredRegister.create(Registries.SOUND_EVENT, Reference.MOD_ID);

    // ---------------------------------------------------------------------
    // Sound events (sounds.json maps these ids to .ogg files).
    // ---------------------------------------------------------------------
    public static final Supplier<SoundEvent> SND_NIGHTCRAWLER_HOWL =
        SOUND_EVENTS.register("nightcrawler.howl",
            () -> SoundEvent.createVariableRangeEvent(Reference.SOUND_NIGHTCRAWLER_HOWL));
    public static final Supplier<SoundEvent> SND_NIGHTCRAWLER_SCREAM =
        SOUND_EVENTS.register("nightcrawler.scream",
            () -> SoundEvent.createVariableRangeEvent(Reference.SOUND_NIGHTCRAWLER_SCREAM));
    public static final Supplier<SoundEvent> SND_SPAWN =
        SOUND_EVENTS.register("spawn",
            () -> SoundEvent.createVariableRangeEvent(Reference.SOUND_SPAWN));
    public static final Supplier<SoundEvent> SND_TEAR =
        SOUND_EVENTS.register("tear",
            () -> SoundEvent.createVariableRangeEvent(Reference.SOUND_TEAR));

    // ---------------------------------------------------------------------
    // Entity types
    // ---------------------------------------------------------------------
    public static final Supplier<EntityType<EntityMinion>> MINION =
        ENTITIES.register("minion",
            () -> EntityType.Builder.<EntityMinion>of(EntityMinion::new, MobCategory.CREATURE)
                .sized(0.6F, 1.8F)
                .clientTrackingRange(8)
                .updateInterval(3)
                .build("minion"));

    public static final Supplier<EntityType<EntityNightCrawler>> NIGHT_CRAWLER =
        ENTITIES.register("nightcrawler",
            () -> EntityType.Builder.<EntityNightCrawler>of(EntityNightCrawler::new, MobCategory.MONSTER)
                .sized(0.6F, 1.0F)
                .clientTrackingRange(8)
                .updateInterval(3)
                .build("nightcrawler"));

    public static final Supplier<EntityType<EntityTeddy>> TEDDY =
        ENTITIES.register("teddy",
            () -> EntityType.Builder.<EntityTeddy>of(EntityTeddy::new, MobCategory.CREATURE)
                .sized(0.6F, 0.8F)
                .clientTrackingRange(8)
                .updateInterval(3)
                .build("teddy"));

    public static final Supplier<EntityType<EntityIsaacNormal>> ISAAC_NORMAL =
        ENTITIES.register("isaac_normal",
            () -> EntityType.Builder.<EntityIsaacNormal>of(EntityIsaacNormal::new, MobCategory.MONSTER)
                .sized(0.6F, 1.8F)
                .clientTrackingRange(8)
                .updateInterval(3)
                .build("isaac_normal"));

    public static final Supplier<EntityType<EntityIsaacBlood>> ISAAC_BLOOD =
        ENTITIES.register("isaac_blood",
            () -> EntityType.Builder.<EntityIsaacBlood>of(EntityIsaacBlood::new, MobCategory.MONSTER)
                .sized(0.6F, 1.8F)
                .clientTrackingRange(8)
                .updateInterval(3)
                .build("isaac_blood"));

    public static final Supplier<EntityType<EntityIsaacHead>> ISAAC_HEAD =
        ENTITIES.register("isaac_head",
            () -> EntityType.Builder.<EntityIsaacHead>of(EntityIsaacHead::new, MobCategory.MONSTER)
                .sized(0.6F, 1.0F)
                .clientTrackingRange(8)
                .updateInterval(3)
                .build("isaac_head"));

    public static final Supplier<EntityType<EntityIsaacBody>> ISAAC_BODY =
        ENTITIES.register("isaac_body",
            () -> EntityType.Builder.<EntityIsaacBody>of(EntityIsaacBody::new, MobCategory.MONSTER)
                .sized(0.6F, 1.8F)
                .clientTrackingRange(8)
                .updateInterval(3)
                .build("isaac_body"));

    public static final Supplier<EntityType<EntityTear>> TEAR =
        ENTITIES.register("tear",
            () -> EntityType.Builder.<EntityTear>of(EntityTear::new, MobCategory.MISC)
                .sized(0.25F, 0.25F)
                .clientTrackingRange(8)
                .updateInterval(10)
                .build("tear"));

    public static final Supplier<EntityType<EntityTearBlood>> TEAR_BLOOD =
        ENTITIES.register("tear_blood",
            () -> EntityType.Builder.<EntityTearBlood>of(EntityTearBlood::new, MobCategory.MISC)
                .sized(0.25F, 0.25F)
                .clientTrackingRange(8)
                .updateInterval(10)
                .build("tear_blood"));

    // ---------------------------------------------------------------------
    // Spawn eggs (registered via the existing items DeferredRegister).
    // ---------------------------------------------------------------------
    public static final DeferredItem<DeferredSpawnEggItem> SPAWN_EGG_MINION =
        com.sirolf2009.necromancy.item.NecromancyItems.REGISTRY.register("minion_spawn_egg",
            () -> new DeferredSpawnEggItem(MINION, 0x6E4D2A, 0xA7A3A0, new Item.Properties()));
    public static final DeferredItem<DeferredSpawnEggItem> SPAWN_EGG_NIGHT_CRAWLER =
        com.sirolf2009.necromancy.item.NecromancyItems.REGISTRY.register("nightcrawler_spawn_egg",
            () -> new DeferredSpawnEggItem(NIGHT_CRAWLER, 0x060606, 0x0D0D0D, new Item.Properties()));
    public static final DeferredItem<DeferredSpawnEggItem> SPAWN_EGG_TEDDY =
        com.sirolf2009.necromancy.item.NecromancyItems.REGISTRY.register("teddy_spawn_egg",
            () -> new DeferredSpawnEggItem(TEDDY, 0x63451D, 0xFF0000, new Item.Properties()));
    public static final DeferredItem<DeferredSpawnEggItem> SPAWN_EGG_ISAAC =
        com.sirolf2009.necromancy.item.NecromancyItems.REGISTRY.register("isaac_spawn_egg",
            () -> new DeferredSpawnEggItem(ISAAC_NORMAL, 0x060606, 0xCC9999, new Item.Properties()));

    private NecromancyEntities() {}

    /** Bus listener registered from {@link com.sirolf2009.necromancy.Necromancy}. */
    public static void registerAttributes(EntityAttributeCreationEvent event) {
        event.put(MINION.get(),         EntityMinion.createAttributes().build());
        event.put(NIGHT_CRAWLER.get(),  EntityNightCrawler.createAttributes().build());
        event.put(TEDDY.get(),          EntityTeddy.createAttributes().build());
        event.put(ISAAC_NORMAL.get(),   EntityIsaacBody.createAttributes().build());
        event.put(ISAAC_BLOOD.get(),    EntityIsaacBlood.createAttributes().build());
        event.put(ISAAC_HEAD.get(),     EntityIsaacHead.createAttributes().build());
        event.put(ISAAC_BODY.get(),     EntityIsaacBody.createAttributes().build());
    }

    /** Bus listener for spawn placements (fall-back data; MapModifier handles biome-level rules). */
    public static void registerSpawnPlacements(RegisterSpawnPlacementsEvent event) {
        event.register(NIGHT_CRAWLER.get(),
            SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
            Monster::checkMonsterSpawnRules, RegisterSpawnPlacementsEvent.Operation.REPLACE);
        event.register(ISAAC_NORMAL.get(),
            SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
            Monster::checkMonsterSpawnRules, RegisterSpawnPlacementsEvent.Operation.REPLACE);
    }
}
