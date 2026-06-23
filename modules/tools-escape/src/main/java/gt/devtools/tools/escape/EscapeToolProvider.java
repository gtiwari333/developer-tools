package gt.devtools.tools.escape;

import gt.devtools.tools.api.ToolFactory;
import gt.devtools.tools.api.ToolProvider;

import java.util.List;

/** Registers all escape/unescape tools. */
public final class EscapeToolProvider implements ToolProvider {
    public EscapeToolProvider() {}

    @Override public String getName() { return "Text Escape"; }

    @Override
    public List<ToolFactory<?>> getTools() {
        return List.of(
                new HtmlEntitiesEscaperUnescaper.Factory(),
                new JavaStringEscaperUnescaper.Factory(),
                new JsonTextEscaperUnescaper.Factory(),
                new CsvTextEscaperUnescaper.Factory(),
                new XmlTextEscaperUnescaper.Factory(),
                new EscapeSequencesEscaperUnescaper.Factory()
        );
    }
}
