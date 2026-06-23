package gt.devtools.tools.formatters;

import gt.devtools.tools.api.ToolFactory;
import gt.devtools.tools.api.ToolProvider;

import java.util.List;

/** Registers formatting tools. */
public final class FormattersToolProvider implements ToolProvider {
    public FormattersToolProvider() {}

    @Override public String getName() { return "Formatters"; }

    @Override
    public List<ToolFactory<?>> getTools() {
        return List.of(
                new SqlFormatterTool.Factory(),
                new CliCommandConverterTool.Factory()
        );
    }
}
