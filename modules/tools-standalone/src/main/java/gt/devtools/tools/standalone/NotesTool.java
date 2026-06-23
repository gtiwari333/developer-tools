package gt.devtools.tools.standalone;

import gt.devtools.common.ValueProperty;
import gt.devtools.settings.ToolConfiguration;
import gt.devtools.tools.api.DeveloperTool;
import gt.devtools.tools.api.ToolFactory;
import gt.devtools.tools.api.ToolPresentation;
import gt.devtools.tools.api.converter.TextEditor;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.StringSelection;

/** Simple persistent note-taking tool. Content is saved automatically. */
public final class NotesTool extends DeveloperTool {

    private TextEditor editor;
    private final ValueProperty<String> content;

    private NotesTool(ToolConfiguration config) {
        super(config);
        this.content = registerInput("noteContent", "");
    }

    @Override
    protected void buildUi(JPanel panel) {
        panel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        panel.setLayout(new BorderLayout(0, 4));

        var toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 2));
        var copyBtn = new JButton("Copy All");
        copyBtn.addActionListener(e -> Toolkit.getDefaultToolkit().getSystemClipboard()
                .setContents(new StringSelection(editor.getText()), null));
        toolbar.add(copyBtn);
        var clearBtn = new JButton("Clear");
        clearBtn.addActionListener(e -> editor.setText(""));
        toolbar.add(clearBtn);
        toolbar.add(new JLabel("  Notes are saved automatically."));
        panel.add(toolbar, BorderLayout.NORTH);

        editor = new TextEditor(TextEditor.Mode.INPUT, content);
        editor.setSyntaxStyle("text/markdown");
        panel.add(editor, BorderLayout.CENTER);
    }

    public static final class Factory implements ToolFactory<NotesTool> {
        public Factory() {}
        @Override public String getId() { return "notes"; }
        @Override public ToolPresentation getPresentation() {
            return ToolPresentation.of("notes", "Notes", "Notes");
        }
        @Override public NotesTool create(ToolConfiguration config) { return new NotesTool(config); }
    }
}
