package gt.devtools.app;

import gt.devtools.tools.api.ToolFactory;

/**
 * Value type for TreeView items in the sidebar.
 * Distinguishes between group nodes (containers) and tool nodes (leaves).
 */
record TreeItemData(String label, ToolFactory<?> factory, boolean isGroup) {

    static TreeItemData group(String label) {
        return new TreeItemData(label, null, true);
    }

    static TreeItemData tool(String label, ToolFactory<?> factory) {
        return new TreeItemData(label, factory, false);
    }
}
