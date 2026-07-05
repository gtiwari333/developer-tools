import gt.devtools.tools.api.ToolProvider;
import gt.devtools.tools.text.TextToolProvider;

module gt.devtools.tools.text {
    requires transitive gt.devtools.tools.api;
    requires io.github.javadiffutils;
    requires javafx.controls;

    exports gt.devtools.tools.text;

    provides ToolProvider with TextToolProvider;
}