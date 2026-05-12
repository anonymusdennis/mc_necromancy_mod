package com.sirolf2009.necromancy.network;

import com.sirolf2009.necromancy.Reference;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

/**
 * Network entry point.
 *
 * <p>Modern equivalent of legacy {@code NetworkHelper}.  Custom payloads (the
 * Isaac-tear shot keybind packet) are registered here and handled in their
 * own classes inside {@code com.sirolf2009.necromancy.network.payload}.
 */
public final class NecromancyNetwork {

    public static final String VERSION = "1";

    private NecromancyNetwork() {}

    public static void register(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(Reference.MOD_ID).versioned(VERSION);
        registrar.playToServer(
            com.sirolf2009.necromancy.network.payload.TearShotPayload.TYPE,
            com.sirolf2009.necromancy.network.payload.TearShotPayload.STREAM_CODEC,
            com.sirolf2009.necromancy.network.payload.TearShotPayload::handle);
        registrar.playToServer(
            MinionCommandPacket.TYPE,
            MinionCommandPacket.STREAM_CODEC,
            MinionCommandPacket::handle);
        registrar.playToServer(
            com.sirolf2009.necromancy.network.payload.BodypartDevApplyPayload.TYPE,
            com.sirolf2009.necromancy.network.payload.BodypartDevApplyPayload.STREAM_CODEC,
            com.sirolf2009.necromancy.network.payload.BodypartDevApplyPayload::handle);
        registrar.playToServer(
            com.sirolf2009.necromancy.network.payload.BodypartDevSavePayload.TYPE,
            com.sirolf2009.necromancy.network.payload.BodypartDevSavePayload.STREAM_CODEC,
            com.sirolf2009.necromancy.network.payload.BodypartDevSavePayload::handle);
        registrar.playToClient(
            com.sirolf2009.necromancy.multipart.network.payload.MultipartTopologyNotifyPayload.TYPE,
            com.sirolf2009.necromancy.multipart.network.payload.MultipartTopologyNotifyPayload.STREAM_CODEC,
            com.sirolf2009.necromancy.multipart.network.payload.MultipartTopologyNotifyPayload::handle);
        registrar.playToClient(
            com.sirolf2009.necromancy.multipart.network.payload.MultipartEditLockNotifyPayload.TYPE,
            com.sirolf2009.necromancy.multipart.network.payload.MultipartEditLockNotifyPayload.STREAM_CODEC,
            com.sirolf2009.necromancy.multipart.network.payload.MultipartEditLockNotifyPayload::handle);
        registrar.playToClient(
            com.sirolf2009.necromancy.multipart.network.payload.MultipartTopologyDeltaPayload.TYPE,
            com.sirolf2009.necromancy.multipart.network.payload.MultipartTopologyDeltaPayload.STREAM_CODEC,
            com.sirolf2009.necromancy.multipart.network.payload.MultipartTopologyDeltaPayload::handle);
        registrar.playToClient(
            com.sirolf2009.necromancy.multipart.network.payload.MultipartTransformDeltaPayload.TYPE,
            com.sirolf2009.necromancy.multipart.network.payload.MultipartTransformDeltaPayload.STREAM_CODEC,
            com.sirolf2009.necromancy.multipart.network.payload.MultipartTransformDeltaPayload::handle);
        registrar.playToClient(
            com.sirolf2009.necromancy.multipart.network.payload.MultipartDamageSyncPayload.TYPE,
            com.sirolf2009.necromancy.multipart.network.payload.MultipartDamageSyncPayload.STREAM_CODEC,
            com.sirolf2009.necromancy.multipart.network.payload.MultipartDamageSyncPayload::handle);
    }
}
