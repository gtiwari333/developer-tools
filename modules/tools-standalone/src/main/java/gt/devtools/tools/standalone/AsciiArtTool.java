package gt.devtools.tools.standalone;

import com.github.lalyos.jfiglet.FigletFont;
import gt.devtools.common.ValueProperty;
import gt.devtools.settings.ToolConfiguration;
import gt.devtools.tools.api.DeveloperTool;
import gt.devtools.tools.api.ToolFactory;
import gt.devtools.tools.api.ToolPresentation;
import gt.devtools.tools.api.converter.TextEditor;

import javax.swing.*;
import java.awt.*;

/** Generates ASCII art banners using FIGlet fonts. */
public final class AsciiArtTool extends DeveloperTool {

    private JTextField inputField;
    private TextEditor outputEditor;
    private final ValueProperty<String> fontName;
    private final ValueProperty<String> text;

    private AsciiArtTool(ToolConfiguration config) {
        super(config);
        this.fontName = registerConfig("figletFont", "standard");
        this.text = registerConfig("asciiText", "Hello");
    }

    @Override
    protected void buildUi(JPanel panel) {
        panel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        panel.setLayout(new BorderLayout(0, 8));

        var configPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        configPanel.add(new JLabel("Font:"));
        var fontField = new JTextField(fontName.get(), 12);
        fontField.addActionListener(e -> { fontName.set(fontField.getText()); render(); });
        configPanel.add(fontField);
        configPanel.add(new JLabel("Text:"));
        inputField = new JTextField(text.get(), 20);
        inputField.addActionListener(e -> { text.set(inputField.getText()); render(); });
        configPanel.add(inputField);
        var renderBtn = new JButton("Render");
        renderBtn.addActionListener(e -> render());
        configPanel.add(renderBtn);
        panel.add(configPanel, BorderLayout.NORTH);

        outputEditor = new TextEditor(TextEditor.Mode.OUTPUT);
        outputEditor.setSyntaxStyle("text/plain");
        panel.add(outputEditor, BorderLayout.CENTER);

        if (text.get() != null && !text.get().isEmpty()) render();
    }

    private void render() {
        try {
            String result = FigletFont.convertOneLine(inputField.getText());
            outputEditor.setText(result);
        } catch (Exception e) {
            outputEditor.setText("Error: " + e.getMessage());
        }
    }

    public static final class Factory implements ToolFactory<AsciiArtTool> {
        public Factory() {}
        @Override public String getId() { return "ascii-art"; }
        @Override public ToolPresentation getPresentation() {
            return ToolPresentation.of("ascii-art", "ASCII Art", "ASCII Art Generator");
        }
        @Override public AsciiArtTool create(ToolConfiguration config) { return new AsciiArtTool(config); }
    }
}
