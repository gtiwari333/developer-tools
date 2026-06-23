package gt.devtools.tools.escape;

import gt.devtools.tools.api.ToolFactory;
import gt.devtools.tools.api.ToolProvider;

import java.util.List;

public final class EscapeToolProvider implements ToolProvider {
    public EscapeToolProvider() {}
    @Override public String getName() { return "Text Escape"; }
    @Override public List<ToolFactory<?>> getTools() { return List.of(); }
}
