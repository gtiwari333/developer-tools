package gt.devtools.app;

import gt.devtools.tools.api.fx.ToolFxFactory;

/**
 * Value type for TreeView items in the sidebar.
 * Distinguishes between group nodes (containers) and tool nodes (leaves).
 */
record TreeItemData(String label, ToolFxFactory<?> factory, boolean isGroup) {

    static TreeItemData group(String label) {
        return new TreeItemData(label, null, true);
    }

    static TreeItemData tool(String label, ToolFxFactory<?> factory) {
        return new TreeItemData(label, factory, false);
    }
}