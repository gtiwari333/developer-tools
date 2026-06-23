package gt.devtools.tools.standalone;

import com.jayway.jsonpath.JsonPath;
import gt.devtools.common.ValueProperty;
import gt.devtools.settings.ToolConfiguration;
import gt.devtools.tools.api.ToolFactory;
import gt.devtools.tools.api.ToolPresentation;
import gt.devtools.tools.api.text.TextTransformer;

import javax.swing.*;
import java.awt.*;

/**
 * Evaluates JSONPath expressions against JSON input.
 */
public final class JsonPathTool extends TextTransformer {

    private final ValueProperty<String> expression;

    private JsonPathTool(ToolConfiguration config) {
        super(config);
        this.expression = registerConfig("jsonPathExpr", "$");
    }

    @Override
    protected void buildUi(JPanel panel) {
        var configBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        configBar.add(new JLabel("JSONPath:"));
        var exprField = new JTextField(expression.get(), 30);
        exprField.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override public void focusLost(java.awt.event.FocusEvent e) {
                expression.set(exprField.getText());
                if (Boolean.TRUE.equals(liveTransformation.get())) transform();
            }
        });
        configBar.add(exprField);
        panel.add(configBar, BorderLayout.NORTH);
        super.buildUi(panel);
        sourceEditor.setSyntaxStyle("text/json");
        resultEditor.setSyntaxStyle("text/json");
    }

    @Override
    protected String getTransformLabel() { return "Evaluate"; }

    @Override
    protected String doTransform(String input) {
        if (input.isBlank()) return "Paste JSON on the left.";
        try {
            Object result = JsonPath.read(input, expression.get());
            if (result instanceof String s) return s;
            if (result instanceof Number || result instanceof Boolean) return result.toString();
            // Pretty-print arrays and objects
            return new tools.jackson.databind.ObjectMapper()
                    .writerWithDefaultPrettyPrinter()
                    .writeValueAsString(result);
        } catch (Exception e) {
            return "JSONPath error: " + e.getMessage();
        }
    }

    public static final class Factory implements ToolFactory<JsonPathTool> {
        public Factory() {}
        @Override public String getId() { return "json-path"; }
        @Override public ToolPresentation getPresentation() {
            return ToolPresentation.of("json-path", "JSON Path", "JSON Path Evaluator");
        }
        @Override public JsonPathTool create(ToolConfiguration config) { return new JsonPathTool(config); }
    }
}
