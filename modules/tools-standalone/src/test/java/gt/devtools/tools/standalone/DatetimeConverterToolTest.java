package gt.devtools.tools.standalone;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("DatetimeConverterToolFx static methods")
class DatetimeConverterToolTest {

    @Test @DisplayName("parseTimestamp: auto-detects Unix milliseconds")
    void parseUnixMillis() {
        Instant result = DatetimeConverterToolFx.parseTimestamp("1700000000000", "auto");
        assertThat(result.toEpochMilli()).isEqualTo(1700000000000L);
    }

    @Test @DisplayName("parseTimestamp: auto-detects Unix seconds")
    void parseUnixSeconds() {
        Instant result = DatetimeConverterToolFx.parseTimestamp("1700000000", "auto");
        assertThat(result.getEpochSecond()).isEqualTo(1700000000L);
    }

    @Test @DisplayName("parseTimestamp: explicit Unix seconds mode")
    void parseExplicitSeconds() {
        Instant result = DatetimeConverterToolFx.parseTimestamp("1700000000", "unix-seconds");
        assertThat(result.getEpochSecond()).isEqualTo(1700000000L);
    }

    @Test @DisplayName("parseTimestamp: explicit Unix milliseconds mode")
    void parseExplicitMillis() {
        Instant result = DatetimeConverterToolFx.parseTimestamp("1700000000000", "unix-millis");
        assertThat(result.toEpochMilli()).isEqualTo(1700000000000L);
    }

    @Test @DisplayName("parseTimestamp: parses ISO-8601")
    void parseIso8601() {
        Instant result = DatetimeConverterToolFx.parseTimestamp("2024-01-15T10:30:00Z", "auto");
        assertThat(result).isNotNull();
        assertThat(result.toString()).contains("2024-01-15");
    }

    @Test @DisplayName("formatInstant: ISO-8601 default")
    void formatIso8601() {
        Instant instant = Instant.parse("2024-01-15T10:30:00Z");
        String result = DatetimeConverterToolFx.formatInstant(instant, "ISO-8601");
        assertThat(result).contains("2024-01-15");
    }

    @Test @DisplayName("formatInstant: Unix milliseconds")
    void formatUnixMillis() {
        Instant instant = Instant.ofEpochMilli(1700000000000L);
        String result = DatetimeConverterToolFx.formatInstant(instant, "Unix milliseconds");
        assertThat(result).isEqualTo("1700000000000");
    }

    @Test @DisplayName("formatInstant: Unix seconds")
    void formatUnixSeconds() {
        Instant instant = Instant.ofEpochSecond(1700000000L);
        String result = DatetimeConverterToolFx.formatInstant(instant, "Unix seconds");
        assertThat(result).isEqualTo("1700000000");
    }

    @Test @DisplayName("formatInstant: RFC-1123")
    void formatRfc1123() {
        Instant instant = Instant.parse("2024-01-15T10:30:00Z");
        String result = DatetimeConverterToolFx.formatInstant(instant, "RFC-1123");
        assertThat(result).contains("2024");
    }

    @Test @DisplayName("round-trip: parse then format")
    void parseThenFormat() {
        Instant parsed = DatetimeConverterToolFx.parseTimestamp("1700000000000", "auto");
        String formatted = DatetimeConverterToolFx.formatInstant(parsed, "Unix milliseconds");
        assertThat(formatted).isEqualTo("1700000000000");
    }
}
