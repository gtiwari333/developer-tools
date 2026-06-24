package gt.devtools.tools.encoders;

import gt.devtools.settings.ToolConfiguration;
import gt.devtools.tools.api.ToolPresentation;
import gt.devtools.tools.api.fx.EncoderDecoderFx;
import gt.devtools.tools.api.fx.ToolFxFactory;
import java.nio.charset.StandardCharsets;

public final class AsciiEncoderDecoderFx extends EncoderDecoderFx {
    private AsciiEncoderDecoderFx(ToolConfiguration config) { super(config); }
    @Override protected byte[] doConvertForward(byte[] input) {
        return AsciiEncoderDecoder.encodeHex(input).getBytes(StandardCharsets.UTF_8); }
    @Override protected byte[] doConvertBackward(byte[] input) {
        return AsciiEncoderDecoder.decodeHex(new String(input, StandardCharsets.UTF_8)); }
    public static final class Factory implements ToolFxFactory<AsciiEncoderDecoderFx> {
        public Factory() {}
        @Override public String getId() { return "ascii-encoder-decoder-fx"; }
        @Override public ToolPresentation getPresentation() {
            return ToolPresentation.of("ascii-encoder-decoder-fx", "ASCII Hex (FX)", "ASCII Encoder / Decoder").withGroupId("encoders"); }
        @Override public AsciiEncoderDecoderFx create(ToolConfiguration c) { return new AsciiEncoderDecoderFx(c); }
    }
}
