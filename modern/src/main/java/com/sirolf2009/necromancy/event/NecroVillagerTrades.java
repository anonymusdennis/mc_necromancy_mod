package com.sirolf2009.necromancy.event;

import com.sirolf2009.necromancy.bodypart.BodyPartConfigGate;
import com.sirolf2009.necromancy.item.NecromancyItems;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.village.VillagerTradesEvent;

import java.util.List;
import java.util.Random;

/**
 * Adds the legacy "Necro Villager" trades.
 *
 * <p>The 1.7.10 mod registered a new villager profession id and attached a
 * trade handler that ran when the villager spawned.  In modern NeoForge we
 * instead piggy-back on the existing {@code librarian} profession (the
 * legacy mod also reused villager textures for its custom profession), and
 * inject our trades on every level so dedicated necromancy-trade villagers
 * are not strictly required.
 */
public final class NecroVillagerTrades {
    private NecroVillagerTrades() {}

    @SubscribeEvent
    public static void onTrades(VillagerTradesEvent event) {
        if (event.getType() != VillagerProfession.LIBRARIAN) return;

        // Only validated bodyparts appear in trades — no chat explanation (contrasts with altar/item use UX).
        var partItems = NecromancyItems.BODY_PARTS.values().stream()
            .map(d -> d.get().getDefaultInstance())
            .filter(BodyPartConfigGate::allows)
            .toList();

        addLevel(event, 1,
            new BasicTrade(new ItemStack(Items.EMERALD, 6), new ItemStack(Items.BOOK),
                NecromancyItems.NECRONOMICON.get().getDefaultInstance(), 4, 8));
        if (!partItems.isEmpty()) {
            addLevel(event, 2,
                new RandomBodyPartTrade(partItems, false));
            addLevel(event, 3,
                new RandomBodyPartTrade(partItems, true));
        }
    }

    private static void addLevel(VillagerTradesEvent event, int level, VillagerTrades.ItemListing trade) {
        event.getTrades().computeIfAbsent(level, k -> new java.util.ArrayList<>()).add(trade);
    }

    /** A simple emerald-for-item trade, used for the Necronomicon listing. */
    private record BasicTrade(ItemStack price, ItemStack secondPrice, ItemStack result,
                              int maxUses, int xp) implements VillagerTrades.ItemListing {
        @Override
        public net.minecraft.world.item.trading.MerchantOffer getOffer(net.minecraft.world.entity.Entity trader,
                                                                       net.minecraft.util.RandomSource random) {
            return new net.minecraft.world.item.trading.MerchantOffer(
                new net.minecraft.world.item.trading.ItemCost(price.getItem(), price.getCount()),
                java.util.Optional.of(new net.minecraft.world.item.trading.ItemCost(secondPrice.getItem(), secondPrice.getCount())),
                result.copy(), maxUses, xp, 0.05F);
        }
    }

    /**
     * Trades a random body-part item against (or for) emeralds, exactly like
     * the legacy {@code manipulateTradesForVillager}.  We bake one item per
     * trade because vanilla expects deterministic offers, but each villager
     * rolls a fresh random so different villagers offer different parts.
     */
    private record RandomBodyPartTrade(List<ItemStack> parts, boolean buy) implements VillagerTrades.ItemListing {
        @Override
        public net.minecraft.world.item.trading.MerchantOffer getOffer(net.minecraft.world.entity.Entity trader,
                                                                       net.minecraft.util.RandomSource random) {
            ItemStack part = parts.get(random.nextInt(parts.size()));
            int emeralds = 1 + random.nextInt(3);
            if (buy) {
                return new net.minecraft.world.item.trading.MerchantOffer(
                    new net.minecraft.world.item.trading.ItemCost(part.getItem()),
                    new ItemStack(Items.EMERALD, emeralds), 6, 4, 0.05F);
            }
            return new net.minecraft.world.item.trading.MerchantOffer(
                new net.minecraft.world.item.trading.ItemCost(Items.EMERALD, emeralds),
                part.copy(), 6, 4, 0.05F);
        }
    }
}
