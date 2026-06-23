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
 * A text editor panel wrapping {@link RSyntaxTextArea} with:
 * <ul>
 *   <li>Syntax highlighting (auto-detected from file extension)</li>
 *   <li>Line numbers via {@link RTextScrollPane}</li>
 *   <li>Bi-directional binding to a {@link ValueProperty} for persistence</li>
 *   <li>Status bar showing character / line / byte counts</li>
 *   <li>File load / save helpers</li>
 *   <li>Clipboard copy</li>
 * </ul>
 *
 * <h3>Modes</h3>
 * <ul>
 *   <li>{@link Mode#INPUT} — editable, white background, changes synced to
 *       the bound property</li>
 *   <li>{@link Mode#OUTPUT} — read-only, dimmed background, for displaying
 *       conversion results</li>
 * </ul>
 *
 * <h3>Persistence</h3>
 * Pass a {@link ValueProperty} to the constructor — edits are
 * automatically synced back to the property so {@link ToolConfiguration}
 * persists the content.
 */
public class TextEditor extends JPanel {

    /** Whether the editor is for user input or output display. */
    public enum Mode { INPUT, OUTPUT }

    private final RSyntaxTextArea textArea;
    private final ValueProperty<String> textProperty;
    private final JLabel statusLabel;
    private Path loadedFile;

    /** Create an editor with no property binding. */
    public TextEditor(Mode mode) {
        this(mode, null);
    }

    /**
     * Create an editor bound to a persistence property.
     *
     * @param mode         INPUT (editable) or OUTPUT (read-only)
     * @param textProperty the property to sync edits to (may be null)
     */
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

        // Sync edits back to the property (so they survive restarts)
        textArea.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            @Override public void insertUpdate(javax.swing.event.DocumentEvent e) { syncToProperty(); }
            @Override public void removeUpdate(javax.swing.event.DocumentEvent e) { syncToProperty(); }
            @Override public void changedUpdate(javax.swing.event.DocumentEvent e) { syncToProperty(); }
            private void syncToProperty() {
                if (textProperty != null && textArea.isEditable()) {
                    textProperty.set(textArea.getText());
                }
            }
        });

        var scrollPane = new RTextScrollPane(textArea);
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

    // ---------------------------------------------------------------
    // Text accessors
    // ---------------------------------------------------------------

    /** The underlying {@link RSyntaxTextArea} for advanced configuration. */
    public RSyntaxTextArea getTextArea() { return textArea; }

    /** Read the full text content. */
    public String getText() { return textArea.getText(); }

    /** Replace the full text content. */
    public void setText(String text) { textArea.setText(text != null ? text : ""); }

    /** Read the text as UTF-8 bytes — used by {@link Converter}. */
    public byte[] getBytes() {
        return getText().getBytes(java.nio.charset.StandardCharsets.UTF_8);
    }

    /** Replace text from UTF-8 bytes. */
    public void setBytes(byte[] data) {
        setText(new String(data, java.nio.charset.StandardCharsets.UTF_8));
    }

    // ---------------------------------------------------------------
    // Appearance
    // ---------------------------------------------------------------

    /** Set the syntax highlighting style (use {@link SyntaxConstants}). */
    public void setSyntaxStyle(String style) { textArea.setSyntaxEditingStyle(style); }

    /** Toggle editability. */
    public void setEditable(boolean editable) { textArea.setEditable(editable); }

    // ---------------------------------------------------------------
    // Actions
    // ---------------------------------------------------------------

    /** Copy the editor content to the system clipboard. */
    public void copyToClipboard() {
        var sel = new StringSelection(textArea.getText());
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(sel, null);
    }

    /** Load text from a file and auto-detect syntax highlighting. */
    public void loadFile(Path path) throws IOException {
        String content = Files.readString(path);
        setText(content);
        this.loadedFile = path;
        setSyntaxStyle(detectSyntax(path));
    }

    /** Save text to a file. */
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
        int lines = textArea.getLineCount();
        long bytes = getBytes().length;
        statusLabel.setText(String.format(" %d chars | %d lines | %d bytes (UTF-8)",
                chars, lines, bytes));
    }

    /** Detect RSyntaxTextArea syntax style from file extension. */
    private static String detectSyntax(Path path) {
        String name = path.getFileName().toString().toLowerCase();
        if (name.endsWith(".json"))       return SyntaxConstants.SYNTAX_STYLE_JSON;
        if (name.endsWith(".xml"))        return SyntaxConstants.SYNTAX_STYLE_XML;
        if (name.endsWith(".html"))       return SyntaxConstants.SYNTAX_STYLE_XML;
        if (name.endsWith(".yaml"))       return SyntaxConstants.SYNTAX_STYLE_YAML;
        if (name.endsWith(".yml"))        return SyntaxConstants.SYNTAX_STYLE_YAML;
        if (name.endsWith(".sql"))        return SyntaxConstants.SYNTAX_STYLE_SQL;
        if (name.endsWith(".java"))       return SyntaxConstants.SYNTAX_STYLE_JAVA;
        if (name.endsWith(".properties")) return SyntaxConstants.SYNTAX_STYLE_NONE;
        if (name.endsWith(".py"))         return SyntaxConstants.SYNTAX_STYLE_PYTHON;
        if (name.endsWith(".js"))         return SyntaxConstants.SYNTAX_STYLE_JAVASCRIPT;
        if (name.endsWith(".ts"))         return SyntaxConstants.SYNTAX_STYLE_JAVASCRIPT;
        if (name.endsWith(".css"))        return SyntaxConstants.SYNTAX_STYLE_CSS;
        if (name.endsWith(".md"))         return SyntaxConstants.SYNTAX_STYLE_MARKDOWN;
        return SyntaxConstants.SYNTAX_STYLE_NONE;
    }
}
