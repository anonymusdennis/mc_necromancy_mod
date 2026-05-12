package com.sirolf2009.necromancy.inventory;

import com.sirolf2009.necromancy.Reference;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * MenuTypes for the altar and sewing machine.
 *
 * <p>Both MenuTypes are created via {@link IMenuTypeExtension#create} so the
 * server can encode a {@link net.minecraft.core.BlockPos} into the open-screen
 * payload, allowing the client constructor to look up the matching block
 * entity.  This is the standard 1.21.1 pattern for "open-block-GUI" menus.
 */
public final class NecromancyMenus {

    public static final DeferredRegister<MenuType<?>> MENUS =
        DeferredRegister.create(Registries.MENU, Reference.MOD_ID);

    public static final DeferredHolder<MenuType<?>, MenuType<ContainerAltar>> ALTAR =
        MENUS.register("altar",
            () -> IMenuTypeExtension.create(ContainerAltar::new));

    public static final DeferredHolder<MenuType<?>, MenuType<ContainerSewing>> SEWING =
        MENUS.register("sewing_machine",
            () -> IMenuTypeExtension.create(ContainerSewing::new));

    public static final DeferredHolder<MenuType<?>, MenuType<ContainerBodypartDev>> BODYPART_DEV =
        MENUS.register("bodypart_dev_editor",
            () -> IMenuTypeExtension.create(ContainerBodypartDev::new));

    public static final DeferredHolder<MenuType<?>, MenuType<ContainerOperationTable>> OPERATION_TABLE =
        MENUS.register("operation_table",
            () -> IMenuTypeExtension.create(ContainerOperationTable::new));

    private NecromancyMenus() {}
}
