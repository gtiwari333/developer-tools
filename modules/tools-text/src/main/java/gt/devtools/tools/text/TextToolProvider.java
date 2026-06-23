package gt.devtools.tools.text;

import gt.devtools.tools.api.ToolFactory;
import gt.devtools.tools.api.ToolProvider;

import java.util.List;

public final class TextToolProvider implements ToolProvider {
    public TextToolProvider() {}
    @Override public String getName() { return "Text Utilities"; }
    @Override public List<ToolFactory<?>> getTools() { return List.of(); }
}
