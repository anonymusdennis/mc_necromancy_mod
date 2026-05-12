package com.sirolf2009.necromancy.block;

import com.sirolf2009.necromancy.Reference;
import com.sirolf2009.necromancy.block.entity.BlockEntityAltar;
import com.sirolf2009.necromancy.block.entity.BlockEntityBodypartDev;
import com.sirolf2009.necromancy.block.entity.BlockEntityOperationTable;
import com.sirolf2009.necromancy.block.entity.BlockEntitySewing;
import com.sirolf2009.necromancy.fluid.NecromancyFluids;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Central registry for every block, block-item and block-entity in the mod.
 *
 * <p>Mirrors {@code RegistryBlocksNecromancy}.  Block entities for the altar
 * and the sewing machine are registered here so the BE renderers can pick them
 * up via the {@code SAVED} field.
 */
public final class NecromancyBlocks {

    public static final DeferredRegister.Blocks REGISTRY =
        DeferredRegister.createBlocks(Reference.MOD_ID);
    public static final DeferredRegister.Items ITEMS =
        DeferredRegister.createItems(Reference.MOD_ID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
        DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, Reference.MOD_ID);

    public static final Map<String, DeferredHolder<Item, BlockItem>> BLOCK_ITEMS = new LinkedHashMap<>();

    // ------------------------------------------------------------------ --
    // Blocks
    // ------------------------------------------------------------------ --
    public static final DeferredBlock<BlockAltar> ALTAR = REGISTRY.register("altar",
        () -> new BlockAltar(BlockBehaviour.Properties.of()
            .strength(4F)
            .sound(SoundType.STONE)
            .noOcclusion()));

    public static final DeferredBlock<BlockAltarBlock> ALTAR_BLOCK = REGISTRY.register("altar_block",
        () -> new BlockAltarBlock(BlockBehaviour.Properties.of()
            .strength(4F)
            .sound(SoundType.STONE)));

    public static final DeferredBlock<BlockSewing> SEWING_MACHINE = REGISTRY.register("sewing_machine",
        () -> new BlockSewing(BlockBehaviour.Properties.of()
            .strength(4F)
            .sound(SoundType.METAL)
            .noOcclusion()));

    public static final DeferredBlock<BlockSkullWall> SKULL_WALL = REGISTRY.register("skull_wall",
        () -> new BlockSkullWall(BlockBehaviour.Properties.of()
            .strength(1F)
            .sound(SoundType.BONE_BLOCK)
            .noOcclusion()
            .noCollission()));

    /** Developer-only bodypart JSON editor (uncraftable; creative/op only). */
    public static final DeferredBlock<BlockBodypartDev> BODYPART_DEV = REGISTRY.register("bodypart_dev_editor",
        () -> new BlockBodypartDev(BlockBehaviour.Properties.of()
            .mapColor(MapColor.COLOR_BLACK)
            .strength(-1.0F, 3600000.0F)
            .sound(SoundType.METAL)
            .noLootTable()));

    /** Operation table shell — surgical UX backlog (non-functional ledger grid). */
    public static final DeferredBlock<BlockOperationTable> OPERATION_TABLE = REGISTRY.register("operation_table",
        () -> new BlockOperationTable(BlockBehaviour.Properties.of()
            .strength(4F)
            .sound(SoundType.METAL)
            .noOcclusion()));

    /** Liquid block backed by the {@code blood} fluid. */
    public static final DeferredBlock<LiquidBlock> BLOOD = REGISTRY.register("blood",
        () -> new LiquidBlock(NecromancyFluids.BLOOD_SOURCE.get(),
            BlockBehaviour.Properties.of()
                .replaceable()
                .noCollission()
                .strength(100F)
                .pushReaction(PushReaction.DESTROY)
                .noLootTable()
                .liquid()
                .mapColor(MapColor.COLOR_RED)));

    // ------------------------------------------------------------------ --
    // Block entities
    // ------------------------------------------------------------------ --
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<BlockEntityAltar>> ALTAR_BE =
        BLOCK_ENTITIES.register("altar",
            () -> BlockEntityType.Builder.of(BlockEntityAltar::new, ALTAR.get()).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<BlockEntitySewing>> SEWING_BE =
        BLOCK_ENTITIES.register("sewing_machine",
            () -> BlockEntityType.Builder.of(BlockEntitySewing::new, SEWING_MACHINE.get()).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<BlockEntityBodypartDev>> BODYPART_DEV_BE =
        BLOCK_ENTITIES.register("bodypart_dev_editor",
            () -> BlockEntityType.Builder.of(BlockEntityBodypartDev::new, BODYPART_DEV.get()).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<BlockEntityOperationTable>> OPERATION_TABLE_BE =
        BLOCK_ENTITIES.register("operation_table",
            () -> BlockEntityType.Builder.of(BlockEntityOperationTable::new, OPERATION_TABLE.get()).build(null));

    static {
        // Block items registered in the same step so they share the namespace.
        registerBlockItem(ALTAR);
        registerBlockItem(ALTAR_BLOCK);
        registerBlockItem(SEWING_MACHINE);
        registerBlockItem(SKULL_WALL);
        registerBlockItem(BODYPART_DEV);
        registerBlockItem(OPERATION_TABLE);
        // BLOOD has no block-item, only a bucket
    }

    private static <B extends Block> void registerBlockItem(DeferredBlock<B> block) {
        String id = block.getId().getPath();
        BLOCK_ITEMS.put(id,
            ITEMS.register(id, () -> new BlockItem(block.get(), new Item.Properties())));
    }

    private NecromancyBlocks() {}
}
