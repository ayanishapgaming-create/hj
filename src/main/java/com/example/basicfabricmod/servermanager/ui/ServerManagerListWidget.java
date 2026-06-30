package com.example.basicfabricmod.servermanager.ui;

import com.example.basicfabricmod.BasicFabricMod;
import com.example.basicfabricmod.config.ModConfig;
import com.example.basicfabricmod.geo.CountryLookupResult;
import com.example.basicfabricmod.geo.LookupStatus;
import com.example.basicfabricmod.geo.ServerCountryService;
import com.example.basicfabricmod.servermanager.model.FolderViewModel;
import com.example.basicfabricmod.servermanager.model.ServerEntryViewModel;
import com.example.basicfabricmod.servermanager.model.ServerManagerViewModel;
import com.example.basicfabricmod.servermanager.render.FolderIconRegistry;
import com.example.basicfabricmod.servermanager.service.PingDisplayCache;
import com.example.basicfabricmod.servermanager.service.ServerFolderManager;
import com.example.basicfabricmod.servermanager.ui.controller.ClipboardController;
import com.example.basicfabricmod.servermanager.ui.controller.ContextMenuController;
import com.example.basicfabricmod.servermanager.ui.controller.DragAndDropController;
import com.example.basicfabricmod.servermanager.ui.controller.FolderDialogController;
import com.example.basicfabricmod.servermanager.ui.controller.HitTestCache;
import com.example.basicfabricmod.servermanager.ui.controller.SelectionController;
import com.example.basicfabricmod.servermanager.ui.controller.UiActionController;
import com.example.basicfabricmod.servermanager.ui.controller.UndoController;
import com.example.basicfabricmod.servermanager.ui.drag.DropTarget;
import com.example.basicfabricmod.servermanager.ui.drag.DropTargetType;
import com.example.basicfabricmod.servermanager.ui.layout.RowLayout;
import com.example.basicfabricmod.servermanager.ui.layout.RowLayoutEngine;
import com.example.basicfabricmod.servermanager.ui.menu.ContextMenuAction;
import com.example.basicfabricmod.servermanager.ui.menu.ContextMenuModel;
import com.example.basicfabricmod.servermanager.ui.render.FolderAnimationController;
import com.example.basicfabricmod.servermanager.ui.render.HighlightTextRenderer;
import com.example.basicfabricmod.servermanager.ui.render.HoverAnimationController;
import com.example.basicfabricmod.servermanager.ui.row.ManagedRowData;
import com.example.basicfabricmod.servermanager.ui.row.ManagedRowKind;
import com.example.basicfabricmod.servermanager.ui.state.MultiplayerUiState;
import com.example.basicfabricmod.servermanager.ui.state.TooltipRegion;
import com.example.basicfabricmod.servermanager.ui.state.TooltipRegistry;
import com.example.basicfabricmod.servermanager.ui.state.VisibleRowCache;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.gui.widget.AlwaysSelectedEntryListWidget;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;

/**
 * Custom list widget that renders root, folder, and server rows in one virtualized list.
 */
public final class ServerManagerListWidget extends AlwaysSelectedEntryListWidget<ServerManagerListWidget.Entry> {
    private final MultiplayerScreen screen;
    private final MultiplayerUiState uiState;
    private final RowLayoutEngine layoutEngine = new RowLayoutEngine();
    private final FolderAnimationController animationController = new FolderAnimationController();
    private final HoverAnimationController hoverAnimations = new HoverAnimationController();
    private final SelectionController selectionController = new SelectionController();
    private final DragAndDropController dragAndDropController = new DragAndDropController();
    private final ContextMenuController contextMenuController = new ContextMenuController();
    private final ClipboardController clipboardController = new ClipboardController();
    private final UndoController undoController = new UndoController(20);
    private final FolderDialogController folderDialogController = new FolderDialogController();
    private final UiActionController uiActionController = new UiActionController();
    private final TooltipRegistry tooltipRegistry = new TooltipRegistry();
    private final VisibleRowCache visibleRowCache = new VisibleRowCache();
    private final HitTestCache hitTestCache = new HitTestCache();
    private final List<ServerInfo> currentServers = new ArrayList<>();
    private java.util.function.Consumer<String> onServerSelected;

    public void setOnServerSelected(java.util.function.Consumer<String> listener) {
        this.onServerSelected = listener;
    }

