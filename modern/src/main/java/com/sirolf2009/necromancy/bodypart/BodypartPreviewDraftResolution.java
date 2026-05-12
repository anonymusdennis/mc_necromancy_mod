package com.sirolf2009.necromancy.bodypart;

import com.sirolf2009.necromancy.block.entity.BlockEntityBodypartDev;
import com.sirolf2009.necromancy.entity.EntityBodypartPreview;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Single precedence order for bodypart preview definition: live typing draft → synced BE draft → disk manager.
 * Keeps the last good definition per dev block + part id when the live JSON string is briefly unparseable so meshes/outlines do not thrash.
 */
public final class BodypartPreviewDraftResolution {

    private static final ConcurrentHashMap<String, BodypartDefinition> STICKY = new ConcurrentHashMap<>();

    private BodypartPreviewDraftResolution() {}

    private record FreshResult(BodypartDefinition def, String source) {}

    private static @Nullable String stickyKey(EntityBodypartPreview entity) {
        ResourceLocation id = entity.getPartIdRl();
        if (id == null) return null;
        return entity.getDevBlockPacked() + ":" + id;
    }

    public static Optional<BodypartDefinition> resolve(EntityBodypartPreview entity, @Nullable Level level) {
        String key = stickyKey(entity);
        FreshResult fresh = tryResolveFresh(entity, level);
        if (fresh != null) {
            if (key != null) STICKY.put(key, fresh.def());
            BodypartPreviewDiagnostics.logResolve(entity, fresh.source(), fresh.def());
            return Optional.of(fresh.def());
        }
        if (key != null) {
            BodypartDefinition cached = STICKY.get(key);
            if (cached != null) {
                BodypartPreviewDiagnostics.logResolve(entity, "sticky", cached);
                return Optional.of(cached);
            }
        }
        return Optional.empty();
    }

    private static @Nullable FreshResult tryResolveFresh(EntityBodypartPreview entity, @Nullable Level level) {
        long packed = entity.getDevBlockPacked();
        String live = BodypartDevLiveDraft.getUtf8OrEmpty(packed);
        if (!live.isEmpty()) {
            try {
                BodypartDefinition d = BodypartDefinition.fromJson(BodypartDefinitionIo.fromJson(live));
                return new FreshResult(d, "live");
            } catch (Exception ignored) {
            }
        }
        if (level != null && packed != 0L) {
            BlockEntity be = level.getBlockEntity(BlockPos.of(packed));
            if (be instanceof BlockEntityBodypartDev dev) {
                try {
                    BodypartDefinition d = BodypartDefinition.fromJson(BodypartDefinitionIo.fromJson(dev.getDraftJson()));
                    return new FreshResult(d, "be");
                } catch (Exception ignored) {
                }
            }
        }
        ResourceLocation id = entity.getPartIdRl();
        if (id == null) return null;
        return BodyPartConfigManager.INSTANCE.get(id)
            .map(d -> new FreshResult(d, "disk"))
            .orElse(null);
    }
}
