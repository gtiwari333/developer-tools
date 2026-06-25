package gt.devtools.tools.text;

import gt.devtools.tools.api.ToolProvider;
import gt.devtools.tools.api.fx.ToolFxFactory;

import java.util.List;

/** Registers all text utility tools. */
public final class TextToolProvider implements ToolProvider {
    public TextToolProvider() {}

    @Override public String getName() { return "Text Utilities"; }

    @Override
    public List<ToolFxFactory<?>> getTools() {
        return List.of(
                new HashingToolFx.Factory(),
                new HmacToolFx.Factory(),
                new TextSortingToolFx.Factory(),
                new TextCaseToolFx.Factory(),
                new TextFilterToolFx.Factory(),
                new TextStatisticsToolFx.Factory(),
                new TextDiffToolFx.Factory()
        );
    }
}
