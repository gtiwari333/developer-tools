package gt.devtools.tools.standalone;

import gt.devtools.common.ValueProperty;
import gt.devtools.settings.ToolConfiguration;
import gt.devtools.tools.api.DeveloperTool;
import gt.devtools.tools.api.ToolFactory;
import gt.devtools.tools.api.ToolPresentation;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.StringSelection;

/**
 * Color picker with hex, RGB, and HSL display. Click the color swatch
 * to open a chooser dialog. Values update in real time.
 */
public final class ColorPickerTool extends DeveloperTool {

    private JPanel colorPreview;
    private JTextField hexField;
    private JLabel rgbLabel, hslLabel;
    private Color currentColor = Color.BLACK;
    private final ValueProperty<String> hexValue;

    private ColorPickerTool(ToolConfiguration config) {
        super(config);
        this.hexValue = registerConfig("colorHex", "#000000");
    }

    @Override
    protected void buildUi(JPanel panel) {
        panel.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        // Color preview swatch
        colorPreview = new JPanel();
        colorPreview.setPreferredSize(new Dimension(200, 120));
        colorPreview.setMaximumSize(new Dimension(Integer.MAX_VALUE, 120));
        colorPreview.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));
        colorPreview.setBackground(currentColor);
        colorPreview.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        colorPreview.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseClicked(java.awt.event.MouseEvent e) { openChooser(); }
        });
        panel.add(colorPreview);
        panel.add(Box.createVerticalStrut(12));

        // Hex input
        var hexPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        hexPanel.add(new JLabel("Hex:"));
        hexField = new JTextField(hexValue.get(), 10);
        hexField.addActionListener(e -> updateFromHex(hexField.getText()));
        hexPanel.add(hexField);
        var copyBtn = new JButton("Copy");
        copyBtn.addActionListener(e -> Toolkit.getDefaultToolkit().getSystemClipboard()
                .setContents(new StringSelection(hexField.getText()), null));
        hexPanel.add(copyBtn);
        panel.add(hexPanel);

        // RGB display
        rgbLabel = new JLabel();
        rgbLabel.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        panel.add(rgbLabel);

        // HSL display
        hslLabel = new JLabel();
        hslLabel.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        panel.add(hslLabel);

        // Restore persisted color
        String saved = hexValue.get();
        if (saved != null && !saved.isEmpty()) updateFromHex(saved);
    }

    private void openChooser() {
        Color chosen = JColorChooser.showDialog(component, "Pick a Color", currentColor);
        if (chosen != null) setColor(chosen);
    }

    private void setColor(Color c) {
        currentColor = c;
        colorPreview.setBackground(c);
        String hex = String.format("#%02x%02x%02x", c.getRed(), c.getGreen(), c.getBlue());
        hexField.setText(hex);
        hexValue.set(hex);
        rgbLabel.setText(String.format("RGB: %d, %d, %d", c.getRed(), c.getGreen(), c.getBlue()));
        float[] hsb = Color.RGBtoHSB(c.getRed(), c.getGreen(), c.getBlue(), null);
        hslLabel.setText(String.format("HSL: %.0f°, %.0f%%, %.0f%%",
                hsb[0] * 360, hsb[1] * 100, hsb[2] * 100));
    }

    private void updateFromHex(String hex) {
        try {
            if (!hex.startsWith("#")) hex = "#" + hex;
            Color c = Color.decode(hex);
            setColor(c);
        } catch (Exception ignored) {
            // Invalid hex, ignore
        }
    }

    public static final class Factory implements ToolFactory<ColorPickerTool> {
        public Factory() {}
        @Override public String getId() { return "color-picker"; }
        @Override public ToolPresentation getPresentation() {
            return ToolPresentation.of("color-picker", "Color Picker", "Color Picker")
                    .withGroupId("creativity")
                    .withDescription("Pick a color, see its hex/RGB/HSL values, copy to clipboard.");
        }
        @Override public ColorPickerTool create(ToolConfiguration config) { return new ColorPickerTool(config); }
    }
}
