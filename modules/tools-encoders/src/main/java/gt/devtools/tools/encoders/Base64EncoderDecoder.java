package gt.devtools.tools.encoders;

import gt.devtools.settings.ToolConfiguration;
import gt.devtools.tools.api.ToolFactory;
import gt.devtools.tools.api.ToolPresentation;
import gt.devtools.tools.api.converter.EncoderDecoder;

import java.util.Base64;

/**
 * Base64 encode / decode tool.
 */
public final class Base64EncoderDecoder extends EncoderDecoder {

    private Base64EncoderDecoder(ToolConfiguration config) {
        super(config);
    }

    @Override
    protected byte[] doConvertForward(byte[] input) {
        return Base64.getEncoder().encode(input);
    }

    @Override
    protected byte[] doConvertBackward(byte[] input) {
        return Base64.getDecoder().decode(input);
    }

    // -- Factory

    public static final class Factory implements ToolFactory<Base64EncoderDecoder> {
        public Factory() {}

        @Override
        public String getId() {
            return "base64-encoder-decoder";
        }

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
