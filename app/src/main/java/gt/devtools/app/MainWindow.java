package gt.devtools.app;

import gt.devtools.settings.AppSettings;
import gt.devtools.settings.SettingsManager;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

/**
 * JavaFX main window — Phase 0 minimal shell.
 * <p>
 * Layout:
 * <pre>
 * ┌──────────────────────────────────────┐
 * │  Menu Bar (File, View, Help)         │
 * ├──────────┬───────────────────────────┤
 * │          │                           │
 * │ Sidebar  │    Content Area           │
 * │ (280px)  │    (placeholder)          │
 * │          │                           │
 * ├──────────┴───────────────────────────┤
 * │  Status Bar                          │
 * └──────────────────────────────────────┘
 * </pre>
 */
public final class MainWindow extends BorderPane {

    private final SettingsManager settingsManager;
    private final AppSettings appSettings;
    private final SplitPane splitPane;
    private final VBox sidebar;
    private final BorderPane contentArea;
    private Label statusLabel;
    private TreeView<String> toolTree;

    public MainWindow(SettingsManager settingsManager, AppSettings appSettings) {
        this.settingsManager = settingsManager;
        this.appSettings = appSettings;

        // -- Top: Menu Bar
        setTop(buildMenuBar());

        // -- Center: Sidebar + Content split
        sidebar = buildSidebar();
        contentArea = buildContentArea();

        splitPane = new SplitPane(sidebar, contentArea);
        splitPane.setOrientation(Orientation.HORIZONTAL);
        splitPane.setDividerPositions(0.22); // ~280px at 1280px width
        SplitPane.setResizableWithParent(sidebar, false);
        setCenter(splitPane);

        // -- Bottom: Status bar
        setBottom(buildStatusBar());
    }

    // ---------------------------------------------------------------
    // Menu Bar
    // ---------------------------------------------------------------

    private MenuBar buildMenuBar() {
        var menuBar = new MenuBar();

        var fileMenu = new Menu("File");
        var settingsItem = new MenuItem("Settings");
        settingsItem.setOnAction(e -> System.out.println("Settings not yet implemented in JavaFX shell"));
        var exitItem = new MenuItem("Exit");
        exitItem.setOnAction(e -> {
            saveSettings();
            javafx.application.Platform.exit();
            System.exit(0);
        });
        fileMenu.getItems().addAll(settingsItem, new SeparatorMenuItem(), exitItem);

        var viewMenu = new Menu("View");
        var themeItem = new MenuItem("Toggle Theme");
        themeItem.setOnAction(e -> {
            String current = appSettings.getTheme();
            appSettings.setTheme("dark".equals(current) ? "light" : "dark");
            // Theme switch would be applied on restart in Phase 0
            System.out.println("Theme changed to: " + appSettings.getTheme() + " (restart to apply)");
        });
        viewMenu.getItems().add(themeItem);

        var helpMenu = new Menu("Help");
        var aboutItem = new MenuItem("About");
        aboutItem.setOnAction(e -> {
            var alert = new Alert(Alert.AlertType.INFORMATION,
                    "Developer Tools\nJavaFX Edition (Phase 0 Shell)\nVersion 1.0.0-SNAPSHOT");
            alert.setTitle("About");
            alert.showAndWait();
        });
        helpMenu.getItems().add(aboutItem);

        menuBar.getMenus().addAll(fileMenu, viewMenu, helpMenu);
        return menuBar;
    }

    // ---------------------------------------------------------------
    // Sidebar
    // ---------------------------------------------------------------

    private VBox buildSidebar() {
        var sidebarPanel = new VBox();
        sidebarPanel.setPadding(new Insets(8));
        sidebarPanel.setSpacing(4);
        sidebarPanel.setMinWidth(240);
        sidebarPanel.setPrefWidth(280);

        // Search field
        var searchField = new TextField();
        searchField.setPromptText("Filter tools...");
        searchField.textProperty().addListener((obs, old, text) -> filterTree(text));
        sidebarPanel.getChildren().add(searchField);

        // Tool tree
        toolTree = buildToolTree();
        VBox.setVgrow(toolTree, Priority.ALWAYS);
        sidebarPanel.getChildren().add(toolTree);

        return sidebarPanel;
    }

    private TreeView<String> buildToolTree() {
        var registry = gt.devtools.tools.api.ToolRegistry.getInstance();

        var root = new TreeItem<String>("Developer Tools");
        root.setExpanded(true);

        for (var group : registry.getGroups()) {
            var groupItem = new TreeItem<String>(group.menuTitle());
            groupItem.setExpanded(group.initiallyExpanded());

            for (var factory : registry.getToolsByGroup(group.id())) {
                var toolItem = new TreeItem<String>(
                        factory.getPresentation().menuTitle());
                groupItem.getChildren().add(toolItem);
            }
            if (!groupItem.getChildren().isEmpty()) {
                root.getChildren().add(groupItem);
            }
        }

        // Ungrouped tools
        for (String id : registry.getUngroupedToolIds()) {
            var factory = registry.getTool(id);
            if (factory != null) {
                root.getChildren().add(
                        new TreeItem<String>(factory.getPresentation().menuTitle()));
            }
        }

        var tree = new TreeView<String>(root);
        tree.setShowRoot(false);
        tree.setCellFactory(tv -> new TreeCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item);
            }
        });

        // Single-click selects, would open tool in Phase 2
        tree.getSelectionModel().selectedItemProperty().addListener(
                (obs, old, selected) -> {
                    if (selected != null && selected.isLeaf()) {
                        statusLabel.setText("Selected: " + selected.getValue()
                                + " (tool opening in Phase 2)");
                    }
                });

        return tree;
    }

    private void filterTree(String query) {
        // Phase 1 will implement full filter logic; Phase 0 skeleton
        if (query == null || query.isEmpty()) {
            // Reset tree — rebuild from registry
            // (Simplified for Phase 0)
        }
    }

    // ---------------------------------------------------------------
    // Content Area
    // ---------------------------------------------------------------

    private BorderPane buildContentArea() {
        var area = new BorderPane();
        area.setPadding(new Insets(16));

        var placeholder = new Label("Select a tool from the sidebar to begin.");
        placeholder.setStyle("-fx-text-fill: -fx-text-background-color; -fx-font-size: 14px;");
        area.setCenter(placeholder);

        return area;
    }

    // ---------------------------------------------------------------
    // Status Bar
    // ---------------------------------------------------------------

    private HBox buildStatusBar() {
        var bar = new HBox();
        bar.setPadding(new Insets(4, 12, 4, 12));
        bar.setStyle("-fx-background-color: -fx-control-inner-background; "
                + "-fx-border-color: -fx-box-border; -fx-border-width: 1 0 0 0;");

        statusLabel = new Label("Ready");
        statusLabel.setStyle("-fx-font-size: 11px;");
        bar.getChildren().add(statusLabel);

        return bar;
    }

    // ---------------------------------------------------------------
    // Persistence
    // ---------------------------------------------------------------

    /** Save window geometry and settings before exit. */
    public void saveSettings() {
        var stage = (javafx.stage.Stage) getScene().getWindow();
        appSettings.setWindowWidth((int) stage.getWidth());
        appSettings.setWindowHeight((int) stage.getHeight());
        appSettings.setWindowX((int) stage.getX());
        appSettings.setWindowY((int) stage.getY());
        appSettings.setDividerLocation((int) (splitPane.getDividerPositions()[0]
                * getScene().getWidth()));
        System.out.println("JavaFX: Settings saved.");
    }
}
