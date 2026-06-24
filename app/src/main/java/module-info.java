module gt.devtools.app {
    requires gt.devtools.common;
    requires gt.devtools.settings;
    requires gt.devtools.tools.api;
    requires gt.devtools.tools.encoders;
    requires gt.devtools.tools.escape;
    requires gt.devtools.tools.text;
    requires gt.devtools.tools.crypto;
    requires gt.devtools.tools.standalone;
    requires com.formdev.flatlaf;
    requires org.fife.RSyntaxTextArea;
    requires java.desktop;
    requires java.net.http;

    // JavaFX (Phase 0 — migration shell)
    requires transitive javafx.controls;
    requires transitive javafx.graphics;
    requires javafx.swing;

    exports gt.devtools.app;
}
