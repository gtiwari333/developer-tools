package gt.devtools.tools.standalone;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("JsonPathTool")
class JsonPathToolTest {

    @Test @DisplayName("factory is accessible")
    void factoryAccessible() {
        var factory = new JsonPathTool.Factory();
        assertThat(factory.getId()).isEqualTo("json-path");
        assertThat(factory.getPresentation().menuTitle()).isEqualTo("JSON Path");
    }
}
