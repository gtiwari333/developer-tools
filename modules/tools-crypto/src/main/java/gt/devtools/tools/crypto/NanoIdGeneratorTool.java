package gt.devtools.tools.crypto;

import com.aventrix.jnanoid.jnanoid.NanoIdUtils;
import gt.devtools.common.ValueProperty;
import gt.devtools.settings.ToolConfiguration;
import gt.devtools.tools.api.ToolFactory;
import gt.devtools.tools.api.ToolPresentation;
import gt.devtools.tools.api.generator.OneLineTextGenerator;

import javax.swing.*;
import java.awt.*;
import java.security.SecureRandom;

/**
 * Generates compact, URL-safe, cryptographically-strong random IDs
 * using the Nano ID algorithm. Uses {@code jnanoid} library.
 */
public final class NanoIdGeneratorTool extends OneLineTextGenerator {

    private final ValueProperty<Integer> length;

    private NanoIdGeneratorTool(ToolConfiguration config) {
        super(config);
        this.length = registerConfig("nanoIdLength", 21);
    }

    @Override
    protected void buildConfigurationUi(JPanel configPanel) {
        var row = new JPanel(new FlowLayout(FlowLayout.LEFT));
        row.add(new JLabel("Length:"));
        var spinner = new JSpinner(new javax.swing.SpinnerNumberModel(
                length.get().intValue(), 1, 256, 1));
        spinner.addChangeListener(e -> length.set((Integer) spinner.getValue()));
        row.add(spinner);
        configPanel.add(row);
    }

    @Override
    protected String generate() {
        return NanoIdUtils.randomNanoId(new SecureRandom(),
                NanoIdUtils.DEFAULT_ALPHABET, length.get());
    }

    public static final class Factory implements ToolFactory<NanoIdGeneratorTool> {
        public Factory() {}
        @Override public String getId() { return "nano-id-generator"; }
        @Override
        public ToolPresentation getPresentation() {
            return ToolPresentation.of("nano-id-generator",
                    "Nano ID Generator", "Nano ID Generator").withGroupId("crypto");
        }
        @Override
        public NanoIdGeneratorTool create(ToolConfiguration config) { return new NanoIdGeneratorTool(config); }
    }
}
