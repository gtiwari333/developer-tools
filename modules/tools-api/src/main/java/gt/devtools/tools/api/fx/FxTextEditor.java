package gt.devtools.tools.api.fx;

import gt.devtools.common.ValueProperty;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * JavaFX text editor panel replacing RSyntaxTextArea with JavaFX TextArea.
 * <p>
 * Features:
 * <ul>
 *   <li>INPUT / OUTPUT modes (editable vs read-only)</li>
 *   <li>Bi-directional binding to {@link ValueProperty} for persistence</li>
 *   <li>Status bar showing character / line / byte counts</li>
 *   <li>File load / save helpers</li>
 *   <li>Clipboard copy</li>
 *   <li>Syntax style hint (styling applied via CSS in Phase 5)</li>
 * </ul>
 */
public class FxTextEditor extends BorderPane {

    public enum Mode { INPUT, OUTPUT }

    private final TextArea textArea;
    private final ValueProperty<String> textProperty;
    private final Label statusLabel;
    private Path loadedFile;
    private String syntaxStyle = "text/plain";

    public FxTextEditor(Mode mode) {
        this(mode, null);
    }

    public FxTextEditor(Mode mode, ValueProperty<String> textProperty) {
        this.textProperty = textProperty;

        // -- Text area
        textArea = new TextArea();
        textArea.setWrapText(true);
        textArea.setPrefRowCount(20);
        textArea.setPrefColumnCount(60);

        if (mode == Mode.OUTPUT) {
            textArea.setEditable(false);
            textArea.setStyle("-fx-control-inner-background: #f0f0f0;");
        }

        // Restore persisted text
        if (textProperty != null && textProperty.get() != null) {
            textArea.setText(textProperty.get());
        }

        // Sync edits back to property
        textArea.textProperty().addListener((obs, old, text) -> {
            if (textProperty != null && textArea.isEditable()) {
                textProperty.set(text);
            }
            updateStatus();
        });

        setCenter(textArea);

        // -- Status bar
        statusLabel = new Label(" ");
        statusLabel.setStyle("-fx-font-size: 11px; -fx-padding: 2 4;");
        var statusBar = new BorderPane();
        statusBar.setLeft(statusLabel);
        statusBar.setStyle("-fx-background-color: -fx-control-inner-background; "
                + "-fx-border-color: -fx-box-border; -fx-border-width: 1 0 0 0;");
        setBottom(statusBar);

        updateStatus();
    }

    // ---------------------------------------------------------------
    // Text accessors
    // ---------------------------------------------------------------

    public TextArea getTextArea() { return textArea; }

    public String getText() { return textArea.getText(); }

    public void setText(String text) { textArea.setText(text != null ? text : ""); }

    public byte[] getBytes() {
        return getText().getBytes(StandardCharsets.UTF_8);
    }

    public void setBytes(byte[] data) {
        setText(new String(data, StandardCharsets.UTF_8));
    }

    // ---------------------------------------------------------------
    // Appearance
    // ---------------------------------------------------------------

    public void setSyntaxStyle(String style) {
        this.syntaxStyle = style;
        // Phase 5: apply CSS classes based on syntax style
        if (style != null) {
            if (style.contains("json")) textArea.setStyle("-fx-font-family: 'Monospaced';");
            else if (style.contains("sql")) textArea.setStyle("-fx-font-family: 'Monospaced';");
            else if (style.contains("xml")) textArea.setStyle("-fx-font-family: 'Monospaced';");
            else if (style.contains("markdown")) textArea.setStyle("-fx-font-family: 'Monospaced';");
        }
    }

    public void setEditable(boolean editable) { textArea.setEditable(editable); }

    // ---------------------------------------------------------------
    // Actions
    // ---------------------------------------------------------------

    public void copyToClipboard() {
        var content = new ClipboardContent();
        content.putString(textArea.getText());
        Clipboard.getSystemClipboard().setContent(content);
    }

    public void loadFile(Path path) throws IOException {
        String content = Files.readString(path);
        setText(content);
        this.loadedFile = path;
        setSyntaxStyle(detectSyntax(path));
    }

    public void saveFile(Path path) throws IOException {
        Files.writeString(path, getText());
        this.loadedFile = path;
    }

    // ---------------------------------------------------------------
    // Internal
    // ---------------------------------------------------------------

    private void updateStatus() {
        String text = textArea.getText();
        int chars = text.length();
        int lines = chars == 0 ? 0 : text.split("\n", -1).length;
        long bytes = getBytes().length;
        statusLabel.setText(String.format(" %d chars | %d lines | %d bytes (UTF-8)",
                chars, lines, bytes));
    }

    private static String detectSyntax(Path path) {
        String name = path.getFileName().toString().toLowerCase();
        if (name.endsWith(".json"))       return "text/json";
        if (name.endsWith(".xml"))        return "text/xml";
        if (name.endsWith(".html"))       return "text/xml";
        if (name.endsWith(".yaml"))       return "text/yaml";
        if (name.endsWith(".yml"))        return "text/yaml";
        if (name.endsWith(".sql"))        return "text/sql";
        if (name.endsWith(".java"))       return "text/java";
        if (name.endsWith(".properties")) return "text/plain";
        if (name.endsWith(".py"))         return "text/python";
        if (name.endsWith(".js"))         return "text/javascript";
        if (name.endsWith(".ts"))         return "text/javascript";
        if (name.endsWith(".css"))        return "text/css";
        if (name.endsWith(".md"))         return "text/markdown";
        return "text/plain";
    }
}
