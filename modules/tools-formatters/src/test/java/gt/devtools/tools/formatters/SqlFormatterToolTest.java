package gt.devtools.tools.formatters;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("SqlFormatterToolFx")
class SqlFormatterToolTest {

    @Test @DisplayName("factory is accessible")
    void factoryAccessible() {
        var factory = new SqlFormatterToolFx.Factory();
        assertThat(factory.getId()).isEqualTo("sql-formatting");
        assertThat(factory.getPresentation().menuTitle()).isEqualTo("SQL Formatter");
    }
}
