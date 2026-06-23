package gt.devtools.tools.api.converter;

import gt.devtools.settings.ToolConfiguration;

import javax.swing.*;
import java.awt.*;

/**
 * A {@link Converter} that works in both directions — forward (encode)
 * and backward (decode). Adds a second action button and swaps the
 * source/target semantics for the backward pass.
 *
 * <h3>Action bar</h3>
 * Adds "Encode ↓", "Decode ↑", and copy-both-sides buttons compared
 * to the base {@link Converter}.
 *
 * <h3>Subclassing</h3>
 * Implement both {@link #doConvertForward} (from {@link Converter})
 * AND {@link #doConvertBackward}.
 * <p>
 * For encoder/decoder tools with standard labels, extend
 * {@link EncoderDecoder} instead.
 */
public abstract class BidirectionalConverter extends Converter {

    protected BidirectionalConverter(ToolConfiguration config) {
        super(config);
    }

    @Override
    protected String getConvertButtonLabel() {
        return "Encode ↓";
    }

    /** Action bar with encode, decode, and copy-both-sides buttons. */
    @Override
    protected JPanel buildActionBar() {
        var bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));

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
     * Run the backward (decode) conversion: reads bytes from the target
     * editor, calls {@link #doConvertBackward}, and writes the result
     * to the source editor.
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
                    sourceEditor.setBytes(get());
                } catch (Exception e) {
                    sourceEditor.setText("Error: " + e.getMessage());
                }
            }
        }.execute();
    }

    /**
     * Backward conversion logic (decode / unescape).
     * Reads from the target editor, writes to the source editor.
     *
     * @param input target text as UTF-8 bytes
     * @return decoded bytes displayed in the source editor
     * @throws Exception on conversion failure
     */
    protected abstract byte[] doConvertBackward(byte[] input) throws Exception;
}
