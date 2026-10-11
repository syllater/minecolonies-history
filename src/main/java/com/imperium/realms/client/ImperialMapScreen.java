package com.imperium.realms.client;

import com.imperium.realms.network.StrategicMapDataPayload;
import com.imperium.realms.network.StrategicMapRequestPayload;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.neoforged.neoforge.network.PacketDistributor;
import org.lwjgl.glfw.GLFW;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Interactive map for display only: server-provided realm data, local pan/zoom,
 * clickable province markers and an information panel.
 */
public final class ImperialMapScreen extends Screen {
    private static final int GRID_COLUMNS = 29;
    private static final int GRID_ROWS = 17;
    private static final int CELL_SIZE = 18;
    private static final int MAP_LEFT = 24;
    private static final int MAP_TOP = 74;
    private static final String MARKER_POOL = "0123456789ABCDEFGIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz@!";
    private static final int[] ZOOM_LEVELS = {8, 16, 32, 64, 128, 256, 512, 1_024, 2_048};
    private static final int MIN_COORDINATE = -30_000_000;
    private static final int MAX_COORDINATE = 30_000_000;

    private StrategicMapDataPayload snapshot;
    private StrategicMapDataPayload.ProvinceView selectedProvince;
    private int centerX;
    private int centerZ;
    private int zoomIndex = 4;

    public ImperialMapScreen() {
        super(Component.translatable("imperium_realms.gui.map.title"));
    }

    @Override
    protected void init() {
        super.init();
        final int buttonY = this.height - 28;
        int buttonX = MAP_LEFT;
        addRenderableWidget(Button.builder(
                Component.translatable("imperium_realms.gui.map.zoom_out"),
                button -> zoomBy(1, this.width / 2.0, this.height / 2.0))
                .bounds(buttonX, buttonY, 82, 20).build());
        buttonX += 86;
        addRenderableWidget(Button.builder(
                Component.translatable("imperium_realms.gui.map.zoom_in"),
                button -> zoomBy(-1, this.width / 2.0, this.height / 2.0))
                .bounds(buttonX, buttonY, 82, 20).build());
        buttonX += 86;
        addRenderableWidget(Button.builder(
                Component.translatable("imperium_realms.gui.map.recenter"),
                button -> recenter())
                .bounds(buttonX, buttonY, 94, 20).build());
        buttonX += 98;
        addRenderableWidget(Button.builder(
                Component.translatable("imperium_realms.gui.map.refresh"),
                button -> requestSnapshot())
                .bounds(buttonX, buttonY, 82, 20).build());
        addRenderableWidget(Button.builder(
                Component.translatable("imperium_realms.gui.map.close"),
                button -> onClose())
                .bounds(this.width - 98, buttonY, 78, 20).build());
    }

    public void acceptServerSnapshot(final StrategicMapDataPayload payload) {
        Objects.requireNonNull(payload, "payload");
        final boolean firstSnapshot = snapshot == null;
        snapshot = payload;
        if (firstSnapshot) {
            centerX = payload.centerX();
            centerZ = payload.centerZ();
        }
        if (selectedProvince != null) {
            selectedProvince = payload.provinces().stream()
                    .filter(province -> province.colonyId() == selectedProvince.colonyId()
                            && province.dimensionId().equals(selectedProvince.dimensionId()))
                    .findFirst().orElse(null);
        }
    }

    private void requestSnapshot() {
        if (this.minecraft != null && this.minecraft.player != null) {
            PacketDistributor.sendToServer(new StrategicMapRequestPayload());
        }
    }

    private void recenter() {
        if (snapshot != null && snapshot.available()) {
            centerX = snapshot.centerX();
            centerZ = snapshot.centerZ();
            zoomIndex = 4;
        }
    }

    private int blocksPerCell() {
        return ZOOM_LEVELS[zoomIndex];
    }

