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
 * Abstract base for converter tools: a vertical split with source editor,
 * action buttons, and target editor. Supports live (debounced) conversion.
 */
public abstract class Converter extends DeveloperTool {

    protected TextEditor sourceEditor;
    protected TextEditor targetEditor;
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

    @Override
    protected void buildUi(JPanel panel) {
        var splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        splitPane.setResizeWeight(0.48);
        splitPane.setBorder(null);

        // -- source side
        sourceEditor = createSourceEditor();
        splitPane.setTopComponent(sourceEditor);

        // -- middle: action bar
        JPanel middle = buildActionBar();
        // target editor
        targetEditor = createTargetEditor();

        var rightPanel = new JPanel(new BorderLayout());
        rightPanel.add(middle, BorderLayout.NORTH);
        rightPanel.add(targetEditor, BorderLayout.CENTER);
        splitPane.setBottomComponent(rightPanel);

        panel.add(splitPane, BorderLayout.CENTER);
    }

    protected TextEditor createSourceEditor() {
        var editor = new TextEditor(TextEditor.Mode.INPUT, sourceTextProperty());
        editor.getTextArea().getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            @Override public void insertUpdate(javax.swing.event.DocumentEvent e) { onSourceChanged(); }
            @Override public void removeUpdate(javax.swing.event.DocumentEvent e) { onSourceChanged(); }
            @Override public void changedUpdate(javax.swing.event.DocumentEvent e) { onSourceChanged(); }
        });
        return editor;
    }

    protected TextEditor createTargetEditor() {
        return new TextEditor(TextEditor.Mode.OUTPUT, targetTextProperty());
    }

    /**
     * Build the action button bar between source and target.
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

    protected String getConvertButtonLabel() {
        return "Convert ↓";
    }

    @Override
    public void activated() {
        liveConversion.addListener(p -> {
            if (Boolean.TRUE.equals(p.get())) {
                convert();
            }
        });
    }

    @Override
    public void dispose() {
        super.dispose();
        scheduler.shutdownNow();
    }

    protected void onSourceChanged() {
        if (Boolean.TRUE.equals(liveConversion.get())) {
            scheduleConversion();
        }
    }

    private void scheduleConversion() {
        if (pendingConversion != null) {
            pendingConversion.cancel(false);
        }
        pendingConversion = scheduler.schedule(this::convert, 300, TimeUnit.MILLISECONDS);
    }

    /**
     * Execute conversion synchronously on a background thread then update
     * the target editor on the EDT.
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
                    byte[] result = get();
                    targetEditor.setBytes(result);
                } catch (Exception e) {
                    targetEditor.setText("Error: " + e.getMessage());
                }
            }
        }.execute();
    }

    /**
     * Swap source and target content.
     */
    protected void swap() {
        String sourceText = sourceEditor.getText();
        String targetText = targetEditor.getText();
        sourceEditor.setText(targetText);
        targetEditor.setText(sourceText);
    }

    /**
     * The persisted property key for source text.
     */
    protected ValueProperty<String> sourceTextProperty() {
        return registerInput("sourceText", "");
    }

    /**
     * The persisted property key for target text.
     */
    protected ValueProperty<String> targetTextProperty() {
        return registerInput("targetText", "");
    }

    /**
     * Subclasses implement the actual forward conversion.
     */
    protected abstract byte[] doConvertForward(byte[] input) throws Exception;
}
