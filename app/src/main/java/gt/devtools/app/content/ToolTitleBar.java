package gt.devtools.app.content;

import gt.devtools.tools.api.ToolFactory;

import javax.swing.*;
import java.awt.*;

/**
 * Title bar displayed above the workbench tabs showing the tool
 * name, a description tooltip, and action buttons (new tab, reset, help).
 */
public final class ToolTitleBar extends JPanel {

    private final ToolFactory<?> factory;
    private final WorkbenchTabbedPane workbench;

    public ToolTitleBar(ToolFactory<?> factory, WorkbenchTabbedPane workbench) {
        super(new BorderLayout());
        this.factory = factory;
        this.workbench = workbench;

        setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0,
                UIManager.getColor("Separator.foreground")));

        // -- left: title
        var leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        leftPanel.setOpaque(false);

        var titleLabel = new JLabel(factory.getPresentation().contentTitle());
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 14f));
        leftPanel.add(titleLabel);

        String desc = factory.getPresentation().description();
        if (desc != null && !desc.isEmpty()) {
            var helpBtn = new JButton("?");
            helpBtn.setFont(helpBtn.getFont().deriveFont(Font.BOLD, 10f));
            helpBtn.setBorder(BorderFactory.createEmptyBorder(0, 4, 0, 4));
            helpBtn.setContentAreaFilled(false);
            helpBtn.setFocusable(false);
            helpBtn.setToolTipText(desc);
            leftPanel.add(helpBtn);
        }

        add(leftPanel, BorderLayout.WEST);

        // -- right: actions
        var rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 4, 4));
        rightPanel.setOpaque(false);

        var newTabBtn = new JButton("+");
        newTabBtn.setFont(newTabBtn.getFont().deriveFont(Font.BOLD, 14f));
        newTabBtn.setBorder(BorderFactory.createEmptyBorder(0, 6, 0, 6));
        newTabBtn.setContentAreaFilled(false);
        newTabBtn.setFocusable(false);
        newTabBtn.setToolTipText("New workbench tab");
        newTabBtn.addActionListener(e -> workbench.newWorkbench());
        rightPanel.add(newTabBtn);

        var resetBtn = new JButton("↺");
        resetBtn.setFont(resetBtn.getFont().deriveFont(Font.BOLD, 14f));
        resetBtn.setBorder(BorderFactory.createEmptyBorder(0, 6, 0, 6));
        resetBtn.setContentAreaFilled(false);
        resetBtn.setFocusable(false);
        resetBtn.setToolTipText("Reset tool state");
        resetBtn.addActionListener(e -> workbench.resetCurrent());
        rightPanel.add(resetBtn);

        add(rightPanel, BorderLayout.EAST);

        setOpaque(true);
    }
}
