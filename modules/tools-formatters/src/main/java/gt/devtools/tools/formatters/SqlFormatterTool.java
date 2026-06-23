package gt.devtools.tools.formatters;

import com.github.vertical_blank.sqlformatter.SqlFormatter;
import gt.devtools.settings.ToolConfiguration;
import gt.devtools.tools.api.ToolFactory;
import gt.devtools.tools.api.ToolPresentation;
import gt.devtools.tools.api.text.TextTransformer;

import javax.swing.*;
import java.awt.*;

/** Formats SQL statements with proper indentation and casing. */
public final class SqlFormatterTool extends TextTransformer {

    private SqlFormatterTool(ToolConfiguration config) { super(config); }

    @Override
    protected void buildUi(JPanel panel) {
        var configBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        configBar.add(new JLabel("Paste SQL on the left, formatted output on the right."));
        panel.add(configBar, BorderLayout.NORTH);
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

    public static final class Factory implements ToolFactory<SqlFormatterTool> {
        public Factory() {}
        @Override public String getId() { return "sql-formatting"; }
        @Override public ToolPresentation getPresentation() {
            return ToolPresentation.of("sql-formatting",
                    "SQL Formatter", "SQL Formatting").withGroupId("formatters");
        }
        @Override public SqlFormatterTool create(ToolConfiguration config) { return new SqlFormatterTool(config); }
    }
}
