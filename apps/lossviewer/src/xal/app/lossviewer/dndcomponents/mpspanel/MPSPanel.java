/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package xal.app.lossviewer.dndcomponents.mpspanel;

import xal.app.lossviewer.dndcomponents.*;
import xal.app.lossviewer.LossDetector;
import xal.app.lossviewer.signals.ScalarSignalValue;
import xal.app.lossviewer.signals.SignalHistory;
import xal.app.lossviewer.views.View;
import xal.app.lossviewer.views.ViewEvent;
import xal.app.lossviewer.views.ViewListener;



import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.Paint;
import java.awt.Stroke;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.geom.Point2D;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JTextField;
import javax.swing.SwingWorker;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.time.Millisecond;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.xy.XYBarDataset;
import org.jfree.data.xy.XYDataItem;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import xal.tools.data.DataAdaptor;
import xal.tools.data.DataListener;

/**
 *
 * @author az9
 */
public class MPSPanel extends MPSPanelNB implements SelectionHandler<LossDetector>, DataListener, ViewListener {
    private static final long serialVersionUID = -3143305083569787661L;

    private View<LossDetector> view;
    private LossDetector showingDetector;
    private TimeSeriesCollection historyDataSet;
    private TimeSeries history;
    private XYSeries histogram;
    private XYBarDataset barDataSet;

    public static void setupPlot(ChartPanel chartPanel) {

        JFreeChart chart = chartPanel.getChart();
        XYPlot plot = (XYPlot) chart.getPlot();
        Paint plotBackgroundPaint = ((Color) chart.getBackgroundPaint());//.darker();                

        chart.setBackgroundPaint(plotBackgroundPaint);
        chart.setAntiAlias(false);
        chartPanel.setMinimumDrawWidth(0);
        chartPanel.setMinimumDrawHeight(0);
        chartPanel.setMaximumDrawWidth(Integer.MAX_VALUE);
        chartPanel.setMaximumDrawHeight(Integer.MAX_VALUE);

        plot.setBackgroundPaint(plotBackgroundPaint);

        Stroke gridLine = new BasicStroke(0.5f);
        plot.setRangeGridlinePaint(Color.LIGHT_GRAY);
        plot.setRangeGridlineStroke(gridLine);
        plot.setDomainGridlinePaint(Color.LIGHT_GRAY);
        plot.setDomainGridlineStroke(gridLine);
    }

    public MPSPanel(View<LossDetector> v) {
        super();
        this.view = v;

        historyDataSet = new TimeSeriesCollection();
        history = new TimeSeries("", Millisecond.class);
        histogram = new XYSeries("");
        historyDataSet.addSeries(history);

        JFreeChart historychart = ChartFactory.createTimeSeriesChart("", "", "Rad", historyDataSet, false, false, false);
        ((NumberAxis) ((XYPlot) historychart.getPlot()).getRangeAxis()).setNumberFormatOverride(limitFormat);
        ChartPanel historyPanel = new ChartPanel(historychart);
        setupPlot(historyPanel);

        XYSeriesCollection histogramDataset = new XYSeriesCollection();
        histogramDataset.addSeries(histogram);
        barDataSet = new XYBarDataset(histogramDataset, 0.1);
        JFreeChart histogramChart = ChartFactory.createXYBarChart(
                "", // chart title
                "Rad", // domain axis label
                false,
                "Hit", // range axis label
                barDataSet, // data
                PlotOrientation.VERTICAL,
                false, // include legend
                false,
                false);


        ((NumberAxis) ((XYPlot) histogramChart.getPlot()).getDomainAxis()).setNumberFormatOverride(limitFormat);
        ChartPanel histogramPanel = new ChartPanel(histogramChart);
        setupPlot(histogramPanel);

        plotPanel.setLayout(new GridLayout(2, 1));
        plotPanel.add(historyPanel);
        plotPanel.add(histogramPanel);

        pushButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                pushDetectorLimit();
            }
        });
        pushAllButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                pushAllDetectorLimit();
            }
        });
        revertButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                revertLimits();
            }
        });


