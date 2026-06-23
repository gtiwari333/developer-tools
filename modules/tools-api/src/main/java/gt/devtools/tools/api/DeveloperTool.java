package gt.devtools.tools.api;

import gt.devtools.settings.ToolConfiguration;
import gt.devtools.common.ValueProperty;

import javax.swing.*;
import java.awt.*;

/**
 * Root base class for every developer tool in the application.
 *
 * <h3>What is a "tool"?</h3>
 * A tool is a self-contained Swing panel that provides one developer
 * utility: Base64 encode/decode, UUID generation, JSON formatting, etc.
 * Each tool is backed by a {@link ToolConfiguration} that persists its
 * settings and input content across restarts.
 *
 * <h3>Lifecycle</h3>
 * <ol>
 *   <li>{@link #createComponent()} — called once when the tool is first
 *       opened. Delegates to {@link #buildUi(JPanel)} then
 *       {@link #afterBuildUi()}.</li>
 *   <li>{@link #activated()} — called every time the tab containing this
 *       tool becomes visible. Register listeners here.</li>
 *   <li>{@link #deactivated()} — called when the tab is hidden (another
 *       tab is selected). Unregister listeners if they are expensive.</li>
 *   <li>{@link #dispose()} — called when the workbench tab is permanently
 *       closed. Release resources.</li>
 * </ol>
 *
 * <h3>Creating a new tool</h3>
 * <ol>
 *   <li>Pick a base class:
 *       <ul>
 *         <li>{@code EncoderDecoder} / {@code EscaperUnescaper} — two-pane
 *             encode↔decode or escape↔unescape tools</li>
 *         <li>{@code OneLineTextGenerator} — tools that generate a single
 *             value (UUID, password, hash)</li>
 *         <li>{@code TextTransformer} — one-way text→text transformations
 *             (sorting, case conversion, formatting)</li>
 *         <li>Plain {@code DeveloperTool} — for tools with a completely
 *             custom UI (color picker, QR code, notes, etc.)</li>
 *       </ul>
 *   </li>
 *   <li>Implement {@link #buildUi(JPanel)} to build your Swing UI.</li>
 *   <li>Use {@link #registerConfig}, {@link #registerInput}, and
 *       {@link #registerSensitive} to create persisted settings.</li>
 *   <li>Create a {@code public static final class Factory implements
 *       ToolFactory<YourTool>} inner class.</li>
 *   <li>Register the factory in your module's {@code ToolProvider}.</li>
 * </ol>
 *
 * @see ToolFactory
 * @see ToolConfiguration
 */
public abstract class DeveloperTool {

    /** Per-workbench configuration — use {@link #registerConfig} etc. to add properties. */
    protected final ToolConfiguration config;

    /** The root Swing component, set by {@link #createComponent()}. */
    protected JComponent component;

    /** Whether {@link #dispose()} has been called. */
    protected boolean disposed;

    protected DeveloperTool(ToolConfiguration config) {
        this.config = config;
    }

    /**
     * Build and return the Swing UI for this tool. Called once per
     * workbench instance by the framework. Do not call directly.
     */
    public final JComponent createComponent() {
        var panel = new JPanel(new BorderLayout());
        buildUi(panel);
        this.component = panel;
        afterBuildUi();
        return panel;
    }

    // ---------------------------------------------------------------
    // Subclass contract
    // ---------------------------------------------------------------

    /**
     * Build the tool's UI into {@code panel}. The panel already has a
     * {@link BorderLayout} set — add components to NORTH, CENTER, SOUTH,
     * etc. as needed.
     */
    protected abstract void buildUi(JPanel panel);

    /** Hook called after {@link #buildUi} completes but before first activation. */
    protected void afterBuildUi() {}

    /**
     * Called each time the workbench tab containing this tool is selected.
     * Register configuration change listeners and other live behaviour here.
     */
    public void activated() {}

    /**
     * Called when the workbench tab is deselected. Unregister expensive
     * listeners if necessary to avoid work while the tool is hidden.
     */
    public void deactivated() {}

    /** Called when the workbench tab is permanently closed. Release resources. */
    public void dispose() {
        this.disposed = true;
    }

    /** Reset the tool to its default state. Override to clear inputs etc. */
    public void reset() {}

    // ---------------------------------------------------------------
    // Configuration helpers
    // ---------------------------------------------------------------

    /** Shorthand for {@code config.register(key, defaultValue, CONFIGURATION)}. */
    protected <T> ValueProperty<T> registerConfig(String key, T defaultValue) {
        return config.register(key, defaultValue, ToolConfiguration.PropertyType.CONFIGURATION);
    }

    /** Shorthand for {@code config.register(key, defaultValue, INPUT)}. */
    protected <T> ValueProperty<T> registerInput(String key, T defaultValue) {
        return config.register(key, defaultValue, ToolConfiguration.PropertyType.INPUT);
    }

    /** Shorthand for {@code config.register(key, defaultValue, SENSITIVE)}. */
    protected <T> ValueProperty<T> registerSensitive(String key, T defaultValue) {
        return config.register(key, defaultValue, ToolConfiguration.PropertyType.SENSITIVE);
    }

    public ToolConfiguration getConfig() { return config; }
}
