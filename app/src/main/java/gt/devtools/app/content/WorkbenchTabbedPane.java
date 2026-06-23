package gt.devtools.app.content;

import gt.devtools.settings.SettingsManager;
import gt.devtools.settings.ToolConfiguration;
import gt.devtools.tools.api.DeveloperTool;
import gt.devtools.tools.api.ToolFactory;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * A tabbed pane that holds one or more workbench instances of the same tool.
 * Each tab = one {@link DeveloperTool} instance with its own
 * {@link ToolConfiguration}.
 */
public final class WorkbenchTabbedPane extends JPanel {

    private final ToolFactory<?> factory;
    private final SettingsManager settingsManager;
    private final JTabbedPane tabbedPane;
    private final List<TabEntry> tabs = new ArrayList<>();
    private final ToolTitleBar titleBar;

    private Runnable onEmpty; // called when all workbenches are closed

    public WorkbenchTabbedPane(ToolFactory<?> factory, SettingsManager settingsManager) {
        super(new BorderLayout());
        this.factory = factory;
        this.settingsManager = settingsManager;

        // Title bar
        titleBar = new ToolTitleBar(factory, this);
        add(titleBar, BorderLayout.NORTH);

        // Tabbed pane
        tabbedPane = new JTabbedPane(JTabbedPane.TOP);
        tabbedPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
        add(tabbedPane, BorderLayout.CENTER);

        // Create initial workbench
        newWorkbench();

        tabbedPane.addChangeListener(e -> {
            int idx = tabbedPane.getSelectedIndex();
            if (idx >= 0 && idx < tabs.size()) {
                // Deactivate previous, activate new
                for (int i = 0; i < tabs.size(); i++) {
                    if (i == idx) tabs.get(i).tool.activated();
                    else tabs.get(i).tool.deactivated();
                }
            }
        });
    }

    public void activate() {
        int idx = tabbedPane.getSelectedIndex();
        if (idx >= 0 && idx < tabs.size()) {
            tabs.get(idx).tool.activated();
        }
    }

    public void deactivate() {
        for (var tab : tabs) {
            tab.tool.deactivated();
        }
    }

    public void newWorkbench() {
        UUID id = UUID.randomUUID();
        ToolConfiguration config = settingsManager.loadToolConfig(id);
        config.setName("Workbench " + (tabs.size() + 1));

        @SuppressWarnings("unchecked")
        ToolFactory<DeveloperTool> tf = (ToolFactory<DeveloperTool>) factory;
        DeveloperTool tool = tf.create(config);
        if (tool == null) return;

        JComponent component = tool.createComponent();
        var tabPanel = new JPanel(new BorderLayout());
        tabPanel.add(component, BorderLayout.CENTER);
        tabbedPane.addTab(config.getName(), tabPanel);
        int index = tabbedPane.getTabCount() - 1;
        tabbedPane.setSelectedIndex(index);

        // Add close button to tab
        var closePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        closePanel.setOpaque(false);
        var titleLabel = new JLabel(config.getName() + "  ");
        closePanel.add(titleLabel);
        var closeBtn = new JButton("×");
        closeBtn.setFont(closeBtn.getFont().deriveFont(Font.BOLD, 14f));
        closeBtn.setBorder(BorderFactory.createEmptyBorder(0, 2, 0, 2));
        closeBtn.setContentAreaFilled(false);
        closeBtn.setFocusable(false);
        int tabIndex = index;
        closeBtn.addActionListener(e -> closeWorkbench(tabIndex));
        closePanel.add(closeBtn);
        tabbedPane.setTabComponentAt(index, closePanel);

        tabs.add(new TabEntry(config, tool));
    }

    public void closeWorkbench(int index) {
        if (index < 0 || index >= tabs.size()) return;

        TabEntry entry = tabs.remove(index);
        entry.tool.dispose();
        settingsManager.deleteToolConfig(entry.config.getId());
        tabbedPane.removeTabAt(index);

        if (tabs.isEmpty() && onEmpty != null) {
            onEmpty.run();
            return;
        }

        // Update remaining close button indices
        for (int i = 0; i < tabbedPane.getTabCount(); i++) {
            var comp = tabbedPane.getTabComponentAt(i);
            if (comp instanceof JPanel panel && panel.getComponentCount() > 1) {
                var btn = (JButton) panel.getComponent(1);
                for (var al : btn.getActionListeners()) btn.removeActionListener(al);
                final int idx = i;
                btn.addActionListener(e -> closeWorkbench(idx));
            }
        }
    }

    /** Called by ContentPanel so this workbench can request its own removal. */
    void setOnEmpty(Runnable onEmpty) { this.onEmpty = onEmpty; }

    public void resetCurrent() {
        int idx = tabbedPane.getSelectedIndex();
        if (idx >= 0 && idx < tabs.size()) {
            tabs.get(idx).tool.reset();
        }
    }

    public void saveConfigs(SettingsManager sm) {
        for (var tab : tabs) {
            sm.saveToolConfig(tab.config);
        }
    }

    public ToolFactory<?> getFactory() {
        return factory;
    }

    public String getToolId() {
        return factory.getId();
    }

    private record TabEntry(ToolConfiguration config, DeveloperTool tool) {}
}
