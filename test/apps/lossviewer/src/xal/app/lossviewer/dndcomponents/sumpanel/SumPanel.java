/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package xal.app.lossviewer.dndcomponents.sumpanel;

import xal.app.lossviewer.LossDetector;
import xal.app.lossviewer.dndcomponents.mpspanel.MPSPanel;
import xal.app.lossviewer.signals.ScalarSignalValue;
import xal.app.lossviewer.signals.SignalHistory;
import xal.app.lossviewer.views.SumView;
import xal.app.lossviewer.views.View;
import xal.app.lossviewer.views.ViewEvent;
import xal.app.lossviewer.views.ViewListener;

import java.awt.BorderLayout;
import java.text.DecimalFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import javax.swing.JTextField;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.time.Millisecond;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.time.TimeSeriesDataItem;
import xal.tools.data.DataAdaptor;
import xal.tools.data.DataListener;

/**
 *
 * @author azukov
 */
public class SumPanel extends SumPanelNB implements DataListener, ViewListener {
    private static final long serialVersionUID = -589776116322060246L;

    private View<LossDetector> view;
    private TimeSeriesCollection historyDataSet;
    private TimeSeries history;
    private TimeSeries runningAverage;
    private static final int RUNNINGPERIOD = 50;
    private String lossSignal = "Slow60";

    public SumPanel(View<LossDetector> v) {
        super();
        this.view = v;

        historyDataSet = new TimeSeriesCollection();
        history = new TimeSeries("", Millisecond.class);
        runningAverage = new TimeSeries("", Millisecond.class);

        historyDataSet.addSeries(runningAverage);
        historyDataSet.addSeries(history);

        JFreeChart historychart = ChartFactory.createTimeSeriesChart("Average History", "", "Rad", historyDataSet, false, false, false);
        ((NumberAxis) ((XYPlot) historychart.getPlot()).getRangeAxis()).setNumberFormatOverride(limitFormat);
        ChartPanel historyPanel = new ChartPanel(historychart);

        MPSPanel.setupPlot(historyPanel);

        plotPanel.setLayout(new BorderLayout());
        plotPanel.add(historyPanel);
        view.addViewListener(this);


    }

    public String dataLabel() {
        return "SumPanel";
    }

    public void fireDataChanged() {



        history.setNotify(false);
        runningAverage.setNotify(false);

        //      System.out.println("History "+System.currentTimeMillis());
        prepareHistory();
        updateLabels();



        history.setNotify(true);
        runningAverage.setNotify(true);



    }

    public String getLossSignal() {
       return lossSignal;
    }

    public void switchLoss(String type) {
        lossSignal = type;
            fireDataChanged();
    }

    public void update(DataAdaptor adaptor) {

    }

    public void write(DataAdaptor adaptor) {

    }
    private static final int HISTOGRAMSIZE = 60;
    private static DecimalFormat sigmaFormat = new DecimalFormat("#.0");
    private static DecimalFormat limitFormat = new DecimalFormat("0.##E0");
//    private double lastRunningAverage = 0.0;
    private long oldTst = 0;

