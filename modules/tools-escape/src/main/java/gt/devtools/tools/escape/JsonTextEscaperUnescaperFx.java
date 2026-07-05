package gt.devtools.tools.escape;

import gt.devtools.settings.ToolConfiguration;
import gt.devtools.tools.api.ToolPresentation;
import gt.devtools.tools.api.fx.EscaperUnescaperFx;
import gt.devtools.tools.api.fx.ToolFxFactory;
import org.apache.commons.text.StringEscapeUtils;

import java.nio.charset.StandardCharsets;

/** JavaFX version of JSON text escape/unescape. */
public final class JsonTextEscaperUnescaperFx extends EscaperUnescaperFx {
    private JsonTextEscaperUnescaperFx(ToolConfiguration config) { super(config); }
    @Override protected byte[] doConvertForward(byte[] input) {
        return StringEscapeUtils.escapeJson(new String(input, StandardCharsets.UTF_8)).getBytes(StandardCharsets.UTF_8); }
    @Override protected byte[] doConvertBackward(byte[] input) {
        return StringEscapeUtils.unescapeJson(new String(input, StandardCharsets.UTF_8)).getBytes(StandardCharsets.UTF_8); }

    public static String escape(String input) { return StringEscapeUtils.escapeJson(input); }
    public static String unescape(String input) { return StringEscapeUtils.unescapeJson(input); }

    public static final class Factory implements ToolFxFactory<JsonTextEscaperUnescaperFx> {
        public Factory() {} @Override public String getId() { return "json-text-escape"; }
        @Override public ToolPresentation getPresentation() {
            return ToolPresentation.of("json-text-escape", "JSON Escape", "JSON String Escape / Unescape").withGroupId("escape"); }
        @Override public JsonTextEscaperUnescaperFx create(ToolConfiguration c) { return new JsonTextEscaperUnescaperFx(c); }
    }
}
