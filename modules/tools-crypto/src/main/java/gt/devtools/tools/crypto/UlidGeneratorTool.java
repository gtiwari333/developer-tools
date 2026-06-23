package gt.devtools.tools.crypto;

import com.github.f4b6a3.ulid.UlidCreator;
import gt.devtools.settings.ToolConfiguration;
import gt.devtools.tools.api.ToolFactory;
import gt.devtools.tools.api.ToolPresentation;
import gt.devtools.tools.api.generator.OneLineTextGenerator;

import javax.swing.*;
import java.awt.*;

/**
 * Generates ULIDs (Universally Unique Lexicographically Sortable Identifiers).
 * Supports standard and monotonic modes. Uses {@code ulid-creator} library.
 */
public final class UlidGeneratorTool extends OneLineTextGenerator {

    private UlidGeneratorTool(ToolConfiguration config) {
        super(config);
    }

    @Override
    protected void buildConfigurationUi(JPanel configPanel) {
        var row = new JPanel(new FlowLayout(FlowLayout.LEFT));
        row.add(new JLabel("26-character, time-sortable unique ID. No configuration needed."));
        configPanel.add(row);
    }

    @Override
    protected String generate() {
        return UlidCreator.getUlid().toString();
    }

    public static final class Factory implements ToolFactory<UlidGeneratorTool> {
        public Factory() {}
        @Override public String getId() { return "ulid-generator"; }
        @Override
        public ToolPresentation getPresentation() {
            return ToolPresentation.of("ulid-generator",
                    "ULID Generator", "ULID Generator").withGroupId("crypto");
        }
        @Override
        public UlidGeneratorTool create(ToolConfiguration config) { return new UlidGeneratorTool(config); }
    }
}
