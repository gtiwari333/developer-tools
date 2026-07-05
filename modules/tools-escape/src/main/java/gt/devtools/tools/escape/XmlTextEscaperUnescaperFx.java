package gt.devtools.tools.escape;

import gt.devtools.settings.ToolConfiguration;
import gt.devtools.tools.api.ToolPresentation;
import gt.devtools.tools.api.fx.EscaperUnescaperFx;
import gt.devtools.tools.api.fx.ToolFxFactory;
import org.apache.commons.text.StringEscapeUtils;

import java.nio.charset.StandardCharsets;

/** JavaFX version of XML text escape/unescape. */
public final class XmlTextEscaperUnescaperFx extends EscaperUnescaperFx {

    private XmlTextEscaperUnescaperFx(ToolConfiguration config) { super(config); }

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
        return StringEscapeUtils.escapeXml10(input);
    }

    public static String unescape(String input) {
        return StringEscapeUtils.unescapeXml(input);
    }

    public static final class Factory implements ToolFxFactory<XmlTextEscaperUnescaperFx> {
        public Factory() {}
        @Override public String getId() { return "xml-text-escape"; }
        @Override public ToolPresentation getPresentation() {
            return ToolPresentation.of("xml-text-escape",
                    "XML Text Escape / Unescape", "XML Text Escape / Unescape")
                    .withGroupId("escape");
        }
        @Override
        public XmlTextEscaperUnescaperFx create(ToolConfiguration config) {
            return new XmlTextEscaperUnescaperFx(config);
        }
    }
}
