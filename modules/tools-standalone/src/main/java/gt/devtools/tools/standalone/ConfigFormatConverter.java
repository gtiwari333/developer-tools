package gt.devtools.tools.standalone;

import gt.devtools.common.ValueProperty;
import gt.devtools.settings.ToolConfiguration;
import gt.devtools.tools.api.ToolFactory;
import gt.devtools.tools.api.ToolPresentation;
import gt.devtools.tools.api.converter.Converter;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.dataformat.toml.TomlMapper;
import tools.jackson.dataformat.xml.XmlMapper;
import tools.jackson.dataformat.yaml.YAMLMapper;

import javax.swing.*;
import java.awt.*;
import java.nio.charset.StandardCharsets;

/** Converts between JSON, YAML, XML, and TOML configuration formats. */
public final class ConfigFormatConverter extends Converter {

    private static final String[] FORMATS = {"JSON", "YAML", "XML", "TOML"};
    private final ValueProperty<String> sourceFormat;
    private final ValueProperty<String> targetFormat;
    private final JsonMapper jsonMapper = new JsonMapper();
    private final YAMLMapper yamlMapper = new YAMLMapper();
    private final XmlMapper xmlMapper = new XmlMapper();
    private final TomlMapper tomlMapper = new TomlMapper();

    private ConfigFormatConverter(ToolConfiguration config) {
        super(config);
        this.sourceFormat = registerConfig("sourceFormat", "JSON");
        this.targetFormat = registerConfig("targetFormat", "YAML");
    }

    @Override
    protected void buildUi(JPanel panel) {
        var configBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        configBar.add(new JLabel("From:"));
        var srcCombo = new JComboBox<>(FORMATS);
        srcCombo.setSelectedItem(sourceFormat.get());
        srcCombo.addActionListener(e -> sourceFormat.set((String) srcCombo.getSelectedItem()));
        configBar.add(srcCombo);
        configBar.add(new JLabel("→ To:"));
        var tgtCombo = new JComboBox<>(FORMATS);
        tgtCombo.setSelectedItem(targetFormat.get());
        tgtCombo.addActionListener(e -> targetFormat.set((String) tgtCombo.getSelectedItem()));
        configBar.add(tgtCombo);
        panel.add(configBar, BorderLayout.NORTH);
        super.buildUi(panel);
    }

    @Override
    protected byte[] doConvertForward(byte[] input) throws Exception {
        String srcText = new String(input, StandardCharsets.UTF_8).strip();
        if (srcText.isEmpty()) return new byte[0];
        JsonNode tree = switch (sourceFormat.get()) {
            case "JSON" -> jsonMapper.readTree(srcText);
            case "YAML" -> yamlMapper.readTree(srcText);
            case "XML" -> xmlMapper.readTree(srcText);
            case "TOML" -> tomlMapper.readTree(srcText);
            default -> throw new IllegalArgumentException("Unknown format");
        };
        String result = switch (targetFormat.get()) {
            case "JSON" -> jsonMapper.writerWithDefaultPrettyPrinter().writeValueAsString(tree);
            case "YAML" -> yamlMapper.writerWithDefaultPrettyPrinter().writeValueAsString(tree);
            case "XML" -> xmlMapper.writerWithDefaultPrettyPrinter().writeValueAsString(tree);
            case "TOML" -> tomlMapper.writerWithDefaultPrettyPrinter().writeValueAsString(tree);
            default -> throw new IllegalArgumentException("Unknown format");
        };
        return result.getBytes(StandardCharsets.UTF_8);
    }

    public static final class Factory implements ToolFactory<ConfigFormatConverter> {
        public Factory() {}
        @Override public String getId() { return "config-format-converter"; }
        @Override public ToolPresentation getPresentation() {
            return ToolPresentation.of("config-format-converter",
                    "Config Format Converter", "Config Format Converter")
                    .withGroupId("formatters");
        }
        @Override public ConfigFormatConverter create(ToolConfiguration config) { return new ConfigFormatConverter(config); }
    }
}
