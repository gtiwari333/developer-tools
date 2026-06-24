package gt.devtools.tools.standalone;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import gt.devtools.common.ValueProperty;
import gt.devtools.settings.ToolConfiguration;
import gt.devtools.tools.api.DeveloperTool;
import gt.devtools.tools.api.ToolFactory;
import gt.devtools.tools.api.ToolPresentation;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

/** Generates QR codes from text input. */
public final class QrCodeGeneratorTool extends DeveloperTool {

    private JTextArea inputArea;
    private JLabel imageLabel;
    private JScrollPane imageScroll;
    private final ValueProperty<String> text;

    private QrCodeGeneratorTool(ToolConfiguration config) {
        super(config);
        this.text = registerConfig("qrText", "");
    }

    @Override
    protected void buildUi(JPanel panel) {
        panel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        var split = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        split.setResizeWeight(0.35);
        split.setBorder(null);

        // Input
        var inputPanel = new JPanel(new BorderLayout(0, 4));
        inputPanel.setBorder(BorderFactory.createTitledBorder("Input Text"));
        inputArea = new JTextArea(text.get(), 4, 40);
        inputArea.setLineWrap(true);
        inputArea.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            @Override public void insertUpdate(javax.swing.event.DocumentEvent e) { regenerate(); }
            @Override public void removeUpdate(javax.swing.event.DocumentEvent e) { regenerate(); }
            @Override public void changedUpdate(javax.swing.event.DocumentEvent e) { regenerate(); }
        });
        inputPanel.add(new JScrollPane(inputArea), BorderLayout.CENTER);
        split.setTopComponent(inputPanel);

        // Output
        var outputPanel = new JPanel(new BorderLayout());
        outputPanel.setBorder(BorderFactory.createTitledBorder("QR Code"));
        imageLabel = new JLabel("", SwingConstants.CENTER);
        imageScroll = new JScrollPane(imageLabel);
        outputPanel.add(imageScroll, BorderLayout.CENTER);
        split.setBottomComponent(outputPanel);

        panel.add(split, BorderLayout.CENTER);
        regenerate();
    }

    private void regenerate() {
        String t = inputArea.getText().strip();
        text.set(t);
        if (t.isEmpty()) { imageLabel.setIcon(null); imageLabel.setText("Enter text to generate QR code."); return; }
        try {
            QRCodeWriter writer = new QRCodeWriter();
            BitMatrix matrix = writer.encode(t, BarcodeFormat.QR_CODE, 300, 300);
            BufferedImage img = MatrixToImageWriter.toBufferedImage(matrix);
            imageLabel.setIcon(new ImageIcon(img));
            imageLabel.setText("");
        } catch (Exception e) {
            imageLabel.setIcon(null);
            imageLabel.setText("Error: " + e.getMessage());
        }
    }

    public static final class Factory implements ToolFactory<QrCodeGeneratorTool> {
        public Factory() {}
        @Override public String getId() { return "qr-code-generator"; }
        @Override public ToolPresentation getPresentation() {
            return ToolPresentation.of("qr-code-generator", "QR Code Generator", "QR Code Generator")
                    .withGroupId("creativity");
        }
        @Override public QrCodeGeneratorTool create(ToolConfiguration config) { return new QrCodeGeneratorTool(config); }
    }
}
