package gt.devtools.tools.escape;

import gt.devtools.settings.ToolConfiguration;
import gt.devtools.tools.api.ToolPresentation;
import gt.devtools.tools.api.fx.EscaperUnescaperFx;
import gt.devtools.tools.api.fx.ToolFxFactory;
import org.apache.commons.text.StringEscapeUtils;

import java.nio.charset.StandardCharsets;

/** JavaFX version of CSV text escape/unescape. */
public final class CsvTextEscaperUnescaperFx extends EscaperUnescaperFx {

    private CsvTextEscaperUnescaperFx(ToolConfiguration config) { super(config); }

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
        return StringEscapeUtils.escapeCsv(input);
    }

    public static String unescape(String input) {
        return StringEscapeUtils.unescapeCsv(input);
    }

    public static final class Factory implements ToolFxFactory<CsvTextEscaperUnescaperFx> {
        public Factory() {}
        @Override public String getId() { return "csv-text-escape"; }
        @Override public ToolPresentation getPresentation() {
            return ToolPresentation.of("csv-text-escape",
                    "CSV Text Escape / Unescape", "CSV Text Escape / Unescape")
                    .withGroupId("escape");
        }
        @Override
        public CsvTextEscaperUnescaperFx create(ToolConfiguration config) {
            return new CsvTextEscaperUnescaperFx(config);
        }
    }
}
