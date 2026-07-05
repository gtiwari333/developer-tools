package gt.devtools.tools.crypto;

import gt.devtools.common.ValueProperty;
import gt.devtools.settings.ToolConfiguration;
import gt.devtools.tools.api.ToolPresentation;
import gt.devtools.tools.api.fx.OneLineTextGeneratorFx;
import gt.devtools.tools.api.fx.ToolFxFactory;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.security.SecureRandom;

/**
 * JavaFX version: Generates compact, URL-safe, cryptographically-strong
 * random IDs using the Nano ID algorithm.
 */
public final class NanoIdGeneratorToolFx extends OneLineTextGeneratorFx {

    private static final char[] ALPHABET =
            "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz_-".toCharArray();

    private final ValueProperty<Integer> length;

    private NanoIdGeneratorToolFx(ToolConfiguration config) {
        super(config);
        this.length = registerConfig("nanoIdLength", 21);
    }

    @Override
    protected void buildConfigurationUi(VBox configPanel) {
        var row = new HBox(8);
        row.getChildren().add(new Label("Length:"));
        var spinner = new Spinner<Integer>(1, 256, length.get());
        spinner.setEditable(true);
        spinner.valueProperty().addListener((obs, old, val) -> length.set(val));
        row.getChildren().add(spinner);
        configPanel.getChildren().add(row);
    }

    @Override
    protected String generate() {
        return generate(length.get());
    }

    /** Generates a Nano ID of the given length using the default alphabet. */
    public static String generate(int length) {
        var rng = new SecureRandom();
        var sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(ALPHABET[rng.nextInt(ALPHABET.length)]);
        }
        return sb.toString();
    }

    /** Returns the default Nano ID alphabet (64 URL-safe characters). */
    public static char[] alphabet() {
        return ALPHABET.clone();
    }

    public static final class Factory implements ToolFxFactory<NanoIdGeneratorToolFx> {
        public Factory() {}
        @Override public String getId() { return "nano-id-generator"; }
        @Override public ToolPresentation getPresentation() {
            return ToolPresentation.of("nano-id-generator",
                    "Nano ID Generator", "Nano ID Generator").withGroupId("crypto");
        }
        @Override
        public NanoIdGeneratorToolFx create(ToolConfiguration config) {
            return new NanoIdGeneratorToolFx(config);
        }
    }
}
