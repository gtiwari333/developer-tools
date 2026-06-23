import gt.devtools.tools.api.ToolProvider;
import gt.devtools.tools.standalone.StandaloneToolProvider;

module gt.devtools.tools.standalone {
    requires transitive gt.devtools.tools.api;
    requires java.desktop;
    requires jdk.httpserver;
    requires tools.jackson.databind;
    requires tools.jackson.dataformat.yaml;
    requires tools.jackson.dataformat.xml;
    requires tools.jackson.dataformat.toml;
    requires json.path;
    requires com.cronutils;
    requires com.google.zxing;
    requires com.google.zxing.javase;
    requires org.apache.commons.compress;
    requires okhttp3;

    exports gt.devtools.tools.standalone;

    provides ToolProvider with StandaloneToolProvider;
}