    private void zoomBy(final int direction, final double anchorX, final double anchorY) {
        final int oldScale = blocksPerCell();
        final int nextIndex = Math.max(0, Math.min(ZOOM_LEVELS.length - 1, zoomIndex + direction));
        if (nextIndex == zoomIndex) {
            return;
        }

        final boolean overMap = anchorX >= MAP_LEFT
                && anchorX < MAP_LEFT + GRID_COLUMNS * CELL_SIZE
                && anchorY >= MAP_TOP
                && anchorY < MAP_TOP + GRID_ROWS * CELL_SIZE;
        if (overMap) {
            final int column = Math.max(0, Math.min(GRID_COLUMNS - 1,
                    (int) ((anchorX - MAP_LEFT) / CELL_SIZE)));
            final int row = Math.max(0, Math.min(GRID_ROWS - 1,
                    (int) ((anchorY - MAP_TOP) / CELL_SIZE)));
            final long anchorWorldX = (long) centerX + (long) (column - GRID_COLUMNS / 2) * oldScale;
            final long anchorWorldZ = (long) centerZ + (long) (row - GRID_ROWS / 2) * oldScale;
            final int nextScale = ZOOM_LEVELS[nextIndex];
            centerX = clampCoordinate(anchorWorldX - (long) (column - GRID_COLUMNS / 2) * nextScale);
            centerZ = clampCoordinate(anchorWorldZ - (long) (row - GRID_ROWS / 2) * nextScale);
        }
        zoomIndex = nextIndex;
    }

    private static int clampCoordinate(final long coordinate) {
        return (int) Math.max(MIN_COORDINATE, Math.min(MAX_COORDINATE, coordinate));
    }

    private GridCell project(final StrategicMapDataPayload.ProvinceView province) {
        if (snapshot == null || !province.hasCoordinates()
                || !snapshot.dimensionId().equals(province.dimensionId())) {
            return null;
        }
        final long column = GRID_COLUMNS / 2L
                + Math.floorDiv((long) province.x() - centerX, (long) blocksPerCell());
        final long row = GRID_ROWS / 2L
                + Math.floorDiv((long) province.z() - centerZ, (long) blocksPerCell());
        if (column < 0 || column >= GRID_COLUMNS || row < 0 || row >= GRID_ROWS) {
            return null;
        }
        return new GridCell((int) column, (int) row);
    }

    @Override
    public void render(
            final GuiGraphics graphics,
            final int mouseX,
            final int mouseY,
            final float partialTick) {
        renderBackground(graphics, mouseX, mouseY, partialTick);
        graphics.drawCenteredString(this.font, this.title, this.width / 2, 14, 0xFFF2E4BD);
        graphics.drawString(this.font, Component.translatable(
                "imperium_realms.gui.map.navigation"), MAP_LEFT, 42, 0xFFE1E7EC, false);

        if (snapshot == null) {
            graphics.drawString(this.font, Component.translatable(
                    "imperium_realms.gui.map.loading"), MAP_LEFT, MAP_TOP, 0xFFFFFFFF, false);
            super.render(graphics, mouseX, mouseY, partialTick);
            return;
        }
        if (!snapshot.available()) {
            graphics.drawString(this.font, Component.translatable(snapshot.messageKey()),
                    MAP_LEFT, MAP_TOP, 0xFFFFA0A0, false);
            super.render(graphics, mouseX, mouseY, partialTick);
            return;
        }

        final int mapRight = MAP_LEFT + GRID_COLUMNS * CELL_SIZE;
        final int mapBottom = MAP_TOP + GRID_ROWS * CELL_SIZE;
        graphics.fill(MAP_LEFT - 2, MAP_TOP - 2, mapRight + 2, mapBottom + 2, 0xC010171E);
        for (int column = 0; column <= GRID_COLUMNS; column++) {
            final int x = MAP_LEFT + column * CELL_SIZE;
            graphics.fill(x, MAP_TOP, x + 1, mapBottom, 0x553E5A68);
        }
        for (int row = 0; row <= GRID_ROWS; row++) {
            final int y = MAP_TOP + row * CELL_SIZE;
            graphics.fill(MAP_LEFT, y, mapRight, y + 1, 0x553E5A68);
        }
        final int midX = MAP_LEFT + (GRID_COLUMNS / 2) * CELL_SIZE;
        final int midY = MAP_TOP + (GRID_ROWS / 2) * CELL_SIZE;
        graphics.fill(midX, MAP_TOP, midX + 2, mapBottom, 0xAAE0BD6B);
        graphics.fill(MAP_LEFT, midY, mapRight, midY + 2, 0xAAE0BD6B);

        final Map<Integer, StrategicMapDataPayload.ProvinceView> occupied = new HashMap<>();
        final Set<Integer> collisions = new HashSet<>();
        for (final StrategicMapDataPayload.ProvinceView province : snapshot.provinces()) {
            final GridCell cell = project(province);
            if (cell == null) {
                continue;
            }
            final int key = cell.row() * GRID_COLUMNS + cell.column();
            if (occupied.putIfAbsent(key, province) != null) {
                collisions.add(key);
            }
        }
        for (final Map.Entry<Integer, StrategicMapDataPayload.ProvinceView> entry : occupied.entrySet()) {
            final int key = entry.getKey();
            final int column = key % GRID_COLUMNS;
            final int row = key / GRID_COLUMNS;
            final int x = MAP_LEFT + column * CELL_SIZE;
            final int y = MAP_TOP + row * CELL_SIZE;
            final StrategicMapDataPayload.ProvinceView province = entry.getValue();
            final int fill = collisions.contains(key) ? 0xFFFFAA55 : markerColor(province);
            graphics.fill(x + 2, y + 2, x + CELL_SIZE - 2, y + CELL_SIZE - 2, fill);
            final String marker = collisions.contains(key) ? "*" : Character.toString(markerFor(province));
            graphics.drawCenteredString(this.font, marker, x + CELL_SIZE / 2, y + 4, 0xFF10202A);
            if (isSelected(province)) {
                graphics.fill(x, y, x + CELL_SIZE, y + 2, 0xFFFFFFFF);
                graphics.fill(x, y + CELL_SIZE - 2, x + CELL_SIZE, y + CELL_SIZE, 0xFFFFFFFF);
                graphics.fill(x, y, x + 2, y + CELL_SIZE, 0xFFFFFFFF);
                graphics.fill(x + CELL_SIZE - 2, y, x + CELL_SIZE, y + CELL_SIZE, 0xFFFFFFFF);
            }
        }

        renderDetails(graphics, mapRight + 18);
        graphics.drawString(this.font, Component.translatable(
                "imperium_realms.gui.map.center", centerX, centerZ, blocksPerCell()),
                MAP_LEFT, mapBottom + 8, 0xFFF2E4BD, false);
        super.render(graphics, mouseX, mouseY, partialTick);
    }

