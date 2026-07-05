package gt.devtools.tools.encoders;

import gt.devtools.settings.ToolConfiguration;
import gt.devtools.tools.api.ToolPresentation;
import gt.devtools.tools.api.fx.EncoderDecoderFx;
import gt.devtools.tools.api.fx.ToolFxFactory;

import java.util.Base64;

/** JavaFX version of Base64 encoder/decoder. */
public final class Base64EncoderDecoderFx extends EncoderDecoderFx {

    private Base64EncoderDecoderFx(ToolConfiguration config) { super(config); }

    @Override protected byte[] doConvertForward(byte[] input) { return encode(input); }
    @Override protected byte[] doConvertBackward(byte[] input) { return decode(input); }

    public static byte[] encode(byte[] input) { return Base64.getEncoder().encode(input); }
    public static byte[] decode(byte[] input) { return Base64.getDecoder().decode(input); }

    public static final class Factory implements ToolFxFactory<Base64EncoderDecoderFx> {
        public Factory() {}
        @Override public String getId() { return "base64-encoder-decoder"; }
        @Override public ToolPresentation getPresentation() {
            return ToolPresentation.of("base64-encoder-decoder",
                    "Base64", "Base64 Encoder / Decoder")
                    .withGroupId("encoders");
        }
        @Override
        public Base64EncoderDecoderFx create(ToolConfiguration config) {
            return new Base64EncoderDecoderFx(config);
        }
    }
}
