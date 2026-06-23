import gt.devtools.tools.api.ToolProvider;
import gt.devtools.tools.encoders.EncodersToolProvider;

module gt.devtools.tools.encoders {
    requires transitive gt.devtools.tools.api;
    requires org.apache.commons.codec;

    exports gt.devtools.tools.encoders;

    provides ToolProvider with EncodersToolProvider;
}
