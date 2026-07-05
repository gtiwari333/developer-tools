import gt.devtools.tools.api.ToolProvider;
import gt.devtools.tools.encoders.EncodersToolProvider;

module gt.devtools.tools.encoders {
    requires transitive gt.devtools.tools.api;
    requires javafx.controls;
    requires org.apache.commons.codec;
    requires org.jose4j;

    exports gt.devtools.tools.encoders;

    provides ToolProvider with EncodersToolProvider;
}