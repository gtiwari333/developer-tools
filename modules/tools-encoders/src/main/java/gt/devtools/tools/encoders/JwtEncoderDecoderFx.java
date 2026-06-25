package gt.devtools.tools.encoders;

import gt.devtools.common.ValueProperty;
import gt.devtools.settings.ToolConfiguration;
import gt.devtools.tools.api.ToolPresentation;
import gt.devtools.tools.api.fx.EncoderDecoderFx;
import gt.devtools.tools.api.fx.ToolFxFactory;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import org.jose4j.jws.AlgorithmIdentifiers;
import org.jose4j.jws.JsonWebSignature;
import org.jose4j.jwt.JwtClaims;
import org.jose4j.jwt.consumer.JwtConsumerBuilder;
import org.jose4j.keys.HmacKey;

import java.nio.charset.StandardCharsets;

/**
 * JavaFX version of JWT (JSON Web Token) encode / decode tool.
 * <p>
 * Supports HMAC-based algorithms (HS256, HS384, HS512). The user provides
 * the secret key in the configuration panel. Encoding: header+payload → JWT.
 * Decoding: JWT → header+payload JSON (with signature verification).
 */
public final class JwtEncoderDecoderFx extends EncoderDecoderFx {

    private static final String[] ALGORITHMS = {
            AlgorithmIdentifiers.HMAC_SHA256,
            AlgorithmIdentifiers.HMAC_SHA384,
            AlgorithmIdentifiers.HMAC_SHA512
    };

    private final ValueProperty<String> secret;
    private final ValueProperty<String> algorithm;

    private JwtEncoderDecoderFx(ToolConfiguration config) {
        super(config);
        this.algorithm = registerConfig("jwtAlgorithm", ALGORITHMS[0]);
        this.secret = registerSensitive("jwtSecret", "");
    }

    @Override
    protected void buildUi(BorderPane panel) {
        // Config row at top
        var configBar = new HBox(8);
        configBar.setStyle("-fx-padding: 4 0;");
        configBar.getChildren().add(new Label("Algorithm:"));

        var algoCombo = new ComboBox<String>();
        algoCombo.getItems().addAll(ALGORITHMS);
        algoCombo.setValue(algorithm.get());
        algoCombo.setOnAction(e -> algorithm.set(algoCombo.getValue()));
        configBar.getChildren().add(algoCombo);

        configBar.getChildren().add(new Label("Secret:"));
        var secretField = new TextField(secret.get());
        secretField.setPrefColumnCount(25);
        secretField.textProperty().addListener((obs, old, val) -> secret.set(val));
        configBar.getChildren().add(secretField);

        panel.setTop(configBar);

        // Standard converter layout below
        super.buildUi(panel);
    }

    @Override
    protected void afterBuildUi() {
        // Show JSON syntax highlighting for JWT payload
        sourceEditor.setSyntaxStyle("json");
        targetEditor.setSyntaxStyle("text/plain");
    }

    @Override
    protected byte[] doConvertForward(byte[] input) throws Exception {
        String payload = new String(input, StandardCharsets.UTF_8);
        String key = secret.get();
        if (key == null || key.isEmpty()) {
            throw new IllegalStateException("Secret key is required for JWT signing");
        }

        JwtClaims claims = JwtClaims.parse(payload);
        JsonWebSignature jws = new JsonWebSignature();
        jws.setPayload(claims.toJson());
        jws.setAlgorithmHeaderValue(algorithm.get());
        jws.setKey(new HmacKey(key.getBytes(StandardCharsets.UTF_8)));
        return jws.getCompactSerialization().getBytes(StandardCharsets.UTF_8);
    }

    @Override
    protected byte[] doConvertBackward(byte[] input) throws Exception {
        String token = new String(input, StandardCharsets.UTF_8).trim();
        String key = secret.get();
        if (key == null || key.isEmpty()) {
            throw new IllegalStateException("Secret key is required for JWT verification");
        }

        var consumer = new JwtConsumerBuilder()
                .setVerificationKey(new HmacKey(key.getBytes(StandardCharsets.UTF_8)))
                .setRelaxVerificationKeyValidation()
                .build();

        var claims = consumer.processToClaims(token);
        return claims.toJson().getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public void reset() {
        secret.reset();
        algorithm.reset();
    }

    public static final class Factory implements ToolFxFactory<JwtEncoderDecoderFx> {
        public Factory() {}
        @Override public String getId() { return "jwt-encoder-decoder"; }
        @Override
        public ToolPresentation getPresentation() {
            return ToolPresentation.of("jwt-encoder-decoder",
                    "JWT Encoder / Decoder",
                    "JWT Encoder / Decoder")
                    .withGroupId("encoders")
                    .withDescription("Encode/decode JSON Web Tokens with HMAC algorithms. " +
                            "Enter the JWT claims as JSON on the left to sign; " +
                            "paste a JWT token on the right to verify and decode.");
        }
        @Override
        public JwtEncoderDecoderFx create(ToolConfiguration config) {
            return new JwtEncoderDecoderFx(config);
        }
    }
}
