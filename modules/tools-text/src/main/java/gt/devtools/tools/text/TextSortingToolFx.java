package gt.devtools.tools.text;

import gt.devtools.common.ValueProperty;
import gt.devtools.settings.ToolConfiguration;
import gt.devtools.tools.api.ToolPresentation;
import gt.devtools.tools.api.fx.TextTransformerFx;
import gt.devtools.tools.api.fx.ToolFxFactory;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;

import java.util.Arrays;
import java.util.Comparator;

/**
 * JavaFX version: Sorts lines of text in ascending or descending order.
 */
public final class TextSortingToolFx extends TextTransformerFx {

    private final ValueProperty<String> direction;

    private TextSortingToolFx(ToolConfiguration config) {
        super(config);
        this.direction = registerConfig("sortDirection", "Ascending");
    }

    @Override
    protected void buildUi(BorderPane panel) {
        var configBar = new HBox(8);
        configBar.setStyle("-fx-padding: 4 0;");
        configBar.getChildren().add(new Label("Order:"));
        var combo = new ComboBox<String>();
        combo.getItems().addAll("Ascending", "Descending");
        combo.setValue(direction.get());
        combo.setOnAction(e -> {
            direction.set(combo.getValue());
            if (Boolean.TRUE.equals(liveTransformation.get())) transform();
        });
        configBar.getChildren().add(combo);
        panel.setTop(configBar);
        super.buildUi(panel);
    }

    @Override
    protected String getTransformLabel() { return "Sort"; }

    @Override
    protected String doTransform(String input) {
        if ("Descending".equals(direction.get())) {
            return sortDescending(input);
        }
        return sortAscending(input);
    }

    /** Sorts lines alphabetically in ascending order. */
    public static String sortAscending(String input) {
        String[] lines = input.split("\n");
        Arrays.sort(lines);
        return String.join("\n", lines);
    }

    /** Sorts lines alphabetically in descending order. */
    public static String sortDescending(String input) {
        String[] lines = input.split("\n");
        Arrays.sort(lines, Comparator.reverseOrder());
        return String.join("\n", lines);
    }

    public static final class Factory implements ToolFxFactory<TextSortingToolFx> {
        public Factory() {}
        @Override public String getId() { return "text-sorting-transformer"; }
        @Override public ToolPresentation getPresentation() {
            return ToolPresentation.of("text-sorting-transformer",
                    "Text Sorting", "Text Sorting").withGroupId("text");
        }
        @Override public TextSortingToolFx create(ToolConfiguration config) { return new TextSortingToolFx(config); }
    }
}
