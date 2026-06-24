package gt.devtools.tools.escape;

import gt.devtools.settings.ToolConfiguration;
import gt.devtools.tools.api.ToolPresentation;
import gt.devtools.tools.api.fx.EscaperUnescaperFx;
import gt.devtools.tools.api.fx.ToolFxFactory;
import java.nio.charset.StandardCharsets;

public final class EscapeSequencesEscaperUnescaperFx extends EscaperUnescaperFx {
    private EscapeSequencesEscaperUnescaperFx(ToolConfiguration config) { super(config); }
    @Override protected byte[] doConvertForward(byte[] input) {
        return EscapeSequencesEscaperUnescaper.escape(new String(input, StandardCharsets.UTF_8)).getBytes(StandardCharsets.UTF_8); }
    @Override protected byte[] doConvertBackward(byte[] input) {
        return EscapeSequencesEscaperUnescaper.unescape(new String(input, StandardCharsets.UTF_8)).getBytes(StandardCharsets.UTF_8); }
    public static final class Factory implements ToolFxFactory<EscapeSequencesEscaperUnescaperFx> {
        public Factory() {} @Override public String getId() { return "escape-sequence-escaper-unescaper-fx"; }
        @Override public ToolPresentation getPresentation() {
            return ToolPresentation.of("escape-sequence-escaper-unescaper-fx", "Escape Seqs (FX)", "Escape Sequences Escaper / Unescaper").withGroupId("escape"); }
        @Override public EscapeSequencesEscaperUnescaperFx create(ToolConfiguration c) { return new EscapeSequencesEscaperUnescaperFx(c); }
    }
}
