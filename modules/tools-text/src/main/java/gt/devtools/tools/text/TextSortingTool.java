package gt.devtools.tools.text;

import gt.devtools.common.ValueProperty;
import gt.devtools.settings.ToolConfiguration;
import gt.devtools.tools.api.ToolFactory;
import gt.devtools.tools.api.ToolPresentation;
import gt.devtools.tools.api.text.TextTransformer;

import javax.swing.*;
import java.awt.*;
import java.util.Arrays;

/**
 * Sorts lines of text in ascending or descending order.
 */
public final class TextSortingTool extends TextTransformer {

    private final ValueProperty<String> direction;

    private TextSortingTool(ToolConfiguration config) {
        super(config);
        this.direction = registerConfig("sortDirection", "Ascending");
    }

    @Override
    protected void buildUi(JPanel panel) {
        var configBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        configBar.add(new JLabel("Order:"));
        var combo = new JComboBox<>(new String[]{"Ascending", "Descending"});
        combo.setSelectedItem(direction.get());
        combo.addActionListener(e -> {
            direction.set((String) combo.getSelectedItem());
            if (Boolean.TRUE.equals(liveTransformation.get())) transform();
        });
        configBar.add(combo);
        panel.add(configBar, BorderLayout.NORTH);
        super.buildUi(panel);
    }

    @Override
    protected String getTransformLabel() { return "Sort"; }

    @Override
    protected String doTransform(String input) {
        String[] lines = input.split("\n");
        if ("Descending".equals(direction.get())) {
            Arrays.sort(lines, java.util.Comparator.reverseOrder());
        } else {
            Arrays.sort(lines);
        }
        return String.join("\n", lines);
    }

    public static final class Factory implements ToolFactory<TextSortingTool> {
        public Factory() {}
        @Override public String getId() { return "text-sorting-transformer"; }
        @Override public ToolPresentation getPresentation() {
            return ToolPresentation.of("text-sorting-transformer",
                    "Text Sorting", "Text Sorting").withGroupId("text");
        }
        @Override public TextSortingTool create(ToolConfiguration config) { return new TextSortingTool(config); }
    }
}
