package com.sirolf2009.necromancy.bodypart;

import com.sirolf2009.necromancy.Reference;
import com.sirolf2009.necromancy.api.BodyPartLocation;
import com.sirolf2009.necromancy.entity.EntityMinion;
import com.sirolf2009.necromancy.multipart.TransformHierarchy;
import com.sirolf2009.necromancy.multipart.collision.OrientedLocalVolume;
import com.sirolf2009.necromancy.multipart.math.PartTransform;
import com.sirolf2009.necromancy.multipart.part.AttachmentPoint;
import com.sirolf2009.necromancy.multipart.part.BodyPartNode;
import com.sirolf2009.necromancy.multipart.part.HitboxComponent;
import com.sirolf2009.necromancy.multipart.part.SocketBindSpace;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;

import java.util.EnumMap;
import java.util.Map;

/**
 * Builds a {@link TransformHierarchy} for {@link EntityMinion} from the five altar slots.
 * Topology mirrors {@link MinionCompositeCollision} slot offsets (torso-root hub) until attachment JSON drives sockets.
 */
public final class MinionSkeletonBinder {

    private MinionSkeletonBinder() {}

    public static void rebuild(EntityMinion minion, TransformHierarchy hierarchy) {
        try (var batch = hierarchy.beginEditBatch()) {
            hierarchy.clearStructureInBatch();
            EnumMap<BodyPartLocation, ResourceLocation> filled = new EnumMap<>(BodyPartLocation.class);
            for (BodyPartLocation loc : BodyPartLocation.values()) {
                String mobName = minion.getBodyPartName(loc);
                if (mobName == null || mobName.isEmpty()) continue;
                ResourceLocation partId = BodyPartItemIds.inferredPartId(mobName, loc);
                if (!BodyPartConfigManager.INSTANCE.has(partId)) continue;
                filled.put(loc, partId);
            }
            if (filled.isEmpty()) {
                return;
            }
            BodyPartLocation rootLoc = chooseHubBone(filled);
            ResourceLocation rootId = filled.get(rootLoc);
            BodypartDefinition rootDef = BodyPartConfigManager.INSTANCE.get(rootId).orElse(null);
            if (rootDef == null) return;

            BodyPartNode rootNode = createNode(rootId, rootDef);
            hierarchy.registerRoot(rootNode);
            rootNode.simulationLocalTransform().setTranslation(slotOffset(rootLoc));

            for (Map.Entry<BodyPartLocation, ResourceLocation> e : filled.entrySet()) {
                if (e.getKey() == rootLoc) continue;
                BodypartDefinition def = BodyPartConfigManager.INSTANCE.get(e.getValue()).orElse(null);
                if (def == null) continue;
                BodyPartNode child = createNode(e.getValue(), def);
                Vec3 delta = slotOffset(e.getKey()).subtract(slotOffset(rootLoc));
                ResourceLocation sock = Reference.rl("minion_socket_" + e.getKey().name().toLowerCase());
                PartTransform socket = PartTransform.identity();
                socket.setTranslation(delta);
                if (!def.attachments().isEmpty()) {
                    BodypartAttachmentJson aj = def.attachments().get(0);
                    Quaternionf q = new Quaternionf();
                    aj.socketLocalTransform().rotationInto(q);
                    socket.setRotation(q);
                }
                rootNode.addAttachmentPoint(new AttachmentPoint(sock, socket, SocketBindSpace.SIMULATION, child.id(), 0));
                hierarchy.registerChild(child, rootNode.id());
            }
        }
    }

    /**
     * Deterministic hub bone for the altar-era star topology (RC-B1 ordering contract).
     * Package-private so tests can lock selection rules without booting Minecraft.
     */
    static BodyPartLocation chooseHubBone(Map<BodyPartLocation, ResourceLocation> filled) {
        if (filled.containsKey(BodyPartLocation.Torso)) return BodyPartLocation.Torso;
        if (filled.containsKey(BodyPartLocation.Head)) return BodyPartLocation.Head;
        if (filled.containsKey(BodyPartLocation.Legs)) return BodyPartLocation.Legs;
        if (filled.containsKey(BodyPartLocation.ArmRight)) return BodyPartLocation.ArmRight;
        return BodyPartLocation.ArmLeft;
    }

    private static Vec3 slotOffset(BodyPartLocation loc) {
        return switch (loc) {
            case Head -> new Vec3(0, 1.45, 0);
            case Torso -> new Vec3(0, 0.92, 0);
            case Legs -> new Vec3(0, 0.42, 0);
            case ArmLeft -> new Vec3(-0.32, 1.05, 0);
            case ArmRight -> new Vec3(0.32, 1.05, 0);
        };
    }

    private static BodyPartNode createNode(ResourceLocation partId, BodypartDefinition def) {
        OrientedLocalVolume vol = OrientedLocalVolume.fromAxisAlignedBox(def.localHitbox());
        HitboxComponent hb = new HitboxComponent.FixedOrientedHitbox(vol, true);
        return new BodyPartNode(partId, hb);
    }
}
