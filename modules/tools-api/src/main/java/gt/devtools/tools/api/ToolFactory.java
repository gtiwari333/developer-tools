package gt.devtools.tools.api;

import gt.devtools.settings.ToolConfiguration;

/**
 * Creates tool instances from persisted configurations.
 * <p>
 * Every tool must provide a {@code Factory} inner class implementing this
 * interface. Factories are registered via a module-level
 * {@link ToolProvider} and discovered by the framework via
 * {@link java.util.ServiceLoader}.
 *
 * <h3>Example</h3>
 * <pre>{@code
 * public static final class Factory implements ToolFactory<Base64EncoderDecoder> {
 *     public String getId() { return "base64-encoder-decoder"; }
 *     public ToolPresentation getPresentation() {
 *         return ToolPresentation.of("base64-encoder-decoder",
 *             "Base64 Encoder / Decoder", "Base64 Encoder / Decoder");
 *     }
 *     public Base64EncoderDecoder create(ToolConfiguration config) {
 *         return new Base64EncoderDecoder(config);
 *     }
 * }
 * }</pre>
 *
 * @param <T> the concrete tool type this factory produces
 */
public interface ToolFactory<T extends DeveloperTool> {

    /**
     * Stable unique identifier for this tool. Used as the extension-point
     * key and for persisting which tools were last open.
     * <p>
     * Convention: lowercase-with-hyphens, e.g. {@code "base64-encoder-decoder"}.
     */
    String getId();

    /**
     * Metadata shown in the sidebar tree, title bar, and search results.
     */
    ToolPresentation getPresentation();

    /**
     * Create a new tool instance bound to the given configuration.
     * <p>
     * Return {@code null} if the tool cannot be created (e.g. missing
     * optional dependency). The framework will skip the tool gracefully.
     */
    T create(ToolConfiguration config);
}
