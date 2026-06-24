package gt.devtools.tools.standalone;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("CronExpressionTool")
class CronExpressionToolTest {

    @Test @DisplayName("factory is accessible")
    void factoryAccessible() {
        var factory = new CronExpressionTool.Factory();
        assertThat(factory.getId()).isEqualTo("cron-expression");
        assertThat(factory.getPresentation().menuTitle()).isEqualTo("Cron Expression");
    }
}
