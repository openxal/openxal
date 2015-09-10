/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package xal.app.lossviewer.waterfall;

import xal.extension.widgets.plot.ColorGenerator;
import xal.extension.widgets.plot.RainbowColorGenerator;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;

/**
 *
 * @author az9
 */
public class WaterfallPanel extends JPanel implements WaterfallDataListener {
    private static final long serialVersionUID = 5979034276112693437L;

    private BufferedImage bImg;
    private BufferedImage footerImg;
    private Dimension dimension = new Dimension();
    private Dimension oldDimension;
    private boolean bImgIsInvalid = true;
    private boolean footerbImgIsInvalid = true;
    private int footerHeight = 0;
    private Dimension dataDimension;
    private Dimension footerDimension;
    private boolean dataUpdated = true;
    private WaterfallData waterfallData;
    private boolean wasDataAdded = false;
    private DataSlice recentData;
    private static final ColorGenerator colGen = RainbowColorGenerator.getColorGenerator();
    private int pixelsPerVertLabel = 60;
    private Color labelColor = Color.BLACK;
    private DateFormat axisFormat = new SimpleDateFormat("HH:mm:ss");
    private DataGeometry dg = null;
    private int lastLabelDrawnShift;
    protected int nX = 99;
    protected int nY = 601;
    private double minLogV = -5;
    private double maxLogV = -1;
    private int yGap = 0;
    private int minYSize = 2;
    private int xGap = 2;
    private int minXSize = 5;
    private long repaintCounter = 0;
    private long paintCounter = 0;
    private boolean footerUpdated = true;
    private boolean isLogScale;

    public ColorGenerator getColorGenerator() {
        return colGen;
    }

    public DataPoint getDataPointFromScreen(int x, int y) {
        final String blank = "";
        String name = blank;
        int i = -1;
        if (x < dg.xBorders[0]) {
            name = blank;
        } else if (x > dg.xBorders[dg.xBorders.length - 1] + dg.xWaste) {
            name = blank;
        } else {
            i = Arrays.binarySearch(dg.xBorders, x);
            if (i < 0) {
                i = -i - 2;
            }
            name = waterfallData.getXName(i);
        }



        long timestamp = 0;
        double value = 0.0;
        int j = -1;
        if (y < 0) {
            timestamp = 0;
        } else if (y > dg.dataHeight) {
            timestamp = 0;
        } else {
            j = -(dg.dataHeight - dg.vHeight - y) / dg.yStep;
            if (j < 0) {
                timestamp = 0;
                value = 0.0;
            }
            if (j >= waterfallData.getSizeY()) {
                timestamp = 0;
                value = 0.0;
            } else {
                timestamp = waterfallData.getZBand(j).timestamp;
                if (!name.equals(blank)) {
                    value = waterfallData.getZBand(j).data[i];
                }
            }
        }


        return new DataPoint(name, value, timestamp, getColorByValue(value));
    }

    public double getMax() {
        return maxLogV;
    }

    public double getMin() {
        return minLogV;
    }

    public WaterfallPanel(WaterfallData wd, double lMin, double lMax) {
        setData(wd);
        minLogV = lMin;
        maxLogV = lMax;
        isLogScale = true;

    }

    public boolean isLogScaleUsed() {
        return isLogScale;
    }

    public void setData(WaterfallData wd) {
        waterfallData = wd;
        nX = waterfallData.getSizeX();
        nY = waterfallData.getSizeY();
        dg = new DataGeometry(nX);
        waterfallData.addDataListener(this);
        updateData();
        footerUpdated = true;

    }

    public void updateMaxMinLog(String max, String min, boolean log) {
        boolean changed = false;

        if (isLogScale != log) {
            isLogScale = log;
            changed = true;
        }
        try {
            double d = Double.parseDouble(max);
            if (maxLogV != d) {
                maxLogV = d;
                changed = true;
            }
        } catch (NumberFormatException nfe) {
        }

        try {
            double d = Double.parseDouble(min);
            if (minLogV != d) {
                minLogV = d;
                changed = true;
            }
        } catch (NumberFormatException nfe) {
        }
        if (changed) {
            updateData();
        }
    }

    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
//        System.out.println((System.currentTimeMillis()/1000)%1000+" paintComponent() called");
        checkValidity();

