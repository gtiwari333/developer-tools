package gt.devtools.tools.api;

import java.util.List;

/**
 * A module-level provider that contributes one or more {@link ToolFactory}
 * instances. Discovered via {@link java.util.ServiceLoader}.
 */
public interface ToolProvider {

    /**
     * Human-readable name of this provider (e.g. "Encoders").
     */
    String getName();

    /**
     * The tool factories this provider contributes.
     */
    List<ToolFactory<?>> getTools();
}
