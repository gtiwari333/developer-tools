package gt.devtools.settings;

import gt.devtools.common.ValueProperty;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Per-workbench property bag that survives application restarts.
 *
 * <h3>Concept</h3>
 * Each workbench tab gets exactly one {@code ToolConfiguration}. Tools
 * register their settings via {@link #register(String, Object)} and
 * receive a {@link ValueProperty} they can read, write, and listen to.
 * The configuration is persisted as JSON by {@link SettingsManager}.
 *
 * <h3>Property types</h3>
 * <ul>
 *   <li>{@link PropertyType#CONFIGURATION} — tool behaviour (e.g. "live mode on/off").
 *       Always persisted.</li>
 *   <li>{@link PropertyType#INPUT} — user content (e.g. the text in an editor pane).
 *       Persisted so work is not lost between sessions.</li>
 *   <li>{@link PropertyType#SENSITIVE} — secrets or transient state (e.g. an API key).
 *       Never persisted.</li>
 * </ul>
 *
 * <h3>Lifecycle</h3>
 * <ol>
 *   <li>The tool's {@code Factory.create(config)} is called with a fresh
 *       or restored configuration.</li>
 *   <li>The tool calls {@code config.register(...)} for each setting it
 *       needs during {@code buildUi()}.</li>
 *   <li>On shutdown, {@link SettingsManager#saveToolConfig(ToolConfiguration)}
 *       snapshots non-sensitive properties to disk.</li>
 * </ol>
 */
public final class ToolConfiguration {

    /** Controls whether and how a property is persisted. */
    public enum PropertyType {
        /** Tool behaviour — always persisted. */
        CONFIGURATION,
        /** User-entered content — persisted so work survives restarts. */
        INPUT,
        /** Secrets or transient state — never written to disk. */
        SENSITIVE
    }

    private final UUID id;
    private String name;
    private final Map<String, PropertyEntry<?>> properties = new LinkedHashMap<>();

    /**
     * Create a new configuration with the given identity.
     *
     * @param id   stable UUID used as the filename on disk
     * @param name human-readable label shown in the workbench tab
     */
    public ToolConfiguration(UUID id, String name) {
        this.id = id;
        this.name = name;
    }

    public UUID getId() { return id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    /**
     * Register a {@link PropertyType#CONFIGURATION} property.
     * <p>
     * If a property with this key was already registered (e.g. because the
     * persisted JSON contained it), the existing {@code ValueProperty} is
     * returned and {@code defaultValue} is ignored.
     *
     * @param key          unique key within this configuration (used as JSON field name)
     * @param defaultValue fallback value when nothing is persisted
     * @param <T>          the value type — must be JSON-serializable (String, Boolean,
     *                     Number, enum, or a simple JavaBean)
     * @return the property handle — store it and call get()/set()/addListener()
     */
    @SuppressWarnings("unchecked")
    public <T> ValueProperty<T> register(String key, T defaultValue) {
        return register(key, defaultValue, PropertyType.CONFIGURATION);
    }

    /**
     * Register a property with an explicit {@link PropertyType}.
     *
     * @see #register(String, Object)
     */
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
     * Called by {@link SettingsManager} to restore values from a previously
     * persisted JSON file. Keys that were never registered are ignored;
     * values that cannot be coerced to the expected type are silently skipped.
     */
    @SuppressWarnings("unchecked")
    public void applyPersistedValues(Map<String, Object> persisted) {
        for (var entry : persisted.entrySet()) {
            PropertyEntry<?> propEntry = properties.get(entry.getKey());
            if (propEntry != null && entry.getValue() != null) {
                try {
                    Object coerced = coerce(entry.getValue(),
                            propEntry.property.getDefaultValue());
                    if (coerced != null) {
                        ((ValueProperty<Object>) propEntry.property).set(coerced);
                    }
                } catch (Exception ignored) {
                    // Use default if coercion fails — never crash on corrupt data
                }
            }
        }
    }

    /**
     * Snapshot current property values for persistence.
     * {@link PropertyType#SENSITIVE} properties are excluded.
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
     * Snapshot the property-type metadata so the file can be inspected
     * by hand and types can be recovered on reload.
     */
    public Map<String, String> snapshotTypes() {
        Map<String, String> result = new LinkedHashMap<>();
        for (var entry : properties.entrySet()) {
            result.put(entry.getKey(), entry.getValue().type.name());
        }
        return result;
    }

    /** Returns an immutable snapshot of all registered properties. */
    public Map<String, PropertyEntry<?>> getProperties() {
        return Map.copyOf(properties);
    }

    /**
     * Best-effort type coercion so that JSON primitives (numbers, strings,
     * booleans) survive a round-trip through Jackson's {@code Map.class}
     * deserialization without losing their Java type.
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    private static Object coerce(Object value, Object template) {
        if (template instanceof String)                return value.toString();
        if (template instanceof Boolean && value instanceof String s) return Boolean.parseBoolean(s);
        if (template instanceof Integer && value instanceof Number n)  return n.intValue();
        if (template instanceof Long    && value instanceof Number n)  return n.longValue();
        if (template instanceof Double  && value instanceof Number n)  return n.doubleValue();
        if (template instanceof Enum<?> e && value instanceof String s) {
            for (var constant : e.getDeclaringClass().getEnumConstants()) {
                if (((Enum) constant).name().equals(s)) return constant;
            }
        }
        return value;
    }

    /** A property together with its persistence classification. */
    public record PropertyEntry<T>(ValueProperty<T> property, PropertyType type) {}
}
