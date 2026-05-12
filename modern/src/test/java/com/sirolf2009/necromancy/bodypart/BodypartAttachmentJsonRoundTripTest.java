package com.sirolf2009.necromancy.bodypart;

import org.joml.Quaternionf;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * RC-F2: attachment euler / quaternion fields survive Gson IO and produce stable {@link BodypartAttachmentJson#socketLocalTransform()} poses.
 */
class BodypartAttachmentJsonRoundTripTest {

    @Test
    void eulerSocketRoundTripsThroughDefinitionJsonIo() {
        BodypartDefinitionJson def = new BodypartDefinitionJson();
        def.id = "necromancy:socket_roundtrip_euler";
        BodypartAttachmentJson a = new BodypartAttachmentJson();
        a.ox = 0.1;
        a.oy = 0.2;
        a.oz = -0.15;
        a.eulerYawDeg = 12d;
        a.eulerPitchDeg = -8d;
        a.eulerRollDeg = 45d;
        def.attachments.add(a);

        String raw = BodypartDefinitionIo.toJson(def);
        BodypartDefinitionJson loaded = BodypartDefinitionIo.fromJson(raw);
        assertEquals(1, loaded.attachments.size());

        Quaternionf q0 = new Quaternionf();
        a.socketLocalTransform().rotationInto(q0);
        Quaternionf q1 = new Quaternionf();
        loaded.attachments.get(0).socketLocalTransform().rotationInto(q1);
        assertTrue(q0.equals(q1, 1e-5f), "socket orientation should match after JSON round-trip");
    }

    @Test
    void quaternionSocketDominatesConflictingEulerAfterRoundTrip() {
        BodypartAttachmentJson a = new BodypartAttachmentJson();
        a.quatW = 0.9238795325112867;
        a.quatX = 0.3826834323650898;
        a.quatY = 0d;
        a.quatZ = 0d;
        a.eulerYawDeg = 999d;

        BodypartDefinitionJson def = new BodypartDefinitionJson();
        def.id = "necromancy:socket_roundtrip_quat";
        def.attachments.add(a);
        BodypartAttachmentJson b = BodypartDefinitionIo.fromJson(BodypartDefinitionIo.toJson(def)).attachments.get(0);

        Quaternionf fromSocket = new Quaternionf();
        b.socketLocalTransform().rotationInto(fromSocket);

        BodypartAttachmentJson eulerLie = new BodypartAttachmentJson();
        eulerLie.eulerYawDeg = 999d;
        Quaternionf eulerOnly = new Quaternionf();
        eulerLie.socketLocalTransform().rotationInto(eulerOnly);

        assertFalse(fromSocket.equals(eulerOnly, 1e-3f), "pose must follow quaternion branch, not bogus euler");

        Quaternionf original = new Quaternionf();
        a.socketLocalTransform().rotationInto(original);
        assertTrue(original.equals(fromSocket, 1e-5f));
    }
}
