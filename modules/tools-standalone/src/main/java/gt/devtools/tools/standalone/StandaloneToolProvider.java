package gt.devtools.tools.standalone;

import gt.devtools.tools.api.ToolProvider;
import gt.devtools.tools.api.fx.ToolFxFactory;

import java.util.List;

/** Registers standalone tools (no group). */
public final class StandaloneToolProvider implements ToolProvider {
    public StandaloneToolProvider() {}

    @Override public String getName() { return "Standalone Tools"; }

    @Override
    public List<ToolFxFactory<?>> getTools() {
        return List.of(
                new ConfigFormatConverterFx.Factory(),
                new RegexMatcherToolFx.Factory(),
                new JsonPathToolFx.Factory(),
                new DatetimeConverterToolFx.Factory(),
                new CronExpressionToolFx.Factory(),
                new ColorPickerToolFx.Factory(),
                new QrCodeGeneratorToolFx.Factory(),
                new AsciiArtToolFx.Factory(),
                new UnitConverterToolFx.Factory(),
                new ServerCertificatesToolFx.Factory(),
                new ArchiveInspectorToolFx.Factory(),
                new HttpServerToolFx.Factory(),
                new NotesToolFx.Factory(),
                new RubberDuckToolFx.Factory()
        );
    }
}
