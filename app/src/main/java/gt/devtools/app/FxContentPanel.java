package gt.devtools.app;

import gt.devtools.settings.SettingsManager;
import gt.devtools.tools.api.ToolRegistry;
import gt.devtools.tools.api.fx.ToolFxFactory;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.util.*;

/**
 * JavaFX content area with a TabPane for tool tabs.
 * <p>
 * Supports tab closing, pinning, right-click context menus,
 * and "close similar" / "close all" batch operations.
 * <p>
 * Phase 1: Shows placeholder content when a tool is selected
 * (actual tool UIs are still Swing — ported in Phase 2).
 */
public final class FxContentPanel extends BorderPane {

    private final SettingsManager settingsManager;
    private final TabPane tabPane;
    private final Map<String, ToolEntry> openTools = new LinkedHashMap<>();
    private final Set<String> pinnedToolIds = new LinkedHashSet<>();
    private final List<Runnable> openStateListeners = new ArrayList<>();
    private final StackPane emptyPane;

    public FxContentPanel(SettingsManager settingsManager) {
        this.settingsManager = settingsManager;

        tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.ALL_TABS);
        tabPane.getSelectionModel().selectedItemProperty().addListener((obs, old, tab) -> {
            if (old != null) deactivateTab(old);
            if (tab != null) activateTab(tab);
        });

