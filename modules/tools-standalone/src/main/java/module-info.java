import gt.devtools.tools.api.ToolProvider;
import gt.devtools.tools.standalone.StandaloneToolProvider;

module gt.devtools.tools.standalone {
    requires transitive gt.devtools.tools.api;

    exports gt.devtools.tools.standalone;

    provides ToolProvider with StandaloneToolProvider;
}
