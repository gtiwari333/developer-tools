package gt.devtools.tools.text;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("TextDiffTool.diff")
class TextDiffToolTest {

    @Test
    @DisplayName("identical inputs return '(no differences)'")
    void identicalInputs() {
        String result = TextDiffTool.diff("line1\nline2", "line1\nline2");
        assertThat(result).isEqualTo("(no differences)");
    }

    @Test
    @DisplayName("single line changed")
    void singleLineChanged() {
        String result = TextDiffTool.diff("hello", "world");

        assertThat(result).startsWith("--- original\n+++ revised");
        assertThat(result).contains("- hello");
        assertThat(result).contains("+ world");
    }

    @Test
    @DisplayName("line added")
    void lineAdded() {
        String result = TextDiffTool.diff("line1\nline2", "line1\nline2\nline3");

        assertThat(result).contains("+ line3");
    }

    @Test
    @DisplayName("line removed")
    void lineRemoved() {
        String result = TextDiffTool.diff("line1\nline2\nline3", "line1\nline3");

        assertThat(result).contains("- line2");
    }

    @Test
    @DisplayName("multiple changes")
    void multipleChanges() {
        String result = TextDiffTool.diff("a\nb\nc", "a\nx\nc");

        assertThat(result).contains("- b");
        assertThat(result).contains("+ x");
    }

    @Test
    @DisplayName("empty original with content in revised")
    void emptyOriginal() {
        String result = TextDiffTool.diff("", "new content");

        assertThat(result).contains("+ new content");
    }

    @Test
    @DisplayName("content in original with empty revised")
    void emptyRevised() {
        String result = TextDiffTool.diff("old content", "");

        assertThat(result).contains("- old content");
    }

    @Test
    @DisplayName("both empty")
    void bothEmpty() {
        String result = TextDiffTool.diff("", "");
        assertThat(result).isEqualTo("(no differences)");
    }
}
