package gt.devtools.tools.standalone;

import gt.devtools.common.ValueProperty;
import gt.devtools.settings.ToolConfiguration;
import gt.devtools.tools.api.ToolPresentation;
import gt.devtools.tools.api.fx.ConverterFx;
import gt.devtools.tools.api.fx.ToolFxFactory;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.dataformat.toml.TomlMapper;
import tools.jackson.dataformat.xml.XmlMapper;
import tools.jackson.dataformat.yaml.YAMLMapper;

import java.nio.charset.StandardCharsets;

/** JavaFX version: Converts between JSON, YAML, XML, and TOML configuration formats. */
public final class ConfigFormatConverterFx extends ConverterFx {

    private static final String[] FORMATS = {"JSON", "YAML", "XML", "TOML"};

    private final ValueProperty<String> sourceFormat;
    private final ValueProperty<String> targetFormat;
    private final JsonMapper jsonMapper = new JsonMapper();
    private final YAMLMapper yamlMapper = new YAMLMapper();
    private final XmlMapper xmlMapper = new XmlMapper();
    private final TomlMapper tomlMapper = new TomlMapper();

    private ConfigFormatConverterFx(ToolConfiguration config) {
        super(config);
        this.sourceFormat = registerConfig("sourceFormat", "JSON");
        this.targetFormat = registerConfig("targetFormat", "YAML");
    }

    @Override
    protected void buildUi(BorderPane panel) {
        var configBar = new HBox(8);
        configBar.setStyle("-fx-padding: 4 0;");
        configBar.getChildren().add(new Label("From:"));
        var srcCombo = new ComboBox<String>();
        srcCombo.getItems().addAll(FORMATS);
        srcCombo.setValue(sourceFormat.get());
        srcCombo.setOnAction(e -> sourceFormat.set(srcCombo.getValue()));
        configBar.getChildren().add(srcCombo);

        configBar.getChildren().add(new Label("→ To:"));
        var tgtCombo = new ComboBox<String>();
        tgtCombo.getItems().addAll(FORMATS);
        tgtCombo.setValue(targetFormat.get());
        tgtCombo.setOnAction(e -> targetFormat.set(tgtCombo.getValue()));
        configBar.getChildren().add(tgtCombo);

        panel.setTop(configBar);
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
            default -> throw new IllegalArgumentException("Unknown format: " + sourceFormat.get());
        };

        String result = switch (targetFormat.get()) {
            case "JSON" -> jsonMapper.writerWithDefaultPrettyPrinter().writeValueAsString(tree);
            case "YAML" -> yamlMapper.writerWithDefaultPrettyPrinter().writeValueAsString(tree);
            case "XML" -> xmlMapper.writerWithDefaultPrettyPrinter().writeValueAsString(tree);
            case "TOML" -> tomlMapper.writerWithDefaultPrettyPrinter().writeValueAsString(tree);
            default -> throw new IllegalArgumentException("Unknown format: " + targetFormat.get());
        };

        return result.getBytes(StandardCharsets.UTF_8);
    }

    public static final class Factory implements ToolFxFactory<ConfigFormatConverterFx> {
        public Factory() {}
        @Override public String getId() { return "config-format-converter"; }
        @Override public ToolPresentation getPresentation() {
            return ToolPresentation.of("config-format-converter",
                    "Config Format Converter", "Config Format Converter")
                    .withGroupId("formatters");
        }
        @Override
        public ConfigFormatConverterFx create(ToolConfiguration config) {
            return new ConfigFormatConverterFx(config);
        }
    }
}
