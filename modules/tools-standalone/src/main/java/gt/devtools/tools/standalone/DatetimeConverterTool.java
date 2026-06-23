package gt.devtools.tools.standalone;

import gt.devtools.common.ValueProperty;
import gt.devtools.settings.ToolConfiguration;
import gt.devtools.tools.api.ToolFactory;
import gt.devtools.tools.api.ToolPresentation;
import gt.devtools.tools.api.text.TextTransformer;

import javax.swing.*;
import java.awt.*;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Converts between Unix timestamps, ISO-8601, and custom date formats.
 * Displays the current time in multiple formats.
 */
public final class DatetimeConverterTool extends TextTransformer {

    private final ValueProperty<String> inputFormat;
    private final ValueProperty<String> outputFormat;
    private final ValueProperty<String> timezone;

    private DatetimeConverterTool(ToolConfiguration config) {
        super(config);
        this.inputFormat = registerConfig("dtInputFormat", "Auto (Unix ms / ISO-8601)");
        this.outputFormat = registerConfig("dtOutputFormat", "ISO-8601");
        this.timezone = registerConfig("dtTimezone", "UTC");
    }

    @Override
    protected void buildUi(JPanel panel) {
        var configBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        configBar.add(new JLabel("Input:"));
        var inCombo = new JComboBox<>(new String[]{
                "Auto (Unix ms / ISO-8601)", "Unix seconds", "Unix milliseconds"
        });
        inCombo.setSelectedItem(inputFormat.get());
        inCombo.addActionListener(e -> {
            inputFormat.set((String) inCombo.getSelectedItem());
            transform();
        });
        configBar.add(inCombo);
        configBar.add(new JLabel("Output:"));
        var outCombo = new JComboBox<>(new String[]{
                "ISO-8601", "RFC-1123", "Unix milliseconds", "Unix seconds"
        });
        outCombo.setSelectedItem(outputFormat.get());
        outCombo.addActionListener(e -> {
            outputFormat.set((String) outCombo.getSelectedItem());
            transform();
        });
        configBar.add(outCombo);
        configBar.add(new JLabel("TZ:"));
        var tzField = new JTextField(timezone.get(), 8);
        tzField.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override public void focusLost(java.awt.event.FocusEvent e) { timezone.set(tzField.getText()); }
        });
        configBar.add(tzField);
        panel.add(configBar, BorderLayout.NORTH);
        super.buildUi(panel);
        sourceEditor.setText(String.valueOf(System.currentTimeMillis()));
    }

    @Override
    protected String getTransformLabel() { return "Convert"; }

    @Override
    protected String doTransform(String input) {
        if (input.isBlank()) return "Enter a timestamp or ISO-8601 date.";
        try {
            Instant instant = parseInput(input.strip());
            ZoneId zone = ZoneId.of(timezone.get().isEmpty() ? "UTC" : timezone.get());
            ZonedDateTime zdt = instant.atZone(zone);
            return formatOutput(zdt);
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    private Instant parseInput(String s) {
        String fmt = inputFormat.get();
        if (fmt.contains("Unix seconds")) return Instant.ofEpochSecond(Long.parseLong(s));
        if (fmt.contains("Unix milliseconds")) return Instant.ofEpochMilli(Long.parseLong(s));
        // Auto-detect
        if (s.matches("\\d{10,13}")) {
            return s.length() > 10 ? Instant.ofEpochMilli(Long.parseLong(s))
                    : Instant.ofEpochSecond(Long.parseLong(s));
        }
        return Instant.parse(s); // ISO-8601
    }

    private String formatOutput(ZonedDateTime zdt) {
        return switch (outputFormat.get()) {
            case "RFC-1123" -> zdt.format(DateTimeFormatter.RFC_1123_DATE_TIME);
            case "Unix milliseconds" -> String.valueOf(zdt.toInstant().toEpochMilli());
            case "Unix seconds" -> String.valueOf(zdt.toInstant().getEpochSecond());
            default -> zdt.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        };
    }

    public static final class Factory implements ToolFactory<DatetimeConverterTool> {
        public Factory() {}
        @Override public String getId() { return "date-time-converter"; }
        @Override
        public ToolPresentation getPresentation() {
            return ToolPresentation.of("date-time-converter",
                    "Date / Time Converter", "Date / Time Converter");
        }
        @Override
        public DatetimeConverterTool create(ToolConfiguration config) { return new DatetimeConverterTool(config); }
    }
}
