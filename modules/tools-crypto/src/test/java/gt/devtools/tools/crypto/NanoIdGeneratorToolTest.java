package gt.devtools.tools.crypto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.HashSet;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("NanoIdGeneratorToolFx.generate")
class NanoIdGeneratorToolTest {

    @Test
    @DisplayName("generated ID has requested length")
    void correctLength() {
        assertThat(NanoIdGeneratorToolFx.generate(10)).hasSize(10);
        assertThat(NanoIdGeneratorToolFx.generate(21)).hasSize(21);
        assertThat(NanoIdGeneratorToolFx.generate(50)).hasSize(50);
    }

    @Test
    @DisplayName("generated ID only uses alphabet characters")
    void onlyAlphabetCharacters() {
        var alphabet = new HashSet<Character>();
        for (char c : NanoIdGeneratorToolFx.alphabet()) alphabet.add(c);

        for (int i = 0; i < 20; i++) {
            String id = NanoIdGeneratorToolFx.generate(100);
            for (char c : id.toCharArray()) {
                assertThat(alphabet).contains(c);
            }
        }
    }

    @Test
    @DisplayName("alphabet has 64 characters")
    void alphabetSize() {
        assertThat(NanoIdGeneratorToolFx.alphabet()).hasSize(64);
    }

    @Test
    @DisplayName("alphabet contains URL-safe characters only")
    void alphabetUrlSafe() {
        for (char c : NanoIdGeneratorToolFx.alphabet()) {
            // URL-safe: A-Z, a-z, 0-9, -, _
            assertThat(Character.isLetterOrDigit(c) || c == '-' || c == '_')
                    .as("Character '%c' should be URL-safe", c)
                    .isTrue();
        }
    }

    @ParameterizedTest
    @ValueSource(ints = {5, 10, 21, 50, 100})
    @DisplayName("generated IDs are unique across many calls")
    void uniqueAcrossCalls(int length) {
        var seen = new HashSet<String>();
        for (int i = 0; i < 50; i++) {
            String id = NanoIdGeneratorToolFx.generate(length);
            assertThat(seen.add(id)).as("Duplicate ID: %s", id).isTrue();
        }
    }

    @Test
    @DisplayName("minimum length 1 works")
    void minimumLength() {
        String id = NanoIdGeneratorToolFx.generate(1);
        assertThat(id).hasSize(1);
    }
}
