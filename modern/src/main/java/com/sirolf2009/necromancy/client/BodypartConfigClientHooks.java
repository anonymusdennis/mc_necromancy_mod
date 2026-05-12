package com.sirolf2009.necromancy.client;

import com.sirolf2009.necromancy.Reference;
import com.sirolf2009.necromancy.bodypart.BodyPartConfigManager;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;

/** Loads bodypart JSON from the shared config folder on the client (single-player dev previews). */
@EventBusSubscriber(modid = Reference.MOD_ID, value = Dist.CLIENT)
public final class BodypartConfigClientHooks {
    private BodypartConfigClientHooks() {}

    @SubscribeEvent
    public static void onLoggingIn(ClientPlayerNetworkEvent.LoggingIn event) {
        BodyPartConfigManager.INSTANCE.reloadFromDisk();
    }
}
