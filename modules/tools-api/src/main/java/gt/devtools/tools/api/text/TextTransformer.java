package gt.devtools.tools.api.text;

import gt.devtools.common.ValueProperty;
import gt.devtools.settings.ToolConfiguration;
import gt.devtools.tools.api.DeveloperTool;
import gt.devtools.tools.api.converter.TextEditor;

import javax.swing.*;
import java.awt.*;

/**
 * Base for single-input text transformation tools (hashing, sorting,
 * case conversion, filtering, etc.). Source on the left, result on
 * the right with action buttons.
 */
public abstract class TextTransformer extends DeveloperTool {

    protected TextEditor sourceEditor;
    protected TextEditor resultEditor;
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

        // -- source
        sourceEditor = new TextEditor(TextEditor.Mode.INPUT, sourceTextProperty());
        splitPane.setLeftComponent(sourceEditor);

        // -- right: actions + result
        var rightPanel = new JPanel(new BorderLayout());
        rightPanel.add(buildActionBar(), BorderLayout.NORTH);

        resultEditor = new TextEditor(TextEditor.Mode.OUTPUT, resultTextProperty());
        rightPanel.add(resultEditor, BorderLayout.CENTER);

        splitPane.setRightComponent(rightPanel);
        panel.add(splitPane, BorderLayout.CENTER);
    }

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

    protected abstract String doTransform(String input) throws Exception;

    protected ValueProperty<String> sourceTextProperty() {
        return registerInput("sourceText", "");
    }

    protected ValueProperty<String> resultTextProperty() {
        return registerInput("resultText", "");
    }
}
