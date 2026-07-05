package gt.devtools.tools.standalone;

import gt.devtools.common.ValueProperty;
import gt.devtools.settings.ToolConfiguration;
import gt.devtools.tools.api.ToolPresentation;
import gt.devtools.tools.api.fx.DeveloperToolFx;
import gt.devtools.tools.api.fx.FxTextEditor;
import gt.devtools.tools.api.fx.ToolFxFactory;
import java.util.random.RandomGenerator;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.control.*;
import javafx.scene.layout.*;

public final class RubberDuckToolFx extends DeveloperToolFx {
    private static final String[] QUOTES = {
        "What does the stack trace say?", "Is there a missing null check?",
        "Did you read the documentation?", "Maybe sleep on it and try again tomorrow.",
        "What would happen if you removed that line?", "Have you written a test for it?",
        "🦆 Quack! (That means you've got this!)"
    };
    private FxTextEditor inputEditor, outputEditor;
    private final ValueProperty<String> problem;

    private RubberDuckToolFx(ToolConfiguration config) {
        super(config); this.problem = registerInput("duckProblem", "");
    }

    @Override protected void buildUi(BorderPane panel) {
        panel.setPadding(new Insets(8));
        var header = new HBox(8);
        header.getChildren().add(new Label("🦆"));
        var quackBtn = new Button("Quack!");
        quackBtn.setOnAction(e -> quack());
        header.getChildren().add(quackBtn);
        var clearBtn = new Button("Clear");
        clearBtn.setOnAction(e -> { inputEditor.setText(""); outputEditor.setText(""); });
        header.getChildren().add(clearBtn);
        panel.setTop(header);

        var split = new SplitPane();
        split.setOrientation(Orientation.VERTICAL);
        split.setDividerPositions(0.45);
        inputEditor = new FxTextEditor(FxTextEditor.Mode.INPUT, problem);
        var rightPane = new BorderPane();
        var outTitle = new Label("Rubber Duck says:");
        outTitle.setStyle("-fx-font-weight: bold; -fx-padding: 4 8;");
        rightPane.setTop(outTitle);
        outputEditor = new FxTextEditor(FxTextEditor.Mode.OUTPUT);
        rightPane.setCenter(outputEditor);
        split.getItems().addAll(inputEditor, rightPane);
        panel.setCenter(split);
    }

    private void quack() {
        String quote = QUOTES[RandomGenerator.getDefault().nextInt(QUOTES.length)];
        outputEditor.setText(quote);
    }

    public static final class Factory implements ToolFxFactory<RubberDuckToolFx> {
        public Factory() {} @Override public String getId() { return "rubber-duck"; }
        @Override public ToolPresentation getPresentation() {
            return ToolPresentation.of("rubber-duck", "Rubber Duck", "Rubber Duck Debugging"); }
        @Override public RubberDuckToolFx create(ToolConfiguration c) { return new RubberDuckToolFx(c); }
    }
}
