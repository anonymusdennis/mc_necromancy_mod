package com.sirolf2009.necromancy.multipart.network;

import com.sirolf2009.necromancy.multipart.RootMobEntity;
import com.sirolf2009.necromancy.multipart.TransformHierarchy;
import com.sirolf2009.necromancy.multipart.damage.PartDamageState;
import com.sirolf2009.necromancy.multipart.damage.PartFunctionalFlag;
import com.sirolf2009.necromancy.multipart.editor.MultipartEditorHooks;
import com.sirolf2009.necromancy.multipart.math.PartTransform;
import com.sirolf2009.necromancy.multipart.network.binary.MultipartCanonicalPartOrder;
import com.sirolf2009.necromancy.multipart.network.binary.MultipartDamageDeltaBinary;
import com.sirolf2009.necromancy.multipart.network.binary.MultipartDecodeStatus;
import com.sirolf2009.necromancy.multipart.network.binary.MultipartReplicationScratch;
import com.sirolf2009.necromancy.multipart.network.binary.MultipartTopologyDeltaBinary;
import com.sirolf2009.necromancy.multipart.network.binary.MultipartTransformDeltaBinary;
import com.sirolf2009.necromancy.multipart.network.payload.MultipartDamageSyncPayload;
import com.sirolf2009.necromancy.multipart.network.payload.MultipartTopologyDeltaPayload;
import com.sirolf2009.necromancy.multipart.network.payload.MultipartTransformDeltaPayload;
import com.sirolf2009.necromancy.multipart.part.BodyPartNode;
import net.minecraft.resources.ResourceLocation;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

/**
 * Applies decoded multipart replication payloads onto {@link TransformHierarchy} without forcing full-graph synchronisation.
 *
 * <p><strong>Interpolation:</strong> consume payloads during the same client tick <em>before</em>
 * {@link RootMobEntity#multipartTick()} runs so {@link RootMobEntity#multipartPoseInterpolationCapture()} snapshots the prior
 * authoritative poses; layered render overlays follow automatically via propagation after locals mutate.
 */
public final class MultipartReplicationBridge {

    private MultipartReplicationBridge() {}

    public static MultipartDecodeStatus applyTransformDeltaPayload(RootMobEntity root, MultipartTransformDeltaPayload payload) {
        return applyTransformDeltaPayload(root, payload, new MultipartReplicationScratch());
    }

    public static MultipartDecodeStatus applyTransformDeltaPayload(RootMobEntity root, MultipartTransformDeltaPayload payload,
                                                                   MultipartReplicationScratch scratch) {
        TransformHierarchy h = root.multipartHierarchy();
        ResourceLocation[] sorted = MultipartCanonicalPartOrder.sortedIds(
            h.nodes().stream().map(BodyPartNode::id).collect(Collectors.toList()));
        HashSet<ResourceLocation> touched = new HashSet<>();
        AtomicReference<MultipartDecodeStatus> sinkErr = new AtomicReference<>(MultipartDecodeStatus.OK);
        MultipartDecodeStatus head = MultipartTransformDeltaBinary.decodeV1(payload.packedOps(), h.topologyRevision(),
            new MultipartTransformDeltaBinary.Sink() {
                @Override
                public void onSimLocal(int sortedPartIndex, PartTransform pose) {
                    if (sinkErr.get() != MultipartDecodeStatus.OK) {
                        return;
                    }
                    if (sortedPartIndex < 0 || sortedPartIndex >= sorted.length) {
                        sinkErr.set(MultipartDecodeStatus.PART_INDEX_OOB);
                        return;
                    }
                    ResourceLocation id = sorted[sortedPartIndex];
                    BodyPartNode node = h.get(id);
                    if (node == null) {
                        sinkErr.set(MultipartDecodeStatus.PART_INDEX_OOB);
                        return;
                    }
                    node.simulationLocalTransform().set(pose);
                    touched.add(id);
                }

                @Override
                public void onAttached(int sortedPartIndex, boolean attachedToParent) {
                    if (sinkErr.get() != MultipartDecodeStatus.OK) {
                        return;
                    }
                    if (sortedPartIndex < 0 || sortedPartIndex >= sorted.length) {
                        sinkErr.set(MultipartDecodeStatus.PART_INDEX_OOB);
                        return;
                    }
                    ResourceLocation id = sorted[sortedPartIndex];
                    BodyPartNode node = h.get(id);
                    if (node == null) {
                        sinkErr.set(MultipartDecodeStatus.PART_INDEX_OOB);
                        return;
                    }
                    node.setAttachedToParent(attachedToParent);
                    touched.add(id);
                }
            }, scratch);
        if (head != MultipartDecodeStatus.OK) {
            return head;
        }
        MultipartDecodeStatus sinkStatus = sinkErr.get();
        if (sinkStatus != MultipartDecodeStatus.OK) {
            return sinkStatus;
        }
        try (var __ = h.beginEditBatch()) {
            for (ResourceLocation id : touched) {
                h.markSubtreeDirty(id);
            }
        }
        return MultipartDecodeStatus.OK;
    }

