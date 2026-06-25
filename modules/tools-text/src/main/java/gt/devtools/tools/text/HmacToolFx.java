package gt.devtools.tools.text;

import gt.devtools.common.ValueProperty;
import gt.devtools.settings.ToolConfiguration;
import gt.devtools.tools.api.ToolPresentation;
import gt.devtools.tools.api.fx.TextTransformerFx;
import gt.devtools.tools.api.fx.ToolFxFactory;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

public final class HmacToolFx extends TextTransformerFx {
    private static final String[] ALGORITHMS = {"HmacSHA256", "HmacSHA384", "HmacSHA512", "HmacSHA1"};
    private final ValueProperty<String> algorithm;
    private final ValueProperty<String> secret;

    private HmacToolFx(ToolConfiguration config) {
        super(config); this.algorithm = registerConfig("hmacAlgorithm", "HmacSHA256"); this.secret = registerSensitive("hmacSecret", ""); }
    @Override protected void buildUi(BorderPane panel) {
        var bar = new HBox(8); bar.getChildren().add(new Label("Alg:"));
        var combo = new ComboBox<String>(); combo.getItems().addAll(ALGORITHMS); combo.setValue(algorithm.get());
        combo.setOnAction(e -> algorithm.set(combo.getValue())); bar.getChildren().add(combo);
        bar.getChildren().add(new Label("Secret:"));
        var f = new TextField(secret.get()); f.focusedProperty().addListener((o, w, foc) -> { if (!foc) secret.set(f.getText()); });
        bar.getChildren().add(f); panel.setTop(bar); super.buildUi(panel);
    }
    @Override protected String getTransformLabel() { return "HMAC"; }
    @Override protected String doTransform(String input) throws Exception { return hmac(input, algorithm.get(), secret.get()); }

    /** Computes an HMAC of the input and returns it as a hex string. */
    public static String hmac(String input, String algorithm, String secret) throws Exception {
        if (secret == null || secret.isEmpty()) {
            throw new IllegalStateException("Secret key is required");
        }
        var mac = Mac.getInstance(algorithm);
        mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), algorithm));
        byte[] digest = mac.doFinal(input.getBytes(StandardCharsets.UTF_8));
        var sb = new StringBuilder();
        for (byte b : digest) sb.append(String.format("%02x", b & 0xff));
        return sb.toString();
    }

    public static final class Factory implements ToolFxFactory<HmacToolFx> {
        public Factory() {} @Override public String getId() { return "hmac-transformer"; }
        @Override public ToolPresentation getPresentation() {
            return ToolPresentation.of("hmac-transformer", "HMAC", "HMAC Transformer").withGroupId("text"); }
        @Override public HmacToolFx create(ToolConfiguration c) { return new HmacToolFx(c); }
    }
}
