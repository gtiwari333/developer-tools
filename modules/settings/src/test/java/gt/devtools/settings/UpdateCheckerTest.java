package gt.devtools.settings;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("UpdateChecker.isNewer")
class UpdateCheckerTest {

    @ParameterizedTest
    @CsvSource({
        "1.0.0, 2.0.0,   true",
        "2.0.0, 1.0.0,   false",
        "1.0.0, 1.0.0,   false",
        "1.0.0, 1.0.1,   true",
        "1.0.1, 1.0.0,   false",
        "1.9.0, 1.10.0,  true",
        "1.0.0, v2.0.0,  true",
        "2.0.0, v1.0.0,  false",
        "1.0.0-beta, 1.0.0, false",   // pre-release suffix stripped, 1.0.0 == 1.0.0
    })
    @DisplayName("semver comparison")
    void semverComparison(String current, String latest, boolean expected) {
        assertThat(UpdateChecker.isNewer(current, latest)).isEqualTo(expected);
    }

    @Test @DisplayName("null tag returns false")
    void nullTag() {
        assertThat(UpdateChecker.isNewer("1.0.0", null)).isFalse();
    }

    @Test @DisplayName("same version with v prefix")
    void vPrefix() {
        assertThat(UpdateChecker.isNewer("1.0.0", "v1.0.0")).isFalse();
    }

    @Test @DisplayName("three-part vs two-part version")
    void differentLengths() {
        assertThat(UpdateChecker.isNewer("1.0", "1.0.1")).isTrue();
        assertThat(UpdateChecker.isNewer("1.0.1", "1.0")).isFalse();
    }
}
