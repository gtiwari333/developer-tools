package gt.devtools.tools.standalone;

import gt.devtools.common.ValueProperty;
import gt.devtools.settings.ToolConfiguration;
import gt.devtools.tools.api.ToolPresentation;
import gt.devtools.tools.api.fx.DeveloperToolFx;
import gt.devtools.tools.api.fx.ToolFxFactory;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;

public final class UnitConverterToolFx extends DeveloperToolFx {
    private final ValueProperty<String> category;
    private TextField inputField;
    private VBox resultsBox;

    private UnitConverterToolFx(ToolConfiguration config) {
        super(config); this.category = registerConfig("unitCategory", "Data Size (1024)");
    }

    @Override protected void buildUi(BorderPane panel) {
        panel.setPadding(new Insets(16));
        var bar = new HBox(8);
        bar.getChildren().add(new Label("Category:"));
        var combo = new ComboBox<String>();
        combo.getItems().addAll("Data Size (1024)", "Data Size (1000)", "Number Base");
        combo.setValue(category.get());
        combo.setOnAction(e -> { category.set(combo.getValue()); convert(); });
        bar.getChildren().add(combo);
        bar.getChildren().add(new Label("Value:"));
        inputField = new TextField("1");
        inputField.setOnAction(e -> convert());
        bar.getChildren().add(inputField);
        var btn = new Button("Convert"); btn.setOnAction(e -> convert()); bar.getChildren().add(btn);
        panel.setTop(bar);

        resultsBox = new VBox(4); resultsBox.setPadding(new Insets(8));
        var scroll = new ScrollPane(resultsBox); scroll.setFitToWidth(true);
        panel.setCenter(scroll);
        convert();
    }

    private void convert() {
        resultsBox.getChildren().clear();
        try {
            String cat = category.get();
            double value = Double.parseDouble(inputField.getText().strip());
            if (cat.contains("Number Base")) {
                for (String line : UnitConverterTool.convertNumberBases((long) value))
                    resultsBox.getChildren().add(makeLabel(line));
            } else {
                int base = cat.contains("1000") ? 1000 : 1024;
                for (var e : UnitConverterTool.convertDataSize(value, base).entrySet())
                    resultsBox.getChildren().add(makeLabel(String.format("%,.2f %s", e.getValue(), e.getKey())));
            }
        } catch (NumberFormatException e) { resultsBox.getChildren().add(makeLabel("Invalid number")); }
    }

    private Label makeLabel(String text) {
        var lbl = new Label(text);
        lbl.setFont(Font.font("Monospaced", 13));
        lbl.setPadding(new Insets(2, 8, 2, 8));
        return lbl;
    }

    public static final class Factory implements ToolFxFactory<UnitConverterToolFx> {
        public Factory() {} @Override public String getId() { return "units-converter-fx"; }
        @Override public ToolPresentation getPresentation() {
            return ToolPresentation.of("units-converter-fx", "Unit Converter (FX)", "Unit Converter").withGroupId("formatters"); }
        @Override public UnitConverterToolFx create(ToolConfiguration c) { return new UnitConverterToolFx(c); }
    }
}
