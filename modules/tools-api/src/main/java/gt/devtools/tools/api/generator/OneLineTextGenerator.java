package gt.devtools.tools.api.generator;

import gt.devtools.common.ValueProperty;
import gt.devtools.settings.ToolConfiguration;
import gt.devtools.tools.api.DeveloperTool;
import gt.devtools.tools.api.converter.TextEditor;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;

/**
 * Base for generator tools that produce a single line of output
 * (UUID, NanoID, password, etc.). Provides a config area, a large
 * generated-value display, copy/regenerate actions, and optional
 * bulk generation.
 */
public abstract class OneLineTextGenerator extends DeveloperTool {

    protected JLabel generatedValueLabel;
    protected JButton regenerateButton;
    protected JButton copyButton;
    protected JLabel errorLabel;
    protected ValueProperty<Integer> bulkCount;
    protected TextEditor bulkOutput;

    protected OneLineTextGenerator(ToolConfiguration config) {
        super(config);
    }

    @Override
    protected void buildUi(JPanel panel) {
        panel.setLayout(new BorderLayout(0, 12));
        panel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        // -- top: config
        var configPanel = new JPanel();
        configPanel.setLayout(new BoxLayout(configPanel, BoxLayout.Y_AXIS));
        buildConfigurationUi(configPanel);
        panel.add(configPanel, BorderLayout.NORTH);

        // -- center: generated value
        var centerPanel = new JPanel(new BorderLayout(0, 8));
        centerPanel.setBorder(BorderFactory.createTitledBorder("Generated Value"));

        generatedValueLabel = new JLabel(" ", SwingConstants.CENTER);
        generatedValueLabel.setFont(new Font(Font.MONOSPACED, Font.BOLD, 16));
        generatedValueLabel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        centerPanel.add(generatedValueLabel, BorderLayout.CENTER);

        errorLabel = new JLabel(" ", SwingConstants.CENTER);
        errorLabel.setForeground(UIManager.getColor("TextField.inactiveForeground"));
        errorLabel.setFont(errorLabel.getFont().deriveFont(11f));
        centerPanel.add(errorLabel, BorderLayout.NORTH);

        var buttonBar = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 4));
        regenerateButton = new JButton("Regenerate");
        regenerateButton.addActionListener(e -> regenerate());
        buttonBar.add(regenerateButton);

        copyButton = new JButton("Copy");
        copyButton.addActionListener(e -> {
            var sel = new StringSelection(generatedValueLabel.getText());
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(sel, null);
        });
        buttonBar.add(copyButton);
        centerPanel.add(buttonBar, BorderLayout.SOUTH);

        panel.add(centerPanel, BorderLayout.CENTER);

        // -- bottom: bulk generation
        if (supportsBulkGeneration()) {
            var bulkPanel = new JPanel(new BorderLayout(0, 4));
            bulkPanel.setBorder(BorderFactory.createTitledBorder("Bulk Generation"));

            var bulkControl = new JPanel(new FlowLayout(FlowLayout.LEFT));
            bulkControl.add(new JLabel("Count:"));
            var countSpinner = new JSpinner(new SpinnerNumberModel(10, 1, 99999, 1));
            bulkCount = registerConfig("bulkCount", 10);
            countSpinner.setValue(bulkCount.get());
            countSpinner.addChangeListener(e -> bulkCount.set((Integer) countSpinner.getValue()));
            bulkControl.add(countSpinner);

            var bulkBtn = new JButton("Generate");
            bulkBtn.addActionListener(e -> generateBulk());
            bulkControl.add(bulkBtn);
            bulkPanel.add(bulkControl, BorderLayout.NORTH);

            bulkOutput = new TextEditor(TextEditor.Mode.OUTPUT);
            bulkPanel.add(bulkOutput, BorderLayout.CENTER);

            panel.add(bulkPanel, BorderLayout.SOUTH);
        }

        addAdditionalUi(panel);
    }

    @Override
    public void activated() {
        regenerate();
    }

    /**
     * Build the configuration controls above the generated value.
     */
    protected abstract void buildConfigurationUi(JPanel configPanel);

    /**
     * Generate a single value. Called on activation and when the user
     * clicks "Regenerate".
     */
    protected abstract String generate() throws Exception;

    /**
     * Whether bulk generation is supported.
     */
    protected boolean supportsBulkGeneration() {
        return true;
    }

    /**
     * Optional additional UI components.
     */
    protected void addAdditionalUi(JPanel panel) {}

    protected void regenerate() {
        try {
            String value = generate();
            generatedValueLabel.setText(value);
            errorLabel.setText(" ");
        } catch (Exception e) {
            errorLabel.setText("Error: " + e.getMessage());
            generatedValueLabel.setText("—");
        }
    }

    protected void generateBulk() {
        int count = bulkCount != null ? bulkCount.get() : 10;
        var sb = new StringBuilder();
        for (int i = 0; i < count; i++) {
            try {
                sb.append(generate()).append("\n");
            } catch (Exception e) {
                sb.append("ERROR: ").append(e.getMessage()).append("\n");
            }
        }
        bulkOutput.setText(sb.toString());
    }
}
