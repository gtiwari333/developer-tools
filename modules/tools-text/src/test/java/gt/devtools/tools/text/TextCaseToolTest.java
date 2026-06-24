package gt.devtools.tools.text;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("TextCaseTool static methods")
class TextCaseToolTest {

    // -- toTitleCase

    @Test
    @DisplayName("toTitleCase: capitalizes first letter of each word")
    void titleCaseBasic() {
        assertThat(TextCaseTool.toTitleCase("hello world")).isEqualTo("Hello World");
    }

    @Test
    @DisplayName("toTitleCase: lowercases subsequent letters")
    void titleCaseLowercasesRest() {
        assertThat(TextCaseTool.toTitleCase("hELLO wORLD")).isEqualTo("Hello World");
    }

    @Test
    @DisplayName("toTitleCase: single word")
    void titleCaseSingleWord() {
        assertThat(TextCaseTool.toTitleCase("hello")).isEqualTo("Hello");
    }

    @Test
    @DisplayName("toTitleCase: empty string")
    void titleCaseEmpty() {
        assertThat(TextCaseTool.toTitleCase("")).isEmpty();
    }

    // -- toCamelCase

    @Test
    @DisplayName("toCamelCase: basic conversion")
    void camelCaseBasic() {
        assertThat(TextCaseTool.toCamelCase("hello world")).isEqualTo("helloWorld");
    }

    @Test
    @DisplayName("toCamelCase: from snake_case")
    void camelCaseFromSnake() {
        assertThat(TextCaseTool.toCamelCase("hello_world_example")).isEqualTo("helloWorldExample");
    }

    @Test
    @DisplayName("toCamelCase: from kebab-case")
    void camelCaseFromKebab() {
        assertThat(TextCaseTool.toCamelCase("hello-world-example")).isEqualTo("helloWorldExample");
    }

    @Test
    @DisplayName("toCamelCase: single word stays lowercase")
    void camelCaseSingleWord() {
        assertThat(TextCaseTool.toCamelCase("HELLO")).isEqualTo("hello");
    }

    // -- toPascalCase

    @Test
    @DisplayName("toPascalCase: basic conversion")
    void pascalCaseBasic() {
        assertThat(TextCaseTool.toPascalCase("hello world")).isEqualTo("HelloWorld");
    }

    @Test
    @DisplayName("toPascalCase: from snake_case")
    void pascalCaseFromSnake() {
        assertThat(TextCaseTool.toPascalCase("hello_world")).isEqualTo("HelloWorld");
    }

    @Test
    @DisplayName("toPascalCase: single word")
    void pascalCaseSingleWord() {
        assertThat(TextCaseTool.toPascalCase("hello")).isEqualTo("Hello");
    }

    // -- toSnakeCase

    @Test
    @DisplayName("toSnakeCase: spaces to underscores")
    void snakeCaseSpaces() {
        assertThat(TextCaseTool.toSnakeCase("hello world")).isEqualTo("hello_world");
    }

    @Test
    @DisplayName("toSnakeCase: hyphens to underscores")
    void snakeCaseHyphens() {
        assertThat(TextCaseTool.toSnakeCase("hello-world")).isEqualTo("hello_world");
    }

    @Test
    @DisplayName("toSnakeCase: lowercases")
    void snakeCaseLowercases() {
        assertThat(TextCaseTool.toSnakeCase("Hello World")).isEqualTo("hello_world");
    }

    @Test
    @DisplayName("toSnakeCase: trims whitespace")
    void snakeCaseTrims() {
        assertThat(TextCaseTool.toSnakeCase("  hello world  ")).isEqualTo("hello_world");
    }

    // -- toKebabCase

    @Test
    @DisplayName("toKebabCase: spaces to hyphens")
    void kebabCaseSpaces() {
        assertThat(TextCaseTool.toKebabCase("hello world")).isEqualTo("hello-world");
    }

    @Test
    @DisplayName("toKebabCase: underscores to hyphens")
    void kebabCaseUnderscores() {
        assertThat(TextCaseTool.toKebabCase("hello_world")).isEqualTo("hello-world");
    }

    @Test
    @DisplayName("toKebabCase: lowercases")
    void kebabCaseLowercases() {
        assertThat(TextCaseTool.toKebabCase("Hello World")).isEqualTo("hello-world");
    }

    // -- toConstantCase

    @Test
    @DisplayName("toConstantCase: spaces to underscores, uppercase")
    void constantCaseSpaces() {
        assertThat(TextCaseTool.toConstantCase("hello world")).isEqualTo("HELLO_WORLD");
    }

    @Test
    @DisplayName("toConstantCase: hyphens to underscores, uppercase")
    void constantCaseHyphens() {
        assertThat(TextCaseTool.toConstantCase("hello-world")).isEqualTo("HELLO_WORLD");
    }

    // -- edge cases

    @ParameterizedTest
    @CsvSource({
        "hello world,      helloWorld,     HelloWorld,     hello_world,    hello-world,    HELLO_WORLD",
        "foo bar baz,      fooBarBaz,      FooBarBaz,      foo_bar_baz,    foo-bar-baz,    FOO_BAR_BAZ",
        "single,           single,         Single,         single,         single,         SINGLE",
        "UPPER CASE,       upperCase,      UpperCase,      upper_case,     upper-case,     UPPER_CASE",
    })
    @DisplayName("round-trip consistency across case formats")
    void caseConversionConsistency(String input, String camel, String pascal, String snake, String kebab, String constant) {
        assertThat(TextCaseTool.toCamelCase(input)).isEqualTo(camel);
        assertThat(TextCaseTool.toPascalCase(input)).isEqualTo(pascal);
        assertThat(TextCaseTool.toSnakeCase(input)).isEqualTo(snake);
        assertThat(TextCaseTool.toKebabCase(input)).isEqualTo(kebab);
        assertThat(TextCaseTool.toConstantCase(input)).isEqualTo(constant);
    }
}
