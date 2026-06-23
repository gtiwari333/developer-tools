package gt.devtools.tools.api.text;

import gt.devtools.common.ValueProperty;
import gt.devtools.settings.ToolConfiguration;
import gt.devtools.tools.api.DeveloperTool;
import gt.devtools.tools.api.converter.TextEditor;

import javax.swing.*;
import java.awt.*;

/**
 * Base class for one-way text transformation tools — source text on the
 * left, result on the right, action buttons in between.
 *
 * <h3>Layout</h3>
 * <pre>
 * ┌──────────────────┬──────────────────┐
 * │  Source editor   │ [Hash →] [Live]  │
 * │  (INPUT)         │ [Copy Result]    │
 * │                  │ ───────────────  │
 * │                  │  Result editor   │
 * │                  │  (OUTPUT)        │
 * └──────────────────┴──────────────────┘
 * </pre>
 *
 * <h3>Use this for</h3>
 * <ul>
 *   <li>Hashing (SHA, MD5)</li>
 *   <li>HMAC generation</li>
 *   <li>Text sorting / filtering / case conversion</li>
 *   <li>SQL / code formatting</li>
 *   <li>Regex matching / substitution</li>
 * </ul>
 *
 * <h3>Subclassing</h3>
 * Implement {@link #doTransform(String)} and {@link #getTransformLabel()}.
 * The source and result text are persisted automatically.
 */
public abstract class TextTransformer extends DeveloperTool {

    /** The source (input) editor. */
    protected TextEditor sourceEditor;

    /** The result (output) editor — read-only. */
    protected TextEditor resultEditor;

    /** Whether live transformation is enabled (persisted). */
    protected final ValueProperty<Boolean> liveTransformation;

    protected TextTransformer(ToolConfiguration config) {
        super(config);
        this.liveTransformation = registerConfig("liveTransformation", false);
    }

    @Override
    protected void buildUi(JPanel panel) {
        var splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setResizeWeight(0.50);
        splitPane.setBorder(null);

        sourceEditor = new TextEditor(TextEditor.Mode.INPUT, sourceTextProperty());
        splitPane.setLeftComponent(sourceEditor);

        var rightPanel = new JPanel(new BorderLayout());
        rightPanel.add(buildActionBar(), BorderLayout.NORTH);
        resultEditor = new TextEditor(TextEditor.Mode.OUTPUT, resultTextProperty());
        rightPanel.add(resultEditor, BorderLayout.CENTER);

        splitPane.setRightComponent(rightPanel);
        panel.add(splitPane, BorderLayout.CENTER);
    }

    /** Build the action bar between source and result. */
    protected JPanel buildActionBar() {
        var bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));

        var transformBtn = new JButton(getTransformLabel() + " →");
        transformBtn.addActionListener(e -> transform());
        bar.add(transformBtn);

        var liveCheck = new JCheckBox("Live");
        liveCheck.setSelected(liveTransformation.get());
        liveCheck.addActionListener(e -> liveTransformation.set(liveCheck.isSelected()));
        bar.add(liveCheck);

        var copyBtn = new JButton("Copy Result");
        copyBtn.addActionListener(e -> resultEditor.copyToClipboard());
        bar.add(copyBtn);

        return bar;
    }

    /** Label for the action button (e.g. "Hash", "Sort", "Format"). */
    protected abstract String getTransformLabel();

    @Override
    public void activated() {
        liveTransformation.addListener(p -> {
            if (Boolean.TRUE.equals(p.get())) transform();
        });
        sourceEditor.getTextArea().getDocument().addDocumentListener(
            new javax.swing.event.DocumentListener() {
                @Override public void insertUpdate(javax.swing.event.DocumentEvent e) {
                    if (Boolean.TRUE.equals(liveTransformation.get())) transform();
                }
                @Override public void removeUpdate(javax.swing.event.DocumentEvent e) {
                    if (Boolean.TRUE.equals(liveTransformation.get())) transform();
                }
                @Override public void changedUpdate(javax.swing.event.DocumentEvent e) {
                    if (Boolean.TRUE.equals(liveTransformation.get())) transform();
                }
            });
    }

    /**
     * Execute the transformation on a background thread and display
     * the result.
     */
    public void transform() {
        new SwingWorker<String, Void>() {
            @Override
            protected String doInBackground() throws Exception {
                return doTransform(sourceEditor.getText());
            }
            @Override
            protected void done() {
                try {
                    resultEditor.setText(get());
                } catch (Exception e) {
                    resultEditor.setText("Error: " + e.getMessage());
                }
            }
        }.execute();
    }

    /**
     * The transformation logic. Receives the raw source text and returns
     * the transformed result.
     *
     * @param input the source text
     * @return the transformed text
     * @throws Exception on transformation failure
     */
    protected abstract String doTransform(String input) throws Exception;

    // Persistence
    protected ValueProperty<String> sourceTextProperty() { return registerInput("sourceText", ""); }
    protected ValueProperty<String> resultTextProperty() { return registerInput("resultText", ""); }
}
