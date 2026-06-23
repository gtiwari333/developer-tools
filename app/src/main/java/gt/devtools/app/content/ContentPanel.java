package gt.devtools.app.content;

import gt.devtools.settings.SettingsManager;
import gt.devtools.tools.api.DeveloperTool;
import gt.devtools.tools.api.ToolFactory;

import javax.swing.*;
import java.awt.*;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The right-side content area that displays the currently selected tool.
 * Manages a {@link WorkbenchTabbedPane} per tool factory so that switching
 * tools preserves state.
 */
public final class ContentPanel extends JPanel {

    private final SettingsManager settingsManager;
    private final Map<String, WorkbenchTabbedPane> toolPanes = new ConcurrentHashMap<>();
    private ToolFactory<?> currentFactory;
    private WorkbenchTabbedPane currentPane;
    private JPanel emptyPanel;

    public ContentPanel(SettingsManager settingsManager) {
        super(new BorderLayout());
        this.settingsManager = settingsManager;

        emptyPanel = new JPanel(new GridBagLayout());
        var label = new JLabel("Select a tool from the sidebar");
        label.setFont(label.getFont().deriveFont(16f));
        label.setForeground(UIManager.getColor("TextField.inactiveForeground"));
        emptyPanel.add(label);
        add(emptyPanel, BorderLayout.CENTER);
    }

    /**
     * Open (or switch to) a tool in the content area.
     */
    public void openTool(ToolFactory<?> factory) {
        if (currentFactory == factory) return;

        // Deactivate current
        if (currentPane != null) {
            currentPane.deactivate();
        }

        currentFactory = factory;
        currentPane = toolPanes.computeIfAbsent(factory.getId(),
                id -> new WorkbenchTabbedPane(factory, settingsManager));

        removeAll();
        add(currentPane, BorderLayout.CENTER);
        revalidate();
        repaint();

        currentPane.activate();
    }

    /**
     * Persist all tool configurations on exit.
     */
    public void saveAllConfigs(SettingsManager sm) {
        for (var pane : toolPanes.values()) {
            pane.saveConfigs(sm);
        }
    }
}
