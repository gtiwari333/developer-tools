package gt.devtools.settings;

import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.SerializationFeature;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.UUID;

/**
 * Read/write application and tool configuration as human-readable JSON
 * files under {@code ~/.developer-tools/}.
 *
 * <h3>Directory layout</h3>
 * <pre>
 * ~/.developer-tools/
 *   settings.json           — {@link AppSettings}
 *   tools/
 *     &lt;uuid&gt;.json           — one file per {@link ToolConfiguration}
 * </pre>
 *
 * <h3>Thread safety</h3>
 * This class performs file I/O on the calling thread. It is safe to
 * call from any thread, but callers should be aware that
 * {@link #saveToolConfig} and {@link #saveAppSettings} block.
 */
public final class SettingsManager {

    /** Root config directory: {@code ~/.developer-tools/}. */
    private static final Path CONFIG_DIR = Path.of(
            System.getProperty("user.home"), ".developer-tools");
    private static final Path TOOLS_DIR = CONFIG_DIR.resolve("tools");
    private static final Path APP_SETTINGS_PATH = CONFIG_DIR.resolve("settings.json");

    private final ObjectMapper mapper;

    public SettingsManager() {
        this.mapper = JsonMapper.builder()
                .enable(SerializationFeature.INDENT_OUTPUT)
                .build();
        ensureDirectories();
    }

    // ---------------------------------------------------------------
    // App settings (global, not per-tool)
    // ---------------------------------------------------------------

    /**
     * Load persisted app settings, falling back to defaults if the file
     * does not exist or is corrupt.
     */
    public AppSettings loadAppSettings() {
        if (Files.exists(APP_SETTINGS_PATH)) {
            try {
                return mapper.readValue(APP_SETTINGS_PATH.toFile(), AppSettings.class);
            } catch (Exception e) {
                // Corrupt file → return defaults; next save will overwrite it
            }
        }
        return new AppSettings();
    }

    /** Persist global app settings to disk. */
    public void saveAppSettings(AppSettings settings) {
        try {
            mapper.writeValue(APP_SETTINGS_PATH.toFile(), settings);
        } catch (Exception e) {
            System.err.println("Failed to save app settings: " + e.getMessage());
        }
    }

    // ---------------------------------------------------------------
    // Tool configuration (per-workbench)
    // ---------------------------------------------------------------

    /**
     * Restore a tool configuration from disk, or return a fresh one
     * if no persisted state exists.
     *
     * @param id the stable identifier for this workbench
     * @return a configuration with previously-saved values applied
     */
    public ToolConfiguration loadToolConfig(UUID id) {
        Path file = toolConfigPath(id);
        if (Files.exists(file)) {
            try {
                @SuppressWarnings("unchecked")
                Map<String, Object> raw = mapper.readValue(file.toFile(), Map.class);
                String name = (String) raw.getOrDefault("name", "Workbench");
                var tc = new ToolConfiguration(id, name);
                @SuppressWarnings("unchecked")
                var props = (Map<String, Object>) raw.getOrDefault("properties", Map.of());
                tc.applyPersistedValues(props);
                return tc;
            } catch (Exception e) {
                // Corrupt → fresh start
            }
        }
        return new ToolConfiguration(id, "Workbench");
    }

    /** Persist a tool configuration to disk. */
    public void saveToolConfig(ToolConfiguration config) {
        try {
            var data = Map.of(
                    "id",         config.getId().toString(),
                    "name",       config.getName(),
                    "properties", config.snapshotValues(),
                    "types",      config.snapshotTypes()
            );
            mapper.writeValue(toolConfigPath(config.getId()).toFile(), data);
        } catch (Exception e) {
            System.err.println("Failed to save tool config: " + e.getMessage());
        }
    }

    /** Delete a tool configuration file from disk (called when a workbench is closed). */
    public void deleteToolConfig(UUID id) {
        try {
            Files.deleteIfExists(toolConfigPath(id));
        } catch (IOException ignored) {
        }
    }

    // ---------------------------------------------------------------
    // Internal helpers
    // ---------------------------------------------------------------

    private static void ensureDirectories() {
        try {
            Files.createDirectories(TOOLS_DIR);
        } catch (IOException ignored) {
            // Directory creation failure is non-fatal on startup
        }
    }

    private static Path toolConfigPath(UUID id) {
        return TOOLS_DIR.resolve(id.toString() + ".json");
    }

    /** Exposed so other components can reference the config directory. */
    public static Path getConfigDir() {
        return CONFIG_DIR;
    }
}
