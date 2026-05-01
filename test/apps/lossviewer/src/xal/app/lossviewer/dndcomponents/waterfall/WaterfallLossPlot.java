/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package xal.app.lossviewer.dndcomponents.waterfall;

import xal.app.lossviewer.LossDetector;
import xal.app.lossviewer.signals.ScalarSignalValue;
import xal.app.lossviewer.signals.SignalEvent;
import xal.app.lossviewer.signals.SignalHistory;
import xal.app.lossviewer.views.View;
import xal.app.lossviewer.views.ViewEvent;
import xal.app.lossviewer.views.ViewListener;
import xal.app.lossviewer.views.WaterView;
import xal.app.lossviewer.waterfall.ColorLegend;
import xal.app.lossviewer.waterfall.DataPoint;
import xal.app.lossviewer.waterfall.DataSlice;
import xal.app.lossviewer.waterfall.DefaultWaterfallData;
import xal.app.lossviewer.waterfall.WaterfallData;
import xal.app.lossviewer.waterfall.WaterfallPanel;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Set;
import javax.swing.JPanel;
import xal.tools.data.DataAdaptor;
import xal.tools.data.DataListener;

/**
 *
 * @author az9
 */
public class WaterfallLossPlot extends JPanel implements ViewListener, DataListener {
    private static final long serialVersionUID = 2139482815413493560L;

    private WaterView view;
    private int HISTORY_SIZE = 600;
    private String signalType = "PulseLoss";
    private LossDetector[] detectors;
    private WaterfallData waterData;
    private static long TIME_TOLERANCE = 500;
    private Set<LossDetector> detSet;

    public WaterfallLossPlot(View<LossDetector> v) {
        super();
        this.view = (WaterView) v;
        view.addViewListener(this);
        initializePlot();

    }
   public void switchLossSignal(String lossSig) {
        signalType = lossSig;
        init(detSet);
    }
   public String getLossSignal(){
       return signalType;
   }
   
    public void processViewEvent(ViewEvent event) {
        if("switchloss".equals(event.getCommand())){
            switchLossSignal((String)(event.getArgument()));
        }
        else{
            init(event.getSource().getDetectors());
        }
        
    }
    WaterfallPanel waterfallPanel;

    public void signalUpdated(SignalEvent event) {
        double[] newVal = new double[detectors.length];
        int i = 0;
        for (LossDetector ld : detectors) {
            ScalarSignalValue sv = ((ScalarSignalValue) (ld.getValue(signalType)));
            if (sv != null) {
                newVal[i] = sv.getValue();
            }
            i++;
        }
        if (i == 0) {
            return;
        }
        long tst = detectors[0].getValue(signalType).getTimestamp();
        waterData.addData(new DataSlice(tst, newVal));

    }

    protected void init(Set<LossDetector> dets) {
        detSet = dets;
        detectors = new LossDetector[dets.size()];
        detectors = dets.toArray(detectors);

        if (dets == null || dets.size() == 0) {
            return;
        }
        waterData = createWaterData(detectors);
        waterfallPanel.setData(waterData);
    }


     ParamPanel paramPanel = null;
    protected void initializePlot() {
        setLayout(new BorderLayout());
        waterfallPanel = new WaterfallPanel(new DefaultWaterfallData(10, 10), 1E-6, 1E-3);

        add(waterfallPanel, BorderLayout.CENTER);
        paramPanel = new ParamPanel(waterfallPanel.getMax(), waterfallPanel.getMin(), true);

        GridBagConstraints gc = new GridBagConstraints();
        gc.gridx = 3;
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.insets = new Insets(2, 2, 2, 2);
        ColorLegend cl = new ColorLegend(waterfallPanel.getColorGenerator());
        cl.setPreferredSize(new Dimension(100, 20));
        cl.setMinimumSize(new Dimension(50, 20));
        paramPanel.add(cl, gc);
        

        add(paramPanel, BorderLayout.NORTH);

        waterfallPanel.addMouseListener(new MouseAdapter() {

            public void mouseClicked(MouseEvent ev) {
                DataPoint dp = waterfallPanel.getDataPointFromScreen(ev.getX(), ev.getY());
                paramPanel.setClickedName(dp);
            }
        });


    }

