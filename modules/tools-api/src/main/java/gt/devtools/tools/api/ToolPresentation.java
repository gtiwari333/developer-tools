package gt.devtools.tools.api;

import gt.devtools.settings.ToolConfiguration;

/**
 * Presentation metadata for a tool: its title, group, and optional description.
 */
public record ToolPresentation(
        String id,
        String menuTitle,
        String contentTitle,
        String groupId,
        boolean preferredSelected,
        boolean internalTool,
        String description
) {
    public ToolPresentation(String id, String menuTitle, String contentTitle) {
        this(id, menuTitle, contentTitle, "", false, false, null);
    }

    public ToolPresentation(String id, String menuTitle, String contentTitle, String groupId) {
        this(id, menuTitle, contentTitle, groupId, false, false, null);
    }

    /**
     * Builder-like factory for inline construction.
     */
    public static ToolPresentation of(String id, String menuTitle, String contentTitle) {
        return new ToolPresentation(id, menuTitle, contentTitle, "", false, false, null);
    }

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
