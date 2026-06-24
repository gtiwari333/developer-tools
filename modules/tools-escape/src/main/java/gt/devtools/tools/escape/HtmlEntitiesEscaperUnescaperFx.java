package gt.devtools.tools.escape;

import gt.devtools.settings.ToolConfiguration;
import gt.devtools.tools.api.ToolPresentation;
import gt.devtools.tools.api.fx.EscaperUnescaperFx;
import gt.devtools.tools.api.fx.ToolFxFactory;

import java.nio.charset.StandardCharsets;

/** JavaFX version of HTML Entities escaper. */
public final class HtmlEntitiesEscaperUnescaperFx extends EscaperUnescaperFx {

    private HtmlEntitiesEscaperUnescaperFx(ToolConfiguration config) { super(config); }

    @Override protected byte[] doConvertForward(byte[] input) {
        return HtmlEntitiesEscaperUnescaper.escape(
                new String(input, StandardCharsets.UTF_8)).getBytes(StandardCharsets.UTF_8);
    }

    @Override protected byte[] doConvertBackward(byte[] input) {
        return HtmlEntitiesEscaperUnescaper.unescape(
                new String(input, StandardCharsets.UTF_8)).getBytes(StandardCharsets.UTF_8);
    }

    public static final class Factory implements ToolFxFactory<HtmlEntitiesEscaperUnescaperFx> {
        public Factory() {}
        @Override public String getId() { return "html-entities-escape-fx"; }
        @Override public ToolPresentation getPresentation() {
            return ToolPresentation.of("html-entities-escape-fx",
                    "HTML Entities (FX)", "HTML Entities Escape / Unescape")
                    .withGroupId("escape");
        }
        @Override public HtmlEntitiesEscaperUnescaperFx create(ToolConfiguration config) {
            return new HtmlEntitiesEscaperUnescaperFx(config);
        }
    }
}
