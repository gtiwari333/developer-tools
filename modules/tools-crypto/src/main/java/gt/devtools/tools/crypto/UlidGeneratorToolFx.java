package gt.devtools.tools.crypto;

import com.github.f4b6a3.ulid.UlidCreator;
import gt.devtools.settings.ToolConfiguration;
import gt.devtools.tools.api.ToolPresentation;
import gt.devtools.tools.api.fx.OneLineTextGeneratorFx;
import gt.devtools.tools.api.fx.ToolFxFactory;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

/**
 * JavaFX version: Generates ULIDs (Universally Unique Lexicographically
 * Sortable Identifiers).
 */
public final class UlidGeneratorToolFx extends OneLineTextGeneratorFx {

    private UlidGeneratorToolFx(ToolConfiguration config) {
        super(config);
    }

    @Override
    protected void buildConfigurationUi(VBox configPanel) {
        configPanel.getChildren().add(
                new Label("26-character, time-sortable unique ID. No configuration needed."));
    }

    @Override
    protected String generate() {
        return UlidCreator.getUlid().toString();
    }

    public static final class Factory implements ToolFxFactory<UlidGeneratorToolFx> {
        public Factory() {}
        @Override public String getId() { return "ulid-generator"; }
        @Override public ToolPresentation getPresentation() {
            return ToolPresentation.of("ulid-generator",
                    "ULID Generator", "ULID Generator").withGroupId("crypto");
        }
        @Override
        public UlidGeneratorToolFx create(ToolConfiguration config) {
            return new UlidGeneratorToolFx(config);
        }
    }
}
