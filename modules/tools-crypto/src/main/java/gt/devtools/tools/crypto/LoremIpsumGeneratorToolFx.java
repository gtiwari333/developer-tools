package gt.devtools.tools.crypto;

import gt.devtools.common.ValueProperty;
import gt.devtools.settings.ToolConfiguration;
import gt.devtools.tools.api.ToolPresentation;
import gt.devtools.tools.api.fx.OneLineTextGeneratorFx;
import gt.devtools.tools.api.fx.ToolFxFactory;
import javafx.scene.control.*;
import javafx.scene.layout.*;

public final class LoremIpsumGeneratorToolFx extends OneLineTextGeneratorFx {
    private final ValueProperty<String> unit; private final ValueProperty<Integer> count;
    private LoremIpsumGeneratorToolFx(ToolConfiguration config) {
        super(config); unit = registerConfig("loremUnit", "paragraphs"); count = registerConfig("loremCount", 3); }
    @Override protected void buildConfigurationUi(VBox panel) {
        var r1 = new HBox(8); r1.getChildren().add(new Label("Generate:"));
        var combo = new ComboBox<String>(); combo.getItems().addAll("paragraphs", "sentences", "words"); combo.setValue(unit.get());
        combo.setOnAction(e -> unit.set(combo.getValue())); r1.getChildren().add(combo); panel.getChildren().add(r1);
        var r2 = new HBox(8); r2.getChildren().add(new Label("Count:"));
        var spin = new Spinner<Integer>(1, 500, count.get());
        spin.valueProperty().addListener((o, w, v) -> count.set(v)); r2.getChildren().add(spin); panel.getChildren().add(r2);
    }
    @Override protected String generate() {
        return switch (unit.get()) { case "sentences" -> generateSentences(count.get());
            case "words" -> generateWords(count.get());
            default -> generateParagraphs(count.get()); }; }

    private static final String[] LOREM_WORDS = {
        "lorem", "ipsum", "dolor", "sit", "amet", "consectetur", "adipiscing", "elit",
        "sed", "do", "eiusmod", "tempor", "incididunt", "ut", "labore", "et", "dolore",
        "magna", "aliqua", "ut", "enim", "ad", "minim", "veniam", "quis", "nostrud",
        "exercitation", "ullamco", "laboris", "nisi", "ut", "aliquip", "ex", "ea",
        "commodo", "consequat", "duis", "aute", "irure", "dolor", "in", "reprehenderit",
        "in", "voluptate", "velit", "esse", "cillum", "dolore", "eu", "fugiat", "nulla",
        "pariatur", "excepteur", "sint", "occaecat", "cupidatat", "non", "proident",
        "sunt", "in", "culpa", "qui", "officia", "deserunt", "mollit", "anim", "id", "est", "laborum"
    };
    private static final java.util.Random RNG = new java.util.Random();

    public static String[] wordList() { return LOREM_WORDS; }

    public static String generateParagraphs(int count) {
        var sb = new StringBuilder();
        for (int p = 0; p < count; p++) {
            if (p > 0) sb.append("\n\n");
            int sentences = 4 + RNG.nextInt(5);
            for (int s = 0; s < sentences; s++) {
                if (s > 0) sb.append("  ");
                sb.append(generateSentence());
            }
        }
        return sb.toString();
    }

    public static String generateSentences(int count) {
        var sb = new StringBuilder();
        for (int i = 0; i < count; i++) {
            if (i > 0) sb.append("  ");
            sb.append(generateSentence());
        }
        return sb.toString();
    }

    public static String generateWords(int count) {
        var sb = new StringBuilder();
        for (int i = 0; i < count; i++) {
            if (i > 0) sb.append(" ");
            sb.append(LOREM_WORDS[RNG.nextInt(LOREM_WORDS.length)]);
        }
        return sb.toString();
    }

    private static String generateSentence() {
        int wordCount = 5 + RNG.nextInt(11);
        var sb = new StringBuilder();
        for (int i = 0; i < wordCount; i++) {
            if (i > 0) sb.append(" ");
            String word = LOREM_WORDS[RNG.nextInt(LOREM_WORDS.length)];
            if (i == 0) word = Character.toUpperCase(word.charAt(0)) + word.substring(1);
            sb.append(word);
        }
        sb.append(".");
        return sb.toString();
    }

    public static final class Factory implements ToolFxFactory<LoremIpsumGeneratorToolFx> {
        public Factory() {} @Override public String getId() { return "lorem-ipsum-generator"; }
        @Override public ToolPresentation getPresentation() {
            return ToolPresentation.of("lorem-ipsum-generator", "Lorem Ipsum", "Lorem Ipsum Generator").withGroupId("crypto"); }
        @Override public LoremIpsumGeneratorToolFx create(ToolConfiguration c) { return new LoremIpsumGeneratorToolFx(c); }
    }
}
