package gt.devtools.tools.escape;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("EscapeSequencesEscaperUnescaperFx static methods")
class EscapeSequencesEscaperUnescaperTest {

    // -- escape

    @Test
    @DisplayName("escape: converts newline to \\n")
    void escapeNewline() {
        assertThat(EscapeSequencesEscaperUnescaperFx.escape("a\nb")).isEqualTo("a\\nb");
    }

    @Test
    @DisplayName("escape: converts tab to \\t")
    void escapeTab() {
        assertThat(EscapeSequencesEscaperUnescaperFx.escape("a\tb")).isEqualTo("a\\tb");
    }

    @Test
    @DisplayName("escape: converts carriage return to \\r")
    void escapeCarriageReturn() {
        assertThat(EscapeSequencesEscaperUnescaperFx.escape("a\rb")).isEqualTo("a\\rb");
    }

    @Test
    @DisplayName("escape: converts backslash to \\\\")
    void escapeBackslash() {
        assertThat(EscapeSequencesEscaperUnescaperFx.escape("a\\b")).isEqualTo("a\\\\b");
    }

    @Test
    @DisplayName("escape: converts double quote to \\\"")
    void escapeDoubleQuote() {
        assertThat(EscapeSequencesEscaperUnescaperFx.escape("a\"b")).isEqualTo("a\\\"b");
    }

    @Test
    @DisplayName("escape: converts null byte to \\0")
    void escapeNullByte() {
        assertThat(EscapeSequencesEscaperUnescaperFx.escape("a\0b")).isEqualTo("a\\0b");
    }

    @Test
    @DisplayName("escape: handles multiple special characters")
    void escapeMultipleSpecialChars() {
        String result = EscapeSequencesEscaperUnescaperFx.escape("hello\nworld\t!");
        assertThat(result).isEqualTo("hello\\nworld\\t!");
    }

    @Test
    @DisplayName("escape: normal text unchanged")
    void escapeNoSpecialChars() {
        assertThat(EscapeSequencesEscaperUnescaperFx.escape("hello world"))
                .isEqualTo("hello world");
    }

    @Test
    @DisplayName("escape: empty string")
    void escapeEmpty() {
        assertThat(EscapeSequencesEscaperUnescaperFx.escape("")).isEmpty();
    }

    @Test
    @DisplayName("escape: backslash escape happens first to avoid double-escaping")
    void escapeBackslashFirst() {
        // \n literal (backslash + n) → should become \\n, not \\\n
        String result = EscapeSequencesEscaperUnescaperFx.escape("\\n");
        assertThat(result).isEqualTo("\\\\n");
    }

    // -- unescape

    @Test
    @DisplayName("unescape: converts \\n to newline")
    void unescapeNewline() {
        assertThat(EscapeSequencesEscaperUnescaperFx.unescape("a\\nb")).isEqualTo("a\nb");
    }

    @Test
    @DisplayName("unescape: converts \\t to tab")
    void unescapeTab() {
        assertThat(EscapeSequencesEscaperUnescaperFx.unescape("a\\tb")).isEqualTo("a\tb");
    }

    @Test
    @DisplayName("unescape: converts \\r to carriage return")
    void unescapeCarriageReturn() {
        assertThat(EscapeSequencesEscaperUnescaperFx.unescape("a\\rb")).isEqualTo("a\rb");
    }

    @Test
    @DisplayName("unescape: converts \\\\ to single backslash")
    void unescapeBackslash() {
        assertThat(EscapeSequencesEscaperUnescaperFx.unescape("a\\\\b")).isEqualTo("a\\b");
    }

    @Test
    @DisplayName("unescape: converts \\\" to double quote")
    void unescapeDoubleQuote() {
        assertThat(EscapeSequencesEscaperUnescaperFx.unescape("a\\\"b")).isEqualTo("a\"b");
    }

    @Test
    @DisplayName("unescape: converts \\0 to null byte")
    void unescapeNullByte() {
        assertThat(EscapeSequencesEscaperUnescaperFx.unescape("a\\0b")).isEqualTo("a\0b");
    }

    @Test
    @DisplayName("unescape: unknown escape sequences preserved as-is")
    void unescapeUnknownEscape() {
        assertThat(EscapeSequencesEscaperUnescaperFx.unescape("a\\xb"))
                .isEqualTo("a\\xb");
    }

    @Test
    @DisplayName("unescape: normal text unchanged")
    void unescapeNoSequences() {
        assertThat(EscapeSequencesEscaperUnescaperFx.unescape("hello world"))
                .isEqualTo("hello world");
    }

    @Test
    @DisplayName("unescape: empty string")
    void unescapeEmpty() {
        assertThat(EscapeSequencesEscaperUnescaperFx.unescape("")).isEmpty();
    }

    @Test
    @DisplayName("unescape: trailing backslash left as-is")
    void unescapeTrailingBackslash() {
        assertThat(EscapeSequencesEscaperUnescaperFx.unescape("hello\\"))
                .isEqualTo("hello\\");
    }

    // -- round-trip

    @ParameterizedTest
    @CsvSource(delimiter = '|', value = {
        "hello world",
        "line1\nline2",
        "tab\tseparated",
        "back\\slash",
        "double\"quote",
        "mixed\n\t\rchars",
    })
    @DisplayName("round-trip: escape then unescape returns original")
    void roundTrip(String original) {
        String escaped = EscapeSequencesEscaperUnescaperFx.escape(original);
        String unescaped = EscapeSequencesEscaperUnescaperFx.unescape(escaped);
        assertThat(unescaped).isEqualTo(original);
    }

    @Test
    @DisplayName("round-trip: complex string with all special chars")
    void roundTripComplex() {
        String original = "hello\nworld\t\"test\"\r\nfoo\\bar\0null";
        String escaped = EscapeSequencesEscaperUnescaperFx.escape(original);
        String unescaped = EscapeSequencesEscaperUnescaperFx.unescape(escaped);
        assertThat(unescaped).isEqualTo(original);
    }
}
