package gt.devtools.tools.text;

import gt.devtools.common.ValueProperty;
import gt.devtools.settings.ToolConfiguration;
import gt.devtools.tools.api.ToolPresentation;
import gt.devtools.tools.api.fx.TextTransformerFx;
import gt.devtools.tools.api.fx.ToolFxFactory;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public final class TextFilterToolFx extends TextTransformerFx {
    private final ValueProperty<String> mode, pattern;
    private TextFilterToolFx(ToolConfiguration config) {
        super(config); mode = registerConfig("filterMode", "Include matching"); pattern = registerConfig("filterPattern", ""); }
    @Override protected void buildUi(BorderPane panel) {
        var bar = new HBox(8); bar.getChildren().add(new Label("Mode:"));
        var combo = new ComboBox<String>(); combo.getItems().addAll("Include matching", "Exclude matching", "Unique lines", "Trim whitespace");
        combo.setValue(mode.get()); combo.setOnAction(e -> { mode.set(combo.getValue()); if (Boolean.TRUE.equals(liveTransformation.get())) transform(); });
        bar.getChildren().add(combo); bar.getChildren().add(new Label("Pattern:"));
        var f = new TextField(pattern.get()); f.focusedProperty().addListener((o, w, foc) -> { if (!foc) pattern.set(f.getText()); });
        bar.getChildren().add(f); panel.setTop(bar); super.buildUi(panel);
    }
    @Override protected String getTransformLabel() { return "Filter"; }
    @Override protected String doTransform(String input) {
        return switch (mode.get()) {
            case "Include matching" -> filterInclude(input, pattern.get());
            case "Exclude matching" -> filterExclude(input, pattern.get());
            case "Unique lines" -> filterUnique(input);
            case "Trim whitespace" -> trimLines(input);
            default -> input; }; }

    /** Keep only lines matching the regex. */
    public static String filterInclude(String input, String pattern) {
        if (pattern.isEmpty()) return input;
        var p = Pattern.compile(pattern);
        return Arrays.stream(input.split("\n")).filter(l -> p.matcher(l).find()).collect(Collectors.joining("\n"));
    }
    /** Remove lines matching the regex. */
    public static String filterExclude(String input, String pattern) {
        if (pattern.isEmpty()) return input;
        var p = Pattern.compile(pattern);
        return Arrays.stream(input.split("\n")).filter(l -> !p.matcher(l).find()).collect(Collectors.joining("\n"));
    }
    /** Deduplicate lines, preserving first occurrence order. */
    public static String filterUnique(String input) {
        var seen = new LinkedHashSet<String>();
        for (String line : input.split("\n")) seen.add(line);
        return String.join("\n", seen);
    }
    /** Trim leading/trailing whitespace from each line. */
    public static String trimLines(String input) {
        var result = Arrays.stream(input.split("\n", -1)).map(String::trim)
                .collect(Collectors.joining("\n"));
        // If all lines were whitespace-only, return empty
        return result.isBlank() ? "" : result;
    }

    public static final class Factory implements ToolFxFactory<TextFilterToolFx> {
        public Factory() {} @Override public String getId() { return "text-filter"; }
        @Override public ToolPresentation getPresentation() {
            return ToolPresentation.of("text-filter", "Text Filter", "Text Filter").withGroupId("text"); }
        @Override public TextFilterToolFx create(ToolConfiguration c) { return new TextFilterToolFx(c); }
    }
}
