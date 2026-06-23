package gt.devtools.tools.standalone;

import gt.devtools.common.ValueProperty;
import gt.devtools.settings.ToolConfiguration;
import gt.devtools.tools.api.DeveloperTool;
import gt.devtools.tools.api.ToolFactory;
import gt.devtools.tools.api.ToolPresentation;
import gt.devtools.tools.api.converter.TextEditor;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.Random;

/**
 * Rubber duck debugging assistant. Type your problem, click "Quack",
 * and get an unhelpful-but-encouraging response. Sometimes that's all
 * you need to find the bug yourself.
 */
public final class RubberDuckTool extends DeveloperTool {

    private static final List<String> RESPONSES = List.of(
            "Have you tried turning it off and on again?",
            "What does the stack trace say?",
            "Is there a missing null check?",
            "Did you read the documentation?",
            "Maybe sleep on it and try again tomorrow.",
            "What would happen if you removed that line?",
            "Is the problem in the code, or in your assumptions?",
            "Try explaining the problem to me in German.",
            "Have you written a test for it?",
            "The bug is probably in the last thing you changed.",
            "Are you sure the database is running?",
            "What happens if you simplify the input?",
            "Could this be a race condition?",
            "Is there a typo in the variable name?",
            "🦆 Quack! (That means you've got this!)"
    );

    private TextEditor inputEditor;
    private TextEditor outputEditor;
    private final ValueProperty<String> problem;

    private RubberDuckTool(ToolConfiguration config) {
        super(config);
        this.problem = registerInput("duckProblem", "");
    }

    @Override
    protected void buildUi(JPanel panel) {
        panel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        panel.setLayout(new BorderLayout(0, 6));

        var header = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        header.add(new JLabel("🦆"));
        var quackBtn = new JButton("Quack!");
        quackBtn.addActionListener(e -> quack());
        header.add(quackBtn);
        var clearBtn = new JButton("Clear");
        clearBtn.addActionListener(e -> { inputEditor.setText(""); outputEditor.setText(""); });
        header.add(clearBtn);
        panel.add(header, BorderLayout.NORTH);

        var split = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        split.setResizeWeight(0.60);
        split.setBorder(null);

        inputEditor = new TextEditor(TextEditor.Mode.INPUT, problem);
        inputEditor.setSyntaxStyle("text/plain");
        var inputPanel = new JPanel(new BorderLayout());
        inputPanel.setBorder(BorderFactory.createTitledBorder("Describe your problem"));
        inputPanel.add(inputEditor, BorderLayout.CENTER);
        split.setTopComponent(inputPanel);

        outputEditor = new TextEditor(TextEditor.Mode.OUTPUT);
        outputEditor.setSyntaxStyle("text/plain");
        var outputPanel = new JPanel(new BorderLayout());
        outputPanel.setBorder(BorderFactory.createTitledBorder("🦆 says..."));
        outputPanel.add(outputEditor, BorderLayout.CENTER);
        split.setBottomComponent(outputPanel);

        panel.add(split, BorderLayout.CENTER);
    }

    private void quack() {
        var rng = new Random();
        String response = RESPONSES.get(rng.nextInt(RESPONSES.size()));
        String current = outputEditor.getText();
        if (!current.isEmpty()) current += "\n\n";
        outputEditor.setText(current + "🦆 " + response);
    }

    public static final class Factory implements ToolFactory<RubberDuckTool> {
        public Factory() {}
        @Override public String getId() { return "rubber-duck"; }
        @Override public ToolPresentation getPresentation() {
            return ToolPresentation.of("rubber-duck", "Rubber Duck", "Rubber Duck Debugging");
        }
        @Override public RubberDuckTool create(ToolConfiguration config) { return new RubberDuckTool(config); }
    }
}
