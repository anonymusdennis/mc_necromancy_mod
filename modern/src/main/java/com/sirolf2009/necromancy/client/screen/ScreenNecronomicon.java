package com.sirolf2009.necromancy.client.screen;

import com.sirolf2009.necromancy.entity.EntityMinion;
import com.sirolf2009.necromancy.network.MinionCommandPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.List;

/**
 * The Necronomicon's hand-held screen.  Shows nearby minions and lets the
 * player toggle their behaviour or dismiss them.
 *
 * <p>The 1.7.10 mod opened a custom paged UI.  We provide a minimal but
 * functional equivalent here: list nearby tamed minions, expose Sit/Follow
 * buttons per minion, and a Dismiss button.
 *
 * <p>Notably, we override {@link #renderBackground(GuiGraphics, int, int, float)}
 * to draw a flat semi-transparent backdrop.  The vanilla blur shader has a
 * higher z-index than custom widgets, which made the legacy UX unusable.
 */
public class ScreenNecronomicon extends Screen {

    private final List<EntityMinion> minions = new ArrayList<>();

    public ScreenNecronomicon() {
        super(Component.translatable("container.necromancy.necronomicon"));
    }

    @Override
    protected void init() {
        super.init();
        minions.clear();
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null) {
            AABB aabb = mc.player.getBoundingBox().inflate(32);
            mc.player.level().getEntitiesOfClass(EntityMinion.class, aabb).stream()
                .filter(m -> mc.player.getUUID().equals(m.getOwnerUUID()))
                .forEach(minions::add);
        }

        int y = 30;
        for (EntityMinion m : minions) {
            int eid = m.getId();

            addRenderableWidget(Button.builder(
                Component.translatable(m.isOrderedToSit()
                    ? "necromancy.necronomicon.follow"
                    : "necromancy.necronomicon.sit"), b -> {
                PacketDistributor.sendToServer(new MinionCommandPacket(eid, MinionCommandPacket.Op.TOGGLE_SIT));
                this.onClose();
            }).bounds(this.width / 2 - 100, y, 95, 20).build());

            addRenderableWidget(Button.builder(
                Component.translatable("necromancy.necronomicon.dismiss"), b -> {
                PacketDistributor.sendToServer(new MinionCommandPacket(eid, MinionCommandPacket.Op.DISMISS));
                this.onClose();
            }).bounds(this.width / 2 + 5, y, 95, 20).build());

            y += 24;
        }

        addRenderableWidget(Button.builder(
            Component.translatable("gui.done"), b -> this.onClose())
            .bounds(this.width / 2 - 50, this.height - 30, 100, 20).build());
    }

    @Override
    public void renderBackground(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        g.fill(0, 0, this.width, this.height, 0xC0000000);
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        renderBackground(g, mouseX, mouseY, partialTick);
        super.render(g, mouseX, mouseY, partialTick);
        Component header = minions.isEmpty()
            ? Component.translatable("necromancy.necronomicon.no_minions")
            : Component.translatable("necromancy.necronomicon.minion_count", minions.size());
        g.drawCenteredString(this.font, header, this.width / 2, 10, 0xFFFFFF);
    }

    @Override
    public boolean isPauseScreen() { return false; }
}
