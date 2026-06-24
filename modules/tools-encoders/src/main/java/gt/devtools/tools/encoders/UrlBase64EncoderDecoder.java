package gt.devtools.tools.encoders;

import gt.devtools.settings.ToolConfiguration;
import gt.devtools.tools.api.ToolFactory;
import gt.devtools.tools.api.ToolPresentation;
import gt.devtools.tools.api.converter.EncoderDecoder;

import java.util.Base64;

/**
 * URL-safe Base64 encoding/decoding (uses '-' and '_' instead of '+' and '/').
 * Uses JDK {@link java.util.Base64}.
 */
public final class UrlBase64EncoderDecoder extends EncoderDecoder {

    private UrlBase64EncoderDecoder(ToolConfiguration config) {
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

    public static byte[] encode(byte[] input) { return java.util.Base64.getUrlEncoder().encode(input); }
    public static byte[] decode(byte[] input) { return java.util.Base64.getUrlDecoder().decode(input); }

    public static final class Factory implements ToolFactory<UrlBase64EncoderDecoder> {
        public Factory() {}
        @Override public String getId() { return "url-base64-encoder-decoder"; }
        @Override
        public ToolPresentation getPresentation() {
            return ToolPresentation.of("url-base64-encoder-decoder",
                    "URL Base64 Encoder / Decoder", "URL Base64 Encoder / Decoder")
                    .withGroupId("encoders");
        }
        @Override
        public UrlBase64EncoderDecoder create(ToolConfiguration config) {
            return new UrlBase64EncoderDecoder(config);
        }
    }
}
