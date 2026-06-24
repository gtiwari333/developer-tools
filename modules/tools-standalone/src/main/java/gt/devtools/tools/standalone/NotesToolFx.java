package gt.devtools.tools.standalone;

import gt.devtools.common.ValueProperty;
import gt.devtools.settings.ToolConfiguration;
import gt.devtools.tools.api.ToolPresentation;
import gt.devtools.tools.api.fx.DeveloperToolFx;
import gt.devtools.tools.api.fx.FxTextEditor;
import gt.devtools.tools.api.fx.ToolFxFactory;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;

public final class NotesToolFx extends DeveloperToolFx {
    private final ValueProperty<String> content;
    private FxTextEditor editor;

    private NotesToolFx(ToolConfiguration config) {
        super(config);
        this.content = registerInput("noteContent", "");
    }

    @Override protected void buildUi(BorderPane panel) {
        panel.setPadding(new Insets(8, 8, 8, 8));
        var bar = new HBox(8);
        var copyBtn = new Button("Copy All");
        copyBtn.setOnAction(e -> editor.copyToClipboard());
        bar.getChildren().add(copyBtn);
        var clearBtn = new Button("Clear");
        clearBtn.setOnAction(e -> editor.setText(""));
        bar.getChildren().add(clearBtn);
        bar.getChildren().add(new Label("  Notes are saved automatically."));
        panel.setTop(bar);
        editor = new FxTextEditor(FxTextEditor.Mode.INPUT, content);
        editor.setSyntaxStyle("text/markdown");
        panel.setCenter(editor);
    }

    public static final class Factory implements ToolFxFactory<NotesToolFx> {
        public Factory() {} @Override public String getId() { return "notes-fx"; }
        @Override public ToolPresentation getPresentation() {
            return ToolPresentation.of("notes-fx", "Notes (FX)", "Notes"); }
        @Override public NotesToolFx create(ToolConfiguration c) { return new NotesToolFx(c); }
    }
}
