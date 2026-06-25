package gt.devtools.tools.text;

import gt.devtools.common.ValueProperty;
import gt.devtools.settings.ToolConfiguration;
import gt.devtools.tools.api.ToolPresentation;
import gt.devtools.tools.api.fx.TextTransformerFx;
import gt.devtools.tools.api.fx.ToolFxFactory;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

/** JavaFX version of Hashing tool. */
public final class HashingToolFx extends TextTransformerFx {

    private static final String[] ALGORITHMS = {"SHA-256", "SHA-512", "SHA-384", "SHA-1", "MD5"};
    private final ValueProperty<String> algorithm;

    private HashingToolFx(ToolConfiguration config) {
        super(config);
        this.algorithm = registerConfig("hashAlgorithm", "SHA-256");
    }

    @Override
    protected void buildUi(BorderPane panel) {
        var bar = new HBox(8);
        bar.getChildren().add(new Label("Algorithm:"));
        var combo = new ComboBox<String>();
        combo.getItems().addAll(ALGORITHMS);
        combo.setValue(algorithm.get());
        combo.setOnAction(e -> {
            algorithm.set(combo.getValue());
            if (Boolean.TRUE.equals(liveTransformation.get())) transform();
        });
        bar.getChildren().add(combo);
        panel.setTop(bar);
        super.buildUi(panel);
    }

    @Override protected String getTransformLabel() { return "Hash"; }

    @Override
    protected String doTransform(String input) throws Exception {
        return hash(input, algorithm.get());
    }

    /** Computes a cryptographic hash of the input and returns it as a hex string. */
    public static String hash(String input, String algorithm) throws Exception {
        var md = MessageDigest.getInstance(algorithm);
        byte[] digest = md.digest(input.getBytes(StandardCharsets.UTF_8));
        var sb = new StringBuilder();
        for (byte b : digest) sb.append(String.format("%02x", b & 0xff));
        return sb.toString();
    }

    public static final class Factory implements ToolFxFactory<HashingToolFx> {
        public Factory() {}
        @Override public String getId() { return "hashing-transformer"; }
        @Override public ToolPresentation getPresentation() {
            return ToolPresentation.of("hashing-transformer",
                    "Hashing", "Hashing Transformer")
                    .withGroupId("text");
        }
        @Override public HashingToolFx create(ToolConfiguration config) {
            return new HashingToolFx(config);
        }
    }
}
