/*
 * @@COPYRIGHT@@
 */
package xal.extension.widgets.swing.wheelswitch.util;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;


/**
 * Static helper class for painting standardised syimbols over
 * existing Graphics objects.
 *
 * @author <a href="mailto:jernej.kamenik@cosylab.com">Jernej Kamenik</a>
 * @version $id$
 */
public class PaintHelper {
	
    protected static RenderingHints qualityHints = null;

    /**
     *
     */
    public static RenderingHints getAntialiasingHints() {
        if (qualityHints == null) {
            qualityHints = new RenderingHints(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
            qualityHints.put(RenderingHints.KEY_RENDERING,
                RenderingHints.VALUE_RENDER_QUALITY);
        }

        return qualityHints;
    }

    /**
     *
     */
    public static void paintRectangle(Graphics g, int x, int y, int width,
        int height, Color color, float strokeWidth) {
        Graphics2D g2D = (Graphics2D) g;
        g2D.addRenderingHints(getAntialiasingHints());
        g2D.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,
                (float) 0.40));
        g2D.setStroke( new BasicStroke( strokeWidth, BasicStroke.CAP_ROUND, BasicStroke.JOIN_MITER ) );
        g2D.setColor(color);
        g2D.drawRect(x + (int) (strokeWidth), y + (int) (strokeWidth),
            width - ((int) strokeWidth * 2), height - ((int) strokeWidth * 2));
    }

    /**
     *
     */
    public static void paintRectangle(Graphics g, Color color, float strokeWidth) {
        paintRectangle(g, g.getClipBounds().x, g.getClipBounds().y,
            g.getClipBounds().width, g.getClipBounds().height, color,
            strokeWidth);
    }
}
