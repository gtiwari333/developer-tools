package gt.devtools.tools.crypto;

import gt.devtools.tools.api.ToolProvider;
import gt.devtools.tools.api.fx.ToolFxFactory;

import java.util.List;

/** Registers all generator tools. */
public final class CryptoToolProvider implements ToolProvider {
    public CryptoToolProvider() {}

    @Override public String getName() { return "Cryptography"; }

    @Override
    public List<ToolFxFactory<?>> getTools() {
        return List.of(
                new UuidGeneratorToolFx.Factory(),
                new NanoIdGeneratorToolFx.Factory(),
                new UlidGeneratorToolFx.Factory(),
                new PasswordGeneratorToolFx.Factory(),
                new LoremIpsumGeneratorToolFx.Factory()
        );
    }
}
