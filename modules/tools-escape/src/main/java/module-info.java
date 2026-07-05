import gt.devtools.tools.api.ToolProvider;
import gt.devtools.tools.escape.EscapeToolProvider;

module gt.devtools.tools.escape {
    requires transitive gt.devtools.tools.api;
    requires javafx.controls;
    requires org.apache.commons.text;

    exports gt.devtools.tools.escape;

    provides ToolProvider with EscapeToolProvider;
}
