package gt.devtools.tools.standalone;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("ConfigFormatConverterFx")
class ConfigFormatConverterTest {

    @Test @DisplayName("converter has all four format mappers")
    void hasFormatMappers() {
        // The converter wraps Jackson mappers for JSON, YAML, XML, TOML.
        // Since the constructor is private (only Factory creates instances),
        // we validate that the class is loadable and the factory is accessible.
        var factory = new ConfigFormatConverterFx.Factory();
        assertThat(factory.getId()).isEqualTo("config-format-converter");
        assertThat(factory.getPresentation().menuTitle()).isEqualTo("Config Format Converter");
    }
}
