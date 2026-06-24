package gt.devtools.tools.crypto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashSet;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("PasswordGeneratorTool")
class PasswordGeneratorToolTest {

    // -- generate

    @Test
    @DisplayName("generated password has requested length")
    void correctLength() {
        assertThat(PasswordGeneratorTool.generate(16, true, true, true, true)).hasSize(16);
        assertThat(PasswordGeneratorTool.generate(32, true, false, false, false)).hasSize(32);
    }

    @Test
    @DisplayName("only upper case: all chars from A-Z")
    void onlyUpper() {
        var upper = new HashSet<Character>();
        for (char c : PasswordGeneratorTool.charPool(true, false, false, false).toCharArray())
            upper.add(c);

        for (int i = 0; i < 10; i++) {
            String pw = PasswordGeneratorTool.generate(100, true, false, false, false);
            for (char c : pw.toCharArray()) {
                assertThat(upper).contains(c);
            }
        }
    }

    @Test
    @DisplayName("only lower case: all chars from a-z")
    void onlyLower() {
        var lower = new HashSet<Character>();
        for (char c : PasswordGeneratorTool.charPool(false, true, false, false).toCharArray())
            lower.add(c);

        for (int i = 0; i < 10; i++) {
            String pw = PasswordGeneratorTool.generate(100, false, true, false, false);
            for (char c : pw.toCharArray()) {
                assertThat(lower).contains(c);
            }
        }
    }

    @Test
    @DisplayName("only digits: all chars from 0-9")
    void onlyDigits() {
        for (int i = 0; i < 10; i++) {
            String pw = PasswordGeneratorTool.generate(100, false, false, true, false);
            assertThat(pw).matches("[0-9]+");
        }
    }

    @Test
    @DisplayName("only symbols")
    void onlySymbols() {
        var symbols = new HashSet<Character>();
        for (char c : PasswordGeneratorTool.charPool(false, false, false, true).toCharArray())
            symbols.add(c);

        for (int i = 0; i < 10; i++) {
            String pw = PasswordGeneratorTool.generate(100, false, false, false, true);
            for (char c : pw.toCharArray()) {
                assertThat(symbols).contains(c);
            }
        }
    }

    @Test
    @DisplayName("all pools empty falls back to lowercase")
    void emptyPoolsFallback() {
        var lower = new HashSet<Character>();
        for (char c : PasswordGeneratorTool.charPool(false, false, false, false).toCharArray())
            lower.add(c);

        assertThat(lower).isNotEmpty();
        for (int i = 0; i < 5; i++) {
            String pw = PasswordGeneratorTool.generate(50, false, false, false, false);
            for (char c : pw.toCharArray()) {
                assertThat(lower).contains(c);
            }
        }
    }

    @Test
    @DisplayName("passwords are unique across multiple calls")
    void uniqueAcrossCalls() {
        var seen = new HashSet<String>();
        for (int i = 0; i < 100; i++) {
            String pw = PasswordGeneratorTool.generate(20, true, true, true, true);
            assertThat(seen.add(pw)).as("Duplicate password: %s", pw).isTrue();
        }
    }

    // -- charPool

    @Test
    @DisplayName("charPool includes uppercase when enabled")
    void charPoolUpper() {
        String pool = PasswordGeneratorTool.charPool(true, false, false, false);
        assertThat(pool).contains("A", "Z");
        assertThat(pool).doesNotContain("a", "0", "!");
    }

    @Test
    @DisplayName("charPool includes lowercase when enabled")
    void charPoolLower() {
        String pool = PasswordGeneratorTool.charPool(false, true, false, false);
        assertThat(pool).contains("a", "z");
        assertThat(pool).doesNotContain("A", "0", "!");
    }

    @Test
    @DisplayName("charPool includes digits when enabled")
    void charPoolDigits() {
        String pool = PasswordGeneratorTool.charPool(false, false, true, false);
        assertThat(pool).contains("0", "9");
    }

    @Test
    @DisplayName("charPool includes symbols when enabled")
    void charPoolSymbols() {
        String pool = PasswordGeneratorTool.charPool(false, false, false, true);
        assertThat(pool).contains("!", "@", "#", "$");
    }

    @Test
    @DisplayName("charPool combines multiple character classes")
    void charPoolCombined() {
        String pool = PasswordGeneratorTool.charPool(true, true, true, true);
        assertThat(pool).contains("A", "Z", "a", "z", "0", "9", "!", "@");
    }

    @Test
    @DisplayName("charPool all false falls back to lowercase")
    void charPoolAllFalseFallback() {
        String pool = PasswordGeneratorTool.charPool(false, false, false, false);
        assertThat(pool).isEqualTo(PasswordGeneratorTool.charPool(false, true, false, false));
    }
}
