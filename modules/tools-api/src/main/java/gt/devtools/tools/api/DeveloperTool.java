package gt.devtools.tools.api;

import gt.devtools.settings.ToolConfiguration;
import gt.devtools.common.ValueProperty;

import javax.swing.*;
import java.awt.*;

/**
 * Root base class for every developer tool.
 * <p>
 * Lifecycle: {@code createComponent()} → {@code buildUi()} → {@code afterBuildUi()}
 * → {@code activated()} / {@code deactivated()} → {@code dispose()}.
 * <p>
 * One instance per workbench tab.
 */
public abstract class DeveloperTool {

    protected final ToolConfiguration config;
    protected JComponent component;
    protected boolean disposed;

    protected DeveloperTool(ToolConfiguration config) {
        this.config = config;
    }

    /**
     * Entry point: build the Swing UI and return the root component.
     */
    public final JComponent createComponent() {
        var panel = new JPanel(new BorderLayout());
        buildUi(panel);
        this.component = panel;
        afterBuildUi();
        return panel;
    }

    /**
     * Subclasses build their UI into {@code panel}.
     */
    protected abstract void buildUi(JPanel panel);

    /**
     * Hook called after UI is built and before first activation.
     */
    protected void afterBuildUi() {}

    /**
     * Called when the workbench tab containing this tool is selected.
     */
    public void activated() {}

    /**
     * Called when the workbench tab is deselected.
     */
    public void deactivated() {}

    /**
     * Called when the tool is permanently removed.
     */
    public void dispose() {
        this.disposed = true;
    }

    /**
     * Reset the tool to its default state.
     */
    public void reset() {}

    public ToolConfiguration getConfig() {
        return config;
    }

    /**
     * Convenience: register a CONFIGURATION-typed property.
     */
    protected <T> ValueProperty<T> registerConfig(String key, T defaultValue) {
        return config.register(key, defaultValue, ToolConfiguration.PropertyType.CONFIGURATION);
    }

    /**
     * Convenience: register an INPUT-typed property (persisted user content).
     */
    protected <T> ValueProperty<T> registerInput(String key, T defaultValue) {
        return config.register(key, defaultValue, ToolConfiguration.PropertyType.INPUT);
    }

    /**
     * Convenience: register a SENSITIVE property (never persisted).
     */
    protected <T> ValueProperty<T> registerSensitive(String key, T defaultValue) {
        return config.register(key, defaultValue, ToolConfiguration.PropertyType.SENSITIVE);
    }
}
