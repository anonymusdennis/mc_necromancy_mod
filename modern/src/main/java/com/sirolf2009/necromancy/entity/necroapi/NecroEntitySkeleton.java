package com.sirolf2009.necromancy.entity.necroapi;

import com.sirolf2009.necromancy.Reference;
import com.sirolf2009.necromancy.api.BodyPartLocation;
import com.sirolf2009.necromancy.api.ISkull;
import com.sirolf2009.necromancy.api.LocomotionProfile;
import com.sirolf2009.necromancy.api.NecroEntityBiped;
import com.sirolf2009.necromancy.api.VoiceProfile;
import com.sirolf2009.necromancy.api.feature.PartFeature;
import com.sirolf2009.necromancy.api.feature.SkeletonArmVolleyFeature;
import com.sirolf2009.necromancy.item.NecromancyItems;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.List;

/** 1:1 port of {@code NecroEntitySkeleton} (also implements {@link ISkull}). */
public class NecroEntitySkeleton extends NecroEntityBiped implements ISkull {
    public NecroEntitySkeleton() {
        super("Skeleton");
        headItem  = new ItemStack(Items.SKELETON_SKULL);
        torsoItem = new ItemStack(NecromancyItems.bodyPart("Skeleton Torso"));
        armItem   = new ItemStack(NecromancyItems.bodyPart("Skeleton Arm"));
        legItem   = new ItemStack(NecromancyItems.bodyPart("Skeleton Legs"));
        texture   = ResourceLocation.parse("minecraft:textures/entity/skeleton/skeleton.png");
        skullItem = new ItemStack(Items.SKELETON_SKULL);
    }
    @Override public void initRecipes() { initDefaultRecipes(Items.BONE); }
    @Override public LocomotionProfile locomotion() { return LocomotionProfile.walk(1.05F, SoundEvents.SKELETON_STEP); }
    @Override public VoiceProfile voice() { return VoiceProfile.SKELETON; }
    @Override
    public List<PartFeature> features(BodyPartLocation location) {
        List<PartFeature> out = new ArrayList<>(super.features(location));
        if (location == BodyPartLocation.ArmLeft || location == BodyPartLocation.ArmRight) {
            out.add(SkeletonArmVolleyFeature.INSTANCE);
        }
        return out;
    }
    @Override public ResourceLocation getSkullTexture() { return ResourceLocation.parse("minecraft:textures/entity/skeleton/skeleton.png"); }
    @Override public String getSkullName() { return "Skeleton"; }
    @Override public void setAttributes(LivingEntity m, BodyPartLocation loc) {
        switch (loc) {
            case Head     -> addAttributeMods(m, "Head", 1, 1, 0, 0, 0);
            case Torso    -> addAttributeMods(m, "Torso", 1.5, 0, 0, 0, 0);
            case ArmLeft  -> addAttributeMods(m, "ArmL", 0.5, 0, 0, 0, 0.5);
            case ArmRight -> addAttributeMods(m, "ArmR", 0.5, 0, 0, 0, 0.5);
            case Legs     -> addAttributeMods(m, "Legs", 0.75, 0, 3, 3, 0);
        }
    }
}
