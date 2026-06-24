package gt.devtools.tools.encoders;

import gt.devtools.settings.ToolConfiguration;
import gt.devtools.tools.api.ToolFactory;
import gt.devtools.tools.api.ToolPresentation;
import gt.devtools.tools.api.converter.EncoderDecoder;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * URL percent-encoding / decoding (application/x-www-form-urlencoded).
 * Uses JDK {@link java.net.URLEncoder} and {@link java.net.URLDecoder}.
 */
public final class UrlEncodingEncoderDecoder extends EncoderDecoder {

    private UrlEncodingEncoderDecoder(ToolConfiguration config) {
        super(config);
    }

    @Override
    protected byte[] doConvertForward(byte[] input) {
        return encode(input);
    }

    @Override
    protected byte[] doConvertBackward(byte[] input) throws Exception {
        return decode(input);
    }

    public static byte[] encode(byte[] input) {
        String encoded = java.net.URLEncoder.encode(new String(input, java.nio.charset.StandardCharsets.UTF_8),
                java.nio.charset.StandardCharsets.UTF_8);
        return encoded.getBytes(java.nio.charset.StandardCharsets.UTF_8);
    }

    public static byte[] decode(byte[] input) throws Exception {
        String decoded = java.net.URLDecoder.decode(new String(input, java.nio.charset.StandardCharsets.UTF_8),
                java.nio.charset.StandardCharsets.UTF_8);
        return decoded.getBytes(java.nio.charset.StandardCharsets.UTF_8);
    }

    public static final class Factory implements ToolFactory<UrlEncodingEncoderDecoder> {
        public Factory() {}
        @Override public String getId() { return "url-encoding-encoder-decoder"; }
        @Override
        public ToolPresentation getPresentation() {
            return ToolPresentation.of("url-encoding-encoder-decoder",
                    "URL Encoding Encoder / Decoder", "URL Encoding Encoder / Decoder")
                    .withGroupId("encoders");
        }
        @Override
        public UrlEncodingEncoderDecoder create(ToolConfiguration config) {
            return new UrlEncodingEncoderDecoder(config);
        }
    }
}
