package gt.devtools.tools.crypto;

import gt.devtools.tools.api.ToolFactory;
import gt.devtools.tools.api.ToolProvider;

import java.util.List;

public final class CryptoToolProvider implements ToolProvider {
    public CryptoToolProvider() {}
    @Override public String getName() { return "Cryptography"; }
    @Override public List<ToolFactory<?>> getTools() { return List.of(); }
}
