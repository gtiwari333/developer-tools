package gt.devtools.tools.text;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("HmacTool.hmac")
class HmacToolTest {

    @Test
    @DisplayName("HmacSHA256 produces 64-character hex string")
    void hmacSha256OutputLength() throws Exception {
        String result = HmacTool.hmac("hello", "HmacSHA256", "secret");
        assertThat(result).hasSize(64);
        assertThat(result).matches("[0-9a-f]{64}");
    }

    @Test
    @DisplayName("HmacSHA512 produces 128-character hex string")
    void hmacSha512OutputLength() throws Exception {
        String result = HmacTool.hmac("hello", "HmacSHA512", "secret");
        assertThat(result).hasSize(128);
    }

    @Test
    @DisplayName("same inputs produce same HMAC")
    void deterministic() throws Exception {
        String hmac1 = HmacTool.hmac("hello", "HmacSHA256", "secret");
        String hmac2 = HmacTool.hmac("hello", "HmacSHA256", "secret");
        assertThat(hmac1).isEqualTo(hmac2);
    }

    @Test
    @DisplayName("different secrets produce different HMACs")
    void differentSecretsDifferentHmacs() throws Exception {
        String hmac1 = HmacTool.hmac("hello", "HmacSHA256", "secret1");
        String hmac2 = HmacTool.hmac("hello", "HmacSHA256", "secret2");
        assertThat(hmac1).isNotEqualTo(hmac2);
    }

    @Test
    @DisplayName("different algorithms produce different HMACs")
    void differentAlgorithmsDifferentHmacs() throws Exception {
        String hmac256 = HmacTool.hmac("hello", "HmacSHA256", "secret");
        String hmac512 = HmacTool.hmac("hello", "HmacSHA512", "secret");
        assertThat(hmac256).isNotEqualTo(hmac512);
    }

    @Test
    @DisplayName("known test vector for HmacSHA256")
    void knownTestVector() throws Exception {
        // RFC 4231 test case 1
        String result = HmacTool.hmac(
                "Hi There",
                "HmacSHA256",
                new String(new byte[]{0x0b, 0x0b, 0x0b, 0x0b, 0x0b, 0x0b, 0x0b, 0x0b,
                        0x0b, 0x0b, 0x0b, 0x0b, 0x0b, 0x0b, 0x0b, 0x0b,
                        0x0b, 0x0b, 0x0b, 0x0b}));
        assertThat(result).isEqualTo(
                "b0344c61d8db38535ca8afceaf0bf12b881dc200c9833da726e9376c2e32cff7");
    }

    @Test
    @DisplayName("null secret throws IllegalStateException")
    void nullSecretThrows() {
        assertThatThrownBy(() -> HmacTool.hmac("hello", "HmacSHA256", null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Secret key is required");
    }

    @Test
    @DisplayName("empty secret throws IllegalStateException")
    void emptySecretThrows() {
        assertThatThrownBy(() -> HmacTool.hmac("hello", "HmacSHA256", ""))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Secret key is required");
    }

    @Test
    @DisplayName("empty input works")
    void emptyInput() throws Exception {
        String result = HmacTool.hmac("", "HmacSHA256", "secret");
        assertThat(result).hasSize(64);
    }
}
