package gt.devtools.settings;

import gt.devtools.common.ValueProperty;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("ToolConfiguration")
class ToolConfigurationTest {

    private ToolConfiguration config;
    private UUID id;

    @BeforeEach
    void setUp() {
        id = UUID.randomUUID();
        config = new ToolConfiguration(id, "Test Tool");
    }

    @Test
    @DisplayName("should return the assigned id")
    void idIsPreserved() {
        assertThat(config.getId()).isEqualTo(id);
    }

    @Test
    @DisplayName("should return the assigned name")
    void nameIsPreserved() {
        assertThat(config.getName()).isEqualTo("Test Tool");
    }

    @Test
    @DisplayName("should allow changing the name")
    void setName() {
        config.setName("New Name");
        assertThat(config.getName()).isEqualTo("New Name");
    }

    @Test
    @DisplayName("should register a new property with default value")
    void registerNewProperty() {
        ValueProperty<Boolean> prop = config.register("enabled", true);

        assertThat(prop.get()).isTrue();
        assertThat(prop.getDefaultValue()).isTrue();
    }

    @Test
    @DisplayName("should return existing property on duplicate registration")
    void registerDuplicateReturnsExisting() {
        ValueProperty<String> first = config.register("key", "value1");
        ValueProperty<String> second = config.register("key", "value2");

        assertThat(second).isSameAs(first);
        assertThat(second.get()).isEqualTo("value1"); // original value preserved
    }

    @Test
    @DisplayName("should track properties in snapshot")
    void snapshotValues() {
        config.register("mode", "dark");
        config.register("count", 42);

        Map<String, Object> snapshot = config.snapshotValues();

        assertThat(snapshot).containsEntry("mode", "dark");
        assertThat(snapshot).containsEntry("count", 42);
    }

    @Test
    @DisplayName("should exclude SENSITIVE properties from snapshot")
    void snapshotExcludesSensitiveProperties() {
        config.register("theme", "dark", ToolConfiguration.PropertyType.CONFIGURATION);
        config.register("apiKey", "secret123", ToolConfiguration.PropertyType.SENSITIVE);

        Map<String, Object> snapshot = config.snapshotValues();

        assertThat(snapshot).containsKey("theme");
        assertThat(snapshot).doesNotContainKey("apiKey");
    }

    @Test
    @DisplayName("should include INPUT properties in snapshot")
    void snapshotIncludesInputProperties() {
        config.register("sourceText", "hello", ToolConfiguration.PropertyType.INPUT);

        Map<String, Object> snapshot = config.snapshotValues();

        assertThat(snapshot).containsEntry("sourceText", "hello");
    }

    @Test
    @DisplayName("should snapshot property types")
    void snapshotTypes() {
        config.register("mode", "dark", ToolConfiguration.PropertyType.CONFIGURATION);
        config.register("apiKey", "secret", ToolConfiguration.PropertyType.SENSITIVE);

        Map<String, String> types = config.snapshotTypes();

        assertThat(types).containsEntry("mode", "CONFIGURATION");
        assertThat(types).containsEntry("apiKey", "SENSITIVE");
    }

    @Test
    @DisplayName("should return immutable copy of properties")
    void getPropertiesReturnsImmutableCopy() {
        config.register("key", "value");

        var props = config.getProperties();

        assertThat(props).containsKey("key");
        assertThat(props.get("key").type()).isEqualTo(ToolConfiguration.PropertyType.CONFIGURATION);
    }

    @Test
    @DisplayName("should apply persisted values to registered properties")
    void applyPersistedValues() {
        config.register("mode", "dark");

        config.applyPersistedValues(Map.of("mode", "light"));

        assertThat(config.snapshotValues()).containsEntry("mode", "light");
    }

    @Test
    @DisplayName("should ignore unknown keys in persisted values")
    void applyPersistedValuesIgnoresUnknownKeys() {
        config.register("mode", "dark");

        config.applyPersistedValues(Map.of("unknownKey", "something"));

        // Should not throw; unknown keys are silently ignored
        assertThat(config.snapshotValues()).containsEntry("mode", "dark");
    }

    @Test
    @DisplayName("should coerce string to boolean")
    void coercionStringToBoolean() {
        config.register("flag", false);

        config.applyPersistedValues(Map.of("flag", "true"));

        assertThat(config.snapshotValues()).containsEntry("flag", true);
    }

    @Test
    @DisplayName("should coerce number to integer")
    void coercionNumberToInteger() {
        config.register("count", 0);

        config.applyPersistedValues(Map.of("count", 42.0));

        assertThat(config.snapshotValues()).containsEntry("count", 42);
    }

    @Test
    @DisplayName("should coerce number to long")
    void coercionNumberToLong() {
        config.register("timestamp", 0L);

        config.applyPersistedValues(Map.of("timestamp", 1.7e9));

        assertThat(config.snapshotValues()).containsEntry("timestamp", 1700000000L);
    }

    @Test
    @DisplayName("should coerce number to double")
    void coercionNumberToDouble() {
        config.register("ratio", 0.0);

        config.applyPersistedValues(Map.of("ratio", 3.14));

        assertThat(config.snapshotValues()).containsEntry("ratio", 3.14);
    }

    @Test
    @DisplayName("should coerce string to enum")
    void coercionStringToEnum() {
        config.register("type", ToolConfiguration.PropertyType.CONFIGURATION);

        config.applyPersistedValues(Map.of("type", "SENSITIVE"));

        assertThat(config.snapshotValues())
                .containsEntry("type", ToolConfiguration.PropertyType.SENSITIVE);
    }

    @Test
    @DisplayName("should handle null persisted value gracefully")
    void applyPersistedNullValue() {
        config.register("mode", "dark");

        // Map.of rejects null values, use HashMap instead
        var persisted = new java.util.HashMap<String, Object>();
        persisted.put("mode", null);
        config.applyPersistedValues(persisted);

        assertThat(config.snapshotValues()).containsEntry("mode", "dark"); // unchanged
    }

    @Test
    @DisplayName("should handle empty persisted map")
    void applyEmptyPersistedValues() {
        config.register("mode", "dark");

        config.applyPersistedValues(Map.of());

        assertThat(config.snapshotValues()).containsEntry("mode", "dark");
    }
}