    private void renderDetails(final GuiGraphics graphics, final int left) {
        final int rightWidth = Math.max(160, this.width - left - 18);
        int y = MAP_TOP;
        y = drawWrapped(graphics, Component.translatable(
                "imperium_realms.gui.map.realm", snapshot.realmName()), left, y, rightWidth, 0xFFF2E4BD);
        y = drawWrapped(graphics, Component.translatable(
                "imperium_realms.gui.map.dimension", snapshot.dimensionId()), left, y, rightWidth, 0xFFDAE2E8);

        final long plotted = snapshot.provinces().stream()
                .filter(province -> province.dimensionId().equals(snapshot.dimensionId())
                        && province.hasCoordinates()).count();
        final long otherDimensions = snapshot.provinces().stream()
                .filter(province -> !province.dimensionId().equals(snapshot.dimensionId())).count();
        final long unresolved = snapshot.provinces().stream()
                .filter(province -> !province.hasCoordinates()).count();
        y += 2;
        y = drawWrapped(graphics, Component.translatable(
                "imperium_realms.gui.map.provinces", snapshot.provinces().size(), plotted),
                left, y, rightWidth, 0xFFDAE2E8);
        y = drawWrapped(graphics, Component.translatable(
                "imperium_realms.gui.map.unloaded_count", unresolved),
                left, y, rightWidth, 0xFFB9C4CC);
        y = drawWrapped(graphics, Component.translatable(
                "imperium_realms.gui.map.other_dimension_count", otherDimensions),
                left, y, rightWidth, 0xFFB9C4CC);
        y += 4;
        graphics.fill(left, y, left + rightWidth, y + 1, 0xFF556570);
        y += 6;

        if (selectedProvince == null) {
            drawWrapped(graphics, Component.translatable(
                    "imperium_realms.gui.map.select_marker"), left, y, rightWidth, 0xFFE1E7EC);
            return;
        }

        y = drawWrapped(graphics, Component.translatable(
                "imperium_realms.gui.map.selected_province",
                selectedProvince.name(), selectedProvince.colonyId()),
                left, y, rightWidth, 0xFFFFFFFF);
        y += 2;
        if (selectedProvince.hasCoordinates()) {
            y = drawWrapped(graphics, Component.translatable(
                    "imperium_realms.gui.map.selected_position",
                    selectedProvince.x(), selectedProvince.z()),
                    left, y, rightWidth, 0xFFDAE2E8);
        } else {
            y = drawWrapped(graphics, Component.translatable(
                    "imperium_realms.gui.map.no_coordinates"),
                    left, y, rightWidth, 0xFFDAE2E8);
        }
        y = drawWrapped(graphics, Component.translatable(
                "imperium_realms.gui.map.selected_metrics",
                selectedProvince.stability(), selectedProvince.unrest(), selectedProvince.loyalty()),
                left, y, rightWidth, 0xFFDAE2E8);
        if (selectedProvince.capital()) {
            drawWrapped(graphics, Component.translatable(
                    "imperium_realms.gui.map.capital_marker"), left, y + 2, rightWidth, 0xFFFFD166);
        }
    }

