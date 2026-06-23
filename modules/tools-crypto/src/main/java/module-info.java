import gt.devtools.tools.api.ToolProvider;
import gt.devtools.tools.crypto.CryptoToolProvider;

module gt.devtools.tools.crypto {
    requires transitive gt.devtools.tools.api;

    exports gt.devtools.tools.crypto;

    provides ToolProvider with CryptoToolProvider;
}
