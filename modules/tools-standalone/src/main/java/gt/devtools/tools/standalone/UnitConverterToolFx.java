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

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class UnitConverterToolFx extends DeveloperToolFx {
    private final ValueProperty<String> category;
    private TextField inputField;
    private VBox resultsBox;

    private static final String[] DATA_SIZE_UNITS = {"bytes", "KB", "MB", "GB", "TB", "PB"};

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
                for (String line : convertNumberBases((long) value))
                    resultsBox.getChildren().add(makeLabel(line));
            } else {
                int base = cat.contains("1000") ? 1000 : 1024;
                for (var e : convertDataSize(value, base).entrySet())
                    resultsBox.getChildren().add(makeLabel(String.format("%,.2f %s", e.getValue(), e.getKey())));
            }
        } catch (NumberFormatException e) { resultsBox.getChildren().add(makeLabel("Invalid number")); }
    }

    public static List<String> convertNumberBases(long value) {
        return List.of(
            "Binary:     " + Long.toBinaryString(value),
            "Octal:      " + Long.toOctalString(value),
            "Decimal:    " + value,
            "Hex:        " + Long.toHexString(value).toUpperCase(),
            "Hex (0x):   " + "0x" + Long.toHexString(value).toUpperCase()
        );
    }

    public static Map<String, Double> convertDataSize(double bytes, int base) {
        var result = new LinkedHashMap<String, Double>();
        result.put(DATA_SIZE_UNITS[0], bytes);
        double v = bytes;
        for (int i = 1; i < DATA_SIZE_UNITS.length; i++) {
            v /= base;
            result.put(DATA_SIZE_UNITS[i], v);
        }
        return result;
    }

    private Label makeLabel(String text) {
        var lbl = new Label(text);
        lbl.setFont(Font.font("Monospaced", 13));
        lbl.setPadding(new Insets(2, 8, 2, 8));
        return lbl;
    }

    public static final class Factory implements ToolFxFactory<UnitConverterToolFx> {
        public Factory() {} @Override public String getId() { return "units-converter"; }
        @Override public ToolPresentation getPresentation() {
            return ToolPresentation.of("units-converter", "Unit Converter", "Unit Converter").withGroupId("formatters"); }
        @Override public UnitConverterToolFx create(ToolConfiguration c) { return new UnitConverterToolFx(c); }
    }
}
