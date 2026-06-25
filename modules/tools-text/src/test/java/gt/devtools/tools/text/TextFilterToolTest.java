package gt.devtools.tools.text;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("TextFilterToolFx static methods")
class TextFilterToolTest {

    // -- filterInclude

    @Test
    @DisplayName("filterInclude: keeps only matching lines")
    void includeMatchingLines() {
        String input = "apple\nbanana\napricot\ncherry";
        String result = TextFilterToolFx.filterInclude(input, "^a");

        assertThat(result).isEqualTo("apple\napricot");
    }

    @Test
    @DisplayName("filterInclude: empty result when no matches")
    void includeNoMatches() {
        String result = TextFilterToolFx.filterInclude("a\nb\nc", "z");

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("filterInclude: all lines match")
    void includeAllMatch() {
        String result = TextFilterToolFx.filterInclude("cat\ndog\nbat", "[a-z]+");

        assertThat(result).isEqualTo("cat\ndog\nbat");
    }

    // -- filterExclude

    @Test
    @DisplayName("filterExclude: removes matching lines")
    void excludeMatchingLines() {
        String input = "apple\nbanana\napricot\ncherry";
        String result = TextFilterToolFx.filterExclude(input, "^a");

        assertThat(result).isEqualTo("banana\ncherry");
    }

    @Test
    @DisplayName("filterExclude: keeps all when no matches")
    void excludeNoMatches() {
        String result = TextFilterToolFx.filterExclude("a\nb\nc", "z");

        assertThat(result).isEqualTo("a\nb\nc");
    }

    @Test
    @DisplayName("filterExclude: removes everything when all match")
    void excludeAllMatch() {
        String result = TextFilterToolFx.filterExclude("cat\ndog\nbat", "[a-z]+");

        assertThat(result).isEmpty();
    }

    // -- filterUnique

    @Test
    @DisplayName("filterUnique: removes duplicate lines")
    void uniqueRemovesDuplicates() {
        String input = "a\nb\na\nc\nb";
        String result = TextFilterToolFx.filterUnique(input);

        assertThat(result).isEqualTo("a\nb\nc");
    }

    @Test
    @DisplayName("filterUnique: preserves order of first occurrence")
    void uniquePreservesOrder() {
        String input = "z\na\nz\nb\na";
        String result = TextFilterToolFx.filterUnique(input);

        assertThat(result).isEqualTo("z\na\nb");
    }

    @Test
    @DisplayName("filterUnique: no change when already unique")
    void uniqueNoDuplicates() {
        String result = TextFilterToolFx.filterUnique("a\nb\nc");

        assertThat(result).isEqualTo("a\nb\nc");
    }

    @Test
    @DisplayName("filterUnique: single line")
    void uniqueSingleLine() {
        assertThat(TextFilterToolFx.filterUnique("hello")).isEqualTo("hello");
    }

    // -- trimLines

    @Test
    @DisplayName("trimLines: strips whitespace from each line")
    void trimBasic() {
        String input = "  hello  \n  world  \n  foo  ";
        String result = TextFilterToolFx.trimLines(input);

        assertThat(result).isEqualTo("hello\nworld\nfoo");
    }

    @Test
    @DisplayName("trimLines: removes trailing newline")
    void trimNoTrailingNewline() {
        String result = TextFilterToolFx.trimLines("a\nb\nc");

        assertThat(result).isEqualTo("a\nb\nc");
    }

    @Test
    @DisplayName("trimLines: empty lines become empty")
    void trimEmptyLines() {
        String result = TextFilterToolFx.trimLines("   \n   ");

        assertThat(result).isEmpty();
    }

    // -- edge cases

    @Test
    @DisplayName("regex with special characters")
    void regexSpecialChars() {
        String input = "(test)\n[real]\n{json}";
        String result = TextFilterToolFx.filterInclude(input, "\\(");

        assertThat(result).isEqualTo("(test)");
    }
}
