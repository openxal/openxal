/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package xal.app.lossviewer.waterfall;

import xal.extension.widgets.plot.ColorGenerator;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import javax.swing.JComponent;

/**
 *
 * @author az9
 */
public class ColorLegend extends JComponent {
    private static final long serialVersionUID = -3581547085597324224L;

    private ColorGenerator colorGenerator;
    private BufferedImage bImg;
    private int compWidth;
    private int compHeight;

    public ColorLegend(ColorGenerator colgen) {
        super();
        this.colorGenerator = colgen;
    }

    public void paintComponent(Graphics g) {
        bImg = getGraphicsConfiguration().createCompatibleImage(getWidth(), getHeight());
        Graphics2D bGr = (Graphics2D) bImg.getGraphics();
        compWidth = getWidth();
        compHeight = getHeight();
        for(int i=0;i<compWidth;i++){
            double value = (double)i/compWidth;
            bGr.setColor(colorGenerator.getColor(value));
            bGr.drawLine(i, 0, i, compHeight);
        }
        bGr.dispose();
        g.drawImage(bImg, 0, 0, null);

    }
}
