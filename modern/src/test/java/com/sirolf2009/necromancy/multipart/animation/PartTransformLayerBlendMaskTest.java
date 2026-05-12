package com.sirolf2009.necromancy.multipart.animation;

import com.sirolf2009.necromancy.Reference;
import com.sirolf2009.necromancy.multipart.math.PartTransform;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PartTransformLayerBlendMaskTest {

    private static final ResourceLocation PART = ResourceLocation.fromNamespaceAndPath(Reference.MOD_ID, "test_bone");

    @Test
    void maskedTranslationAccumulatesOnlyEnabledAxes() {
        PartTransform delta = new PartTransform();
        delta.setTranslation(new Vec3(1f, 2f, 3f));
        TransformBlendMask mask = TransformBlendMask.translationOnly(TransformBlendMask.AXIS_Y | TransformBlendMask.AXIS_Z);
        WeightedPartTransform w = new WeightedPartTransform(PART, RenderLayerPhase.PROCEDURAL, 0, 1f, delta, mask,
            RotationBlendStyle.POST_MULTIPLY);

        PartTransform out = new PartTransform();
        PartTransformLayerBlend.blendInto(List.of(w), out);

        Vec3 t = out.translation();
        assertEquals(0f, t.x, 1e-6f);
        assertEquals(2f, t.y, 1e-6f);
        assertEquals(3f, t.z, 1e-6f);
    }

    @Test
    void legacyWeightedTransformUsesFullMaskMatchesWholeDelta() {
        PartTransform delta = new PartTransform();
        delta.setTranslation(new Vec3(0.5f, -0.25f, 0f));
        Quaternionf q = new Quaternionf().rotateY((float) Math.toRadians(30));
        delta.setRotation(q);
        delta.setUniformScale(1.1f);

        WeightedPartTransform w = new WeightedPartTransform(PART, RenderLayerPhase.ADDITIVE_OVERLAY, 0, 1f, delta);

        PartTransform out = new PartTransform();
        PartTransformLayerBlend.blendInto(List.of(w), out);

        assertEquals(0.5f, (float) out.translation().x, 1e-5f);
        Vector3f s = new Vector3f();
        out.scaleInto(s);
        assertEquals(1.1f, s.x, 1e-5f);
        Quaternionf qo = new Quaternionf();
        out.rotationInto(qo);
        assertTrue(Math.abs(qo.dot(q)) > 0.99f);
    }

    @Test
    void preMultiplyVersusPostMultiplyDiffersForSameAccumulatedRotation() {
        PartTransform d1 = new PartTransform();
        d1.setRotation(new Quaternionf().rotateX((float) Math.toRadians(72)));
        PartTransform d2 = new PartTransform();
        d2.setRotation(new Quaternionf().rotateY((float) Math.toRadians(81)));

        WeightedPartTransform firstPost = new WeightedPartTransform(PART, RenderLayerPhase.PROCEDURAL, 0, 1f, d1,
            TransformBlendMask.rotationWholeQuaternionOnly(), RotationBlendStyle.POST_MULTIPLY);
        WeightedPartTransform secondPre = new WeightedPartTransform(PART, RenderLayerPhase.PROCEDURAL, 1, 1f, d2,
            TransformBlendMask.rotationWholeQuaternionOnly(), RotationBlendStyle.PRE_MULTIPLY);

        PartTransform mixedOrder = new PartTransform();
        PartTransformLayerBlend.blendInto(List.of(firstPost, secondPre), mixedOrder);

        WeightedPartTransform bothPostFirst = new WeightedPartTransform(PART, RenderLayerPhase.PROCEDURAL, 0, 1f, d1,
            TransformBlendMask.rotationWholeQuaternionOnly(), RotationBlendStyle.POST_MULTIPLY);
        WeightedPartTransform bothPostSecond = new WeightedPartTransform(PART, RenderLayerPhase.PROCEDURAL, 1, 1f, d2,
            TransformBlendMask.rotationWholeQuaternionOnly(), RotationBlendStyle.POST_MULTIPLY);

        PartTransform postBoth = new PartTransform();
        PartTransformLayerBlend.blendInto(List.of(bothPostFirst, bothPostSecond), postBoth);

        Quaternionf a = new Quaternionf();
        mixedOrder.rotationInto(a);
        Quaternionf b = new Quaternionf();
        postBoth.rotationInto(b);
        double angularSepRad = 2.0 * Math.acos(Math.min(1.0, Math.abs(a.dot(b))));
        assertTrue(angularSepRad > Math.toRadians(0.25),
            "PRE_MULTIPLY vs POST_MULTIPLY ordering should change accumulated overlay orientation");
    }
}
