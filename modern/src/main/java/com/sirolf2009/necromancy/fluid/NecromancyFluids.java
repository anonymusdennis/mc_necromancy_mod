package com.sirolf2009.necromancy.fluid;

import com.sirolf2009.necromancy.Reference;
import com.sirolf2009.necromancy.block.NecromancyBlocks;
import com.sirolf2009.necromancy.item.NecromancyItems;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.fluids.BaseFlowingFluid;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Consumer;

/**
 * Blood fluid family: {@link FluidType}, {@link BaseFlowingFluid} pair.
 *
 * <p>Mirrors the legacy {@code FluidRegistry.registerFluid("blood", ...)} +
 * {@code BlockBlood} pairing.  In NeoForge 1.21.1 we need the FluidType
 * separately, which carries client-side overlay and tint.
 */
public final class NecromancyFluids {

    public static final DeferredRegister<FluidType> FLUID_TYPES =
        DeferredRegister.create(net.neoforged.neoforge.registries.NeoForgeRegistries.Keys.FLUID_TYPES, Reference.MOD_ID);

    public static final DeferredRegister<Fluid> FLUIDS =
        DeferredRegister.create(Registries.FLUID, Reference.MOD_ID);

    /** The {@link FluidType} -- carries appearance + density. */
    public static final DeferredHolder<FluidType, FluidType> BLOOD_TYPE =
        FLUID_TYPES.register("blood", () -> new FluidType(FluidType.Properties.create()
            .descriptionId("fluid_type.necromancy.blood")
            .density(1500)              // heavier than water
            .viscosity(1500)
            .canSwim(false)
            .canDrown(true)
            .canExtinguish(false)
            .fallDistanceModifier(0.5F)
            .pathType(net.minecraft.world.level.pathfinder.PathType.LAVA)
            .adjacentPathType(null)
            .sound(net.neoforged.neoforge.common.SoundActions.BUCKET_FILL, SoundEvents.BUCKET_FILL)
            .sound(net.neoforged.neoforge.common.SoundActions.BUCKET_EMPTY, SoundEvents.BUCKET_EMPTY)
            .lightLevel(0)) {

            @Override
            public void initializeClient(Consumer<IClientFluidTypeExtensions> consumer) {
                consumer.accept(new IClientFluidTypeExtensions() {
                    private static final ResourceLocation STILL =
                        ResourceLocation.parse("minecraft:block/lava_still");
                    private static final ResourceLocation FLOW =
                        ResourceLocation.parse("minecraft:block/lava_flow");
                    private static final ResourceLocation OVERLAY =
                        ResourceLocation.parse("minecraft:textures/block/red_concrete.png");

                    @Override public ResourceLocation getStillTexture()    { return STILL; }
                    @Override public ResourceLocation getFlowingTexture()  { return FLOW; }
                    @Override public ResourceLocation getRenderOverlayTexture(net.minecraft.client.Minecraft mc) { return OVERLAY; }

                    /** Tints the flowing/still texture pure red, since we re-use lava. */
                    @Override public int getTintColor() { return 0xFF7F0000; }
                });
            }
        });

    private static final BaseFlowingFluid.Properties FLUID_PROPS =
        new BaseFlowingFluid.Properties(
            BLOOD_TYPE,
            () -> NecromancyFluids.BLOOD_SOURCE.get(),
            () -> NecromancyFluids.BLOOD_FLOWING.get())
        .block(() -> NecromancyBlocks.BLOOD.get())
        .bucket(() -> NecromancyItems.BUCKET_BLOOD.get())
        .slopeFindDistance(2)
        .levelDecreasePerBlock(2);

    public static final DeferredHolder<Fluid, BaseFlowingFluid.Source> BLOOD_SOURCE =
        FLUIDS.register("blood",         () -> new BaseFlowingFluid.Source(FLUID_PROPS));

    public static final DeferredHolder<Fluid, BaseFlowingFluid.Flowing> BLOOD_FLOWING =
        FLUIDS.register("blood_flowing", () -> new BaseFlowingFluid.Flowing(FLUID_PROPS));

    private NecromancyFluids() {}
}
