package gt.devtools.app.content;

import gt.devtools.settings.SettingsManager;
import gt.devtools.tools.api.ToolFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Right-side content area with a tabbed pane. Each selected tool opens
 * as a new tab; switching tools keeps previous tools open with their
 * state intact.
 * <p>
 * Supports pinning tabs (pinned tabs survive "close similar" and "close all")
 * and a right-click context menu for batch tab operations.
 */
public final class ContentPanel extends JPanel {

    private final SettingsManager settingsManager;
    private final JTabbedPane tabbedPane;
    private final Map<String, ToolTab> openTabs = new LinkedHashMap<>();
    private final Set<String> pinnedToolIds = new LinkedHashSet<>();
    private final JPanel emptyPanel;
    private final List<Runnable> openStateListeners = new ArrayList<>();

    public ContentPanel(SettingsManager settingsManager) {
        super(new BorderLayout());
        this.settingsManager = settingsManager;

        tabbedPane = new JTabbedPane(JTabbedPane.TOP);
        tabbedPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);

        // Activate/deactivate tools on tab switch
        tabbedPane.addChangeListener(e -> {
            int idx = tabbedPane.getSelectedIndex();
            if (idx >= 0) {
                var comp = tabbedPane.getComponentAt(idx);
                for (var tab : openTabs.values()) {
                    if (tab.workbench == comp) tab.workbench.activate();
                    else tab.workbench.deactivate();
                }
            }
        });

