package gt.devtools.settings;

/**
 * Application-level preferences that are global (not per-tool).
 * Serialized as {@code ~/.developer-tools/settings.json}.
 *
 * <p>Jackson maps this object directly — field names become JSON keys,
 * so keep getter/setter names consistent with the expected JSON shape.
 */
public final class AppSettings {

    public AppSettings() {}

    // -- Window geometry
    private int windowWidth = 1200;
    private int windowHeight = 800;
    private int windowX = -1;   // -1 → centre on first launch
    private int windowY = -1;
    private int dividerLocation = 280;  // sidebar width in pixels

    // -- Appearance
    private String theme = "dark";       // "dark" | "light" | "" (auto-detect)

    // -- Session
    private String lastSelectedTool = "base64-encoder-decoder";
    private boolean checkForUpdates = true;
    private boolean showInternalTools = false;

    // ---------------------------------------------------------------
    // Accessors
    // ---------------------------------------------------------------

    public int getWindowWidth() { return windowWidth; }
    public void setWindowWidth(int v) { this.windowWidth = v; }

    public int getWindowHeight() { return windowHeight; }
    public void setWindowHeight(int v) { this.windowHeight = v; }

    public int getWindowX() { return windowX; }
    public void setWindowX(int v) { this.windowX = v; }

    public int getWindowY() { return windowY; }
    public void setWindowY(int v) { this.windowY = v; }

    public int getDividerLocation() { return dividerLocation; }
    public void setDividerLocation(int v) { this.dividerLocation = v; }

    /** "dark", "light", or "" (empty = auto-detect from OS). */
    public String getTheme() { return theme; }
    public void setTheme(String v) { this.theme = v; }

    /** The tool id that was selected when the app last closed. */
    public String getLastSelectedTool() { return lastSelectedTool; }
    public void setLastSelectedTool(String v) { this.lastSelectedTool = v; }

    /** Whether to ping GitHub for new releases on startup. */
    public boolean isCheckForUpdates() { return checkForUpdates; }
    public void setCheckForUpdates(boolean v) { this.checkForUpdates = v; }

    /** Whether developer-oriented / internal tools are visible in the sidebar. */
    public boolean isShowInternalTools() { return showInternalTools; }
    public void setShowInternalTools(boolean v) { this.showInternalTools = v; }
}
