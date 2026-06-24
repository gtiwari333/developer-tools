package gt.devtools.tools.escape;

import gt.devtools.settings.ToolConfiguration;
import gt.devtools.tools.api.ToolFactory;
import gt.devtools.tools.api.ToolPresentation;
import gt.devtools.tools.api.converter.EscaperUnescaper;
import org.apache.commons.text.StringEscapeUtils;

import java.nio.charset.StandardCharsets;

/** Escape/unescape JSON string values. */
public final class JsonTextEscaperUnescaper extends EscaperUnescaper {

    private JsonTextEscaperUnescaper(ToolConfiguration config) { super(config); }

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
        return org.apache.commons.text.StringEscapeUtils.escapeJson(input);
    }
    public static String unescape(String input) {
        return org.apache.commons.text.StringEscapeUtils.unescapeJson(input);
    }

    public static final class Factory implements ToolFactory<JsonTextEscaperUnescaper> {
        public Factory() {}
        @Override public String getId() { return "json-text-escape"; }
        @Override
        public ToolPresentation getPresentation() {
            return ToolPresentation.of("json-text-escape",
                    "JSON String Escape / Unescape", "JSON String Escape / Unescape")
                    .withGroupId("escape");
        }
        @Override
        public JsonTextEscaperUnescaper create(ToolConfiguration config) {
            return new JsonTextEscaperUnescaper(config);
        }
    }
}
