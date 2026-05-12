package com.sirolf2009.necromancy.client.screen;

import com.sirolf2009.necromancy.bodypart.BodypartAttachmentJson;
import com.sirolf2009.necromancy.bodypart.BodypartDevLiveDraft;
import com.sirolf2009.necromancy.bodypart.BodypartDefinitionIo;
import com.sirolf2009.necromancy.bodypart.BodypartDefinitionJson;
import com.sirolf2009.necromancy.bodypart.BodypartPreviewMask;
import com.sirolf2009.necromancy.bodypart.BodypartVisualOffsetJson;
import com.sirolf2009.necromancy.gui.BodypartDevLayout;
import com.sirolf2009.necromancy.inventory.ContainerBodypartDev;
import com.sirolf2009.necromancy.network.payload.BodypartDevApplyPayload;
import com.sirolf2009.necromancy.network.payload.BodypartDevSavePayload;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.network.PacketDistributor;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class ScreenBodypartDev extends AbstractContainerScreen<ContainerBodypartDev> {

    private enum Tab { GEOMETRY, FLAGS, SOCKETS, PREVIEW }

    private Tab activeTab = Tab.GEOMETRY;

    private BodypartDefinitionJson draft;
    private int attachmentIndex;

    private EditBox ox, oy, oz, sx, sy, sz;
    private EditBox vx, vy, vz;
    private EditBox attOx, attOy, attOz, attPri, attName;
    private EditBox pivotOx, pivotOy, pivotOz;
    private EditBox eulerYaw, eulerPitch, eulerRoll;
    private EditBox quatX, quatY, quatZ, quatW;

    private Checkbox chkFlagHead, chkFlagTorso, chkFlagArm, chkFlagLeg, chkFlagSpecial;
    private Checkbox chkPvMesh, chkPvCollision, chkPvSockets, chkPvPivots;

    private final List<AbstractWidget> tabGeometry = new ArrayList<>();
    private final List<AbstractWidget> tabFlags = new ArrayList<>();
    private final List<AbstractWidget> tabSockets = new ArrayList<>();
    private final List<AbstractWidget> tabPreview = new ArrayList<>();

    public ScreenBodypartDev(ContainerBodypartDev menu, Inventory inv, Component title) {
        super(menu, inv, title);
        this.imageWidth = BodypartDevLayout.PANEL_WIDTH;
        this.imageHeight = BodypartDevLayout.PANEL_HEIGHT;
        this.inventoryLabelY = BodypartDevLayout.INVENTORY_LABEL_Y;
    }

    @Override
    protected void init() {
        super.init();
        this.titleLabelX = BodypartDevLayout.EDITOR_LEFT;
        this.titleLabelY = 8;

        reloadDraftFromBlockEntity();

        int lx = leftPos + BodypartDevLayout.EDITOR_LEFT;
        int tabW = BodypartDevLayout.tabButtonWidth();
        int tabGap = BodypartDevLayout.TAB_COLUMN_GAP;
        int tabY1 = topPos + BodypartDevLayout.TAB_ROW1_Y;
        int tabY2 = topPos + BodypartDevLayout.TAB_ROW2_Y;
        int th = BodypartDevLayout.TAB_BUTTON_HEIGHT;
        addRenderableWidget(Button.builder(Component.translatable("gui.necromancy.bodypart_dev.tab_geometry"),
            b -> selectTab(Tab.GEOMETRY)).bounds(lx, tabY1, tabW, th).build());
        addRenderableWidget(Button.builder(Component.translatable("gui.necromancy.bodypart_dev.tab_flags"),
            b -> selectTab(Tab.FLAGS)).bounds(lx + tabW + tabGap, tabY1, tabW, th).build());
        addRenderableWidget(Button.builder(Component.translatable("gui.necromancy.bodypart_dev.tab_sockets"),
            b -> selectTab(Tab.SOCKETS)).bounds(lx, tabY2, tabW, th).build());
        addRenderableWidget(Button.builder(Component.translatable("gui.necromancy.bodypart_dev.tab_preview"),
            b -> selectTab(Tab.PREVIEW)).bounds(lx + tabW + tabGap, tabY2, tabW, th).build());

        int row = topPos + BodypartDevLayout.CONTENT_TOP;
        int rowGap = 22;

        ox = eb(lx, row, 50, "ox"); oy = eb(lx + 54, row, 50, "oy"); oz = eb(lx + 108, row, 50, "oz");
        sx = eb(lx + 166, row, 50, "sx"); sy = eb(lx + 220, row, 50, "sy"); sz = eb(lx + 274, row, 50, "sz");
        addGeom(ox); addGeom(oy); addGeom(oz); addGeom(sx); addGeom(sy); addGeom(sz);

        row += rowGap;
        vx = eb(lx, row, 54, "vx"); vy = eb(lx + 58, row, 54, "vy"); vz = eb(lx + 116, row, 54, "vz");
        addGeom(vx); addGeom(vy); addGeom(vz);

        row = topPos + BodypartDevLayout.CONTENT_TOP;
        chkFlagHead = Checkbox.builder(Component.translatable("gui.necromancy.bodypart_dev.slot_head"), font)
            .pos(lx, row).selected(draft.flags != null && draft.flags.head).build();
        chkFlagTorso = Checkbox.builder(Component.translatable("gui.necromancy.bodypart_dev.slot_torso"), font)
            .pos(lx + 150, row).selected(draft.flags != null && draft.flags.torso).build();
        chkFlagArm = Checkbox.builder(Component.translatable("gui.necromancy.bodypart_dev.slot_arm"), font)
            .pos(lx, row + 24).selected(draft.flags != null && draft.flags.arm).build();
        chkFlagLeg = Checkbox.builder(Component.translatable("gui.necromancy.bodypart_dev.slot_leg"), font)
            .pos(lx + 150, row + 24).selected(draft.flags != null && draft.flags.leg).build();
        chkFlagSpecial = Checkbox.builder(Component.translatable("gui.necromancy.bodypart_dev.slot_special"), font)
            .pos(lx, row + 48).selected(draft.flags != null && draft.flags.special).build();
        addFlag(chkFlagHead); addFlag(chkFlagTorso); addFlag(chkFlagArm); addFlag(chkFlagLeg); addFlag(chkFlagSpecial);

        row = topPos + BodypartDevLayout.CONTENT_TOP;
        attName = eb(lx, row, 140, "sock");
        attOx = eb(lx + 146, row, 42, "ax"); attOy = eb(lx + 190, row, 42, "ay"); attOz = eb(lx + 234, row, 42, "az");
        attPri = eb(lx + 282, row, 36, "pri");
        addSock(attName); addSock(attOx); addSock(attOy); addSock(attOz); addSock(attPri);

        row += rowGap;
        eulerYaw = eb(lx, row, 52, "ey"); eulerPitch = eb(lx + 56, row, 52, "ep"); eulerRoll = eb(lx + 112, row, 52, "er");
        addSock(eulerYaw); addSock(eulerPitch); addSock(eulerRoll);

        row += rowGap;
        quatX = eb(lx, row, 52, "qx"); quatY = eb(lx + 56, row, 52, "qy");
        quatZ = eb(lx + 112, row, 52, "qz"); quatW = eb(lx + 168, row, 52, "qw");
        addSock(quatX); addSock(quatY); addSock(quatZ); addSock(quatW);

        row += rowGap;
        pivotOx = eb(lx, row, 54, "px"); pivotOy = eb(lx + 58, row, 54, "py"); pivotOz = eb(lx + 116, row, 54, "pz");
        addSock(pivotOx); addSock(pivotOy); addSock(pivotOz);

        Button bPrev = Button.builder(Component.literal("<"), b -> bumpAtt(-1)).bounds(lx + 190, row, 28, 18).build();
        Button bNext = Button.builder(Component.literal(">"), b -> bumpAtt(1)).bounds(lx + 222, row, 28, 18).build();
        Button bAdd = Button.builder(Component.literal("+"), b -> addAtt()).bounds(lx + 256, row, 28, 18).build();
        Button bRem = Button.builder(Component.literal("-"), b -> removeAtt()).bounds(lx + 288, row, 28, 18).build();
        addSock(bPrev); addSock(bNext); addSock(bAdd); addSock(bRem);

        int pm = menu.getDev().getPreviewVisibilityMask();
        int pvRow = topPos + BodypartDevLayout.CONTENT_TOP;
        int pvGap = 26;
        chkPvMesh = Checkbox.builder(Component.translatable("gui.necromancy.bodypart_dev.chk_mesh"), font)
            .pos(lx, pvRow).selected((pm & BodypartPreviewMask.MESH) != 0).build();
        chkPvCollision = Checkbox.builder(Component.translatable("gui.necromancy.bodypart_dev.chk_collision_outline"), font)
            .pos(lx, pvRow + pvGap).selected((pm & BodypartPreviewMask.COLLISION_OUTLINE) != 0).build();
        chkPvSockets = Checkbox.builder(Component.translatable("gui.necromancy.bodypart_dev.chk_socket_markers"), font)
            .pos(lx, pvRow + pvGap * 2).selected((pm & BodypartPreviewMask.SOCKET_MARKERS) != 0).build();
        chkPvPivots = Checkbox.builder(Component.translatable("gui.necromancy.bodypart_dev.chk_pivot_markers"), font)
            .pos(lx, pvRow + pvGap * 3).selected((pm & BodypartPreviewMask.PIVOT_MARKERS) != 0).build();
        addPv(chkPvMesh); addPv(chkPvCollision); addPv(chkPvSockets); addPv(chkPvPivots);

        int ay = pvRow + pvGap * 4 + 14;
        addRenderableWidget(Button.builder(Component.translatable("gui.necromancy.bodypart_dev.apply"), b -> sendApply())
            .bounds(lx, ay, 118, 22).build());
        addRenderableWidget(Button.builder(Component.translatable("gui.necromancy.bodypart_dev.save_disk"), b -> sendSave())
            .bounds(lx + 126, ay, 132, 22).build());

        pushDraftToWidgets();
        applyTabVisibility();
    }

    private void addGeom(AbstractWidget w) {
        tabGeometry.add(w);
        addRenderableWidget(w);
    }

    private void addFlag(AbstractWidget w) {
        tabFlags.add(w);
        addRenderableWidget(w);
    }

    private void addSock(AbstractWidget w) {
        tabSockets.add(w);
        addRenderableWidget(w);
    }

    private void addPv(AbstractWidget w) {
        tabPreview.add(w);
        addRenderableWidget(w);
    }

    private EditBox eb(int x, int y, int w, String name) {
        return new EditBox(font, x, y, w, 14, Component.literal(name));
    }

    private void selectTab(Tab t) {
        activeTab = t;
        applyTabVisibility();
    }

    private void applyTabVisibility() {
        setVis(tabGeometry, activeTab == Tab.GEOMETRY);
        setVis(tabFlags, activeTab == Tab.FLAGS);
        setVis(tabSockets, activeTab == Tab.SOCKETS);
        setVis(tabPreview, activeTab == Tab.PREVIEW);
    }

    private static void setVis(List<? extends AbstractWidget> list, boolean on) {
        for (AbstractWidget w : list) {
            w.visible = on;
            w.active = on;
        }
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        widgetsToDraft();
        BodypartDevLiveDraft.update(menu.getDevBlockPos().asLong(), BodypartDefinitionIo.toJson(draft), previewMaskFromUi());
    }

    @Override
    public void removed() {
        BodypartDevLiveDraft.clear();
        super.removed();
    }

    private void reloadDraftFromBlockEntity() {
        try {
            draft = BodypartDefinitionIo.fromJson(menu.getDev().getDraftJson());
            if (draft.attachments == null) draft.attachments = new ArrayList<>();
            if (draft.attachments.isEmpty()) draft.attachments.add(new BodypartAttachmentJson());
            if (draft.flags == null) draft.flags = new com.sirolf2009.necromancy.bodypart.BodypartFlagsJson();
            if (draft.hitbox == null) draft.hitbox = new com.sirolf2009.necromancy.bodypart.BodypartHitboxJson();
            if (draft.visualOffset == null) draft.visualOffset = new BodypartVisualOffsetJson();
            attachmentIndex = 0;
        } catch (Exception e) {
            draft = new BodypartDefinitionJson();
            draft.attachments = new ArrayList<>();
            draft.attachments.add(new BodypartAttachmentJson());
            draft.flags = new com.sirolf2009.necromancy.bodypart.BodypartFlagsJson();
            draft.hitbox = new com.sirolf2009.necromancy.bodypart.BodypartHitboxJson();
            draft.visualOffset = new BodypartVisualOffsetJson();
        }
    }

    private void bumpAtt(int d) {
        widgetsToDraft();
        if (draft.attachments.isEmpty()) return;
        attachmentIndex = (attachmentIndex + d + draft.attachments.size()) % draft.attachments.size();
        pullAttachmentToWidgets();
    }

    private void addAtt() {
        widgetsToDraft();
        draft.attachments.add(new BodypartAttachmentJson());
        attachmentIndex = draft.attachments.size() - 1;
        pullAttachmentToWidgets();
    }

    private void removeAtt() {
        widgetsToDraft();
        if (draft.attachments.size() <= 1) return;
        draft.attachments.remove(attachmentIndex);
        attachmentIndex = Math.min(attachmentIndex, draft.attachments.size() - 1);
        pullAttachmentToWidgets();
    }

    private int previewMaskFromUi() {
        int m = 0;
        if (chkPvSockets != null && chkPvSockets.selected()) m |= BodypartPreviewMask.SOCKET_MARKERS;
        if (chkPvPivots != null && chkPvPivots.selected()) m |= BodypartPreviewMask.PIVOT_MARKERS;
        if (chkPvMesh != null && chkPvMesh.selected()) m |= BodypartPreviewMask.MESH;
        if (chkPvCollision != null && chkPvCollision.selected()) m |= BodypartPreviewMask.COLLISION_OUTLINE;
        return m;
    }

    private void pushDraftToWidgets() {
        if (draft.hitbox == null) draft.hitbox = new com.sirolf2009.necromancy.bodypart.BodypartHitboxJson();
        if (draft.visualOffset == null) draft.visualOffset = new BodypartVisualOffsetJson();
        ox.setValue(fmt(draft.hitbox.ox));
        oy.setValue(fmt(draft.hitbox.oy));
        oz.setValue(fmt(draft.hitbox.oz));
        sx.setValue(fmt(draft.hitbox.sx));
        sy.setValue(fmt(draft.hitbox.sy));
        sz.setValue(fmt(draft.hitbox.sz));
        vx.setValue(fmt(draft.visualOffset.dx));
        vy.setValue(fmt(draft.visualOffset.dy));
        vz.setValue(fmt(draft.visualOffset.dz));
        pullAttachmentToWidgets();
    }

    private void pullAttachmentToWidgets() {
        BodypartAttachmentJson at = draft.attachments.get(Math.max(0, attachmentIndex));
        attName.setValue(at.name == null ? "" : at.name);
        attOx.setValue(fmt(at.ox));
        attOy.setValue(fmt(at.oy));
        attOz.setValue(fmt(at.oz));
        attPri.setValue(Integer.toString(at.priority));
        pivotOx.setValue(fmt(at.pivotOx));
        pivotOy.setValue(fmt(at.pivotOy));
        pivotOz.setValue(fmt(at.pivotOz));
        eulerYaw.setValue(at.eulerYawDeg != null ? fmt(at.eulerYawDeg) : "");
        eulerPitch.setValue(at.eulerPitchDeg != null ? fmt(at.eulerPitchDeg) : "");
        eulerRoll.setValue(at.eulerRollDeg != null ? fmt(at.eulerRollDeg) : "");
        quatX.setValue(at.quatX != null ? fmt(at.quatX) : "");
        quatY.setValue(at.quatY != null ? fmt(at.quatY) : "");
        quatZ.setValue(at.quatZ != null ? fmt(at.quatZ) : "");
        quatW.setValue(at.quatW != null ? fmt(at.quatW) : "");
    }

    private static String fmt(double v) {
        return Double.toString(Math.round(v * 1000.0) / 1000.0);
    }

    private void widgetsToDraft() {
        draft.hitbox.ox = parseD(ox);
        draft.hitbox.oy = parseD(oy);
        draft.hitbox.oz = parseD(oz);
        draft.hitbox.sx = Math.max(1e-4, parseD(sx));
        draft.hitbox.sy = Math.max(1e-4, parseD(sy));
        draft.hitbox.sz = Math.max(1e-4, parseD(sz));
        draft.visualOffset.dx = parseD(vx);
        draft.visualOffset.dy = parseD(vy);
        draft.visualOffset.dz = parseD(vz);

        if (draft.flags == null) draft.flags = new com.sirolf2009.necromancy.bodypart.BodypartFlagsJson();
        if (chkFlagHead != null) {
            draft.flags.head = chkFlagHead.selected();
            draft.flags.torso = chkFlagTorso.selected();
            draft.flags.arm = chkFlagArm.selected();
            draft.flags.leg = chkFlagLeg.selected();
            draft.flags.special = chkFlagSpecial.selected();
        }

        BodypartAttachmentJson at = draft.attachments.get(Math.max(0, attachmentIndex));
        at.name = attName.getValue();
        at.ox = parseD(attOx);
        at.oy = parseD(attOy);
        at.oz = parseD(attOz);
        try {
            at.priority = Integer.parseInt(attPri.getValue().trim());
        } catch (NumberFormatException e) {
            at.priority = 0;
        }
        at.eulerYawDeg = parseNullableD(eulerYaw);
        at.eulerPitchDeg = parseNullableD(eulerPitch);
        at.eulerRollDeg = parseNullableD(eulerRoll);
        Double qx = parseNullableD(quatX);
        Double qy = parseNullableD(quatY);
        Double qz = parseNullableD(quatZ);
        Double qw = parseNullableD(quatW);
        if (qx != null && qy != null && qz != null && qw != null) {
            at.quatX = qx;
            at.quatY = qy;
            at.quatZ = qz;
            at.quatW = qw;
        } else {
            at.quatX = at.quatY = at.quatZ = at.quatW = null;
        }

        boolean limb = draft.flags.arm || draft.flags.leg;
        if (limb) {
            at.pivotOx = parseD(pivotOx);
            at.pivotOy = parseD(pivotOy);
            at.pivotOz = parseD(pivotOz);
            at.hasRotationPivot = Math.abs(at.pivotOx) > 1e-6 || Math.abs(at.pivotOy) > 1e-6 || Math.abs(at.pivotOz) > 1e-6;
        } else {
            at.hasRotationPivot = false;
            at.pivotOx = at.pivotOy = at.pivotOz = 0;
        }
    }

    private static Double parseNullableD(EditBox box) {
        String s = box.getValue().trim().replace(',', '.');
        if (s.isEmpty()) return null;
        try {
            return Double.parseDouble(s);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static double parseD(EditBox box) {
        try {
            return Double.parseDouble(box.getValue().trim().replace(',', '.'));
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private void sendApply() {
        widgetsToDraft();
        BlockPos pos = menu.getDevBlockPos();
        String json = BodypartDefinitionIo.toJson(draft);
        byte[] utf8 = json.getBytes(StandardCharsets.UTF_8);
        int pm = previewMaskFromUi();
        BodypartDevLiveDraft.update(pos.asLong(), json, pm);
        PacketDistributor.sendToServer(new BodypartDevApplyPayload(pos, utf8, pm));
    }

    private void sendSave() {
        widgetsToDraft();
        BlockPos pos = menu.getDevBlockPos();
        String json = BodypartDefinitionIo.toJson(draft);
        int pm = previewMaskFromUi();
        BodypartDevLiveDraft.update(pos.asLong(), json, pm);
        PacketDistributor.sendToServer(new BodypartDevApplyPayload(pos,
            json.getBytes(StandardCharsets.UTF_8), pm));
        PacketDistributor.sendToServer(new BodypartDevSavePayload(pos));
    }

    @Override
    public void render(GuiGraphics g, int mx, int my, float partialTick) {
        renderBackground(g, mx, my, partialTick);
        super.render(g, mx, my, partialTick);
        renderTooltip(g, mx, my);

        int lx = leftPos + BodypartDevLayout.EDITOR_LEFT;
        int hint = 0xFFC8CCD8;
        if (activeTab == Tab.GEOMETRY) {
            g.drawString(font, Component.translatable("gui.necromancy.bodypart_dev.hitbox_section"),
                lx, topPos + BodypartDevLayout.CONTENT_TOP - 12, 0xFFE8ECFF, true);
            g.drawString(font, Component.translatable("gui.necromancy.bodypart_dev.visual_offset_section"),
                lx, topPos + BodypartDevLayout.CONTENT_TOP + 26, 0xFFE8ECFF, true);
            g.drawString(font, Component.literal(draft.id != null ? draft.id : ""),
                lx, topPos + BodypartDevLayout.CONTENT_TOP + 52, 0xFFAAB0C0, true);
            Boolean v = draft.validated;
            g.drawString(font, Component.translatable(v != null && v
                ? "gui.necromancy.bodypart_dev.validated_yes"
                : "gui.necromancy.bodypart_dev.validated_no"),
                lx, topPos + BodypartDevLayout.CONTENT_TOP + 64, hint, true);
        } else if (activeTab == Tab.FLAGS) {
            g.drawString(font, Component.translatable("gui.necromancy.bodypart_dev.flags_help"),
                lx, topPos + BodypartDevLayout.CONTENT_TOP - 12, hint, true);
        } else if (activeTab == Tab.SOCKETS) {
            g.drawString(font, Component.translatable("gui.necromancy.bodypart_dev.socket_section"),
                lx, topPos + BodypartDevLayout.CONTENT_TOP - 12, 0xFFE8ECFF, true);
            g.drawString(font, Component.translatable("gui.necromancy.bodypart_dev.euler_section"),
                lx, topPos + BodypartDevLayout.CONTENT_TOP + 26, hint, true);
            g.drawString(font, Component.translatable("gui.necromancy.bodypart_dev.quat_section"),
                lx, topPos + BodypartDevLayout.CONTENT_TOP + 70, hint, true);
            g.drawString(font, Component.translatable("gui.necromancy.bodypart_dev.pivot_section"),
                lx, topPos + BodypartDevLayout.CONTENT_TOP + 114, hint, true);
            int idx = draft.attachments.isEmpty() ? 0 : attachmentIndex + 1;
            g.drawString(font, idx + "/" + draft.attachments.size(),
                leftPos + imageWidth - 56, topPos + BodypartDevLayout.CONTENT_TOP + 132, 0xFFAAB0C0, true);
        } else if (activeTab == Tab.PREVIEW) {
            int pvBase = topPos + BodypartDevLayout.CONTENT_TOP;
            g.drawString(font, Component.translatable("gui.necromancy.bodypart_dev.preview_help"),
                lx, pvBase + 26 * 4 + 8, hint, true);
        }
    }

    @Override
    protected void renderBg(GuiGraphics g, float partialTick, int mx, int my) {
        int x = leftPos;
        int y = topPos;
        g.fill(x, y, x + imageWidth, y + imageHeight, 0xFF101018);
        int editorBottom = y + BodypartDevLayout.PLAYER_INV_FIRST_ROW_Y - 18;
        g.fill(x + 4, y + 18, x + imageWidth - 4, editorBottom, 0xFF161622);
        g.fill(x + 4, editorBottom - 2, x + imageWidth - 4, editorBottom + 2, 0xFF383855);
        int invBandTop = y + BodypartDevLayout.PLAYER_INV_FIRST_ROW_Y - 10;
        g.fill(x + 4, invBandTop, x + imageWidth - 4, invBandTop + 4, 0xFF282838);
    }
}
