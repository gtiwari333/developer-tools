package gt.devtools.tools.encoders;

import gt.devtools.tools.api.ToolFactory;
import gt.devtools.tools.api.ToolProvider;

import java.util.List;

/**
 * {@link ToolProvider} for the encoders/decoders module.
 * <p>
 * Discovered via {@link java.util.ServiceLoader} because
 * {@code module-info.java} declares:
 * <pre>{@code provides ToolProvider with EncodersToolProvider;}</pre>
 *
 * <h3>Adding a new tool to this module</h3>
 * Add the factory to the list returned by {@link #getTools()}:
 * <pre>{@code
 * public List<ToolFactory<?>> getTools() {
 *     return List.of(
 *         new Base64EncoderDecoder.Factory(),
 *         new Base32EncoderDecoder.Factory(),   // add here
 *         new JwtEncoderDecoder.Factory()       // add here
 *     );
 * }
 * }</pre>
 */
public final class EncodersToolProvider implements ToolProvider {
    public EncodersToolProvider() {}

    @Override
    public String getName() { return "Encoders / Decoders"; }

    @Override
    public List<ToolFactory<?>> getTools() {
        return List.of(
                new Base64EncoderDecoder.Factory()
        );
    }
}
