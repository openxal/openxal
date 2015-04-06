package xal.app.lossviewer.dndcomponents.lossplot;

import xal.app.lossviewer.*;
import xal.app.lossviewer.signals.*;
import java.awt.*;
import java.awt.geom.*;
import java.util.*;
import org.jfree.chart.axis.*;
import org.jfree.text.*;
import org.jfree.ui.*;

import java.util.List;

public class BLMAxisFactory {

    private static Map<Integer, Color> statusColors = new HashMap<Integer, Color>();


    static {
        statusColors.put(LossDetector.STATUS_OK, Color.BLACK);
        statusColors.put(LossDetector.STATUS_NEGATIVE, Color.MAGENTA);
        statusColors.put(LossDetector.STATUS_NOISE, Color.YELLOW);
        statusColors.put(LossDetector.STATUS_INVALID, Color.WHITE);
    }
    static boolean STATUS_DECORATION = true;

    public static SymbolAxis getBLMAxis(final String param, final Map<String, TypeDataSet> typeSeries, Map<String, NumberAxis> typeAxes) {

        final ArrayList<LossDetector> detectors = new ArrayList<LossDetector>();

        for (TypeDataSet v : typeSeries.values()) {
            if (v.isVisible()) {
                detectors.addAll(v.getDetectors());
            }
        }
        if (detectors.size() > 0) {
            Collections.sort(detectors, LossDetector.getComparator());
        }


        String[] names = new String[detectors.size()];
        final LossDetector[] dets = new LossDetector[detectors.size()];
        final ValueAxis[] axes = new ValueAxis[detectors.size()];

        int index = 0;
        for (LossDetector det : detectors) {
            names[index] = det.getShortName();
            dets[index] = det;
            axes[index] = typeAxes.get(det.getType());
            index++;
        }
        
        @SuppressWarnings("rawtypes")
        SymbolAxis axis = new SymbolAxis("", names) {
            private static final long serialVersionUID = -2911592900608877261L;

            protected void drawGridBandsHorizontal(Graphics2D g2,
                    Rectangle2D plotArea,
                    Rectangle2D dataArea,
                    boolean firstGridBandIsDark,
                    List ticks) {

                boolean currentGridBandIsDark = firstGridBandIsDark;
                double yy = dataArea.getY();
                double xx1, xx2;

                //gets the outline stroke width of the plot
                double outlineStrokeWidth;
                if (getPlot().getOutlineStroke() != null) {
                    outlineStrokeWidth = ((BasicStroke) getPlot().getOutlineStroke()).getLineWidth();
                } else {
                    outlineStrokeWidth = 1d;
                }

                Iterator iterator = ticks.iterator();
                ValueTick tick;
                Rectangle2D band;
                while (iterator.hasNext()) {
                    tick = (ValueTick) iterator.next();

                    int index = (int) tick.getValue();


                    xx1 = valueToJava2D(tick.getValue() - 0.5d, dataArea,
                            RectangleEdge.BOTTOM);
                    xx2 = valueToJava2D(tick.getValue() + 0.5d, dataArea,
                            RectangleEdge.BOTTOM);

                    currentGridBandIsDark = !currentGridBandIsDark;
                    if (dets.length > 0) {
                        LossDetector detector = dets[index];
                        Set<String> normalization = typeSeries.get(detector.getType()).getNormalization();
                        boolean limitVisibility = typeSeries.get(detector.getType()).isLimitVisible();
                        if (limitVisibility) {
                            ScalarSignalValue limit = (ScalarSignalValue) detector.getValue(param, normalization);
                            if (limit != null) {
                                ValueAxis ax = axes[index];
                                double losslimit = Math.abs(limit.getValue());
                                double zz = ax.valueToJava2D(losslimit, dataArea,
                                        RectangleEdge.RIGHT);



                                g2.setPaint(Color.red);
                                Rectangle2D.Double zRect = new Rectangle2D.Double(xx1, yy + outlineStrokeWidth,
                                        xx2 - xx1, zz - yy - outlineStrokeWidth);
                                g2.fill(zRect);
                            }
                        }

                        if (STATUS_DECORATION) {
                            int status = detectors.get((int) tick.getValue()).getStatus();
                            Color color = statusColors.get(status);
                            if (color == null || status == LossDetector.STATUS_OK) {
                            } else {
                                g2.setPaint(color);
                                double x, y, w, h;
                                x = xx1;
                                y = yy + outlineStrokeWidth;
                                w = xx2 - xx1;
                                h = 10;
                                band = new Rectangle2D.Double(x, y, w, 10);
                                g2.fill(band);
                            }

                        }

                    }

                }
                g2.setPaintMode();
            }

            protected AxisState drawTickMarksAndLabels(Graphics2D g2,
                    double cursor,
                    Rectangle2D plotArea,
                    Rectangle2D dataArea,
                    RectangleEdge edge) {

                AxisState state = new AxisState(cursor);

                if (isAxisLineVisible()) {
                    drawAxisLine(g2, cursor, dataArea, edge);
                }

                double ol = getTickMarkOutsideLength();
                double il = getTickMarkInsideLength();

                List ticks = refreshTicks(g2, state, dataArea, edge);
                state.setTicks(ticks);
                g2.setFont(getTickLabelFont());
                Iterator iterator = ticks.iterator();
                while (iterator.hasNext()) {
                    ValueTick tick = (ValueTick) iterator.next();
                    if (isTickLabelsVisible()) {
                        ///status decorations

                        if (STATUS_DECORATION && dets.length > 0) {
                            int status = detectors.get((int) tick.getValue()).getStatus();
                            Color color = statusColors.get(status);
                            if (color == null) {
                                g2.setPaint(getTickLabelPaint());
                            } else {
                                g2.setPaint(color);
                            }
                        } else {
                            g2.setPaint(getTickLabelPaint());
                        }


                        float[] anchorPoint = calculateAnchorPoint(
                                tick, cursor, dataArea, edge);

                        TextUtilities.drawRotatedString(
                                tick.getText(), g2,
                                anchorPoint[0], anchorPoint[1],
                                tick.getTextAnchor(),
                                tick.getAngle(),
                                tick.getRotationAnchor());
                    }

                    if (isTickMarksVisible()) {
                        float xx = (float) valueToJava2D(
                                tick.getValue(), dataArea, edge);
                        Line2D mark = null;
                        g2.setStroke(getTickMarkStroke());
                        g2.setPaint(getTickMarkPaint());
                        if (edge == RectangleEdge.LEFT) {
                            mark = new Line2D.Double(cursor - ol, xx, cursor + il, xx);
                        } else if (edge == RectangleEdge.RIGHT) {
                            mark = new Line2D.Double(cursor + ol, xx, cursor - il, xx);
                        } else if (edge == RectangleEdge.TOP) {
                            mark = new Line2D.Double(xx, cursor - ol, xx, cursor + il);
                        } else if (edge == RectangleEdge.BOTTOM) {
                            mark = new Line2D.Double(xx, cursor + ol, xx, cursor - il);
                        }
                        g2.draw(mark);
                    }
                }

                // need to work out the space used by the tick labels...
                // so we can update the cursor...
                double used = 0.0;
                if (isTickLabelsVisible()) {
                    if (edge == RectangleEdge.LEFT) {
                        used += findMaximumTickLabelWidth(
                                ticks, g2, plotArea, isVerticalTickLabels());
                        state.cursorLeft(used);
                    } else if (edge == RectangleEdge.RIGHT) {
                        used = findMaximumTickLabelWidth(
                                ticks, g2, plotArea, isVerticalTickLabels());
                        state.cursorRight(used);
                    } else if (edge == RectangleEdge.TOP) {
                        used = findMaximumTickLabelHeight(
                                ticks, g2, plotArea, isVerticalTickLabels());
                        state.cursorUp(used);
                    } else if (edge == RectangleEdge.BOTTOM) {
                        used = findMaximumTickLabelHeight(
                                ticks, g2, plotArea, isVerticalTickLabels());
                        state.cursorDown(used);
                    }
                }

                return state;
            }
        };
        //	axis.setTickLabelFont(axis.getTickLabelFont().deriveFont(Font.BOLD, 14.0f));
        return axis;
    }
}