    private int drawWrapped(
            final GuiGraphics graphics,
            final Component component,
            final int x,
            final int y,
            final int maxWidth,
            final int color) {
        int nextY = y;
        final List<FormattedCharSequence> lines = this.font.split(component, Math.max(80, maxWidth));
        for (final FormattedCharSequence line : lines) {
            graphics.drawString(this.font, line, x, nextY, color, false);
            nextY += 10;
        }
        return nextY + 2;
    }

    private int markerColor(final StrategicMapDataPayload.ProvinceView province) {
        if (isSelected(province)) return 0xFF6BE3B0;
        if (province.capital()) return 0xFFFFD166;
        if (province.unrest() >= 60 || province.stability() < 30) return 0xFFE87B73;
        return 0xFF7EC8F3;
    }

    private char markerFor(final StrategicMapDataPayload.ProvinceView province) {
        if (province.capital()) {
            return 'H';
        }
        int index = 0;
        for (final StrategicMapDataPayload.ProvinceView item : snapshot.provinces()) {
            if (item.capital()) {
                continue;
            }
            if (item == province || item.equals(province)) {
                return MARKER_POOL.charAt(index % MARKER_POOL.length());
            }
            index++;
        }
        return '?';
    }

    private boolean isSelected(final StrategicMapDataPayload.ProvinceView province) {
        return selectedProvince != null
                && selectedProvince.colonyId() == province.colonyId()
                && selectedProvince.dimensionId().equals(province.dimensionId());
    }

    @Override
    public boolean mouseClicked(final double mouseX, final double mouseY, final int button) {
        if (button == 0 && snapshot != null && snapshot.available()
                && mouseX >= MAP_LEFT && mouseX < MAP_LEFT + GRID_COLUMNS * CELL_SIZE
                && mouseY >= MAP_TOP && mouseY < MAP_TOP + GRID_ROWS * CELL_SIZE) {
            final int column = (int) ((mouseX - MAP_LEFT) / CELL_SIZE);
            final int row = (int) ((mouseY - MAP_TOP) / CELL_SIZE);
            selectedProvince = snapshot.provinces().stream()
                    .filter(province -> {
                        final GridCell cell = project(province);
                        return cell != null && cell.column() == column && cell.row() == row;
                    })
                    .findFirst().orElse(selectedProvince);
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(
            final double mouseX,
            final double mouseY,
            final double scrollX,
            final double scrollY) {
        if (scrollY != 0.0D) {
            zoomBy(scrollY > 0.0D ? -1 : 1, mouseX, mouseY);
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    public boolean keyPressed(final int keyCode, final int scanCode, final int modifiers) {
        final int panStep = blocksPerCell() * 4;
        switch (keyCode) {
            case GLFW.GLFW_KEY_LEFT -> centerX = clampCoordinate((long) centerX - panStep);
            case GLFW.GLFW_KEY_RIGHT -> centerX = clampCoordinate((long) centerX + panStep);
            case GLFW.GLFW_KEY_UP -> centerZ = clampCoordinate((long) centerZ - panStep);
            case GLFW.GLFW_KEY_DOWN -> centerZ = clampCoordinate((long) centerZ + panStep);
            case GLFW.GLFW_KEY_MINUS, GLFW.GLFW_KEY_KP_SUBTRACT ->
                    zoomBy(1, this.width / 2.0, this.height / 2.0);
            case GLFW.GLFW_KEY_EQUAL, GLFW.GLFW_KEY_KP_ADD ->
                    zoomBy(-1, this.width / 2.0, this.height / 2.0);
            case GLFW.GLFW_KEY_R -> recenter();
            default -> {
                return super.keyPressed(keyCode, scanCode, modifiers);
            }
        }
        return true;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private record GridCell(int column, int row) {
    }
}
