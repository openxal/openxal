/*
 * @@COPYRIGHT@@
 */
package xal.tools.swing.wheelswitch.util;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.RenderingHints;
import java.awt.TexturePaint;
import java.awt.image.BufferedImage;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;


/**
 * Static helper class for painting standardised syimbols over
 * existing Graphics objects.
 *
 * @author <a href="mailto:jernej.kamenik@cosylab.com">Jernej Kamenik</a>
 * @version $id$
 */
public class PaintHelper {
    public static HashMap networkErrorImages;
    protected static RenderingHints qualityHints = null;
    protected static BufferedImage rasterPattern = null;
    public static HashMap warningImages = null;
    public static HashMap alarmImages = null;
    public static HashMap emergencyImages = null;
    public static HashMap clockCaseImages = null;
    public static HashMap minutePointers = null;
    public static HashMap hourPointers = null;

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

    public static Paint getRasterPaint(Color color) {
        BufferedImage rasterPattern = new BufferedImage(2, 2,
                BufferedImage.TYPE_4BYTE_ABGR);
        Graphics2D patternGraphics = rasterPattern.createGraphics();
        patternGraphics.setComposite(AlphaComposite.getInstance(
                AlphaComposite.SRC_OVER, (float) 0.30));
        patternGraphics.setColor(color);
        patternGraphics.fillRect(0, 0, 1, 1);
        patternGraphics.fillRect(1, 1, 1, 1);

        return new TexturePaint(rasterPattern,
            rasterPattern.getRaster().getBounds());
    }

    public static Paint getLinePaint(Color color) {
        BufferedImage rasterPattern = new BufferedImage(3, 3,
                BufferedImage.TYPE_4BYTE_ABGR);
        Graphics2D patternGraphics = rasterPattern.createGraphics();
        patternGraphics.setComposite(AlphaComposite.getInstance(
                AlphaComposite.SRC_OVER, (float) 0.30));
        patternGraphics.setColor(color);
        patternGraphics.fillRect(0, 0, 1, 1);
        patternGraphics.fillRect(1, 1, 1, 1);
        patternGraphics.fillRect(2, 2, 1, 1);

        return new TexturePaint(rasterPattern,
            rasterPattern.getRaster().getBounds());
    }

