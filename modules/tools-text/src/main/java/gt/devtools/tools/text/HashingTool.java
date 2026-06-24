package gt.devtools.tools.text;

import gt.devtools.common.ValueProperty;
import gt.devtools.settings.ToolConfiguration;
import gt.devtools.tools.api.ToolFactory;
import gt.devtools.tools.api.ToolPresentation;
import gt.devtools.tools.api.text.TextTransformer;

import javax.swing.*;
import java.awt.*;

/**
 * Computes cryptographic hashes (MD5, SHA-1, SHA-256, SHA-384, SHA-512).
 */
public final class HashingTool extends TextTransformer {

    private static final String[] ALGORITHMS = {
            "SHA-256", "SHA-512", "SHA-384", "SHA-1", "MD5"
    };
    private final ValueProperty<String> algorithm;

    private HashingTool(ToolConfiguration config) {
        super(config);
        this.algorithm = registerConfig("hashAlgorithm", "SHA-256");
    }

    @Override
    protected void buildUi(JPanel panel) {
        var configBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        configBar.add(new JLabel("Algorithm:"));
        var combo = new JComboBox<>(ALGORITHMS);
        combo.setSelectedItem(algorithm.get());
        combo.addActionListener(e -> {
            algorithm.set((String) combo.getSelectedItem());
            if (Boolean.TRUE.equals(liveTransformation.get())) transform();
        });
        configBar.add(combo);
        panel.add(configBar, BorderLayout.NORTH);
        super.buildUi(panel);
    }

    @Override
    protected String getTransformLabel() { return "Hash"; }

    @Override
    protected String doTransform(String input) throws Exception {
        return hash(input, algorithm.get());
    }

    // -- public static method (testable without Swing)

    /** Computes a cryptographic hash of the input and returns it as a hex string. */
    public static String hash(String input, String algorithm) throws Exception {
        var md = java.security.MessageDigest.getInstance(algorithm);
        byte[] digest = md.digest(input.getBytes(java.nio.charset.StandardCharsets.UTF_8));
        var sb = new StringBuilder();
        for (byte b : digest) sb.append(String.format("%02x", b & 0xff));
        return sb.toString();
    }

    public static final class Factory implements ToolFactory<HashingTool> {
        public Factory() {}
        @Override public String getId() { return "hashing-transformer"; }
        @Override public ToolPresentation getPresentation() {
            return ToolPresentation.of("hashing-transformer",
                    "Hashing", "Hashing Transformer").withGroupId("text");
        }
        @Override public HashingTool create(ToolConfiguration config) { return new HashingTool(config); }
    }
}
