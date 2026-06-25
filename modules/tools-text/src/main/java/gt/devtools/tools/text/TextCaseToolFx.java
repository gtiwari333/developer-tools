package gt.devtools.tools.text;

import gt.devtools.common.ValueProperty;
import gt.devtools.settings.ToolConfiguration;
import gt.devtools.tools.api.ToolPresentation;
import gt.devtools.tools.api.fx.TextTransformerFx;
import gt.devtools.tools.api.fx.ToolFxFactory;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.util.Arrays;
import java.util.stream.Collectors;

public final class TextCaseToolFx extends TextTransformerFx {
    private static final String[] MODES = {"UPPER CASE", "lower case", "Title Case", "camelCase", "snake_case", "kebab-case", "PascalCase", "CONSTANT_CASE"};
    private final ValueProperty<String> mode;
    private TextCaseToolFx(ToolConfiguration config) { super(config); this.mode = registerConfig("caseMode", "UPPER CASE"); }
    @Override protected void buildUi(BorderPane panel) {
        var bar = new HBox(8); bar.getChildren().add(new Label("Mode:"));
        var combo = new ComboBox<String>(); combo.getItems().addAll(MODES); combo.setValue(mode.get());
        combo.setOnAction(e -> { mode.set(combo.getValue()); if (Boolean.TRUE.equals(liveTransformation.get())) transform(); });
        bar.getChildren().add(combo); panel.setTop(bar); super.buildUi(panel);
    }
    @Override protected String getTransformLabel() { return "Convert"; }
    @Override protected String doTransform(String input) {
        return switch (mode.get()) {
            case "UPPER CASE" -> input.toUpperCase(); case "lower case" -> input.toLowerCase();
            case "Title Case" -> toTitleCase(input); case "camelCase" -> toCamelCase(input);
            case "snake_case" -> toSnakeCase(input); case "kebab-case" -> toKebabCase(input);
            case "PascalCase" -> toPascalCase(input); case "CONSTANT_CASE" -> toConstantCase(input);
            default -> input; }; }

    public static String toTitleCase(String input) {
        var sb = new StringBuilder();
        boolean nextUpper = true;
        for (char c : input.toCharArray()) {
            if (Character.isWhitespace(c)) { nextUpper = true; sb.append(c); }
            else { sb.append(nextUpper ? Character.toUpperCase(c) : Character.toLowerCase(c)); nextUpper = false; }
        }
        return sb.toString();
    }
    public static String toCamelCase(String input) {
        String[] words = input.split("[\\s_-]+");
        var sb = new StringBuilder();
        for (int i = 0; i < words.length; i++) {
            if (words[i].isEmpty()) continue;
            sb.append(i == 0 ? words[i].toLowerCase() : Character.toUpperCase(words[i].charAt(0)) + words[i].substring(1).toLowerCase());
        }
        return sb.toString();
    }
    public static String toSnakeCase(String input) {
        return Arrays.stream(input.split("[\\s_-]+")).filter(w -> !w.isEmpty()).map(String::toLowerCase).collect(Collectors.joining("_"));
    }
    public static String toKebabCase(String input) {
        return Arrays.stream(input.split("[\\s_-]+")).filter(w -> !w.isEmpty()).map(String::toLowerCase).collect(Collectors.joining("-"));
    }
    public static String toPascalCase(String input) {
        String[] words = input.split("[\\s_-]+");
        var sb = new StringBuilder();
        for (String word : words) { if (!word.isEmpty()) sb.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1).toLowerCase()); }
        return sb.toString();
    }
    public static String toConstantCase(String input) {
        return Arrays.stream(input.split("[\\s_-]+")).filter(w -> !w.isEmpty()).map(String::toUpperCase).collect(Collectors.joining("_"));
    }

    public static final class Factory implements ToolFxFactory<TextCaseToolFx> {
        public Factory() {} @Override public String getId() { return "text-case-transformer"; }
        @Override public ToolPresentation getPresentation() {
            return ToolPresentation.of("text-case-transformer", "Text Case", "Text Case Transformer").withGroupId("text"); }
        @Override public TextCaseToolFx create(ToolConfiguration c) { return new TextCaseToolFx(c); }
    }
}