    public ServerManagerListWidget(MinecraftClient client, MultiplayerScreen screen, int width, int height, int y, int itemHeight, MultiplayerUiState uiState) {
        super(client, width, height, y, itemHeight);
        this.screen = screen;
        this.uiState = uiState;
        this.centerListVertically = false;
    }

    public void rebuild(List<ServerInfo> servers) {
        try {
            currentServers.clear();
            currentServers.addAll(servers);
            ServerManagerViewModel model = ServerFolderManager.getInstance().buildViewModel(servers, uiState.getSearchQuery());
            List<ManagedRowData> rows = new ArrayList<>();
            rows.add(ManagedRowData.root("Servers", model.rootServers().size()));
            for (ServerEntryViewModel rootServer : model.rootServers()) {
                rows.add(ManagedRowData.server(rootServer));
            }
            for (FolderViewModel folder : model.folders()) {
                rows.add(ManagedRowData.folder(folder.folder(), folder.visibleServerCount()));
                boolean expanded = model.searching() || !folder.folder().isCollapsed();
                float expansion = animationController.getExpansion(folder.folder().getId(), expanded, BasicFabricMod.getConfig().isExpandAnimations() && MinecraftClient.getInstance().getCurrentFps() > 20);
                if (expanded || expansion > 0.0F) {
                    for (ServerEntryViewModel server : folder.servers()) {
                        rows.add(ManagedRowData.server(server));
                    }
                }
            }
            uiState.replaceRows(rows);
            boolean changed = visibleRowCache.update(rows);
            if (changed) {
                List<Entry> entries = new ArrayList<>(rows.size());
                for (ManagedRowData row : rows) {
                    entries.add(new Entry(row));
                }
                replaceEntries(entries);
            }
            BasicFabricMod.LOGGER.info("[ServerManager+] rebuild: {} input servers -> {} rows ({} root, {} folders)",
                    servers.size(), rows.size(), model.rootServers().size(), model.folders().size());
        } catch (Throwable t) {
            BasicFabricMod.LOGGER.error("[ServerManager+] rebuild() failed with {} input servers", servers.size(), t);
        }
    }

    public boolean performUndo() {
        boolean result = undoController.undo();
        if (result) {
            rebuild(currentServers);
        }
        return result;
    }

    public SelectionController getSelectionController() {
        return selectionController;
    }

    public void renderOverlays(DrawContext context, int mouseX, int mouseY) {
        TooltipRegion tooltip = tooltipRegistry.find(mouseX, mouseY);
        if (tooltip != null) {
            context.drawTooltip(this.client.textRenderer, tooltip.tooltip(), mouseX, mouseY);
        }
        ContextMenuModel menu = contextMenuController.getCurrentMenu();
        if (menu != null) {
            context.fill(menu.x(), menu.y(), menu.x() + menu.width(), menu.y() + menu.actions().size() * 12 + 6, 0xCC101010);
            int y = menu.y() + 4;
            for (ContextMenuAction action : menu.actions()) {
                context.drawTextWithShadow(client.textRenderer, action.label(), menu.x() + 4, y, action.enabled() ? 0xFFFFFF : 0x777777);
                y += 12;
            }
        }
    }

    @Override
    public void renderWidget(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
        tooltipRegistry.clear();
        hitTestCache.clear();
        super.renderWidget(context, mouseX, mouseY, deltaTicks);
    }

    public final class Entry extends AlwaysSelectedEntryListWidget.Entry<Entry> {
        private final ManagedRowData row;

        private Entry(ManagedRowData row) {
            this.row = row;
        }

        @Override
        public Text getNarration() {
            return switch (row.kind()) {
                case ROOT -> Text.literal(row.label() + ", " + row.serverCount() + " servers");
                case FOLDER -> Text.literal((row.folder().isCollapsed() ? "Collapsed folder " : "Folder ") + row.folder().getName() + ", " + row.serverCount() + " servers");
                case SERVER -> Text.literal(row.server().serverInfo().name + (row.server().favorite() ? ", favorite" : ""));
            };
        }

