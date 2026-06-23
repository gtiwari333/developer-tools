package gt.devtools.tools.escape;

import gt.devtools.settings.ToolConfiguration;
import gt.devtools.tools.api.ToolFactory;
import gt.devtools.tools.api.ToolPresentation;
import gt.devtools.tools.api.converter.EscaperUnescaper;

import java.nio.charset.StandardCharsets;

/**
 * Converts common escape sequences:
 * <ul>
 *   <li>\\n ↔ newline, \\t ↔ tab, \\r ↔ carriage return</li>
 *   <li>\\\" ↔ double quote, \\\\ ↔ backslash</li>
 *   <li>Custom: \\0 ↔ null byte</li>
 * </ul>
 */
public final class EscapeSequencesEscaperUnescaper extends EscaperUnescaper {

    private EscapeSequencesEscaperUnescaper(ToolConfiguration config) { super(config); }

    @Override
    protected byte[] doConvertForward(byte[] input) {
        // Escape: literal chars → escape sequences
        return new String(input, StandardCharsets.UTF_8)
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t")
                .replace("\0", "\\0")
                .getBytes(StandardCharsets.UTF_8);
    }

    @Override
    protected byte[] doConvertBackward(byte[] input) {
        // Unescape: escape sequences → literal chars
        String s = new String(input, StandardCharsets.UTF_8);
        StringBuilder out = new StringBuilder(s.length());
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '\\' && i + 1 < s.length()) {
                char next = s.charAt(++i);
                switch (next) {
                    case 'n'  -> out.append('\n');
                    case 't'  -> out.append('\t');
                    case 'r'  -> out.append('\r');
                    case '0'  -> out.append('\0');
                    case '"'  -> out.append('"');
                    case '\\' -> out.append('\\');
                    default   -> { out.append('\\'); out.append(next); }
                }
            } else {
                out.append(c);
            }
        }
        return out.toString().getBytes(StandardCharsets.UTF_8);
    }

    public static final class Factory implements ToolFactory<EscapeSequencesEscaperUnescaper> {
        public Factory() {}
        @Override public String getId() { return "escape-sequence-escaper-unescaper"; }
        @Override
        public ToolPresentation getPresentation() {
            return ToolPresentation.of("escape-sequence-escaper-unescaper",
                    "Escape Sequences", "Escape Sequences Escaper / Unescaper")
                    .withGroupId("escape")
                    .withDescription("Convert between literal characters and " +
                            "escape sequences (\\n, \\t, \\r, \\\", \\\\)");
        }
        @Override
        public EscapeSequencesEscaperUnescaper create(ToolConfiguration config) {
            return new EscapeSequencesEscaperUnescaper(config);
        }
    }
}
