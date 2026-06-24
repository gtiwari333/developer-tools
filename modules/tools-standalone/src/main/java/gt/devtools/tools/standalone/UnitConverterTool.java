package gt.devtools.tools.standalone;

import gt.devtools.common.ValueProperty;
import gt.devtools.settings.ToolConfiguration;
import gt.devtools.tools.api.DeveloperTool;
import gt.devtools.tools.api.ToolFactory;
import gt.devtools.tools.api.ToolPresentation;

import javax.swing.*;
import java.awt.*;
import java.util.Map;

/** Converts between data size units, number bases, and time units. */
public final class UnitConverterTool extends DeveloperTool {

    private static final String[][] UNIT_GROUPS = {
            {"bytes", "KB", "MB", "GB", "TB", "PB"},
            {"bits", "Kb", "Mb", "Gb", "Tb"},
    };
    private final ValueProperty<String> category;
    private JTextField inputField;
    private JPanel resultsPanel;

    private UnitConverterTool(ToolConfiguration config) {
        super(config);
        this.category = registerConfig("unitCategory", "Data Size (1024)");
    }

    @Override
    protected void buildUi(JPanel panel) {
        panel.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        panel.setLayout(new BorderLayout(0, 12));

        var configPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        configPanel.add(new JLabel("Category:"));
        var catCombo = new JComboBox<>(new String[]{
                "Data Size (1024)", "Data Size (1000)", "Number Base"
        });
        catCombo.setSelectedItem(category.get());
        catCombo.addActionListener(e -> { category.set((String) catCombo.getSelectedItem()); convert(); });
        configPanel.add(catCombo);
        configPanel.add(new JLabel("Value:"));
        inputField = new JTextField("1", 12);
        inputField.addActionListener(e -> convert());
        configPanel.add(inputField);
        var convertBtn = new JButton("Convert");
        convertBtn.addActionListener(e -> convert());
        configPanel.add(convertBtn);
        panel.add(configPanel, BorderLayout.NORTH);

        resultsPanel = new JPanel();
        resultsPanel.setLayout(new BoxLayout(resultsPanel, BoxLayout.Y_AXIS));
        panel.add(new JScrollPane(resultsPanel), BorderLayout.CENTER);

        convert();
    }

    private void convert() {
        resultsPanel.removeAll();
        try {
            String cat = category.get();
            double value = Double.parseDouble(inputField.getText().strip());

            if (cat.contains("Number Base")) {
                showNumberBases((long) value);
            } else {
                int base = cat.contains("1000") ? 1000 : 1024;
                for (var entry : convertDataSize(value, base).entrySet()) {
                    addResult(entry.getKey(), entry.getValue());
                }
            }
        } catch (NumberFormatException e) {
            addResult("Error", 0);
        }
        resultsPanel.revalidate();
        resultsPanel.repaint();
    }

    private void showNumberBases(long n) {
        var mono = new Font(Font.MONOSPACED, Font.PLAIN, 13);
        for (String line : convertNumberBases(n)) {
            addLabel(line, mono);
        }
    }

    // -- public static methods (testable without Swing)

    /** Converts a byte value to all data-size units using the given base (1024 or 1000). */
    public static java.util.Map<String, Double> convertDataSize(double bytes, int base) {
        java.util.Map<String, Double> result = new java.util.LinkedHashMap<>();
        String[] units = {"bytes", "KB", "MB", "GB", "TB", "PB"};
        for (int i = 0; i < units.length; i++) {
            result.put(units[i], bytes / Math.pow(base, i));
        }
        return result;
    }

    /** Converts a number to binary, octal, decimal, and hex representations. */
    public static java.util.List<String> convertNumberBases(long n) {
        return java.util.List.of(
                "Binary:     " + Long.toBinaryString(n),
                "Octal:      " + Long.toOctalString(n),
                "Decimal:    " + n,
                "Hex:        " + Long.toHexString(n).toUpperCase(),
                "Hex (0x):   0x" + Long.toHexString(n).toUpperCase()
        );
    }

    private void addResult(String unit, double value) {
        addLabel(String.format("%,.2f %s", value, unit),
                new Font(Font.MONOSPACED, Font.PLAIN, 14));
    }

    private void addLabel(String text, Font font) {
        var label = new JLabel(text);
        label.setFont(font);
        label.setBorder(BorderFactory.createEmptyBorder(2, 8, 2, 8));
        resultsPanel.add(label);
    }

    public static final class Factory implements ToolFactory<UnitConverterTool> {
        public Factory() {}
        @Override public String getId() { return "units-converter"; }
        @Override public ToolPresentation getPresentation() {
            return ToolPresentation.of("units-converter", "Unit Converter", "Unit Converter")
                    .withGroupId("formatters");
        }
        @Override public UnitConverterTool create(ToolConfiguration config) { return new UnitConverterTool(config); }
    }
}
