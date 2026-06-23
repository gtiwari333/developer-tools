import gt.devtools.tools.api.ToolProvider;

module gt.devtools.tools.api {
    requires transitive gt.devtools.common;
    requires transitive gt.devtools.settings;
    requires com.formdev.flatlaf;
    requires org.fife.RSyntaxTextArea;
    requires java.desktop;
    requires java.datatransfer;
    requires org.slf4j;

    exports gt.devtools.tools.api;
    exports gt.devtools.tools.api.converter;
    exports gt.devtools.tools.api.generator;
    exports gt.devtools.tools.api.text;

    uses ToolProvider;
}
