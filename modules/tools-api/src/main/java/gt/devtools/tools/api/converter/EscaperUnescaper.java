package gt.devtools.tools.api.converter;

import gt.devtools.settings.ToolConfiguration;

import javax.swing.*;
import java.awt.*;

/**
 * Convenience base for escape/unescape tools with pre-set labels.
 * <p>
 * Subclasses only need to implement two methods:
 * <ul>
 *   <li>{@link #doConvertForward(byte[])} — escape (source → target)</li>
 *   <li>{@link #doConvertBackward(byte[])} — unescape (target → source)</li>
 * </ul>
 *
 * <h3>Example: HTML Entities</h3>
 * <pre>{@code
 * public final class HtmlEntitiesEscaperUnescaper extends EscaperUnescaper {
 *     public HtmlEntitiesEscaperUnescaper(ToolConfiguration config) { super(config); }
 *     protected byte[] doConvertForward(byte[] input) {
 *         return StringEscapeUtils.escapeHtml4(new String(input, UTF_8)).getBytes(UTF_8);
 *     }
 *     protected byte[] doConvertBackward(byte[] input) {
 *         return StringEscapeUtils.unescapeHtml4(new String(input, UTF_8)).getBytes(UTF_8);
 *     }
 * }
 * }</pre>
 */
public abstract class EscaperUnescaper extends BidirectionalConverter {

    protected EscaperUnescaper(ToolConfiguration config) {
        super(config);
    }

    @Override
    protected JPanel buildActionBar() {
        var bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));

        var escapeBtn = new JButton("Escape ↓");
        escapeBtn.addActionListener(e -> convert());
        bar.add(escapeBtn);

        var unescapeBtn = new JButton("Unescape ↑");
        unescapeBtn.addActionListener(e -> convertBackward());
        bar.add(unescapeBtn);

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

    /** Escape: source plaintext → target escaped text. */
    @Override
    protected abstract byte[] doConvertForward(byte[] input) throws Exception;

    /** Unescape: target escaped text → source plaintext. */
    @Override
    protected abstract byte[] doConvertBackward(byte[] input) throws Exception;
}
