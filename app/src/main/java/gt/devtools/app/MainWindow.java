package gt.devtools.app;

import gt.devtools.settings.AppSettings;
import gt.devtools.settings.SettingsManager;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;

/**
 * JavaFX main window — Phase 1 functional shell.
 * <p>
 * Layout:
 * <pre>
 * ┌───────────────────────────────────────┐
 * │  Menu Bar (File, View, Help)          │
 * ├──────────┬────────────────────────────┤
 * │          │                            │
 * │ Sidebar  │    Content Area            │
 * │ (tree)   │    (tabbed tools)          │
 * │          │                            │
 * ├──────────┴────────────────────────────┤
 * │  Status Bar                           │
 * └───────────────────────────────────────┘
 * </pre>
 */
public final class MainWindow extends BorderPane {

    private final SettingsManager settingsManager;
    private final AppSettings appSettings;
    private final FxToolSidebar sidebar;
    private final FxContentPanel contentPanel;
    private final SplitPane splitPane;
    private Label statusLabel;

    public MainWindow(SettingsManager settingsManager, AppSettings appSettings) {
        this.settingsManager = settingsManager;
        this.appSettings = appSettings;

        // -- Components
        contentPanel = new FxContentPanel(settingsManager);
        sidebar = new FxToolSidebar(contentPanel);

        // -- Top: Menu Bar
        setTop(buildMenuBar());

        // -- Center: Sidebar + Content split
        splitPane = new SplitPane(sidebar, contentPanel);
        splitPane.setOrientation(Orientation.HORIZONTAL);
        splitPane.setDividerPositions(0.22);
        SplitPane.setResizableWithParent(sidebar, false);
        setCenter(splitPane);

        // -- Bottom: Status bar
        setBottom(buildStatusBar());

        // Track sidebar selection for status updates
        contentPanel.addOpenStateListener(this::updateStatus);
    }

    // ---------------------------------------------------------------
    // Menu Bar
    // ---------------------------------------------------------------

    private MenuBar buildMenuBar() {
        var menuBar = new MenuBar();

        var fileMenu = new Menu("File");
        var settingsItem = new MenuItem("Settings");
        settingsItem.setAccelerator(new KeyCodeCombination(KeyCode.COMMA, KeyCombination.CONTROL_DOWN));
        settingsItem.setOnAction(e -> openSettings());
        var exitItem = new MenuItem("Exit");
        exitItem.setAccelerator(new KeyCodeCombination(KeyCode.Q, KeyCombination.CONTROL_DOWN));
        exitItem.setOnAction(e -> saveAndExit());
        fileMenu.getItems().addAll(settingsItem, new SeparatorMenuItem(), exitItem);

        var viewMenu = new Menu("View");
        var searchItem = new MenuItem("Focus Search");
        searchItem.setAccelerator(new KeyCodeCombination(KeyCode.F, KeyCombination.CONTROL_DOWN));
        searchItem.setOnAction(e -> sidebar.focusSearch());
        var themeItem = new MenuItem("Toggle Theme");
        themeItem.setOnAction(e -> {
            String newTheme = "dark".equals(appSettings.getTheme()) ? "light" : "dark";
            DevToolsAppFx.applyTheme(newTheme);
        });
        viewMenu.getItems().addAll(searchItem, new SeparatorMenuItem(), themeItem);

        var helpMenu = new Menu("Help");
        var aboutItem = new MenuItem("About");
        aboutItem.setOnAction(e -> {
            new Alert(Alert.AlertType.INFORMATION,
                    "Developer Tools\nJavaFX Edition (Phase 1)\nVersion 1.0.0-SNAPSHOT")
                    .showAndWait();
        });
        helpMenu.getItems().add(aboutItem);

        menuBar.getMenus().addAll(fileMenu, viewMenu, helpMenu);

        // Keyboard shortcuts on the scene
        menuBar.sceneProperty().addListener((obs, old, scene) -> {
            if (scene != null) setupKeyboardShortcuts(scene);
        });

        return menuBar;
    }

    private void setupKeyboardShortcuts(Scene scene) {
        // Ctrl+F → focus search
        scene.getAccelerators().put(
                new KeyCodeCombination(KeyCode.F, KeyCombination.CONTROL_DOWN),
                sidebar::focusSearch);
        // Ctrl+W → close current tab
        scene.getAccelerators().put(
                new KeyCodeCombination(KeyCode.W, KeyCombination.CONTROL_DOWN),
                () -> {
                    // Close current tab — handled via TabPane close
                });
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

    private void updateStatus() {
        int count = 0;
        // Count open tools via ToolRegistry + contentPanel
        var registry = gt.devtools.tools.api.ToolRegistry.getInstance();
        for (var factory : registry.getAllTools()) {
            if (contentPanel.isToolOpen(factory.getId())) count++;
        }
        statusLabel.setText(count == 0 ? "Ready"
                : count + " tool" + (count != 1 ? "s" : "") + " open");
    }

    // ---------------------------------------------------------------
    // Persistence
    // ---------------------------------------------------------------

    public void saveSettings() {
        var stage = (javafx.stage.Stage) getScene().getWindow();
        appSettings.setWindowWidth((int) stage.getWidth());
        appSettings.setWindowHeight((int) stage.getHeight());
        appSettings.setWindowX((int) stage.getX());
        appSettings.setWindowY((int) stage.getY());
        appSettings.setDividerLocation((int) (splitPane.getDividerPositions()[0]
                * getScene().getWidth()));

        contentPanel.saveAllConfigs(settingsManager);
        settingsManager.saveAppSettings(appSettings);
        System.out.println("JavaFX: Settings saved.");
    }

    private void saveAndExit() {
        saveSettings();
        Platform.exit();
        System.exit(0);
    }

    private void openSettings() {
        var dialog = new FxSettingsDialog(appSettings);
        var result = dialog.showAndWait();
        result.ifPresent(newSettings -> {
            // Copy new values back to the live AppSettings
            appSettings.setTheme(newSettings.getTheme());
            appSettings.setCheckForUpdates(newSettings.isCheckForUpdates());
            appSettings.setShowInternalTools(newSettings.isShowInternalTools());
            // Apply theme immediately if changed
            DevToolsAppFx.applyTheme(appSettings.getTheme());
            // Save to disk
            settingsManager.saveAppSettings(appSettings);
        });
    }
}