    private void prepareHistory() {
        Set<LossDetector> dets = view.getDetectors();


        LossDetector longestDet = null;
        int maxSize = Integer.MIN_VALUE;
        for (LossDetector d : dets) {
            SignalHistory sh = d.getHistory(lossSignal);
            if(sh!=null){
                int size = sh.size();            
                if (maxSize < size) {
                    maxSize = size;
                    longestDet = d;
                }
            }
        }
        if(longestDet==null)
            return;

        SignalHistory longetsHistory = longestDet.getHistory(lossSignal);

        long[] timestamps = new long[longetsHistory.size()];
        double[] values = new double[longetsHistory.size()];
        double[] totalW = new double[longetsHistory.size()];

        for (int i = 0; i < longetsHistory.size(); i++) {
            timestamps[i] = longetsHistory.getBackWard(i).getTimestamp();
            values[i] = 0.0;
        }

        for (LossDetector d : dets) {
            double w = ((SumView) view).getWeight(d.getName());
            SignalHistory sh = d.getHistory(lossSignal);
            if(sh==null)
                continue;
            for (int i = 0; i < sh.size(); i++) {
                
                long tst = 0;
                ScalarSignalValue sv;
                double v = 0;
                boolean done = false;

                int index = i;
                if (sh.size() > index) {
                    sv = (ScalarSignalValue) sh.getBackWard(index);
                    if(sv==null)
                        continue;
                    tst = sv.getTimestamp();
                    if (tst == timestamps[i]) {
                        v = sv.getValue();
                        done = true;
                    }
                }
                index = i - 1;
                if (!done && sh.size() > index&&index>0) {
                    
                    sv = (ScalarSignalValue) sh.getBackWard(index);
                    if(sv==null)
                        continue;
                    tst = sv.getTimestamp();
                    if (tst == timestamps[i]) {
                        v = sv.getValue();
                        done = true;
                    }
                }
                index = i + 1;
                if (!done && sh.size() > index ) {
                    
                    sv = (ScalarSignalValue) sh.getBackWard(index);
                    if(sv==null)
                        continue;
                    tst = sv.getTimestamp();
                    if (tst == timestamps[i]) {
                        v = sv.getValue();
                        done = true;
                    }
                }

                if (done) {
                    values[i] += v * w;
                    totalW[i] += w;
                }
            }



        }


        history.clear();
        runningAverage.clear();
        double lastRunningAverage=0;
        int length = timestamps.length;
        for (int i = 0; i < length; i++) {
            Millisecond t = new Millisecond(new Date(timestamps[length-i-1]));
            double newValue = values[length-i-1] / totalW[length-i-1];
            history.add(t, newValue);
            
            lastRunningAverage =lastRunningAverage + newValue;

            if (i == RUNNINGPERIOD-1) {
                runningAverage.add(t, lastRunningAverage/RUNNINGPERIOD);
            } else if (i >= RUNNINGPERIOD) {
                double obsoleteV = values[length-i-1+RUNNINGPERIOD] / totalW[length-i-1+RUNNINGPERIOD];
                lastRunningAverage = lastRunningAverage - obsoleteV;
                runningAverage.add(t, lastRunningAverage/RUNNINGPERIOD);
            }
        }

        
//
        

    }
    @SuppressWarnings("unchecked")
    private void runnAv() {

        List<TimeSeriesDataItem> items = (List<TimeSeriesDataItem>) history.getItems();
        if (items.size() < RUNNINGPERIOD) {
            return;
        }
        double runningValue = 0;

        for (int i = 0; i < RUNNINGPERIOD; i++) {
            runningValue += (Double) items.get(i).getValue();
        }
        runningAverage.addOrUpdate(items.get(RUNNINGPERIOD - 1).getPeriod(), runningValue / RUNNINGPERIOD);
        for (int i = RUNNINGPERIOD; i < items.size(); i++) {
            runningValue += (Double) items.get(i).getValue();
            runningValue -= (Double) items.get(i - RUNNINGPERIOD).getValue();
            runningAverage.addOrUpdate(items.get(i).getPeriod(), runningValue / RUNNINGPERIOD);

        }



    }

    private void updateLabels() {

        runningPeriodTF.setText("" + history.getItemCount());
     

    }

    private void setTextInTextField(String text, JTextField tf) {
        if (tf == null) {
            return;
        }
        if (text.equals(tf.getText())) {
            return;
        }
        int start = tf.getSelectionStart();
        int end = tf.getSelectionEnd();
        tf.setText(text);
        tf.setSelectionStart(start);
        tf.setSelectionEnd(end);

    }
    Map<Long, Double> histValues = new HashMap<Long, Double>(600);
    Map<Long, Double> histWeights = new HashMap<Long, Double>(600);

    private void hist(SignalHistory longestHistory, Set<LossDetector> dets) {
        hist1(longestHistory);
        hist2(dets);
        hist3();
    }

    private void hist1(SignalHistory longestHistory) {

        for (int i = 0; i < longestHistory.size(); i++) {
            long tst = longestHistory.get(i).getTimestamp();
            histValues.put(tst, 0.0);
            histWeights.put(tst, 0.0);
        }
    }

    private void hist2(Set<LossDetector> dets) {


        for (LossDetector d : dets) {
            SignalHistory sh = d.getHistory(lossSignal);
            double w = ((SumView) view).getWeight(d.getName());
            for (int i = 0; i < sh.size(); i++) {
                ScalarSignalValue sv = (ScalarSignalValue) sh.get(i);
                long tst = sv.getTimestamp();
                double v = sv.getValue();
                Double weight = histWeights.get(tst);
                Double value = histValues.get(tst);
                if (weight != null) {
                    histWeights.put(tst, w + weight);
                    histValues.put(tst, value + v * w);
                }
            }
        }
    }

    private void hist3() {


        for (long tst : histWeights.keySet()) {
            double value = histValues.get(tst) / histWeights.get(tst);
            history.add(new Millisecond(new Date(tst)), value, false);
        }
    }

    private void prepareHistory0() {
        history.clear();
        runningAverage.clear();

        Set<LossDetector> dets = view.getDetectors();

        int maxSize = Integer.MIN_VALUE;
        SignalHistory longestHistory = null;
        for (LossDetector d : dets) {
            SignalHistory his = d.getHistory(lossSignal);

            int size = his.size();
            if (size > maxSize) {
                maxSize = size;
                longestHistory = his;

            }
        }

        histValues.clear();
        histWeights.clear();

        hist(longestHistory, dets);
        runnAv();


    }

    public void processViewEvent(ViewEvent event) {
        if("switchloss".equals(event.getCommand())){
           switchLoss((String)(event.getArgument()));
           
        }
    }
}
