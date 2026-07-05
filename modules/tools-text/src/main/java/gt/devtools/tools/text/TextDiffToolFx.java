package gt.devtools.tools.text;

import gt.devtools.settings.ToolConfiguration;
import gt.devtools.tools.api.ToolPresentation;
import gt.devtools.tools.api.fx.TextTransformerFx;
import gt.devtools.tools.api.fx.ToolFxFactory;

import java.util.Arrays;

/**
 * JavaFX version: Computes a unified diff between two text blocks.
 * Input: original text, then "---" on its own line, then revised text.
 */
public final class TextDiffToolFx extends TextTransformerFx {

    private TextDiffToolFx(ToolConfiguration config) { super(config); }

    @Override
    protected String getTransformLabel() { return "Diff"; }

    @Override
    protected String doTransform(String input) {
        if (input.isBlank()) return "Paste original text, then ---, then revised text.";
        String[] parts = input.split("\n---\n", 2);
        if (parts.length < 2) return "Use --- on its own line to separate original and revised text.";
        return diff(parts[0], parts[1]);
    }

    /** Computes a unified diff between two text blocks. */
    public static String diff(String original, String revised) {
        var originalLines = Arrays.asList(original.split("\n", -1));
        var revisedLines = Arrays.asList(revised.split("\n", -1));
        com.github.difflib.patch.Patch<String> patch =
                com.github.difflib.DiffUtils.diff(originalLines, revisedLines);

        if (patch.getDeltas().isEmpty()) return "(no differences)";

        var sb = new StringBuilder();
        sb.append("--- original\n+++ revised\n@@ diff summary @@\n");
        for (com.github.difflib.patch.AbstractDelta<String> delta : patch.getDeltas()) {
            for (String line : delta.getSource().getLines()) {
                sb.append("- ").append(line).append("\n");
            }
            for (String line : delta.getTarget().getLines()) {
                sb.append("+ ").append(line).append("\n");
            }
        }
        return sb.toString();
    }

    public static final class Factory implements ToolFxFactory<TextDiffToolFx> {
        public Factory() {}
        @Override public String getId() { return "text-diff"; }
        @Override public ToolPresentation getPresentation() {
            return ToolPresentation.of("text-diff", "Text Diff", "Text Diff Viewer").withGroupId("text");
        }
        @Override public TextDiffToolFx create(ToolConfiguration config) { return new TextDiffToolFx(config); }
    }
}
