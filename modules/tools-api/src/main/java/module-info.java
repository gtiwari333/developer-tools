import gt.devtools.tools.api.ToolProvider;

module gt.devtools.tools.api {
    requires transitive gt.devtools.common;
    requires transitive gt.devtools.settings;
    requires com.formdev.flatlaf;
    requires org.fife.RSyntaxTextArea;
    requires java.desktop;
    requires java.datatransfer;
    requires org.slf4j;

    requires javafx.controls;
    requires javafx.graphics;

    exports gt.devtools.tools.api;
    exports gt.devtools.tools.api.converter;
    exports gt.devtools.tools.api.generator;
    exports gt.devtools.tools.api.text;
    exports gt.devtools.tools.api.fx;

    uses ToolProvider;
}
