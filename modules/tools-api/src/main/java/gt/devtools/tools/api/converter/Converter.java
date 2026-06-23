package gt.devtools.tools.api.converter;

import gt.devtools.common.ValueProperty;
import gt.devtools.settings.ToolConfiguration;
import gt.devtools.tools.api.DeveloperTool;

import javax.swing.*;
import java.awt.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Base class for tools that convert text from one form to another.
 * Provides a vertical split pane with a source editor on top, an action bar
 * in the middle, and a target editor at the bottom.
 *
 * <h3>Layout</h3>
 * <pre>
 * ┌──────────────────────────────┐
 * │  Source editor (INPUT)       │
 * ├──────────────────────────────┤
 * │ [Convert ↓] [✓ Live] [Copy] [↑↓ Swap] │
 * ├──────────────────────────────┤
 * │  Target editor (OUTPUT)      │
 * └──────────────────────────────┘
 * </pre>
 *
 * <h3>Live conversion</h3>
 * When the "Live" checkbox is checked, every change to the source triggers
 * a debounced (300ms) conversion on a background thread. Results are
 * posted back to the target editor on the EDT.
 *
 * <h3>Subclassing</h3>
 * Implement {@link #doConvertForward(byte[])} to provide the conversion
 * logic. Override {@link #buildActionBar()} to customise the button row.
 *
 * <h3>Persistence</h3>
 * Both source and target text are persisted as {@code INPUT}-type
 * properties so user work survives restarts.
 */
public abstract class Converter extends DeveloperTool {

    /** The source (input) editor — editable by the user. */
    protected TextEditor sourceEditor;

    /** The target (output) editor — read-only, shows conversion results. */
    protected TextEditor targetEditor;

    /** Whether live conversion is enabled (persisted). */
    protected final ValueProperty<Boolean> liveConversion;

    private final ScheduledExecutorService scheduler =
            Executors.newSingleThreadScheduledExecutor(r -> {
                Thread t = new Thread(r, "converter-worker");
                t.setDaemon(true);
                return t;
            });
    private ScheduledFuture<?> pendingConversion;

    protected Converter(ToolConfiguration config) {
        super(config);
        this.liveConversion = registerConfig("liveConversion", false);
    }

    // ---------------------------------------------------------------
    // UI construction
    // ---------------------------------------------------------------

    @Override
    protected void buildUi(JPanel panel) {
        var splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        splitPane.setResizeWeight(0.48);
        splitPane.setBorder(null);

        sourceEditor = createSourceEditor();
        splitPane.setTopComponent(sourceEditor);

        JPanel middle = buildActionBar();
        targetEditor = createTargetEditor();

        var rightPanel = new JPanel(new BorderLayout());
        rightPanel.add(middle, BorderLayout.NORTH);
        rightPanel.add(targetEditor, BorderLayout.CENTER);
        splitPane.setBottomComponent(rightPanel);

        panel.add(splitPane, BorderLayout.CENTER);
    }

    /** Create the source editor with a document listener for live mode. */
    protected TextEditor createSourceEditor() {
        var editor = new TextEditor(TextEditor.Mode.INPUT, sourceTextProperty());
        editor.getTextArea().getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            @Override public void insertUpdate(javax.swing.event.DocumentEvent e) { onSourceChanged(); }
            @Override public void removeUpdate(javax.swing.event.DocumentEvent e) { onSourceChanged(); }
            @Override public void changedUpdate(javax.swing.event.DocumentEvent e) { onSourceChanged(); }
        });
        return editor;
    }

    /** Create the target editor (read-only). */
    protected TextEditor createTargetEditor() {
        return new TextEditor(TextEditor.Mode.OUTPUT, targetTextProperty());
    }

    /**
     * Build the action bar between source and target.
     * Override to add tool-specific buttons.
     */
    protected JPanel buildActionBar() {
        var bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));

        var convertBtn = new JButton(getConvertButtonLabel());
        convertBtn.addActionListener(e -> convert());
        bar.add(convertBtn);

        var liveCheck = new JCheckBox("Live");
        liveCheck.setSelected(liveConversion.get());
        liveCheck.addActionListener(e -> liveConversion.set(liveCheck.isSelected()));
        bar.add(liveCheck);

        var copyBtn = new JButton("Copy Result");
        copyBtn.addActionListener(e -> targetEditor.copyToClipboard());
        bar.add(copyBtn);

        var swapBtn = new JButton("↑↓ Swap");
        swapBtn.addActionListener(e -> swap());
        bar.add(swapBtn);

        return bar;
    }

    /** Label for the primary action button. Default: "Convert ↓". */
    protected String getConvertButtonLabel() {
        return "Convert ↓";
    }

    // ---------------------------------------------------------------
    // Lifecycle
    // ---------------------------------------------------------------

    @Override
    public void activated() {
        liveConversion.addListener(p -> {
            if (Boolean.TRUE.equals(p.get())) convert();
        });
    }

    @Override
    public void dispose() {
        super.dispose();
        scheduler.shutdownNow();
    }

    // ---------------------------------------------------------------
    // Conversion engine
    // ---------------------------------------------------------------

    /** Called on every source text change; triggers live conversion if enabled. */
    protected void onSourceChanged() {
        if (Boolean.TRUE.equals(liveConversion.get())) scheduleConversion();
    }

    /** Debounce: cancel pending work and schedule a new conversion in 300ms. */
    private void scheduleConversion() {
        if (pendingConversion != null) pendingConversion.cancel(false);
        pendingConversion = scheduler.schedule(this::convert, 300, TimeUnit.MILLISECONDS);
    }

    /**
     * Run the forward conversion on a background thread and post the
     * result to the target editor on the EDT.
     */
    public void convert() {
        new SwingWorker<byte[], Void>() {
            @Override
            protected byte[] doInBackground() throws Exception {
                return doConvertForward(sourceEditor.getBytes());
            }
            @Override
            protected void done() {
                try {
                    targetEditor.setBytes(get());
                } catch (Exception e) {
                    targetEditor.setText("Error: " + e.getMessage());
                }
            }
        }.execute();
    }

    /** Swap source and target content. */
    protected void swap() {
        String sourceText = sourceEditor.getText();
        String targetText = targetEditor.getText();
        sourceEditor.setText(targetText);
        targetEditor.setText(sourceText);
    }

    // ---------------------------------------------------------------
    // Persistence
    // ---------------------------------------------------------------

    /** Property key for persisting source text. */
    protected ValueProperty<String> sourceTextProperty() {
        return registerInput("sourceText", "");
    }

    /** Property key for persisting target text. */
    protected ValueProperty<String> targetTextProperty() {
        return registerInput("targetText", "");
    }

    // ---------------------------------------------------------------
    // Subclass contract
    // ---------------------------------------------------------------

    /**
     * The actual conversion logic. Receives the raw bytes from the
     * source editor and returns the converted bytes to display in
     * the target editor.
     *
     * @param input source text as UTF-8 bytes
     * @return converted bytes (will be displayed as UTF-8 text)
     * @throws Exception on conversion failure (error message shown in target)
     */
    protected abstract byte[] doConvertForward(byte[] input) throws Exception;
}
