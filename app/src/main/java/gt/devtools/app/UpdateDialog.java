package gt.devtools.app;

import gt.devtools.settings.AppSettings;
import gt.devtools.settings.SettingsManager;
import gt.devtools.settings.UpdateChecker;

import javax.swing.*;
import java.awt.*;
import java.io.BufferedInputStream;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

/**
 * Modal dialog shown when an update is available. Allows the user to
 * download and install the new version, or skip.
 */
final class UpdateDialog extends JDialog {

    private final UpdateChecker.UpdateInfo update;
    private final SettingsManager settingsManager;
    private JLabel statusLabel;
    private JProgressBar progressBar;
    private JButton downloadBtn;
    private JButton skipBtn;

    UpdateDialog(JFrame owner, UpdateChecker.UpdateInfo update, SettingsManager settingsManager) {
        super(owner, "Update Available", true);
        this.update = update;
        this.settingsManager = settingsManager;
        buildUi();
        pack();
        setLocationRelativeTo(owner);
    }

    private void buildUi() {
        var panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        // Header
        var header = new JPanel(new BorderLayout());
        var title = new JLabel("Version " + update.version() + " is available!");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 16f));
        header.add(title, BorderLayout.NORTH);
        var subtitle = new JLabel("You are running an older version. Download the update?");
        subtitle.setBorder(BorderFactory.createEmptyBorder(4, 0, 8, 0));
        header.add(subtitle, BorderLayout.CENTER);
        panel.add(header, BorderLayout.NORTH);

        // Changelog (truncated)
        String changelog = update.changelog();
        if (changelog != null && changelog.length() > 2000) {
            changelog = changelog.substring(0, 2000) + "\n\n... (see GitHub for full changelog)";
        }
        var changelogArea = new JTextArea(changelog != null ? changelog : "", 10, 50);
        changelogArea.setEditable(false);
        changelogArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 11));
        panel.add(new JScrollPane(changelogArea), BorderLayout.CENTER);

        // Status and progress
        var statusPanel = new JPanel(new BorderLayout(5, 5));
        statusLabel = new JLabel(" ");
        statusPanel.add(statusLabel, BorderLayout.NORTH);
        progressBar = new JProgressBar(0, 100);
        progressBar.setVisible(false);
        statusPanel.add(progressBar, BorderLayout.CENTER);
        panel.add(statusPanel, BorderLayout.SOUTH);

        // Buttons
        var buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        skipBtn = new JButton("Skip This Version");
        skipBtn.addActionListener(e -> { settingsManager.saveAppSettings(
                settingsManager.loadAppSettings()); dispose(); });
        buttonPanel.add(skipBtn);

        downloadBtn = new JButton("Download & Install");
        downloadBtn.addActionListener(e -> downloadAndInstall());
        buttonPanel.add(downloadBtn);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        setContentPane(panel);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    }

    private void downloadAndInstall() {
        downloadBtn.setEnabled(false);
        skipBtn.setEnabled(false);
        progressBar.setVisible(true);
        statusLabel.setText("Downloading...");

        new SwingWorker<Path, Integer>() {
            @Override
            protected Path doInBackground() throws Exception {
                String url = update.findDownloadUrl();
                if (url == null) throw new RuntimeException("No download URL found for this platform");
                Path tmp = Files.createTempFile("devtools-update-", ".bin");
                try (InputStream in = new BufferedInputStream(
                        URI.create(url).toURL().openStream())) {
                    long total = update.assetSize();
                    byte[] buf = new byte[8192];
                    int read, downloaded = 0;
                    while ((read = in.read(buf)) != -1) {
                        Files.write(tmp, java.util.Arrays.copyOf(buf, read),
                                java.nio.file.StandardOpenOption.APPEND,
                                java.nio.file.StandardOpenOption.CREATE);
                        downloaded += read;
                        if (total > 0) setProgress((int) (downloaded * 100 / total));
                    }
                }
                // Make executable on Unix
                if (!System.getProperty("os.name").toLowerCase().contains("win")) {
                    tmp.toFile().setExecutable(true);
                }
                return tmp;
            }

            @Override
            protected void done() {
                try {
                    Path downloaded = get();
                    statusLabel.setText("Downloaded to: " + downloaded);
                    progressBar.setValue(100);
                    JOptionPane.showMessageDialog(UpdateDialog.this,
                            "Update downloaded to:\n" + downloaded +
                                    "\n\nPlease replace the current binary and restart.",
                            "Download Complete", JOptionPane.INFORMATION_MESSAGE);
                    dispose();
                } catch (Exception e) {
                    statusLabel.setText("Download failed: " + e.getMessage());
                    downloadBtn.setEnabled(true);
                    skipBtn.setEnabled(true);
                }
            }
        }.execute();
    }
}
