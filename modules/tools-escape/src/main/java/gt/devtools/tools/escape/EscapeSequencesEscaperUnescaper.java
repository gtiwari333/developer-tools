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

    // -- public static conversion methods (testable without Swing)

    /**
     * Escapes special characters to their escape-sequence representation.
     * Maps: \ → \\, " → \", newline → \n, CR → \r, tab → \t, NUL → \0.
     */
    public static String escape(String input) {
        return input
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t")
                .replace("\0", "\\0");
    }

    /**
     * Unescapes escape sequences back to literal characters.
     * Recognises: \\, \", \n, \t, \r, \0.
     * Unknown escape sequences are left as-is.
     */
    public static String unescape(String input) {
        StringBuilder out = new StringBuilder(input.length());
        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            if (c == '\\' && i + 1 < input.length()) {
                char next = input.charAt(++i);
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
        return out.toString();
    }

    @Override
    protected byte[] doConvertForward(byte[] input) {
        return escape(new String(input, StandardCharsets.UTF_8))
                .getBytes(StandardCharsets.UTF_8);
    }

    @Override
    protected byte[] doConvertBackward(byte[] input) {
        return unescape(new String(input, StandardCharsets.UTF_8))
                .getBytes(StandardCharsets.UTF_8);
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
