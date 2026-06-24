package gt.devtools.tools.crypto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("UuidGeneratorTool static methods")
class UuidGeneratorToolTest {

    @Test @DisplayName("generateV4 produces valid UUID format")
    void v4Format() {
        String uuid = UuidGeneratorTool.generateV4();
        assertThat(uuid).matches("[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}");
    }

    @Test @DisplayName("generateV4 produces unique values")
    void v4Unique() {
        var seen = new java.util.HashSet<String>();
        for (int i = 0; i < 100; i++) {
            assertThat(seen.add(UuidGeneratorTool.generateV4())).isTrue();
        }
    }

    @Test @DisplayName("generateV3 produces valid UUID format")
    void v3Format() {
        String uuid = UuidGeneratorTool.generateV3("namespace", "name");
        assertThat(uuid).matches("[0-9a-f]{8}-[0-9a-f]{4}-3[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}");
    }

    @Test @DisplayName("generateV3 is deterministic for same inputs")
    void v3Deterministic() {
        String a = UuidGeneratorTool.generateV3("ns", "name");
        String b = UuidGeneratorTool.generateV3("ns", "name");
        assertThat(a).isEqualTo(b);
    }

    @Test @DisplayName("generateV3 different namespaces produce different UUIDs")
    void v3DifferentNamespace() {
        String a = UuidGeneratorTool.generateV3("ns-a", "name");
        String b = UuidGeneratorTool.generateV3("ns-b", "name");
        assertThat(a).isNotEqualTo(b);
    }

    @Test @DisplayName("generateV1 produces valid UUID format")
    void v1Format() {
        String uuid = UuidGeneratorTool.generateV1();
        assertThat(uuid).matches("[0-9a-f]{8}-[0-9a-f]{4}-1[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}");
    }

    @Test @DisplayName("generateV6 produces valid UUID format")
    void v6Format() {
        String uuid = UuidGeneratorTool.generateV6();
        assertThat(uuid).matches("[0-9a-f]{8}-[0-9a-f]{4}-6[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}");
    }

    @Test @DisplayName("generateV7 produces valid UUID format")
    void v7Format() {
        String uuid = UuidGeneratorTool.generateV7();
        assertThat(uuid).matches("[0-9a-f]{8}-[0-9a-f]{4}-7[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}");
    }
}
