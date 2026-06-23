package gt.devtools.tools.standalone;

import gt.devtools.tools.api.ToolFactory;
import gt.devtools.tools.api.ToolProvider;

import java.util.List;

/** Registers standalone tools (no group). */
public final class StandaloneToolProvider implements ToolProvider {
    public StandaloneToolProvider() {}

    @Override public String getName() { return "Standalone Tools"; }

    @Override
    public List<ToolFactory<?>> getTools() {
        return List.of(
                new ConfigFormatConverter.Factory(),
                new RegexMatcherTool.Factory(),
                new JsonPathTool.Factory(),
                new DatetimeConverterTool.Factory(),
                new CronExpressionTool.Factory(),
                new ColorPickerTool.Factory(),
                new QrCodeGeneratorTool.Factory(),
                new AsciiArtTool.Factory(),
                new UnitConverterTool.Factory(),
                new ServerCertificatesTool.Factory(),
                new ArchiveInspectorTool.Factory(),
                new HttpServerTool.Factory(),
                new NotesTool.Factory(),
                new RubberDuckTool.Factory()
        );
    }
}
