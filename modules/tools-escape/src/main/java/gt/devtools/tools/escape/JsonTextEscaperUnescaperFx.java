package gt.devtools.tools.escape;

import gt.devtools.settings.ToolConfiguration;
import gt.devtools.tools.api.ToolPresentation;
import gt.devtools.tools.api.fx.EscaperUnescaperFx;
import gt.devtools.tools.api.fx.ToolFxFactory;
import java.nio.charset.StandardCharsets;

public final class JsonTextEscaperUnescaperFx extends EscaperUnescaperFx {
    private JsonTextEscaperUnescaperFx(ToolConfiguration config) { super(config); }
    @Override protected byte[] doConvertForward(byte[] input) {
        return JsonTextEscaperUnescaper.escape(new String(input, StandardCharsets.UTF_8)).getBytes(StandardCharsets.UTF_8); }
    @Override protected byte[] doConvertBackward(byte[] input) {
        return JsonTextEscaperUnescaper.unescape(new String(input, StandardCharsets.UTF_8)).getBytes(StandardCharsets.UTF_8); }
    public static final class Factory implements ToolFxFactory<JsonTextEscaperUnescaperFx> {
        public Factory() {} @Override public String getId() { return "json-text-escape-fx"; }
        @Override public ToolPresentation getPresentation() {
            return ToolPresentation.of("json-text-escape-fx", "JSON Escape (FX)", "JSON String Escape / Unescape").withGroupId("escape"); }
        @Override public JsonTextEscaperUnescaperFx create(ToolConfiguration c) { return new JsonTextEscaperUnescaperFx(c); }
    }
}
