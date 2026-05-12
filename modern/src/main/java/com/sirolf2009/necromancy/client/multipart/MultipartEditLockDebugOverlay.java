package com.sirolf2009.necromancy.client.multipart;

import com.sirolf2009.necromancy.multipart.RootMobEntity;
import com.sirolf2009.necromancy.multipart.editor.debug.MultipartEditDebugProbe;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.CustomizeGuiOverlayEvent;

/**
 * Appends multipart topology lease lines to the F3 debug panel when enabled.
 *
 * <p>Registered on the game event bus from {@link com.sirolf2009.necromancy.Necromancy}.
 */
public final class MultipartEditLockDebugOverlay {

    private MultipartEditLockDebugOverlay() {}

    @SubscribeEvent
    public static void onDebugText(CustomizeGuiOverlayEvent.DebugText event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.options.hideGui || !mc.getDebugOverlay().showDebugScreen()) {
            return;
        }
        HitResult hit = mc.hitResult;
        if (!(hit instanceof EntityHitResult ehit)) {
            return;
        }
        Entity looked = ehit.getEntity();
        if (!(looked instanceof RootMobEntity)) {
            return;
        }
        event.getLeft().add(MultipartEditDebugProbe.formatClientLockLine(looked));
    }
}
