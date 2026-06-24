package gt.devtools.app;

import gt.devtools.tools.api.ToolFactory;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;

/**
 * JavaFX tool title bar shown above each tool's workbench tabs.
 * <p>
 * Left: tool name + help button
 * Right: new-workbench (+), reset (↺) buttons
 * Bottom: separator line
 */
public final class FxToolTitleBar extends BorderPane {

    private final ToolFactory<?> factory;
    private final FxWorkbenchTabs workbench;

    public FxToolTitleBar(ToolFactory<?> factory, FxWorkbenchTabs workbench) {
        this.factory = factory;
        this.workbench = workbench;

        setPadding(new Insets(4, 8, 4, 8));
        setStyle("-fx-border-color: -fx-box-border; -fx-border-width: 0 0 1 0;");

        // Left: title + help
        var leftBox = new HBox(8);
        leftBox.setAlignment(Pos.CENTER_LEFT);

        var titleLabel = new Label(factory.getPresentation().contentTitle());
        titleLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 13px;");
        leftBox.getChildren().add(titleLabel);

        // Help button with description tooltip
        String desc = factory.getPresentation().description();
        if (desc != null && !desc.isEmpty()) {
            var helpBtn = new Button("?");
            helpBtn.setStyle("-fx-background-color: transparent; -fx-font-size: 11px; "
                    + "-fx-padding: 0 4;");
            helpBtn.setTooltip(new Tooltip(desc));
            leftBox.getChildren().add(helpBtn);
        }

        setLeft(leftBox);

        // Right: action buttons
        var rightBox = new HBox(4);
        rightBox.setAlignment(Pos.CENTER_RIGHT);

        var newBtn = new Button("+");
        newBtn.setStyle("-fx-background-color: transparent; -fx-font-size: 14px; "
                + "-fx-font-weight: bold; -fx-padding: 0 6;");
        newBtn.setTooltip(new Tooltip("New workbench"));
        newBtn.setOnAction(e -> workbench.newWorkbench());
        rightBox.getChildren().add(newBtn);

        var resetBtn = new Button("↺");
        resetBtn.setStyle("-fx-background-color: transparent; -fx-font-size: 14px; "
                + "-fx-padding: 0 6;");
        resetBtn.setTooltip(new Tooltip("Reset tool to defaults"));
        resetBtn.setOnAction(e -> workbench.resetCurrentWorkbench());
        rightBox.getChildren().add(resetBtn);

        setRight(rightBox);
    }
}
