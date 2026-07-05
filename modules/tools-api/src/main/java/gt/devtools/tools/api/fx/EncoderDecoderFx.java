package gt.devtools.tools.api.fx;

import gt.devtools.settings.ToolConfiguration;

/**
 * JavaFX convenience base for encoder/decoder tools.
 * Subclasses implement encode (→) and decode (←) as byte[] operations.
 */
public abstract class EncoderDecoderFx extends BidirectionalConverterFx {

    protected EncoderDecoderFx(ToolConfiguration config) {
        super(config);
    }
}
