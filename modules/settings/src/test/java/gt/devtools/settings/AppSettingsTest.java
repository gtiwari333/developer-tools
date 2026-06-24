package gt.devtools.settings;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("AppSettings")
class AppSettingsTest {

    @Test @DisplayName("default theme is dark")
    void defaultTheme() {
        assertThat(new AppSettings().getTheme()).isEqualTo("dark");
    }

    @Test @DisplayName("default lastSelectedTool is base64-encoder-decoder")
    void defaultLastSelectedTool() {
        assertThat(new AppSettings().getLastSelectedTool()).isEqualTo("base64-encoder-decoder");
    }

    @Test @DisplayName("default checkForUpdates is true")
    void defaultCheckForUpdates() {
        assertThat(new AppSettings().isCheckForUpdates()).isTrue();
    }

    @Test @DisplayName("default showInternalTools is false")
    void defaultShowInternalTools() {
        assertThat(new AppSettings().isShowInternalTools()).isFalse();
    }

    @Test @DisplayName("default dividerLocation is 320")
    void defaultDividerLocation() {
        assertThat(new AppSettings().getDividerLocation()).isEqualTo(320);
    }

    @Test @DisplayName("default windowX/Y are -1 (center on first launch)")
    void defaultWindowPosition() {
        var settings = new AppSettings();
        assertThat(settings.getWindowX()).isEqualTo(-1);
        assertThat(settings.getWindowY()).isEqualTo(-1);
    }

    @Test @DisplayName("setters update values")
    void setters() {
        var settings = new AppSettings();
        settings.setTheme("light");
        settings.setCheckForUpdates(false);
        settings.setDividerLocation(400);
        settings.setLastSelectedTool("other-tool");

        assertThat(settings.getTheme()).isEqualTo("light");
        assertThat(settings.isCheckForUpdates()).isFalse();
        assertThat(settings.getDividerLocation()).isEqualTo(400);
        assertThat(settings.getLastSelectedTool()).isEqualTo("other-tool");
    }
}
