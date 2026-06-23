package gt.devtools.app;

import gt.devtools.settings.AppSettings;
import gt.devtools.settings.SettingsManager;

import javax.swing.*;
import java.awt.*;

/**
 * Settings dialog for configuring theme, update checks, and window behaviour.
 */
final class SettingsDialog extends JDialog {

    private final AppSettings settings;
    private final SettingsManager manager;
    private JComboBox<String> themeCombo;
    private JCheckBox updateCheck;
    private JCheckBox showInternalTools;
    private boolean themeChanged;

    SettingsDialog(JFrame owner, AppSettings settings, SettingsManager manager) {
        super(owner, "Settings", true);
        this.settings = settings;
        this.manager = manager;
        buildUi();
        pack();
        setLocationRelativeTo(owner);
        setResizable(false);
    }

    private void buildUi() {
        var panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        var form = new JPanel(new GridBagLayout());
        var gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(4, 4, 4, 4);
        gbc.gridx = 0; gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;

        // Theme
        form.add(new JLabel("Theme:"), gbc);
        gbc.gridx = 1;
        themeCombo = new JComboBox<>(new String[]{"Dark", "Light", "System"});
        String current = settings.getTheme();
        if ("light".equals(current)) themeCombo.setSelectedItem("Light");
        else if ("dark".equals(current)) themeCombo.setSelectedItem("Dark");
        else themeCombo.setSelectedItem("System");
        form.add(themeCombo, gbc);

        // Check for updates
        gbc.gridx = 0; gbc.gridy = 1;
        form.add(new JLabel("Updates:"), gbc);
        gbc.gridx = 1;
        updateCheck = new JCheckBox("Check for updates on startup", settings.isCheckForUpdates());
        form.add(updateCheck, gbc);

        // Internal tools
        gbc.gridx = 0; gbc.gridy = 2;
        form.add(new JLabel("Developer:"), gbc);
        gbc.gridx = 1;
        showInternalTools = new JCheckBox("Show internal tools", settings.isShowInternalTools());
        form.add(showInternalTools, gbc);

        // Sidebar
        gbc.gridx = 0; gbc.gridy = 3;
        form.add(new JLabel("Sidebar width:"), gbc);
        gbc.gridx = 1;
        var sidebarSpinner = new JSpinner(new javax.swing.SpinnerNumberModel(
                settings.getDividerLocation(), 200, 600, 10));
        sidebarSpinner.addChangeListener(e ->
                settings.setDividerLocation((Integer) sidebarSpinner.getValue()));
        form.add(sidebarSpinner, gbc);

        panel.add(form, BorderLayout.CENTER);

        // Buttons
        var buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        var saveBtn = new JButton("Save");
        saveBtn.addActionListener(e -> save());
        buttons.add(saveBtn);
        var cancelBtn = new JButton("Cancel");
        cancelBtn.addActionListener(e -> dispose());
        buttons.add(cancelBtn);
        panel.add(buttons, BorderLayout.SOUTH);

        setContentPane(panel);
    }

    private void save() {
        String selected = (String) themeCombo.getSelectedItem();
        String oldTheme = settings.getTheme();
        if ("Light".equals(selected)) settings.setTheme("light");
        else if ("Dark".equals(selected)) settings.setTheme("dark");
        else settings.setTheme("auto");
        themeChanged = !settings.getTheme().equals(oldTheme);

        settings.setCheckForUpdates(updateCheck.isSelected());
        settings.setShowInternalTools(showInternalTools.isSelected());
        manager.saveAppSettings(settings);

        if (themeChanged) {
            JOptionPane.showMessageDialog(this,
                    "Theme change will apply on next restart.",
                    "Theme Changed", JOptionPane.INFORMATION_MESSAGE);
        }
        dispose();
    }
}
