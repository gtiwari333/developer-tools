package gt.devtools.tools.escape;

import gt.devtools.settings.ToolConfiguration;
import gt.devtools.tools.api.ToolPresentation;
import gt.devtools.tools.api.fx.EscaperUnescaperFx;
import gt.devtools.tools.api.fx.ToolFxFactory;
import org.apache.commons.text.StringEscapeUtils;

import java.nio.charset.StandardCharsets;

/** JavaFX version of HTML Entities escaper. */
public final class HtmlEntitiesEscaperUnescaperFx extends EscaperUnescaperFx {

    private HtmlEntitiesEscaperUnescaperFx(ToolConfiguration config) { super(config); }

    @Override protected byte[] doConvertForward(byte[] input) {
        return StringEscapeUtils.escapeHtml4(
                new String(input, StandardCharsets.UTF_8)).getBytes(StandardCharsets.UTF_8);
    }

    @Override protected byte[] doConvertBackward(byte[] input) {
        return StringEscapeUtils.unescapeHtml4(
                new String(input, StandardCharsets.UTF_8)).getBytes(StandardCharsets.UTF_8);
    }

    public static String escape(String input) { return StringEscapeUtils.escapeHtml4(input); }
    public static String unescape(String input) { return StringEscapeUtils.unescapeHtml4(input); }

    public static final class Factory implements ToolFxFactory<HtmlEntitiesEscaperUnescaperFx> {
        public Factory() {}
        @Override public String getId() { return "html-entities-escape"; }
        @Override public ToolPresentation getPresentation() {
            return ToolPresentation.of("html-entities-escape",
                    "HTML Entities", "HTML Entities Escape / Unescape")
                    .withGroupId("escape");
        }
        @Override public HtmlEntitiesEscaperUnescaperFx create(ToolConfiguration config) {
            return new HtmlEntitiesEscaperUnescaperFx(config);
        }
    }
}