        if ((repaintCounter - paintCounter) > 1900) {
            bImgIsInvalid = true;
     //       System.out.println(repaintCounter + " " + paintCounter);
        }
        paintCounter = System.currentTimeMillis();

        if (bImgIsInvalid || dataUpdated) {
            if (footerHeight == 0) {
                calculateDataGeometry((Graphics2D) g, dimension);
            }
            dataDimension = new Dimension(dimension.width, dimension.height - footerHeight);
            if (dataDimension.height > 0) {
                bImg = getGraphicsConfiguration().createCompatibleImage(dataDimension.width, dataDimension.height);
                Graphics2D bGr = (Graphics2D) bImg.getGraphics();
                paintData(bGr, dataDimension);
                bGr.dispose();
                dataUpdated = false;
                wasDataAdded = false;
            }
        } else if (wasDataAdded) {
            wasDataAdded = false;
            BufferedImage previousBImg = bImg;
            if (dataDimension.width > 0 && dataDimension.height > 0) {
                bImg = getGraphicsConfiguration().createCompatibleImage(dataDimension.width, dataDimension.height);
                Graphics2D bGr = (Graphics2D) bImg.getGraphics();
                paintData(bGr, dataDimension, previousBImg);
                bGr.dispose();
            }


        }

        if (footerbImgIsInvalid || footerUpdated) {
            footerDimension = new Dimension(dimension.width, footerHeight);
            if (footerDimension.width > 0 && footerDimension.height > 0) {
                footerImg = getGraphicsConfiguration().createCompatibleImage(footerDimension.width, footerDimension.height);
                Graphics2D bGr = (Graphics2D) footerImg.getGraphics();
                paintFooter(bGr, footerDimension);
                bGr.dispose();
                footerUpdated = false;
            }
        }

