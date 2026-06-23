package gt.devtools.app.sidebar;

import gt.devtools.app.content.ContentPanel;
import gt.devtools.tools.api.ToolFactory;
import gt.devtools.tools.api.ToolRegistry;

import javax.swing.*;
import javax.swing.tree.*;
import java.awt.*;
import java.util.*;

/**
 * Sidebar panel with a search field and a tree showing tool groups
 * and individual tools. Selecting a tool opens it in the content area.
 */
public final class ToolSidebar extends JPanel {

    private final ContentPanel contentPanel;
    private final JTextField searchField;
    private final JTree tree;
    private final DefaultTreeModel treeModel;
    private final DefaultMutableTreeNode rootNode;
    private final Map<String, DefaultMutableTreeNode> groupNodes = new LinkedHashMap<>();

    public ToolSidebar(ContentPanel contentPanel) {
        super(new BorderLayout());
        this.contentPanel = contentPanel;

        setMinimumSize(new Dimension(200, 0));
        setPreferredSize(new Dimension(280, 0));

        // -- search field
        searchField = new JTextField();
        searchField.putClientProperty("JTextField.placeholderText", "Filter tools...");
        searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            @Override public void insertUpdate(javax.swing.event.DocumentEvent e) { filter(); }
            @Override public void removeUpdate(javax.swing.event.DocumentEvent e) { filter(); }
            @Override public void changedUpdate(javax.swing.event.DocumentEvent e) { filter(); }
        });
        add(searchField, BorderLayout.NORTH);

        // -- tree
        rootNode = new DefaultMutableTreeNode("Developer Tools");
        treeModel = new DefaultTreeModel(rootNode);
        buildTreeModel();

        tree = new JTree(treeModel);
        tree.setRootVisible(false);
        tree.setShowsRootHandles(true);
        tree.setCellRenderer(new ToolTreeCellRenderer());
        tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);

        // Use mouse listener so clicking an already-selected node still opens it
        tree.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mousePressed(java.awt.event.MouseEvent e) {
                var path = tree.getPathForLocation(e.getX(), e.getY());
                if (path != null) {
                    var node = (DefaultMutableTreeNode) path.getLastPathComponent();
                    if (node instanceof ToolTreeNode toolNode) {
                        tree.setSelectionPath(path);
                        contentPanel.openTool(toolNode.factory);
                    }
                }
            }
        });

        // Expand groups after tree is created
        expandInitiallyExpanded();

        var scrollPane = new JScrollPane(tree);
        scrollPane.setBorder(null);
        add(scrollPane, BorderLayout.CENTER);
    }

    private void buildTreeModel() {
        var registry = ToolRegistry.getInstance();

        // Add grouped tools
        for (var group : registry.getGroups()) {
            var groupNode = new DefaultMutableTreeNode(group.menuTitle());
            groupNodes.put(group.id(), groupNode);

            for (var factory : registry.getToolsByGroup(group.id())) {
                groupNode.add(new ToolTreeNode(factory));
            }
            rootNode.add(groupNode);
        }

        // Add ungrouped tools directly under root
        for (String id : registry.getUngroupedToolIds()) {
            var factory = registry.getTool(id);
            if (factory != null) {
                rootNode.add(new ToolTreeNode(factory));
            }
        }
    }

    private void expandInitiallyExpanded() {
        var registry = ToolRegistry.getInstance();
        for (var group : registry.getGroups()) {
            if (group.initiallyExpanded()) {
                var node = groupNodes.get(group.id());
                if (node != null) {
                    tree.expandPath(new TreePath(node.getPath()));
                }
            }
        }
    }

    public void selectTool(String toolId) {
        selectInTree(rootNode, toolId);
    }

    private void selectInTree(DefaultMutableTreeNode node, String toolId) {
        for (int i = 0; i < node.getChildCount(); i++) {
            var child = node.getChildAt(i);
            if (child instanceof ToolTreeNode tn && tn.factory.getId().equals(toolId)) {
                TreePath path = new TreePath(((DefaultMutableTreeNode) child).getPath());
                tree.setSelectionPath(path);
                tree.scrollPathToVisible(path);
                return;
            }
            if (child instanceof DefaultMutableTreeNode dn) {
                selectInTree(dn, toolId);
            }
        }
    }

    public String getSelectedToolId() {
        var node = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
        if (node instanceof ToolTreeNode tn) {
            return tn.factory.getId();
        }
        return "";
    }

    private void filter() {
        String query = searchField.getText().toLowerCase().trim();
        var registry = ToolRegistry.getInstance();

        rootNode.removeAllChildren();

        if (query.isEmpty()) {
            rootNode.removeAllChildren();
            buildTreeModel();
            treeModel.reload();
            expandInitiallyExpanded();
            return;
        }

        // Flatten filtered view — no groups
        for (var factory : registry.getAllTools()) {
            var pres = factory.getPresentation();
            if (pres.menuTitle().toLowerCase().contains(query)
                    || pres.id().toLowerCase().contains(query)) {
                rootNode.add(new ToolTreeNode(factory));
            }
        }

        treeModel.reload();
        // Expand all in filtered view
        for (int i = 0; i < tree.getRowCount(); i++) {
            tree.expandRow(i);
        }
    }

    // -- custom tree node

    private static final class ToolTreeNode extends DefaultMutableTreeNode {
        final ToolFactory<?> factory;

        ToolTreeNode(ToolFactory<?> factory) {
            super(factory.getPresentation().menuTitle());
            this.factory = factory;
        }
    }

    private static final class ToolTreeCellRenderer extends DefaultTreeCellRenderer {
        @Override
        public Component getTreeCellRendererComponent(JTree tree, Object value,
                                                      boolean sel, boolean expanded,
                                                      boolean leaf, int row, boolean hasFocus) {
            super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
            if (value instanceof ToolTreeNode) {
                setIcon(UIManager.getIcon("Tree.leafIcon"));
            }
            return this;
        }
    }

}
