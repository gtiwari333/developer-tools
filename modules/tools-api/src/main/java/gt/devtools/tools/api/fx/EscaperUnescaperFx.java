package gt.devtools.tools.api.fx;

import gt.devtools.settings.ToolConfiguration;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.layout.HBox;

/**
 * JavaFX convenience base for escape/unescape tools.
 * Overrides the action bar with "Escape ↓" / "Unescape ↑" labels.
 */
public abstract class EscaperUnescaperFx extends BidirectionalConverterFx {

    protected EscaperUnescaperFx(ToolConfiguration config) {
        super(config);
    }

    @Override
    protected HBox buildActionBar() {
        var bar = new HBox(8);
        bar.setAlignment(Pos.CENTER_LEFT);
        bar.setStyle("-fx-padding: 4 0;");

        var escapeBtn = new Button("Escape ↓");
        escapeBtn.setOnAction(e -> convert());
        bar.getChildren().add(escapeBtn);

        var unescapeBtn = new Button("Unescape ↑");
        unescapeBtn.setOnAction(e -> convertBackward());
        bar.getChildren().add(unescapeBtn);

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
}
