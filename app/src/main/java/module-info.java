module gt.devtools.app {
    requires gt.devtools.common;
    requires gt.devtools.settings;
    requires gt.devtools.tools.api;
    requires gt.devtools.tools.encoders;
    requires gt.devtools.tools.escape;
    requires gt.devtools.tools.text;
    requires gt.devtools.tools.crypto;
    requires gt.devtools.tools.standalone;
    requires java.net.http;

    // JavaFX
    requires transitive javafx.controls;
    requires transitive javafx.graphics;

    exports gt.devtools.app;
}