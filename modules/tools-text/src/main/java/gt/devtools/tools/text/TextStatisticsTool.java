package gt.devtools.tools.text;

import gt.devtools.settings.ToolConfiguration;
import gt.devtools.tools.api.ToolFactory;
import gt.devtools.tools.api.ToolPresentation;
import gt.devtools.tools.api.text.TextTransformer;

import javax.swing.*;
import java.awt.*;
import java.nio.charset.StandardCharsets;

/**
 * Displays text statistics: character/word/line/byte counts, unique words,
 * frequency analysis. Shows results in the output panel as formatted text.
 */
public final class TextStatisticsTool extends TextTransformer {

    private TextStatisticsTool(ToolConfiguration config) {
        super(config);
    }

    // -- public value type (testable without Swing)

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
        int uniqueWords = java.util.Arrays.stream(words).collect(
                java.util.stream.Collectors.toCollection(java.util.LinkedHashSet::new)).size();

        return new TextStats(chars, charsNoSpaces, wordCount, uniqueWords, lineCount, byteCount);
    }

    @Override
    protected void buildUi(JPanel panel) {
        var configBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        configBar.add(new JLabel("Live statistics as you type."));
        panel.add(configBar, BorderLayout.NORTH);
        super.buildUi(panel);
    }

    @Override
    protected String getTransformLabel() { return "Stats"; }

    @Override
    protected String doTransform(String input) {
        if (input.isEmpty()) return "Enter text on the left to see statistics.";
        return computeStats(input).format();
    }

    public static final class Factory implements ToolFactory<TextStatisticsTool> {
        public Factory() {}
        @Override public String getId() { return "text-statistic"; }
        @Override public ToolPresentation getPresentation() {
            return ToolPresentation.of("text-statistic",
                    "Text Statistics", "Text Statistics").withGroupId("text");
        }
        @Override public TextStatisticsTool create(ToolConfiguration config) { return new TextStatisticsTool(config); }
    }
}
