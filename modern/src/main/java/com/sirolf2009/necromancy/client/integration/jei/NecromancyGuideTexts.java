package com.sirolf2009.necromancy.client.integration.jei;

import com.sirolf2009.necromancy.Reference;
import com.sirolf2009.necromancy.api.BodyPartLocation;
import com.sirolf2009.necromancy.item.ItemBodyPart;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.Locale;

/** Long descriptions for the JEI handbook category (keys live under {@code jei.necromancy.*}). */
public final class NecromancyGuideTexts {

    private NecromancyGuideTexts() {}

    public static Component describe(ItemStack stack) {
        if (stack.isEmpty()) {
            return Component.empty();
        }
        var item = stack.getItem();
        if (item instanceof ItemBodyPart bp) {
            String mobKey = "jei.necromancy.mob." + bp.getMobName().toLowerCase(Locale.ROOT);
            String slotKey = slotLangKey(bp.getLocation());
            return Component.translatable("jei.necromancy.guide.body_part",
                Component.translatable(mobKey),
                Component.translatable(slotKey));
        }
        ResourceLocation id = BuiltInRegistries.ITEM.getKey(item);
        if (id == null || !Reference.MOD_ID.equals(id.getNamespace())) {
            return Component.translatable("jei.necromancy.guide.generic_item", stack.getHoverName());
        }
        String pathKey = "jei.necromancy.guide.item." + id.getPath();
        if (!I18n.exists(pathKey)) {
            return Component.translatable("jei.necromancy.guide.generic_item", stack.getHoverName());
        }
        return Component.translatable(pathKey);
    }

    private static String slotLangKey(BodyPartLocation loc) {
        return switch (loc) {
            case Head -> "necromancy.slot.head";
            case Torso -> "necromancy.slot.torso";
            case ArmLeft, ArmRight -> "necromancy.slot.arm";
            case Legs -> "necromancy.slot.legs";
        };
    }
}
