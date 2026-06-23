package gt.devtools.tools.api.converter;

import gt.devtools.settings.ToolConfiguration;

/**
 * Convenience base for encoder/decoder tools with pre-set labels.
 * Subclasses only need to implement {@link #doConvertForward(byte[])}
 * (encoding) and {@link #doConvertBackward(byte[])} (decoding).
 */
public abstract class EncoderDecoder extends BidirectionalConverter {

    protected EncoderDecoder(ToolConfiguration config) {
        super(config);
    }

    /**
     * Encoding = forward direction (source → target).
     */
    @Override
    protected abstract byte[] doConvertForward(byte[] input) throws Exception;

    /**
     * Decoding = backward direction (target → source).
     */
    @Override
    protected abstract byte[] doConvertBackward(byte[] input) throws Exception;
}