    public static MultipartDecodeStatus applyDamagePayload(RootMobEntity root, MultipartDamageSyncPayload payload) {
        return applyDamagePayload(root, payload, new MultipartReplicationScratch());
    }

    public static MultipartDecodeStatus applyDamagePayload(RootMobEntity root, MultipartDamageSyncPayload payload,
                                                           MultipartReplicationScratch scratch) {
        TransformHierarchy h = root.multipartHierarchy();
        ResourceLocation[] sorted = MultipartCanonicalPartOrder.sortedIds(
            h.nodes().stream().map(BodyPartNode::id).collect(Collectors.toList()));
        AtomicReference<MultipartDecodeStatus> sinkErr = new AtomicReference<>(MultipartDecodeStatus.OK);
        MultipartDecodeStatus head = MultipartDamageDeltaBinary.decodeV1(payload.packedDamage(), h.topologyRevision(),
            (sortedPartIndex, curHp, maxHp, functionalFlagBits, severed, destroyed, mask) -> {
                if (sinkErr.get() != MultipartDecodeStatus.OK) {
                    return;
                }
                if (sortedPartIndex < 0 || sortedPartIndex >= sorted.length) {
                    sinkErr.set(MultipartDecodeStatus.PART_INDEX_OOB);
                    return;
                }
                BodyPartNode node = h.get(sorted[sortedPartIndex]);
                if (node == null) {
                    sinkErr.set(MultipartDecodeStatus.PART_INDEX_OOB);
                    return;
                }
                PartDamageState dmg = node.damageState();
                if ((mask & MultipartDamageDeltaBinary.MASK_HAS_HP_MAX) != 0) {
                    dmg.setMaxHealth(maxHp);
                }
                if ((mask & MultipartDamageDeltaBinary.MASK_HAS_HP_CUR) != 0) {
                    dmg.setCurrentHealth(curHp);
                }
                if ((mask & MultipartDamageDeltaBinary.MASK_HAS_FLAGS) != 0) {
                    EnumSet<PartFunctionalFlag> unpacked = MultipartDamageDeltaBinary.unpackFunctionalFlags(functionalFlagBits);
                    for (PartFunctionalFlag f : PartFunctionalFlag.values()) {
                        dmg.removeFlag(f);
                    }
                    for (PartFunctionalFlag f : unpacked) {
                        dmg.addFlag(f);
                    }
                }
                dmg.setSevered(severed);
                dmg.setDestroyed(destroyed);
            }, scratch);
        if (head != MultipartDecodeStatus.OK) {
            return head;
        }
        return sinkErr.get();
    }

    public static MultipartDecodeStatus applyTopologyDeltaPayload(RootMobEntity root, MultipartTopologyDeltaPayload payload) {
        return applyTopologyDeltaPayload(root, payload, new MultipartReplicationScratch());
    }

    public static MultipartDecodeStatus applyTopologyDeltaPayload(RootMobEntity root, MultipartTopologyDeltaPayload payload,
                                                                  MultipartReplicationScratch scratch) {
        TransformHierarchy h = root.multipartHierarchy();
        AtomicReference<MultipartDecodeStatus> sinkErr = new AtomicReference<>(MultipartDecodeStatus.OK);
        MultipartDecodeStatus head = MultipartTopologyDeltaBinary.decodeV1(payload.packedTopologyOps(),
            payload.topologyRevision(),
            (parentPart, socketId, childPartOrNull) -> {
                if (sinkErr.get() != MultipartDecodeStatus.OK) {
                    return;
                }
                if (childPartOrNull == null) {
                    MultipartEditorHooks.clearSocket(h, parentPart, socketId);
                } else {
                    BodyPartNode child = h.get(childPartOrNull);
                    if (child == null) {
                        sinkErr.set(MultipartDecodeStatus.MISSING_CHILD_PART);
                        return;
                    }
                    MultipartEditorHooks.attachToSocket(h, parentPart, socketId, child);
                }
            }, scratch);
        if (head != MultipartDecodeStatus.OK) {
            return head;
        }
        return sinkErr.get();
    }
}
