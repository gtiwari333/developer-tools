package gt.devtools.app.content;

import gt.devtools.settings.SettingsManager;
import gt.devtools.tools.api.ToolFactory;

import javax.swing.*;
import java.awt.*;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Right-side content area with a tabbed pane. Each selected tool opens
 * as a new tab; switching tools keeps previous tools open with their
 * state intact. Tabs can be closed individually with the X button.
 */
public final class ContentPanel extends JPanel {

    private final SettingsManager settingsManager;
    private final JTabbedPane tabbedPane;
    private final Map<String, ToolTab> openTabs = new LinkedHashMap<>();
    private final JPanel emptyPanel;

    public ContentPanel(SettingsManager settingsManager) {
        super(new BorderLayout());
        this.settingsManager = settingsManager;

        tabbedPane = new JTabbedPane(JTabbedPane.TOP);
        tabbedPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
        tabbedPane.addChangeListener(e -> {
            int idx = tabbedPane.getSelectedIndex();
            if (idx >= 0) {
                var comp = tabbedPane.getComponentAt(idx);
                for (var tab : openTabs.values()) {
                    if (tab.workbench == comp) {
                        tab.workbench.activate();
                    } else {
                        tab.workbench.deactivate();
                    }
                }
            }
        });

        emptyPanel = new JPanel(new GridBagLayout());
        var label = new JLabel("Select a tool from the sidebar");
        label.setFont(label.getFont().deriveFont(16f));
        label.setForeground(UIManager.getColor("TextField.inactiveForeground"));
        emptyPanel.add(label);

        add(emptyPanel, BorderLayout.CENTER);
    }

    /**
     * Open a tool: if it's already open in a tab, switch to it;
     * otherwise create a new tab. Never replaces existing tabs.
     */
    public void openTool(ToolFactory<?> factory) {
        String toolId = factory.getId();

        // Already open — just select its tab
        ToolTab existing = openTabs.get(toolId);
        if (existing != null) {
            int idx = tabbedPane.indexOfComponent(existing.workbench);
            if (idx >= 0) {
                tabbedPane.setSelectedIndex(idx);
                return;
            }
        }

        // Create new tab
        var workbench = new WorkbenchTabbedPane(factory, settingsManager);
        openTabs.put(toolId, new ToolTab(factory, workbench));

        // Switch from empty state to tabbed pane
        if (tabbedPane.getTabCount() == 0) {
            removeAll();
            add(tabbedPane, BorderLayout.CENTER);
        }

        tabbedPane.addTab(factory.getPresentation().contentTitle(), workbench);
        int index = tabbedPane.getTabCount() - 1;
        tabbedPane.setSelectedIndex(index);
        tabbedPane.setTabComponentAt(index, createTabHeader(factory, workbench, index));

        workbench.activate();
        revalidate();
        repaint();
    }

    private JPanel createTabHeader(ToolFactory<?> factory, WorkbenchTabbedPane workbench, int tabIndex) {
        var header = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        header.setOpaque(false);

        var titleLabel = new JLabel(factory.getPresentation().contentTitle());
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 4));
        header.add(titleLabel);

        var closeBtn = new JButton("×");
        closeBtn.setFont(closeBtn.getFont().deriveFont(Font.BOLD, 14f));
        closeBtn.setBorder(BorderFactory.createEmptyBorder(0, 2, 0, 2));
        closeBtn.setContentAreaFilled(false);
        closeBtn.setFocusable(false);
        closeBtn.setToolTipText("Close " + factory.getPresentation().contentTitle());
        closeBtn.addActionListener(e -> closeTool(factory.getId()));
        header.add(closeBtn);

        return header;
    }

    /**
     * Close a tool tab and dispose its workbench.
     */
    public void closeTool(String toolId) {
        ToolTab tab = openTabs.remove(toolId);
        if (tab == null) return;

        int idx = tabbedPane.indexOfComponent(tab.workbench);
        if (idx >= 0) {
            tab.workbench.deactivate();
            tab.workbench.saveConfigs(settingsManager);
            tabbedPane.removeTabAt(idx);
        }

        // If no tabs left, show empty state
        if (tabbedPane.getTabCount() == 0) {
            removeAll();
            add(emptyPanel, BorderLayout.CENTER);
            revalidate();
            repaint();
        }

        // Update close button indices for remaining tabs
        for (int i = 0; i < tabbedPane.getTabCount(); i++) {
            var comp = tabbedPane.getTabComponentAt(i);
            if (comp instanceof JPanel header && header.getComponentCount() > 1) {
                var btn = (JButton) header.getComponent(1);
                for (var al : btn.getActionListeners()) btn.removeActionListener(al);
                final int idx2 = i;
                // Find the tool ID for this tab
                var toolId2 = findToolIdForComponent(tabbedPane.getComponentAt(i));
                if (toolId2 != null) {
                    btn.addActionListener(e -> closeTool(toolId2));
                }
            }
        }
    }

    private String findToolIdForComponent(Component comp) {
        for (var entry : openTabs.entrySet()) {
            if (entry.getValue().workbench == comp) {
                return entry.getKey();
            }
        }
        return null;
    }

    /**
     * Persist all tool configurations on exit.
     */
    public void saveAllConfigs(SettingsManager sm) {
        for (var tab : openTabs.values()) {
            tab.workbench.saveConfigs(sm);
        }
    }

    private record ToolTab(ToolFactory<?> factory, WorkbenchTabbedPane workbench) {}
}
