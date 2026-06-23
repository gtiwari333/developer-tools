package gt.devtools.tools.text;

import gt.devtools.tools.api.ToolFactory;
import gt.devtools.tools.api.ToolProvider;

import java.util.List;

/** Registers all text utility tools. */
public final class TextToolProvider implements ToolProvider {
    public TextToolProvider() {}

    @Override public String getName() { return "Text Utilities"; }

    @Override
    public List<ToolFactory<?>> getTools() {
        return List.of(
                new HashingTool.Factory(),
                new HmacTool.Factory(),
                new TextSortingTool.Factory(),
                new TextCaseTool.Factory(),
                new TextFilterTool.Factory(),
                new TextStatisticsTool.Factory(),
                new TextDiffTool.Factory()
        );
    }
}
