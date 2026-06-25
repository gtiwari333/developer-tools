package gt.devtools.tools.formatters;

import com.github.vertical_blank.sqlformatter.SqlFormatter;
import gt.devtools.settings.ToolConfiguration;
import gt.devtools.tools.api.ToolPresentation;
import gt.devtools.tools.api.fx.TextTransformerFx;
import gt.devtools.tools.api.fx.ToolFxFactory;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;

/** JavaFX version: Formats SQL statements with proper indentation and casing. */
public final class SqlFormatterToolFx extends TextTransformerFx {

    private SqlFormatterToolFx(ToolConfiguration config) { super(config); }

    @Override
    protected void buildUi(BorderPane panel) {
        var configBar = new HBox(8);
        configBar.setStyle("-fx-padding: 4 0;");
        configBar.getChildren().add(new Label("Paste SQL on the left, formatted output on the right."));
        panel.setTop(configBar);
        super.buildUi(panel);
        sourceEditor.setSyntaxStyle("text/sql");
        resultEditor.setSyntaxStyle("text/sql");
    }

    @Override
    protected String getTransformLabel() { return "Format"; }

    @Override
    protected String doTransform(String input) {
        if (input.isBlank()) return "Paste SQL to format.";
        try {
            return SqlFormatter.format(input);
        } catch (Exception e) {
            return "SQL formatting error: " + e.getMessage();
        }
    }

    public static final class Factory implements ToolFxFactory<SqlFormatterToolFx> {
        public Factory() {}
        @Override public String getId() { return "sql-formatting"; }
        @Override public ToolPresentation getPresentation() {
            return ToolPresentation.of("sql-formatting",
                    "SQL Formatter", "SQL Formatting").withGroupId("formatters");
        }
        @Override public SqlFormatterToolFx create(ToolConfiguration config) { return new SqlFormatterToolFx(config); }
    }
}
