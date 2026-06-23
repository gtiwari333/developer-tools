package gt.devtools.tools.text;

import com.github.difflib.DiffUtils;
import com.github.difflib.patch.AbstractDelta;
import com.github.difflib.patch.Patch;
import gt.devtools.settings.ToolConfiguration;
import gt.devtools.tools.api.ToolFactory;
import gt.devtools.tools.api.ToolPresentation;
import gt.devtools.tools.api.text.TextTransformer;

import javax.swing.*;
import java.awt.*;
import java.util.Arrays;
import java.util.List;

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

        List<String> original = Arrays.asList(parts[0].split("\n", -1));
        List<String> revised = Arrays.asList(parts[1].split("\n", -1));
        Patch<String> patch = DiffUtils.diff(original, revised);

        if (patch.getDeltas().isEmpty()) return "(no differences)";

        var sb = new StringBuilder();
        sb.append("--- original\n+++ revised\n@@ diff summary @@\n");
        for (AbstractDelta<String> delta : patch.getDeltas()) {
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