//        signalTypeCombo.addActionListener(new ActionListener() {
//
//            public void actionPerformed(ActionEvent e) {
//                setSignalType(signalTypeCombo.getSelectedIndex());
//            }
//        });



        FocusListener fl = new FocusAdapter() {

            public void focusLost(FocusEvent ev) {
                checkLimitTF();
            }
        };

        ActionListener al = new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                checkLimitTF();
            }
        };

        minLimitTF.addFocusListener(fl);
        minLimitTF.addActionListener(al);
        maxLimitTF.addFocusListener(fl);
        maxLimitTF.addActionListener(al);



        setLimit(minLimitSw, maxLimitSw);
        view.addViewListener(this);

    }
    public static final int PULSE_LOSS = 0;
    public static final int SECOND_LOSS = 1;
    private int signalType = SECOND_LOSS;

    public int getSignalType() {
        return signalType;
    }

    public void setSignalType(int selectedIndex) {

        signalType = selectedIndex;

        switch (signalType) {
            case PULSE_LOSS:
                setLimit(minLimitHw, maxLimitHw);
                minLimitTF.setEditable(true);
                maxLimitTF.setEditable(true);
                break;
            case SECOND_LOSS:
                setLimit(minLimitSw, maxLimitSw);
                minLimitTF.setEditable(true);
                maxLimitTF.setEditable(true);
                break;
            default:
                break;
        }

        fireDataChanged();

    }

    private void checkLimitTF() {
        String newMin = minLimitTF.getText();
        String newMax = maxLimitTF.getText();
        try {
            setLimit(Double.parseDouble(newMin), Double.parseDouble(newMax));
        } catch (NumberFormatException nfe) {
            switch (signalType) {
                case PULSE_LOSS:
                    setLimit(minLimitHw, maxLimitHw);
                    break;
                case SECOND_LOSS:
                    setLimit(minLimitSw, maxLimitSw);
                    break;
                default:
                    break;
            }
        }
    }

    public String dataLabel() {
        return "MPSPanel";
    }

    public void fireDataChanged() {

        if (showingDetector == null) {
            return;
        }

        SignalHistory sh;

        switch (signalType) {
            case PULSE_LOSS:
                sh = showingDetector.getHistory("PulseLoss");
                break;
            case SECOND_LOSS:
                sh = showingDetector.getHistory("Slow60");
                break;
            default:
                return;
        }

        if (sh == null) {
            return;
        }

        history.setNotify(false);
        histogram.setNotify(false);

        prepareHistory(sh);
        prepareHistogram(sh);

        histogram.setNotify(true);
        history.setNotify(true);

        updateLabels(calculateStats(showingDetector));


    }

    public void update(DataAdaptor adaptor) {
        DataAdaptor da = adaptor.childAdaptor("MinimumLimit");
        if (da != null) {
            try {
                minLimitSw = da.doubleValue("value");
            } catch (Exception e) {
            }
        }
        da = adaptor.childAdaptor("MaximumLimit");
        if (da != null) {
            try {
                maxLimitSw = da.doubleValue("value");
            } catch (Exception e) {
            }
        }

        da = adaptor.childAdaptor("MinimumHwLimit");
        if (da != null) {
            try {
                minLimitHw = da.doubleValue("value");
            } catch (Exception e) {
            }
        }
        da = adaptor.childAdaptor("MaximumHwLimit");
        if (da != null) {
            try {
                maxLimitHw = da.doubleValue("value");
            } catch (Exception e) {
            }
        }

        da = adaptor.childAdaptor("SignalType");
        if (da != null) {
            try {
                signalType = da.intValue("value");
            } catch (Exception e) {
            }
        }

        setSignalType(signalType);

    }

    public void write(DataAdaptor adaptor) {
        if (minLimitSw != DEFAULT_MIN_LIMIT) {
            DataAdaptor da = adaptor.createChild("MinimumLimit");
            da.setValue("value", minLimitSw);
        }
        if (maxLimitSw != DEFAULT_MAX_LIMIT) {
            DataAdaptor da = adaptor.createChild("MaximumLimit");
            da.setValue("value", maxLimitSw);
        }
        if (minLimitHw != DEFAULT_MIN_HW_LIMIT) {
            DataAdaptor da = adaptor.createChild("MinimumHwLimit");
            da.setValue("value", minLimitHw);
        }
        if (maxLimitHw != DEFAULT_MAX_HW_LIMIT) {
            DataAdaptor da = adaptor.createChild("MaximumHwLimit");
            da.setValue("value", maxLimitHw);
        }

        DataAdaptor da = adaptor.createChild("SignalType");
        da.setValue("value", signalType);

    }

    public void addSelectionListener(SelectionHandler<LossDetector> s) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void removeSelectionListener(SelectionHandler<LossDetector> s) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void removeAllSelectionListeners() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void fireSelectionUpdate(SelectionEvent<LossDetector> event) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void processSelectionEvent(SelectionEvent<LossDetector> event) {
        if (event.contains(this)) {
            return;
        }
        event.addProcessedHandler(this);
        if (event.getSelection().isEmpty()) {
            return;
        }
        LossDetector detector = event.getSelection().iterator().next();

        if (detector != null) {
            title.setText(detector.getName());
            showingDetector = detector;
            fireDataChanged();
        }

    }

    public Collection<LossDetector> getSelection() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setSelection(Collection<LossDetector> se) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public View<LossDetector> getRoot() {
        return view;
    }
    private static final int HISTOGRAMSIZE = 60;

    private void prepareHistogram(SignalHistory sh) {
        histogram.clear();
        double minimumValue = Double.POSITIVE_INFINITY;
        double maximumValue = Double.NEGATIVE_INFINITY;
        for (int i = 0; i < sh.size(); i++) {
            ScalarSignalValue sv = (ScalarSignalValue) sh.get(i);
            if (sv == null) {
                histogram.clear();
                return;
            }
            double value = sv.getValue();
            if (value > maximumValue) {
                maximumValue = value;
            }
            if (value < minimumValue) {
                minimumValue = value;
            }

        }
        double delta = maximumValue - minimumValue;
        double step = delta / HISTOGRAMSIZE;

        for (int i = 0; i < HISTOGRAMSIZE + 1; i++) {
            histogram.add(new XYDataItem(minimumValue + (i + 0.5) * step, 0.0));
        }
        barDataSet.setBarWidth(step);

        for (int i = 0; i < sh.size(); i++) {
            ScalarSignalValue sv = (ScalarSignalValue) sh.get(i);
            if (sv == null) {
                histogram.clear();
                return;
            }
            double value = sv.getValue();
            int binNumber = (int) ((value - minimumValue) / (step));
            double binValue = (Double) (histogram.getY(binNumber)) + 1;
            histogram.updateByIndex(binNumber, binValue);
        }

    }
    private DecimalFormat sigmaFormat = new DecimalFormat("#.0");
    private DecimalFormat limitFormat = new DecimalFormat("0.##E0");

    private void prepareHistory(SignalHistory sh) {
        history.clear();
        for (int i = 0; i < sh.size(); i++) {
            ScalarSignalValue sv = (ScalarSignalValue) sh.get(i);
            if (sv == null) {
                break;
            }
            long tst = sv.getTimestamp();
            double value = sv.getValue();
            history.addOrUpdate(new Millisecond(new Date(tst)), value);
        }
    }
    private static double HI_LIMIT_FRACTION = 0.5;

    HistoryStats calculateStats(LossDetector ld) {

        SignalHistory sh = ld.getHistory("Slow60");
        HistoryStats stat = new HistoryStats();
        if(sh==null){
            return stat;
        }
        int size = sh.size();

        int nonZeroCount = sh.getNonZeroCount();

        double average = 0.0, sigma = 0.0, sq = 0.0;
        if (nonZeroCount == 0) {
            average = 0.0;
            sigma = 0.0;
        } else {
            average = sh.getSum() / nonZeroCount;
            sq = sh.getSumSquared() / nonZeroCount;
            sigma = Math.abs(Math.sqrt(sq - average * average) / average * 100);
        }

        Point2D range = sh.getRange();
        //proposed calculations

        double proposedLimit = Math.max(average * (1 + 4 * sigma / 100), 2 * average);
        proposedLimit = Math.max(range.getY() * 1.5, proposedLimit);
        double proposedLimitAlrm = average + (proposedLimit - average) * HI_LIMIT_FRACTION;


        if (proposedLimit == average * (1 + 4 * sigma / 100)) {
            stat.selectedStr = 0;
        } else if (proposedLimit == 2 * average) {
            stat.selectedStr = 1;
        } else if (proposedLimit == range.getY() * 1.5) {
            stat.selectedStr = 2;
        }


        stat.aver = average;
        stat.size = size;
        stat.counts = nonZeroCount;
        stat.sigma = sigma;
        stat.range = range;
        stat.propHIHI = proposedLimit;
        stat.propHI = proposedLimitAlrm;



        sh = ld.getHistory("PulseLoss");
        size = sh.size();

        nonZeroCount = sh.getNonZeroCount();
        if (nonZeroCount == 0) {
            average = 0.0;
            sigma = 0.0;
        } else {
            average = sh.getSum() / nonZeroCount;
            sq = sh.getSumSquared() / nonZeroCount;
            sigma = Math.abs(Math.sqrt(sq - average * average) / average * 100);
        }
        range = sh.getRange();
        //proposed calculations        
        proposedLimit = Math.max(average * (1 + 4 * sigma / 100), 2 * average);
        proposedLimit = Math.max(range.getY() * 1.5, proposedLimit);


        if (proposedLimit == average * (1 + 4 * sigma / 100)) {
            stat.selectedStrHw = 0;
        } else if (proposedLimit == 2 * average) {
            stat.selectedStrHw = 1;
        } else if (proposedLimit == range.getY() * 1.5) {
            stat.selectedStrHw = 2;
        }



        stat.averHw = average;
        stat.sizeHw = size;
        stat.countsHw = nonZeroCount;
        stat.sigmaHw = sigma;
        stat.rangeHw = range;
        stat.propHw = proposedLimit;


        return stat;
    }

    public void setLimit(double min, double max) {
        minLimitTF.setText(limitFormat.format(min));
        maxLimitTF.setText(limitFormat.format(max));
        switch (signalType) {
            case PULSE_LOSS:
                minLimitHw = min;
                maxLimitHw = max;
                break;
            case SECOND_LOSS:
                minLimitSw = min;
                maxLimitSw = max;
                break;
            default:
                break;
        }


    }
    private final String[] strategies = new String[]{"4*S", "2*Av", "1.5*HI"};

    private void updateLabels(HistoryStats stats) {

        double sigma = 0.0, aver = 0.0, limit = 0.0, proposedLimit = 0.0;
        int size = 0, counts = 0;
        Point2D range = null;

        int selectedStr = -1;


        switch (signalType) {
            case PULSE_LOSS:
                counts = stats.countsHw;
                size = stats.sizeHw;
                sigma = stats.sigmaHw;
                aver = stats.averHw;
                range = stats.rangeHw;
                limit = ((ScalarSignalValue) showingDetector.getValue("HwTrip")).getValue();
                proposedLimit = stats.propHw;
                selectedStr = stats.selectedStrHw;
                break;
            case SECOND_LOSS:
                counts = stats.counts;
                size = stats.size;
                sigma = stats.sigma;
                aver = stats.aver;
                range = stats.range;
                limit = ((ScalarSignalValue) showingDetector.getValue("SwTrip")).getValue();
                proposedLimit = stats.propHIHI;
                selectedStr = stats.selectedStr;
                break;
            default:
                return;

        }
        String selectedStrString = "";
        if (selectedStr != -1) {
            selectedStrString = strategies[selectedStr];
        }

        historySizeTF.setText(counts + "/" + size);


        //sigmaTF.setText(sigmaFormat.format(sigma));
        setTextInTextField(sigmaFormat.format(sigma), sigmaTF);


        //averageTF.setText(limitFormat.format(average));
        setTextInTextField(limitFormat.format(aver), averageTF);

        //minTF.setText(limitFormat.format(range.getX()));
        setTextInTextField(limitFormat.format(range.getX()), minTF);

        //maxTF.setText(limitFormat.format(range.getY()));
        setTextInTextField(limitFormat.format(range.getY()), maxTF);


        //limitTF.setText(limitFormat.format(limit));
        setTextInTextField(limitFormat.format(limit), limitTF);


        //propLimitTF.setText(limitFormat.format(propLimit));
        setTextInTextField(selectedStrString + " " + limitFormat.format(proposedLimit), propLimitTF);


    }

    private void setTextInTextField(String text, JTextField tf) {
        if (tf == null) {
            return;
        }
        if (text.equals(tf.getText())) {
            return;
        }
        tf.setText(text);

//        int start = tf.getSelectionStart();
//        int end = tf.getSelectionEnd();

//        tf.setSelectionStart(start);
//        tf.setSelectionEnd(end);

    }
    private static final double DEFAULT_MIN_LIMIT = 0.0;
    private static final double DEFAULT_MAX_LIMIT = 0.2;
    private static final double DEFAULT_MIN_HW_LIMIT = 1.0E-3;
    private static final double DEFAULT_MAX_HW_LIMIT = 3.0E-3;
    private double minLimitSw = DEFAULT_MIN_LIMIT;
    private double maxLimitSw = DEFAULT_MAX_LIMIT;
    private double minLimitHw = DEFAULT_MIN_HW_LIMIT;
    private double maxLimitHw = DEFAULT_MAX_HW_LIMIT;
    private List<LimitValue> oldLimits = new ArrayList<LimitValue>();

    public List<LimitValue> getOldLimits() {
        return oldLimits;
    }

    private void pushDetectorLimit() {
        if (showingDetector == null) {
            return;
        }
        pushDetectorLimit(Arrays.asList(new LossDetector[]{showingDetector}));
    }

    private void pushAllDetectorLimit() {

        List<LossDetector> dets = new ArrayList<LossDetector>();
        for (Object ld : view.getDetectors()) {
            LossDetector detector = (LossDetector) ld;
            dets.add(detector);
        }
        pushDetectorLimit(dets);

    }

    private void pushDetectorLimit(List<LossDetector> detectors) {
        List<LimitValue> dets = new ArrayList<LimitValue>(detectors.size());

        for (LossDetector detector : detectors) {
            HistoryStats hs = calculateStats(detector);
            dets.add(new LimitValue(detector, hs.propHIHI, hs.propHI, hs.propHw));
        }
        SwingWorker<Boolean,Void> sw = new PushWorker(this, signalType == PULSE_LOSS, dets, minLimitSw, maxLimitSw, minLimitHw, maxLimitHw);
        pushButton.setEnabled(false);
        pushAllButton.setEnabled(false);
        revertButton.setEnabled(false);
        sw.execute();

    }

    private void revertLimits() {
        SwingWorker<Boolean,Void> sw = new PushWorker(this, signalType == PULSE_LOSS, oldLimits, minLimitSw, maxLimitSw, minLimitHw, maxLimitHw, true);
        pushButton.setEnabled(false);
        pushAllButton.setEnabled(false);
        revertButton.setEnabled(false);
        sw.execute();


    }

    protected void finishPut(boolean result, boolean reverting) {
        if (!result) {

            statusTF.setForeground(Color.RED);
            statusTF.setText("PUSH FAILED");
        } else {
            statusTF.setForeground(Color.GREEN.darker().darker());
            statusTF.setText("PUSH OK");
        }
        pushButton.setEnabled(true);
        pushAllButton.setEnabled(true);
        revertButton.setEnabled(!reverting);
    }

    public void processViewEvent(ViewEvent event) {
        if ("switchloss".equals(event.getCommand())) {
            String lossSig = (String) (event.getArgument());

            if ("PulseLoss".equals(lossSig)) {
                setSignalType(MPSPanel.PULSE_LOSS);

            } else if ("Slow60".equals(lossSig)) {
                setSignalType(MPSPanel.SECOND_LOSS);
            }

        }
    }
}

