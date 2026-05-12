package com.sirolf2009.necromancy.bodypart;

import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BodypartValidatedMigrationTest {

    @Test
    void legacyJsonWithoutValidatedFieldIsTreatedAsValidated() {
        String raw = """
            {"id":"necromancy:test_part","hitbox":{"ox":0,"oy":0,"oz":0,"sx":1,"sy":1,"sz":1},"flags":{},"attachments":[]}
            """;
        BodypartDefinitionJson json = BodypartDefinitionIo.fromJson(raw);
        assertNull(json.validated);
        BodypartDefinition def = BodypartDefinition.fromJson(json);
        assertTrue(def.validated());
    }

    @Test
    void explicitFalseValidatedSurvivesRoundTrip() {
        BodypartDefinitionJson j = new BodypartDefinitionJson();
        j.id = "necromancy:zombie_head";
        j.validated = Boolean.FALSE;
        BodypartDefinition def = BodypartDefinition.fromJson(j);
        assertFalse(def.validated());
        BodypartDefinitionJson out = def.toJson();
        assertEquals(Boolean.FALSE, out.validated);
    }
}
