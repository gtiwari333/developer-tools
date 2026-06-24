package gt.devtools.app;

import gt.devtools.settings.ToolConfiguration;
import gt.devtools.tools.api.fx.DeveloperToolFx;
import gt.devtools.tools.api.fx.ToolFxFactory;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Registry of JavaFX tool factories.
 * <p>
 * Phase 3: Manually registered pilot tools.
 * Future: ServiceLoader-based discovery via {@link ToolFxFactory} providers.
 */
public final class FxToolRegistry {

    private static final Map<String, ToolFxFactory<?>> factories = new LinkedHashMap<>();

    static {
        // Phase 3/4 — JavaFX tools
        // Encoders
        register(new gt.devtools.tools.encoders.Base64EncoderDecoderFx.Factory());
        register(new gt.devtools.tools.encoders.AsciiEncoderDecoderFx.Factory());
        register(new gt.devtools.tools.encoders.UrlBase64EncoderDecoderFx.Factory());
        register(new gt.devtools.tools.encoders.UrlEncodingEncoderDecoderFx.Factory());
        register(new gt.devtools.tools.encoders.MimeBase64EncoderDecoderFx.Factory());
        // Escapers
        register(new gt.devtools.tools.escape.HtmlEntitiesEscaperUnescaperFx.Factory());
        register(new gt.devtools.tools.escape.JsonTextEscaperUnescaperFx.Factory());
        register(new gt.devtools.tools.escape.EscapeSequencesEscaperUnescaperFx.Factory());
        // Text tools
        register(new gt.devtools.tools.text.HashingToolFx.Factory());
        register(new gt.devtools.tools.text.HmacToolFx.Factory());
        register(new gt.devtools.tools.text.TextCaseToolFx.Factory());
        register(new gt.devtools.tools.text.TextFilterToolFx.Factory());
        // Generators
        register(new gt.devtools.tools.crypto.UuidGeneratorToolFx.Factory());
        register(new gt.devtools.tools.crypto.PasswordGeneratorToolFx.Factory());
        register(new gt.devtools.tools.crypto.LoremIpsumGeneratorToolFx.Factory());
        // Formatters
        register(new gt.devtools.tools.formatters.CliCommandConverterToolFx.Factory());
        // Standalone (medium)
        register(new gt.devtools.tools.standalone.RegexMatcherToolFx.Factory());
        // Complex tools
        register(new gt.devtools.tools.standalone.AsciiArtToolFx.Factory());
        register(new gt.devtools.tools.standalone.NotesToolFx.Factory());
        register(new gt.devtools.tools.standalone.RubberDuckToolFx.Factory());
        register(new gt.devtools.tools.standalone.UnitConverterToolFx.Factory());
        register(new gt.devtools.tools.standalone.ColorPickerToolFx.Factory());
        register(new gt.devtools.tools.standalone.QrCodeGeneratorToolFx.Factory());
        register(new gt.devtools.tools.standalone.HttpServerToolFx.Factory());
        register(new gt.devtools.tools.standalone.ServerCertificatesToolFx.Factory());
        register(new gt.devtools.tools.standalone.DatetimeConverterToolFx.Factory());
        register(new gt.devtools.tools.standalone.JsonPathToolFx.Factory());
        register(new gt.devtools.tools.standalone.CronExpressionToolFx.Factory());
    }

    private FxToolRegistry() {}

    public static void register(ToolFxFactory<?> factory) {
        factories.put(factory.getId(), factory);
    }

    public static ToolFxFactory<?> get(String toolId) {
        return factories.get(toolId);
    }

    /** Try to create an FX version of a tool by its base ID (without "-fx" suffix). */
    public static DeveloperToolFx createFxTool(String baseToolId, ToolConfiguration config) {
        String fxId = baseToolId + "-fx";
        var factory = factories.get(fxId);
        if (factory != null) {
            return factory.create(config);
        }
        return null;
    }
}
