package gt.devtools.tools.crypto;

import gt.devtools.tools.api.ToolFactory;
import gt.devtools.tools.api.ToolProvider;

import java.util.List;

/** Registers all generator tools. */
public final class CryptoToolProvider implements ToolProvider {
    public CryptoToolProvider() {}

    @Override public String getName() { return "Cryptography"; }

    @Override
    public List<ToolFactory<?>> getTools() {
        return List.of(
                new UuidGeneratorTool.Factory(),
                new NanoIdGeneratorTool.Factory(),
                new UlidGeneratorTool.Factory(),
                new PasswordGeneratorTool.Factory(),
                new LoremIpsumGeneratorTool.Factory()
        );
    }
}
