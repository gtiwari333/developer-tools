package gt.devtools.tools.api.converter;

import gt.devtools.settings.ToolConfiguration;

/**
 * Convenience base for encoder/decoder tools.
 * <p>
 * Subclasses only need to implement two methods:
 * <ul>
 *   <li>{@link #doConvertForward(byte[])} — encoding (source → target)</li>
 *   <li>{@link #doConvertBackward(byte[])} — decoding (target → source)</li>
 * </ul>
 *
 * <h3>Example: Base64</h3>
 * <pre>{@code
 * public final class Base64EncoderDecoder extends EncoderDecoder {
 *     public Base64EncoderDecoder(ToolConfiguration config) { super(config); }
 *
 *     protected byte[] doConvertForward(byte[] input) {
 *         return Base64.getEncoder().encode(input);
 *     }
 *     protected byte[] doConvertBackward(byte[] input) {
 *         return Base64.getDecoder().decode(input);
 *     }
 * }
 * }</pre>
 */
public abstract class EncoderDecoder extends BidirectionalConverter {

    protected EncoderDecoder(ToolConfiguration config) {
        super(config);
    }

    /** Encoding: source plaintext → target encoded text. */
    @Override
    protected abstract byte[] doConvertForward(byte[] input) throws Exception;

    /** Decoding: target encoded text → source plaintext. */
    @Override
    protected abstract byte[] doConvertBackward(byte[] input) throws Exception;
}
