package gt.devtools.app.content;

import javax.swing.*;
import java.awt.*;

/**
 * A small pin icon drawn with Graphics2D for consistent cross-platform rendering.
 * Active (pinned) state is filled; inactive state is an outline.
 */
final class PinIcon implements Icon {

    private static final int SIZE = 12;
    private final boolean active;

    PinIcon(boolean active) { this.active = active; }

    @Override
    public void paintIcon(Component c, Graphics g, int x, int y) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        Color fg = c.getForeground();
        if (fg == null) fg = UIManager.getColor("Label.foreground");
        if (fg == null) fg = Color.GRAY;
        g2.setColor(fg);

        int cx = x + SIZE / 2;
        int cy = y + 1;

        // Pin head (circle)
        int headR = 3;
        if (active) {
            g2.fillOval(cx - headR, cy, headR * 2, headR * 2);
        } else {
            g2.drawOval(cx - headR, cy, headR * 2, headR * 2);
        }

        // Pin body (line down)
        g2.setStroke(new BasicStroke(1.2f));
        g2.drawLine(cx, cy + headR, cx, cy + SIZE - 1);

        // Pin point (small triangle at bottom)
        int[] xs = { cx - 2, cx, cx + 2 };
        int[] ys = { cy + SIZE - 4, cy + SIZE, cy + SIZE - 4 };
        if (active) {
            g2.fillPolygon(xs, ys, 3);
        } else {
            g2.drawPolyline(xs, ys, 3);
        }

        g2.dispose();
    }

    @Override public int getIconWidth() { return SIZE; }
    @Override public int getIconHeight() { return SIZE; }
}
