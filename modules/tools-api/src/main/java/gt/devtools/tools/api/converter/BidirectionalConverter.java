package gt.devtools.tools.api.converter;

import gt.devtools.settings.ToolConfiguration;

import javax.swing.*;
import java.awt.*;

/**
 * A bidirectional converter supports both forward and backward conversion
 * (e.g., encode ↔ decode).
 */
public abstract class BidirectionalConverter extends Converter {

    protected BidirectionalConverter(ToolConfiguration config) {
        super(config);
    }

    @Override
    protected String getConvertButtonLabel() {
        return "Encode ↓";
    }

    @Override
    protected JPanel buildActionBar() {
        var bar = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 8, 4));

        var forwardBtn = new JButton("Encode ↓");
        forwardBtn.addActionListener(e -> convert());
        bar.add(forwardBtn);

        var backwardBtn = new JButton("Decode ↑");
        backwardBtn.addActionListener(e -> convertBackward());
        bar.add(backwardBtn);

        var liveCheck = new JCheckBox("Live");
        liveCheck.setSelected(liveConversion.get());
        liveCheck.addActionListener(e -> liveConversion.set(liveCheck.isSelected()));
        bar.add(liveCheck);

        var copySourceBtn = new JButton("Copy Source");
        copySourceBtn.addActionListener(e -> sourceEditor.copyToClipboard());
        bar.add(copySourceBtn);

        var copyTargetBtn = new JButton("Copy Result");
        copyTargetBtn.addActionListener(e -> targetEditor.copyToClipboard());
        bar.add(copyTargetBtn);

        var swapBtn = new JButton("↑↓ Swap");
        swapBtn.addActionListener(e -> swap());
        bar.add(swapBtn);

        return bar;
    }

    /**
     * Execute backward (decode) conversion.
     */
    public void convertBackward() {
        new SwingWorker<byte[], Void>() {
            @Override
            protected byte[] doInBackground() throws Exception {
                return doConvertBackward(targetEditor.getBytes());
            }

            @Override
            protected void done() {
                try {
                    byte[] result = get();
                    sourceEditor.setBytes(result);
                } catch (Exception e) {
                    sourceEditor.setText("Error: " + e.getMessage());
                }
            }
        }.execute();
    }

    /**
     * Subclasses implement the backward (decode) conversion.
     */
    protected abstract byte[] doConvertBackward(byte[] input) throws Exception;
}
