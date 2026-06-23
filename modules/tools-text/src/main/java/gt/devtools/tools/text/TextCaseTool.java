package gt.devtools.tools.text;

import gt.devtools.common.ValueProperty;
import gt.devtools.settings.ToolConfiguration;
import gt.devtools.tools.api.ToolFactory;
import gt.devtools.tools.api.ToolPresentation;
import gt.devtools.tools.api.text.TextTransformer;

import javax.swing.*;
import java.awt.*;

/**
 * Converts text between various casing conventions.
 */
public final class TextCaseTool extends TextTransformer {

    private static final String[] MODES = {
            "UPPER CASE", "lower case", "Title Case", "camelCase",
            "snake_case", "kebab-case", "PascalCase", "CONSTANT_CASE"
    };
    private final ValueProperty<String> mode;

    private TextCaseTool(ToolConfiguration config) {
        super(config);
        this.mode = registerConfig("caseMode", "UPPER CASE");
    }

    @Override
    protected void buildUi(JPanel panel) {
        var configBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        configBar.add(new JLabel("Mode:"));
        var combo = new JComboBox<>(MODES);
        combo.setSelectedItem(mode.get());
        combo.addActionListener(e -> {
            mode.set((String) combo.getSelectedItem());
            if (Boolean.TRUE.equals(liveTransformation.get())) transform();
        });
        configBar.add(combo);
        panel.add(configBar, BorderLayout.NORTH);
        super.buildUi(panel);
    }

    @Override
    protected String getTransformLabel() { return "Convert"; }

    @Override
    protected String doTransform(String input) {
        return switch (mode.get()) {
            case "UPPER CASE" -> input.toUpperCase();
            case "lower case" -> input.toLowerCase();
            case "Title Case" -> toTitleCase(input);
            case "camelCase" -> toCamelCase(input);
            case "snake_case" -> toSnakeCase(input);
            case "kebab-case" -> toKebabCase(input);
            case "PascalCase" -> toPascalCase(input);
            case "CONSTANT_CASE" -> toConstantCase(input);
            default -> input;
        };
    }

    private static String toTitleCase(String s) {
        var sb = new StringBuilder();
        boolean next = true;
        for (char c : s.toCharArray()) {
            sb.append(next ? Character.toUpperCase(c) : Character.toLowerCase(c));
            next = Character.isWhitespace(c);
        }
        return sb.toString();
    }

    private static String toCamelCase(String s) {
        String[] words = s.split("[\\s_-]+");
        var sb = new StringBuilder();
        for (int i = 0; i < words.length; i++) {
            if (words[i].isEmpty()) continue;
            sb.append(i == 0
                    ? words[i].toLowerCase()
                    : Character.toUpperCase(words[i].charAt(0)) + words[i].substring(1).toLowerCase());
        }
        return sb.toString();
    }

    private static String toPascalCase(String s) {
        String[] words = s.split("[\\s_-]+");
        var sb = new StringBuilder();
        for (String w : words) {
            if (!w.isEmpty()) sb.append(Character.toUpperCase(w.charAt(0)))
                    .append(w.substring(1).toLowerCase());
        }
        return sb.toString();
    }

    private static String toSnakeCase(String s) {
        return s.trim().toLowerCase().replaceAll("[\\s-]+", "_");
    }

    private static String toKebabCase(String s) {
        return s.trim().toLowerCase().replaceAll("[\\s_]+", "-");
    }

    private static String toConstantCase(String s) {
        return s.trim().toUpperCase().replaceAll("[\\s-]+", "_");
    }

    public static final class Factory implements ToolFactory<TextCaseTool> {
        public Factory() {}
        @Override public String getId() { return "text-case-transformer"; }
        @Override public ToolPresentation getPresentation() {
            return ToolPresentation.of("text-case-transformer",
                    "Text Case", "Text Case Transformer").withGroupId("text");
        }
        @Override public TextCaseTool create(ToolConfiguration config) { return new TextCaseTool(config); }
    }
}
