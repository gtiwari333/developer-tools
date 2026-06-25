package gt.devtools.tools.formatters;

import gt.devtools.common.ValueProperty;
import gt.devtools.settings.ToolConfiguration;
import gt.devtools.tools.api.ToolPresentation;
import gt.devtools.tools.api.fx.TextTransformerFx;
import gt.devtools.tools.api.fx.ToolFxFactory;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.util.ArrayList;
import java.util.List;

public final class CliCommandConverterToolFx extends TextTransformerFx {
    private final ValueProperty<String> mode;
    private CliCommandConverterToolFx(ToolConfiguration config) { super(config); mode = registerConfig("cliMode", "Split to multiple lines"); }
    @Override protected void buildUi(BorderPane panel) {
        var bar = new HBox(8); bar.getChildren().add(new Label("Mode:"));
        var combo = new ComboBox<String>(); combo.getItems().addAll("Split to multiple lines", "Join to single line"); combo.setValue(mode.get());
        combo.setOnAction(e -> { mode.set(combo.getValue()); if (Boolean.TRUE.equals(liveTransformation.get())) transform(); });
        bar.getChildren().add(combo); panel.setTop(bar); super.buildUi(panel);
    }
    @Override protected String getTransformLabel() { return "Convert"; }
    @Override protected String doTransform(String input) {
        if (input.isBlank()) return "Paste a CLI command.";
        return mode.get().contains("Join") ? joinCommand(input) : splitCommand(input); }

    /** Join a multi-line command into a single line. */
    public static String joinCommand(String input) {
        var sb = new StringBuilder();
        for (String line : input.split("\n")) {
            String trimmed = line.strip().replaceAll("\\\\$", "").strip();
            if (!trimmed.isEmpty()) {
                if (!sb.isEmpty()) sb.append(" ");
                sb.append(trimmed);
            }
        }
        return sb.toString();
    }
    /** Split a single-line command into multiple lines with backslash continuations. */
    public static String splitCommand(String input) {
        List<String> args = parseArgs(input);
        if (args.isEmpty()) return "";
        var sb = new StringBuilder(args.getFirst());
        for (int i = 1; i < args.size(); i++) {
            sb.append(" \\\n  ").append(args.get(i));
        }
        return sb.toString();
    }
    /** Parse command-line arguments respecting quotes. */
    public static List<String> parseArgs(String input) {
        var args = new ArrayList<String>();
        var current = new StringBuilder();
        boolean inSingle = false, inDouble = false;
        for (char c : input.toCharArray()) {
            if (c == '\'' && !inDouble) { inSingle = !inSingle; current.append(c); }
            else if (c == '"' && !inSingle) { inDouble = !inDouble; current.append(c); }
            else if (c == ' ' && !inSingle && !inDouble) {
                if (!current.isEmpty()) { args.add(current.toString()); current.setLength(0); }
            }
            else current.append(c);
        }
        if (!current.isEmpty()) args.add(current.toString());
        return args;
    }

    public static final class Factory implements ToolFxFactory<CliCommandConverterToolFx> {
        public Factory() {} @Override public String getId() { return "cli-command-converter"; }
        @Override public ToolPresentation getPresentation() {
            return ToolPresentation.of("cli-command-converter", "CLI Converter", "CLI Command Converter").withGroupId("formatters"); }
        @Override public CliCommandConverterToolFx create(ToolConfiguration c) { return new CliCommandConverterToolFx(c); }
    }
}
