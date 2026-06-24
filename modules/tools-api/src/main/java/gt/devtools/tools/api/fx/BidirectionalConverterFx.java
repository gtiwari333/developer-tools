package gt.devtools.tools.api.fx;

import gt.devtools.settings.ToolConfiguration;
import javafx.concurrent.Task;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.layout.HBox;

/**
 * JavaFX Converter that works in both directions — encode and decode.
 * <p>
 * Adds "Decode ↑" button and handles backward conversion.
 * Tools should extend {@link EncoderDecoderFx} or {@link EscaperUnescaperFx}.
 */
public abstract class BidirectionalConverterFx extends ConverterFx {

    protected BidirectionalConverterFx(ToolConfiguration config) {
        super(config);
    }

    @Override
    protected String getConvertButtonLabel() {
        return "Encode ↓";
    }

    @Override
    protected HBox buildActionBar() {
        var bar = new HBox(8);
        bar.setAlignment(Pos.CENTER_LEFT);
        bar.setStyle("-fx-padding: 4 0;");

        var forwardBtn = new Button("Encode ↓");
        forwardBtn.setOnAction(e -> convert());
        bar.getChildren().add(forwardBtn);

        var backwardBtn = new Button("Decode ↑");
        backwardBtn.setOnAction(e -> convertBackward());
        bar.getChildren().add(backwardBtn);

        var liveCheck = new CheckBox("Live");
        liveCheck.setSelected(liveConversion.get());
        liveCheck.setOnAction(e -> liveConversion.set(liveCheck.isSelected()));
        bar.getChildren().add(liveCheck);

        var copySourceBtn = new Button("Copy Source");
        copySourceBtn.setOnAction(e -> sourceEditor.copyToClipboard());
        bar.getChildren().add(copySourceBtn);

        var copyTargetBtn = new Button("Copy Result");
        copyTargetBtn.setOnAction(e -> targetEditor.copyToClipboard());
        bar.getChildren().add(copyTargetBtn);

        var swapBtn = new Button("↑↓ Swap");
        swapBtn.setOnAction(e -> swap());
        bar.getChildren().add(swapBtn);

        return bar;
    }

    /** Run the backward (decode) conversion. */
    public void convertBackward() {
        var task = new Task<byte[]>() {
            @Override
            protected byte[] call() throws Exception {
                return doConvertBackward(targetEditor.getBytes());
            }
        };
        task.setOnSucceeded(e -> {
            try {
                sourceEditor.setBytes(task.getValue());
            } catch (Exception ex) {
                sourceEditor.setText("Error: " + ex.getMessage());
            }
        });
        task.setOnFailed(e ->
                sourceEditor.setText("Error: " + task.getException().getMessage()));
        new Thread(task).start();
    }

    /** Backward conversion: target → source. */
    protected abstract byte[] doConvertBackward(byte[] input) throws Exception;
}
