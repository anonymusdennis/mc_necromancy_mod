package com.sirolf2009.necromancy;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Mod entry point.
 *
 * <p>This class is the 1.21.1 NeoForge equivalent of the legacy
 * {@code com.sirolf2009.necromancy.Necromancy} {@code @Mod} class.  It owns:
 * <ul>
 *     <li>the registration of every {@code DeferredRegister} we ship,</li>
 *     <li>the lifecycle wiring (config registration, common setup, client setup),</li>
 *     <li>the public {@link IEventBus} reference used by client subsystems.</li>
 * </ul>
 *
 * <p>Subsystems live in their own packages and are registered <em>here</em>
 * to keep all bus subscriptions in one auditable place.
 */
@Mod(Reference.MOD_ID)
public final class Necromancy {

    public static final Logger LOGGER = LoggerFactory.getLogger(Reference.MOD_NAME);

    /**
     * Required NeoForge constructor.  All registries are wired through the
     * mod-event bus passed in here.
     */
    public Necromancy(IEventBus modBus, ModContainer modContainer) {
        LOGGER.info("Necromancy port for 1.21.1 initialising");

        // -- Configuration ------------------------------------------------
        modContainer.registerConfig(ModConfig.Type.COMMON, NecromancyConfig.SPEC);
        modContainer.registerConfig(ModConfig.Type.CLIENT, NecromancyClientConfig.SPEC);

        // -- DeferredRegisters --------------------------------------------
        com.sirolf2009.necromancy.item.NecromancyItems.REGISTRY.register(modBus);
        com.sirolf2009.necromancy.item.NecromancyItems.CREATIVE_TABS.register(modBus);
        com.sirolf2009.necromancy.block.NecromancyBlocks.REGISTRY.register(modBus);
        com.sirolf2009.necromancy.block.NecromancyBlocks.ITEMS.register(modBus);
        com.sirolf2009.necromancy.block.NecromancyBlocks.BLOCK_ENTITIES.register(modBus);
        com.sirolf2009.necromancy.fluid.NecromancyFluids.FLUID_TYPES.register(modBus);
        com.sirolf2009.necromancy.fluid.NecromancyFluids.FLUIDS.register(modBus);
        com.sirolf2009.necromancy.entity.NecromancyEntities.ENTITIES.register(modBus);
        com.sirolf2009.necromancy.particle.NecromancyParticles.PARTICLES.register(modBus);
        com.sirolf2009.necromancy.inventory.NecromancyMenus.MENUS.register(modBus);
        com.sirolf2009.necromancy.crafting.NecromancyRecipes.RECIPE_TYPES.register(modBus);
        com.sirolf2009.necromancy.crafting.NecromancyRecipes.RECIPE_SERIALIZERS.register(modBus);
        com.sirolf2009.necromancy.entity.NecromancyEntities.SOUND_EVENTS.register(modBus);
        com.sirolf2009.necromancy.worldgen.NecromancyFeatures.FEATURES.register(modBus);

        // -- Bus listeners ------------------------------------------------
        // Event handlers live with the subsystem they belong to but are wired here.
        modBus.addListener(com.sirolf2009.necromancy.entity.NecromancyEntities::registerAttributes);
        modBus.addListener(com.sirolf2009.necromancy.entity.NecromancyEntities::registerSpawnPlacements);
        modBus.addListener(com.sirolf2009.necromancy.network.NecromancyNetwork::register);
        modBus.addListener(com.sirolf2009.necromancy.crafting.CraftingManagerSewing::onCommonSetup);

        com.sirolf2009.necromancy.event.NecromancyEvents.register();

        if (FMLEnvironment.dist == Dist.CLIENT) {
            NeoForge.EVENT_BUS.register(com.sirolf2009.necromancy.client.MinionInsightRenderer.class);
            NeoForge.EVENT_BUS.register(com.sirolf2009.necromancy.client.multipart.MultipartEditLockDebugOverlay.class);
            NeoForge.EVENT_BUS.register(com.sirolf2009.necromancy.client.multipart.MultipartActivityDebugRenderer.class);
            NeoForge.EVENT_BUS.register(com.sirolf2009.necromancy.client.multipart.MultipartTelemetryHud.class);
        }

        LOGGER.info("Necromancy port wired");
    }
}