        // Right-click context menu on tabs
        tabbedPane.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (e.isPopupTrigger()) showTabContextMenu(e);
            }
            @Override
            public void mouseReleased(MouseEvent e) {
                if (e.isPopupTrigger()) showTabContextMenu(e);
            }
        });

        emptyPanel = new JPanel(new GridBagLayout());
        var label = new JLabel("Select a tool from the sidebar");
        label.setFont(label.getFont().deriveFont(16f));
        label.setForeground(UIManager.getColor("TextField.inactiveForeground"));
        emptyPanel.add(label);

        add(emptyPanel, BorderLayout.CENTER);
    }

    // ---------------------------------------------------------------
    // Open / close
    // ---------------------------------------------------------------

    /**
     * Open a tool or focus its existing tab (single-click behaviour).
     */
    public void openTool(ToolFactory<?> factory) {
        String toolId = factory.getId();

        ToolTab existing = openTabs.get(toolId);
        if (existing != null) {
            int idx = tabbedPane.indexOfComponent(existing.workbench);
            if (idx >= 0) { tabbedPane.setSelectedIndex(idx); return; }
        }

        openToolInNewTab(factory);
    }

    /**
     * Always create a fresh tab for this tool, even if one is already open.
     * Used for double-click and right-click "Open in new tab".
     */
    public void openToolInNewTab(ToolFactory<?> factory) {
        String toolId = factory.getId();

        var workbench = new WorkbenchTabbedPane(factory, settingsManager);
        // Use a unique key when multiple tabs of the same tool are open
        String uniqueKey = toolId + "@" + System.nanoTime();
        openTabs.put(uniqueKey, new ToolTab(factory, workbench));

        if (tabbedPane.getTabCount() == 0) {
            removeAll();
            add(tabbedPane, BorderLayout.CENTER);
        }

        String title = factory.getPresentation().contentTitle();
        // Append count if there are already tabs for this tool
        int sameToolCount = countOpenTabsFor(toolId);
        if (sameToolCount > 1) title = title + " (" + sameToolCount + ")";

        tabbedPane.addTab(null, workbench);
        int index = tabbedPane.getTabCount() - 1;
        tabbedPane.setTabComponentAt(index, createTabHeader(factory, workbench, uniqueKey, title));
        tabbedPane.setSelectedIndex(index);

        workbench.activate();
        revalidate();
        repaint();
        fireOpenStateChanged();
    }

    private int countOpenTabsFor(String toolId) {
        int count = 0;
        for (String key : openTabs.keySet()) {
            if (key.startsWith(toolId + "@")) count++;
        }
        return count;
    }

    /** Close a specific tool tab. Always allowed even if pinned. */
    public void closeTool(String toolId) {
        closeToolInternal(toolId, true);
    }

    /** Close a tool tab unless its base tool is pinned and force is false. */
    private void closeToolInternal(String uniqueKey, boolean force) {
        if (!force && pinnedToolIds.contains(extractBaseId(uniqueKey))) return;

        ToolTab tab = openTabs.remove(uniqueKey);
        if (tab == null) return;
        // Only remove pin if this was the last tab for that tool
        String base = extractBaseId(uniqueKey);
        if (openTabs.keySet().stream().noneMatch(k -> extractBaseId(k).equals(base))) {
            pinnedToolIds.remove(base);
        }

        int idx = tabbedPane.indexOfComponent(tab.workbench);
        if (idx >= 0) {
            tab.workbench.deactivate();
            tab.workbench.saveConfigs(settingsManager);
            tabbedPane.removeTabAt(idx);
        }

        if (tabbedPane.getTabCount() == 0) {
            removeAll();
            add(emptyPanel, BorderLayout.CENTER);
            revalidate();
            repaint();
        }

        rebuildCloseButtonListeners();
        fireOpenStateChanged();
    }

    // ---------------------------------------------------------------
    // Batch operations
    // ---------------------------------------------------------------

    /** Close all tabs in the same group as {@code uniqueKey}, except this one and pinned. */
    public void closeSimilarTools(String uniqueKey) {
        String baseId = extractBaseId(uniqueKey);
        for (String id : new ArrayList<>(openTabs.keySet())) {
            if (id.equals(uniqueKey)) continue; // keep this tab
            if (sameToolGroup(baseId, extractBaseId(id))) {
                closeToolInternal(id, false);
            }
        }
    }

    /** Close all tabs except pinned ones. */
    public void closeAllTools() {
        for (String id : new ArrayList<>(openTabs.keySet())) {
            closeToolInternal(id, false);
        }
    }

    /** Toggle pin. Pins are stored by base tool ID (not unique key). */
    public void togglePin(String uniqueKey) {
        String base = extractBaseId(uniqueKey);
        if (pinnedToolIds.contains(base)) {
            pinnedToolIds.remove(base);
        } else if (openTabs.containsKey(uniqueKey)) {
            pinnedToolIds.add(base);
        }
        refreshTabHeaders();
    }

    public boolean isPinned(String uniqueKey) {
        return pinnedToolIds.contains(extractBaseId(uniqueKey));
    }

    /** Strip the "@timestamp" suffix to get the base tool ID. */
    static String extractBaseId(String uniqueKey) {
        int at = uniqueKey.lastIndexOf('@');
        return at > 0 ? uniqueKey.substring(0, at) : uniqueKey;
    }

    // ---------------------------------------------------------------
    // UI helpers
    // ---------------------------------------------------------------

    private void showTabContextMenu(MouseEvent e) {
        int idx = tabbedPane.indexAtLocation(e.getX(), e.getY());
        if (idx < 0) return;

        String toolId = findToolIdForComponent(tabbedPane.getComponentAt(idx));
        if (toolId == null) return;

        var menu = new JPopupMenu();

        var closeThis = new JMenuItem("Close this tab");
        closeThis.addActionListener(ev -> closeTool(toolId));
        menu.add(closeThis);

        var closeSimilar = new JMenuItem("Close similar tool tabs");
        closeSimilar.addActionListener(ev -> closeSimilarTools(toolId));
        menu.add(closeSimilar);

        menu.addSeparator();

        var closeAll = new JMenuItem("Close all tabs");
        closeAll.addActionListener(ev -> closeAllTools());
        menu.add(closeAll);

        var pinItem = new JMenuItem(pinnedToolIds.contains(toolId)
                ? "Unpin tab" : "Pin tab");
        pinItem.addActionListener(ev -> togglePin(toolId));
        menu.add(pinItem);

        menu.show(tabbedPane, e.getX(), e.getY());
    }

    private JPanel createTabHeader(ToolFactory<?> factory, WorkbenchTabbedPane workbench,
                                   String toolId) {
        return createTabHeader(factory, workbench, toolId,
                factory.getPresentation().contentTitle());
    }

    private JPanel createTabHeader(ToolFactory<?> factory, WorkbenchTabbedPane workbench,
                                   String toolId, String title) {
        var header = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        header.setOpaque(false);

        // Pin toggle
        var pinBtn = new JButton(new PinIcon(pinnedToolIds.contains(toolId)));
        pinBtn.setBorder(BorderFactory.createEmptyBorder(0, 2, 0, 0));
        pinBtn.setContentAreaFilled(false);
        pinBtn.setFocusable(false);
        pinBtn.setToolTipText("Pin tab — pinned tabs survive 'close similar' and 'close all'");
        pinBtn.addActionListener(e -> {
            togglePin(toolId);
            pinBtn.setIcon(new PinIcon(pinnedToolIds.contains(toolId)));
        });
        header.add(pinBtn);

        var titleLabel = new JLabel(title);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 2, 0, 4));
        header.add(titleLabel);

        var closeBtn = new JButton("×");
        closeBtn.setFont(closeBtn.getFont().deriveFont(Font.BOLD, 14f));
        closeBtn.setBorder(BorderFactory.createEmptyBorder(0, 2, 0, 2));
        closeBtn.setContentAreaFilled(false);
        closeBtn.setFocusable(false);
        closeBtn.setToolTipText("Close " + factory.getPresentation().contentTitle());
        closeBtn.addActionListener(e -> closeTool(toolId));
        header.add(closeBtn);

        return header;
    }

    /** Refresh all tab headers after pin state changes. */
    private void refreshTabHeaders() {
        for (int i = 0; i < tabbedPane.getTabCount(); i++) {
            var comp = tabbedPane.getComponentAt(i);
            String toolId = findToolIdForComponent(comp);
            if (toolId != null) {
                var tab = openTabs.get(toolId);
                if (tab != null) {
                    tabbedPane.setTabComponentAt(i,
                            createTabHeader(tab.factory, tab.workbench, toolId));
                }
            }
        }
    }

    /** Repair close-button listeners after a tab removal shifts indices. */
    private void rebuildCloseButtonListeners() {
        for (int i = 0; i < tabbedPane.getTabCount(); i++) {
            var comp = tabbedPane.getTabComponentAt(i);
            if (comp instanceof JPanel header && header.getComponentCount() >= 3) {
                // Components: [pinBtn, titleLabel, closeBtn]
                var closeBtn = (JButton) header.getComponent(2);
                for (var al : closeBtn.getActionListeners()) closeBtn.removeActionListener(al);
                String toolId = findToolIdForComponent(tabbedPane.getComponentAt(i));
                if (toolId != null) {
                    var id = toolId; // effectively final
                    closeBtn.addActionListener(e -> closeTool(id));
                }
            }
        }
    }

    // ---------------------------------------------------------------
    // Queries
    // ---------------------------------------------------------------

    private String findToolIdForComponent(Component comp) {
        for (var entry : openTabs.entrySet()) {
            if (entry.getValue().workbench == comp) return entry.getKey();
        }
        return null;
    }

    /** Check if two tool IDs belong to the same group for "close similar". */
    private boolean sameToolGroup(String a, String b) {
        if (a.equals(b)) return true;
        var reg = gt.devtools.tools.api.ToolRegistry.getInstance();
        var fa = reg.getTool(a);
        var fb = reg.getTool(b);
        if (fa == null || fb == null) return false;
        String ga = fa.getPresentation().groupId();
        String gb = fb.getPresentation().groupId();
        return ga != null && ga.equals(gb) && !ga.isEmpty();
    }

    /** True if any tab of this tool type is open. */
    public boolean isToolOpen(String toolId) {
        return openTabs.keySet().stream().anyMatch(k -> k.startsWith(toolId + "@"));
    }
    public void addOpenStateListener(Runnable listener) { openStateListeners.add(listener); }
    private void fireOpenStateChanged() { openStateListeners.forEach(Runnable::run); }

    public void saveAllConfigs(SettingsManager sm) {
        for (var tab : openTabs.values()) tab.workbench.saveConfigs(sm);
    }

    private record ToolTab(ToolFactory<?> factory, WorkbenchTabbedPane workbench) {}
}
