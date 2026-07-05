package gt.devtools.tools.api.fx;

import gt.devtools.common.ValueProperty;
import gt.devtools.settings.ToolConfiguration;
import javafx.concurrent.Task;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;

/**
 * JavaFX base class for text-to-text transformation tools (horizontal split).
 * <p>
 * Layout: source editor on left, action bar + result editor on right.
 */
public abstract class TextTransformerFx extends DeveloperToolFx {

    protected FxTextEditor sourceEditor;
    protected FxTextEditor resultEditor;
    protected final ValueProperty<Boolean> liveTransformation;

    protected TextTransformerFx(ToolConfiguration config) {
        super(config);
        this.liveTransformation = registerConfig("liveTransformation", false);
    }

    @Override
    protected void buildUi(BorderPane panel) {
        var splitPane = new SplitPane();
        splitPane.setOrientation(Orientation.HORIZONTAL);
        splitPane.setDividerPositions(0.50);

        sourceEditor = new FxTextEditor(FxTextEditor.Mode.INPUT, registerInput("sourceText", ""));
        splitPane.getItems().add(sourceEditor);

        var rightPanel = new BorderPane();
        rightPanel.setTop(buildActionBar());
        resultEditor = new FxTextEditor(FxTextEditor.Mode.OUTPUT, registerInput("resultText", ""));
        rightPanel.setCenter(resultEditor);
        splitPane.getItems().add(rightPanel);

        panel.setCenter(splitPane);
    }

    /** Build the action bar above the result editor. */
    protected HBox buildActionBar() {
        var bar = new HBox(8);
        bar.setAlignment(Pos.CENTER_LEFT);
        bar.setStyle("-fx-padding: 4 0;");

        var transformBtn = new Button(getTransformLabel());
        transformBtn.setOnAction(e -> transform());
        bar.getChildren().add(transformBtn);

        var liveCheck = new CheckBox("Live");
        liveCheck.setSelected(liveTransformation.get());
        liveCheck.setOnAction(e -> liveTransformation.set(liveCheck.isSelected()));
        bar.getChildren().add(liveCheck);

        var copyBtn = new Button("Copy Result");
        copyBtn.setOnAction(e -> resultEditor.copyToClipboard());
        bar.getChildren().add(copyBtn);

        return bar;
    }

    protected String getTransformLabel() {
        return "Transform";
    }

    // ---------------------------------------------------------------
    // Lifecycle
    // ---------------------------------------------------------------

    @Override
    public void activated() {
        sourceEditor.textProperty().addListener((obs, old, text) -> {
            if (Boolean.TRUE.equals(liveTransformation.get())) transform();
        });
    }

    // ---------------------------------------------------------------
    // Transformation
    // ---------------------------------------------------------------

    /** Run the transformation on a background thread. */
    public void transform() {
        String input = sourceEditor.getText();
        var task = new Task<String>() {
            @Override
            protected String call() throws Exception {
                return doTransform(input);
            }
        };
        task.setOnSucceeded(e -> resultEditor.setText(task.getValue()));
        task.setOnFailed(e ->
                resultEditor.setText("Error: " + task.getException().getMessage()));
        new Thread(task).start();
    }

    /** The transformation logic. */
    protected abstract String doTransform(String input) throws Exception;
}
