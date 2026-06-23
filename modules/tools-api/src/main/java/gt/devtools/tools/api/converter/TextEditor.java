package gt.devtools.tools.api.converter;

import gt.devtools.common.ValueProperty;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rtextarea.RTextScrollPane;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * A text editor component wrapping {@link RSyntaxTextArea} with
 * syntax highlighting, line numbers, and optional file I/O.
 */
public class TextEditor extends JPanel {

    private final RSyntaxTextArea textArea;
    private final RTextScrollPane scrollPane;
    private final ValueProperty<String> textProperty;
    private final JLabel statusLabel;
    private Path loadedFile;

    public enum Mode { INPUT, OUTPUT }

    public TextEditor(Mode mode) {
        this(mode, null);
    }

    public TextEditor(Mode mode, ValueProperty<String> textProperty) {
        super(new BorderLayout());
        this.textProperty = textProperty;

        textArea = new RSyntaxTextArea(20, 60);
        textArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_NONE);
        textArea.setCodeFoldingEnabled(true);
        textArea.setAntiAliasingEnabled(true);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setTabSize(2);

        if (mode == Mode.OUTPUT) {
            textArea.setEditable(false);
            textArea.setBackground(UIManager.getColor("TextField.inactiveBackground"));
        }

        // Restore persisted text
        if (textProperty != null && textProperty.get() != null) {
            textArea.setText(textProperty.get());
        }

        // Sync edits back to property
        textArea.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            @Override
            public void insertUpdate(javax.swing.event.DocumentEvent e) { syncToProperty(); }
            @Override
            public void removeUpdate(javax.swing.event.DocumentEvent e) { syncToProperty(); }
            @Override
            public void changedUpdate(javax.swing.event.DocumentEvent e) { syncToProperty(); }
            private void syncToProperty() {
                if (textProperty != null && textArea.isEditable()) {
                    textProperty.set(textArea.getText());
                }
            }
        });

        scrollPane = new RTextScrollPane(textArea);
        scrollPane.setLineNumbersEnabled(true);
        add(scrollPane, BorderLayout.CENTER);

        // Status bar
        var southPanel = new JPanel(new BorderLayout());
        statusLabel = new JLabel(" ");
        statusLabel.setFont(statusLabel.getFont().deriveFont(11f));
        southPanel.add(statusLabel, BorderLayout.WEST);
        add(southPanel, BorderLayout.SOUTH);

        updateStatus();
        textArea.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            @Override public void insertUpdate(javax.swing.event.DocumentEvent e) { updateStatus(); }
            @Override public void removeUpdate(javax.swing.event.DocumentEvent e) { updateStatus(); }
            @Override public void changedUpdate(javax.swing.event.DocumentEvent e) { updateStatus(); }
        });
    }

    public RSyntaxTextArea getTextArea() {
        return textArea;
    }

    public String getText() {
        return textArea.getText();
    }

    public void setText(String text) {
        textArea.setText(text != null ? text : "");
    }

    public byte[] getBytes() {
        return getText().getBytes(java.nio.charset.StandardCharsets.UTF_8);
    }

    public void setBytes(byte[] data) {
        setText(new String(data, java.nio.charset.StandardCharsets.UTF_8));
    }

    public void setSyntaxStyle(String style) {
        textArea.setSyntaxEditingStyle(style);
    }

    public void setEditable(boolean editable) {
        textArea.setEditable(editable);
    }

    public void copyToClipboard() {
        var sel = new StringSelection(textArea.getText());
        Toolkit.getDefaultToolkit().getSystemClipboard()
                .setContents(sel, null);
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

    private void updateStatus() {
        String text = textArea.getText();
        int chars = text.length();
        int lines = textArea.getLineCount();
        long bytes = getBytes().length;
        statusLabel.setText(String.format(" %d chars | %d lines | %d bytes (UTF-8)",
                chars, lines, bytes));
    }

    private static String detectSyntax(Path path) {
        String name = path.getFileName().toString().toLowerCase();
        if (name.endsWith(".json")) return SyntaxConstants.SYNTAX_STYLE_JSON;
        if (name.endsWith(".xml") || name.endsWith(".html")) return SyntaxConstants.SYNTAX_STYLE_XML;
        if (name.endsWith(".yaml") || name.endsWith(".yml")) return SyntaxConstants.SYNTAX_STYLE_YAML;
        if (name.endsWith(".sql")) return SyntaxConstants.SYNTAX_STYLE_SQL;
        if (name.endsWith(".java")) return SyntaxConstants.SYNTAX_STYLE_JAVA;
        if (name.endsWith(".properties")) return SyntaxConstants.SYNTAX_STYLE_NONE;
        if (name.endsWith(".py")) return SyntaxConstants.SYNTAX_STYLE_PYTHON;
        if (name.endsWith(".js") || name.endsWith(".ts")) return SyntaxConstants.SYNTAX_STYLE_JAVASCRIPT;
        if (name.endsWith(".css")) return SyntaxConstants.SYNTAX_STYLE_CSS;
        if (name.endsWith(".md")) return SyntaxConstants.SYNTAX_STYLE_MARKDOWN;
        return SyntaxConstants.SYNTAX_STYLE_NONE;
    }
}
