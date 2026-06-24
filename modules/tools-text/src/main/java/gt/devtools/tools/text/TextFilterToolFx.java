package gt.devtools.tools.text;

import gt.devtools.common.ValueProperty;
import gt.devtools.settings.ToolConfiguration;
import gt.devtools.tools.api.ToolPresentation;
import gt.devtools.tools.api.fx.TextTransformerFx;
import gt.devtools.tools.api.fx.ToolFxFactory;
import javafx.scene.control.*;
import javafx.scene.layout.*;

public final class TextFilterToolFx extends TextTransformerFx {
    private final ValueProperty<String> mode, pattern;
    private TextFilterToolFx(ToolConfiguration config) {
        super(config); mode = registerConfig("filterMode", "Include matching"); pattern = registerConfig("filterPattern", ""); }
    @Override protected void buildUi(BorderPane panel) {
        var bar = new HBox(8); bar.getChildren().add(new Label("Mode:"));
        var combo = new ComboBox<String>(); combo.getItems().addAll("Include matching", "Exclude matching", "Unique lines", "Trim whitespace");
        combo.setValue(mode.get()); combo.setOnAction(e -> { mode.set(combo.getValue()); if (Boolean.TRUE.equals(liveTransformation.get())) transform(); });
        bar.getChildren().add(combo); bar.getChildren().add(new Label("Pattern:"));
        var f = new TextField(pattern.get()); f.focusedProperty().addListener((o, w, foc) -> { if (!foc) pattern.set(f.getText()); });
        bar.getChildren().add(f); panel.setTop(bar); super.buildUi(panel);
    }
    @Override protected String getTransformLabel() { return "Filter"; }
    @Override protected String doTransform(String input) {
        return switch (mode.get()) {
            case "Include matching" -> TextFilterTool.filterInclude(input, pattern.get());
            case "Exclude matching" -> TextFilterTool.filterExclude(input, pattern.get());
            case "Unique lines" -> TextFilterTool.filterUnique(input);
            case "Trim whitespace" -> TextFilterTool.trimLines(input);
            default -> input; }; }
    public static final class Factory implements ToolFxFactory<TextFilterToolFx> {
        public Factory() {} @Override public String getId() { return "text-filter-fx"; }
        @Override public ToolPresentation getPresentation() {
            return ToolPresentation.of("text-filter-fx", "Text Filter (FX)", "Text Filter").withGroupId("text"); }
        @Override public TextFilterToolFx create(ToolConfiguration c) { return new TextFilterToolFx(c); }
    }
}
