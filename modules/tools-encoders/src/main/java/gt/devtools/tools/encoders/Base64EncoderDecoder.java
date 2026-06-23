package gt.devtools.tools.encoders;

import gt.devtools.settings.ToolConfiguration;
import gt.devtools.tools.api.ToolFactory;
import gt.devtools.tools.api.ToolPresentation;
import gt.devtools.tools.api.converter.EncoderDecoder;

import java.util.Base64;

/**
 * Encodes plain text to Base64 and decodes Base64 back to plain text.
 *
 * <h3>Implementation notes</h3>
 * Uses {@link java.util.Base64} from the JDK — no external dependencies.
 * Extends {@link EncoderDecoder} which provides the two-pane UI, live
 * conversion, copy/swap buttons, and text persistence.
 *
 * <h3>Adding a new encoder</h3>
 * Follow this pattern:
 * <ol>
 *   <li>Extend {@link EncoderDecoder}</li>
 *   <li>Implement {@code doConvertForward} (encode) and
 *       {@code doConvertBackward} (decode)</li>
 *   <li>Create a {@code public static final class Factory} inner class</li>
 *   <li>Register the factory in {@link EncodersToolProvider#getTools()}</li>
 * </ol>
 */
public final class Base64EncoderDecoder extends EncoderDecoder {

    /** Constructor is private — only the {@link Factory} creates instances. */
    private Base64EncoderDecoder(ToolConfiguration config) {
        super(config);
    }

    /** Encode: plaintext → Base64. */
    @Override
    protected byte[] doConvertForward(byte[] input) {
        return Base64.getEncoder().encode(input);
    }

    /** Decode: Base64 → plaintext. */
    @Override
    protected byte[] doConvertBackward(byte[] input) {
        return Base64.getDecoder().decode(input);
    }

    // ---------------------------------------------------------------
    // Factory — registered via ServiceLoader
    // ---------------------------------------------------------------

    /**
     * Factory that creates {@link Base64EncoderDecoder} instances.
     * Discovered automatically by {@link ToolRegistry} at startup
     * via {@link EncodersToolProvider}.
     */
    public static final class Factory implements ToolFactory<Base64EncoderDecoder> {
        public Factory() {}

        @Override
        public String getId() { return "base64-encoder-decoder"; }

        @Override
        public ToolPresentation getPresentation() {
            return ToolPresentation.of(
                    "base64-encoder-decoder",
                    "Base64 Encoder / Decoder",
                    "Base64 Encoder / Decoder"
            ).withGroupId("encoders");
        }

        @Override
        public Base64EncoderDecoder create(ToolConfiguration config) {
            return new Base64EncoderDecoder(config);
        }
    }
}
