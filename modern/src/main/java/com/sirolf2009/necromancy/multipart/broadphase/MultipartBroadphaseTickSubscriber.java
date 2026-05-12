package com.sirolf2009.necromancy.multipart.broadphase;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

/**
 * Defines the query/read boundary: after level ticking completes, multipart broad-phase structures are frozen into
 * {@link MultipartBroadphaseSnapshot} instances cheaply when idle.
 * <p>
 * Registered on {@link net.neoforged.neoforge.common.NeoForge#EVENT_BUS} from {@link com.sirolf2009.necromancy.event.NecromancyEvents}.
 */
public final class MultipartBroadphaseTickSubscriber {

    private MultipartBroadphaseTickSubscriber() {
    }

    @SubscribeEvent
    public static void onLevelTickPost(LevelTickEvent.Post event) {
        MultipartBroadphaseRegistry.publishReadSnapshot(event.getLevel());
    }
}
