package gt.devtools.tools.text;

import gt.devtools.settings.ToolConfiguration;
import gt.devtools.tools.api.ToolPresentation;
import gt.devtools.tools.api.fx.TextTransformerFx;
import gt.devtools.tools.api.fx.ToolFxFactory;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.stream.Collectors;

/**
 * JavaFX version: Displays text statistics: character/word/line/byte counts,
 * unique words, frequency analysis.
 */
public final class TextStatisticsToolFx extends TextTransformerFx {

    private TextStatisticsToolFx(ToolConfiguration config) {
        super(config);
    }

    /** Immutable snapshot of text statistics. */
    public record TextStats(int charCount, int charCountNoSpaces, int wordCount,
                            int uniqueWordCount, int lineCount, int byteCountUtf8) {

        /** Formats the statistics as a human-readable report. */
        public String format() {
            return """
                   Character count (with spaces):  %,d
                   Character count (no spaces):   %,d
                   Word count:                    %,d
                   Unique words:                  %,d
                   Line count:                    %,d
                   Byte count (UTF-8):            %,d
                   """.formatted(charCount, charCountNoSpaces, wordCount,
                    uniqueWordCount, lineCount, byteCountUtf8);
        }
    }

    /** Computes text statistics from the given input string. */
    public static TextStats computeStats(String input) {
        if (input.isEmpty()) return null;

        int chars = input.length();
        int charsNoSpaces = input.replaceAll("\\s", "").length();
        String[] lines = input.split("\n", -1);
        int lineCount = lines.length;
        String[] words = input.trim().split("\\s+");
        int wordCount = input.trim().isEmpty() ? 0 : words.length;
        int byteCount = input.getBytes(StandardCharsets.UTF_8).length;
        int uniqueWords = Arrays.stream(words)
                .collect(Collectors.toCollection(LinkedHashSet::new)).size();

        return new TextStats(chars, charsNoSpaces, wordCount, uniqueWords, lineCount, byteCount);
    }

    @Override
    protected void buildUi(BorderPane panel) {
        var configBar = new HBox(8);
        configBar.setStyle("-fx-padding: 4 0;");
        configBar.getChildren().add(new Label("Live statistics as you type."));
        panel.setTop(configBar);
        super.buildUi(panel);
    }

    @Override
    protected String getTransformLabel() { return "Stats"; }

    @Override
    protected String doTransform(String input) {
        if (input.isEmpty()) return "Enter text on the left to see statistics.";
        var stats = computeStats(input);
        return stats != null ? stats.format() : "Enter text on the left to see statistics.";
    }

    public static final class Factory implements ToolFxFactory<TextStatisticsToolFx> {
        public Factory() {}
        @Override public String getId() { return "text-statistic"; }
        @Override public ToolPresentation getPresentation() {
            return ToolPresentation.of("text-statistic",
                    "Text Statistics", "Text Statistics").withGroupId("text");
        }
        @Override public TextStatisticsToolFx create(ToolConfiguration config) { return new TextStatisticsToolFx(config); }
    }
}
