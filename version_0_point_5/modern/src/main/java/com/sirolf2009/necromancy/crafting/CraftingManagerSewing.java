package com.sirolf2009.necromancy.crafting;

import com.sirolf2009.necromancy.api.NecroEntityBase;
import com.sirolf2009.necromancy.api.NecroEntityRegistry;
import com.sirolf2009.necromancy.entity.RegistryNecromancyEntities;
import com.sirolf2009.necromancy.item.NecromancyItems;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Programmatic sewing-recipe registry.
 *
 * <p>Mirrors the legacy {@code CraftingManagerSewing}.  At common-setup we
 * iterate every {@link NecroEntityBase} in the
 * {@link NecroEntityRegistry} and build {@link SewingRecipe}s from their
 * {@code Object[]} recipe arrays.  We also add the legacy "side recipes":
 * <ul>
 *     <li>1 leather -> 8 skin (shapeless)</li>
 *     <li>5 rotten flesh + 2 ghast tears + soul + heart -> 1 spawner (shapeless)</li>
 *     <li>4x4 leather/wool teddy spawn-egg surrogate -> Necro Teddy spawn egg</li>
 * </ul>
 */
public final class CraftingManagerSewing {

    private static final List<SewingRecipe> RECIPES = Collections.synchronizedList(new ArrayList<>());

    private CraftingManagerSewing() {}

    public static void register(SewingRecipe recipe) {
        RECIPES.add(recipe);
    }

    public static void onCommonSetup(FMLCommonSetupEvent event) {
        // The 17 adapters are constructed here too, so they have to register
        // first.  RegistryNecromancyEntities.bootstrap() is the gate.
        event.enqueueWork(() -> {
            RegistryNecromancyEntities.bootstrap();
            buildRecipes();
        });
    }

    private static void buildRecipes() {
        RECIPES.clear();
        // Per-mob body-part recipes
        for (NecroEntityBase mob : NecroEntityRegistry.registeredEntities.values()) {
            mob.initRecipes();
            RECIPES.addAll(mob.buildRecipes());
        }
        // 1 leather -> 8 skin
        RECIPES.add(SewingRecipe.shapeless(
            new ItemStack(NecromancyItems.SKIN.get(), 8),
            Items.LEATHER));
        // Spawner from rotten flesh / ghast tears / soul jar / heart
        RECIPES.add(SewingRecipe.shapeless(
            new ItemStack(NecromancyItems.SPAWNER.get(), 1),
            Items.ROTTEN_FLESH, Items.ROTTEN_FLESH, Items.ROTTEN_FLESH,
            Items.ROTTEN_FLESH, Items.ROTTEN_FLESH, Items.GHAST_TEAR,
            Items.GHAST_TEAR, NecromancyItems.SOUL_IN_A_JAR.get(), NecromancyItems.HEART.get()));
        // Teddy spawn-egg recipe (4x4 leather + wool centre)
        RECIPES.add(SewingRecipe.shaped(new Object[] {
            "LLLL", "LWWL", "LWWL", "LLLL",
            'L', new ItemStack(Items.LEATHER),
            'W', new ItemStack(Items.WHITE_WOOL)
        }, new ItemStack(com.sirolf2009.necromancy.entity.NecromancyEntities.SPAWN_EGG_TEDDY.get())));
    }

    /** Returns the result {@link ItemStack} for the given crafting matrix, or empty. */
    public static ItemStack findMatching(Container craftMatrix, Level level) {
        for (SewingRecipe r : RECIPES) {
            if (r.matches(craftMatrix)) return r.result.copy();
        }
        return ItemStack.EMPTY;
    }
}
