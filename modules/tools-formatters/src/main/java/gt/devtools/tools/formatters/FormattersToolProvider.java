package gt.devtools.tools.formatters;

import gt.devtools.tools.api.ToolFactory;
import gt.devtools.tools.api.ToolProvider;

import java.util.List;

public final class FormattersToolProvider implements ToolProvider {
    public FormattersToolProvider() {}
    @Override public String getName() { return "Formatters"; }
    @Override public List<ToolFactory<?>> getTools() { return List.of(); }
}
