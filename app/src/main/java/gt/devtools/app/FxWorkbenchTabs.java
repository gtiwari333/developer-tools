package gt.devtools.app;

import gt.devtools.settings.SettingsManager;
import gt.devtools.settings.ToolConfiguration;
import gt.devtools.tools.api.fx.DeveloperToolFx;
import gt.devtools.tools.api.fx.ToolFxFactory;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.BorderPane;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * JavaFX workbench tabs for a single tool type.
 * <p>
 * Manages multiple workbenches (nested tabs) for the same tool.
 * Each workbench is a separate instance of the DeveloperToolFx.
 */
public final class FxWorkbenchTabs extends BorderPane {

    private final ToolFxFactory<?> factory;
    private final SettingsManager settingsManager;
    private final TabPane workbenchTabs;
    private final List<Tab> tabs = new ArrayList<>();
    private final List<DeveloperToolFx> tools = new ArrayList<>();
    private Runnable onEmpty;

    public FxWorkbenchTabs(ToolFxFactory<?> factory, SettingsManager settingsManager) {
        this.factory = factory;
        this.settingsManager = settingsManager;

        // Title bar
        setTop(new FxToolTitleBar(factory, this));

        // Workbench tabs
        workbenchTabs = new TabPane();
        workbenchTabs.setTabClosingPolicy(TabPane.TabClosingPolicy.ALL_TABS);

        // When last workbench tab closes, notify parent
        workbenchTabs.getTabs().addListener((javafx.collections.ListChangeListener<Tab>) c -> {
            while (c.next()) {
                if (c.wasRemoved() && workbenchTabs.getTabs().isEmpty() && onEmpty != null) {
                    onEmpty.run();
                }
            }
        });

        // Activation/deactivation on tab selection
        workbenchTabs.getSelectionModel().selectedIndexProperty().addListener(
                (obs, oldIdx, newIdx) -> {
                    if (oldIdx.intValue() >= 0 && oldIdx.intValue() < tools.size()) {
                        tools.get(oldIdx.intValue()).deactivated();
                    }
                    if (newIdx.intValue() >= 0 && newIdx.intValue() < tools.size()) {
                        tools.get(newIdx.intValue()).activated();
                    }
                });

        setCenter(workbenchTabs);

        // Create initial workbench
        newWorkbench();
    }

    // ---------------------------------------------------------------
    // Workbench lifecycle
    // ---------------------------------------------------------------

    /** Create a new workbench tab for this tool. */
    public void newWorkbench() {
        int count = tabs.size() + 1;

        String title = factory.getPresentation().contentTitle();
        if (count > 1) title += " (" + count + ")";

        // Create Fx tool instance directly (no Swing interop needed)
        var config = new ToolConfiguration(
                UUID.randomUUID(), factory.getPresentation().contentTitle());
        var fxTool = factory.create(config);
        var fxNode = fxTool.createComponent();

        var tab = new Tab(title);
        tab.setContent(fxNode);
        tab.setClosable(true);

        tabs.add(tab);
        tools.add(fxTool);
        workbenchTabs.getTabs().add(tab);
        workbenchTabs.getSelectionModel().select(tab);

        // Track removal for cleanup
        tab.setOnClosed(e -> {
            int idx = tabs.indexOf(tab);
            if (idx >= 0 && idx < tools.size()) {
                tools.get(idx).dispose();
                tools.remove(idx);
            }
            tabs.remove(tab);
        });

        fxTool.activated();
    }

    /** Called when this tool tab becomes the selected one in the parent TabPane. */
    public void activate() {
        int idx = workbenchTabs.getSelectionModel().getSelectedIndex();
        if (idx >= 0 && idx < tools.size()) {
            tools.get(idx).activated();
        }
    }

    /** Called when this tool tab is deselected in the parent TabPane. */
    public void deactivate() {
        int idx = workbenchTabs.getSelectionModel().getSelectedIndex();
        if (idx >= 0 && idx < tools.size()) {
            tools.get(idx).deactivated();
        }
    }

    // ---------------------------------------------------------------
    // Actions
    // ---------------------------------------------------------------

    public void resetCurrentWorkbench() {
        int idx = workbenchTabs.getSelectionModel().getSelectedIndex();
        if (idx >= 0 && idx < tools.size()) {
            tools.get(idx).reset();
        }
    }

    // ---------------------------------------------------------------
    // Persistence
    // ---------------------------------------------------------------

    public void saveConfigs(SettingsManager sm) {
        for (var tool : tools) {
            sm.saveToolConfig(tool.getConfig());
        }
    }

    public void setOnEmpty(Runnable handler) {
        this.onEmpty = handler;
    }
}
