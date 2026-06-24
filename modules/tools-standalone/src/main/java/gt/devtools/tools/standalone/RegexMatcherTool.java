package gt.devtools.tools.standalone;

import gt.devtools.common.ValueProperty;
import gt.devtools.settings.ToolConfiguration;
import gt.devtools.tools.api.ToolFactory;
import gt.devtools.tools.api.ToolPresentation;
import gt.devtools.tools.api.text.TextTransformer;

import javax.swing.*;
import java.awt.*;
// Matcher/Pattern used via FQN in static methods

/**
 * Tests regular expressions against input text. Shows matches, groups,
 * and supports find/replace.
 */
public final class RegexMatcherTool extends TextTransformer {

    private final ValueProperty<String> pattern;
    private final ValueProperty<String> replacement;
    private final ValueProperty<String> mode;

    private RegexMatcherTool(ToolConfiguration config) {
        super(config);
        this.pattern = registerConfig("regexPattern", "");
        this.replacement = registerConfig("regexReplacement", "");
        this.mode = registerConfig("regexMode", "Find matches");
    }

    @Override
    protected void buildUi(JPanel panel) {
        var configBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        configBar.add(new JLabel("Pattern:"));
        var patternField = new JTextField(pattern.get(), 20);
        patternField.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override public void focusLost(java.awt.event.FocusEvent e) {
                pattern.set(patternField.getText());
                if (Boolean.TRUE.equals(liveTransformation.get())) transform();
            }
        });
        configBar.add(patternField);
        configBar.add(new JLabel("Mode:"));
        var modeCombo = new JComboBox<>(new String[]{"Find matches", "Replace", "Split"});
        modeCombo.setSelectedItem(mode.get());
        modeCombo.addActionListener(e -> mode.set((String) modeCombo.getSelectedItem()));
        configBar.add(modeCombo);
        configBar.add(new JLabel("Replace:"));
        var replaceField = new JTextField(replacement.get(), 12);
        replaceField.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override public void focusLost(java.awt.event.FocusEvent e) { replacement.set(replaceField.getText()); }
        });
        configBar.add(replaceField);
        panel.add(configBar, BorderLayout.NORTH);
        super.buildUi(panel);
        sourceEditor.setSyntaxStyle("text/plain");
    }

    @Override
    protected String getTransformLabel() { return "Match"; }

    @Override
    protected String doTransform(String input) {
        String pat = pattern.get();
        if (pat.isEmpty()) return "Enter a regex pattern above.";
        try {
            return switch (mode.get()) {
                case "Find matches" -> findMatches(input, pat);
                case "Replace" -> replaceAll(input, pat, replacement.get());
                case "Split" -> split(input, pat);
                default -> input;
            };
        } catch (Exception e) {
            return "Regex error: " + e.getMessage();
        }
    }

    // -- public static methods (testable without Swing)

    /** Finds all regex matches and returns them with group details. */
    public static String findMatches(String input, String pattern) {
        java.util.regex.Matcher m = java.util.regex.Pattern.compile(pattern).matcher(input);
        var sb = new StringBuilder();
        int count = 0;
        while (m.find()) {
            count++;
            sb.append("Match ").append(count).append(": ").append(m.group()).append("\n");
            for (int i = 1; i <= m.groupCount(); i++) {
                sb.append("  Group ").append(i).append(": ").append(m.group(i)).append("\n");
            }
        }
        return count == 0 ? "No matches found." : sb.toString();
    }

    /** Replaces all regex matches with the given replacement string. */
    public static String replaceAll(String input, String pattern, String replacement) {
        return java.util.regex.Pattern.compile(pattern).matcher(input)
                .replaceAll(replacement);
    }

    /** Splits the input by the given regex pattern. */
    public static String split(String input, String pattern) {
        String[] parts = java.util.regex.Pattern.compile(pattern).split(input);
        return String.join("\n", parts);
    }

    public static final class Factory implements ToolFactory<RegexMatcherTool> {
        public Factory() {}
        @Override public String getId() { return "regular-expression-matcher"; }
        @Override
        public ToolPresentation getPresentation() {
            return ToolPresentation.of("regular-expression-matcher",
                    "Regex Matcher", "Regular Expression Matcher");
        }
        @Override
        public RegexMatcherTool create(ToolConfiguration config) { return new RegexMatcherTool(config); }
    }
}
