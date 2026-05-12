package com.sirolf2009.necromancy.multipart;

import com.sirolf2009.necromancy.multipart.editor.MultipartEditorHooks;
import com.sirolf2009.necromancy.multipart.part.AttachmentPoint;
import com.sirolf2009.necromancy.multipart.part.BodyPartNode;
import com.sirolf2009.necromancy.multipart.part.HitboxComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class MultipartHierarchyMementoTest {

    private static HitboxComponent box() {
        return new HitboxComponent.FixedLocalBox(new AABB(0, 0, 0, 1, 1, 1), false);
    }

    @Test
    void captureEmptyReplayClears() {
        TransformHierarchy h = new TransformHierarchy();
        MultipartHierarchyMemento m = MultipartHierarchyMemento.capture(h);
        BodyPartNode root = new BodyPartNode(ResourceLocation.parse("necromancy:r"), box());
        h.registerRoot(root);
        assertEquals(1, h.nodes().size());
        m.applyTo(h);
        assertEquals(0, h.nodes().size());
    }

    @Test
    void roundTripRestoresSocketAttachment() {
        TransformHierarchy h = new TransformHierarchy();
        ResourceLocation rootId = ResourceLocation.parse("necromancy:torso");
        ResourceLocation socketId = ResourceLocation.parse("necromancy:socket_arm");
        ResourceLocation armId = ResourceLocation.parse("necromancy:arm");

        BodyPartNode root = new BodyPartNode(rootId, box());
        BodyPartNode arm = new BodyPartNode(armId, box());
        h.registerRoot(root);
        MultipartEditorHooks.attachToSocket(h, rootId, socketId, arm);

        MultipartHierarchyMemento snap = MultipartHierarchyMemento.capture(h);
        int expectedTopo = h.topologyRevision();

        MultipartEditorHooks.clearSocket(h, rootId, socketId);
        AttachmentPoint cleared = h.get(rootId).attachmentPointsView().stream()
            .filter(ap -> ap.socketId().equals(socketId))
            .findFirst()
            .orElseThrow();
        assertNull(cleared.childPartId());

        snap.applyTo(h);

        AttachmentPoint restored = h.get(rootId).attachmentPointsView().stream()
            .filter(ap -> ap.socketId().equals(socketId))
            .findFirst()
            .orElseThrow();
        assertEquals(armId, restored.childPartId());
        assertNotNull(h.get(armId));
        assertEquals(expectedTopo, h.topologyRevision());
    }

    @Test
    void restoresSimulationLocalPose() {
        TransformHierarchy h = new TransformHierarchy();
        ResourceLocation id = ResourceLocation.parse("necromancy:bone");
        BodyPartNode n = new BodyPartNode(id, box());
        h.registerRoot(n);
        n.simulationLocalTransform().setTranslation(new Vec3(1, 2, 3));

        MultipartHierarchyMemento snap = MultipartHierarchyMemento.capture(h);
        n.simulationLocalTransform().setTranslation(Vec3.ZERO);
        snap.applyTo(h);
        assertEquals(1d, h.get(id).simulationLocalTransform().translation().x, 1e-6);
        assertEquals(2d, h.get(id).simulationLocalTransform().translation().y, 1e-6);
        assertEquals(3d, h.get(id).simulationLocalTransform().translation().z, 1e-6);
    }
}
