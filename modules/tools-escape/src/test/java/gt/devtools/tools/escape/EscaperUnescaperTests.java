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
        String escaped = HtmlEntitiesEscaperUnescaperFx.escape(input);
        String unescaped = HtmlEntitiesEscaperUnescaperFx.unescape(escaped);
        assertThat(unescaped).isEqualTo(input);
    }

    @Test @DisplayName("HTML: escapes special characters")
    void htmlEscapes() {
        String result = HtmlEntitiesEscaperUnescaperFx.escape("<tag>");
        assertThat(result).contains("&lt;").contains("&gt;");
        assertThat(result).doesNotContain("<tag>");
    }

    @Test @DisplayName("Java: escape then unescape returns original")
    void javaRoundTrip() {
        String input = "hello\nworld\t\"test\"";
        String escaped = JavaStringEscaperUnescaperFx.escape(input);
        String unescaped = JavaStringEscaperUnescaperFx.unescape(escaped);
        assertThat(unescaped).isEqualTo(input);
    }

    @Test @DisplayName("Java: escapes special characters")
    void javaEscapes() {
        String result = JavaStringEscaperUnescaperFx.escape("a\nb");
        assertThat(result).contains("\\n");
    }

    @Test @DisplayName("JSON: escape then unescape returns original")
    void jsonRoundTrip() {
        String input = "hello \"world\"\nline2";
        String escaped = JsonTextEscaperUnescaperFx.escape(input);
        String unescaped = JsonTextEscaperUnescaperFx.unescape(escaped);
        assertThat(unescaped).isEqualTo(input);
    }

    @Test @DisplayName("JSON: escapes double quotes")
    void jsonEscapes() {
        String result = JsonTextEscaperUnescaperFx.escape("a\"b");
        assertThat(result).contains("\\\"");
    }

    @Test @DisplayName("XML: escape then unescape returns original")
    void xmlRoundTrip() {
        String input = "<root attr=\"val\">text</root>";
        String escaped = XmlTextEscaperUnescaperFx.escape(input);
        String unescaped = XmlTextEscaperUnescaperFx.unescape(escaped);
        assertThat(unescaped).isEqualTo(input);
    }

    @Test @DisplayName("XML: escapes special characters")
    void xmlEscapes() {
        String result = XmlTextEscaperUnescaperFx.escape("<tag>");
        assertThat(result).contains("&lt;").contains("&gt;");
    }

    @Test @DisplayName("CSV: escape then unescape returns original")
    void csvRoundTrip() {
        String input = "hello, \"world\"";
        String escaped = CsvTextEscaperUnescaperFx.escape(input);
        String unescaped = CsvTextEscaperUnescaperFx.unescape(escaped);
        assertThat(unescaped).isEqualTo(input);
    }

    @Test @DisplayName("CSV: normal text unchanged")
    void csvNormalText() {
        String result = CsvTextEscaperUnescaperFx.escape("simple");
        assertThat(result).isEqualTo("simple");
    }
}
