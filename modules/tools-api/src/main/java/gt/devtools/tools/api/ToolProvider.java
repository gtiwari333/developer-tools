package gt.devtools.tools.api;

import gt.devtools.tools.api.fx.ToolFxFactory;

import java.util.List;

/**
 * Discovered via {@link java.util.ServiceLoader}. Each module that
 * contributes tools must implement this interface and declare it in
 * {@code module-info.java} with
 * {@code provides ToolProvider with MyProvider;}.
 *
 * <h3>Example {@code module-info.java}</h3>
 * <pre>{@code
 * provides ToolProvider with EncodersToolProvider;
 * }</pre>
 */
public interface ToolProvider {

    /**
     * Human-readable name for this provider, used in diagnostic output.
     */
    String getName();

    /**
     * All tool factories contributed by this provider.
     */
    List<ToolFxFactory<?>> getTools();
}
