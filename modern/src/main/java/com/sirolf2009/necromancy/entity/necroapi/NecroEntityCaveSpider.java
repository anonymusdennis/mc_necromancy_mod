package com.sirolf2009.necromancy.entity.necroapi;

import com.mojang.blaze3d.vertex.PoseStack;
import com.sirolf2009.necromancy.api.BodyPartLocation;
import com.sirolf2009.necromancy.api.LocomotionProfile;
import com.sirolf2009.necromancy.item.NecromancyItems;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/**
 * 1:1 port of the legacy {@code NecroEntityCaveSpider}.
 *
 * <p>Cave spiders inherit the spider geometry and walk-cycle wholesale.  The
 * legacy adapter wrapped the entire render in a {@code GL11.glScalef(0.7F)}
 * via its {@code preRender}/{@code postRender} hooks; we do the same here by
 * pushing a 0.7x scale on the {@link PoseStack} for each body-part group.
 */
public class NecroEntityCaveSpider extends NecroEntitySpider {

    public NecroEntityCaveSpider() {
        super("CaveSpider");
        headItem  = new ItemStack(NecromancyItems.bodyPart("CaveSpider Head"));
        torsoItem = new ItemStack(NecromancyItems.bodyPart("CaveSpider Torso"));
        legItem   = new ItemStack(NecromancyItems.bodyPart("CaveSpider Legs"));
        texture   = ResourceLocation.parse("minecraft:textures/entity/spider/cave_spider.png");
        hasArms   = false;
    }

    @Override public void initRecipes() { initDefaultRecipes(Items.STRING); }
    @Override public LocomotionProfile locomotion() { return LocomotionProfile.walk(1.30F, SoundEvents.SPIDER_STEP); }
    // Voice inherits SPIDER from NecroEntitySpider.

    @Override
    public void preRender(LivingEntity minion, PoseStack pose, BodyPartLocation location) {
        // Cave spiders are smaller than regular spiders.
        pose.scale(0.7F, 0.7F, 0.7F);
    }

    @Override public void setAttributes(LivingEntity m, BodyPartLocation loc) {
        switch (loc) {
            case Head  -> addAttributeMods(m, "Head", 0.5, 1, 0, 0, 0.5);
            case Torso -> addAttributeMods(m, "Torso", 2, 0, 0, 0, 0);
            case Legs  -> addAttributeMods(m, "Legs", 0.5, 0, 1, 2, 0.5);
            default    -> {}
        }
    }
}
