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
 * Manages persistence of application and tool configuration as JSON files
 * in {@code ~/.developer-tools/}.
 */
public final class SettingsManager {

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

    private static void ensureDirectories() {
        try {
            Files.createDirectories(TOOLS_DIR);
        } catch (IOException ignored) {
            // non-fatal
        }
    }

    // -- App settings

    public AppSettings loadAppSettings() {
        if (Files.exists(APP_SETTINGS_PATH)) {
            try {
                return mapper.readValue(APP_SETTINGS_PATH.toFile(), AppSettings.class);
            } catch (Exception e) {
                // fall through to defaults
            }
        }
        return new AppSettings();
    }

    public void saveAppSettings(AppSettings settings) {
        try {
            mapper.writeValue(APP_SETTINGS_PATH.toFile(), settings);
        } catch (Exception e) {
            System.err.println("Failed to save app settings: " + e.getMessage());
        }
    }

    // -- Tool configurations

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
                // fall through
            }
        }
        return new ToolConfiguration(id, "Workbench");
    }

    public void saveToolConfig(ToolConfiguration config) {
        try {
            var data = Map.of(
                    "id", config.getId().toString(),
                    "name", config.getName(),
                    "properties", config.snapshotValues(),
                    "types", config.snapshotTypes()
            );
            mapper.writeValue(toolConfigPath(config.getId()).toFile(), data);
        } catch (Exception e) {
            System.err.println("Failed to save tool config: " + e.getMessage());
        }
    }

    public void deleteToolConfig(UUID id) {
        try {
            Files.deleteIfExists(toolConfigPath(id));
        } catch (IOException ignored) {
        }
    }

    private static Path toolConfigPath(UUID id) {
        return TOOLS_DIR.resolve(id.toString() + ".json");
    }

    public static Path getConfigDir() {
        return CONFIG_DIR;
    }
}
