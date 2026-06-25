package gt.devtools.tools.encoders;

import gt.devtools.settings.ToolConfiguration;
import gt.devtools.tools.api.ToolPresentation;
import gt.devtools.tools.api.fx.EncoderDecoderFx;
import gt.devtools.tools.api.fx.ToolFxFactory;

import java.nio.charset.StandardCharsets;

public final class AsciiEncoderDecoderFx extends EncoderDecoderFx {
    private AsciiEncoderDecoderFx(ToolConfiguration config) { super(config); }

    @Override protected byte[] doConvertForward(byte[] input) {
        return encodeHex(input).getBytes(StandardCharsets.UTF_8); }

    @Override protected byte[] doConvertBackward(byte[] input) {
        return decodeHex(new String(input, StandardCharsets.UTF_8)); }

    /** Converts bytes to a hex-encoded string. */
    public static String encodeHex(byte[] input) {
        var sb = new StringBuilder();
        for (byte b : input) sb.append(String.format("%02x", b & 0xff));
        return sb.toString();
    }

    /** Decodes a hex string back to bytes. Strips all whitespace. */
    public static byte[] decodeHex(String hex) {
        // Strip all whitespace
        String clean = hex.replaceAll("\\s+", "");
        int len = clean.length();
        if (len % 2 != 0) throw new IllegalArgumentException("Odd hex length");
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(clean.charAt(i), 16) << 4)
                    + Character.digit(clean.charAt(i + 1), 16));
        }
        return data;
    }

    public static final class Factory implements ToolFxFactory<AsciiEncoderDecoderFx> {
        public Factory() {}
        @Override public String getId() { return "ascii-encoder-decoder"; }
        @Override public ToolPresentation getPresentation() {
            return ToolPresentation.of("ascii-encoder-decoder", "ASCII Hex", "ASCII Encoder / Decoder").withGroupId("encoders"); }
        @Override public AsciiEncoderDecoderFx create(ToolConfiguration c) { return new AsciiEncoderDecoderFx(c); }
    }
}
