package gt.devtools.tools.standalone;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.common.BitMatrix;
import gt.devtools.common.ValueProperty;
import gt.devtools.settings.ToolConfiguration;
import gt.devtools.tools.api.ToolPresentation;
import gt.devtools.tools.api.fx.DeveloperToolFx;
import gt.devtools.tools.api.fx.ToolFxFactory;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.*;

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
            imageView.setImage(bitMatrixToFxImage(matrix));
        } catch (Exception e) { imageView.setImage(null); }
    }

    /** Convert ZXing BitMatrix to JavaFX Image directly (no Swing/AWT dependency). */
    private static javafx.scene.image.Image bitMatrixToFxImage(BitMatrix matrix) {
        int w = matrix.getWidth();
        int h = matrix.getHeight();
        WritableImage image = new WritableImage(w, h);
        PixelWriter pw = image.getPixelWriter();
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                pw.setArgb(x, y, matrix.get(x, y) ? 0xFF000000 : 0xFFFFFFFF);
            }
        }
        return image;
    }

    public static final class Factory implements ToolFxFactory<QrCodeGeneratorToolFx> {
        public Factory() {} @Override public String getId() { return "qr-code-generator"; }
        @Override public ToolPresentation getPresentation() {
            return ToolPresentation.of("qr-code-generator", "QR Code", "QR Code Generator").withGroupId("creativity"); }
        @Override public QrCodeGeneratorToolFx create(ToolConfiguration c) { return new QrCodeGeneratorToolFx(c); }
    }
}
