package gt.devtools.tools.formatters;

import gt.devtools.common.ValueProperty;
import gt.devtools.settings.ToolConfiguration;
import gt.devtools.tools.api.ToolPresentation;
import gt.devtools.tools.api.fx.TextTransformerFx;
import gt.devtools.tools.api.fx.ToolFxFactory;
import javafx.scene.control.*;
import javafx.scene.layout.*;

public final class CliCommandConverterToolFx extends TextTransformerFx {
    private final ValueProperty<String> mode;
    private CliCommandConverterToolFx(ToolConfiguration config) { super(config); mode = registerConfig("cliMode", "Split to multiple lines"); }
    @Override protected void buildUi(BorderPane panel) {
        var bar = new HBox(8); bar.getChildren().add(new Label("Mode:"));
        var combo = new ComboBox<String>(); combo.getItems().addAll("Split to multiple lines", "Join to single line"); combo.setValue(mode.get());
        combo.setOnAction(e -> { mode.set(combo.getValue()); if (Boolean.TRUE.equals(liveTransformation.get())) transform(); });
        bar.getChildren().add(combo); panel.setTop(bar); super.buildUi(panel);
    }
    @Override protected String getTransformLabel() { return "Convert"; }
    @Override protected String doTransform(String input) {
        if (input.isBlank()) return "Paste a CLI command.";
        return mode.get().contains("Join") ? CliCommandConverterTool.joinCommand(input) : CliCommandConverterTool.splitCommand(input); }
    public static final class Factory implements ToolFxFactory<CliCommandConverterToolFx> {
        public Factory() {} @Override public String getId() { return "cli-command-converter-fx"; }
        @Override public ToolPresentation getPresentation() {
            return ToolPresentation.of("cli-command-converter-fx", "CLI Converter (FX)", "CLI Command Converter").withGroupId("formatters"); }
        @Override public CliCommandConverterToolFx create(ToolConfiguration c) { return new CliCommandConverterToolFx(c); }
    }
}
