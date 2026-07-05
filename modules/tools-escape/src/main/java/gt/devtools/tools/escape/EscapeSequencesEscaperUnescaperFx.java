package gt.devtools.tools.escape;

import gt.devtools.settings.ToolConfiguration;
import gt.devtools.tools.api.ToolPresentation;
import gt.devtools.tools.api.fx.EscaperUnescaperFx;
import gt.devtools.tools.api.fx.ToolFxFactory;

import java.nio.charset.StandardCharsets;

/** JavaFX version of escape sequences escaper (\\n \\t \\r etc.). */
public final class EscapeSequencesEscaperUnescaperFx extends EscaperUnescaperFx {
    private EscapeSequencesEscaperUnescaperFx(ToolConfiguration config) { super(config); }
    @Override protected byte[] doConvertForward(byte[] input) {
        return escape(new String(input, StandardCharsets.UTF_8)).getBytes(StandardCharsets.UTF_8); }
    @Override protected byte[] doConvertBackward(byte[] input) {
        return unescape(new String(input, StandardCharsets.UTF_8)).getBytes(StandardCharsets.UTF_8); }

    /** Escape special characters to their backslash representations. */
    public static String escape(String input) {
        // Backslash must be escaped first to avoid double-escaping
        var sb = new StringBuilder();
        for (char c : input.toCharArray()) {
            switch (c) {
                case '\\' -> sb.append("\\\\");
                case '\n' -> sb.append("\\n");
                case '\t'  -> sb.append("\\t");
                case '\r'  -> sb.append("\\r");
                case '"'  -> sb.append("\\\"");
                case '\0' -> sb.append("\\0");
                default -> sb.append(c);
            }
        }
        return sb.toString();
    }

    /** Unescape backslash sequences back to their original characters. */
    public static String unescape(String input) {
        var sb = new StringBuilder();
        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            if (c == '\\' && i + 1 < input.length()) {
                char next = input.charAt(i + 1);
                switch (next) {
                    case 'n'  -> { sb.append('\n'); i++; }
                    case 't'  -> { sb.append('\t'); i++; }
                    case 'r'  -> { sb.append('\r'); i++; }
                    case '\\' -> { sb.append('\\'); i++; }
                    case '"'  -> { sb.append('"'); i++; }
                    case '0'  -> { sb.append('\0'); i++; }
                    default -> sb.append(c); // unknown escape preserved as-is
                }
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    public static final class Factory implements ToolFxFactory<EscapeSequencesEscaperUnescaperFx> {
        public Factory() {} @Override public String getId() { return "escape-sequence-escaper-unescaper"; }
        @Override public ToolPresentation getPresentation() {
            return ToolPresentation.of("escape-sequence-escaper-unescaper", "Escape Sequences", "Escape Sequences Escaper / Unescaper").withGroupId("escape"); }
        @Override public EscapeSequencesEscaperUnescaperFx create(ToolConfiguration c) { return new EscapeSequencesEscaperUnescaperFx(c); }
    }
}
