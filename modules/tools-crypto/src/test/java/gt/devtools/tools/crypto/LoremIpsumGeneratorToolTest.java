package gt.devtools.tools.crypto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.HashSet;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("LoremIpsumGeneratorToolFx")
class LoremIpsumGeneratorToolTest {

    @Test @DisplayName("wordList contains expected words")
    void wordListContainsLorem() {
        assertThat(LoremIpsumGeneratorToolFx.wordList()).contains("lorem", "ipsum", "dolor");
    }

    @Test @DisplayName("generateWords returns correct count")
    void wordsCount() {
        assertThat(LoremIpsumGeneratorToolFx.generateWords(5).split(" ")).hasSize(5);
        assertThat(LoremIpsumGeneratorToolFx.generateWords(10).split(" ")).hasSize(10);
    }

    @Test @DisplayName("generateWords only uses words from the word list")
    void wordsFromList() {
        var valid = new HashSet<>(Arrays.asList(LoremIpsumGeneratorToolFx.wordList()));
        for (String word : LoremIpsumGeneratorToolFx.generateWords(50).split(" ")) {
            assertThat(valid).contains(word);
        }
    }

    @Test @DisplayName("generateSentences starts with capital letter and ends with period")
    void sentencesCapitalizedAndPunctuated() {
        String sentences = LoremIpsumGeneratorToolFx.generateSentences(5);
        assertThat(sentences).matches("^[A-Z].*\\.$");
    }

    @Test @DisplayName("generateSentences returns correct number of sentences")
    void sentencesCount() {
        String s = LoremIpsumGeneratorToolFx.generateSentences(3);
        assertThat(s.split("\\. ")).hasSize(3);
    }

    @Test @DisplayName("generateParagraphs separates paragraphs with blank lines")
    void paragraphsSeparated() {
        String result = LoremIpsumGeneratorToolFx.generateParagraphs(3);
        assertThat(result).contains("\n\n");
    }

    @Test @DisplayName("generateParagraphs has correct paragraph count")
    void paragraphsCount() {
        String result = LoremIpsumGeneratorToolFx.generateParagraphs(2);
        assertThat(result.split("\n\n")).hasSize(2);
    }
}
