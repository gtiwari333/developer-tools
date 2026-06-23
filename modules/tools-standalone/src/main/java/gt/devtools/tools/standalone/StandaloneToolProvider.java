package gt.devtools.tools.standalone;

import gt.devtools.tools.api.ToolFactory;
import gt.devtools.tools.api.ToolProvider;

import java.util.List;

public final class StandaloneToolProvider implements ToolProvider {
    public StandaloneToolProvider() {}
    @Override public String getName() { return "Standalone Tools"; }
    @Override public List<ToolFactory<?>> getTools() { return List.of(); }
}
