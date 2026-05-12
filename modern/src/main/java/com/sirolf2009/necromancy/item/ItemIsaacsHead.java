package com.sirolf2009.necromancy.item;

import com.sirolf2009.necromancy.Reference;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ArmorMaterials;
import net.minecraft.world.item.Item;

import java.util.function.Supplier;

/**
 * Isaac's Severed Head -- a head-slot armour item that lets the wearer fire
 * tear projectiles when they press the configured key.
 *
 * <p>The legacy mod created an {@code ArmorMaterial} with damage-reduction 0
 * and unlimited durability ("undestructible cosmetic").  We mirror this by
 * using the vanilla {@code LEATHER} material and overriding the texture +
 * unlimited durability via the item properties.
 */
public class ItemIsaacsHead extends ArmorItem {

    public ItemIsaacsHead() {
        super(ArmorMaterials.LEATHER, ArmorItem.Type.HELMET, new Item.Properties()
            .stacksTo(1)
            .durability(0)        // 0 == no durability bar
            .fireResistant());
    }

    /** Returns the texture used when this armour is rendered on a player. */
    public static net.minecraft.resources.ResourceLocation armorTexture() {
        return Reference.TEXTURE_ARMOR_ISAAC;
    }
}