    /**
     *
     */
    public static void paintDisabled(Graphics g, int x, int y, int width,
        int height) {
        Graphics2D g2D = (Graphics2D) g;
        g2D.addRenderingHints(getAntialiasingHints());

        g2D.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,
                (float) 1.));
        g2D.setPaint(getRasterPaint(Color.BLACK));
        g2D.fillRect(x, y, width, height);
    }

    /**
     *
     */
    public static void paintDisabled(Graphics g) {
        paintDisabled(g, g.getClipBounds().x, g.getClipBounds().y,
            g.getClipBounds().width, g.getClipBounds().height);
    }

    /**
     *
     */
    public static void paintDark(Graphics g, int x, int y, int width, int height) {
        Graphics2D g2D = (Graphics2D) g;
        g2D.addRenderingHints(getAntialiasingHints());
        g2D.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,
                (float) 0.9));
        g2D.setColor(Color.BLACK);
        g2D.fillRect(x, y, width, height);
        g2D.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC,
                (float) 0));
    }

    /**
     *
     */
    public static void paintDark(Graphics g) {
        paintDark(g, g.getClipBounds().x, g.getClipBounds().y,
            g.getClipBounds().width, g.getClipBounds().height);
    }

    /**
     *
     */
    public static void paintTimeout(Graphics g, int x, int y, int clockSize,
        Date time) {
        int width = g.getClipBounds().width;
        int height = g.getClipBounds().height;
        float strokeWidth = (float) (clockSize / 75.);
        double pointerWidth = Math.max(clockSize / 36., 1);
        double pointerTipWidth = clockSize / 110.;
        double minutePointerLength = (clockSize * 2.8) / 8.;
        double hourPointerLength = (clockSize * 4.4) / 16.;
        double backPointerLength = (clockSize * 2.5) / 16.;
        int[] xs = { 0, 0, 0, 0, 0, 0 };
        int[] ys = { 0, 0, 0, 0, 0, 0 };
        Integer size = new Integer(clockSize);

        if (clockCaseImages == null) {
            clockCaseImages = new HashMap();
        }

        if (clockCaseImages.get(size) == null) {
            //System.out.println("Making new clockcase image.");
            BufferedImage clockCaseImage = new BufferedImage((int) clockSize,
                    (int) clockSize, BufferedImage.TYPE_4BYTE_ABGR);
            Graphics2D clockCaseGraphics = clockCaseImage.createGraphics();
            clockCaseGraphics.addRenderingHints(getAntialiasingHints());
            clockCaseGraphics.setComposite(AlphaComposite.getInstance(
                    AlphaComposite.SRC_OVER, (float) 0.40));
            clockCaseGraphics.setStroke(new BasicStroke(strokeWidth,
                    BasicStroke.CAP_ROUND, BasicStroke.JOIN_MITER));
            clockCaseGraphics.setColor(ColorHelper.getTimeOut());
            clockCaseGraphics.drawOval((clockSize / 2) -
                (int) ((clockSize / 2.) - (strokeWidth / 2.)),
                (clockSize / 2) -
                (int) ((clockSize / 2.) - (strokeWidth / 2.)),
                ((int) ((clockSize / 2.) - (strokeWidth / 2.))) * 2,
                ((int) ((clockSize / 2.) - (strokeWidth / 2.))) * 2);
            clockCaseGraphics.drawOval((clockSize / 2) -
                (int) (((clockSize * 7.) / 16.) - (strokeWidth / 2.)),
                (clockSize / 2) -
                (int) (((clockSize * 7.) / 16.) - (strokeWidth / 2.)),
                ((int) (((clockSize * 7.) / 16.) - (strokeWidth / 2.))) * 2,
                ((int) (((clockSize * 7.) / 16.) - (strokeWidth / 2.))) * 2);
            clockCaseGraphics.setStroke(new BasicStroke(
                    (float) (strokeWidth * 1.3), BasicStroke.CAP_ROUND,
                    BasicStroke.JOIN_MITER));

            for (int i = 0; i < 12; i++) {
                xs[0] = (clockSize / 2) +
                    (int) ((((clockSize * 7.) / 8.) - (2.5 * strokeWidth)) / 2. * Math.cos((2 * Math.PI * i) / 12));
                ys[0] = (clockSize / 2) +
                    (int) ((((clockSize * 7.) / 8.) - (2.5 * strokeWidth)) / 2. * Math.sin((2 * Math.PI * i) / 12));
                xs[1] = (clockSize / 2) +
                    (int) ((((clockSize * 6.5) / 8.) - (2.5 * strokeWidth)) / 2. * Math.cos((2 * Math.PI * i) / 12));
                ys[1] = (clockSize / 2) +
                    (int) ((((clockSize * 6.5) / 8.) - (2.5 * strokeWidth)) / 2. * Math.sin((2 * Math.PI * i) / 12));
                clockCaseGraphics.drawLine(xs[0], ys[0], xs[1], ys[1]);
            }

            clockCaseImages.put(new Integer(clockSize), clockCaseImage);
        }

        if (hourPointers == null) {
            hourPointers = new HashMap();
        }

        if (hourPointers.get(size) == null) {
            //System.out.println("Making new hour pointer image.");
            BufferedImage hourPointer = new BufferedImage((int) (pointerWidth),
                    (int) (hourPointerLength + backPointerLength),
                    BufferedImage.TYPE_4BYTE_ABGR);
            Graphics2D hourPointerGraphics = hourPointer.createGraphics();
            hourPointerGraphics.addRenderingHints(getAntialiasingHints());
            hourPointerGraphics.setComposite(AlphaComposite.getInstance(
                    AlphaComposite.SRC_OVER, (float) 0.40));
            hourPointerGraphics.setStroke(new BasicStroke((float) strokeWidth,
                    BasicStroke.CAP_ROUND, BasicStroke.JOIN_MITER));
            hourPointerGraphics.setColor(ColorHelper.getTimeOut());
            xs[0] = (int) 0;
            ys[0] = (int) (hourPointerLength);
            xs[1] = (int) 0 + (int) ((pointerWidth - pointerTipWidth) / 2.);
            ys[1] = 0;
            xs[2] = (int) 0 + (int) ((pointerWidth + pointerTipWidth) / 2.);
            ys[2] = 0;
            xs[3] = (int) 0 + (int) pointerWidth;
            ys[3] = (int) (hourPointerLength);
            xs[4] = (int) 0 + (int) ((pointerWidth + pointerTipWidth) / 2.);
            ys[4] = (int) (hourPointerLength + backPointerLength);
            xs[5] = (int) 0 + (int) ((pointerWidth - pointerTipWidth) / 2.);
            ys[5] = (int) (hourPointerLength + backPointerLength);
            ;
            hourPointerGraphics.fillPolygon(xs, ys, 6);
            hourPointers.put(size, hourPointer);
        }

        if (minutePointers == null) {
            minutePointers = new HashMap();
        }

        if (minutePointers.get(size) == null) {
            //System.out.println("Making new minute pointer image.");
            BufferedImage minutePointer = new BufferedImage((int) (pointerWidth),
                    (int) (minutePointerLength + backPointerLength),
                    BufferedImage.TYPE_4BYTE_ABGR);
            Graphics2D minutePointerGraphics = minutePointer.createGraphics();
            minutePointerGraphics.addRenderingHints(getAntialiasingHints());
            minutePointerGraphics.setComposite(AlphaComposite.getInstance(
                    AlphaComposite.SRC_OVER, (float) 0.40));
            minutePointerGraphics.setStroke(new BasicStroke(
                    (float) strokeWidth, BasicStroke.CAP_ROUND,
                    BasicStroke.JOIN_MITER));
            minutePointerGraphics.setColor(ColorHelper.getTimeOut());
            xs[0] = 0;
            ys[0] = (int) (minutePointerLength);
            xs[1] = (int) ((pointerWidth - pointerTipWidth) / 2.);
            ys[1] = 0;
            xs[2] = (int) ((pointerWidth + pointerTipWidth) / 2.);
            ys[2] = 0;
            xs[3] = (int) pointerWidth;
            ys[3] = (int) (minutePointerLength);
            xs[4] = (int) ((pointerWidth + pointerTipWidth) / 2.);
            ys[4] = (int) (minutePointerLength + backPointerLength);
            xs[5] = (int) ((pointerWidth - pointerTipWidth) / 2.);
            ys[5] = (int) (minutePointerLength + backPointerLength);
            ;
            minutePointerGraphics.fillPolygon(xs, ys, 6);
            minutePointers.put(size, minutePointer);
        }

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(time);

        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);
        double hourAngle = (2 * Math.PI * (hour + (minute / 60.))) / 12.;
        double minuteAngle = (2 * Math.PI * minute) / 60.;

        Graphics2D g2D = (Graphics2D) g;
        g2D.addRenderingHints(qualityHints);
        g2D.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,
                (float) 1.));
        g2D.drawImage((BufferedImage) clockCaseImages.get(size), null, x, y);
        g2D.rotate(hourAngle, (int) (x + (clockSize / 2.)),
            (int) (y + (clockSize / 2.)));
        g2D.drawImage((BufferedImage) hourPointers.get(size), null,
            (int) (x + ((clockSize - pointerWidth) / 2.)),
            (int) ((y + (clockSize / 2.)) - hourPointerLength));
        g2D.rotate(-hourAngle, (int) (x + (clockSize / 2.)),
            (int) (y + (clockSize / 2.)));
        g2D.rotate(minuteAngle, (int) (x + (clockSize / 2.)),
            (int) (y + (clockSize / 2.)));
        g2D.drawImage((BufferedImage) minutePointers.get(size), null,
            (int) (x + ((clockSize - pointerWidth) / 2.)),
            (int) ((y + (clockSize / 2.)) - minutePointerLength));
        g2D.rotate(-minuteAngle, (int) (x + (clockSize / 2.)),
            (int) (y + (clockSize / 2.)));
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
        g2D.setStroke(new BasicStroke((float) strokeWidth,
                BasicStroke.CAP_ROUND, BasicStroke.JOIN_MITER));
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

    /**
     *
     */
    public static void paintWarning(Graphics g, int x, int y, int warningSize) {
        float strokeWidth = (float) (warningSize / 70.);
        double exclamationInset = warningSize / 5;
        double exclamationWidth = warningSize / 3.;
        double exclamationBright = warningSize / 25.;
        double exclamationHeight = warningSize / 2.5;
        double exclamationArcHeight = warningSize / 15.;
        double exclamationPointSize = warningSize / 5.;
        double exclamationPointInset = warningSize / 1.55;
        int[] xs = { 0, 0, 0, 0 };
        int[] ys = { 0, 0, 0, 0 };
        Integer size = new Integer(warningSize);

        if (warningImages == null) {
            warningImages = new HashMap();
        }

        if (warningImages.get(size) == null) {
            //System.out.println("Making new warning image.");
            BufferedImage warningImage = new BufferedImage((int) warningSize,
                    (int) warningSize, BufferedImage.TYPE_4BYTE_ABGR);
            Graphics2D warningGraphics = warningImage.createGraphics();
            warningGraphics.addRenderingHints(getAntialiasingHints());
            warningGraphics.setComposite(AlphaComposite.getInstance(
                    AlphaComposite.SRC_OVER, (float) 0.4));

            warningGraphics.setColor(ColorHelper.getWarning());
            warningGraphics.fillOval((int) (strokeWidth / 2.),
                (int) (strokeWidth / 2.), (int) (warningSize - strokeWidth),
                (int) (warningSize - strokeWidth));

            warningGraphics.setColor(Color.WHITE);
            xs[0] = (int) ((warningSize - exclamationWidth) / 2);
            ys[0] = (int) (exclamationInset);
            xs[1] = (int) ((warningSize + exclamationWidth) / 2);
            ys[1] = (int) (exclamationInset);
            xs[2] = (int) ((warningSize + exclamationBright) / 2);
            ys[2] = (int) (exclamationInset + exclamationHeight);
            xs[3] = (int) ((warningSize - exclamationBright) / 2);
            ys[3] = (int) (exclamationInset + exclamationHeight);
            warningGraphics.fillPolygon(xs, ys, 4);
            warningGraphics.fillArc(xs[0],
                ys[0] - (int) (exclamationArcHeight / 2),
                (int) exclamationWidth, (int) exclamationArcHeight, 0, 180);
            warningGraphics.fillOval((int) ((warningSize -
                exclamationPointSize) / 2), (int) (exclamationPointInset),
                (int) exclamationPointSize, (int) exclamationPointSize);

            warningGraphics.setColor(ColorHelper.getWarningOutline());
            warningGraphics.setStroke(new BasicStroke(strokeWidth,
                    BasicStroke.CAP_ROUND, BasicStroke.JOIN_MITER));
            warningGraphics.drawOval((int) (strokeWidth / 2.),
                (int) (strokeWidth / 2.), (int) (warningSize - strokeWidth),
                (int) (warningSize - strokeWidth));
            warningGraphics.setStroke(new BasicStroke(strokeWidth / 2,
                    BasicStroke.CAP_ROUND, BasicStroke.JOIN_MITER));
            warningGraphics.drawLine(xs[0], ys[0], xs[3], ys[3]);
            warningGraphics.drawLine(xs[1], ys[1], xs[2], ys[2]);
            warningGraphics.drawLine(xs[2], ys[2], xs[3], ys[3]);
            warningGraphics.drawArc(xs[0],
                ys[0] - (int) (exclamationArcHeight / 2),
                (int) exclamationWidth, (int) exclamationArcHeight, 0, 180);
            warningGraphics.drawOval((int) ((warningSize -
                exclamationPointSize) / 2), (int) (exclamationPointInset),
                (int) exclamationPointSize, (int) exclamationPointSize);
            warningImages.put(size, warningImage);
        }

        Graphics2D g2D = (Graphics2D) g;
        g2D.addRenderingHints(getAntialiasingHints());
        g2D.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,
                (float) 1.));
        g2D.drawImage((BufferedImage) warningImages.get(size), null, x, y);
    }

    /**
     *
     */
    public static void paintAlarm(Graphics g, int x, int y, int alarmSize) {
        float strokeWidth = (float) (alarmSize / 70.);
        double exclamationInset = alarmSize / 5;
        double exclamationWidth = alarmSize / 3.;
        double exclamationBright = alarmSize / 25.;
        double exclamationHeight = alarmSize / 2.5;
        double exclamationArcHeight = alarmSize / 15.;
        double exclamationPointSize = alarmSize / 5.;
        double exclamationPointInset = alarmSize / 1.55;
        int[] xs = { 0, 0, 0, 0 };
        int[] ys = { 0, 0, 0, 0 };
        Integer size = new Integer(alarmSize);

        if (alarmImages == null) {
            alarmImages = new HashMap();
        }

        if (alarmImages.get(size) == null) {
            //System.out.println("Making new alarm image.");
            BufferedImage alarmImage = new BufferedImage((int) alarmSize,
                    (int) alarmSize, BufferedImage.TYPE_4BYTE_ABGR);
            Graphics2D alarmGraphics = alarmImage.createGraphics();
            alarmGraphics.addRenderingHints(getAntialiasingHints());
            alarmGraphics.setComposite(AlphaComposite.getInstance(
                    AlphaComposite.SRC_OVER, (float) 0.4));

            alarmGraphics.setColor(ColorHelper.getAlarm());
            alarmGraphics.fillOval((int) (strokeWidth / 2.),
                (int) (strokeWidth / 2.), (int) (alarmSize - strokeWidth),
                (int) (alarmSize - strokeWidth));

            alarmGraphics.setColor(Color.WHITE);
            xs[0] = (int) ((alarmSize - exclamationWidth) / 2);
            ys[0] = (int) (exclamationInset);
            xs[1] = (int) ((alarmSize + exclamationWidth) / 2);
            ys[1] = (int) (exclamationInset);
            xs[2] = (int) ((alarmSize + exclamationBright) / 2);
            ys[2] = (int) (exclamationInset + exclamationHeight);
            xs[3] = (int) ((alarmSize - exclamationBright) / 2);
            ys[3] = (int) (exclamationInset + exclamationHeight);
            alarmGraphics.fillPolygon(xs, ys, 4);
            alarmGraphics.fillArc(xs[0],
                ys[0] - (int) (exclamationArcHeight / 2),
                (int) exclamationWidth, (int) exclamationArcHeight, 0, 180);
            alarmGraphics.fillOval((int) ((alarmSize - exclamationPointSize) / 2),
                (int) (exclamationPointInset), (int) exclamationPointSize,
                (int) exclamationPointSize);

            alarmGraphics.setColor(ColorHelper.getAlarmOutline());
            alarmGraphics.setStroke(new BasicStroke(strokeWidth,
                    BasicStroke.CAP_ROUND, BasicStroke.JOIN_MITER));
            alarmGraphics.drawOval((int) (strokeWidth / 2.),
                (int) (strokeWidth / 2.), (int) (alarmSize - strokeWidth),
                (int) (alarmSize - strokeWidth));
            alarmGraphics.setStroke(new BasicStroke(strokeWidth / 2,
                    BasicStroke.CAP_ROUND, BasicStroke.JOIN_MITER));
            alarmGraphics.drawLine(xs[0], ys[0], xs[3], ys[3]);
            alarmGraphics.drawLine(xs[1], ys[1], xs[2], ys[2]);
            alarmGraphics.drawLine(xs[2], ys[2], xs[3], ys[3]);
            alarmGraphics.drawArc(xs[0],
                ys[0] - (int) (exclamationArcHeight / 2),
                (int) exclamationWidth, (int) exclamationArcHeight, 0, 180);
            alarmGraphics.drawOval((int) ((alarmSize - exclamationPointSize) / 2),
                (int) (exclamationPointInset), (int) exclamationPointSize,
                (int) exclamationPointSize);
            alarmImages.put(size, alarmImage);
        }

        Graphics2D g2D = (Graphics2D) g;
        g2D.addRenderingHints(getAntialiasingHints());
        g2D.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,
                (float) 1.));
        g2D.drawImage((BufferedImage) alarmImages.get(size), null, x, y);
    }

    /**
     *
     */
    public static void paintNetworkError(Graphics g, int x, int y,
        int networkSize) {
        float strokeWidth = (float) (networkSize / 70.);
        double crossArmLength = networkSize / 4.;
        double crossArmWidth = networkSize / 6.;
        double crossCenter = networkSize / 2.;
        int[] xs = new int[12];
        int[] ys = new int[12];
        Integer size = new Integer(networkSize);

        if (networkErrorImages == null) {
            networkErrorImages = new HashMap();
        }

        if (networkErrorImages.get(size) == null) {
            //System.out.println("Making new alarm image.");
            BufferedImage networkImage = new BufferedImage((int) networkSize,
                    (int) networkSize, BufferedImage.TYPE_4BYTE_ABGR);
            Graphics2D networkGraphics = networkImage.createGraphics();
            networkGraphics.addRenderingHints(getAntialiasingHints());
            networkGraphics.setComposite(AlphaComposite.getInstance(
                    AlphaComposite.SRC_OVER, (float) 0.4));

            networkGraphics.setColor(ColorHelper.getAlarm());
            networkGraphics.fillOval((int) (strokeWidth / 2.),
                (int) (strokeWidth / 2.), (int) (networkSize - strokeWidth),
                (int) (networkSize - strokeWidth));

            networkGraphics.setColor(Color.WHITE);
            networkGraphics.translate(crossCenter, -crossCenter / 2.4);
            networkGraphics.rotate(Math.PI / 4);
            xs[0] = (int) (crossCenter + (crossArmWidth / 2.));
            ys[0] = (int) (crossCenter - crossArmLength - (crossArmWidth / 2.));
            xs[1] = (int) (crossCenter - (crossArmWidth / 2.));
            ys[1] = (int) (crossCenter - crossArmLength - (crossArmWidth / 2.));
            xs[2] = (int) (crossCenter - (crossArmWidth / 2.));
            ys[2] = (int) (crossCenter - (crossArmWidth / 2.));
            xs[3] = (int) (crossCenter - crossArmLength - (crossArmWidth / 2.));
            ys[3] = (int) (crossCenter - (crossArmWidth / 2.));
            xs[4] = (int) (crossCenter - crossArmLength - (crossArmWidth / 2.));
            ys[4] = (int) (crossCenter + (crossArmWidth / 2.));
            xs[5] = (int) (crossCenter - (crossArmWidth / 2.));
            ys[5] = (int) (crossCenter + (crossArmWidth / 2.));
            xs[6] = (int) (crossCenter - (crossArmWidth / 2.));
            ys[6] = (int) (crossCenter + crossArmLength + (crossArmWidth / 2.));
            xs[7] = (int) (crossCenter + (crossArmWidth / 2.));
            ys[7] = (int) (crossCenter + crossArmLength + (crossArmWidth / 2.));
            xs[8] = (int) (crossCenter + (crossArmWidth / 2.));
            ys[8] = (int) (crossCenter + (crossArmWidth / 2.));
            xs[9] = (int) (crossCenter + crossArmLength + (crossArmWidth / 2.));
            ys[9] = (int) (crossCenter + (crossArmWidth / 2.));
            xs[10] = (int) (crossCenter + crossArmLength +
                (crossArmWidth / 2.));
            ys[10] = (int) (crossCenter - (crossArmWidth / 2.));
            xs[11] = (int) (crossCenter + (crossArmWidth / 2.));
            ys[11] = (int) (crossCenter - (crossArmWidth / 2.));
            networkGraphics.fillPolygon(xs, ys, 12);

            networkGraphics.rotate(-Math.PI / 4);
            networkGraphics.translate(-crossCenter, crossCenter / 2.4);
            networkGraphics.setColor(ColorHelper.getAlarmOutline());
            networkGraphics.setStroke(new BasicStroke(strokeWidth,
                    BasicStroke.CAP_ROUND, BasicStroke.JOIN_MITER));
            networkGraphics.drawOval((int) (strokeWidth / 2.),
                (int) (strokeWidth / 2.), (int) (networkSize - strokeWidth),
                (int) (networkSize - strokeWidth));

            networkGraphics.setStroke(new BasicStroke(strokeWidth / 2,
                    BasicStroke.CAP_ROUND, BasicStroke.JOIN_MITER));
            networkGraphics.translate(crossCenter, -crossCenter / 2.4);
            networkGraphics.rotate(Math.PI / 4);
            networkGraphics.drawLine(xs[0], ys[0], xs[1], ys[1]);
            networkGraphics.drawLine(xs[1], ys[1], xs[2], ys[2]);
            networkGraphics.drawLine(xs[2], ys[2], xs[3], ys[3]);
            networkGraphics.drawLine(xs[3], ys[3], xs[4], ys[4]);
            networkGraphics.drawLine(xs[4], ys[4], xs[5], ys[5]);
            networkGraphics.drawLine(xs[5], ys[5], xs[6], ys[6]);
            networkGraphics.drawLine(xs[6], ys[6], xs[7], ys[7]);
            networkGraphics.drawLine(xs[7], ys[7], xs[8], ys[8]);
            networkGraphics.drawLine(xs[8], ys[8], xs[9], ys[9]);
            networkGraphics.drawLine(xs[9], ys[9], xs[10], ys[10]);
            networkGraphics.drawLine(xs[10], ys[10], xs[11], ys[11]);
            networkGraphics.drawLine(xs[11], ys[11], xs[0], ys[0]);

            networkErrorImages.put(size, networkImage);
        }

        Graphics2D g2D = (Graphics2D) g;
        g2D.addRenderingHints(getAntialiasingHints());
        g2D.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,
                (float) 1.));
        g2D.drawImage((BufferedImage) networkErrorImages.get(size), null, x, y);
    }

    /**
     *
     */
    public static void paintEmergency(Graphics g, int x, int y,
        int emergencySize) {
        float strokeWidth = (float) (emergencySize / 35.);
        double exclamationInset = emergencySize / 5;
        double exclamationWidth = emergencySize / 3.;
        double exclamationBright = emergencySize / 25.;
        double exclamationHeight = emergencySize / 2.5;
        double exclamationArcHeight = emergencySize / 15.;
        double exclamationPointSize = emergencySize / 5.;
        double exclamationPointInset = emergencySize / 1.55;
        int[] xs = { 0, 0, 0, 0 };
        int[] ys = { 0, 0, 0, 0 };
        Integer size = new Integer(emergencySize);

        if (emergencyImages == null) {
            emergencyImages = new HashMap();
        }

        if (emergencyImages.get(size) == null) {
            //System.out.println("Making new emergency image.");
            BufferedImage emergencyImage = new BufferedImage((int) emergencySize,
                    (int) emergencySize, BufferedImage.TYPE_4BYTE_ABGR);
            Graphics2D emergencyGraphics = emergencyImage.createGraphics();
            emergencyGraphics.addRenderingHints(getAntialiasingHints());
            emergencyGraphics.setComposite(AlphaComposite.getInstance(
                    AlphaComposite.SRC_OVER, (float) 0.4));

            emergencyGraphics.setColor(ColorHelper.getEmergency());
            emergencyGraphics.fillOval((int) (strokeWidth / 2.),
                (int) (strokeWidth / 2.), (int) (emergencySize - strokeWidth),
                (int) (emergencySize - strokeWidth));

            emergencyGraphics.setColor(Color.WHITE);
            xs[0] = (int) ((emergencySize - exclamationWidth) / 2);
            ys[0] = (int) (exclamationInset);
            xs[1] = (int) ((emergencySize + exclamationWidth) / 2);
            ys[1] = (int) (exclamationInset);
            xs[2] = (int) ((emergencySize + exclamationBright) / 2);
            ys[2] = (int) (exclamationInset + exclamationHeight);
            xs[3] = (int) ((emergencySize - exclamationBright) / 2);
            ys[3] = (int) (exclamationInset + exclamationHeight);
            emergencyGraphics.fillPolygon(xs, ys, 4);
            emergencyGraphics.fillArc(xs[0],
                ys[0] - (int) (exclamationArcHeight / 2),
                (int) exclamationWidth, (int) exclamationArcHeight, 0, 180);
            emergencyGraphics.fillOval((int) ((emergencySize -
                exclamationPointSize) / 2), (int) (exclamationPointInset),
                (int) exclamationPointSize, (int) exclamationPointSize);

            emergencyGraphics.setColor(ColorHelper.getEmergencyOutline());
            emergencyGraphics.setStroke(new BasicStroke(strokeWidth,
                    BasicStroke.CAP_ROUND, BasicStroke.JOIN_MITER));
            emergencyGraphics.drawOval((int) (strokeWidth / 2.),
                (int) (strokeWidth / 2.), (int) (emergencySize - strokeWidth),
                (int) (emergencySize - strokeWidth));
            emergencyGraphics.setStroke(new BasicStroke(strokeWidth / 2,
                    BasicStroke.CAP_ROUND, BasicStroke.JOIN_MITER));
            emergencyGraphics.drawLine(xs[0], ys[0], xs[3], ys[3]);
            emergencyGraphics.drawLine(xs[1], ys[1], xs[2], ys[2]);
            emergencyGraphics.drawLine(xs[2], ys[2], xs[3], ys[3]);
            emergencyGraphics.drawArc(xs[0],
                ys[0] - (int) (exclamationArcHeight / 2),
                (int) exclamationWidth, (int) exclamationArcHeight, 0, 180);
            emergencyGraphics.drawOval((int) ((emergencySize -
                exclamationPointSize) / 2), (int) (exclamationPointInset),
                (int) exclamationPointSize, (int) exclamationPointSize);
            emergencyImages.put(size, emergencyImage);
        }

        Graphics2D g2D = (Graphics2D) g;
        g2D.addRenderingHints(getAntialiasingHints());
        g2D.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,
                (float) 1.));
        g2D.drawImage((BufferedImage) emergencyImages.get(size), null, x, y);
    }

    /**
     *
     */
    public static void main(String[] args) {
        javax.swing.JApplet applet = new javax.swing.JApplet() {
                public void init() {
                    javax.swing.JPanel mPanel = new javax.swing.JPanel() {
                            public void paintComponent(Graphics g) {
                                super.paintComponent(g);

                                int width = getParent().getBounds().width;
                                int height = getParent().getBounds().height;
                                int x = getParent().getBounds().x;
                                int y = getParent().getBounds().y;
                                int size = (Math.min(width, height) * 9) / 10;
                                int size1 = (int) ((Math.min(width, height) * 9.2) / 10);

                                //paintWarning(g,x+(width-size1)/2,y+(height-size1)/2,size1);
                                //paintAlarm(g,x+(width-size1)/2,y+(height-size1)/2,size1);
                                paintNetworkError(g, x + ((width - size1) / 2),
                                    y + ((height - size1) / 2), size1);

                                //paintEmergency(g,x+(width-size1)/2,y+(height-size1)/2,size1);
                                //paintTimeout(g,x+(width-size)/2,y+(height-size)/2,size,new Date());
                                paintRectangle(g, x, y, width, height,
                                    ColorHelper.getEmergencyOutline(),
                                    (float) Math.max(size / 50, 1));

                                //paintDisabled(g);
                                //paintDark(g);
                            }
                        };

                    mPanel.setVisible(true);
                    mPanel.setOpaque(false);
                    mPanel.setSize(new Dimension(5000, 5000));

                    javax.swing.JPanel pn = new javax.swing.JPanel();
                    pn.setLayout(new FlowLayout());
                    pn.add(new javax.swing.JLabel("JLabel"));
                    pn.add(new javax.swing.JSlider());
                    pn.add(new javax.swing.JButton("JButton"));
                    pn.add(new javax.swing.JTextField("JTextField"));
                    this.getContentPane().add(pn);
                    this.getLayeredPane().add(mPanel, new Integer(101), 0);
                }
            };

        javax.swing.JFrame frame = new javax.swing.JFrame(
                "paintHelper Demo Applet");
        frame.getContentPane().add(applet);
        frame.setSize(300, 150);
        frame.addWindowListener(new java.awt.event.WindowAdapter() {
                public void windowClosing(java.awt.event.WindowEvent e) {
                    System.exit(0);
                }
            });
        applet.init();
        applet.start();
        frame.setVisible(true);
    }

    /**
     *
     */
    public static void paintBumps(Graphics g, int x, int y, int width,
        int height, Color light, Color dark) {
        int bumpRight = (x + width) - 1;
        int bumpBottom = (y + height) - 1;

        g.setColor(light);

        for (int xi = x; xi < bumpRight; xi += 4) {
            for (int yi = y; yi < bumpBottom; yi += 4) {
                g.drawLine(xi, yi, xi, yi);

                if (((xi + 2) < bumpRight) && ((yi + 2) < bumpBottom)) {
                    g.drawLine(xi + 2, yi + 2, xi + 2, yi + 2);
                }
            }
        }

        g.setColor(dark);

        for (int xi = x; xi < bumpRight; xi += 4) {
            for (int yi = y; yi < bumpBottom; yi += 4) {
                g.drawLine(xi + 1, yi + 1, xi + 1, yi + 1);

                if (((xi + 2) < bumpRight) && ((yi + 2) < bumpBottom)) {
                    g.drawLine(xi + 3, yi + 3, xi + 3, yi + 3);
                }
            }
        }
    }
}