        @Override
        public void render(DrawContext context, int mouseX, int mouseY, boolean hovered, float deltaTicks) {
            try {
                int x = getX();
                int y = getY();
                int width = getWidth();
                int height = getHeight();
                RowLayout layout = layoutEngine.getLayout(x, y, width, height, row.kind() != ManagedRowKind.SERVER,
                        row.server() != null && row.server().favorite(), row.folder() != null && row.folder().isPinned(), client.textRenderer);
                float hoverStrength = hoverAnimations.update(rowKey(), hovered, BasicFabricMod.getConfig().isHoverAnimations());
                renderBackground(context, layout, hovered, hoverStrength);
                switch (row.kind()) {
                    case ROOT -> renderRootRow(context, layout);
                    case FOLDER -> renderFolderRow(context, layout, hovered);
                    case SERVER -> renderServerRow(context, layout, hovered);
                }
                renderDragFeedback(context, layout);
            } catch (Throwable t) {
                BasicFabricMod.LOGGER.error("[ServerManager+] Failed to render row kind={}", row.kind(), t);
                context.fill(getX(), getY(), getX() + getWidth(), getY() + getHeight(), 0x66FF0000);
                context.drawTextWithShadow(client.textRenderer, "render error: " + t.getClass().getSimpleName(), getX() + 4, getY() + 2, 0xFFFFFF);
            }
        }

        private void renderBackground(DrawContext context, RowLayout layout, boolean hovered, float hoverStrength) {
            int color = switch (row.kind()) {
                case ROOT -> 0x33444444;
                case FOLDER -> hovered ? lerpColor(0x334F4F4F, 0x445B5B5B, hoverStrength) : 0x334F4F4F;
                case SERVER -> selectionController.isSelected(row.server() == null ? "" : row.server().serverInfo().address)
                        ? 0x336699CC : hovered ? lerpColor(0x00000000, 0x223A3A3A, hoverStrength) : 0x00000000;
            };
            if (color != 0) {
                context.fill(layout.rowX(), layout.rowY(), layout.rowX() + layout.rowWidth(), layout.rowY() + layout.rowHeight(), color);
            }
        }

        private void renderRootRow(DrawContext context, RowLayout layout) {
            String title = row.label() + (BasicFabricMod.getConfig().isShowFolderCounts() ? " (" + row.serverCount() + ")" : "");
            context.drawTextWithShadow(client.textRenderer, title, layout.titleX(), layout.titleY(), 0xFFFFFF);
        }

        private void renderFolderRow(DrawContext context, RowLayout layout, boolean hovered) {
            ModConfig config = BasicFabricMod.getConfig();
            String arrow = row.folder().isCollapsed() ? "▶" : "▼";
            context.drawTextWithShadow(client.textRenderer, arrow, layout.arrowX(), layout.arrowY(), 0xFFFFFF);
            tooltipRegistry.add(new TooltipRegion(layout.arrowX(), layout.arrowY(), 10, 10, Text.literal(row.folder().isCollapsed() ? "Expand Folder" : "Collapse Folder")));
            if (config.isShowFolderIcons()) {
                Identifier texture = FolderIconRegistry.get(row.folder().getIcon());
                context.drawTextWithShadow(client.textRenderer, "■", layout.iconX(), layout.iconY(), 0xFFAA55);
                tooltipRegistry.add(new TooltipRegion(layout.iconX(), layout.iconY(), 10, 10, Text.literal(row.folder().getIcon().name())));
            }
            String title = row.label() + (config.isShowFolderCounts() ? " (" + row.serverCount() + ")" : "");
            HighlightTextRenderer.drawHighlighted(context, client.textRenderer, title, uiState.getSearchQuery(), layout.titleX(), layout.titleY(), 0xFFFFFF);
            tooltipRegistry.add(new TooltipRegion(layout.titleX(), layout.titleY(), Math.min(client.textRenderer.getWidth(title), 140), 10, Text.literal(row.serverCount() + " servers")));
            if (row.folder().isPinned()) {
                context.drawTextWithShadow(client.textRenderer, "📌", layout.favoriteX(), layout.favoriteY(), 0xFFFF55);
                tooltipRegistry.add(new TooltipRegion(layout.favoriteX(), layout.favoriteY(), 10, 10, Text.literal("Pinned Folder")));
            }
            hitTestCache.put(uiState.getVisibleRows().indexOf(row), new DropTarget(DropTargetType.FOLDER, row.folder().getId(), "", uiState.getVisibleRows().indexOf(row), true));
        }

