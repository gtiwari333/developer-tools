package gt.devtools.tools.api;

/**
 * Immutable presentation metadata for a tool — its display name,
 * group membership, and optional description.
 *
 * <p>Thread-safe by virtue of being a {@code record}. Use the
 * {@code with*} methods to derive variants fluently.
 */
public record ToolPresentation(
        // Unique stable id matching ToolFactory.getId()
        String id,
        // Label shown in the sidebar tree leaf
        String menuTitle,
        // Label shown in the title bar above the workbench
        String contentTitle,
        // Group id this tool belongs to, or empty string for ungrouped
        String groupId,
        // If true, this tool is pre-selected when the app first launches
        boolean preferredSelected,
        // If true, only shown when "Show internal tools" is enabled
        boolean internalTool,
        // Optional description shown as a tooltip on the help button
        String description
) {
    /** Compact constructor for the common case. */
    public ToolPresentation(String id, String menuTitle, String contentTitle) {
        this(id, menuTitle, contentTitle, "", false, false, null);
    }

    public ToolPresentation(String id, String menuTitle, String contentTitle, String groupId) {
        this(id, menuTitle, contentTitle, groupId, false, false, null);
    }

    // ---- Factory helpers ----

    public static ToolPresentation of(String id, String menuTitle, String contentTitle) {
        return new ToolPresentation(id, menuTitle, contentTitle);
    }

    // ---- Fluent derivations (return new instances) ----

    public ToolPresentation withGroupId(String groupId) {
        return new ToolPresentation(id, menuTitle, contentTitle, groupId,
                preferredSelected, internalTool, description);
    }

    public ToolPresentation withPreferredSelected(boolean v) {
        return new ToolPresentation(id, menuTitle, contentTitle, groupId,
                v, internalTool, description);
    }

    public ToolPresentation withDescription(String desc) {
        return new ToolPresentation(id, menuTitle, contentTitle, groupId,
                preferredSelected, internalTool, desc);
    }
}
