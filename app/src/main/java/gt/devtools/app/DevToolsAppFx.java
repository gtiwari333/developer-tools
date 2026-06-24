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
            // JavaFX module-path resource lookup: module-name:path
            var moduleCss = "gt.devtools.app:/gt/devtools/app/dark-theme.css";
            var url = DevToolsAppFx.class.getResource("/gt/devtools/app/dark-theme.css");
            if (url != null) {
                scene.getStylesheets().add(url.toExternalForm());
            } else {
                System.err.println("JavaFX: dark-theme.css not found via classpath; "
                        + "trying module-path fallback. "
                        + "Theme will be fully resolved in Phase 5 with AtlantaFX.");
            }
        }
    }

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
