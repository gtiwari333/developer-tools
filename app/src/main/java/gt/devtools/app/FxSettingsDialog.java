package gt.devtools.app;

import gt.devtools.settings.AppSettings;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;

/**
 * JavaFX settings dialog for application-level preferences.
 * <p>
 * Edits theme, update check, and internal-tool visibility.
 * Changes are applied immediately (theme) or on next restart (internal tools).
 */
public final class FxSettingsDialog extends Dialog<AppSettings> {

    private final AppSettings original;
    private final ComboBox<String> themeCombo;
    private final CheckBox updateCheck;
    private final CheckBox internalToolsCheck;

    public FxSettingsDialog(AppSettings current) {
        this.original = current;

        setTitle("Settings");
        setHeaderText("Application Settings");
        setResizable(false);

        // Build form
        var grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 20, 10, 20));

        int row = 0;

        // Theme
        grid.add(new Label("Theme:"), 0, row);
        themeCombo = new ComboBox<>();
        themeCombo.getItems().addAll("Dark", "Light");
        themeCombo.setValue("dark".equals(current.getTheme()) ? "Dark" : "Light");
        grid.add(themeCombo, 1, row++);

        // Check for updates
        updateCheck = new CheckBox("Check for updates on startup");
        updateCheck.setSelected(current.isCheckForUpdates());
        grid.add(updateCheck, 0, row++, 2, 1);

        // Show internal tools
        internalToolsCheck = new CheckBox("Show internal / developer tools");
        internalToolsCheck.setSelected(current.isShowInternalTools());
        grid.add(internalToolsCheck, 0, row++, 2, 1);

        getDialogPane().setContent(grid);

        // Buttons
        var okType = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
        var cancelType = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        getDialogPane().getButtonTypes().addAll(okType, cancelType);

        // Result converter
        setResultConverter(buttonType -> {
            if (buttonType == okType) {
                return buildResult();
            }
            return null;
        });
    }

    private AppSettings buildResult() {
        var result = new AppSettings();
        // Copy geometry from original so those aren't lost
        result.setWindowWidth(original.getWindowWidth());
        result.setWindowHeight(original.getWindowHeight());
        result.setWindowX(original.getWindowX());
        result.setWindowY(original.getWindowY());
        result.setDividerLocation(original.getDividerLocation());
        result.setLastSelectedTool(original.getLastSelectedTool());

        // Apply new values
        result.setTheme(themeCombo.getValue().toLowerCase());
        result.setCheckForUpdates(updateCheck.isSelected());
        result.setShowInternalTools(internalToolsCheck.isSelected());

        return result;
    }
}
