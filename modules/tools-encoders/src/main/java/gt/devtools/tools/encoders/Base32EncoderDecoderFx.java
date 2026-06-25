package gt.devtools.tools.encoders;

import gt.devtools.settings.ToolConfiguration;
import gt.devtools.tools.api.ToolPresentation;
import gt.devtools.tools.api.fx.EncoderDecoderFx;
import gt.devtools.tools.api.fx.ToolFxFactory;
import org.apache.commons.codec.binary.Base32;

/** JavaFX version of Base32 encoder/decoder. */
public final class Base32EncoderDecoderFx extends EncoderDecoderFx {

    private final Base32 codec = new Base32();

    private Base32EncoderDecoderFx(ToolConfiguration config) {
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

    public static final class Factory implements ToolFxFactory<Base32EncoderDecoderFx> {
        public Factory() {}
        @Override public String getId() { return "base32-encoder-decoder"; }
        @Override public ToolPresentation getPresentation() {
            return ToolPresentation.of("base32-encoder-decoder",
                    "Base32 Encoder / Decoder", "Base32 Encoder / Decoder")
                    .withGroupId("encoders");
        }
        @Override
        public Base32EncoderDecoderFx create(ToolConfiguration config) {
            return new Base32EncoderDecoderFx(config);
        }
    }
}
