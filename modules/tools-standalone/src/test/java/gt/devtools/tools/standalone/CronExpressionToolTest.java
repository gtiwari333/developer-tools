package gt.devtools.tools.standalone;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("CronExpressionToolFx")
class CronExpressionToolTest {

    @Test @DisplayName("factory is accessible")
    void factoryAccessible() {
        var factory = new CronExpressionToolFx.Factory();
        assertThat(factory.getId()).isEqualTo("cron-expression");
        assertThat(factory.getPresentation().menuTitle()).isEqualTo("Cron Expression");
    }
}
