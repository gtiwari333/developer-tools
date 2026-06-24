package gt.devtools.tools.escape;

import gt.devtools.settings.ToolConfiguration;
import gt.devtools.tools.api.ToolFactory;
import gt.devtools.tools.api.ToolPresentation;
import gt.devtools.tools.api.converter.EscaperUnescaper;
import org.apache.commons.text.StringEscapeUtils;

import java.nio.charset.StandardCharsets;

/** Escape/unescape XML entities. */
public final class XmlTextEscaperUnescaper extends EscaperUnescaper {

    private XmlTextEscaperUnescaper(ToolConfiguration config) { super(config); }

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
        return org.apache.commons.text.StringEscapeUtils.escapeXml10(input);
    }
    public static String unescape(String input) {
        return org.apache.commons.text.StringEscapeUtils.unescapeXml(input);
    }

    public static final class Factory implements ToolFactory<XmlTextEscaperUnescaper> {
        public Factory() {}
        @Override public String getId() { return "xml-text-escape"; }
        @Override
        public ToolPresentation getPresentation() {
            return ToolPresentation.of("xml-text-escape",
                    "XML Text Escape / Unescape", "XML Text Escape / Unescape")
                    .withGroupId("escape");
        }
        @Override
        public XmlTextEscaperUnescaper create(ToolConfiguration config) {
            return new XmlTextEscaperUnescaper(config);
        }
    }
}
