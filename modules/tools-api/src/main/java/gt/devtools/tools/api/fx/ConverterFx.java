package gt.devtools.tools.api.fx;

import gt.devtools.common.ValueProperty;
import gt.devtools.settings.ToolConfiguration;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * JavaFX base class for tools that convert text (vertical split layout).
 * <p>
 * Mirrors the Swing {@code Converter} with:
 * <ul>
 *   <li>Vertical SplitPane with source editor on top, target on bottom</li>
 *   <li>Action bar with Convert button, Live checkbox, Copy, Swap</li>
 *   <li>Debounced (300ms) live conversion via background Task</li>
 * </ul>
 */
public abstract class ConverterFx extends DeveloperToolFx {

    protected FxTextEditor sourceEditor;
    protected FxTextEditor targetEditor;
    protected final ValueProperty<Boolean> liveConversion;

    private final ScheduledExecutorService scheduler =
            Executors.newSingleThreadScheduledExecutor(r -> {
                Thread t = new Thread(r, "fx-converter-worker");
                t.setDaemon(true);
                return t;
            });
    private ScheduledFuture<?> pendingConversion;

    protected ConverterFx(ToolConfiguration config) {
        super(config);
        this.liveConversion = registerConfig("liveConversion", false);
    }

    @Override
    protected void buildUi(BorderPane panel) {
        var splitPane = new javafx.scene.control.SplitPane();
        splitPane.setOrientation(Orientation.VERTICAL);
        splitPane.setDividerPositions(0.48);

        sourceEditor = createSourceEditor();
        splitPane.getItems().add(sourceEditor);

        var actionBar = buildActionBar();
        targetEditor = createTargetEditor();

        var rightPanel = new BorderPane();
        rightPanel.setTop(actionBar);
        rightPanel.setCenter(targetEditor);
        splitPane.getItems().add(rightPanel);

        panel.setCenter(splitPane);
    }

    protected FxTextEditor createSourceEditor() {
        var editor = new FxTextEditor(FxTextEditor.Mode.INPUT, sourceTextProperty());
        editor.textProperty().addListener((obs, old, text) -> onSourceChanged());
        return editor;
    }

    protected FxTextEditor createTargetEditor() {
        return new FxTextEditor(FxTextEditor.Mode.OUTPUT, targetTextProperty());
    }

    /** Build the action bar between source and target editors. */
    protected HBox buildActionBar() {
        var bar = new HBox(8);
        bar.setAlignment(Pos.CENTER_LEFT);
        bar.setStyle("-fx-padding: 4 0;");

        var convertBtn = new Button(getConvertButtonLabel());
        convertBtn.setOnAction(e -> convert());
        bar.getChildren().add(convertBtn);

        var liveCheck = new CheckBox("Live");
        liveCheck.setSelected(liveConversion.get());
        liveCheck.setOnAction(e -> liveConversion.set(liveCheck.isSelected()));
        bar.getChildren().add(liveCheck);

        var copyBtn = new Button("Copy Result");
        copyBtn.setOnAction(e -> targetEditor.copyToClipboard());
        bar.getChildren().add(copyBtn);

        var swapBtn = new Button("↑↓ Swap");
        swapBtn.setOnAction(e -> swap());
        bar.getChildren().add(swapBtn);

        return bar;
    }

    protected String getConvertButtonLabel() {
        return "Convert ↓";
    }

    // ---------------------------------------------------------------
    // Lifecycle
    // ---------------------------------------------------------------

    @Override
    public void activated() {
        liveConversion.addListener(p -> {
            if (Boolean.TRUE.equals(p.get())) convert();
        });
    }

    @Override
    public void dispose() {
        super.dispose();
        scheduler.shutdownNow();
    }

    // ---------------------------------------------------------------
    // Conversion engine
    // ---------------------------------------------------------------

    protected void onSourceChanged() {
        if (Boolean.TRUE.equals(liveConversion.get())) scheduleConversion();
    }

    private void scheduleConversion() {
        if (pendingConversion != null) pendingConversion.cancel(false);
        pendingConversion = scheduler.schedule(this::convert, 300, TimeUnit.MILLISECONDS);
    }

    /** Run the forward conversion on a background thread. */
    public void convert() {
        var task = new Task<byte[]>() {
            @Override
            protected byte[] call() throws Exception {
                return doConvertForward(sourceEditor.getBytes());
            }
        };
        task.setOnSucceeded(e -> {
            try {
                targetEditor.setBytes(task.getValue());
            } catch (Exception ex) {
                targetEditor.setText("Error: " + ex.getMessage());
            }
        });
        task.setOnFailed(e -> {
            targetEditor.setText("Error: " + task.getException().getMessage());
        });
        new Thread(task).start();
    }

    /** Swap source and target content. */
    protected void swap() {
        String sourceText = sourceEditor.getText();
        String targetText = targetEditor.getText();
        sourceEditor.setText(targetText);
        targetEditor.setText(sourceText);
    }

    // ---------------------------------------------------------------
    // Persistence
    // ---------------------------------------------------------------

    protected ValueProperty<String> sourceTextProperty() {
        return registerInput("sourceText", "");
    }

    protected ValueProperty<String> targetTextProperty() {
        return registerInput("targetText", "");
    }

    // ---------------------------------------------------------------
    // Subclass contract
    // ---------------------------------------------------------------

    /** Forward conversion: source → target. */
    protected abstract byte[] doConvertForward(byte[] input) throws Exception;
}
