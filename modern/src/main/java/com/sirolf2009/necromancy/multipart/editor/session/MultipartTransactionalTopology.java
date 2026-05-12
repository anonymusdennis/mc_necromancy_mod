package com.sirolf2009.necromancy.multipart.editor.session;

import com.sirolf2009.necromancy.multipart.RootMobEntity;
import com.sirolf2009.necromancy.multipart.editor.MultipartEditorHooks;
import com.sirolf2009.necromancy.multipart.part.BodyPartNode;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.server.level.ServerPlayer;

/**
 * Topology mutations that must respect {@link MultipartServerTopologyEditService} leases — call after
 * {@link MultipartServerTopologyEditService#acquireLock(ServerLevel, LivingEntity, ServerPlayer)}.
 */
public final class MultipartTransactionalTopology {

    private MultipartTransactionalTopology() {}

    public static MultipartTopologyEditResult clearSocket(ServerLevel level, LivingEntity entity, ServerPlayer holder,
                                                          ResourceLocation parentId, ResourceLocation socketId) {
        MultipartTopologyEditResult gate = MultipartServerTopologyEditService.ensureLease(level, entity, holder);
        if (!gate.ok()) {
            return gate;
        }
        MultipartEditorHooks.clearSocket(((RootMobEntity) entity).multipartHierarchy(), parentId, socketId);
        return MultipartTopologyEditResult.success();
    }

    public static MultipartTopologyEditResult attachToSocket(ServerLevel level, LivingEntity entity, ServerPlayer holder,
                                                               ResourceLocation parentId, ResourceLocation socketId,
                                                               BodyPartNode child) {
        MultipartTopologyEditResult gate = MultipartServerTopologyEditService.ensureLease(level, entity, holder);
        if (!gate.ok()) {
            return gate;
        }
        MultipartEditorHooks.attachToSocket(((RootMobEntity) entity).multipartHierarchy(), parentId, socketId, child);
        return MultipartTopologyEditResult.success();
    }

    public static MultipartTopologyEditResult reparentFormerRoot(ServerLevel level, LivingEntity entity, ServerPlayer holder,
                                                                   ResourceLocation formerRootId,
                                                                   ResourceLocation newParentId, ResourceLocation socketId) {
        MultipartTopologyEditResult gate = MultipartServerTopologyEditService.ensureLease(level, entity, holder);
        if (!gate.ok()) {
            return gate;
        }
        MultipartEditorHooks.reparentFormerRootAsSocketChild(((RootMobEntity) entity).multipartHierarchy(),
            formerRootId, newParentId, socketId);
        return MultipartTopologyEditResult.success();
    }
}
