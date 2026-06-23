import gt.devtools.tools.api.ToolProvider;
import gt.devtools.tools.formatters.FormattersToolProvider;

module gt.devtools.tools.formatters {
    requires transitive gt.devtools.tools.api;

    exports gt.devtools.tools.formatters;

    provides ToolProvider with FormattersToolProvider;
}
