package gt.devtools.tools.escape;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/** Round-trip and smoke tests for all escaper/unescaper tools. */
@DisplayName("Escaper/Unescaper round-trip tests")
class EscaperUnescaperTests {

    @Test @DisplayName("HTML: escape then unescape returns original")
    void htmlRoundTrip() {
        String input = "<div class=\"test\">Hello & World</div>";
        String escaped = HtmlEntitiesEscaperUnescaper.escape(input);
        String unescaped = HtmlEntitiesEscaperUnescaper.unescape(escaped);
        assertThat(unescaped).isEqualTo(input);
    }

    @Test @DisplayName("HTML: escapes special characters")
    void htmlEscapes() {
        String result = HtmlEntitiesEscaperUnescaper.escape("<tag>");
        assertThat(result).contains("&lt;").contains("&gt;");
        assertThat(result).doesNotContain("<tag>");
    }

    @Test @DisplayName("Java: escape then unescape returns original")
    void javaRoundTrip() {
        String input = "hello\nworld\t\"test\"";
        String escaped = JavaStringEscaperUnescaper.escape(input);
        String unescaped = JavaStringEscaperUnescaper.unescape(escaped);
        assertThat(unescaped).isEqualTo(input);
    }

    @Test @DisplayName("Java: escapes special characters")
    void javaEscapes() {
        String result = JavaStringEscaperUnescaper.escape("a\nb");
        assertThat(result).contains("\\n");
    }

    @Test @DisplayName("JSON: escape then unescape returns original")
    void jsonRoundTrip() {
        String input = "hello \"world\"\nline2";
        String escaped = JsonTextEscaperUnescaper.escape(input);
        String unescaped = JsonTextEscaperUnescaper.unescape(escaped);
        assertThat(unescaped).isEqualTo(input);
    }

    @Test @DisplayName("JSON: escapes double quotes")
    void jsonEscapes() {
        String result = JsonTextEscaperUnescaper.escape("a\"b");
        assertThat(result).contains("\\\"");
    }

    @Test @DisplayName("XML: escape then unescape returns original")
    void xmlRoundTrip() {
        String input = "<root attr=\"val\">text</root>";
        String escaped = XmlTextEscaperUnescaper.escape(input);
        String unescaped = XmlTextEscaperUnescaper.unescape(escaped);
        assertThat(unescaped).isEqualTo(input);
    }

    @Test @DisplayName("XML: escapes special characters")
    void xmlEscapes() {
        String result = XmlTextEscaperUnescaper.escape("<tag>");
        assertThat(result).contains("&lt;").contains("&gt;");
    }

    @Test @DisplayName("CSV: escape then unescape returns original")
    void csvRoundTrip() {
        String input = "hello, \"world\"";
        String escaped = CsvTextEscaperUnescaper.escape(input);
        String unescaped = CsvTextEscaperUnescaper.unescape(escaped);
        assertThat(unescaped).isEqualTo(input);
    }

    @Test @DisplayName("CSV: normal text unchanged")
    void csvNormalText() {
        String result = CsvTextEscaperUnescaper.escape("simple");
        assertThat(result).isEqualTo("simple");
    }
}
