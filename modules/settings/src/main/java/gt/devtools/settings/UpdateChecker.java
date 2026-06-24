package gt.devtools.settings;

import tools.jackson.databind.json.JsonMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.Map;

/**
 * Checks GitHub Releases for newer versions of the application.
 * <p>
 * Compares the running version (from {@code gradle.properties} baked in at
 * build time) against the latest release tag on GitHub.
 */
public final class UpdateChecker {

    private static final String GITHUB_API = "https://api.github.com/repos/%s/%s/releases/latest";
    private final String owner;
    private final String repo;
    private final String currentVersion;

    public UpdateChecker(String owner, String repo, String currentVersion) {
        this.owner = owner;
        this.repo = repo;
        this.currentVersion = currentVersion;
    }

    /** Query GitHub for the latest release. Returns null if already up-to-date. */
    @SuppressWarnings("unchecked")
    public UpdateInfo check() throws Exception {
        var client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
        var request = HttpRequest.newBuilder()
                .uri(URI.create(String.format(GITHUB_API, owner, repo)))
                .header("Accept", "application/vnd.github.v3+json")
                .header("User-Agent", "Developer-Tools-UpdateChecker")
                .timeout(Duration.ofSeconds(10))
                .GET()
                .build();

        var response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) return null;

        var mapper = new JsonMapper();
        Map<String, Object> json = (Map<String, Object>) mapper.readValue(
                response.body(), Map.class);
        String latestTag = (String) json.get("tag_name");
        String body = (String) json.get("body");

        if (isNewer(latestTag)) {
            List<Map<String, Object>> assets = (List<Map<String, Object>>) json.get("assets");
            return new UpdateInfo(latestTag, body, assets);
        }
        return null;
    }

    /**
     * Compares two semver version strings (e.g. "1.2.3" vs "1.2.4").
     * Handles 'v' prefix and pre-release suffixes.
     */
    public static boolean isNewer(String currentVersion, String latestTag) {
        if (latestTag == null) return false;
        // Strip 'v' prefix
        String latest = latestTag.startsWith("v") ? latestTag.substring(1) : latestTag;
        String current = currentVersion.contains("-")
                ? currentVersion.split("-")[0] : currentVersion;
        // Simple semver comparison: split by dots and compare numerically
        String[] latestParts = latest.split("\\.");
        String[] currentParts = current.split("\\.");
        int len = Math.max(latestParts.length, currentParts.length);
        for (int i = 0; i < len; i++) {
            int l = i < latestParts.length ? Integer.parseInt(latestParts[i].replaceAll("\\D", "")) : 0;
            int c = i < currentParts.length ? Integer.parseInt(currentParts[i].replaceAll("\\D", "")) : 0;
            if (l > c) return true;
            if (l < c) return false;
        }
        return false;
    }

    private boolean isNewer(String tag) {
        return isNewer(currentVersion, tag);
    }

    /** Metadata about an available update. */
    public record UpdateInfo(String version, String changelog, List<Map<String, Object>> assets) {
        /** Find the asset matching the current OS and architecture. */
        public String findDownloadUrl() {
            String os = System.getProperty("os.name").toLowerCase();
            String arch = System.getProperty("os.arch").toLowerCase();
            String match;
            if (os.contains("win")) match = "windows";
            else if (os.contains("mac")) match = "macos";
            else match = "linux";

            for (var asset : assets) {
                String name = ((String) asset.get("name")).toLowerCase();
                if (name.contains(match)) return (String) asset.get("browser_download_url");
            }
            // Fallback: return first asset
            if (!assets.isEmpty()) return (String) assets.get(0).get("browser_download_url");
            return null;
        }

        public long assetSize() {
            for (var asset : assets) {
                Object size = asset.get("size");
                if (size instanceof Number n) return n.longValue();
            }
            return 0;
        }
    }
}
