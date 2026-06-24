package gt.devtools.tools.standalone;

import gt.devtools.common.ValueProperty;
import gt.devtools.settings.ToolConfiguration;
import gt.devtools.tools.api.ToolPresentation;
import gt.devtools.tools.api.fx.TextTransformerFx;
import gt.devtools.tools.api.fx.ToolFxFactory;
import java.time.Instant;
import javafx.scene.control.*;
import javafx.scene.layout.*;

public final class DatetimeConverterToolFx extends TextTransformerFx {
    private final ValueProperty<String> inputFormat, outputFormat, timezone;
    private DatetimeConverterToolFx(ToolConfiguration config) {
        super(config);
        inputFormat = registerConfig("dtInputFormat", "Auto (Unix ms / ISO-8601)");
        outputFormat = registerConfig("dtOutputFormat", "ISO-8601");
        timezone = registerConfig("dtTimezone", "UTC");
    }
    @Override protected void buildUi(BorderPane panel) {
        var bar = new HBox(8); bar.getChildren().add(new Label("In:"));
        var in = new ComboBox<String>(); in.getItems().addAll("Auto (Unix ms / ISO-8601)", "Unix seconds", "Unix milliseconds");
        in.setValue(inputFormat.get()); in.setOnAction(e -> { inputFormat.set(in.getValue()); transform(); }); bar.getChildren().add(in);
        bar.getChildren().add(new Label("Out:"));
        var out = new ComboBox<String>(); out.getItems().addAll("ISO-8601", "RFC-1123", "Unix milliseconds", "Unix seconds");
        out.setValue(outputFormat.get()); out.setOnAction(e -> { outputFormat.set(out.getValue()); transform(); }); bar.getChildren().add(out);
        bar.getChildren().add(new Label("TZ:"));
        var tz = new TextField(timezone.get()); tz.focusedProperty().addListener((o, w, f) -> { if (!f) timezone.set(tz.getText()); });
        bar.getChildren().add(tz); panel.setTop(bar); super.buildUi(panel);
        sourceEditor.setText(String.valueOf(System.currentTimeMillis()));
    }
    @Override protected String getTransformLabel() { return "Convert"; }
    @Override protected String doTransform(String input) throws Exception {
        if (input.isBlank()) return "Enter a timestamp or ISO-8601 date.";
        Instant instant = DatetimeConverterTool.parseTimestamp(input.strip(), inputFormat.get());
        return DatetimeConverterTool.formatInstant(instant, outputFormat.get());
    }
    public static final class Factory implements ToolFxFactory<DatetimeConverterToolFx> {
        public Factory() {} @Override public String getId() { return "date-time-converter-fx"; }
        @Override public ToolPresentation getPresentation() {
            return ToolPresentation.of("date-time-converter-fx", "Date/Time (FX)", "Date / Time Converter").withGroupId("formatters"); }
        @Override public DatetimeConverterToolFx create(ToolConfiguration c) { return new DatetimeConverterToolFx(c); }
    }
}
