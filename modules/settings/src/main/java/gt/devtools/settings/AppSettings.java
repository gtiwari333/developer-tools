package gt.devtools.settings;

import java.nio.file.Path;

/**
 * Application-level settings that persist across sessions.
 */
public final class AppSettings {

    public AppSettings() {}

    private int windowWidth = 1200;
    private int windowHeight = 800;
    private int windowX = -1;
    private int windowY = -1;
    private int dividerLocation = 280;
    private String theme = "dark";
    private String lastSelectedTool = "base64-encoder-decoder";
    private boolean checkForUpdates = true;
    private boolean showInternalTools = false;

    // -- accessors

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

    public String getTheme() { return theme; }
    public void setTheme(String v) { this.theme = v; }

    public String getLastSelectedTool() { return lastSelectedTool; }
    public void setLastSelectedTool(String v) { this.lastSelectedTool = v; }

    public boolean isCheckForUpdates() { return checkForUpdates; }
    public void setCheckForUpdates(boolean v) { this.checkForUpdates = v; }

    public boolean isShowInternalTools() { return showInternalTools; }
    public void setShowInternalTools(boolean v) { this.showInternalTools = v; }
}
