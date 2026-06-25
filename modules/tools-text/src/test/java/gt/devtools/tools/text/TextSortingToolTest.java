package gt.devtools.tools.text;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("TextSortingToolFx static methods")
class TextSortingToolTest {

    @Test
    @DisplayName("sortAscending: sorts lines alphabetically")
    void ascendingBasic() {
        String result = TextSortingToolFx.sortAscending("c\na\nb");

        assertThat(result).isEqualTo("a\nb\nc");
    }

    @Test
    @DisplayName("sortAscending: handles duplicates")
    void ascendingWithDuplicates() {
        String result = TextSortingToolFx.sortAscending("z\na\nz\na");

        assertThat(result).isEqualTo("a\na\nz\nz");
    }

    @Test
    @DisplayName("sortAscending: single line unchanged")
    void ascendingSingleLine() {
        assertThat(TextSortingToolFx.sortAscending("hello")).isEqualTo("hello");
    }

    @Test
    @DisplayName("sortAscending: empty input")
    void ascendingEmpty() {
        assertThat(TextSortingToolFx.sortAscending("")).isEmpty();
    }

    @Test
    @DisplayName("sortAscending: case-sensitive by default")
    void ascendingCaseSensitive() {
        String result = TextSortingToolFx.sortAscending("Apple\nbanana\nCherry");

        // Lexicographic: uppercase letters come before lowercase in ASCII
        assertThat(result).isEqualTo("Apple\nCherry\nbanana");
    }

    @Test
    @DisplayName("sortDescending: sorts lines in reverse order")
    void descendingBasic() {
        String result = TextSortingToolFx.sortDescending("a\nb\nc");

        assertThat(result).isEqualTo("c\nb\na");
    }

    @Test
    @DisplayName("sortDescending: with duplicates")
    void descendingWithDuplicates() {
        String result = TextSortingToolFx.sortDescending("a\nz\na\nz");

        assertThat(result).isEqualTo("z\nz\na\na");
    }

    @Test
    @DisplayName("sortDescending: single line unchanged")
    void descendingSingleLine() {
        assertThat(TextSortingToolFx.sortDescending("hello")).isEqualTo("hello");
    }

    @Test
    @DisplayName("ascending and descending are inverses (symmetric input)")
    void ascendingDescendingInverses() {
        String input = "c\nb\na\nc\nb\na";
        String asc = TextSortingToolFx.sortAscending(input);
        String desc = TextSortingToolFx.sortDescending(input);

        // For symmetric input with duplicates, they should be mirror images
        assertThat(asc).isEqualTo(
                new StringBuilder(TextSortingToolFx.sortDescending(input)).reverse().toString()
                        .replace("\n", "|\n|").replace("|\n|", "\n"));
        // Actually, just verify they produce different outputs for non-trivial input
        assertThat(asc).isNotEqualTo(desc);
    }
}
