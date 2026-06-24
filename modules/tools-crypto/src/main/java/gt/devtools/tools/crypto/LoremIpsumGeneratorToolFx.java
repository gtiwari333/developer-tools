package gt.devtools.tools.crypto;

import gt.devtools.common.ValueProperty;
import gt.devtools.settings.ToolConfiguration;
import gt.devtools.tools.api.ToolPresentation;
import gt.devtools.tools.api.fx.OneLineTextGeneratorFx;
import gt.devtools.tools.api.fx.ToolFxFactory;
import javafx.scene.control.*;
import javafx.scene.layout.*;

public final class LoremIpsumGeneratorToolFx extends OneLineTextGeneratorFx {
    private final ValueProperty<String> unit; private final ValueProperty<Integer> count;
    private LoremIpsumGeneratorToolFx(ToolConfiguration config) {
        super(config); unit = registerConfig("loremUnit", "paragraphs"); count = registerConfig("loremCount", 3); }
    @Override protected void buildConfigurationUi(VBox panel) {
        var r1 = new HBox(8); r1.getChildren().add(new Label("Generate:"));
        var combo = new ComboBox<String>(); combo.getItems().addAll("paragraphs", "sentences", "words"); combo.setValue(unit.get());
        combo.setOnAction(e -> unit.set(combo.getValue())); r1.getChildren().add(combo); panel.getChildren().add(r1);
        var r2 = new HBox(8); r2.getChildren().add(new Label("Count:"));
        var spin = new Spinner<Integer>(1, 500, count.get());
        spin.valueProperty().addListener((o, w, v) -> count.set(v)); r2.getChildren().add(spin); panel.getChildren().add(r2);
    }
    @Override protected String generate() {
        return switch (unit.get()) { case "sentences" -> LoremIpsumGeneratorTool.generateSentences(count.get());
            case "words" -> LoremIpsumGeneratorTool.generateWords(count.get());
            default -> LoremIpsumGeneratorTool.generateParagraphs(count.get()); }; }
    public static final class Factory implements ToolFxFactory<LoremIpsumGeneratorToolFx> {
        public Factory() {} @Override public String getId() { return "lorem-ipsum-generator-fx"; }
        @Override public ToolPresentation getPresentation() {
            return ToolPresentation.of("lorem-ipsum-generator-fx", "Lorem Ipsum (FX)", "Lorem Ipsum Generator").withGroupId("crypto"); }
        @Override public LoremIpsumGeneratorToolFx create(ToolConfiguration c) { return new LoremIpsumGeneratorToolFx(c); }
    }
}