        // Prevent default tab close — we handle it manually
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.ALL_TABS);

        // Empty state
        emptyPane = new StackPane();
        var label = new Label("Select a tool from the sidebar");
        label.setStyle("-fx-font-size: 16px; -fx-text-fill: -fx-text-background-color;");
        emptyPane.getChildren().add(label);
        StackPane.setAlignment(label, Pos.CENTER);
        setCenter(emptyPane);
    }

    // ---------------------------------------------------------------
    // Open / close
    // ---------------------------------------------------------------

    /** Open or focus a tool (single-click behaviour). */
    public void openTool(ToolFxFactory<?> factory) {
        String toolId = factory.getId();
        ToolEntry existing = openTools.get(toolId);
        if (existing != null) {
            tabPane.getSelectionModel().select(existing.tab);
            return;
        }
        openToolInNewTab(factory);
    }

    /** Always opens a new tab (or nested workbench). */
    public void openToolInNewTab(ToolFxFactory<?> factory) {
        String toolId = factory.getId();
        ToolEntry existing = openTools.get(toolId);

        if (existing != null) {
            // Add nested workbench to existing tab
            existing.workbench.newWorkbench();
            tabPane.getSelectionModel().select(existing.tab);
            fireOpenStateChanged();
            return;
        }

        // Create new top-level tab
        var workbench = new FxWorkbenchTabs(factory, settingsManager);
        String title = factory.getPresentation().contentTitle();

        var tab = new Tab(title);
        tab.setContent(workbench);
        tab.setClosable(true);
        tab.setOnClosed(e -> {
            openTools.remove(toolId);
            pinnedToolIds.remove(toolId);
            workbench.saveConfigs(settingsManager);
            fireOpenStateChanged();
            if (tabPane.getTabs().isEmpty()) {
                setCenter(emptyPane);
            }
        });

        // When the last workbench child tab is closed, close the parent tab too.
        // Safe to call synchronously — we're removing from the parent TabPane,
        // not the child TabPane whose listener triggered this callback.
        workbench.setOnEmpty(() -> closeTool(toolId));

        // Custom tab header (suppress default text since graphic includes it)
        tab.setGraphic(buildTabHeader(factory, tab, toolId, title));
        tab.setText(null);

        // Right-click context menu on tab header
        tab.getGraphic().setOnContextMenuRequested(e ->
                showTabContextMenu(toolId, e.getScreenX(), e.getScreenY()));

        openTools.put(toolId, new ToolEntry(factory, tab, workbench));

        if (tabPane.getTabs().isEmpty()) {
            setCenter(tabPane);
        }

        tabPane.getTabs().add(tab);
        tabPane.getSelectionModel().select(tab);

        workbench.activate();
        fireOpenStateChanged();
    }

    /** Close a specific tool tab. */
    public void closeTool(String toolId) {
        closeToolInternal(toolId, true);
    }

    private void closeToolInternal(String toolId, boolean force) {
        if (!force && pinnedToolIds.contains(toolId)) return;
        ToolEntry entry = openTools.get(toolId);
        if (entry == null) return;
        entry.workbench.saveConfigs(settingsManager);
        tabPane.getTabs().remove(entry.tab);
        // onClosed handler does the rest
    }

    // ---------------------------------------------------------------
    // Batch operations
    // ---------------------------------------------------------------

    public void closeSimilarTools(String toolId) {
        for (String id : new ArrayList<>(openTools.keySet())) {
            if (id.equals(toolId)) continue;
            if (sameToolGroup(toolId, id)) closeToolInternal(id, false);
        }
    }

    public void closeAllTools() {
        for (String id : new ArrayList<>(openTools.keySet())) {
            closeToolInternal(id, false);
        }
    }

    public void togglePin(String toolId) {
        if (pinnedToolIds.contains(toolId)) {
            pinnedToolIds.remove(toolId);
        } else if (openTools.containsKey(toolId)) {
            pinnedToolIds.add(toolId);
        }
        refreshTabHeaders();
    }

    // ---------------------------------------------------------------
    // Tab header
    // ---------------------------------------------------------------

    private HBox buildTabHeader(ToolFxFactory<?> factory, Tab tab,
                                 String toolId, String title) {
        var header = new HBox(4);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(0, 0, 0, 4));

        // Pin button
        var pinBtn = new Button(pinnedToolIds.contains(toolId) ? "📌" : "📍");
        pinBtn.setStyle("-fx-background-color: transparent; -fx-font-size: 10px; -fx-padding: 0 2;");
        pinBtn.setOnAction(e -> {
            togglePin(toolId);
            pinBtn.setText(pinnedToolIds.contains(toolId) ? "📌" : "📍");
        });
        header.getChildren().add(pinBtn);

        // Title
        var titleLabel = new Label(title);
        titleLabel.setPadding(new Insets(0, 2, 0, 4));
        header.getChildren().add(titleLabel);

        return header;
    }

    private void refreshTabHeaders() {
        for (var entry : openTools.entrySet()) {
            var factory = entry.getValue().factory;
            var tab = entry.getValue().tab;
            tab.setGraphic(buildTabHeader(factory, tab, entry.getKey(),
                    factory.getPresentation().contentTitle()));
        }
    }

    // ---------------------------------------------------------------
    // Context menu
    // ---------------------------------------------------------------

    private void showTabContextMenu(String toolId, double x, double y) {
        var menu = new ContextMenu();

        var closeThis = new MenuItem("Close this tab");
        closeThis.setOnAction(e -> closeTool(toolId));
        menu.getItems().add(closeThis);

        var closeSimilar = new MenuItem("Close similar tool tabs");
        closeSimilar.setOnAction(e -> closeSimilarTools(toolId));
        menu.getItems().add(closeSimilar);

        menu.getItems().add(new SeparatorMenuItem());

        var closeAll = new MenuItem("Close all tabs");
        closeAll.setOnAction(e -> closeAllTools());
        menu.getItems().add(closeAll);

        var pinItem = new MenuItem(pinnedToolIds.contains(toolId)
                ? "Unpin tab" : "Pin tab");
        pinItem.setOnAction(e -> togglePin(toolId));
        menu.getItems().add(pinItem);

        menu.show(tabPane, x, y);
    }

    // ---------------------------------------------------------------
    // Activation
    // ---------------------------------------------------------------

    private void activateTab(Tab tab) {
        for (var entry : openTools.values()) {
            if (entry.tab == tab) entry.workbench.activate();
        }
    }

    private void deactivateTab(Tab tab) {
        for (var entry : openTools.values()) {
            if (entry.tab == tab) entry.workbench.deactivate();
        }
    }

    // ---------------------------------------------------------------
    // Queries
    // ---------------------------------------------------------------

    public boolean isToolOpen(String toolId) {
        return openTools.containsKey(toolId);
    }

    public void addOpenStateListener(Runnable listener) {
        openStateListeners.add(listener);
    }

    private void fireOpenStateChanged() {
        openStateListeners.forEach(Runnable::run);
    }

    public void saveAllConfigs(SettingsManager sm) {
        for (var entry : openTools.values()) {
            entry.workbench.saveConfigs(sm);
        }
    }

    private boolean sameToolGroup(String a, String b) {
        if (a.equals(b)) return true;
        var reg = ToolRegistry.getInstance();
        var fa = reg.getTool(a);
        var fb = reg.getTool(b);
        if (fa == null || fb == null) return false;
        String ga = fa.getPresentation().groupId();
        String gb = fb.getPresentation().groupId();
        return ga != null && ga.equals(gb) && !ga.isEmpty();
    }

    // ---------------------------------------------------------------
    // Data types
    // ---------------------------------------------------------------

    private record ToolEntry(ToolFxFactory<?> factory, Tab tab,
                             FxWorkbenchTabs workbench) {}
}
