package gt.devtools.tools.encoders;

import gt.devtools.settings.ToolConfiguration;
import gt.devtools.tools.api.ToolPresentation;
import gt.devtools.tools.api.fx.EncoderDecoderFx;
import gt.devtools.tools.api.fx.ToolFxFactory;
import java.util.Base64;

public final class MimeBase64EncoderDecoderFx extends EncoderDecoderFx {
    private MimeBase64EncoderDecoderFx(ToolConfiguration config) { super(config); }
    @Override protected byte[] doConvertForward(byte[] input) { return Base64.getMimeEncoder().encode(input); }
    @Override protected byte[] doConvertBackward(byte[] input) { return Base64.getMimeDecoder().decode(input); }
    public static final class Factory implements ToolFxFactory<MimeBase64EncoderDecoderFx> {
        public Factory() {}
        @Override public String getId() { return "mime-base64-encoder-decoder-fx"; }
        @Override public ToolPresentation getPresentation() {
            return ToolPresentation.of("mime-base64-encoder-decoder-fx", "MIME Base64 (FX)", "MIME Base64 Encoder / Decoder").withGroupId("encoders"); }
        @Override public MimeBase64EncoderDecoderFx create(ToolConfiguration c) { return new MimeBase64EncoderDecoderFx(c); }
    }
}
