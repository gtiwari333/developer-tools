package gt.devtools.tools.formatters;

import gt.devtools.tools.api.ToolProvider;
import gt.devtools.tools.api.fx.ToolFxFactory;

import java.util.List;

/** Registers formatting tools. */
public final class FormattersToolProvider implements ToolProvider {
    public FormattersToolProvider() {}

    @Override public String getName() { return "Formatters"; }

    @Override
    public List<ToolFxFactory<?>> getTools() {
        return List.of(
                new SqlFormatterToolFx.Factory(),
                new CliCommandConverterToolFx.Factory()
        );
    }
}