    public String dataLabel() {
        return "WaterfallPlot";
    }

    public void update(DataAdaptor adaptor) {
        DataAdaptor mvda = adaptor.childAdaptor("PlotSettings");
        boolean log = true;
        double min = 1E-5;
        double max = 5E-3;
        if (mvda != null) {

            if (mvda.hasAttribute("log")) {
                log = mvda.booleanValue("log");
            }
            if (mvda.hasAttribute("min")) {
                min = mvda.doubleValue("min");
            }
            if (mvda.hasAttribute("max")) {
                max = mvda.doubleValue("max");
            }
        }
        paramPanel.updatePlotSettings("" + max, "" + min, log);
    }

    public void write(DataAdaptor adaptor) {
        DataAdaptor mvda = adaptor.createChild("PlotSettings");
        mvda.setValue("min", waterfallPanel.getMin());
        mvda.setValue("max", waterfallPanel.getMax());
        mvda.setValue("log", waterfallPanel.isLogScaleUsed());
    }

    private WaterfallData createWaterData(LossDetector[] arr) {

        WaterfallData wd = new DefaultWaterfallData(arr.length, HISTORY_SIZE);

        SignalHistory sh = null;
        int maxSize = 0;
        int maxIndex = -1;
        for (int i = 0; i < arr.length; i++) {
            LossDetector ld = arr[i];
            sh = arr[i].getHistory(signalType);
            if (sh != null && sh.size() > maxSize) {
                maxSize = sh.size();
                maxIndex = i;
            }
            wd.setXName(i, ld.getShortName());

        }

        if (maxSize == 0) {
            return wd;
        }
        sh = arr[maxIndex].getHistory(signalType);

        long[] tst = new long[sh.size()];
        for (int i = 0; i < tst.length; i++) {
            tst[i] = sh.get(i).getTimestamp();
        }

        for (int i = 0; i < tst.length; i++) {
            double[] value = new double[arr.length];
            for (int j = 0; j < arr.length; j++) {
                SignalHistory hist = arr[j].getHistory(signalType);
                if (hist != null) {
                    value[j] = hist.findByTimestamp(tst[i], i, TIME_TOLERANCE);
                } else {
                    value[j] = 0.0;
                }
            }
            wd.addData(new DataSlice(tst[i], value));
        }
        //       System.out.println(wd);
        System.out.println(maxSize + " " + maxIndex);
        return wd;
    }

    private class ParamPanel extends WaterfallParamPanelNB {
        private static final long serialVersionUID = 6296740258310232913L;

        ParamPanel(double max, double min, boolean log) {
            super();
            maxTF.setText("" + max);
            minTF.setText("" + min);
            logCB.setSelected(log);
            ActionListener al = new ActionListener() {

                public void actionPerformed(ActionEvent e) {
                    updatePlotSettings(maxTF.getText(), minTF.getText(), logCB.isSelected());
                }


            };
            maxTF.addActionListener(al);
            minTF.addActionListener(al);
            logCB.addActionListener(al);
        }
        DateFormat df = new SimpleDateFormat("HH:mm:ss");
        DecimalFormat vf = new DecimalFormat("0.00E00");

        public void updatePlotSettings(String max, String min, boolean log) {
                    waterfallPanel.updateMaxMinLog(max,min,log);
                    maxTF.setText("" + waterfallPanel.getMax());
                    minTF.setText("" + waterfallPanel.getMin());
                    logCB.setSelected(waterfallPanel.isLogScaleUsed());

                }

        private void setClickedName(DataPoint dp) {

            blmNameLabel.setText(dp.name);
            if (dp.date != null) {
                blmTimeLabel.setText(df.format(dp.date));
            } else {
                blmTimeLabel.setText("");
            }
            blmValueLabel.setText(vf.format(dp.value));
            blmColorValueLabel.setBackground(dp.color);
            System.out.println(dp);
        }
    }
}
