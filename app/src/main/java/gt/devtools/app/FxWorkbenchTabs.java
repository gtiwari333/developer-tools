package gt.devtools.app;

import gt.devtools.settings.SettingsManager;
import gt.devtools.tools.api.ToolFactory;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;

import java.util.UUID;

/**
 * JavaFX version of WorkbenchTabbedPane.
 * <p>
 * Manages multiple workbenches (nested tabs) for the same tool.
 * Phase 1: Shows placeholder content; actual tool UIs are still Swing.
 * Phase 2: Will host JavaFX versions of tools after base classes are ported.
 */
public final class FxWorkbenchTabs extends BorderPane {

    private final ToolFactory<?> factory;
    private final SettingsManager settingsManager;
    private final TabPane workbenchTabs;
    private Runnable onEmpty;

    public FxWorkbenchTabs(ToolFactory<?> factory, SettingsManager settingsManager) {
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

        workbenchTabs.getSelectionModel().selectedItemProperty().addListener(
                (obs, old, tab) -> {
                    // Activation/deactivation handled by Swing internals via SwingNode
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
        int count = workbenchTabs.getTabs().size() + 1;

        String title = factory.getPresentation().contentTitle();
        if (count > 1) title += " (" + count + ")";

        var tab = new Tab(title);
        // Phase 2: embed Swing tool via SwingNode interop
        tab.setContent(buildSwingInterop());
        tab.setClosable(true);

        workbenchTabs.getTabs().add(tab);
        workbenchTabs.getSelectionModel().select(tab);
    }

    /** Called when this tab becomes the selected one in the parent TabPane. */
    public void activate() {
        // Tool activation handled by Swing internals
    }

    /** Called when this tab is deselected in the parent TabPane. */
    public void deactivate() {
        // Tool deactivation handled by Swing internals
    }

    // ---------------------------------------------------------------
    // Actions
    // ---------------------------------------------------------------

    public void resetCurrentWorkbench() {
        // Phase 2: tool.reset() called via Swing internals
    }

    // ---------------------------------------------------------------
    // Tool creation (Phase 3: FX-first, fallback to Swing interop)
    // ---------------------------------------------------------------

    private BorderPane buildSwingInterop() {
        var pane = new BorderPane();
        pane.setPadding(new Insets(0));

        // Try JavaFX tool first
        var config = new gt.devtools.settings.ToolConfiguration(
                java.util.UUID.randomUUID(), factory.getPresentation().contentTitle());
        var fxTool = FxToolRegistry.createFxTool(factory.getId(), config);

        if (fxTool != null) {
            var fxNode = fxTool.createComponent();
            pane.setCenter(fxNode);
            System.out.println("JavaFX: Using native FX tool for " + factory.getId());
        } else {
            // Fall back to Swing interop
            var swingNode = new javafx.embed.swing.SwingNode();
            var tool = factory.create(config);
            var swingComponent = tool.createComponent();
            javafx.application.Platform.runLater(() ->
                    swingNode.setContent(swingComponent));
            pane.setCenter(swingNode);
        }
        return pane;
    }

    // ---------------------------------------------------------------
    // Persistence
    // ---------------------------------------------------------------

    public void saveConfigs(SettingsManager sm) {
        // Phase 2: save per-workbench configs
    }

    public void setOnEmpty(Runnable handler) {
        this.onEmpty = handler;
    }
}
