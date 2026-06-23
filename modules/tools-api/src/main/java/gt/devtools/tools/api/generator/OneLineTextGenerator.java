package gt.devtools.tools.api.generator;

import gt.devtools.common.ValueProperty;
import gt.devtools.settings.ToolConfiguration;
import gt.devtools.tools.api.DeveloperTool;
import gt.devtools.tools.api.converter.TextEditor;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.StringSelection;

/**
 * Base class for generator tools that produce a single line of output
 * (UUID, NanoID, password, lorem ipsum, hash, HMAC, etc.).
 *
 * <h3>Layout</h3>
 * <pre>
 * ┌──────────────────────────┐
 * │  Configuration controls  │  ← buildConfigurationUi()
 * ├──────────────────────────┤
 * │    abc123-def456-...     │  ← generated value (monospaced bold)
 * │  [Regenerate]  [Copy]    │
 * ├──────────────────────────┤
 * │  Bulk Generation         │  ← optional, collapsible
 * │  Count: [10] [Generate]  │
 * │  ┌──────────────────┐   │
 * │  │ value1           │   │
 * │  │ value2           │   │
 * │  └──────────────────┘   │
 * └──────────────────────────┘
 * </pre>
 *
 * <h3>Subclassing</h3>
 * <ol>
 *   <li>Implement {@link #buildConfigurationUi(JPanel)} to add controls
 *       (combo boxes, spinners, checkboxes) above the generated value.</li>
 *   <li>Implement {@link #generate()} to return a single generated value.</li>
 *   <li>Optionally override {@link #supportsBulkGeneration()} to disable
 *       the bulk panel.</li>
 * </ol>
 *
 * <h3>Persistence</h3>
 * Configuration properties registered via {@link #registerConfig} are
 * persisted automatically. The generated value itself is not persisted
 * (it is regenerated on activation).
 */
public abstract class OneLineTextGenerator extends DeveloperTool {

    /** Displays the generated value in large monospaced bold text. */
    protected JLabel generatedValueLabel;

    /** Triggers a call to {@link #generate()} and updates the display. */
    protected JButton regenerateButton;

    /** Copies the generated value to the system clipboard. */
    protected JButton copyButton;

    /** Shows error text when {@link #generate()} throws. */
    protected JLabel errorLabel;

    /** Persisted bulk count value. */
    protected ValueProperty<Integer> bulkCount;

    /** Output area for bulk generation results. */
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

    /** Generate a new value when the tab is selected. */
    @Override
    public void activated() {
        regenerate();
    }

    // ---------------------------------------------------------------
    // Subclass contract
    // ---------------------------------------------------------------

    /**
     * Build the configuration controls (combo boxes, spinners, checkboxes)
     * that appear above the generated value. Add components to
     * {@code configPanel} which has a vertical {@link BoxLayout}.
     */
    protected abstract void buildConfigurationUi(JPanel configPanel);

    /**
     * Generate a single value. Called when the tab is activated and
     * when the user clicks "Regenerate".
     *
     * @return the generated value as a string
     * @throws Exception if generation fails (error shown in the UI)
     */
    protected abstract String generate() throws Exception;

    /**
     * Whether to show the bulk generation panel. Default is {@code true}.
     * Override and return {@code false} for tools where bulk generation
     * doesn't make sense.
     */
    protected boolean supportsBulkGeneration() { return true; }

    /**
     * Hook for adding extra UI elements beyond the standard layout.
     * {@code panel} has a {@link BorderLayout} — add to any position.
     */
    protected void addAdditionalUi(JPanel panel) {}

    // ---------------------------------------------------------------
    // Actions
    // ---------------------------------------------------------------

    /** Call {@link #generate()} and update the display. */
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

    /** Generate {@code bulkCount} values and write them to the bulk editor. */
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
