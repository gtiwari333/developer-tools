package gt.devtools.app;

import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLightLaf;
import gt.devtools.settings.AppSettings;
import gt.devtools.settings.SettingsManager;
import gt.devtools.tools.api.ToolRegistry;

import javax.swing.*;
import java.awt.*;
import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * Entry point for the Developer Tools desktop application.
 * <p>
 * Sets up the FlatLaf theme, discovers tools via ServiceLoader,
 * and launches the main frame.
 */
public final class DevToolsApp {

    private static volatile MainFrame frame;

    private DevToolsApp() {}

    public static void main(String[] args) {
        // Force heavyweight popup menus — critical for correct dropdown positioning
        // on Linux WMs where lightweight popups get wrong screen coordinates.
        JPopupMenu.setDefaultLightWeightPopupEnabled(false);
        ToolTipManager.sharedInstance().setLightWeightPopupEnabled(false);

        // Use native OS window decorations (minimize, maximize, close buttons).
        // FlatLaf on Linux sometimes draws custom title bars that lack these.
        System.setProperty("flatlaf.useWindowDecorations", "false");

        // Catch any exceptions on the EDT so they don't kill the app silently
        Thread.setDefaultUncaughtExceptionHandler((t, e) -> {
            var sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            System.err.println("Uncaught exception on thread " + t.getName() + ":\n" + sw);
            JOptionPane.showMessageDialog(null,
                    "Unexpected error:\n" + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        });

        System.out.println("Developer Tools starting...");

        var settingsManager = new SettingsManager();
        var appSettings = settingsManager.loadAppSettings();
        System.out.println("Settings loaded, theme=" + appSettings.getTheme());

        // Apply theme
        setupTheme(appSettings);
        System.out.println("Theme applied");

        // Discover tools
        ToolRegistry.getInstance().discover();
        System.out.println("Tools discovered: " + ToolRegistry.getInstance().getAllTools().size() + " tools");

        // Register built-in groups
        registerGroups();

        // Launch on EDT and keep the main thread alive until the frame is closed
        try {
            SwingUtilities.invokeAndWait(() -> {
                frame = new MainFrame(settingsManager, appSettings);
                frame.setVisible(true);
                System.out.println("Frame visible, size=" + frame.getSize());
            });
        } catch (Exception e) {
            System.err.println("Failed to create main frame:");
            e.printStackTrace();
            System.exit(1);
        }

        // After the frame closes, System.exit(0) in saveAndExit() will terminate
    }

    private static void setupTheme(AppSettings settings) {
        String theme = settings.getTheme();
        if ("light".equals(theme)) {
            FlatLightLaf.setup();
        } else if ("dark".equals(theme)) {
            FlatDarkLaf.setup();
        } else {
            // Auto-detect OS theme
            if (isSystemDark()) {
                FlatDarkLaf.setup();
            } else {
                FlatLightLaf.setup();
            }
        }

        // Fine-tune UI defaults
        UIManager.put("Tree.rowHeight", 30);
        UIManager.put("Tree.paintLines", true);
        UIManager.put("TabbedPane.showTabSeparators", true);
        UIManager.put("TabbedPane.tabHeight", 32);
        UIManager.put("ScrollPane.smoothScrolling", true);
    }

    private static boolean isSystemDark() {
        try {
            var lnf = UIManager.getSystemLookAndFeelClassName();
            return lnf.toLowerCase().contains("dark");
        } catch (Exception e) {
            return true; // default to dark
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
                "formatters", "Formatters", "Format SQL, config files, and CLI commands",
                5, false));
    }
}
