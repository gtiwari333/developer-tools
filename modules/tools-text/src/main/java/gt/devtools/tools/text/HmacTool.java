package gt.devtools.tools.text;

import gt.devtools.common.ValueProperty;
import gt.devtools.settings.ToolConfiguration;
import gt.devtools.tools.api.ToolFactory;
import gt.devtools.tools.api.ToolPresentation;
import gt.devtools.tools.api.text.TextTransformer;

import javax.swing.*;
import java.awt.*;

/**
 * Computes HMAC (Hash-based Message Authentication Code) with
 * configurable algorithm and secret key.
 */
public final class HmacTool extends TextTransformer {

    private static final String[] ALGORITHMS = {
            "HmacSHA256", "HmacSHA384", "HmacSHA512", "HmacSHA1"
    };
    private final ValueProperty<String> algorithm;
    private final ValueProperty<String> secret;

    private HmacTool(ToolConfiguration config) {
        super(config);
        this.algorithm = registerConfig("hmacAlgorithm", "HmacSHA256");
        this.secret = registerSensitive("hmacSecret", "");
    }

    @Override
    protected void buildUi(JPanel panel) {
        var configBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        configBar.add(new JLabel("Algorithm:"));
        var combo = new JComboBox<>(ALGORITHMS);
        combo.setSelectedItem(algorithm.get());
        combo.addActionListener(e -> algorithm.set((String) combo.getSelectedItem()));
        configBar.add(combo);
        configBar.add(new JLabel("Secret:"));
        var secretField = new JTextField(secret.get(), 20);
        secretField.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override public void focusLost(java.awt.event.FocusEvent e) { secret.set(secretField.getText()); }
        });
        configBar.add(secretField);
        panel.add(configBar, BorderLayout.NORTH);
        super.buildUi(panel);
    }

    @Override
    protected String getTransformLabel() { return "HMAC"; }

    @Override
    protected String doTransform(String input) throws Exception {
        return hmac(input, algorithm.get(), secret.get());
    }

    // -- public static method (testable without Swing)

    /** Computes an HMAC of the input with the given algorithm and secret key. */
    public static String hmac(String input, String algorithm, String secret) throws Exception {
        if (secret == null || secret.isEmpty()) throw new IllegalStateException("Secret key is required");
        var mac = javax.crypto.Mac.getInstance(algorithm);
        mac.init(new javax.crypto.spec.SecretKeySpec(
                secret.getBytes(java.nio.charset.StandardCharsets.UTF_8), algorithm));
        byte[] result = mac.doFinal(input.getBytes(java.nio.charset.StandardCharsets.UTF_8));
        return java.util.HexFormat.of().formatHex(result);
    }

    public static final class Factory implements ToolFactory<HmacTool> {
        public Factory() {}
        @Override public String getId() { return "hmac-transformer"; }
        @Override public ToolPresentation getPresentation() {
            return ToolPresentation.of("hmac-transformer",
                    "HMAC", "HMAC Transformer").withGroupId("text");
        }
        @Override public HmacTool create(ToolConfiguration config) { return new HmacTool(config); }
    }
}
