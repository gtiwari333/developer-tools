package gt.devtools.tools.api;

import gt.devtools.settings.ToolConfiguration;

/**
 * Factory that creates tool instances from persisted configurations.
 *
 * @param <T> the concrete tool type
 */
public interface ToolFactory<T extends DeveloperTool> {

    /**
     * Stable unique id matching the tool registration.
     */
    String getId();

    /**
     * Presentation metadata for the sidebar tree and title bar.
     */
    ToolPresentation getPresentation();

    /**
     * Create a new tool instance bound to {@code config}.
     * Returns {@code null} if the tool cannot be created (e.g. missing dependency).
     */
    T create(ToolConfiguration config);
}
