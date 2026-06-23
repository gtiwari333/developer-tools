package gt.devtools.tools.crypto;

import gt.devtools.common.ValueProperty;
import gt.devtools.settings.ToolConfiguration;
import gt.devtools.tools.api.ToolFactory;
import gt.devtools.tools.api.ToolPresentation;
import gt.devtools.tools.api.generator.OneLineTextGenerator;

import javax.swing.*;
import java.awt.*;
import java.util.random.RandomGenerator;

/**
 * Generates Lorem Ipsum placeholder text in paragraphs, sentences, or words.
 */
public final class LoremIpsumGeneratorTool extends OneLineTextGenerator {

    private static final String[] WORDS = {
            "lorem", "ipsum", "dolor", "sit", "amet", "consectetur", "adipiscing", "elit",
            "sed", "do", "eiusmod", "tempor", "incididunt", "ut", "labore", "et", "dolore",
            "magna", "aliqua", "ut", "enim", "ad", "minim", "veniam", "quis", "nostrud",
            "exercitation", "ullamco", "laboris", "nisi", "ut", "aliquip", "ex", "ea",
            "commodo", "consequat", "duis", "aute", "irure", "dolor", "in", "reprehenderit",
            "in", "voluptate", "velit", "esse", "cillum", "dolore", "eu", "fugiat", "nulla",
            "pariatur", "excepteur", "sint", "occaecat", "cupidatat", "non", "proident",
            "sunt", "in", "culpa", "qui", "officia", "deserunt", "mollit", "anim", "id",
            "est", "laborum"
    };

    private final ValueProperty<String> unit;
    private final ValueProperty<Integer> count;

    private LoremIpsumGeneratorTool(ToolConfiguration config) {
        super(config);
        this.unit = registerConfig("loremUnit", "paragraphs");
        this.count = registerConfig("loremCount", 3);
    }

    @Override
    protected void buildConfigurationUi(JPanel configPanel) {
        var row1 = new JPanel(new FlowLayout(FlowLayout.LEFT));
        row1.add(new JLabel("Generate:"));
        var unitCombo = new JComboBox<>(new String[]{"paragraphs", "sentences", "words"});
        unitCombo.setSelectedItem(unit.get());
        unitCombo.addActionListener(e -> unit.set((String) unitCombo.getSelectedItem()));
        row1.add(unitCombo);
        configPanel.add(row1);

        var row2 = new JPanel(new FlowLayout(FlowLayout.LEFT));
        row2.add(new JLabel("Count:"));
        var spinner = new JSpinner(new javax.swing.SpinnerNumberModel(
                count.get().intValue(), 1, 500, 1));
        spinner.addChangeListener(e -> count.set((Integer) spinner.getValue()));
        row2.add(spinner);
        configPanel.add(row2);
    }

    @Override
    protected String generate() {
        return switch (unit.get()) {
            case "sentences" -> generateSentences(count.get());
            case "words" -> generateWords(count.get());
            default -> generateParagraphs(count.get());
        };
    }

    private String generateParagraphs(int n) {
        var sb = new StringBuilder();
        for (int p = 0; p < n; p++) {
            sb.append(generateSentences(3 + rng().nextInt(5)));
            if (p < n - 1) sb.append("\n\n");
        }
        return sb.toString();
    }

    private String generateSentences(int n) {
        var sb = new StringBuilder();
        for (int s = 0; s < n; s++) {
            int wordCount = 5 + rng().nextInt(12);
            for (int w = 0; w < wordCount; w++) {
                String word = WORDS[rng().nextInt(WORDS.length)];
                if (w == 0) word = word.substring(0, 1).toUpperCase() + word.substring(1);
                sb.append(word);
                if (w < wordCount - 1) sb.append(" ");
            }
            sb.append(". ");
        }
        return sb.toString().strip();
    }

    private String generateWords(int n) {
        var sb = new StringBuilder();
        for (int w = 0; w < n; w++) {
            sb.append(WORDS[rng().nextInt(WORDS.length)]);
            if (w < n - 1) sb.append(" ");
        }
        return sb.toString();
    }

    private static RandomGenerator rng() { return RandomGenerator.getDefault(); }

    /** Bulk output is text-based, not single-line, so we override bulk to join paragraphs cleanly. */
    @Override
    protected boolean supportsBulkGeneration() { return true; }

    public static final class Factory implements ToolFactory<LoremIpsumGeneratorTool> {
        public Factory() {}
        @Override public String getId() { return "lorem-ipsum-generator"; }
        @Override
        public ToolPresentation getPresentation() {
            return ToolPresentation.of("lorem-ipsum-generator",
                    "Lorem Ipsum Generator", "Lorem Ipsum Generator").withGroupId("crypto");
        }
        @Override
        public LoremIpsumGeneratorTool create(ToolConfiguration config) {
            return new LoremIpsumGeneratorTool(config);
        }
    }
}
