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
        return StringEscapeUtils.escapeJava(
                new String(input, StandardCharsets.UTF_8)).getBytes(StandardCharsets.UTF_8);
    }

    @Override
    protected byte[] doConvertBackward(byte[] input) {
        return StringEscapeUtils.unescapeJava(
                new String(input, StandardCharsets.UTF_8)).getBytes(StandardCharsets.UTF_8);
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
