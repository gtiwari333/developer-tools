package gt.devtools.tools.text;

import gt.devtools.common.ValueProperty;
import gt.devtools.settings.ToolConfiguration;
import gt.devtools.tools.api.ToolPresentation;
import gt.devtools.tools.api.fx.TextTransformerFx;
import gt.devtools.tools.api.fx.ToolFxFactory;
import javafx.scene.control.*;
import javafx.scene.layout.*;

public final class TextCaseToolFx extends TextTransformerFx {
    private static final String[] MODES = {"UPPER CASE", "lower case", "Title Case", "camelCase", "snake_case", "kebab-case", "PascalCase", "CONSTANT_CASE"};
    private final ValueProperty<String> mode;
    private TextCaseToolFx(ToolConfiguration config) { super(config); this.mode = registerConfig("caseMode", "UPPER CASE"); }
    @Override protected void buildUi(BorderPane panel) {
        var bar = new HBox(8); bar.getChildren().add(new Label("Mode:"));
        var combo = new ComboBox<String>(); combo.getItems().addAll(MODES); combo.setValue(mode.get());
        combo.setOnAction(e -> { mode.set(combo.getValue()); if (Boolean.TRUE.equals(liveTransformation.get())) transform(); });
        bar.getChildren().add(combo); panel.setTop(bar); super.buildUi(panel);
    }
    @Override protected String getTransformLabel() { return "Convert"; }
    @Override protected String doTransform(String input) {
        return switch (mode.get()) {
            case "UPPER CASE" -> input.toUpperCase(); case "lower case" -> input.toLowerCase();
            case "Title Case" -> TextCaseTool.toTitleCase(input); case "camelCase" -> TextCaseTool.toCamelCase(input);
            case "snake_case" -> TextCaseTool.toSnakeCase(input); case "kebab-case" -> TextCaseTool.toKebabCase(input);
            case "PascalCase" -> TextCaseTool.toPascalCase(input); case "CONSTANT_CASE" -> TextCaseTool.toConstantCase(input);
            default -> input; }; }
    public static final class Factory implements ToolFxFactory<TextCaseToolFx> {
        public Factory() {} @Override public String getId() { return "text-case-transformer-fx"; }
        @Override public ToolPresentation getPresentation() {
            return ToolPresentation.of("text-case-transformer-fx", "Text Case (FX)", "Text Case Transformer").withGroupId("text"); }
        @Override public TextCaseToolFx create(ToolConfiguration c) { return new TextCaseToolFx(c); }
    }
}
