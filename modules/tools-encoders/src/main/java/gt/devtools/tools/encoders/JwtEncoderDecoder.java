package gt.devtools.tools.encoders;

import gt.devtools.common.ValueProperty;
import gt.devtools.settings.ToolConfiguration;
import gt.devtools.tools.api.ToolFactory;
import gt.devtools.tools.api.ToolPresentation;
import gt.devtools.tools.api.converter.EncoderDecoder;
import org.jose4j.jws.AlgorithmIdentifiers;
import org.jose4j.jws.JsonWebSignature;
import org.jose4j.jwt.JwtClaims;
import org.jose4j.jwt.consumer.JwtConsumerBuilder;
import org.jose4j.keys.HmacKey;

import javax.swing.*;
import java.awt.*;
import java.nio.charset.StandardCharsets;

/**
 * JWT (JSON Web Token) encode / decode tool.
 * <p>
 * Supports HMAC-based algorithms (HS256, HS384, HS512). The user provides
 * the secret key in the configuration panel. Encoding: header+payload → JWT.
 * Decoding: JWT → header+payload JSON (with signature verification).
 */
public final class JwtEncoderDecoder extends EncoderDecoder {

    private static final String[] ALGORITHMS = {
            AlgorithmIdentifiers.HMAC_SHA256,
            AlgorithmIdentifiers.HMAC_SHA384,
            AlgorithmIdentifiers.HMAC_SHA512
    };

    private final ValueProperty<String> secret;
    private final ValueProperty<String> algorithm;

    private JwtEncoderDecoder(ToolConfiguration config) {
        super(config);
        this.algorithm = registerConfig("jwtAlgorithm", ALGORITHMS[0]);
        this.secret = registerSensitive("jwtSecret", "");
    }

    // ---------------------------------------------------------------
    // UI — add config panel above the converter
    // ---------------------------------------------------------------

    @Override
    protected void buildUi(JPanel panel) {
        // Config row at top
        var configBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        configBar.add(new JLabel("Algorithm:"));
        var algoCombo = new JComboBox<>(ALGORITHMS);
        algoCombo.setSelectedItem(algorithm.get());
        algoCombo.addActionListener(e -> algorithm.set((String) algoCombo.getSelectedItem()));
        configBar.add(algoCombo);
        configBar.add(new JLabel("Secret:"));
        var secretField = new JTextField(secret.get(), 25);
        secretField.addActionListener(e -> secret.set(secretField.getText()));
        secretField.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusLost(java.awt.event.FocusEvent e) {
                secret.set(secretField.getText());
            }
        });
        configBar.add(secretField);
        panel.add(configBar, BorderLayout.NORTH);

        // Standard converter layout below
        super.buildUi(panel);
    }

    @Override
    protected void afterBuildUi() {
        // Show JSON syntax highlighting for JWT payload
        sourceEditor.setSyntaxStyle("json");
        targetEditor.setSyntaxStyle("text/plain");
    }

    // ---------------------------------------------------------------
    // Conversion logic
    // ---------------------------------------------------------------

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

    // ---------------------------------------------------------------
    // Factory
    // ---------------------------------------------------------------

    public static final class Factory implements ToolFactory<JwtEncoderDecoder> {
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
        public JwtEncoderDecoder create(ToolConfiguration config) {
            return new JwtEncoderDecoder(config);
        }
    }
}
