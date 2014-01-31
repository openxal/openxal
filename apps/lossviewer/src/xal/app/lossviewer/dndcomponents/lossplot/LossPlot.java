package xal.app.lossviewer.dndcomponents.lossplot;

import xal.app.lossviewer.*;
import xal.app.lossviewer.preferences.*;
import xal.app.lossviewer.views.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import org.jfree.chart.*;
import org.jfree.chart.axis.*;
import org.jfree.chart.plot.*;
import org.jfree.chart.renderer.xy.*;

import java.util.List;
import org.jfree.chart.title.TextTitle;
import xal.tools.data.DataAdaptor;
import xal.tools.data.DataListener;

public class LossPlot extends JPanel implements ViewListener, DataListener {

    public static Paint AVERAGED_COLOR = Color.CYAN;
    private static final long serialVersionUID = 7533174382225104690L;
    private boolean reference;

    public String dataLabel() {

        return "LossPlot";
    }

    /**
     * Update the data based on the information provided by the data provider.
     * @param adaptor The adaptor from which to update the data
     */
    public void update(DataAdaptor adaptor) {
        try {
            List<DataAdaptor> ndas = adaptor.childAdaptor("Normalizations").childAdaptors("Normalization");
            for (DataAdaptor nda : ndas) {
                try {
                    normalizations.get(nda.stringValue("name")).setSelected(true);
                } catch (Exception ex) {
                    continue;
                }
            }
        } catch (Exception e) {
        }

        try {
            DataAdaptor ltda = adaptor.childAdaptor("LossType");
            currentLossType = ltda.stringValue("name");
            if (ltda.hasAttribute("averaged")) {
                averagedMenuItem.setSelected(ltda.booleanValue("averaged"));
            }
            lossTypes.get(currentLossType).setSelected(true);
        } catch (Exception ex) {
        }

        try {
            List<DataAdaptor> tds = adaptor.childAdaptors("TypeDataSet");
            for (DataAdaptor td : tds) {
                String type = td.stringValue("name");
                TypeDataSet typeData = typeSeries.get(type);
                if (typeData != null) {
                    typeData.update(td);
                }
            }
        } catch (Exception ex) {
        }
        updateNormalization();
        switchLossSignal(currentLossType);
//        switchLossSignal(averageSelected);





    }

    public void write(DataAdaptor adaptor) {
        DataAdaptor nda = adaptor.createChild("Normalizations");
        for (String norm : currentNormalization) {
            nda.createChild("Normalization").setValue("name", norm);
        }
        DataAdaptor ltda = adaptor.createChild("LossType");
        ltda.setValue("name", currentLossType);
        ltda.setValue("averaged", averageSelected);

        for (TypeDataSet tds : typeSeries.values()) {
            tds.write(adaptor.createChild("TypeDataSet"));
        }

    }
    private View<LossDetector> view;
    private XYPlot plot;
    private JFreeChart chart;
    ApplicationPreferences preferences;
    protected Set<LossDetector> currentDetectors;
    private String currentLossType = "PulseLoss";
    private String currentLossLimit = "HwTrip";

    public LossPlot(View<LossDetector> v) {
        super();
        this.view = v;

        view.addViewListener(this);
        setLayout(new BorderLayout());
        initializeLossTypes();
        initializeNormalization();
        initializeAxes();
        initializePlot();


    }

