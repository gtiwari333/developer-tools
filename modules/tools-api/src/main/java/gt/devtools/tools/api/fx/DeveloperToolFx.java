package gt.devtools.tools.api.fx;

import gt.devtools.common.ValueProperty;
import gt.devtools.settings.ToolConfiguration;
import javafx.scene.Node;
import javafx.scene.layout.BorderPane;

/**
 * JavaFX root base class for every developer tool.
 * <p>
 * Mirrors {@link gt.devtools.tools.api.DeveloperTool} for JavaFX.
 * Subclasses implement {@link #buildUi(BorderPane)} to construct their UI,
 * and optionally override lifecycle methods.
 *
 * <h3>Lifecycle</h3>
 * <ol>
 *   <li>{@link #createComponent()} — called once, delegates to buildUi</li>
 *   <li>{@link #activated()} — tab becomes visible</li>
 *   <li>{@link #deactivated()} — tab hidden</li>
 *   <li>{@link #dispose()} — tab closed permanently</li>
 * </ol>
 */
public abstract class DeveloperToolFx {

    protected final ToolConfiguration config;
    protected BorderPane rootPane;
    protected boolean disposed;

    protected DeveloperToolFx(ToolConfiguration config) {
        this.config = config;
    }

    /** Build and return the JavaFX UI node. Called once per workbench. */
    public final Node createComponent() {
        rootPane = new BorderPane();
        buildUi(rootPane);
        afterBuildUi();
        return rootPane;
    }

    // ---------------------------------------------------------------
    // Subclass contract
    // ---------------------------------------------------------------

    /** Build the tool's UI into the given BorderPane. */
    protected abstract void buildUi(BorderPane panel);

    /** Hook called after buildUi completes. */
    protected void afterBuildUi() {}

    /** Called when the workbench tab becomes visible. */
    public void activated() {}

    /** Called when the workbench tab is hidden. */
    public void deactivated() {}

    /** Called when the workbench tab is permanently closed. */
    public void dispose() { this.disposed = true; }

    /** Reset the tool to defaults. */
    public void reset() {}

    // ---------------------------------------------------------------
    // Configuration helpers
    // ---------------------------------------------------------------

    protected <T> ValueProperty<T> registerConfig(String key, T defaultValue) {
        return config.register(key, defaultValue, ToolConfiguration.PropertyType.CONFIGURATION);
    }

    protected <T> ValueProperty<T> registerInput(String key, T defaultValue) {
        return config.register(key, defaultValue, ToolConfiguration.PropertyType.INPUT);
    }

    protected <T> ValueProperty<T> registerSensitive(String key, T defaultValue) {
        return config.register(key, defaultValue, ToolConfiguration.PropertyType.SENSITIVE);
    }

    public ToolConfiguration getConfig() { return config; }
}
