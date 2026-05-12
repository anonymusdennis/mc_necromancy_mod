package com.sirolf2009.necromancy.multipart.telemetry;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MultipartTelemetryTest {

    @AfterEach
    void tearDown() {
        MultipartTelemetry.testingSetProfilingOverride(null);
        MultipartTelemetry.reset();
    }

    @Test
    void recordsTransformPropagationsWhenForcedEnabled() {
        MultipartTelemetry.testingSetProfilingOverride(true);
        MultipartTelemetry.recordTransformPropagation(42);
        MultipartTelemetry.recordTransformPropagation(42);
        assertEquals(2L, MultipartTelemetry.testingTransformPropagations());
        assertTrue(MultipartTelemetry.formatPerEntityLines(42, 8).getFirst().contains("xf=2"));
    }

    @Test
    void profilingDisabledByDefaultSkipsMutationThroughPublicApi() {
        MultipartTelemetry.testingSetProfilingOverride(null);
        MultipartTelemetry.recordTransformPropagation(7);
        assertEquals(0L, MultipartTelemetry.testingTransformPropagations());
    }
}
