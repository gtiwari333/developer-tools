package gt.devtools.tools.api.fx;

import gt.devtools.common.ValueProperty;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.BorderPane;
import org.fxmisc.flowless.VirtualizedScrollPane;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.LineNumberFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * JavaFX text editor panel backed by RichTextFX {@link CodeArea}.
 * <p>
 * Features:
 * <ul>
 *   <li>Line numbers in the gutter (default on, toggle via {@link #setShowLineNumbers})</li>
 *   <li>INPUT / OUTPUT modes (editable vs read-only)</li>
 *   <li>Bi-directional binding to {@link ValueProperty} for persistence</li>
 *   <li>Status bar showing character / line / byte counts</li>
 *   <li>File load / save helpers</li>
 *   <li>Clipboard copy</li>
 *   <li>Syntax style hint (monospaced font for code formats)</li>
 * </ul>
 */
public class FxTextEditor extends BorderPane {

    public enum Mode { INPUT, OUTPUT }

    private final CodeArea codeArea;
    private final VirtualizedScrollPane<CodeArea> scrollPane;
    private final ValueProperty<String> textProperty;
    private final Label statusLabel;
    private Path loadedFile;
    private String syntaxStyle = "text/plain";
    private boolean showLineNumbers = true;

    public FxTextEditor(Mode mode) {
        this(mode, null);
    }

    public FxTextEditor(Mode mode, ValueProperty<String> textProperty) {
        this.textProperty = textProperty;

        // -- Code area (RichTextFX)
        codeArea = new CodeArea();
        codeArea.setWrapText(true);
        codeArea.setParagraphGraphicFactory(LineNumberFactory.get(codeArea));
        codeArea.setStyle("-fx-font-size: 13px;");

        if (mode == Mode.OUTPUT) {
            codeArea.setEditable(false);
            codeArea.setStyle("-fx-font-size: 13px; -fx-background-color: #f0f0f0;");
        }

        // Restore persisted text
        if (textProperty != null && textProperty.get() != null) {
            codeArea.replaceText(textProperty.get());
        }

        // Sync edits back to property
        codeArea.textProperty().addListener((obs, old, text) -> {
            if (textProperty != null && codeArea.isEditable()) {
                textProperty.set(text);
            }
            updateStatus();
        });

        // Wrap in VirtualizedScrollPane for proper scrolling + virtualisation
        scrollPane = new VirtualizedScrollPane<>(codeArea);
        setCenter(scrollPane);

        // -- Status bar
        statusLabel = new Label(" ");
        statusLabel.setStyle("-fx-font-size: 11px; -fx-padding: 2 4;");
        var statusBar = new BorderPane();
        statusBar.setLeft(statusLabel);
        statusBar.setStyle("-fx-background-color: -fx-control-inner-background; "
                + "-fx-border-color: -fx-box-border; -fx-border-width: 1 0 0 0;");
        setBottom(statusBar);

        setPrefSize(500, 300);
        updateStatus();
    }

    // ---------------------------------------------------------------
    // Text accessors
    // ---------------------------------------------------------------

    /** Returns the underlying RichTextFX {@link CodeArea} for advanced customization. */
    public CodeArea getCodeArea() { return codeArea; }

    /** Returns the text property for listener registration without reaching into the CodeArea. */
    public ObservableValue<String> textProperty() { return codeArea.textProperty(); }

    public String getText() { return codeArea.getText(); }

    public void setText(String text) {
        codeArea.replaceText(text != null ? text : "");
    }

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
        if (style != null) {
            var monoStyles = java.util.Set.of("text/json", "text/sql", "text/xml",
                    "text/markdown", "text/java", "text/python", "text/javascript",
                    "text/css", "text/yaml", "text/plain");
            if (monoStyles.contains(style)) {
                codeArea.setStyle(codeArea.getStyle() + "; -fx-font-family: 'Monospaced';");
            }
        }
    }

    public void setShowLineNumbers(boolean show) {
        this.showLineNumbers = show;
        if (show) {
            codeArea.setParagraphGraphicFactory(LineNumberFactory.get(codeArea));
        } else {
            codeArea.setParagraphGraphicFactory(null);
        }
    }

    public boolean isShowLineNumbers() { return showLineNumbers; }

    public void setEditable(boolean editable) { codeArea.setEditable(editable); }

    // ---------------------------------------------------------------
    // Actions
    // ---------------------------------------------------------------

    public void copyToClipboard() {
        var content = new ClipboardContent();
        content.putString(codeArea.getText());
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
        String text = codeArea.getText();
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
