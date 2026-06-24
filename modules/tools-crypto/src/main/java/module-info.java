import gt.devtools.tools.api.ToolProvider;
import gt.devtools.tools.crypto.CryptoToolProvider;

module gt.devtools.tools.crypto {
    requires transitive gt.devtools.tools.api;
    requires java.desktop;
    requires com.fasterxml.uuid;
    requires com.github.f4b6a3.ulid;
    requires javafx.controls;

    exports gt.devtools.tools.crypto;

    provides ToolProvider with CryptoToolProvider;
}
