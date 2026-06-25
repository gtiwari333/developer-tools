package gt.devtools.tools.encoders;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("AsciiEncoderDecoderFx static methods")
class AsciiEncoderDecoderTest {

    // -- encodeHex

    @Test
    @DisplayName("encodeHex: basic encoding")
    void encodeBasic() {
        assertThat(AsciiEncoderDecoderFx.encodeHex("abc".getBytes(StandardCharsets.UTF_8)))
                .isEqualTo("616263");
    }

    @Test
    @DisplayName("encodeHex: empty input")
    void encodeEmpty() {
        assertThat(AsciiEncoderDecoderFx.encodeHex(new byte[0])).isEmpty();
    }

    @Test
    @DisplayName("encodeHex: single byte")
    void encodeSingleByte() {
        assertThat(AsciiEncoderDecoderFx.encodeHex(new byte[]{(byte) 0xff}))
                .isEqualTo("ff");
    }

    @Test
    @DisplayName("encodeHex: zero byte")
    void encodeZeroByte() {
        assertThat(AsciiEncoderDecoderFx.encodeHex(new byte[]{0x00}))
                .isEqualTo("00");
    }

    @Test
    @DisplayName("encodeHex: all possible byte values round-trip")
    void encodeAllBytesRoundTrip() {
        byte[] allBytes = new byte[256];
        for (int i = 0; i < 256; i++) allBytes[i] = (byte) i;

        String hex = AsciiEncoderDecoderFx.encodeHex(allBytes);
        byte[] decoded = AsciiEncoderDecoderFx.decodeHex(hex);

        assertThat(decoded).isEqualTo(allBytes);
    }

    // -- decodeHex

    @Test
    @DisplayName("decodeHex: basic decoding")
    void decodeBasic() {
        byte[] result = AsciiEncoderDecoderFx.decodeHex("616263");
        assertThat(new String(result, StandardCharsets.UTF_8)).isEqualTo("abc");
    }

    @Test
    @DisplayName("decodeHex: empty string")
    void decodeEmpty() {
        assertThat(AsciiEncoderDecoderFx.decodeHex("")).isEmpty();
    }

    @Test
    @DisplayName("decodeHex: strips whitespace")
    void decodeStripsWhitespace() {
        byte[] result = AsciiEncoderDecoderFx.decodeHex("61 62 63");
        assertThat(new String(result, StandardCharsets.UTF_8)).isEqualTo("abc");
    }

    @Test
    @DisplayName("decodeHex: strips newlines and tabs")
    void decodeStripsNewlines() {
        byte[] result = AsciiEncoderDecoderFx.decodeHex("61\n62\t63");
        assertThat(new String(result, StandardCharsets.UTF_8)).isEqualTo("abc");
    }

    @Test
    @DisplayName("decodeHex: odd length throws IllegalArgumentException")
    void decodeOddLengthThrows() {
        assertThatThrownBy(() -> AsciiEncoderDecoderFx.decodeHex("616"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Odd hex length");
    }

    @Test
    @DisplayName("decodeHex: uppercase hex")
    void decodeUppercaseHex() {
        byte[] result = AsciiEncoderDecoderFx.decodeHex("616263");
        assertThat(new String(result, StandardCharsets.UTF_8)).isEqualTo("abc");
    }

    // -- round-trip

    @ParameterizedTest
    @CsvSource({
        "hello",
        "Hello, World!",
        "12345",
        "special chars: !@#$%",
    })
    @DisplayName("round-trip: encode then decode returns original")
    void roundTrip(String original) {
        byte[] input = original.getBytes(StandardCharsets.UTF_8);
        String hex = AsciiEncoderDecoderFx.encodeHex(input);
        byte[] decoded = AsciiEncoderDecoderFx.decodeHex(hex);
        assertThat(new String(decoded, StandardCharsets.UTF_8)).isEqualTo(original);
    }
}
