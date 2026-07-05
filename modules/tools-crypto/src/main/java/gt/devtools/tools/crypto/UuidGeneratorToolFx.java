package gt.devtools.tools.crypto;

import gt.devtools.common.ValueProperty;
import gt.devtools.settings.ToolConfiguration;
import gt.devtools.tools.api.ToolPresentation;
import gt.devtools.tools.api.fx.OneLineTextGeneratorFx;
import gt.devtools.tools.api.fx.ToolFxFactory;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.security.SecureRandom;
import java.util.UUID;

/** JavaFX version of UUID generator. */
public final class UuidGeneratorToolFx extends OneLineTextGeneratorFx {

    private static final String[] VERSIONS = {
        "v4 (random)", "v1 (time)", "v7 (epoch+random)"
    };
    private final ValueProperty<String> version;

    private UuidGeneratorToolFx(ToolConfiguration config) {
        super(config);
        this.version = registerConfig("uuidVersion", "v4 (random)");
    }

    @Override
    protected void buildConfigurationUi(VBox configPanel) {
        var row = new HBox(8);
        row.getChildren().add(new Label("Version:"));
        var combo = new ComboBox<String>();
        combo.getItems().addAll(VERSIONS);
        combo.setValue(version.get());
        combo.setOnAction(e -> version.set(combo.getValue()));
        row.getChildren().add(combo);
        configPanel.getChildren().add(row);
    }

    @Override
    protected String generate() {
        String v = version.get();
        if (v.startsWith("v1")) return generateV1();
        if (v.startsWith("v7")) return generateV7();
        return generateV4();
    }

    public static String generateV4() { return UUID.randomUUID().toString(); }
    public static String generateV1() {
        return com.fasterxml.uuid.Generators.timeBasedGenerator().generate().toString();
    }
    public static String generateV7() {
        return com.fasterxml.uuid.Generators.timeBasedEpochGenerator().generate().toString();
    }
    public static String generateV3(String namespace, String name) {
        return UUID.nameUUIDFromBytes((namespace + name).getBytes(java.nio.charset.StandardCharsets.UTF_8)).toString();
    }
    public static String generateV6() {
        return com.fasterxml.uuid.Generators.timeBasedReorderedGenerator().generate().toString();
    }

    public static final class Factory implements ToolFxFactory<UuidGeneratorToolFx> {
        public Factory() {}
        @Override public String getId() { return "uuid-generator"; }
        @Override public ToolPresentation getPresentation() {
            return ToolPresentation.of("uuid-generator",
                    "UUID Generator", "UUID Generator")
                    .withGroupId("crypto");
        }
        @Override public UuidGeneratorToolFx create(ToolConfiguration config) {
            return new UuidGeneratorToolFx(config);
        }
    }
}
