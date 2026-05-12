package com.sirolf2009.necromancy.altar.mock;

import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AltarAttachmentGridMockTest {

    @Test
    void nearestOpenPrefersCloserGridSlot() {
        List<AltarAttachmentGridMock.SlotMock> snap = List.of(
            new AltarAttachmentGridMock.SlotMock(0, AltarAttachmentGridMock.GridSlotState.OCCUPIED, ResourceLocation.parse("necromancy:a")),
            new AltarAttachmentGridMock.SlotMock(1, AltarAttachmentGridMock.GridSlotState.OPEN, null),
            new AltarAttachmentGridMock.SlotMock(4, AltarAttachmentGridMock.GridSlotState.OPEN, null)
        );
        // insertFromEdge index 2 (top row centre) -> nearer to slot 1 than slot 4
        assertEquals(1, AltarAttachmentGridMock.nearestOpenSlot(2, snap));
    }
}
