package gt.devtools.tools.text;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("HashingTool.hash")
class HashingToolTest {

    @Test
    @DisplayName("SHA-256 produces 64-character hex string")
    void sha256OutputLength() throws Exception {
        String result = HashingTool.hash("hello", "SHA-256");
        assertThat(result).hasSize(64);
        assertThat(result).matches("[0-9a-f]{64}");
    }

    @Test
    @DisplayName("SHA-512 produces 128-character hex string")
    void sha512OutputLength() throws Exception {
        String result = HashingTool.hash("hello", "SHA-512");
        assertThat(result).hasSize(128);
    }

    @Test
    @DisplayName("MD5 produces 32-character hex string")
    void md5OutputLength() throws Exception {
        String result = HashingTool.hash("hello", "MD5");
        assertThat(result).hasSize(32);
    }

    @Test
    @DisplayName("SHA-1 produces 40-character hex string")
    void sha1OutputLength() throws Exception {
        String result = HashingTool.hash("hello", "SHA-1");
        assertThat(result).hasSize(40);
    }

    @Test
    @DisplayName("same input produces same hash")
    void deterministic() throws Exception {
        String hash1 = HashingTool.hash("hello", "SHA-256");
        String hash2 = HashingTool.hash("hello", "SHA-256");
        assertThat(hash1).isEqualTo(hash2);
    }

    @Test
    @DisplayName("different inputs produce different hashes")
    void differentInputsDifferentHashes() throws Exception {
        String hash1 = HashingTool.hash("hello", "SHA-256");
        String hash2 = HashingTool.hash("world", "SHA-256");
        assertThat(hash1).isNotEqualTo(hash2);
    }

    @Test
    @DisplayName("different algorithms produce different hashes")
    void differentAlgorithmsDifferentHashes() throws Exception {
        String sha256 = HashingTool.hash("hello", "SHA-256");
        String sha512 = HashingTool.hash("hello", "SHA-512");
        assertThat(sha256).isNotEqualTo(sha512);
    }

    @ParameterizedTest
    @CsvSource({
        "hello, SHA-256, 2cf24dba5fb0a30e26e83b2ac5b9e29e1b161e5c1fa7425e73043362938b9824",
        "hello, MD5, 5d41402abc4b2a76b9719d911017c592",
        "hello, SHA-1, aaf4c61ddcc5e8a2dabede0f3b482cd9aea9434d",
    })
    @DisplayName("known test vectors for common algorithms")
    void knownTestVectors(String input, String algorithm, String expected) throws Exception {
        assertThat(HashingTool.hash(input, algorithm)).isEqualTo(expected);
    }

    @Test
    @DisplayName("empty input")
    void emptyInput() throws Exception {
        String result = HashingTool.hash("", "SHA-256");
        assertThat(result).hasSize(64);
    }

    @Test
    @DisplayName("unicode input works")
    void unicodeInput() throws Exception {
        String result = HashingTool.hash("héllo", "SHA-256");
        assertThat(result).hasSize(64);
        assertThat(result).isNotEqualTo(HashingTool.hash("hello", "SHA-256"));
    }
}
