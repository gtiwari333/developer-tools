package gt.devtools.tools.encoders;

import gt.devtools.settings.ToolConfiguration;
import gt.devtools.tools.api.ToolFactory;
import gt.devtools.tools.api.ToolPresentation;
import gt.devtools.tools.api.converter.EncoderDecoder;

import java.util.Base64;

/**
 * MIME Base64 encoding/decoding (line-wrapped at 76 characters, CRLF line
 * endings). Uses JDK {@link java.util.Base64}.
 */
public final class MimeBase64EncoderDecoder extends EncoderDecoder {

    private MimeBase64EncoderDecoder(ToolConfiguration config) {
        super(config);
    }

    @Override
    protected byte[] doConvertForward(byte[] input) {
        return encode(input);
    }

    @Override
    protected byte[] doConvertBackward(byte[] input) {
        return decode(input);
    }

    public static byte[] encode(byte[] input) { return java.util.Base64.getMimeEncoder().encode(input); }
    public static byte[] decode(byte[] input) { return java.util.Base64.getMimeDecoder().decode(input); }

    public static final class Factory implements ToolFactory<MimeBase64EncoderDecoder> {
        public Factory() {}
        @Override public String getId() { return "mime-base64-encoder-decoder"; }
        @Override
        public ToolPresentation getPresentation() {
            return ToolPresentation.of("mime-base64-encoder-decoder",
                    "MIME Base64 Encoder / Decoder", "MIME Base64 Encoder / Decoder")
                    .withGroupId("encoders");
        }
        @Override
        public MimeBase64EncoderDecoder create(ToolConfiguration config) {
            return new MimeBase64EncoderDecoder(config);
        }
    }
}
