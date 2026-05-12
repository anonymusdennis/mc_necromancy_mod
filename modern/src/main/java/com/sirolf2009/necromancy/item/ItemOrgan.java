package com.sirolf2009.necromancy.item;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;

/**
 * One of the five organ foods (Brains / Heart / Muscle / Lungs / Skin).
 *
 * <p>Direct port of the legacy multi-meta {@code ItemOrgans} food item, split
 * into one {@link Item} subclass instance per organ.  Eating any organ deals 30
 * ticks of {@link MobEffects#HUNGER} like the legacy item.
 */
public class ItemOrgan extends Item {

    public ItemOrgan() {
        super(new Item.Properties()
            .food(new FoodProperties.Builder()
                .nutrition(2)
                .saturationModifier(0.4F)
                .alwaysEdible()
                .effect(() -> new MobEffectInstance(MobEffects.HUNGER, 30, 0), 0.8F)
                .build()));
    }
}
