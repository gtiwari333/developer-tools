package gt.devtools.tools.standalone;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import gt.devtools.common.ValueProperty;
import gt.devtools.settings.ToolConfiguration;
import gt.devtools.tools.api.ToolPresentation;
import gt.devtools.tools.api.fx.DeveloperToolFx;
import gt.devtools.tools.api.fx.ToolFxFactory;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import java.awt.image.BufferedImage;

public final class QrCodeGeneratorToolFx extends DeveloperToolFx {
    private final ValueProperty<String> text;
    private TextArea inputArea;
    private ImageView imageView;

    private QrCodeGeneratorToolFx(ToolConfiguration config) {
        super(config); this.text = registerInput("qrText", "");
    }

    @Override protected void buildUi(BorderPane panel) {
        panel.setPadding(new Insets(8));
        var split = new SplitPane(); split.setOrientation(Orientation.VERTICAL); split.setDividerPositions(0.35);

        var inputPane = new BorderPane();
        inputPane.setTop(new Label("Input Text"));
        inputArea = new TextArea(text.get());
        inputArea.setPrefRowCount(4);
        inputArea.textProperty().addListener((o, w, t) -> { text.set(t); regenerate(); });
        inputPane.setCenter(inputArea);
        split.getItems().add(inputPane);

        var outputPane = new BorderPane();
        outputPane.setTop(new Label("QR Code"));
        imageView = new ImageView();
        imageView.setPreserveRatio(true); imageView.setFitWidth(280);
        var scroll = new ScrollPane(imageView); scroll.setFitToWidth(true);
        outputPane.setCenter(scroll);
        split.getItems().add(outputPane);

        panel.setCenter(split);
        regenerate();
    }

    private void regenerate() {
        String t = inputArea.getText().strip();
        if (t.isEmpty()) { imageView.setImage(null); return; }
        try {
            QRCodeWriter writer = new QRCodeWriter();
            BitMatrix matrix = writer.encode(t, BarcodeFormat.QR_CODE, 300, 300);
            BufferedImage img = MatrixToImageWriter.toBufferedImage(matrix);
            imageView.setImage(SwingFXUtils.toFXImage(img, null));
        } catch (Exception e) { imageView.setImage(null); }
    }

    public static final class Factory implements ToolFxFactory<QrCodeGeneratorToolFx> {
        public Factory() {} @Override public String getId() { return "qr-code-generator-fx"; }
        @Override public ToolPresentation getPresentation() {
            return ToolPresentation.of("qr-code-generator-fx", "QR Code (FX)", "QR Code Generator").withGroupId("creativity"); }
        @Override public QrCodeGeneratorToolFx create(ToolConfiguration c) { return new QrCodeGeneratorToolFx(c); }
    }
}
