package com.sirolf2009.necromancy.bodypart;

import com.sirolf2009.necromancy.api.BodyPartLocation;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

import java.util.EnumMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * RC-B1 ordering contract — hub bone selection must stay deterministic for altar-era star topology.
 */
class MinionSkeletonBinderChooseHubBoneTest {

    private static Map<BodyPartLocation, ResourceLocation> map(BodyPartLocation loc, ResourceLocation rl) {
        EnumMap<BodyPartLocation, ResourceLocation> m = new EnumMap<>(BodyPartLocation.class);
        m.put(loc, rl);
        return m;
    }

    @Test
    void prefersTorsoOverOtherSlots() {
        EnumMap<BodyPartLocation, ResourceLocation> m = new EnumMap<>(BodyPartLocation.class);
        m.put(BodyPartLocation.Head, ResourceLocation.parse("necromancy:h"));
        m.put(BodyPartLocation.Torso, ResourceLocation.parse("necromancy:t"));
        m.put(BodyPartLocation.Legs, ResourceLocation.parse("necromancy:l"));
        assertEquals(BodyPartLocation.Torso, MinionSkeletonBinder.chooseHubBone(m));
    }

    @Test
    void fallsBackToHeadWhenTorsoMissing() {
        EnumMap<BodyPartLocation, ResourceLocation> m = new EnumMap<>(BodyPartLocation.class);
        m.put(BodyPartLocation.Head, ResourceLocation.parse("necromancy:h"));
        m.put(BodyPartLocation.Legs, ResourceLocation.parse("necromancy:l"));
        assertEquals(BodyPartLocation.Head, MinionSkeletonBinder.chooseHubBone(m));
    }

    @Test
    void prefersArmRightWhenBothArmsPresentWithoutTorsoHeadLegs() {
        EnumMap<BodyPartLocation, ResourceLocation> m = new EnumMap<>(BodyPartLocation.class);
        m.put(BodyPartLocation.ArmLeft, ResourceLocation.parse("necromancy:al"));
        m.put(BodyPartLocation.ArmRight, ResourceLocation.parse("necromancy:ar"));
        assertEquals(BodyPartLocation.ArmRight, MinionSkeletonBinder.chooseHubBone(m));
    }

    @Test
    void armLeftWhenOnlyArmLeftFilled() {
        assertEquals(
            BodyPartLocation.ArmLeft,
            MinionSkeletonBinder.chooseHubBone(map(BodyPartLocation.ArmLeft, ResourceLocation.parse("necromancy:al"))));
    }

    @Test
    void emptyMapFallsThroughToArmLeftPlaceholder() {
        assertEquals(BodyPartLocation.ArmLeft, MinionSkeletonBinder.chooseHubBone(new EnumMap<>(BodyPartLocation.class)));
    }
}
