package gt.devtools.tools.standalone;

import com.jayway.jsonpath.JsonPath;
import gt.devtools.common.ValueProperty;
import gt.devtools.settings.ToolConfiguration;
import gt.devtools.tools.api.ToolPresentation;
import gt.devtools.tools.api.fx.TextTransformerFx;
import gt.devtools.tools.api.fx.ToolFxFactory;
import javafx.scene.control.*;
import javafx.scene.layout.*;

public final class JsonPathToolFx extends TextTransformerFx {
    private final ValueProperty<String> expression;
    private JsonPathToolFx(ToolConfiguration config) { super(config); expression = registerConfig("jsonPathExpr", "$"); }
    @Override protected void buildUi(BorderPane panel) {
        var bar = new HBox(8); bar.getChildren().add(new Label("JSONPath:"));
        var f = new TextField(expression.get()); f.focusedProperty().addListener((o, w, foc) -> { if (!foc) { expression.set(f.getText()); if (Boolean.TRUE.equals(liveTransformation.get())) transform(); } });
        bar.getChildren().add(f); panel.setTop(bar); super.buildUi(panel);
    }
    @Override protected String getTransformLabel() { return "Evaluate"; }
    @Override protected String doTransform(String input) {
        if (input.isBlank()) return "Paste JSON on the left.";
        try {
            Object result = JsonPath.read(input, expression.get());
            if (result instanceof String s) return s;
            if (result instanceof Number || result instanceof Boolean) return result.toString();
            return new tools.jackson.databind.ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(result);
        } catch (Exception e) { return "JSONPath error: " + e.getMessage(); }
    }
    public static final class Factory implements ToolFxFactory<JsonPathToolFx> {
        public Factory() {} @Override public String getId() { return "json-path-fx"; }
        @Override public ToolPresentation getPresentation() {
            return ToolPresentation.of("json-path-fx", "JSON Path (FX)", "JSON Path Evaluator").withGroupId("text"); }
        @Override public JsonPathToolFx create(ToolConfiguration c) { return new JsonPathToolFx(c); }
    }
}
