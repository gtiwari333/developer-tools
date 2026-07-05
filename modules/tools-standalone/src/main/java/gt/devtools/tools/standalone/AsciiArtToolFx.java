package gt.devtools.tools.standalone;

import com.github.lalyos.jfiglet.FigletFont;
import gt.devtools.common.ValueProperty;
import gt.devtools.settings.ToolConfiguration;
import gt.devtools.tools.api.ToolPresentation;
import gt.devtools.tools.api.fx.DeveloperToolFx;
import gt.devtools.tools.api.fx.FxTextEditor;
import gt.devtools.tools.api.fx.ToolFxFactory;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;

public final class AsciiArtToolFx extends DeveloperToolFx {
    private final ValueProperty<String> fontName, text;
    private TextField inputField;
    private FxTextEditor outputEditor;

    private AsciiArtToolFx(ToolConfiguration config) {
        super(config);
        this.fontName = registerConfig("figletFont", "standard");
        this.text = registerConfig("asciiText", "Hello");
    }

    @Override protected void buildUi(BorderPane panel) {
        panel.setPadding(new Insets(8));
        var bar = new HBox(8);
        bar.getChildren().add(new Label("Font:"));
        var fontField = new TextField(fontName.get());
        fontField.setOnAction(e -> { fontName.set(fontField.getText()); render(); });
        bar.getChildren().add(fontField);
        bar.getChildren().add(new Label("Text:"));
        inputField = new TextField(text.get());
        inputField.setOnAction(e -> { text.set(inputField.getText()); render(); });
        bar.getChildren().add(inputField);
        var renderBtn = new Button("Render");
        renderBtn.setOnAction(e -> render());
        bar.getChildren().add(renderBtn);
        panel.setTop(bar);
        outputEditor = new FxTextEditor(FxTextEditor.Mode.OUTPUT);
        panel.setCenter(outputEditor);
        if (text.get() != null && !text.get().isEmpty()) render();
    }

    private void render() {
        try { outputEditor.setText(FigletFont.convertOneLine(inputField.getText())); }
        catch (Exception e) { outputEditor.setText("Error: " + e.getMessage()); }
    }

    public static final class Factory implements ToolFxFactory<AsciiArtToolFx> {
        public Factory() {} @Override public String getId() { return "ascii-art"; }
        @Override public ToolPresentation getPresentation() {
            return ToolPresentation.of("ascii-art", "ASCII Art", "ASCII Art Generator").withGroupId("creativity"); }
        @Override public AsciiArtToolFx create(ToolConfiguration c) { return new AsciiArtToolFx(c); }
    }
}