class HistoryStats {

    int size, counts;
    int sizeHw, countsHw;
    int selectedStr = -1;
    int selectedStrHw = -1;
    double aver, sigma, propHIHI, propHI;
    double averHw, sigmaHw, propHw;
    Point2D range, rangeHw;
}

class LimitValue {

    LimitValue(LossDetector ld, double l, double l2, double l3) {
        this.detector = ld;
        this.limitHIHI = l;
        this.limitHI = l2;
        this.limitHw = l3;
    }
    LossDetector detector;
    double limitHIHI, limitHI, limitHw;
}

class PushWorker extends SwingWorker<Boolean,Void> {

    private double minLimit;
    private double maxLimit;
    boolean pushingAll = false;
    private List<LimitValue> lds;
    private boolean reverting = false;
    private List<LimitValue> oldLimits;
    private MPSPanel panel;
    private double minLimitHw;
    private double maxLimitHw;
    private boolean isHW;

    PushWorker(MPSPanel panel, boolean isHW, List<LimitValue> lds, double minLimit, double maxLimit, double minLimitHw, double maxLimitHw) {
        this(panel, isHW, lds, minLimit, maxLimit, minLimitHw, maxLimitHw, false);
    }

    PushWorker(MPSPanel panel, boolean isHW, List<LimitValue> lds, double minLimit, double maxLimit, double minLimitHw, double maxLimitHw, boolean reverting) {
        init(panel, isHW, lds, minLimit, maxLimit, minLimitHw, maxLimitHw, reverting);
    }

