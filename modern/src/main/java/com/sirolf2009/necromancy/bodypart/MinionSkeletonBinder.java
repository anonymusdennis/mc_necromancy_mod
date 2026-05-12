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

import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
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

            BodyPartNode rootNode = createNode(rootId, rootDef, rootLoc);
            hierarchy.registerRoot(rootNode);
            rootNode.simulationLocalTransform().setTranslation(slotOffset(rootLoc));

            for (Map.Entry<BodyPartLocation, ResourceLocation> e : filled.entrySet()) {
                if (e.getKey() == rootLoc) continue;
                BodypartDefinition def = BodyPartConfigManager.INSTANCE.get(e.getValue()).orElse(null);
                if (def == null) continue;
                BodyPartNode child = createNode(e.getValue(), def, e.getKey());
                Vec3 delta = slotOffset(e.getKey()).subtract(slotOffset(rootLoc));

                // Sort attachments by priority so the highest-priority socket is primary.
                List<BodypartAttachmentJson> attachments = new ArrayList<>(def.attachments());
                attachments.sort(Comparator.comparingInt(a -> a.priority));

                // Build the primary socket transform: base delta + attachment fine-tune (ox/oy/oz) + rotation.
                PartTransform socket = PartTransform.identity();
                if (!attachments.isEmpty()) {
                    BodypartAttachmentJson primary = attachments.get(0);
                    socket.setTranslation(delta.add(primary.ox, primary.oy, primary.oz));
                    Quaternionf q = new Quaternionf();
                    primary.socketLocalTransform().rotationInto(q);
                    socket.setRotation(q);
                } else {
                    socket.setTranslation(delta);
                }

                ResourceLocation primarySock = Reference.rl("minion_socket_" + e.getKey().name().toLowerCase());
                rootNode.addAttachmentPoint(new AttachmentPoint(primarySock, socket, SocketBindSpace.SIMULATION, child.id(), 0));
                hierarchy.registerChild(child, rootNode.id());

                // Register additional sockets declared on the child as named empty attachment points on the child.
                // These represent sub-connection points (e.g. torso exposing left_arm_socket, right_arm_socket).
                if (attachments.size() > 1) {
                    for (int i = 1; i < attachments.size(); i++) {
                        BodypartAttachmentJson aj = attachments.get(i);
                        String sockName = (aj.name != null && !aj.name.isBlank()) ? aj.name : ("socket_" + i);
                        // Include slot name as prefix to avoid collisions across different child parts.
                        ResourceLocation sockId = Reference.rl(e.getKey().name().toLowerCase() + "/" + child.id().getPath() + "/" + sockName);
                        PartTransform sockTransform = aj.socketLocalTransform();
                        // childPartId=null: empty socket for future sub-part wiring.
                        child.addAttachmentPoint(new AttachmentPoint(sockId, sockTransform, SocketBindSpace.SIMULATION, null, aj.priority));
                    }
                }
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

    private static BodyPartNode createNode(ResourceLocation partId, BodypartDefinition def, BodyPartLocation loc) {
        // The hitbox stored in BodypartDefinition.localHitbox() is in entity-feet-relative
        // coordinates (i.e. referenced from entity origin = feet level), because that is how
        // the bodypart-dev preview tool displays and allows the author to configure them.
        //
        // A node's world position inside the TransformHierarchy is:
        //   nodeWorld = entity + slotOffset(loc)
        //
        // The OrientedLocalVolume is then positioned relative to the node origin, so to keep
        // the damage hitbox at the author's intended entity-feet-relative position we must
        // subtract the node's slot offset from the AABB before wrapping it:
        //   localHitbox_nodeRelative = localHitbox_entityRelative.move(-slotOffset)
        Vec3 slotOff = slotOffset(loc);
        net.minecraft.world.phys.AABB raw = def.localHitbox();
        net.minecraft.world.phys.AABB nodeLocal = raw.move(-slotOff.x, -slotOff.y, -slotOff.z);
        OrientedLocalVolume vol = OrientedLocalVolume.fromAxisAlignedBox(nodeLocal);
        HitboxComponent hb = new HitboxComponent.FixedOrientedHitbox(vol, true);
        return new BodyPartNode(partId, hb);
    }
}
