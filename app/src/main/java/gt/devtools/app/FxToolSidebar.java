package gt.devtools.app;

import gt.devtools.tools.api.ToolFactory;
import gt.devtools.tools.api.ToolRegistry;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * JavaFX sidebar with a search field and a TreeView showing tool groups
 * and individual tools. Single-click opens/focuses a tool, double-click
 * opens a new workbench, right-click shows a context menu.
 */
public final class FxToolSidebar extends VBox {

    private final FxContentPanel contentPanel;
    private final TextField searchField;
    private final TreeView<TreeItemData> tree;
    private final TreeItem<TreeItemData> rootItem;
    private final Map<String, TreeItem<TreeItemData>> groupItems = new LinkedHashMap<>();

    public FxToolSidebar(FxContentPanel contentPanel) {
        this.contentPanel = contentPanel;

        setPadding(new Insets(8));
        setSpacing(4);
        setMinWidth(240);
        setPrefWidth(280);

        // -- Search field
        searchField = new TextField();
        searchField.setPromptText("Filter tools...");
        searchField.textProperty().addListener((obs, old, text) -> filter(text));
        getChildren().add(searchField);

        // -- Tree
        rootItem = new TreeItem<>(TreeItemData.group("Developer Tools"));
        rootItem.setExpanded(true);
        buildTreeModel();

        tree = new TreeView<>(rootItem);
        tree.setShowRoot(false);
        tree.setCellFactory(tv -> new ToolTreeCell(contentPanel));
        tree.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);

        // Click handling
        tree.setOnMouseClicked(e -> {
            var selected = tree.getSelectionModel().getSelectedItem();
            if (selected == null) return;
            var data = selected.getValue();
            if (data == null || data.isGroup()) return;

            if (e.getButton() == MouseButton.SECONDARY) {
                showContextMenu(data.factory(), e.getScreenX(), e.getScreenY());
            } else if (e.getClickCount() == 2) {
                contentPanel.openToolInNewTab(data.factory());
            } else if (e.getClickCount() <= 1) {
                // clickCount can be 0 on Linux when tree doesn't have focus
                contentPanel.openTool(data.factory());
            }
        });

        // Repaint tree when tabs open/close so indicators update
        contentPanel.addOpenStateListener(() -> tree.refresh());

        VBox.setVgrow(tree, Priority.ALWAYS);
        getChildren().add(tree);
    }

    // ---------------------------------------------------------------
    // Tree construction
    // ---------------------------------------------------------------

    private void buildTreeModel() {
        var registry = ToolRegistry.getInstance();
        groupItems.clear();

        for (var group : registry.getGroups()) {
            var groupItem = new TreeItem<>(TreeItemData.group(group.menuTitle()));
            groupItem.setExpanded(group.initiallyExpanded());
            groupItems.put(group.id(), groupItem);

            for (var factory : registry.getToolsByGroup(group.id())) {
                groupItem.getChildren().add(
                        new TreeItem<>(TreeItemData.tool(
                                factory.getPresentation().menuTitle(), factory)));
            }
            if (!groupItem.getChildren().isEmpty()) {
                rootItem.getChildren().add(groupItem);
            }
        }

        // Ungrouped tools
        for (String id : registry.getUngroupedToolIds()) {
            var factory = registry.getTool(id);
            if (factory != null) {
                rootItem.getChildren().add(
                        new TreeItem<>(TreeItemData.tool(
                                factory.getPresentation().menuTitle(), factory)));
            }
        }
    }

    // ---------------------------------------------------------------
    // Filter
    // ---------------------------------------------------------------

    private void filter(String query) {
        if (query == null) query = "";
        String q = query.toLowerCase().trim();

        if (q.isEmpty()) {
            rootItem.getChildren().clear();
            buildTreeModel();
            return;
        }

        // Flatten filtered view — no groups
        rootItem.getChildren().clear();
        var registry = ToolRegistry.getInstance();
        for (var factory : registry.getAllTools()) {
            var pres = factory.getPresentation();
            if (pres.menuTitle().toLowerCase().contains(q)
                    || pres.id().toLowerCase().contains(q)) {
                var item = new TreeItem<>(TreeItemData.tool(pres.menuTitle(), factory));
                item.setExpanded(true);
                rootItem.getChildren().add(item);
            }
        }
        // Expand the root so filtered tools are visible
        rootItem.setExpanded(true);
    }

    // ---------------------------------------------------------------
    // Context menu
    // ---------------------------------------------------------------

    private void showContextMenu(ToolFactory<?> factory, double x, double y) {
        var menu = new ContextMenu();
        var openItem = new MenuItem("Open this tool");
        openItem.setOnAction(e -> contentPanel.openTool(factory));
        menu.getItems().add(openItem);

        var newTabItem = new MenuItem("Open in new tab");
        newTabItem.setOnAction(e -> contentPanel.openToolInNewTab(factory));
        menu.getItems().add(newTabItem);

        menu.show(tree, x, y);
    }

    // ---------------------------------------------------------------
    // Programmatic selection
    // ---------------------------------------------------------------

    /** Select and scroll to a tool by its ID. */
    public void selectTool(String toolId) {
        selectInTree(rootItem, toolId);
    }

    private void selectInTree(TreeItem<TreeItemData> node, String toolId) {
        for (var child : node.getChildren()) {
            var data = child.getValue();
            if (data != null && !data.isGroup()
                    && data.factory().getId().equals(toolId)) {
                tree.getSelectionModel().select(child);
                tree.scrollTo(tree.getRow(child));
                return;
            }
            selectInTree(child, toolId);
        }
    }

    /** Focus the search field. */
    public void focusSearch() {
        searchField.requestFocus();
        searchField.selectAll();
    }

    /** Returns the selected tool ID, or empty string. */
    public String getSelectedToolId() {
        var selected = tree.getSelectionModel().getSelectedItem();
        if (selected != null) {
            var data = selected.getValue();
            if (data != null && !data.isGroup()) {
                return data.factory().getId();
            }
        }
        return "";
    }

    // ---------------------------------------------------------------
    // Custom cell factory
    // ---------------------------------------------------------------

    private static final class ToolTreeCell extends TreeCell<TreeItemData> {
        private final FxContentPanel contentPanel;

        ToolTreeCell(FxContentPanel contentPanel) {
            this.contentPanel = contentPanel;
        }

        @Override
        protected void updateItem(TreeItemData item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || item == null) {
                setText(null);
                setGraphic(null);
                setStyle("");
                return;
            }

            if (item.isGroup()) {
                setText(item.label());
                setStyle("-fx-font-weight: normal;");
            } else {
                boolean isOpen = contentPanel.isToolOpen(item.factory().getId());
                String prefix = isOpen ? "● " : ""; // ● indicator
                setText(prefix + item.label());
                setStyle(isOpen ? "-fx-font-weight: bold;" : "-fx-font-weight: normal;");
            }
        }
    }
}