        private void renderServerRow(DrawContext context, RowLayout layout, boolean hovered) {
            ModConfig config = BasicFabricMod.getConfig();
            ServerEntryViewModel server = row.server();
            CountryLookupResult country = ServerCountryService.getInstance().getOrRequest(server.serverInfo());
            String flag = !config.isShowServerCountryFlags() ? "" : (country != null && country.status() == LookupStatus.RESOLVED ? country.countryInfo().flagEmoji() : "🌍");
            if (!flag.isBlank()) {
                context.drawTextWithShadow(client.textRenderer, flag, layout.flagX(), layout.flagY(), 0xFFFFFF);
                tooltipRegistry.add(new TooltipRegion(layout.flagX(), layout.flagY(), 12, 10, Text.literal(country == null ? "Unknown Location" : country.countryInfo().countryName())));
            }
            context.drawTextWithShadow(client.textRenderer, "■", layout.iconX(), layout.iconY(), 0xFFFFFF);
            HighlightTextRenderer.drawHighlighted(context, client.textRenderer, server.serverInfo().name, uiState.getSearchQuery(), layout.titleX(), layout.titleY(), 0xFFFFFF);
            if (config.isShowFavorites() && server.favorite()) {
                context.drawTextWithShadow(client.textRenderer, "★", layout.favoriteX(), layout.favoriteY(), 0xFFFF55);
                tooltipRegistry.add(new TooltipRegion(layout.favoriteX(), layout.favoriteY(), 10, 10, Text.literal("Favorite Server")));
            }
            String motd = server.serverInfo().label == null ? "" : server.serverInfo().label.getString();
            HighlightTextRenderer.drawHighlighted(context, client.textRenderer, motd, uiState.getSearchQuery(), layout.subtitleX(), layout.subtitleY(), 0xAAAAAA);
            if (server.serverInfo().playerCountLabel != null) {
                context.drawTextWithShadow(client.textRenderer, server.serverInfo().playerCountLabel, layout.playerCountX(), layout.playerCountY(), 0xAAAAAA);
            }
            if (server.serverInfo().version != null) {
                context.drawTextWithShadow(client.textRenderer, server.serverInfo().version, layout.versionX(), layout.versionY(), 0x888888);
            }
            PingDisplayCache.PingDisplayData pingData = PingDisplayCache.getInstance().get(server.serverInfo());
            String stars = switch (pingData.stars()) {
                case 5 -> "★★★★★";
                case 4 -> "★★★★";
                case 3 -> "★★★";
                case 2 -> "★★";
                case 1 -> "★";
                default -> "";
            };
            context.drawTextWithShadow(client.textRenderer, stars, layout.pingBarsX(), layout.pingBarsY(), 0x55FF55);
            if (config.isShowNumericalPing() && pingData.pingMs() >= 0) {
                String pingText = pingData.quality() + " (" + pingData.pingMs() + " ms)";
                context.drawTextWithShadow(client.textRenderer, pingText, layout.pingTextX(), layout.pingTextY(), 0xAAAAAA);
                String ageText = formatAge(pingData.lastUpdatedTime());
                String tooltipText = pingData.quality() + " - " + pingData.pingMs() + " ms" + (ageText.isEmpty() ? "" : " (updated " + ageText + ")");
                tooltipRegistry.add(new TooltipRegion(layout.pingBarsX(), layout.pingBarsY(), 80, 10, Text.literal(tooltipText)));
            }
            hitTestCache.put(uiState.getVisibleRows().indexOf(row), new DropTarget(DropTargetType.BEFORE_SERVER, server.folderId(), server.serverInfo().address, uiState.getVisibleRows().indexOf(row), true));
        }

        private void renderDragFeedback(DrawContext context, RowLayout layout) {
            if (!dragAndDropController.isDragging()) {
                return;
            }
            DropTarget target = dragAndDropController.getTarget();
            if (target.rowIndex() == uiState.getVisibleRows().indexOf(row)) {
                int lineColor = target.valid() ? 0xFF77CC77 : 0xFFCC5555;
                context.fill(layout.rowX(), layout.rowY() - 1, layout.rowX() + layout.rowWidth(), layout.rowY() + 1, lineColor);
                if (target.type() == DropTargetType.FOLDER) {
                    context.fill(layout.rowX(), layout.rowY(), layout.rowX() + layout.rowWidth(), layout.rowY() + layout.rowHeight(), 0x2244AA44);
                }
            }
            context.fill((int) dragAndDropController.getGhostX(), (int) dragAndDropController.getGhostY(), (int) dragAndDropController.getGhostX() + 80, (int) dragAndDropController.getGhostY() + 12, 0x88222222);
            context.drawTextWithShadow(client.textRenderer, dragAndDropController.getPayload().type().name(), (int) dragAndDropController.getGhostX() + 4, (int) dragAndDropController.getGhostY() + 2, 0xFFFFFF);
        }

