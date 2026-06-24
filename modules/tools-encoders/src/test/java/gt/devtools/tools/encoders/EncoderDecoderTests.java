package gt.devtools.tools.encoders;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

/** Round-trip and smoke tests for all encoder/decoder tools. */
@DisplayName("Encoder/Decoder round-trip tests")
class EncoderDecoderTests {

    @ParameterizedTest
    @CsvSource({
        "Hello, World!",
        "The quick brown fox",
        "1234567890",
        "a",
        "Special chars: !@#$%^&*()",
    })
    @DisplayName("Base64: encode then decode returns original")
    void base64RoundTrip(String input) {
        byte[] data = input.getBytes(StandardCharsets.UTF_8);
        byte[] encoded = Base64EncoderDecoder.encode(data);
        byte[] decoded = Base64EncoderDecoder.decode(encoded);
        assertThat(new String(decoded, StandardCharsets.UTF_8)).isEqualTo(input);
    }

    @Test @DisplayName("Base64: empty input round-trip")
    void base64EmptyRoundTrip() {
        byte[] encoded = Base64EncoderDecoder.encode(new byte[0]);
        byte[] decoded = Base64EncoderDecoder.decode(encoded);
        assertThat(decoded).isEmpty();
    }

    // Base32 uses an instance field for the codec and has a private constructor.
    // Tested indirectly via the encode/decode instance methods through the tool's UI.

    @ParameterizedTest
    @CsvSource({
        "Hello, World!",
        "test+data/with=special",
    })
    @DisplayName("URL Base64: encode then decode returns original")
    void urlBase64RoundTrip(String input) {
        byte[] data = input.getBytes(StandardCharsets.UTF_8);
        byte[] encoded = UrlBase64EncoderDecoder.encode(data);
        byte[] decoded = UrlBase64EncoderDecoder.decode(encoded);
        assertThat(new String(decoded, StandardCharsets.UTF_8)).isEqualTo(input);
    }

    @ParameterizedTest
    @CsvSource({
        "Hello, World!",
        "MIME test data",
    })
    @DisplayName("MIME Base64: encode then decode returns original")
    void mimeBase64RoundTrip(String input) {
        byte[] data = input.getBytes(StandardCharsets.UTF_8);
        byte[] encoded = MimeBase64EncoderDecoder.encode(data);
        byte[] decoded = MimeBase64EncoderDecoder.decode(encoded);
        assertThat(new String(decoded, StandardCharsets.UTF_8)).isEqualTo(input);
    }

    @ParameterizedTest
    @CsvSource({
        "hello world",
        "special chars: !@#",
        "12345",
    })
    @DisplayName("URL Encoding: encode then decode returns original")
    void urlEncodingRoundTrip(String input) throws Exception {
        byte[] data = input.getBytes(StandardCharsets.UTF_8);
        byte[] encoded = UrlEncodingEncoderDecoder.encode(data);
        byte[] decoded = UrlEncodingEncoderDecoder.decode(encoded);
        assertThat(new String(decoded, StandardCharsets.UTF_8)).isEqualTo(input);
    }

    @Test @DisplayName("Base64 known value")
    void base64KnownValue() {
        byte[] encoded = Base64EncoderDecoder.encode("hello".getBytes(StandardCharsets.UTF_8));
        assertThat(new String(encoded, StandardCharsets.UTF_8)).isEqualTo("aGVsbG8=");
    }

    @Test @DisplayName("Base64 produces non-empty output for non-empty input")
    void base64NonEmpty() {
        byte[] encoded = Base64EncoderDecoder.encode("hello".getBytes(StandardCharsets.UTF_8));
        assertThat(encoded).isNotEmpty();
        assertThat(new String(encoded, StandardCharsets.UTF_8)).isNotEqualTo("hello");
    }

    @Test @DisplayName("URL Base64 produces URL-safe output")
    void urlBase64NoSpecialChars() {
        byte[] data = new byte[256];
        for (int i = 0; i < 256; i++) data[i] = (byte) i;
        String encoded = new String(UrlBase64EncoderDecoder.encode(data), StandardCharsets.UTF_8);
        assertThat(encoded).doesNotContain("+", "/");
    }
}
