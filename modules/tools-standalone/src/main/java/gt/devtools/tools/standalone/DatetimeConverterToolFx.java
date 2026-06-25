package gt.devtools.tools.standalone;

import gt.devtools.common.ValueProperty;
import gt.devtools.settings.ToolConfiguration;
import gt.devtools.tools.api.ToolPresentation;
import gt.devtools.tools.api.fx.TextTransformerFx;
import gt.devtools.tools.api.fx.ToolFxFactory;
import java.time.*;
import java.time.format.DateTimeFormatter;
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
        Instant instant = parseTimestamp(input.strip(), normalizeInputFormat(inputFormat.get()));
        return formatInstant(instant, normalizeOutputFormat(outputFormat.get()));
    }
    private static String normalizeInputFormat(String uiFormat) {
        return switch (uiFormat) {
            case "Unix seconds" -> "unix-seconds";
            case "Unix milliseconds" -> "unix-millis";
            default -> "auto";
        };
    }
    private static String normalizeOutputFormat(String uiFormat) {
        return switch (uiFormat) {
            case "Unix seconds" -> "unix-seconds";
            case "Unix milliseconds" -> "unix-millis";
            default -> uiFormat;
        };
    }

    public static Instant parseTimestamp(String text, String inputFormat) {
        return switch (inputFormat) {
            case "unix-seconds" -> Instant.ofEpochSecond(Long.parseLong(text));
            case "unix-millis" -> Instant.ofEpochMilli(Long.parseLong(text));
            default -> {
                try {
                    long num = Long.parseLong(text);
                    // 10-digit numbers are Unix seconds, 13-digit are milliseconds
                    yield text.length() <= 10 ? Instant.ofEpochSecond(num) : Instant.ofEpochMilli(num);
                } catch (NumberFormatException e) { yield Instant.parse(text); }
            }
        };
    }

    public static String formatInstant(Instant instant, String format) {
        return switch (format) {
            case "RFC-1123" -> DateTimeFormatter.RFC_1123_DATE_TIME.withZone(ZoneOffset.UTC).format(instant);
            case "unix-millis", "Unix milliseconds" -> String.valueOf(instant.toEpochMilli());
            case "unix-seconds", "Unix seconds" -> String.valueOf(instant.getEpochSecond());
            default -> instant.toString();
        };
    }

    public static final class Factory implements ToolFxFactory<DatetimeConverterToolFx> {
        public Factory() {} @Override public String getId() { return "date-time-converter"; }
        @Override public ToolPresentation getPresentation() {
            return ToolPresentation.of("date-time-converter", "Date/Time", "Date / Time Converter").withGroupId("formatters"); }
        @Override public DatetimeConverterToolFx create(ToolConfiguration c) { return new DatetimeConverterToolFx(c); }
    }
}
