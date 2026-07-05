package gt.devtools.tools.standalone;

import gt.devtools.settings.ToolConfiguration;
import gt.devtools.tools.api.ToolPresentation;
import gt.devtools.tools.api.fx.DeveloperToolFx;
import gt.devtools.tools.api.fx.ToolFxFactory;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;
import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;

import java.io.*;
import java.nio.file.Files;

/** JavaFX version: Inspects archive files (ZIP, TAR, TAR.GZ). */
public final class ArchiveInspectorToolFx extends DeveloperToolFx {

    private final ObservableList<ArchiveEntryRow> entries = FXCollections.observableArrayList();
    private TableView<ArchiveEntryRow> table;
    private Label statusLabel;
    private File currentArchive;

    private ArchiveInspectorToolFx(ToolConfiguration config) { super(config); }

    @Override
    protected void buildUi(BorderPane panel) {
        panel.setPadding(new Insets(8));

        // -- Top bar
        var topBar = new HBox(8);
        topBar.setPadding(new Insets(0, 0, 4, 0));

        var browseBtn = new Button("Open Archive...");
        browseBtn.setOnAction(e -> browse());
        topBar.getChildren().add(browseBtn);

        statusLabel = new Label("Drag a ZIP/TAR/TAR.GZ file here or click Browse.");
        topBar.getChildren().add(statusLabel);
        panel.setTop(topBar);

        // -- Table
        table = new TableView<>(entries);
        table.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);

        var nameCol = new TableColumn<ArchiveEntryRow, String>("Name");
        nameCol.setCellValueFactory(data -> data.getValue().nameProperty());

        var sizeCol = new TableColumn<ArchiveEntryRow, String>("Size");
        sizeCol.setCellValueFactory(data -> data.getValue().sizeProperty());

        var modCol = new TableColumn<ArchiveEntryRow, String>("Modified");
        modCol.setCellValueFactory(data -> data.getValue().modifiedProperty());

        var typeCol = new TableColumn<ArchiveEntryRow, String>("Type");
        typeCol.setCellValueFactory(data -> data.getValue().typeProperty());

        table.getColumns().add(nameCol);
        table.getColumns().add(sizeCol);
        table.getColumns().add(modCol);
        table.getColumns().add(typeCol);
        table.setPlaceholder(new Label("Open an archive to inspect its contents."));
        panel.setCenter(table);

        // -- Drag-and-drop support
        panel.setOnDragOver(e -> {
            if (e.getGestureSource() != panel && e.getDragboard().hasFiles()) {
                e.acceptTransferModes(TransferMode.COPY);
            }
            e.consume();
        });

        panel.setOnDragDropped(e -> {
            Dragboard db = e.getDragboard();
            boolean success = false;
            if (db.hasFiles() && !db.getFiles().isEmpty()) {
                loadArchive(db.getFiles().getFirst());
                success = true;
            }
            e.setDropCompleted(success);
            e.consume();
        });
    }

    private void browse() {
        var chooser = new FileChooser();
        chooser.setTitle("Open Archive");
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Archives", "*.zip", "*.tar", "*.tar.gz", "*.tgz"));
        File file = chooser.showOpenDialog(rootPane.getScene().getWindow());
        if (file != null) {
            loadArchive(file);
        }
    }

    private void loadArchive(File file) {
        currentArchive = file;
        entries.clear();

        try (InputStream is = new BufferedInputStream(Files.newInputStream(file.toPath()))) {
            InputStream in = is;
            ArchiveInputStream<?> ais;
            String name = file.getName().toLowerCase();
            if (name.endsWith(".tar.gz") || name.endsWith(".tgz")) {
                in = new GzipCompressorInputStream(is);
                ais = new TarArchiveInputStream(in);
            } else if (name.endsWith(".tar")) {
                ais = new TarArchiveInputStream(in);
            } else {
                ais = new ZipArchiveInputStream(in);
            }

            ArchiveEntry entry;
            while ((entry = ais.getNextEntry()) != null) {
                if (!ais.canReadEntryData(entry)) continue;
                String type = entry.isDirectory() ? "DIR" : "FILE";
                String size = entry.isDirectory() ? "" : formatSize(entry.getSize());
                String mod = entry.getLastModifiedDate() != null
                        ? entry.getLastModifiedDate().toInstant().toString().substring(0, 10) : "";
                entries.add(new ArchiveEntryRow(entry.getName(), size, mod, type));
            }
            statusLabel.setText(file.getName() + " — " + entries.size() + " entries");
        } catch (Exception e) {
            statusLabel.setText("Error: " + e.getMessage());
        }
    }

    private static String formatSize(long bytes) {
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return String.format("%.1f KB", bytes / 1024.0);
        return String.format("%.1f MB", bytes / (1024.0 * 1024));
    }

    /** Table row model for archive entries. */
    public static class ArchiveEntryRow {
        private final javafx.beans.property.SimpleStringProperty name;
        private final javafx.beans.property.SimpleStringProperty size;
        private final javafx.beans.property.SimpleStringProperty modified;
        private final javafx.beans.property.SimpleStringProperty type;

        ArchiveEntryRow(String name, String size, String modified, String type) {
            this.name = new javafx.beans.property.SimpleStringProperty(name);
            this.size = new javafx.beans.property.SimpleStringProperty(size);
            this.modified = new javafx.beans.property.SimpleStringProperty(modified);
            this.type = new javafx.beans.property.SimpleStringProperty(type);
        }

        public javafx.beans.property.SimpleStringProperty nameProperty() { return name; }
        public javafx.beans.property.SimpleStringProperty sizeProperty() { return size; }
        public javafx.beans.property.SimpleStringProperty modifiedProperty() { return modified; }
        public javafx.beans.property.SimpleStringProperty typeProperty() { return type; }
    }

    public static final class Factory implements ToolFxFactory<ArchiveInspectorToolFx> {
        public Factory() {}
        @Override public String getId() { return "unarchiver"; }
        @Override public ToolPresentation getPresentation() {
            return ToolPresentation.of("unarchiver", "Archive Inspector", "Archive Inspector")
                    .withGroupId("data-inspectors");
        }
        @Override
        public ArchiveInspectorToolFx create(ToolConfiguration config) {
            return new ArchiveInspectorToolFx(config);
        }
    }
}
