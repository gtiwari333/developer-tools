package gt.devtools.tools.encoders;

import gt.devtools.tools.api.ToolFactory;
import gt.devtools.tools.api.ToolProvider;

import java.util.List;

/**
 * Registers all encoder/decoder tools. Add new tool factories here
 * to make them appear in the sidebar.
 */
public final class EncodersToolProvider implements ToolProvider {
    public EncodersToolProvider() {}

    @Override public String getName() { return "Encoders / Decoders"; }

    @Override
    public List<ToolFactory<?>> getTools() {
        return List.of(
                new Base64EncoderDecoder.Factory(),
                new Base32EncoderDecoder.Factory(),
                new UrlBase64EncoderDecoder.Factory(),
                new MimeBase64EncoderDecoder.Factory(),
                new AsciiEncoderDecoder.Factory(),
                new UrlEncodingEncoderDecoder.Factory(),
                new JwtEncoderDecoder.Factory()
        );
    }
}
