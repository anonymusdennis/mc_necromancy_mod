package com.sirolf2009.necromancy.entity;

import com.sirolf2009.necromancy.api.NecroEntityRegistry;
import com.sirolf2009.necromancy.entity.necroapi.*;

/**
 * Bootstrap point for the {@link NecroEntityRegistry}.
 *
 * <p>Mirrors the legacy {@code RegistryNecromancyEntities.initEntities()}.  The
 * 17 adapter classes are instantiated here; their constructors register
 * themselves in {@link NecroEntityRegistry}.  Called from the
 * {@code FMLCommonSetupEvent} so item registration has already finished.
 */
public final class RegistryNecromancyEntities {

    private static boolean bootstrapped = false;

    private RegistryNecromancyEntities() {}

    public static synchronized void bootstrap() {
        if (bootstrapped) return;
        bootstrapped = true;
        NecroEntityRegistry.registerEntity(new NecroEntityZombie());
        NecroEntityRegistry.registerEntity(new NecroEntitySkeleton());
        NecroEntityRegistry.registerEntity(new NecroEntityPigZombie());
        NecroEntityRegistry.registerEntity(new NecroEntityCow());
        NecroEntityRegistry.registerEntity(new NecroEntityPig());
        NecroEntityRegistry.registerEntity(new NecroEntitySheep());
        NecroEntityRegistry.registerEntity(new NecroEntityChicken());
        NecroEntityRegistry.registerEntity(new NecroEntityCreeper());
        NecroEntityRegistry.registerEntity(new NecroEntitySpider());
        NecroEntityRegistry.registerEntity(new NecroEntityCaveSpider());
        NecroEntityRegistry.registerEntity(new NecroEntitySquid());
        NecroEntityRegistry.registerEntity(new NecroEntityEnderman());
        NecroEntityRegistry.registerEntity(new NecroEntityVillager());
        NecroEntityRegistry.registerEntity(new NecroEntityWitch());
        NecroEntityRegistry.registerEntity(new NecroEntityWolf());
        NecroEntityRegistry.registerEntity(new NecroEntityIronGolem());
        NecroEntityRegistry.registerEntity(new NecroEntityIsaac());

        NecroEntityRegistry.registerEntity(new NecroEntityAllay());
        NecroEntityRegistry.registerEntity(new NecroEntityAxolotl());
        NecroEntityRegistry.registerEntity(new NecroEntityCamel());
        NecroEntityRegistry.registerEntity(new NecroEntityFrog());
        NecroEntityRegistry.registerEntity(new NecroEntityRabbit());
        NecroEntityRegistry.registerEntity(new NecroEntitySniffer());
        NecroEntityRegistry.registerEntity(new NecroEntityWarden());
        NecroEntityRegistry.registerEntity(new NecroEntityBlaze());
        NecroEntityRegistry.registerEntity(new NecroEntityGoat());
    }
}