        @Override
        public boolean mouseClicked(Click click, boolean doubled) {
            if (click.button() == 1 && BasicFabricMod.getConfig().isContextMenus()) {
                openContextMenu();
                return true;
            }
            if (row.kind() == ManagedRowKind.FOLDER && click.button() == 0) {
                ServerFolderManager manager = ServerFolderManager.getInstance();
                boolean oldCollapsed = row.folder().isCollapsed();
                manager.toggleCollapsed(row.folder().getId());
                undoController.push(() -> {
                    if (row.folder().isCollapsed() != oldCollapsed) {
                        manager.toggleCollapsed(row.folder().getId());
                    }
                });
                rebuild(currentServers);
                return true;
            }
            if (row.kind() == ManagedRowKind.SERVER && click.button() == 0) {
                selectionController.click(row.server().serverInfo().address, getVisibleServerAddresses());
                if (onServerSelected != null) {
                    onServerSelected.accept(row.server().serverInfo().address);
                }
                return true;
            }
            return false;
        }

        @Override
        public boolean mouseDragged(Click click, double offsetX, double offsetY) {
            if (!BasicFabricMod.getConfig().isDragAndDrop()) {
                return false;
            }
            if (row.kind() == ManagedRowKind.SERVER) {
                List<String> selected = selectionController.getSelectedServers();
                if (selected.isEmpty()) {
                    selected = List.of(row.server().serverInfo().address);
                }
                dragAndDropController.startServers(selected);
                dragAndDropController.update(click.x(), click.y(), computeDropTarget());
                autoScrollDuringDrag(click.y());
                return true;
            }
            if (row.kind() == ManagedRowKind.FOLDER) {
                dragAndDropController.startFolder(row.folder().getId());
                dragAndDropController.update(click.x(), click.y(), computeDropTarget());
                autoScrollDuringDrag(click.y());
                return true;
            }
            return false;
        }

        @Override
        public boolean mouseReleased(Click click) {
            if (!dragAndDropController.isDragging()) {
                return false;
            }
            applyDropTarget(dragAndDropController.getTarget());
            dragAndDropController.cancel();
            rebuild(currentServers);
            return true;
        }

        private void openContextMenu() {
            List<ContextMenuAction> actions = new ArrayList<>();
            if (row.kind() == ManagedRowKind.FOLDER) {
                actions.add(new ContextMenuAction("Rename", () -> folderDialogController.renameFolder(row.folder(), row.folder().getName() + " Copy"), true));
                actions.add(new ContextMenuAction("Duplicate", () -> ServerFolderManager.getInstance().duplicateFolder(row.folder().getId()), true));
                actions.add(new ContextMenuAction(row.folder().isPinned() ? "Unpin" : "Pin", () -> ServerFolderManager.getInstance().setPinned(row.folder().getId(), !row.folder().isPinned()), true));
                actions.add(new ContextMenuAction("Empty Folder", () -> ServerFolderManager.getInstance().clearFolder(row.folder().getId()), true));
                actions.add(new ContextMenuAction("Delete", () -> ServerFolderManager.getInstance().deleteFolder(row.folder().getId(), true), true));
                actions.add(new ContextMenuAction(row.folder().isCollapsed() ? "Expand" : "Collapse", () -> ServerFolderManager.getInstance().toggleCollapsed(row.folder().getId()), true));
            } else if (row.kind() == ManagedRowKind.SERVER) {
                actions.add(new ContextMenuAction("Join", () -> screen.connect(row.server().serverInfo()), true));
                actions.add(new ContextMenuAction(row.server().favorite() ? "Remove Favorite" : "Favorite", () -> ServerFolderManager.getInstance().setFavorite(row.server().serverInfo().address, !row.server().favorite()), true));
                actions.add(new ContextMenuAction("Favorite Selected", () -> uiActionController.favoriteServers(selectionController.getSelectedServers(), true), true));
                actions.add(new ContextMenuAction("Move to Root", () -> ServerFolderManager.getInstance().assignServerToFolder(row.server().serverInfo().address, ""), true));
                actions.add(new ContextMenuAction("Move Selected to Root", () -> uiActionController.moveServersToRoot(selectionController.getSelectedServers()), true));
                actions.add(new ContextMenuAction("Copy IP Address", () -> clipboardController.copy(row.server().serverInfo().address), true));
                actions.add(new ContextMenuAction("Copy Server Name", () -> clipboardController.copy(row.server().serverInfo().name), true));
            }
            contextMenuController.open(getX() + 20, getY() + 8, screen.width, screen.height, actions);
        }

