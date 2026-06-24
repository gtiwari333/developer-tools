package gt.devtools.tools.text;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("TextStatisticsTool.computeStats")
class TextStatisticsToolTest {

    @Test
    @DisplayName("returns null for empty input")
    void emptyInputReturnsNull() {
        assertThat(TextStatisticsTool.computeStats("")).isNull();
    }

    @Test
    @DisplayName("counts characters including spaces")
    void charCount() {
        var stats = TextStatisticsTool.computeStats("hello world");
        assertThat(stats.charCount()).isEqualTo(11);
    }

    @Test
    @DisplayName("counts characters excluding whitespace")
    void charCountNoSpaces() {
        var stats = TextStatisticsTool.computeStats("hello world");
        assertThat(stats.charCountNoSpaces()).isEqualTo(10);
    }

    @Test
    @DisplayName("counts words")
    void wordCount() {
        var stats = TextStatisticsTool.computeStats("hello world foo bar");
        assertThat(stats.wordCount()).isEqualTo(4);
    }

    @Test
    @DisplayName("word count is zero for whitespace-only input")
    void wordCountWhitespaceOnly() {
        var stats = TextStatisticsTool.computeStats("   ");
        assertThat(stats.wordCount()).isEqualTo(0);
    }

    @Test
    @DisplayName("counts unique words")
    void uniqueWordCount() {
        var stats = TextStatisticsTool.computeStats("a b a c b a");
        assertThat(stats.uniqueWordCount()).isEqualTo(3); // a, b, c
    }

    @Test
    @DisplayName("counts lines")
    void lineCount() {
        var stats = TextStatisticsTool.computeStats("line1\nline2\nline3");
        assertThat(stats.lineCount()).isEqualTo(3);
    }

    @Test
    @DisplayName("counts single line")
    void singleLine() {
        var stats = TextStatisticsTool.computeStats("hello");
        assertThat(stats.lineCount()).isEqualTo(1);
        assertThat(stats.wordCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("counts bytes in UTF-8")
    void byteCount() {
        var stats = TextStatisticsTool.computeStats("abc");
        assertThat(stats.byteCountUtf8()).isEqualTo(3);
    }

    @Test
    @DisplayName("multi-byte UTF-8 characters have more bytes than chars")
    void multiByteUtf8() {
        var stats = TextStatisticsTool.computeStats("héllo");
        // é = 2 bytes in UTF-8, so 6 bytes total
        assertThat(stats.byteCountUtf8()).isEqualTo(6);
        assertThat(stats.charCount()).isEqualTo(5);
    }

    @Test
    @DisplayName("format returns readable report")
    void formatReport() {
        var stats = TextStatisticsTool.computeStats("hello");

        String report = stats.format();
        assertThat(report).contains("Character count");
        assertThat(report).contains("Word count");
        assertThat(report).contains("Byte count");
        assertThat(report).contains("5");
    }

    @Test
    @DisplayName("multiline text with duplicate words")
    void multilineWithDupes() {
        var stats = TextStatisticsTool.computeStats("hello world\nhello again\nworld");

        assertThat(stats.lineCount()).isEqualTo(3);
        assertThat(stats.wordCount()).isEqualTo(5); // hello, world, hello, again, world
        assertThat(stats.uniqueWordCount()).isEqualTo(3); // hello, world, again
    }
}
