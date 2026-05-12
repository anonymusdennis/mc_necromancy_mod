package com.sirolf2009.necromancy.multipart.editor.debug;

import com.sirolf2009.necromancy.multipart.RootMobEntity;
import com.sirolf2009.necromancy.multipart.network.MultipartClientEditLockCache;
import net.minecraft.world.entity.Entity;

/** Shared strings for lock diagnostics (works on integrated client; server uses {@link com.sirolf2009.necromancy.multipart.editor.session.MultipartServerTopologyEditService#describe}). */
public final class MultipartEditDebugProbe {

    private MultipartEditDebugProbe() {}

    /** Crosshair / inspector text fed into debug overlays. */
    public static String formatClientLockLine(Entity entity) {
        if (!(entity instanceof RootMobEntity)) {
            return "";
        }
        MultipartClientEditLockCache.Entry e = MultipartClientEditLockCache.probe(entity.getId());
        if (e == null) {
            return "[necromancy multipart] topology lock: open";
        }
        return "[necromancy multipart] topology lock: session=%d holder=%s".formatted(e.sessionGeneration(),
            e.holderUuid());
    }
}
