package gt.devtools.tools.formatters;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("CliCommandConverterToolFx static methods")
class CliCommandConverterToolTest {

    @Test @DisplayName("parseArgs: basic whitespace split")
    void parseArgsBasic() {
        List<String> args = CliCommandConverterToolFx.parseArgs("java -jar app.jar");
        assertThat(args).containsExactly("java", "-jar", "app.jar");
    }

    @Test @DisplayName("parseArgs: respects single quotes")
    void parseArgsSingleQuotes() {
        List<String> args = CliCommandConverterToolFx.parseArgs("echo 'hello world'");
        assertThat(args).containsExactly("echo", "'hello world'");
    }

    @Test @DisplayName("parseArgs: respects double quotes")
    void parseArgsDoubleQuotes() {
        List<String> args = CliCommandConverterToolFx.parseArgs("echo \"hello world\"");
        assertThat(args).containsExactly("echo", "\"hello world\"");
    }

    @Test @DisplayName("parseArgs: empty string")
    void parseArgsEmpty() {
        assertThat(CliCommandConverterToolFx.parseArgs("")).isEmpty();
    }

    @Test @DisplayName("joinCommand: removes line continuations")
    void joinCommandBasic() {
        String result = CliCommandConverterToolFx.joinCommand("java \\\n  -jar \\\n  app.jar");
        assertThat(result).isEqualTo("java -jar app.jar");
    }

    @Test @DisplayName("splitCommand: adds line continuations")
    void splitCommandBasic() {
        String result = CliCommandConverterToolFx.splitCommand("java -jar app.jar --verbose");
        assertThat(result).contains("\\\n");
        assertThat(result.lines().count()).isGreaterThan(1);
    }

    @Test @DisplayName("splitCommand: piped commands get proper indentation")
    void splitCommandWithPipe() {
        String result = CliCommandConverterToolFx.splitCommand("cat file.txt | grep pattern");
        assertThat(result).contains("|");
        assertThat(result).contains("\\\n");
    }
}