        g.drawImage(bImg, 0, 0, null);
        g.drawImage(footerImg, 0, dataDimension.height, null);

    }

    protected void paintData(Graphics2D g, Dimension d) {
        paintData(g, dataDimension, null);
    }

    protected void paintData(Graphics2D g, Dimension d, BufferedImage previousBImg) {
        g.setBackground(getBackground());
        g.clearRect(0, 0, d.width, d.height);
        if (previousBImg == null) {
            calculateDataGeometry(g, d);
            for (int j = 0; j < nY; j++) {
                drawOneBand(g, dg, j, waterfallData.getZBand(j));
            }
            drawLabels(g);
            int a = 0;

        } else {
            g.drawImage(previousBImg, 0, -dg.yStep, previousBImg.getWidth(), previousBImg.getHeight(), null);
            drawOneBand(g, dg, nY - 1, recentData);
            lastLabelDrawnShift++;
            if (lastLabelDrawnShift > dg.slicesPerLabel * 4 / 3) {
                drawOneLabel(g, dg, nY - (lastLabelDrawnShift - dg.slicesPerLabel));
            }

        }
    }

    private void drawLabels(Graphics2D g) {
        for (int j = 0; j < dg.numOfLabels; j++) {
            int yindex = dg.slicesPerLabel + j * dg.slicesPerLabel;
            if (yindex < nY) {
                drawOneLabel(g, dg, yindex);
            }
        }
    }

    private void drawNames(Graphics2D g, Dimension d) {
        int startIndex = (nX - dg.slicesPerName * dg.numOfNames) / 2;
        for (int j = 0; j < dg.numOfNames; j++) {
            int xindex = startIndex + j * dg.slicesPerName;
            drawOneName(g, d, dg, xindex);
        }
    }

    private void drawOneName(Graphics2D g, Dimension d, DataGeometry dg, int xindex) {
        int x = d.width - dg.axisOffset / 10;
        int xl = d.width - dg.axisOffset / 5;
        int y = dg.xBorders[xindex] + dg.xStep / 2; //dg.axisOffset+xindex * dg.xStep;
        g.setColor(labelColor);
        g.drawLine(x, y, d.width, y);
        g.drawString(waterfallData.getXName(xindex), xl - dg.namesWidth[xindex], y + dg.labelHeight / 2);

    }

    private void drawOneLabel(Graphics2D g, DataGeometry dg, int yindex) {
        int x = dg.axisOffset / 10;
        int y = dg.dataHeight - dg.vHeight + yindex * dg.yStep;
        long tst = waterfallData.getZBand(yindex).timestamp;
        if (tst > 0) {
            g.setColor(labelColor);
            g.drawString(axisFormat.format(new Date(tst)), x, y + minYSize / 2 + dg.labelHeight / 2);
            g.drawLine(dg.axisOffset - x / 2, y + minYSize / 2, dg.axisOffset, y + minYSize / 2);
        }

        lastLabelDrawnShift = nY - yindex;
    }

    private void drawOneBand(Graphics2D g, DataGeometry dg, int index, DataSlice ds) {
        //  int currentX = dg.axisOffset;
        for (int i = 0; i < nX; i++) {
            g.setColor(getColorByValue(ds.data[i]));
            //     int waste = (i >= nX - dg.xWaste) ? 1 : 0;
            //   g.fillRect(currentX, dg.dataHeight - dg.vHeight + index * dg.yStep, dg.xStep - xGap, minYSize);
            g.fillRect(dg.xBorders[i], dg.dataHeight - dg.vHeight + index * dg.yStep, dg.xStep - xGap, minYSize);
        //    currentX += dg.xStep + waste;
        }
    }

    protected void paintFooter(Graphics2D g, Dimension dimension) {
     //   System.out.println("PaintFooter " + g.getClipBounds());
        g.setBackground(getBackground());
        g.clearRect(0, 0, dimension.width, dimension.height);
        AffineTransform oldtransform = g.getTransform();

        AffineTransform transform = new AffineTransform();
        transform.translate(dimension.width / 2, dimension.height / 2);
        transform.rotate(Math.PI * 1.5);
        transform.translate(-dimension.height / 2, -dimension.width / 2);

        g.transform(transform);

//        g.setColor(Color.WHITE);
//        g.drawOval(-3, -3, 6, 6);
//        g.setColor(Color.RED);
//        g.drawLine(5, 5,100, 5);
//        g.setColor(Color.GREEN);
//        g.drawLine(5, 5,5, 100);


        drawNames(g, new Dimension(dimension.height, dimension.width));
        g.setTransform(oldtransform);
    }

    private void checkValidity() {
        dimension = getSize();
        //      System.out.println("validity "+dimension + " "+ oldDimension);
        if (oldDimension == null) {
            footerbImgIsInvalid = true;
            bImgIsInvalid = true;
            oldDimension = dimension;
            return;
        }

        if (!dimension.equals(oldDimension)) {
         //   System.out.println("Dim changed " + dimension);
            bImgIsInvalid = true;
            if (oldDimension.width != dimension.width) {
                footerbImgIsInvalid = true;
            } else {
                footerbImgIsInvalid = false;
            }


        } else {
            bImgIsInvalid = false;
            footerbImgIsInvalid = false;
        }
        oldDimension = dimension;
    }

    public void updateData() {
        dataUpdated = true;
        repaint();
    }

    public void repaint() {
        super.repaint();
        repaintCounter = System.currentTimeMillis();
    }

    public void dataAdded(DataSlice data) {
        wasDataAdded = true;
        recentData = data;
        repaint();
    }

    public Color getColorByValue(double value) {
        if (isLogScale) {

            if (value <= 0) {
                return Color.BLACK;
            }
            double logValue = (Math.log(value) - Math.log(minLogV)) / (Math.log(maxLogV) - Math.log(minLogV));
            //    System.out.println(value+" "+logValue);
            if (logValue < 0) {
                return Color.BLACK;
            }
            return colGen.getColor(logValue);
        } else {
            double v = (value - minLogV) / (maxLogV - minLogV);
            if (v < 0) {
                return Color.BLACK;
            }
            return colGen.getColor(v);
        }
    }

    private DataGeometry calculateDataGeometry(Graphics2D g, Dimension d) {
        //   DataGeometry dg = new DataGeometry();

        dg.axisOffset = (int) (g.getFontMetrics().stringWidth("00:00.00") * 1.2);
        FontMetrics fMetrics = g.getFontMetrics();
        dg.labelHeight = fMetrics.getHeight() - fMetrics.getLeading() - fMetrics.getDescent();

        dg.dataWidth = d.width - dg.axisOffset;
        dg.dataHeight = d.height;
        dg.xStep = dg.dataWidth / nX;
        if (dg.xStep < (xGap + minXSize)) {
            dg.xStep = xGap + minXSize;
            dg.xWaste = 0;
        } else {
            dg.xWaste = dg.dataWidth - dg.xStep * nX;
        }
        dg.yStep = yGap + minYSize; // d.height / nY;
        dg.vHeight = dg.yStep * nY;
        dg.numOfLabels = dg.vHeight / pixelsPerVertLabel - 1;
        dg.slicesPerLabel = nY / dg.numOfLabels;

        dg.nameHeight = fMetrics.getHeight();
        if (dg.nameHeight * nX < dg.dataWidth) {
            dg.slicesPerName = 1;
        } else {
            dg.slicesPerName = (dg.nameHeight * nX) % dg.dataWidth == 0 ? (dg.nameHeight * nX) / dg.dataWidth : (dg.nameHeight * nX) / dg.dataWidth + 1;
        }
        if (dg.slicesPerName != 0) {
            dg.numOfNames = nX / dg.slicesPerName;
        }


        int currentX = dg.axisOffset;
        int maxnw = 0;
        for (int i = 0; i < nX; i++) {
            dg.xBorders[i] = currentX;
            int waste = (i >= nX - dg.xWaste) ? 1 : 0;
            currentX += dg.xStep + waste;
            int nw = g.getFontMetrics().stringWidth(waterfallData.getXName(i));
            dg.namesWidth[i] = nw;
            if (nw > maxnw) {
                maxnw = nw;
            }
        }
        footerHeight = maxnw + dg.axisOffset * 3 / 10;

        return dg;
    }

    public String getNameFromScreen(int x) {
        if (x < dg.xBorders[0]) {
            return "";
        }
        if (x > dg.xBorders[dg.xBorders.length - 1] + dg.xWaste) {
            return "";
        }
        int i = Arrays.binarySearch(dg.xBorders, x);
        if (i < 0) {
            i = -i - 2;
        }
        return waterfallData.getXName(i);
    }

    public long getTimestampFromScreen(int y) {
        if (y < 0) {
            return 0;
        }
        if (y > dg.dataHeight) {
            return 0;
        }
        int i = -(dg.dataHeight - dg.vHeight - y) / dg.yStep;
        if (i < 0) {
            return 0;
        }
        if (i >= waterfallData.getSizeY()) {
            return 0;
        }
        return waterfallData.getZBand(i).timestamp;
    }

    public void setPopupMenu(JPopupMenu m) {
    }
}

class DataGeometry {

    int dataWidth = 0;
    int dataHeight = 0;
    int xStep = 0;
    int xWaste = 0;
    int yStep = 0;
    int vHeight = 0;
    int axisOffset = 0;
    int numOfLabels = 0;
    int labelHeight = 0;
    int slicesPerLabel = 0;
    int nameHeight = 0;
    int slicesPerName = 0;
    int numOfNames = 0;
    int[] xBorders;
    int[] namesWidth;

    public DataGeometry(int n) {

        xBorders = new int[n];
        namesWidth = new int[n];
    }
}
