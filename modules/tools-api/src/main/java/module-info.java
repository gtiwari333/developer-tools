import gt.devtools.tools.api.ToolProvider;

module gt.devtools.tools.api {
    requires transitive gt.devtools.common;
    requires transitive gt.devtools.settings;
    requires org.slf4j;

    requires javafx.controls;
    requires javafx.graphics;
    requires org.fxmisc.richtext;
    requires org.fxmisc.flowless;

    exports gt.devtools.tools.api;
    exports gt.devtools.tools.api.fx;

    uses ToolProvider;
}