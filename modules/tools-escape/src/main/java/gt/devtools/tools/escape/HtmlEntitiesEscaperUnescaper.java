package gt.devtools.tools.escape;

import gt.devtools.settings.ToolConfiguration;
import gt.devtools.tools.api.ToolFactory;
import gt.devtools.tools.api.ToolPresentation;
import gt.devtools.tools.api.converter.EscaperUnescaper;
import org.apache.commons.text.StringEscapeUtils;

import java.nio.charset.StandardCharsets;

/** Escape/unescape HTML entities (&amp; &lt; &gt; etc.). */
public final class HtmlEntitiesEscaperUnescaper extends EscaperUnescaper {

    private HtmlEntitiesEscaperUnescaper(ToolConfiguration config) { super(config); }

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
        return org.apache.commons.text.StringEscapeUtils.escapeHtml4(input);
    }
    public static String unescape(String input) {
        return org.apache.commons.text.StringEscapeUtils.unescapeHtml4(input);
    }

    public static final class Factory implements ToolFactory<HtmlEntitiesEscaperUnescaper> {
        public Factory() {}
        @Override public String getId() { return "html-entities-escape"; }
        @Override
        public ToolPresentation getPresentation() {
            return ToolPresentation.of("html-entities-escape",
                    "HTML Entities Escape / Unescape", "HTML Entities Escape / Unescape")
                    .withGroupId("escape");
        }
        @Override
        public HtmlEntitiesEscaperUnescaper create(ToolConfiguration config) {
            return new HtmlEntitiesEscaperUnescaper(config);
        }
    }
}
