package gt.devtools.tools.encoders;

import gt.devtools.settings.ToolConfiguration;
import gt.devtools.tools.api.ToolFactory;
import gt.devtools.tools.api.ToolPresentation;
import gt.devtools.tools.api.converter.EncoderDecoder;
import org.apache.commons.codec.binary.Base32;

/**
 * Encodes plain text to Base32 and decodes Base32 back to plain text.
 * Uses Apache Commons Codec.
 */
public final class Base32EncoderDecoder extends EncoderDecoder {

    private final Base32 codec = new Base32();

    private Base32EncoderDecoder(ToolConfiguration config) {
        super(config);
    }

    @Override
    protected byte[] doConvertForward(byte[] input) {
        return codec.encode(input);
    }

    @Override
    protected byte[] doConvertBackward(byte[] input) {
        return codec.decode(input);
    }

    public static final class Factory implements ToolFactory<Base32EncoderDecoder> {
        public Factory() {}
        @Override public String getId() { return "base32-encoder-decoder"; }
        @Override
        public ToolPresentation getPresentation() {
            return ToolPresentation.of("base32-encoder-decoder",
                    "Base32 Encoder / Decoder", "Base32 Encoder / Decoder")
                    .withGroupId("encoders");
        }
        @Override
        public Base32EncoderDecoder create(ToolConfiguration config) {
            return new Base32EncoderDecoder(config);
        }
    }
}
