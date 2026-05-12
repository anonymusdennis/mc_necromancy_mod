package com.sirolf2009.necromancy.multipart.network;

import com.sirolf2009.necromancy.multipart.network.payload.MultipartDamageSyncPayload;
import com.sirolf2009.necromancy.multipart.network.payload.MultipartTopologyDeltaPayload;
import com.sirolf2009.necromancy.multipart.network.payload.MultipartTopologyNotifyPayload;
import com.sirolf2009.necromancy.multipart.network.payload.MultipartTransformDeltaPayload;
import com.sirolf2009.necromancy.multipart.telemetry.MultipartTelemetry;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;

/**
 * Server-side helpers for fan-out multipart deltas (trackers / entity-bound audiences added later).
 */
public final class MultipartReplication {

    private MultipartReplication() {}

    public static void notifyTopology(ServerPlayer viewer, MultipartTopologyNotifyPayload payload) {
        MultipartTelemetry.recordNetSentTopologyNotify(payload.entityId());
        PacketDistributor.sendToPlayer(viewer, payload);
    }

    public static void pushTopologyDelta(ServerPlayer viewer, MultipartTopologyDeltaPayload payload) {
        MultipartTelemetry.recordNetSentTopologyDelta(payload.entityId(), payload.packedTopologyOps());
        PacketDistributor.sendToPlayer(viewer, payload);
    }

    public static void pushTransformDelta(ServerPlayer viewer, MultipartTransformDeltaPayload payload) {
        MultipartTelemetry.recordNetSentTransformDelta(payload.entityId(), payload.packedOps());
        PacketDistributor.sendToPlayer(viewer, payload);
    }

    public static void pushDamageSync(ServerPlayer viewer, MultipartDamageSyncPayload payload) {
        MultipartTelemetry.recordNetSentDamageSync(payload.entityId(), payload.packedDamage());
        PacketDistributor.sendToPlayer(viewer, payload);
    }
}
