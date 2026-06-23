package gt.devtools.tools.text;

import gt.devtools.common.ValueProperty;
import gt.devtools.settings.ToolConfiguration;
import gt.devtools.tools.api.ToolFactory;
import gt.devtools.tools.api.ToolPresentation;
import gt.devtools.tools.api.text.TextTransformer;

import javax.swing.*;
import java.awt.*;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.regex.Pattern;

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
        String[] lines = input.split("\n");
        return switch (mode.get()) {
            case "Include matching" -> {
                var p = Pattern.compile(pattern.get());
                yield String.join("\n", Arrays.stream(lines).filter(l -> p.matcher(l).find()).toList());
            }
            case "Exclude matching" -> {
                var p = Pattern.compile(pattern.get());
                yield String.join("\n", Arrays.stream(lines).filter(l -> !p.matcher(l).find()).toList());
            }
            case "Unique lines" -> {
                var set = new LinkedHashSet<>(Arrays.asList(lines));
                yield String.join("\n", set);
            }
            case "Trim whitespace" -> {
                var sb = new StringBuilder();
                for (String l : lines) sb.append(l.strip()).append("\n");
                yield sb.toString().stripTrailing();
            }
            default -> input;
        };
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
