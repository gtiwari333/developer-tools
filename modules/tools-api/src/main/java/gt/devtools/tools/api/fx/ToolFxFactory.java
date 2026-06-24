package gt.devtools.tools.api.fx;

import gt.devtools.settings.ToolConfiguration;
import gt.devtools.tools.api.ToolPresentation;

/**
 * Factory interface for JavaFX tools. Parallel to {@link gt.devtools.tools.api.ToolFactory}
 * but creates {@link DeveloperToolFx} instances instead of Swing tools.
 */
public interface ToolFxFactory<T extends DeveloperToolFx> {

    String getId();
    ToolPresentation getPresentation();
    T create(ToolConfiguration config);
}
