package gt.devtools.tools.formatters;

import gt.devtools.common.ValueProperty;
import gt.devtools.settings.ToolConfiguration;
import gt.devtools.tools.api.ToolFactory;
import gt.devtools.tools.api.ToolPresentation;
import gt.devtools.tools.api.text.TextTransformer;

import javax.swing.*;
import java.awt.*;
// ArrayList/List used via FQN in static methods

/**
 * Converts between single-line and multi-line CLI command formats.
 * Splits long commands onto multiple lines with \\ continuations,
 * or joins multi-line commands back to a single line.
 */
public final class CliCommandConverterTool extends TextTransformer {

    private final ValueProperty<String> mode;

    private CliCommandConverterTool(ToolConfiguration config) {
        super(config);
        this.mode = registerConfig("cliMode", "Split to multiple lines");
    }

    @Override
    protected void buildUi(JPanel panel) {
        var configBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        configBar.add(new JLabel("Mode:"));
        var modeCombo = new JComboBox<>(new String[]{
                "Split to multiple lines", "Join to single line"
        });
        modeCombo.setSelectedItem(mode.get());
        modeCombo.addActionListener(e -> {
            mode.set((String) modeCombo.getSelectedItem());
            if (Boolean.TRUE.equals(liveTransformation.get())) transform();
        });
        configBar.add(modeCombo);
        panel.add(configBar, BorderLayout.NORTH);
        super.buildUi(panel);
    }

    @Override
    protected String getTransformLabel() { return "Convert"; }

    @Override
    protected String doTransform(String input) {
        if (input.isBlank()) return "Paste a CLI command.";
        if (mode.get().contains("Join")) {
            return joinCommand(input);
        } else {
            return splitCommand(input);
        }
    }

    // -- public static methods (testable without Swing)

    /** Joins a multi-line command (with \\ continuations) into a single line. */
    public static String joinCommand(String input) {
        return input.replace("\\\n", " ").replaceAll("\\s+", " ").strip();
    }

    /** Splits a single-line command into multiple lines with \\ continuations. */
    public static String splitCommand(String cmd) {
        java.util.List<String> parts = parseArgs(cmd);
        var sb = new StringBuilder();
        for (int i = 0; i < parts.size(); i++) {
            sb.append(parts.get(i));
            if (i < parts.size() - 1) {
                sb.append(parts.get(i).startsWith("-") || parts.get(i).equals("|")
                        ? " \\\n    " : " \\\n  ");
            }
        }
        return sb.toString();
    }

    /** Parses a command-line string into individual arguments, respecting quotes. */
    public static java.util.List<String> parseArgs(String cmd) {
        var args = new java.util.ArrayList<String>();
        var current = new StringBuilder();
        boolean inQuote = false;
        for (int i = 0; i < cmd.length(); i++) {
            char c = cmd.charAt(i);
            if (c == '"' || c == '\'') { inQuote = !inQuote; current.append(c); }
            else if (Character.isWhitespace(c) && !inQuote) {
                if (!current.isEmpty()) { args.add(current.toString()); current.setLength(0); }
            } else { current.append(c); }
        }
        if (!current.isEmpty()) args.add(current.toString());
        return args;
    }

    public static final class Factory implements ToolFactory<CliCommandConverterTool> {
        public Factory() {}
        @Override public String getId() { return "cli-command-converter"; }
        @Override public ToolPresentation getPresentation() {
            return ToolPresentation.of("cli-command-converter",
                    "CLI Command Converter", "CLI Command Converter").withGroupId("formatters");
        }
        @Override public CliCommandConverterTool create(ToolConfiguration config) { return new CliCommandConverterTool(config); }
    }
}
