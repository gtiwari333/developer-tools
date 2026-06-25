package gt.devtools.tools.escape;

import gt.devtools.tools.api.ToolProvider;
import gt.devtools.tools.api.fx.ToolFxFactory;

import java.util.List;

/** Registers all escape/unescape tools. */
public final class EscapeToolProvider implements ToolProvider {
    public EscapeToolProvider() {}

    @Override public String getName() { return "Text Escape"; }

    @Override
    public List<ToolFxFactory<?>> getTools() {
        return List.of(
                new HtmlEntitiesEscaperUnescaperFx.Factory(),
                new JavaStringEscaperUnescaperFx.Factory(),
                new JsonTextEscaperUnescaperFx.Factory(),
                new CsvTextEscaperUnescaperFx.Factory(),
                new XmlTextEscaperUnescaperFx.Factory(),
                new EscapeSequencesEscaperUnescaperFx.Factory()
        );
    }
}
