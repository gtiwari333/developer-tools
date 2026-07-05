package gt.devtools.tools.encoders;

import gt.devtools.tools.api.ToolProvider;
import gt.devtools.tools.api.fx.ToolFxFactory;

import java.util.List;

/**
 * Registers all encoder/decoder tools. Add new tool factories here
 * to make them appear in the sidebar.
 */
public final class EncodersToolProvider implements ToolProvider {
    public EncodersToolProvider() {}

    @Override public String getName() { return "Encoders / Decoders"; }

    @Override
    public List<ToolFxFactory<?>> getTools() {
        return List.of(
                new Base64EncoderDecoderFx.Factory(),
                new Base32EncoderDecoderFx.Factory(),
                new UrlBase64EncoderDecoderFx.Factory(),
                new MimeBase64EncoderDecoderFx.Factory(),
                new AsciiEncoderDecoderFx.Factory(),
                new UrlEncodingEncoderDecoderFx.Factory(),
                new JwtEncoderDecoderFx.Factory()
        );
    }
}
