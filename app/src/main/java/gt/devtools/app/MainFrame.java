package gt.devtools.app;

import gt.devtools.app.sidebar.ToolSidebar;
import gt.devtools.app.content.ContentPanel;
import gt.devtools.settings.AppSettings;
import gt.devtools.settings.SettingsManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * Main application frame with a sidebar navigation tree and a
 * tabbed content area.
 */
public final class MainFrame extends JFrame {

    private final SettingsManager settingsManager;
    private final AppSettings appSettings;
    private final ToolSidebar sidebar;
    private final ContentPanel contentPanel;
    private final JSplitPane splitPane;
    private boolean menuBarInstalled;

    public MainFrame(SettingsManager settingsManager, AppSettings appSettings) {
        this.settingsManager = settingsManager;
        this.appSettings = appSettings;

        setTitle("Developer Tools");
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);

        // Build content
        contentPanel = new ContentPanel(settingsManager);
        sidebar = new ToolSidebar(contentPanel);

        splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, sidebar, contentPanel);
        splitPane.setDividerLocation(appSettings.getDividerLocation());
        splitPane.setDividerSize(3);
        add(splitPane, BorderLayout.CENTER);
        add(buildStatusBar(), BorderLayout.SOUTH);

        // Size and position — use 75% of screen on first launch
        int width = appSettings.getWindowWidth();
        int height = appSettings.getWindowHeight();
        if (width <= 0 || height <= 0) {
            var screen = Toolkit.getDefaultToolkit().getScreenSize();
            width = (int) (screen.width * 0.75);
            height = (int) (screen.height * 0.75);
        }
        setSize(width, height);
        if (appSettings.getWindowX() >= 0 && appSettings.getWindowY() >= 0) {
            setLocation(appSettings.getWindowX(), appSettings.getWindowY());
        } else {
            setLocationRelativeTo(null);
        }

        // Defer menu bar until the window is fully on-screen so popup
        // coordinates are calculated from the WM-assigned position.
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowOpened(WindowEvent e) {
                if (!menuBarInstalled) {
                    menuBarInstalled = true;
                    setJMenuBar(buildMenuBar());
                    revalidate();
                }
                // Check for updates in the background after a short delay
                if (appSettings.isCheckForUpdates()) {
                    checkForUpdates();
                }
            }
            @Override
            public void windowClosing(WindowEvent e) {
                saveAndExit();
            }
        });

        // Initial tool selection
        String lastTool = appSettings.getLastSelectedTool();
        if (lastTool != null && !lastTool.isEmpty()) {
            SwingUtilities.invokeLater(() -> sidebar.selectTool(lastTool));
        }

        // Divider listener
        splitPane.addPropertyChangeListener(JSplitPane.DIVIDER_LOCATION_PROPERTY, e -> {
            appSettings.setDividerLocation(splitPane.getDividerLocation());
        });
    }

    private JMenuBar buildMenuBar() {
        var mb = new JMenuBar();

        // File
        var fileMenu = new JMenu("File");
        var settingsItem = new JMenuItem("Settings");
        settingsItem.addActionListener(e -> openSettings());
        fileMenu.add(settingsItem);
        fileMenu.addSeparator();
        var exitItem = new JMenuItem("Exit");
        exitItem.addActionListener(e -> saveAndExit());
        fileMenu.add(exitItem);
        mb.add(fileMenu);

        // View
        var viewMenu = new JMenu("View");
        var toggleTheme = new JMenuItem("Toggle Theme");
        toggleTheme.addActionListener(e -> toggleTheme());
        viewMenu.add(toggleTheme);
        mb.add(viewMenu);

        // Help
        var helpMenu = new JMenu("Help");
        var aboutItem = new JMenuItem("About");
        aboutItem.addActionListener(e -> showAbout());
        helpMenu.add(aboutItem);
        mb.add(helpMenu);

        return mb;
    }

    private JPanel buildStatusBar() {
        var bar = new JPanel(new BorderLayout());
        bar.setBorder(BorderFactory.createEmptyBorder(2, 8, 2, 8));
        var label = new JLabel("Developer Tools v" + getVersion());
        label.setFont(label.getFont().deriveFont(11f));
        bar.add(label, BorderLayout.WEST);
        return bar;
    }

    private String getVersion() {
        String version = getClass().getPackage().getImplementationVersion();
        return version != null ? version : "1.0.0-SNAPSHOT";
    }

    private void checkForUpdates() {
        // Delay 3 seconds so the UI is fully painted before network I/O
        new javax.swing.Timer(3000, e -> {
            ((javax.swing.Timer) e.getSource()).stop();
            new SwingWorker<Void, Void>() {
                @Override
                protected Void doInBackground() {
                    try {
                        var checker = new gt.devtools.settings.UpdateChecker(
                                "your-github-user", "developer-tools-desktop", getVersion());
                        var info = checker.check();
                        if (info != null) {
                            SwingUtilities.invokeLater(() ->
                                    new UpdateDialog(MainFrame.this, info, settingsManager)
                                            .setVisible(true));
                        }
                    } catch (Exception ignored) {
                        // Network errors are silent — don't bother the user
                    }
                    return null;
                }
            }.execute();
        }).start();
    }

    private void toggleTheme() {
        String current = appSettings.getTheme();
        appSettings.setTheme("dark".equals(current) ? "light" : "dark");
        settingsManager.saveAppSettings(appSettings);
        JOptionPane.showMessageDialog(this,
                "Theme change will apply on next restart.",
                "Theme Changed", JOptionPane.INFORMATION_MESSAGE);
    }

    private void openSettings() {
        JOptionPane.showMessageDialog(this,
                "Settings dialog coming in a future phase.",
                "Settings", JOptionPane.INFORMATION_MESSAGE);
    }

    private void showAbout() {
        JOptionPane.showMessageDialog(this,
                """
                Developer Tools Desktop
                Version %s

                A collection of developer utilities
                for everyday programming tasks.

                Built with Java 25, Swing, and FlatLaf.
                """.formatted(getVersion()),
                "About Developer Tools",
                JOptionPane.INFORMATION_MESSAGE);
    }

    private void saveAndExit() {
        // Save window state
        appSettings.setWindowWidth(getWidth());
        appSettings.setWindowHeight(getHeight());
        appSettings.setWindowX(getX());
        appSettings.setWindowY(getY());
        appSettings.setDividerLocation(splitPane.getDividerLocation());
        appSettings.setLastSelectedTool(sidebar.getSelectedToolId());
        settingsManager.saveAppSettings(appSettings);

        // Save tool configs
        contentPanel.saveAllConfigs(settingsManager);

        dispose();
        System.exit(0);
    }

    public ToolSidebar getSidebar() { return sidebar; }
    public ContentPanel getContentPanel() { return contentPanel; }
}
