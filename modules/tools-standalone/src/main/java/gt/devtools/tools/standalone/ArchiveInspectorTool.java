package gt.devtools.tools.standalone;

import gt.devtools.settings.ToolConfiguration;
import gt.devtools.tools.api.DeveloperTool;
import gt.devtools.tools.api.ToolFactory;
import gt.devtools.tools.api.ToolPresentation;
import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/** Inspects archive files (ZIP, TAR, TAR.GZ). Drag-and-drop a file or browse. */
public final class ArchiveInspectorTool extends DeveloperTool {

    private DefaultTableModel tableModel;
    private JTable table;
    private JLabel statusLabel;
    private File currentArchive;

    private ArchiveInspectorTool(ToolConfiguration config) { super(config); }

    @Override
    protected void buildUi(JPanel panel) {
        panel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        panel.setLayout(new BorderLayout(0, 4));

        var topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        var browseBtn = new JButton("Open Archive...");
        browseBtn.addActionListener(e -> browse());
        topPanel.add(browseBtn);
        statusLabel = new JLabel("Drag a ZIP/TAR/TAR.GZ file here or click Browse.");
        topPanel.add(statusLabel);
        panel.add(topPanel, BorderLayout.NORTH);

        tableModel = new DefaultTableModel(
                new String[]{"Name", "Size", "Modified", "Type"}, 0);
        table = new JTable(tableModel);
        table.setAutoCreateRowSorter(true);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);

        // Drag-and-drop support
        panel.setTransferHandler(new TransferHandler() {
            @Override public boolean canImport(TransferSupport support) {
                return support.isDataFlavorSupported(DataFlavor.javaFileListFlavor);
            }
            @Override @SuppressWarnings("unchecked")
            public boolean importData(TransferSupport support) {
                try {
                    var files = (List<File>) support.getTransferable()
                            .getTransferData(DataFlavor.javaFileListFlavor);
                    if (!files.isEmpty()) loadArchive(files.get(0));
                } catch (Exception ignored) {}
                return true;
            }
        });
    }

    private void browse() {
        var chooser = new JFileChooser();
        if (chooser.showOpenDialog(component) == JFileChooser.APPROVE_OPTION) {
            loadArchive(chooser.getSelectedFile());
        }
    }

    private void loadArchive(File file) {
        currentArchive = file;
        tableModel.setRowCount(0);
        try (InputStream is = new BufferedInputStream(Files.newInputStream(file.toPath()))) {
            InputStream in = is;
            ArchiveInputStream<?> ais;
            if (file.getName().endsWith(".tar.gz") || file.getName().endsWith(".tgz")) {
                in = new GzipCompressorInputStream(is);
                ais = new TarArchiveInputStream(in);
            } else if (file.getName().endsWith(".tar")) {
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
                tableModel.addRow(new Object[]{entry.getName(), size, mod, type});
            }
            statusLabel.setText(file.getName() + " — " + tableModel.getRowCount() + " entries");
        } catch (Exception e) {
            statusLabel.setText("Error: " + e.getMessage());
        }
    }

    private static String formatSize(long bytes) {
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return String.format("%.1f KB", bytes / 1024.0);
        return String.format("%.1f MB", bytes / (1024.0 * 1024));
    }

    public static final class Factory implements ToolFactory<ArchiveInspectorTool> {
        public Factory() {}
        @Override public String getId() { return "unarchiver"; }
        @Override public ToolPresentation getPresentation() {
            return ToolPresentation.of("unarchiver", "Archive Inspector", "Archive Inspector")
                    .withGroupId("data-inspectors");
        }
        @Override public ArchiveInspectorTool create(ToolConfiguration config) { return new ArchiveInspectorTool(config); }
    }
}
