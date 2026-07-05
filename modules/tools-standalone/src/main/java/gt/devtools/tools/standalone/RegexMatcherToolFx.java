package gt.devtools.tools.standalone;

import gt.devtools.common.ValueProperty;
import gt.devtools.settings.ToolConfiguration;
import gt.devtools.tools.api.ToolPresentation;
import gt.devtools.tools.api.fx.TextTransformerFx;
import gt.devtools.tools.api.fx.ToolFxFactory;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/** JavaFX version of Regex Matcher. */
public final class RegexMatcherToolFx extends TextTransformerFx {

    private final ValueProperty<String> pattern;
    private final ValueProperty<String> replacement;
    private final ValueProperty<String> mode;

    private RegexMatcherToolFx(ToolConfiguration config) {
        super(config);
        this.pattern = registerConfig("regexPattern", "");
        this.replacement = registerConfig("regexReplacement", "");
        this.mode = registerConfig("regexMode", "Find matches");
    }

    @Override
    protected void buildUi(BorderPane panel) {
        var bar = new HBox(8);
        bar.getChildren().add(new Label("Pattern:"));
        var patField = new TextField(pattern.get());
        patField.focusedProperty().addListener((obs, was, focused) -> {
            if (!focused) { pattern.set(patField.getText()); if (Boolean.TRUE.equals(liveTransformation.get())) transform(); }
        });
        bar.getChildren().add(patField);

        bar.getChildren().add(new Label("Mode:"));
        var modeCombo = new ComboBox<String>();
        modeCombo.getItems().addAll("Find matches", "Replace", "Split");
        modeCombo.setValue(mode.get());
        modeCombo.setOnAction(e -> mode.set(modeCombo.getValue()));
        bar.getChildren().add(modeCombo);

        bar.getChildren().add(new Label("Replace:"));
        var repField = new TextField(replacement.get());
        repField.focusedProperty().addListener((obs, was, focused) -> {
            if (!focused) replacement.set(repField.getText());
        });
        bar.getChildren().add(repField);

        panel.setTop(bar);
        super.buildUi(panel);
    }

    @Override protected String getTransformLabel() { return "Match"; }

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

    public static String findMatches(String input, String pattern) {
        var p = Pattern.compile(pattern);
        var m = p.matcher(input);
        var sb = new StringBuilder();
        int count = 0;
        while (m.find()) {
            count++;
            sb.append("Match ").append(count).append(": ").append(m.group());
            // Show capture groups if any
            if (m.groupCount() > 0) {
                for (int i = 1; i <= m.groupCount(); i++) {
                    sb.append("\n  Group ").append(i).append(": ").append(m.group(i));
                }
            }
            sb.append("\n");
        }
        return count == 0 ? "No matches found." : sb.toString().stripTrailing();
    }

    public static String replaceAll(String input, String pattern, String replacement) {
        return Pattern.compile(pattern).matcher(input).replaceAll(replacement);
    }

    public static String split(String input, String pattern) {
        return Pattern.compile(pattern).splitAsStream(input).collect(Collectors.joining("\n"));
    }

    public static final class Factory implements ToolFxFactory<RegexMatcherToolFx> {
        public Factory() {}
        @Override public String getId() { return "regular-expression-matcher"; }
        @Override public ToolPresentation getPresentation() {
            return ToolPresentation.of("regular-expression-matcher",
                    "Regex Matcher", "Regular Expression Matcher")
                    .withGroupId("text");
        }
        @Override public RegexMatcherToolFx create(ToolConfiguration config) {
            return new RegexMatcherToolFx(config);
        }
    }
}
