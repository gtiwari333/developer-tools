package gt.devtools.tools.encoders;

import gt.devtools.settings.ToolConfiguration;
import gt.devtools.tools.api.ToolPresentation;
import gt.devtools.tools.api.fx.EncoderDecoderFx;
import gt.devtools.tools.api.fx.ToolFxFactory;
import java.util.Base64;

public final class UrlBase64EncoderDecoderFx extends EncoderDecoderFx {
    private UrlBase64EncoderDecoderFx(ToolConfiguration config) { super(config); }
    @Override protected byte[] doConvertForward(byte[] input) { return Base64.getUrlEncoder().encode(input); }
    @Override protected byte[] doConvertBackward(byte[] input) { return Base64.getUrlDecoder().decode(input); }
    public static final class Factory implements ToolFxFactory<UrlBase64EncoderDecoderFx> {
        public Factory() {}
        @Override public String getId() { return "url-base64-encoder-decoder-fx"; }
        @Override public ToolPresentation getPresentation() {
            return ToolPresentation.of("url-base64-encoder-decoder-fx", "URL Base64 (FX)", "URL Base64 Encoder / Decoder").withGroupId("encoders"); }
        @Override public UrlBase64EncoderDecoderFx create(ToolConfiguration c) { return new UrlBase64EncoderDecoderFx(c); }
    }
}
