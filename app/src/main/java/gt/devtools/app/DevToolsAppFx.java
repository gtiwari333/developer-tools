package gt.devtools.app;

import gt.devtools.settings.AppSettings;
import gt.devtools.settings.SettingsManager;
import gt.devtools.tools.api.ToolRegistry;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * JavaFX entry point for the Developer Tools desktop application.
 * <p>
 * Phase 0: Minimal shell with BorderPane layout, empty sidebar, and
 * content area. Runs alongside the existing Swing {@link DevToolsApp}.
 * <p>
 * Launch with: {@code ./gradlew runFx}
 */
public final class DevToolsAppFx extends Application {

    private static SettingsManager settingsManager;
    private static AppSettings appSettings;

    /** Launched reflectively by JavaFX runtime. */
    public DevToolsAppFx() {}

    /**
     * Entry point called by the JavaFX launcher after {@link #init()}.
     * Builds the stage on the JavaFX Application Thread.
     */
    @Override
    public void start(Stage primaryStage) {
        // Catch uncaught exceptions on the JavaFX thread
        Thread.currentThread().setUncaughtExceptionHandler((t, e) -> {
            var sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            System.err.println("Uncaught exception on thread " + t.getName() + ":\n" + sw);
            Platform.runLater(() -> {
                var alert = new Alert(Alert.AlertType.ERROR,
                        "Unexpected error:\n" + e.getMessage());
                alert.showAndWait();
            });
        });

        // Load settings (same SettingsManager as Swing app)
        settingsManager = new SettingsManager();
        appSettings = settingsManager.loadAppSettings();

        // Discover tools via ServiceLoader
        ToolRegistry.getInstance().discover();
        registerGroups();
        System.out.println("JavaFX: Tools discovered: "
                + ToolRegistry.getInstance().getAllTools().size() + " tools");

        // Build the main window
        var mainWindow = new MainWindow(settingsManager, appSettings);
        var scene = new Scene(mainWindow, 1200, 800);

        // Apply theme
        applyTheme(scene, appSettings.getTheme());

        primaryStage.setTitle("Developer Tools");
        primaryStage.setScene(scene);
        primaryStage.setOnCloseRequest(e -> {
            mainWindow.saveSettings();
            settingsManager.saveAppSettings(appSettings);
            Platform.exit();
            System.exit(0);
        });

        primaryStage.show();
        System.out.println("JavaFX: Window visible, size="
                + primaryStage.getWidth() + "x" + primaryStage.getHeight());
    }

    private static void applyTheme(Scene scene, String theme) {
        if (!"light".equals(theme)) {
            applyDarkTheme(scene);
        }
    }

    /** Apply dark theme via data URI — avoids JPMS resource-loading issues. */
    private static void applyDarkTheme(Scene scene) {
        scene.getRoot().setStyle(DARK_THEME_CSS);
        // Also add to stylesheets for global application
        try {
            var cssBytes = DARK_THEME_CSS.getBytes(java.nio.charset.StandardCharsets.UTF_8);
            var encoded = java.util.Base64.getEncoder().encodeToString(cssBytes);
            scene.getStylesheets().add("data:text/css;base64," + encoded);
        } catch (Exception ignored) {}
    }

    private static final String DARK_THEME_CSS = """
        .root {
            -fx-base: #1e1e1e; -fx-background: #1e1e1e;
            -fx-color: derive(-fx-base, 10%);
            -fx-control-inner-background: #2d2d2d;
            -fx-control-inner-background-alt: #383838;
            -fx-text-background-color: #d4d4d4;
            -fx-text-fill: #d4d4d4; -fx-text-base-color: #d4d4d4;
            -fx-accent: #0e639c; -fx-focus-color: #0e639c;
            -fx-faint-focus-color: #0e639c22;
            -fx-box-border: #3c3c3c; -fx-outer-border: #333333;
            -fx-inner-border: #454545;
        }
        .tree-view { -fx-background-color: -fx-control-inner-background; }
        .tree-cell { -fx-background-color: -fx-control-inner-background; -fx-text-fill: -fx-text-base-color; }
        .tree-cell:selected { -fx-background-color: -fx-accent; -fx-text-fill: white; }
        .tree-cell:hover { -fx-background-color: derive(-fx-control-inner-background, 10%); }
        .split-pane-divider { -fx-background-color: #333333; -fx-padding: 1; }
        .tab-pane { -fx-background-color: -fx-background; }
        .menu-bar { -fx-background-color: #2d2d2d; }
        .menu:hover, .menu:showing { -fx-background-color: -fx-accent; }
        .context-menu { -fx-background-color: #2d2d2d; }
        .menu-item:hover { -fx-background-color: -fx-accent; }
        .scroll-pane { -fx-background-color: -fx-control-inner-background; }
        .text-field { -fx-background-color: -fx-control-inner-background; -fx-text-fill: -fx-text-base-color; }
        .text-area { -fx-control-inner-background: #2d2d2d; -fx-text-fill: #d4d4d4; }
        .text-area .content { -fx-background-color: -fx-control-inner-background; }
        .label { -fx-text-fill: -fx-text-base-color; }
        .button { -fx-background-color: #3c3c3c; -fx-text-fill: -fx-text-base-color; }
        .button:hover { -fx-background-color: #4a4a4a; }
        .button:pressed { -fx-background-color: -fx-accent; }
        .combo-box { -fx-background-color: #3c3c3c; }
        .combo-box .list-cell { -fx-text-fill: #d4d4d4; }
        .check-box { -fx-text-fill: #d4d4d4; }
        .spinner { -fx-background-color: #3c3c3c; }
        .spinner .text-field { -fx-background-color: #2d2d2d; -fx-text-fill: #d4d4d4; }
        .tab { -fx-background-color: #2d2d2d; }
        .tab:selected { -fx-background-color: #1e1e1e; }
        .tab-header-background { -fx-background-color: #252525; }
        """;

    private static void registerGroups() {
        var registry = ToolRegistry.getInstance();
        registry.registerGroup(new ToolRegistry.ToolGroupDescriptor(
                "encoders", "Encoders / Decoders", "Encode and decode text in various formats",
                1, true));
        registry.registerGroup(new ToolRegistry.ToolGroupDescriptor(
                "escape", "Text Escape", "Escape and unescape text for different contexts",
                2, false));
        registry.registerGroup(new ToolRegistry.ToolGroupDescriptor(
                "crypto", "Cryptography", "Generate identifiers, hashes, and passwords",
                3, false));
        registry.registerGroup(new ToolRegistry.ToolGroupDescriptor(
                "text", "Text Utilities", "Sort, filter, case-convert, and diff text",
                4, false));
        registry.registerGroup(new ToolRegistry.ToolGroupDescriptor(
                "formatters", "Formatters", "Format SQL, config files, CLI commands, dates, and units",
                5, false));
        registry.registerGroup(new ToolRegistry.ToolGroupDescriptor(
                "creativity", "Creativity", "ASCII art, QR codes, and colour tools",
                6, false));
        registry.registerGroup(new ToolRegistry.ToolGroupDescriptor(
                "network", "Network", "HTTP server and SSL certificate inspection",
                7, false));
        registry.registerGroup(new ToolRegistry.ToolGroupDescriptor(
                "data-inspectors", "Data Inspectors", "Inspect archives, cron expressions, and structured data",
                8, false));
    }

    /**
     * Standard main method. Calls {@link Application#launch} which
     * initializes the JavaFX runtime and calls {@link #start(Stage)}.
     */
    public static void main(String[] args) {
        launch(args);
    }
}
