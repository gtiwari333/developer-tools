package gt.devtools.tools.text;

import gt.devtools.common.ValueProperty;
import gt.devtools.settings.ToolConfiguration;
import gt.devtools.tools.api.ToolFactory;
import gt.devtools.tools.api.ToolPresentation;
import gt.devtools.tools.api.text.TextTransformer;

import javax.swing.*;
import java.awt.*;

/**
 * Filters lines of text: include/exclude by regex, deduplicate,
 * trim whitespace.
 */
public final class TextFilterTool extends TextTransformer {

    private final ValueProperty<String> mode;
    private final ValueProperty<String> pattern;

    private TextFilterTool(ToolConfiguration config) {
        super(config);
        this.mode = registerConfig("filterMode", "Include matching");
        this.pattern = registerConfig("filterPattern", "");
    }

    @Override
    protected void buildUi(JPanel panel) {
        var configBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        configBar.add(new JLabel("Mode:"));
        var combo = new JComboBox<>(new String[]{
                "Include matching", "Exclude matching", "Unique lines", "Trim whitespace"
        });
        combo.setSelectedItem(mode.get());
        combo.addActionListener(e -> {
            mode.set((String) combo.getSelectedItem());
            if (Boolean.TRUE.equals(liveTransformation.get())) transform();
        });
        configBar.add(combo);
        configBar.add(new JLabel("Pattern:"));
        var patternField = new JTextField(pattern.get(), 15);
        patternField.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override public void focusLost(java.awt.event.FocusEvent e) { pattern.set(patternField.getText()); }
        });
        configBar.add(patternField);
        panel.add(configBar, BorderLayout.NORTH);
        super.buildUi(panel);
    }

    @Override
    protected String getTransformLabel() { return "Filter"; }

    @Override
    protected String doTransform(String input) {
        return switch (mode.get()) {
            case "Include matching" -> filterInclude(input, pattern.get());
            case "Exclude matching" -> filterExclude(input, pattern.get());
            case "Unique lines"     -> filterUnique(input);
            case "Trim whitespace"  -> trimLines(input);
            default                 -> input;
        };
    }

    // -- public static filter methods (testable without Swing)

    /** Returns only lines that match the given regex pattern. */
    public static String filterInclude(String input, String regex) {
        var p = java.util.regex.Pattern.compile(regex);
        return String.join("\n",
                java.util.Arrays.stream(input.split("\n"))
                        .filter(l -> p.matcher(l).find())
                        .toList());
    }

    /** Returns only lines that do NOT match the given regex pattern. */
    public static String filterExclude(String input, String regex) {
        var p = java.util.regex.Pattern.compile(regex);
        return String.join("\n",
                java.util.Arrays.stream(input.split("\n"))
                        .filter(l -> !p.matcher(l).find())
                        .toList());
    }

    /** Deduplicates lines, preserving first occurrence order. */
    public static String filterUnique(String input) {
        var set = new java.util.LinkedHashSet<>(
                java.util.Arrays.asList(input.split("\n")));
        return String.join("\n", set);
    }

    /** Strips leading/trailing whitespace from each line. */
    public static String trimLines(String input) {
        var sb = new StringBuilder();
        for (String l : input.split("\n")) sb.append(l.strip()).append("\n");
        return sb.toString().stripTrailing();
    }

    public static final class Factory implements ToolFactory<TextFilterTool> {
        public Factory() {}
        @Override public String getId() { return "text-filter"; }
        @Override public ToolPresentation getPresentation() {
            return ToolPresentation.of("text-filter",
                    "Text Filter", "Text Filter").withGroupId("text");
        }
        @Override public TextFilterTool create(ToolConfiguration config) { return new TextFilterTool(config); }
    }
}
