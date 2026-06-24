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
        return HashingTool.hash(input, algorithm.get());
    }

    public static final class Factory implements ToolFxFactory<HashingToolFx> {
        public Factory() {}
        @Override public String getId() { return "hashing-transformer-fx"; }
        @Override public ToolPresentation getPresentation() {
            return ToolPresentation.of("hashing-transformer-fx",
                    "Hashing (FX)", "Hashing Transformer")
                    .withGroupId("text");
        }
        @Override public HashingToolFx create(ToolConfiguration config) {
            return new HashingToolFx(config);
        }
    }
}
