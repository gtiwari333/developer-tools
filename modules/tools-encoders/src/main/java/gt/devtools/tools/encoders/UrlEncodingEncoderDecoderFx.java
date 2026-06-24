package gt.devtools.tools.encoders;

import gt.devtools.settings.ToolConfiguration;
import gt.devtools.tools.api.ToolPresentation;
import gt.devtools.tools.api.fx.EncoderDecoderFx;
import gt.devtools.tools.api.fx.ToolFxFactory;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public final class UrlEncodingEncoderDecoderFx extends EncoderDecoderFx {
    private UrlEncodingEncoderDecoderFx(ToolConfiguration config) { super(config); }
    @Override protected byte[] doConvertForward(byte[] input) {
        return URLEncoder.encode(new String(input, StandardCharsets.UTF_8), StandardCharsets.UTF_8).getBytes(StandardCharsets.UTF_8); }
    @Override protected byte[] doConvertBackward(byte[] input) throws Exception {
        return URLDecoder.decode(new String(input, StandardCharsets.UTF_8), StandardCharsets.UTF_8).getBytes(StandardCharsets.UTF_8); }
    public static final class Factory implements ToolFxFactory<UrlEncodingEncoderDecoderFx> {
        public Factory() {}
        @Override public String getId() { return "url-encoding-encoder-decoder-fx"; }
        @Override public ToolPresentation getPresentation() {
            return ToolPresentation.of("url-encoding-encoder-decoder-fx", "URL Encoding (FX)", "URL Encoding Encoder / Decoder").withGroupId("encoders"); }
        @Override public UrlEncodingEncoderDecoderFx create(ToolConfiguration c) { return new UrlEncodingEncoderDecoderFx(c); }
    }
}
