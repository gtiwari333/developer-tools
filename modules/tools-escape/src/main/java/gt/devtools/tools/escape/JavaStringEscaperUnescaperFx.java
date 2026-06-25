package gt.devtools.tools.escape;

import gt.devtools.settings.ToolConfiguration;
import gt.devtools.tools.api.ToolPresentation;
import gt.devtools.tools.api.fx.EscaperUnescaperFx;
import gt.devtools.tools.api.fx.ToolFxFactory;
import org.apache.commons.text.StringEscapeUtils;

import java.nio.charset.StandardCharsets;

/** JavaFX version of Java string escape/unescape (\\n \\t \\uXXXX etc.). */
public final class JavaStringEscaperUnescaperFx extends EscaperUnescaperFx {

    private JavaStringEscaperUnescaperFx(ToolConfiguration config) { super(config); }

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

    public static String escape(String input) {
        return StringEscapeUtils.escapeJava(input);
    }

    public static String unescape(String input) {
        return StringEscapeUtils.unescapeJava(input);
    }

    public static final class Factory implements ToolFxFactory<JavaStringEscaperUnescaperFx> {
        public Factory() {}
        @Override public String getId() { return "java-text-escape"; }
        @Override public ToolPresentation getPresentation() {
            return ToolPresentation.of("java-text-escape",
                    "Java String Escape / Unescape", "Java String Escape / Unescape")
                    .withGroupId("escape");
        }
        @Override
        public JavaStringEscaperUnescaperFx create(ToolConfiguration config) {
            return new JavaStringEscaperUnescaperFx(config);
        }
    }
}
