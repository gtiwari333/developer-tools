package gt.devtools.tools.standalone;

import gt.devtools.common.ValueProperty;
import gt.devtools.settings.ToolConfiguration;
import gt.devtools.tools.api.ToolPresentation;
import gt.devtools.tools.api.fx.DeveloperToolFx;
import gt.devtools.tools.api.fx.ToolFxFactory;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;

public final class ColorPickerToolFx extends DeveloperToolFx {
    private final ValueProperty<String> hexValue;
    private Rectangle colorPreview;
    private TextField hexField;
    private Label rgbLabel, hslLabel;
    private Color currentColor = Color.BLACK;

    private ColorPickerToolFx(ToolConfiguration config) {
        super(config); this.hexValue = registerConfig("colorHex", "#000000");
    }

    @Override protected void buildUi(BorderPane panel) {
        panel.setPadding(new Insets(16));
        var content = new VBox(12);

        // Color preview swatch
        colorPreview = new Rectangle(120, 80, currentColor);
        colorPreview.setStroke(Color.GRAY);
        colorPreview.setOnMouseClicked(e -> openChooser());
        content.getChildren().add(colorPreview);

        // Pick button
        var pickBtn = new Button("Pick Color...");
        pickBtn.setOnAction(e -> openChooser());
        content.getChildren().add(pickBtn);

        // Hex input
        var hexBar = new HBox(8);
        hexBar.getChildren().add(new Label("Hex:"));
        hexField = new TextField(hexValue.get());
        hexField.setOnAction(e -> updateFromHex(hexField.getText()));
        hexBar.getChildren().add(hexField);
        var copyBtn = new Button("Copy");
        copyBtn.setOnAction(e -> { var c = new ClipboardContent(); c.putString(hexField.getText()); Clipboard.getSystemClipboard().setContent(c); });
        hexBar.getChildren().add(copyBtn);
        content.getChildren().add(hexBar);

        rgbLabel = new Label(); rgbLabel.setFont(Font.font("Monospaced", 13));
        content.getChildren().add(rgbLabel);
        hslLabel = new Label(); hslLabel.setFont(Font.font("Monospaced", 13));
        content.getChildren().add(hslLabel);

        panel.setTop(content);
        String saved = hexValue.get();
        if (saved != null && !saved.isEmpty()) updateFromHex(saved);
    }

    private void openChooser() {
        var picker = new ColorPicker(currentColor);
        // Show dialog
        var dialog = new javafx.stage.Stage();
        dialog.setTitle("Pick a Color");
        var dpane = new VBox(8, picker, new Button("OK"));
        dpane.setPadding(new Insets(16));
        picker.setOnAction(e -> { setColor(picker.getValue()); dialog.close(); });
        dialog.setScene(new javafx.scene.Scene(dpane));
        dialog.show();
    }

    private void setColor(Color c) {
        currentColor = c; colorPreview.setFill(c);
        String hex = String.format("#%02X%02X%02X", (int)(c.getRed()*255), (int)(c.getGreen()*255), (int)(c.getBlue()*255));
        hexField.setText(hex); hexValue.set(hex);
        rgbLabel.setText(String.format("RGB: %.0f, %.0f, %.0f", c.getRed()*255, c.getGreen()*255, c.getBlue()*255));
        hslLabel.setText(String.format("HSL: %.0f°, %.0f%%, %.0f%%", c.getHue(), c.getSaturation()*100, c.getBrightness()*100));
    }

    private void updateFromHex(String hex) {
        try { if (!hex.startsWith("#")) hex = "#" + hex; setColor(Color.web(hex)); } catch (Exception ignored) {}
    }

    public static final class Factory implements ToolFxFactory<ColorPickerToolFx> {
        public Factory() {} @Override public String getId() { return "color-picker"; }
        @Override public ToolPresentation getPresentation() {
            return ToolPresentation.of("color-picker", "Color Picker", "Color Picker").withGroupId("creativity"); }
        @Override public ColorPickerToolFx create(ToolConfiguration c) { return new ColorPickerToolFx(c); }
    }
}