        private DropTarget computeDropTarget() {
            int rowIndex = uiState.getVisibleRows().indexOf(row);
            DropTarget cached = hitTestCache.get(rowIndex);
            if (cached != null) {
                return cached;
            }
            if (row.kind() == ManagedRowKind.FOLDER) {
                cached = new DropTarget(DropTargetType.FOLDER, row.folder().getId(), "", rowIndex, true);
            } else if (row.kind() == ManagedRowKind.SERVER) {
                cached = new DropTarget(DropTargetType.BEFORE_SERVER, row.server().folderId(), row.server().serverInfo().address, rowIndex, true);
            } else {
                cached = new DropTarget(DropTargetType.ROOT, "", "", rowIndex, true);
            }
            hitTestCache.put(rowIndex, cached);
            return cached;
        }

        private void applyDropTarget(DropTarget target) {
            if (target == null || !target.valid()) {
                return;
            }
            ServerFolderManager manager = ServerFolderManager.getInstance();
            if (dragAndDropController.getPayload().type().name().equals("SERVERS")) {
                List<String> addresses = dragAndDropController.getPayload().serverAddresses();
                String oldFolder = row.server() != null ? row.server().folderId() : "";
                manager.reorderServers(addresses, target.folderId());
                undoController.push(() -> manager.reorderServers(addresses, oldFolder));
            } else {
                String folderId = dragAndDropController.getPayload().folderId();
                int oldOrder = row.folder() != null ? row.folder().getOrder() : 0;
                manager.reorderFolder(folderId, Math.max(0, target.rowIndex()));
                undoController.push(() -> manager.reorderFolder(folderId, oldOrder));
            }
        }

        private void autoScrollDuringDrag(double mouseY) {
            double margin = 24.0;
            if (mouseY < getY() + margin) {
                setScrollY(Math.max(0.0, getScrollY() - 10.0));
            } else if (mouseY > getBottom() - margin) {
                setScrollY(getScrollY() + 10.0);
            }
        }

        private String formatAge(long lastUpdatedTimeMs) {
            if (lastUpdatedTimeMs <= 0L) {
                return "";
            }
            long deltaMs = System.currentTimeMillis() - lastUpdatedTimeMs;
            if (deltaMs < 0L) {
                deltaMs = 0L;
            }
            long seconds = deltaMs / 1000L;
            if (seconds < 1L) {
                return "just now";
            }
            if (seconds < 60L) {
                return seconds + "s ago";
            }
            long minutes = seconds / 60L;
            return minutes + "m ago";
        }

        private String rowKey() {
            if (row.folder() != null) {
                return "folder:" + row.folder().getId();
            }
            if (row.server() != null) {
                return "server:" + row.server().serverInfo().address;
            }
            return "root";
        }
    }

    private static int lerpColor(int from, int to, float t) {
        int a1 = (from >>> 24) & 0xFF;
        int r1 = (from >>> 16) & 0xFF;
        int g1 = (from >>> 8) & 0xFF;
        int b1 = from & 0xFF;
        int a2 = (to >>> 24) & 0xFF;
        int r2 = (to >>> 16) & 0xFF;
        int g2 = (to >>> 8) & 0xFF;
        int b2 = to & 0xFF;
        int a = (int) (a1 + (a2 - a1) * t);
        int r = (int) (r1 + (r2 - r1) * t);
        int g = (int) (g1 + (g2 - g1) * t);
        int b = (int) (b1 + (b2 - b1) * t);
        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    private List<String> getVisibleServerAddresses() {
        List<String> addresses = new ArrayList<>();
        for (ManagedRowData row : uiState.getVisibleRows()) {
            if (row.server() != null) {
                addresses.add(row.server().serverInfo().address);
            }
        }
        return addresses;
    }
}
