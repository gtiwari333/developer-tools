package gt.devtools.tools.encoders;

import gt.devtools.tools.api.ToolFactory;
import gt.devtools.tools.api.ToolProvider;

import java.util.List;

/**
 * Registers all encoder/decoder tools.
 */
public final class EncodersToolProvider implements ToolProvider {
    public EncodersToolProvider() {}

    @Override
    public String getName() {
        return "Encoders / Decoders";
    }

    @Override
    public List<ToolFactory<?>> getTools() {
        return List.of(
                new Base64EncoderDecoder.Factory()
        );
    }
}
