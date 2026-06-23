package gt.devtools.tools.crypto;

import com.fasterxml.uuid.Generators;
import com.fasterxml.uuid.impl.TimeBasedEpochGenerator;
import gt.devtools.common.ValueProperty;
import gt.devtools.settings.ToolConfiguration;
import gt.devtools.tools.api.ToolFactory;
import gt.devtools.tools.api.ToolPresentation;
import gt.devtools.tools.api.generator.OneLineTextGenerator;

import javax.swing.*;
import java.awt.*;
import java.util.UUID;

/**
 * Generates UUIDs in versions 1 through 7.
 * Uses {@code java-uuid-generator} for time-based and name-based versions
 * and JDK {@link java.util.UUID} for v3/v4.
 */
public final class UuidGeneratorTool extends OneLineTextGenerator {

    private static final String[] VERSIONS = {"v1 (time)", "v3 (MD5)", "v4 (random)", "v5 (SHA-1)", "v6 (reordered time)", "v7 (epoch+random)"};
    private final ValueProperty<String> version;
    private final ValueProperty<String> namespace;
    private final ValueProperty<String> name;

    private UuidGeneratorTool(ToolConfiguration config) {
        super(config);
        this.version = registerConfig("uuidVersion", "v4 (random)");
        this.namespace = registerConfig("uuidNamespace", "");
        this.name = registerConfig("uuidName", "");
    }

    @Override
    protected void buildConfigurationUi(JPanel configPanel) {
        var row1 = new JPanel(new FlowLayout(FlowLayout.LEFT));
        row1.add(new JLabel("Version:"));
        var versionCombo = new JComboBox<>(VERSIONS);
        versionCombo.setSelectedItem(version.get());
        versionCombo.addActionListener(e -> version.set((String) versionCombo.getSelectedItem()));
        row1.add(versionCombo);
        configPanel.add(row1);

        var row2 = new JPanel(new FlowLayout(FlowLayout.LEFT));
        row2.add(new JLabel("Namespace (for v3/v5):"));
        var nsField = new JTextField(namespace.get(), 25);
        nsField.setToolTipText("UUID of the namespace, e.g. 6ba7b810-9dad-11d1-80b4-00c04fd430c8 for DNS");
        nsField.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override public void focusLost(java.awt.event.FocusEvent e) { namespace.set(nsField.getText()); }
        });
        row2.add(nsField);
        configPanel.add(row2);

        var row3 = new JPanel(new FlowLayout(FlowLayout.LEFT));
        row3.add(new JLabel("Name (for v3/v5):"));
        var nameField = new JTextField(name.get(), 25);
        nameField.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override public void focusLost(java.awt.event.FocusEvent e) { name.set(nameField.getText()); }
        });
        row3.add(nameField);
        configPanel.add(row3);
    }

    @Override
    protected String generate() throws Exception {
        String v = version.get();
        UUID uuid;
        if (v.startsWith("v1")) {
            uuid = Generators.timeBasedGenerator().generate();
        } else if (v.startsWith("v3")) {
            uuid = UUID.nameUUIDFromBytes((namespace.get() + name.get()).getBytes());
        } else if (v.startsWith("v5")) {
            uuid = Generators.nameBasedGenerator(UUID.fromString(namespace.get())).generate(name.get());
        } else if (v.startsWith("v6")) {
            uuid = Generators.timeBasedReorderedGenerator().generate();
        } else if (v.startsWith("v7")) {
            TimeBasedEpochGenerator gen = Generators.timeBasedEpochGenerator();
            uuid = gen.generate();
        } else {
            uuid = UUID.randomUUID();
        }
        return uuid.toString();
    }

    public static final class Factory implements ToolFactory<UuidGeneratorTool> {
        public Factory() {}
        @Override public String getId() { return "uuid-generator"; }
        @Override
        public ToolPresentation getPresentation() {
            return ToolPresentation.of("uuid-generator",
                    "UUID Generator", "UUID Generator").withGroupId("crypto");
        }
        @Override
        public UuidGeneratorTool create(ToolConfiguration config) { return new UuidGeneratorTool(config); }
    }
}