    protected void initializeLossTypes() {
        ButtonGroup grp = new ButtonGroup();
        Action a = new AbstractAction("1 pulse") {
            private static final long serialVersionUID = 4377386270269629176L;

            public void actionPerformed(ActionEvent e) {
                JRadioButtonMenuItem src = (JRadioButtonMenuItem) e.getSource();
                if (src.isSelected()) {
                    switchLossSignal("PulseLoss");
                }
            }
        };
        JRadioButtonMenuItem mi = new JRadioButtonMenuItem(a);
        grp.add(mi);
        lossTypes.put("PulseLoss", mi);
        limitTypes.put("PulseLoss", "HwTrip");
        limitTypes.put("PulseLossAVG", "HwTrip");
        plotTitles.put("PulseLoss", "1 Pulse Loss");
        plotTitles.put("PulseLossAVG", "1 Pulse Loss Averaged");
        mi.setSelected(true);

        a = new AbstractAction("1 second") {
            private static final long serialVersionUID = 4377386270269629176L;

            public void actionPerformed(ActionEvent e) {
                JRadioButtonMenuItem src = (JRadioButtonMenuItem) e.getSource();
                if (src.isSelected()) {
                    switchLossSignal("Slow60");
                }
            }
        };

        mi = new JRadioButtonMenuItem(a);
        grp.add(mi);
        lossTypes.put("Slow60", mi);
        limitTypes.put("Slow60", "SwTrip");
        limitTypes.put("Slow60AVG", "SwTrip");
        plotTitles.put("Slow60", "1 Second Integral Loss");
        plotTitles.put("Slow60AVG", "1 Second Integral Loss Averaged");

    }

