package gt.devtools.tools.standalone;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("RegexMatcherToolFx static methods")
class RegexMatcherToolTest {

    @Test @DisplayName("findMatches: returns matches with indices")
    void findMatchesBasic() {
        String result = RegexMatcherToolFx.findMatches("cat dog cat", "cat");
        assertThat(result).contains("Match 1: cat");
        assertThat(result).contains("Match 2: cat");
    }

    @Test @DisplayName("findMatches: returns 'No matches found' when no match")
    void findMatchesNone() {
        assertThat(RegexMatcherToolFx.findMatches("hello", "xyz"))
                .isEqualTo("No matches found.");
    }

    @Test @DisplayName("findMatches: shows capture groups")
    void findMatchesWithGroups() {
        String result = RegexMatcherToolFx.findMatches("2024-01-15", "(\\d{4})-(\\d{2})-(\\d{2})");
        assertThat(result).contains("Group 1: 2024");
        assertThat(result).contains("Group 2: 01");
        assertThat(result).contains("Group 3: 15");
    }

    @Test @DisplayName("replaceAll: replaces matches")
    void replaceAllBasic() {
        String result = RegexMatcherToolFx.replaceAll("cat dog cat", "cat", "bird");
        assertThat(result).isEqualTo("bird dog bird");
    }

    @Test @DisplayName("replaceAll: no matches leaves input unchanged")
    void replaceAllNoMatch() {
        assertThat(RegexMatcherToolFx.replaceAll("hello", "xyz", "replaced"))
                .isEqualTo("hello");
    }

    @Test @DisplayName("split: splits by pattern")
    void splitBasic() {
        String result = RegexMatcherToolFx.split("a,b,c", ",");
        assertThat(result).isEqualTo("a\nb\nc");
    }

    @Test @DisplayName("split: splits by whitespace")
    void splitByWhitespace() {
        String result = RegexMatcherToolFx.split("hello world foo", "\\s+");
        assertThat(result).isEqualTo("hello\nworld\nfoo");
    }
}
