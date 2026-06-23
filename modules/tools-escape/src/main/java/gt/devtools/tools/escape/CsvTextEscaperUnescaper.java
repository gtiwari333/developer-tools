package gt.devtools.tools.escape;

import gt.devtools.settings.ToolConfiguration;
import gt.devtools.tools.api.ToolFactory;
import gt.devtools.tools.api.ToolPresentation;
import gt.devtools.tools.api.converter.EscaperUnescaper;
import org.apache.commons.text.StringEscapeUtils;

import java.nio.charset.StandardCharsets;

/** Escape/unescape CSV field values. */
public final class CsvTextEscaperUnescaper extends EscaperUnescaper {

    private CsvTextEscaperUnescaper(ToolConfiguration config) { super(config); }

    @Override
    protected byte[] doConvertForward(byte[] input) {
        return StringEscapeUtils.escapeCsv(
                new String(input, StandardCharsets.UTF_8)).getBytes(StandardCharsets.UTF_8);
    }

    @Override
    protected byte[] doConvertBackward(byte[] input) {
        return StringEscapeUtils.unescapeCsv(
                new String(input, StandardCharsets.UTF_8)).getBytes(StandardCharsets.UTF_8);
    }

    public static final class Factory implements ToolFactory<CsvTextEscaperUnescaper> {
        public Factory() {}
        @Override public String getId() { return "csv-text-escape"; }
        @Override
        public ToolPresentation getPresentation() {
            return ToolPresentation.of("csv-text-escape",
                    "CSV Text Escape / Unescape", "CSV Text Escape / Unescape")
                    .withGroupId("escape");
        }
        @Override
        public CsvTextEscaperUnescaper create(ToolConfiguration config) {
            return new CsvTextEscaperUnescaper(config);
        }
    }
}
