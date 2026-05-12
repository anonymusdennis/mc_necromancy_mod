package com.sirolf2009.necromancy.multipart;

import com.sirolf2009.necromancy.bodypart.BodypartDefinition;
import com.sirolf2009.necromancy.bodypart.BodypartDefinitionIo;
import com.sirolf2009.necromancy.multipart.collision.OrientedLocalVolume;
import com.sirolf2009.necromancy.multipart.part.BodyPartNode;
import com.sirolf2009.necromancy.multipart.part.HitboxComponent;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * RC-B2-style invariant: topology revisions must batch cleanly across multi-step edits.
 */
class TransformHierarchyTopologyRevisionTest {

    private static BodyPartNode node(String id) {
        String raw = """
            {"id":"%s","hitbox":{"ox":0,"oy":0,"oz":0,"sx":1,"sy":1,"sz":1},"flags":{},"attachments":[]}
            """.formatted(id);
        BodypartDefinition def = BodypartDefinition.fromJson(BodypartDefinitionIo.fromJson(raw));
        OrientedLocalVolume vol = OrientedLocalVolume.fromAxisAlignedBox(def.localHitbox());
        HitboxComponent hb = new HitboxComponent.FixedOrientedHitbox(vol, true);
        return new BodyPartNode(ResourceLocation.parse(id), hb);
    }

    @Test
    void registerRootOutsideBatchIncrementsTopologyOncePerMutation() {
        TransformHierarchy h = new TransformHierarchy();
        int r0 = h.topologyRevision();
        h.registerRoot(node("necromancy:root"));
        assertEquals(r0 + 1, h.topologyRevision());
    }

    @Test
    void batchesMultipleStructuralMutationsIntoSingleTopologyBump() {
        TransformHierarchy h = new TransformHierarchy();
        int r0 = h.topologyRevision();
        BodyPartNode root = node("necromancy:root");
        BodyPartNode c1 = node("necromancy:c1");
        BodyPartNode c2 = node("necromancy:c2");
        try (HierarchyEditBatch ignored = h.beginEditBatch()) {
            h.registerRoot(root);
            h.registerChild(c1, root.id());
            h.registerChild(c2, root.id());
        }
        assertEquals(r0 + 1, h.topologyRevision());
    }
}
