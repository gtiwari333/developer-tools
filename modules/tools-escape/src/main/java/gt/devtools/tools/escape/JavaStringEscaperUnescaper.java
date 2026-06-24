package gt.devtools.tools.escape;

import gt.devtools.settings.ToolConfiguration;
import gt.devtools.tools.api.ToolFactory;
import gt.devtools.tools.api.ToolPresentation;
import gt.devtools.tools.api.converter.EscaperUnescaper;
import org.apache.commons.text.StringEscapeUtils;

import java.nio.charset.StandardCharsets;

/** Escape/unescape Java string literals (\\n \\t \\uXXXX etc.). */
public final class JavaStringEscaperUnescaper extends EscaperUnescaper {

    private JavaStringEscaperUnescaper(ToolConfiguration config) { super(config); }

    @Override
    protected byte[] doConvertForward(byte[] input) {
        return escape(new String(input, java.nio.charset.StandardCharsets.UTF_8))
                .getBytes(java.nio.charset.StandardCharsets.UTF_8);
    }

    @Override
    protected byte[] doConvertBackward(byte[] input) {
        return unescape(new String(input, java.nio.charset.StandardCharsets.UTF_8))
                .getBytes(java.nio.charset.StandardCharsets.UTF_8);
    }

    public static String escape(String input) {
        return org.apache.commons.text.StringEscapeUtils.escapeJava(input);
    }
    public static String unescape(String input) {
        return org.apache.commons.text.StringEscapeUtils.unescapeJava(input);
    }

    public static final class Factory implements ToolFactory<JavaStringEscaperUnescaper> {
        public Factory() {}
        @Override public String getId() { return "java-text-escape"; }
        @Override
        public ToolPresentation getPresentation() {
            return ToolPresentation.of("java-text-escape",
                    "Java String Escape / Unescape", "Java String Escape / Unescape")
                    .withGroupId("escape");
        }
        @Override
        public JavaStringEscaperUnescaper create(ToolConfiguration config) {
            return new JavaStringEscaperUnescaper(config);
        }
    }
}
