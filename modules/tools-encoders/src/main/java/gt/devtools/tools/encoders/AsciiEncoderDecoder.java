package gt.devtools.tools.encoders;

import gt.devtools.settings.ToolConfiguration;
import gt.devtools.tools.api.ToolFactory;
import gt.devtools.tools.api.ToolPresentation;
import gt.devtools.tools.api.converter.EncoderDecoder;

import java.nio.charset.StandardCharsets;

/**
 * Encodes text to hex-as-ASCII (e.g. "abc" → "616263") and decodes back.
 */
public final class AsciiEncoderDecoder extends EncoderDecoder {

    private AsciiEncoderDecoder(ToolConfiguration config) {
        super(config);
    }

    // -- public static conversion methods (testable without Swing)

    /** Encodes byte array to lowercase hex string (e.g. [0x61, 0x62] → "616263"). */
    public static String encodeHex(byte[] input) {
        StringBuilder hex = new StringBuilder(input.length * 2);
        for (byte b : input) {
            hex.append(String.format("%02x", b & 0xff));
        }
        return hex.toString();
    }

    /** Decodes a hex string back to bytes. Strips whitespace before decoding. */
    public static byte[] decodeHex(String hex) {
        String clean = hex.replaceAll("\\s", "");
        if (clean.length() % 2 != 0) throw new IllegalArgumentException("Odd hex length");
        byte[] result = new byte[clean.length() / 2];
        for (int i = 0; i < clean.length(); i += 2) {
            result[i / 2] = (byte) Integer.parseInt(clean.substring(i, i + 2), 16);
        }
        return result;
    }

    @Override
    protected byte[] doConvertForward(byte[] input) {
        return encodeHex(input).getBytes(StandardCharsets.UTF_8);
    }

    @Override
    protected byte[] doConvertBackward(byte[] input) throws Exception {
        return decodeHex(new String(input, StandardCharsets.UTF_8));
    }

    public static final class Factory implements ToolFactory<AsciiEncoderDecoder> {
        public Factory() {}
        @Override public String getId() { return "ascii-encoder-decoder"; }
        @Override
        public ToolPresentation getPresentation() {
            return ToolPresentation.of("ascii-encoder-decoder",
                    "ASCII Encoder / Decoder", "ASCII Encoder / Decoder")
                    .withGroupId("encoders");
        }
        @Override
        public AsciiEncoderDecoder create(ToolConfiguration config) {
            return new AsciiEncoderDecoder(config);
        }
    }
}
