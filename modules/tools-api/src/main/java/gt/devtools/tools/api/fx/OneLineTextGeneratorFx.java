package gt.devtools.tools.api.fx;

import gt.devtools.common.ValueProperty;
import gt.devtools.settings.ToolConfiguration;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

/**
 * JavaFX base for generator tools (UUID, password, etc.).
 * <p>
 * Layout:
 * <pre>
 * ┌──────────────────────────┐
 * │  Configuration controls  │
 * ├──────────────────────────┤
 * │  Generated Value         │
 * │  [Regenerate] [Copy]     │
 * │  ┌────────────────────┐  │
 * │  │ (FxTextEditor)     │  │
 * │  └────────────────────┘  │
 * ├──────────────────────────┤
 * │  Bulk Generation         │
 * │  Count: [10] [Gen] [Copy] [Clear] │
 * │  ┌────────────────────┐  │
 * │  │ (FxTextEditor)     │  │
 * │  └────────────────────┘  │
 * └──────────────────────────┘
 * </pre>
 */
public abstract class OneLineTextGeneratorFx extends DeveloperToolFx {

    protected FxTextEditor valueOutput;
    protected FxTextEditor bulkOutput;
    protected Button regenerateButton;
    protected Label errorLabel;
    protected ValueProperty<Integer> bulkCount;

    private boolean bulkPanelVisible;

    protected OneLineTextGeneratorFx(ToolConfiguration config) {
        super(config);
    }

    @Override
    protected void buildUi(BorderPane panel) {
        bulkPanelVisible = supportsBulkGeneration();

        // -- Top section: config + single-value output
        var topSection = new VBox(4);
        topSection.setPadding(new javafx.geometry.Insets(8, 8, 4, 8));

        var configPanel = new VBox(4);
        buildConfigurationUi(configPanel);
        topSection.getChildren().add(configPanel);

        // Value output
        var valuePane = new VBox(4);
        var valueTitle = new Label("Generated Value");
        valueTitle.setStyle("-fx-font-weight: bold;");
        valuePane.getChildren().add(valueTitle);

        var valueBar = new HBox(8);
        valueBar.setAlignment(Pos.CENTER_LEFT);
        regenerateButton = new Button("Regenerate");
        regenerateButton.setOnAction(e -> regenerate());
        valueBar.getChildren().add(regenerateButton);

        var copyBtn = new Button("Copy");
        copyBtn.setOnAction(e -> valueOutput.copyToClipboard());
        valueBar.getChildren().add(copyBtn);

        errorLabel = new Label(" ");
        errorLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: -fx-text-background-color;");
        valueBar.getChildren().add(errorLabel);

        valuePane.getChildren().add(valueBar);

        valueOutput = new FxTextEditor(FxTextEditor.Mode.OUTPUT);
        valueOutput.setSyntaxStyle("text/plain");
        VBox.setVgrow(valueOutput, Priority.ALWAYS);
        valuePane.getChildren().add(valueOutput);
        VBox.setVgrow(valuePane, Priority.ALWAYS);
        topSection.getChildren().add(valuePane);

        // -- Bottom: bulk generation
        if (bulkPanelVisible) {
            var bulkPane = buildBulkPanel();

            var splitPane = new javafx.scene.control.SplitPane();
            splitPane.setOrientation(Orientation.VERTICAL);
            splitPane.setDividerPositions(0.50);
            splitPane.getItems().addAll(topSection, bulkPane);
            panel.setCenter(splitPane);
        } else {
            panel.setCenter(topSection);
        }

        addAdditionalUi(panel);
    }

    private VBox buildBulkPanel() {
        var bulkPane = new VBox(4);
        bulkPane.setPadding(new javafx.geometry.Insets(4, 8, 8, 8));

        var bulkTitle = new Label("Bulk Generation");
        bulkTitle.setStyle("-fx-font-weight: bold;");
        bulkPane.getChildren().add(bulkTitle);

        var bulkBar = new HBox(8);
        bulkBar.setAlignment(Pos.CENTER_LEFT);
        bulkBar.getChildren().add(new Label("Count:"));

        var countSpinner = new Spinner<Integer>(1, 99999, 10);
        bulkCount = registerConfig("bulkCount", 10);
        countSpinner.getValueFactory().setValue(bulkCount.get());
        countSpinner.valueProperty().addListener((obs, old, val) -> bulkCount.set(val));
        bulkBar.getChildren().add(countSpinner);

        var genBtn = new Button("Generate");
        genBtn.setOnAction(e -> generateBulk());
        bulkBar.getChildren().add(genBtn);

        var copyBulkBtn = new Button("Copy");
        copyBulkBtn.setOnAction(e -> bulkOutput.copyToClipboard());
        bulkBar.getChildren().add(copyBulkBtn);

        var clearBtn = new Button("Clear");
        clearBtn.setOnAction(e -> bulkOutput.setText(""));
        bulkBar.getChildren().add(clearBtn);

        bulkPane.getChildren().add(bulkBar);

        bulkOutput = new FxTextEditor(FxTextEditor.Mode.OUTPUT);
        bulkOutput.setSyntaxStyle("text/plain");
        VBox.setVgrow(bulkOutput, Priority.ALWAYS);
        bulkPane.getChildren().add(bulkOutput);

        return bulkPane;
    }

    @Override
    public void activated() {
        regenerate();
    }

    // ---------------------------------------------------------------
    // Subclass contract
    // ---------------------------------------------------------------

    protected abstract void buildConfigurationUi(VBox configPanel);

    protected abstract String generate() throws Exception;

    protected boolean supportsBulkGeneration() { return true; }

    protected void addAdditionalUi(BorderPane panel) {}

    // ---------------------------------------------------------------
    // Actions
    // ---------------------------------------------------------------

    protected void regenerate() {
        try {
            String value = generate();
            valueOutput.setText(value);
            errorLabel.setText(" ");
        } catch (Exception e) {
            errorLabel.setText("Error: " + e.getMessage());
            valueOutput.setText("—");
        }
    }

    protected void generateBulk() {
        int count = bulkCount != null ? bulkCount.get() : 10;
        var sb = new StringBuilder();
        for (int i = 0; i < count; i++) {
            try {
                sb.append(generate()).append("\n");
            } catch (Exception e) {
                sb.append("ERROR: ").append(e.getMessage()).append("\n");
            }
        }
        if (bulkOutput != null) {
            bulkOutput.setText(sb.toString());
        }
    }
}