    protected void initializeNormalization() {
        JMenuItem mi = new JCheckBoxMenuItem("Charge");
        normalizations.put("CHRG", mi);
        mi.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                updateNormalization();
            }
        });

        mi = new JCheckBoxMenuItem("Limit %");
        normalizations.put("LMT", mi);
        mi.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                updateNormalization();
            }
        });



        UnselectableButtonGroup grp = new UnselectableButtonGroup();

        mi = new JCheckBoxMenuItem("Distance");
        normalizations.put("DST1", mi);
        grp.add(mi);

        mi = new JCheckBoxMenuItem("Distance^2");
        normalizations.put("DST2", mi);
        grp.add(mi);


    }
    private Set<String> currentNormalization = new HashSet<String>();

    protected void setChartTitle() {
        String name = view.getReferenceName();
        if (name == null || "NONE".equals(name)||"".equals(name)) {
            name = "";
        } else {
            name = " # " + name;
        }
        TextTitle title = new TextTitle(view.getTitle() + name + " - " + plotTitles.get(currentLossType));
        title.setFont(title.getFont().deriveFont(18.0f));
        if (averageSelected) {
            title.setPaint(AVERAGED_COLOR);
        }
        chart.setTitle(title);
    }

    private void updateNormalization() {
        for (String e : normalizations.keySet()) {
            JMenuItem mi = normalizations.get(e);
            if (mi.isSelected()) {
                currentNormalization.add(e);
            } else {
                currentNormalization.remove(e);
            }
        }
        averageSelected = averagedMenuItem.isSelected();
        reinitialize();
        //	fireDataChanger();
    }
    Map<String, String> plotTitles = new HashMap<String, String>();

    private void switchLossSignal(boolean avrg) {
        averageSelected = avrg;
        if (averageSelected) {
            currentLossType = currentLossType + "AVG";
        } else if (currentLossType.contains("AVG")) {
            //delete AVG suffix
            int ps = currentLossType.indexOf("AVG");
            currentLossType = currentLossType.substring(0, ps);
        }

        reinitialize();
    }

    public void switchLossSignal(String lossSig) {
        if (averageSelected && !lossSig.contains("AVG")) {
            currentLossType = lossSig + "AVG";
        } else {
            currentLossType = lossSig;
        }


        reinitialize();
//		fireDataChanger();
    }

    public String getLossSignal() {
        if (currentLossType.contains("AVG")) {
            //delete AVG suffix
            int ps = currentLossType.indexOf("AVG");
            return currentLossType.substring(0, ps);
        }
        return currentLossType;
    }
    protected JPopupMenu plotPopupMenu;
    ArrayList<Action> standardAction = new ArrayList<Action>();

    public ChartPanel getChartPanel() {

        return chartPanel;
    }
    private boolean averageSelected = false;
    private JCheckBoxMenuItem averagedMenuItem;

    protected void initializePopup() {
        plotPopupMenu = new JPopupMenu();


        // 1 pulse vs 1 second loss
//        for (String name : lossTypes.keySet()) {
//            plotPopupMenu.add(lossTypes.get(name));
//        }

        Action avAction = new AbstractAction("Average") {
            private static final long serialVersionUID = 4377386270269629176L;

            public void actionPerformed(ActionEvent event) {
                switchLossSignal(((JCheckBoxMenuItem) (event.getSource())).isSelected());
            }
        };
        averagedMenuItem = new JCheckBoxMenuItem(avAction);
        averagedMenuItem.setSelected(averageSelected);
        plotPopupMenu.add(averagedMenuItem);
        averagedMenuItem.setEnabled(!reference);
        plotPopupMenu.addSeparator();

        JMenu normalizationMenu = new JMenu("Normalization");

        for (String e : normalizations.keySet()) {
            normalizationMenu.add(normalizations.get(e));
        }
        plotPopupMenu.add(normalizationMenu);
        plotPopupMenu.addSeparator();


        // individual menus for loss types
        for (String e : typeSeries.keySet()) {
            plotPopupMenu.add(typeSeries.get(e).getMenu());
        }
        plotPopupMenu.addSeparator();

        for (Action action : standardAction) {
            plotPopupMenu.add(action);
        }


        chartPanel.setPopupMenu(plotPopupMenu);

    }

    public void fireDataChanger() {
        for (TypeDataSet e : typeSeries.values()) {
            e.updateDetectors(currentLossType, !reference);
        }
    }

    protected void initializeAxes() {
        Color BLMCOLOR = Color.BLUE;
        NumberAxis axis = new NumberAxis("");
        axis.setLabelPaint(BLMCOLOR);
        typeAxes.put("BLM", axis);
        typeAxisLocations.put("BLM", AxisLocation.TOP_OR_LEFT);
        typeRenderers.put("BLM", BLMCOLOR);


        Color NDCOLOR = Color.GREEN;
        axis = new NumberAxis("");
        axis.setLabelPaint(NDCOLOR);

        typeAxes.put("ND", axis);
        typeAxisLocations.put("ND", AxisLocation.BOTTOM_OR_RIGHT);
        typeRenderers.put("ND", NDCOLOR);



    }
    private Color plotBackgroundPaint;
    private ChartPanel chartPanel;

    protected void initializePlot() {
        chart = ChartFactory.createXYBarChart(
                "",
                "BLM#",
                false,
                null,
                null,
                PlotOrientation.VERTICAL,
                true,
                true,
                false);

        chartPanel = new ChartPanel(chart, false);
        plotBackgroundPaint = ((Color) chart.getBackgroundPaint()).darker();
        chart.setBackgroundPaint(plotBackgroundPaint);

        chart.setAntiAlias(false);
        chartPanel.setMinimumDrawWidth(0);
        chartPanel.setMinimumDrawHeight(0);
        chartPanel.setMaximumDrawWidth(Integer.MAX_VALUE);
        chartPanel.setMaximumDrawHeight(Integer.MAX_VALUE);


        chartPanel.setPopupMenu(plotPopupMenu);

        add(chartPanel, BorderLayout.CENTER);
        plot = (XYPlot) chart.getPlot();
        plot.setBackgroundPaint(plotBackgroundPaint);
        plot.getRangeAxis().setVisible(false);

        plot.setDomainGridlinesVisible(false);
        plot.setRangeGridlinePaint(Color.LIGHT_GRAY);
        plot.setRangeGridlineStroke(new BasicStroke(0.5f));

        initializeActions();

    }

    protected void initializeActions() {
        standardAction.add(new AbstractAction("Scale All once") {
            private static final long serialVersionUID = 4377386270269629176L;

            public void actionPerformed(ActionEvent e) {
                for (TypeDataSet t : typeSeries.values()) {
                    t.scaleXandY();
                }
            }
        });
        standardAction.add(new AbstractAction("Autoscale All") {
            private static final long serialVersionUID = 4377386270269629176L;

            public void actionPerformed(ActionEvent e) {
                for (TypeDataSet t : typeSeries.values()) {
                    t.setYAutoScale(true);
                }
            }
        });
        standardAction.add(new AbstractAction("Freeze All") {
            private static final long serialVersionUID = 4377386270269629176L;

            public void actionPerformed(ActionEvent e) {
                for (TypeDataSet t : typeSeries.values()) {
                    t.setYAutoScale(false);
                }
            }
        });
    }

    public void processViewEvent(ViewEvent event) {
        if ("switchloss".equals(event.getCommand())) {
            switchLossSignal((String) (event.getArgument()));
        } else {
            if ("referenceON".equals(event.getCommand())) {
                reference = true;
            } else if ("referenceOFF".equals(event.getCommand())) {
                reference = false;
            }
            init(event.getSource().getDetectors());
        }
    }
    Map<String, TypeDataSet> typeSeries = new HashMap<String, TypeDataSet>();
    Map<String, NumberAxis> typeAxes = new HashMap<String, NumberAxis>();
    Map<String, Paint> typeRenderers = new HashMap<String, Paint>();
    Map<String, AxisLocation> typeAxisLocations = new HashMap<String, AxisLocation>();
    Map<String, JRadioButtonMenuItem> lossTypes = new HashMap<String, JRadioButtonMenuItem>();
    Map<String, JMenuItem> normalizations = new HashMap<String, JMenuItem>();
    Map<String, String> limitTypes = new HashMap<String, String>();

    public void reinitialize() {

        currentLossLimit = limitTypes.get(currentLossType);

        int num = plot.getDatasetCount();
        for (int i = 0; i < num; i++) {
            plot.setDataset(i, null);
        }
        int typeIndex = 0;
        for (String type : typeSeries.keySet()) {
            TypeDataSet data = typeSeries.get(type);
            if (data.isVisible()) {
                data.setAxis(typeAxes.get(type), typeRenderers.get(type),
                        typeAxisLocations.get(type));
                plot.setRangeAxis(typeIndex, data.getAxis());
                plot.setDataset(typeIndex, data);
                plot.mapDatasetToRangeAxis(typeIndex, typeIndex);
                plot.setRangeAxisLocation(typeIndex, data.getAxisLocation());

                plot.mapDatasetToDomainAxis(typeIndex, 0);

                XYBarRenderer rend = new XYBarRenderer();
                plot.setRenderer(typeIndex, rend);
                rend.setSeriesPaint(0, typeRenderers.get(type));
                rend.setMargin(0.1);

            }
            data.setNormalization(currentNormalization);

            typeIndex++;
            data.removeAllDetectors();
        }
        int detectorIndex = 0;

        for (LossDetector detector : currentDetectors) {
            TypeDataSet tds = typeSeries.get(detector.getType());
            if (tds.isVisible()) {
                tds.addDetector(detectorIndex, detector);
                detectorIndex++;
            }

        }


        SymbolAxis domainAxis = BLMAxisFactory.getBLMAxis(currentLossLimit, typeSeries, typeAxes);

        domainAxis.setVerticalTickLabels(true);
        domainAxis.setGridBandPaint(plotBackgroundPaint);
        plot.setDomainAxis(0, domainAxis);

        plot.setDomainAxis(0, domainAxis);


        setChartTitle();


        fireDataChanger();
    }

    protected void init(Set<LossDetector> detectors) {
        currentDetectors = detectors;
        for (TypeDataSet data : typeSeries.values()) {
            data.removeAllDetectors();
        }

        int typeIndex = 0;
        for (LossDetector detector : detectors) {
            String type = detector.getType();
            TypeDataSet data = typeSeries.get(type);
            if (data == null) {
                data = new TypeDataSet(type, this);
                typeSeries.put(type, data);
                typeIndex++;
            }
        }

        setChartTitle();
        switchLossSignal(averageSelected && !reference);
        initializePopup();
    }

    class UnselectableButtonGroup implements ActionListener {

        public void actionPerformed(ActionEvent e) {
            AbstractButton b = (AbstractButton) e.getSource();
            if (b.isSelected()) {
                for (AbstractButton button : buttons) {
                    if (button != b) {
                        button.setSelected(false);
                    }
                }
            }
            updateNormalization();
        }
        List<AbstractButton> buttons = new ArrayList<AbstractButton>();

        public void add(AbstractButton b) {
            buttons.add(b);
            b.addActionListener(this);
        }
    }
}