    private void init(MPSPanel panel, boolean isHW, List<LimitValue> lds, double minLimit, double maxLimit, double minLimitHw, double maxLimitHw, boolean reverting) {
        pushingAll = true;
        this.reverting = reverting;
        this.lds = lds;
        this.minLimit = minLimit;
        this.maxLimit = maxLimit;
        this.minLimitHw = minLimitHw;
        this.maxLimitHw = maxLimitHw;
        oldLimits = panel.getOldLimits();
        this.panel = panel;
        this.isHW = isHW;
    }

    @Override
    public Boolean doInBackground() {

        Boolean result = true;
        if (!reverting) {
            oldLimits.clear();
        }
        if (pushingAll) {
            for (LimitValue ld : lds) {
                if (!reverting) {
                    HistoryStats stats = panel.calculateStats(ld.detector);
                    double newLimit = (stats.propHIHI > minLimit) ? stats.propHIHI : minLimit;
                    newLimit = (stats.propHIHI < maxLimit) ? newLimit : maxLimit;
                    double newLimit2 = (stats.propHI > minLimit) ? stats.propHI : minLimit;
                    newLimit2 = (stats.propHI < maxLimit) ? newLimit2 : maxLimit;

                    double newLimit3 = (stats.propHw > minLimitHw) ? stats.propHw : minLimitHw;
                    newLimit3 = (newLimit3 < maxLimitHw) ? newLimit3 : maxLimitHw;



                    double currentLimit = ((ScalarSignalValue) (ld.detector.getValue("SwTrip"))).getValue();
                    double currentLimit2 = ((ScalarSignalValue) (ld.detector.getValue("SwTrip2"))).getValue();
                    double currentLimitHw = ((ScalarSignalValue) (ld.detector.getValue("HwTrip"))).getValue();

                    oldLimits.add(new LimitValue(ld.detector, currentLimit, currentLimit2, currentLimitHw));
                    if (newLimit < 0) {
                        newLimit = currentLimit;
                    }
                    if (newLimit2 < 0) {
                        newLimit2 = currentLimit2;
                    }
                    System.out.println("Pushing" + " " + ld.detector.getName() + " " + newLimit + " " + newLimit2 + " " + newLimit3);

                    if (!isHW) {
                        result = result && ld.detector.setValue("SwTrip", newLimit);
                        result = result && ld.detector.setValue("SwTrip2", newLimit2);
                    } else {
                        result = result && ld.detector.setValue("HwTripSet", newLimit3);
                    }

                } else {
                    System.out.println("Reverting" + " " + ld.detector.getName() + " " + ld.limitHIHI + " " + ld.limitHI + " " + ld.limitHw);
                    if (!isHW) {
                        result = result && ld.detector.setValue("SwTrip", ld.limitHIHI);
                        result = result && ld.detector.setValue("SwTrip2", ld.limitHI);
                    } else {
                        result = result && ld.detector.setValue("HwTripSet", ld.limitHw);
                    }
                }


            }
        }

        if (reverting) {
            oldLimits.clear();
        }

        System.out.println("Pushing in panel " + result);

        return result;
    }

    @Override
    public void done() {
        boolean result=false;
        try {
            result = get();
        } catch (InterruptedException ex) {
            Logger.getLogger(PushWorker.class.getName()).log(Level.WARNING, "Error while calculating limits");
        } catch (ExecutionException ex) {
            Logger.getLogger(PushWorker.class.getName()).log(Level.WARNING, "Error while calculating limits");
        }
        System.out.println("Pushing in finished " + result);
        panel.finishPut(result, reverting);


    }
}
