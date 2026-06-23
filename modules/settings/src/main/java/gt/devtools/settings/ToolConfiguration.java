package
gt.devtools.settings;

import gt.devtools.common.ValueProperty;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Per-workbench property bag. Each tool workbench (tab) gets its own
 * {@code ToolConfiguration} instance. Properties are persisted as JSON
 * via {@link SettingsManager}.
 * <p>
 * Mirrors the source plugin's {@code DeveloperToolConfiguration} but
 * uses a flat {@code Map<String, Object>} for JSON serialization instead
 * of IntelliJ's XML property system.
 */
public final class ToolConfiguration {

    public enum PropertyType {
        CONFIGURATION,
        INPUT,
        SENSITIVE
    }

    private final UUID id;
    private String name;
    private final Map<String, PropertyEntry<?>> properties = new LinkedHashMap<>();

    public ToolConfiguration(UUID id, String name) {
        this.id = id;
        this.name = name;
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * Register a property with the given key and default value.
     * If the property was previously persisted, the persisted value
     * is restored. Otherwise the default is used.
     */
    @SuppressWarnings("unchecked")
    public <T> ValueProperty<T> register(String key, T defaultValue) {
        return register(key, defaultValue, PropertyType.CONFIGURATION);
    }

    @SuppressWarnings("unchecked")
    public <T> ValueProperty<T> register(String key, T defaultValue, PropertyType type) {
        if (properties.containsKey(key)) {
            PropertyEntry<?> existing = properties.get(key);
            if (existing.property.getDefaultValue() != null
                    && existing.property.getDefaultValue().getClass().equals(defaultValue.getClass())) {
                return (ValueProperty<T>) existing.property;
            }
        }

        ValueProperty<T> property = new ValueProperty<>(defaultValue);
        properties.put(key, new PropertyEntry<>(property, type));
        return property;
    }

    /**
     * Apply persisted values to this configuration. Called by
     * {@link SettingsManager} after deserializing saved state.
     */
    @SuppressWarnings("unchecked")
    public void applyPersistedValues(Map<String, Object> persisted) {
        for (var entry : persisted.entrySet()) {
            PropertyEntry<?> propEntry = properties.get(entry.getKey());
            if (propEntry != null && entry.getValue() != null) {
                try {
                    // Coerce the value to the property's type
                    Object coerced = coerce(entry.getValue(),
                            propEntry.property.getDefaultValue());
                    if (coerced != null) {
                        ((ValueProperty<Object>) propEntry.property).set(coerced);
                    }
                } catch (Exception ignored) {
                    // Use default if coercion fails
                }
            }
        }
    }

    /**
     * Snapshot current property values for persistence.
     */
    public Map<String, Object> snapshotValues() {
        Map<String, Object> result = new LinkedHashMap<>();
        for (var entry : properties.entrySet()) {
            if (entry.getValue().type != PropertyType.SENSITIVE) {
                result.put(entry.getKey(), entry.getValue().property.get());
            }
        }
        return result;
    }

    /**
     * Snapshot property types for schema information.
     */
    public Map<String, String> snapshotTypes() {
        Map<String, String> result = new LinkedHashMap<>();
        for (var entry : properties.entrySet()) {
            result.put(entry.getKey(), entry.getValue().type.name());
        }
        return result;
    }

    public Map<String, PropertyEntry<?>> getProperties() {
        return Map.copyOf(properties);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static Object coerce(Object value, Object template) {
        if (template instanceof String) {
            return value.toString();
        }
        if (template instanceof Boolean && value instanceof String s) {
            return Boolean.parseBoolean(s);
        }
        if (template instanceof Integer && value instanceof Number n) {
            return n.intValue();
        }
        if (template instanceof Long && value instanceof Number n) {
            return n.longValue();
        }
        if (template instanceof Double && value instanceof Number n) {
            return n.doubleValue();
        }
        if (template instanceof Enum<?> e && value instanceof String s) {
            for (var constant : e.getDeclaringClass().getEnumConstants()) {
                if (((Enum) constant).name().equals(s)) {
                    return constant;
                }
            }
        }
        return value;
    }

    public record PropertyEntry<T>(ValueProperty<T> property, PropertyType type) {}
}
