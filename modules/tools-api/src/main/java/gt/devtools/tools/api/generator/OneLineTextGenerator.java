package gt.devtools.tools.api.generator;

import gt.devtools.common.ValueProperty;
import gt.devtools.settings.ToolConfiguration;
import gt.devtools.tools.api.DeveloperTool;
import gt.devtools.tools.api.converter.TextEditor;

import javax.swing.*;
import java.awt.*;

/**
 * Base class for generator tools that produce text output (UUID, NanoID,
 * password, lorem ipsum, etc.). Both the single-value and bulk-generation
 * panels use the same {@link TextEditor} component for a uniform look
 * with line numbers, syntax highlighting, and a status bar.
 *
 * <h3>Layout</h3>
 * <pre>
 * ┌──────────────────────────┐
 * │  Configuration controls  │
 * ├──────────────────────────┤
 * │  Generated Value         │
 * │  [Regenerate] [Copy]     │
 * │  ┌────────────────────┐  │
 * │  │ (TextEditor)       │  │  ← line numbers, status bar
 * │  └────────────────────┘  │
 * ├────── divider ───────────┤
 * │  Bulk Generation         │
 * │  Count: [10] [Generate] [Copy] │
 * │  ┌────────────────────┐  │
 * │  │ (TextEditor)       │  │  ← same component style
 * │  └────────────────────┘  │
 * └──────────────────────────┘
 * </pre>
 */
public abstract class OneLineTextGenerator extends DeveloperTool {

    /** Single-value output editor (read-only, with line numbers and status bar). */
    protected TextEditor valueOutput;

    /** Bulk output editor (read-only). */
    protected TextEditor bulkOutput;

    /** Triggers a call to {@link #generate()} and updates the display. */
    protected JButton regenerateButton;

    /** Shows error text when {@link #generate()} throws. */
    protected JLabel errorLabel;

    /** Persisted bulk count value. */
    protected ValueProperty<Integer> bulkCount;

    /** Whether bulk generation is shown. */
    protected boolean bulkPanelVisible;

    protected OneLineTextGenerator(ToolConfiguration config) {
        super(config);
    }

    @Override
    protected void buildUi(JPanel panel) {
        panel.setLayout(new BorderLayout(0, 0));
        panel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));

        // --- top section: config + single-value output ---
        var topSection = new JPanel(new BorderLayout(0, 4));
        topSection.setBorder(BorderFactory.createEmptyBorder(8, 8, 4, 8));

        var configPanel = new JPanel();
        configPanel.setLayout(new BoxLayout(configPanel, BoxLayout.Y_AXIS));
        buildConfigurationUi(configPanel);
        topSection.add(configPanel, BorderLayout.NORTH);

        var valuePanel = new JPanel(new BorderLayout(0, 4));
        valuePanel.setBorder(BorderFactory.createTitledBorder("Generated Value"));

        // Button bar
        var valueButtonBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 2));
        regenerateButton = new JButton("Regenerate");
        regenerateButton.addActionListener(e -> regenerate());
        valueButtonBar.add(regenerateButton);

        var copyValueBtn = new JButton("Copy");
        copyValueBtn.addActionListener(e -> valueOutput.copyToClipboard());
        valueButtonBar.add(copyValueBtn);

        // Error label
        errorLabel = new JLabel(" ");
        errorLabel.setForeground(UIManager.getColor("TextField.inactiveForeground"));
        errorLabel.setFont(errorLabel.getFont().deriveFont(11f));
        valueButtonBar.add(errorLabel);

        valuePanel.add(valueButtonBar, BorderLayout.NORTH);

        // Output editor (read-only, with line numbers and status bar)
        valueOutput = new TextEditor(TextEditor.Mode.OUTPUT);
        valueOutput.setSyntaxStyle("text/plain");
        valuePanel.add(valueOutput, BorderLayout.CENTER);

        topSection.add(valuePanel, BorderLayout.CENTER);

        // --- bottom section: bulk generation ---
        bulkPanelVisible = supportsBulkGeneration();
        if (bulkPanelVisible) {
            var bulkPanel = buildBulkPanel();

            var splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, topSection, bulkPanel);
            splitPane.setResizeWeight(0.50);
            splitPane.setBorder(null);
            splitPane.setDividerSize(5);
            panel.add(splitPane, BorderLayout.CENTER);
        } else {
            panel.add(topSection, BorderLayout.CENTER);
        }

        addAdditionalUi(panel);
    }

    private JPanel buildBulkPanel() {
        var bulkPanel = new JPanel(new BorderLayout(0, 4));
        bulkPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(0, 8, 8, 8),
                BorderFactory.createTitledBorder("Bulk Generation")));

        // Button bar (same style as value panel)
        var bulkButtonBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 2));
        bulkButtonBar.add(new JLabel("Count:"));
        var countSpinner = new JSpinner(new javax.swing.SpinnerNumberModel(10, 1, 99999, 1));
        bulkCount = registerConfig("bulkCount", 10);
        countSpinner.setValue(bulkCount.get());
        countSpinner.addChangeListener(e -> bulkCount.set((Integer) countSpinner.getValue()));
        bulkButtonBar.add(countSpinner);

        var bulkBtn = new JButton("Generate");
        bulkBtn.addActionListener(e -> generateBulk());
        bulkButtonBar.add(bulkBtn);

        var copyBulkBtn = new JButton("Copy");
        copyBulkBtn.addActionListener(e -> bulkOutput.copyToClipboard());
        bulkButtonBar.add(copyBulkBtn);

        var clearBulkBtn = new JButton("Clear");
        clearBulkBtn.addActionListener(e -> bulkOutput.setText(""));
        bulkButtonBar.add(clearBulkBtn);

        bulkPanel.add(bulkButtonBar, BorderLayout.NORTH);

        // Output editor (same component as value panel)
        bulkOutput = new TextEditor(TextEditor.Mode.OUTPUT);
        bulkOutput.setSyntaxStyle("text/plain");
        bulkPanel.add(bulkOutput, BorderLayout.CENTER);

        return bulkPanel;
    }

    @Override
    public void activated() {
        regenerate();
    }

    // ---------------------------------------------------------------
    // Subclass contract
    // ---------------------------------------------------------------

    protected abstract void buildConfigurationUi(JPanel configPanel);

    protected abstract String generate() throws Exception;

    protected boolean supportsBulkGeneration() { return true; }

    protected void addAdditionalUi(JPanel panel) {}

    // ---------------------------------------------------------------
    // Actions
    // ---------------------------------------------------------------

    protected void regenerate() {
        try {
            String value = generate();
            valueOutput.setText(value);
            errorLabel.setText(" ");
        } catch (Exception e) {
            errorLabel.setText("Error: " + e.getMessage());
            valueOutput.setText("—");
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
        if (bulkOutput != null) {
            bulkOutput.setText(sb.toString());
        }
    }
}
