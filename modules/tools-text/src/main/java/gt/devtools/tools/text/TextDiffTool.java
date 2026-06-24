package gt.devtools.tools.text;

import gt.devtools.settings.ToolConfiguration;
import gt.devtools.tools.api.ToolFactory;
import gt.devtools.tools.api.ToolPresentation;
import gt.devtools.tools.api.text.TextTransformer;

import javax.swing.*;
import java.awt.*;

/** Computes a unified diff between two text blocks. */
public final class TextDiffTool extends TextTransformer {

    private TextDiffTool(ToolConfiguration config) { super(config); }

    @Override
    protected void buildUi(JPanel panel) {
        super.buildUi(panel);
    }

    @Override
    protected String getTransformLabel() { return "Diff"; }

    @Override
    protected String doTransform(String input) {
        if (input.isBlank()) return "Paste original text, then ---, then revised text.";
        String[] parts = input.split("\n---\n", 2);
        if (parts.length < 2) return "Use --- on its own line to separate original and revised text.";
        return diff(parts[0], parts[1]);
    }

    // -- public static method (testable without Swing)

    /**
     * Computes a unified diff between two text blocks.
     * Returns a human-readable diff or "(no differences)" if identical.
     */
    public static String diff(String original, String revised) {
        var originalLines = java.util.Arrays.asList(original.split("\n", -1));
        var revisedLines = java.util.Arrays.asList(revised.split("\n", -1));
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

    public static final class Factory implements ToolFactory<TextDiffTool> {
        public Factory() {}
        @Override public String getId() { return "text-diff"; }
        @Override public ToolPresentation getPresentation() {
            return ToolPresentation.of("text-diff", "Text Diff", "Text Diff Viewer").withGroupId("text");
        }
        @Override public TextDiffTool create(ToolConfiguration config) { return new TextDiffTool(config); }
    }
}
