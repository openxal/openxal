/*
 *  SigmaGraphPanel.java
 *
 *  Created on July 12, 2004
 */
package xal.app.bpmviewer;

import java.awt.Color;
import java.util.*;
import javax.swing.*;
import java.awt.*;
import javax.swing.border.*;
import java.text.*;

import xal.extension.widgets.plot.*;
import xal.extension.widgets.apputils.SimpleChartPopupMenu;
import xal.tools.apputils.*;
import xal.extension.widgets.swing.*;


/**
 *  The panel includes graph data and GUI elements for the dispersions of phase
 *  and positions arrays of BPM's signals.
 *
 *@author     shishlo
 */
public class SigmaGraphPanel {

    private JPanel panel = new JPanel();

    private TitledBorder border = null;

    private FunctionGraphsJPanel GP = null;

    private String graphName = null;

    private ValuesGraphPanel bpmPhaseGraphPanel = null;
    private ValuesGraphPanel bpmXposGraphPanel = null;
    private ValuesGraphPanel bpmYposGraphPanel = null;

    private BpmViewerDocument bpmViewer = null;

    private Vector<CurveData> graphV = new Vector<CurveData>();

    //format for numbers
    private DecimalFormat int_Format = new DecimalFormat("0");
    private DecimalFormat dbl_Format = new DecimalFormat("##0");

    //GUI elements
    private JLabel explLabel = new JLabel("1 - x-pos.  2 - y-pos.  3 - phase", JLabel.CENTER);


    /**
     *  Constructor for the SigmaGraphPanel object
     *
     *@param  graphNameIn           The name of the graph
     *@param  GPIn                  The graph panel by itself
     *@param  bpmPhaseGraphPanelIn  Description of the Parameter
     *@param  bpmXposGraphPanelIn   Description of the Parameter
     *@param  bpmYposGraphPanelIn   Description of the Parameter
     */
    public SigmaGraphPanel(
        String graphNameIn,
        ValuesGraphPanel bpmPhaseGraphPanelIn,
        ValuesGraphPanel bpmXposGraphPanelIn,
        ValuesGraphPanel bpmYposGraphPanelIn,
        FunctionGraphsJPanel GPIn) {

        graphName = graphNameIn;

        GP = GPIn;

        bpmPhaseGraphPanel = bpmPhaseGraphPanelIn;
        bpmXposGraphPanel = bpmXposGraphPanelIn;
        bpmYposGraphPanel = bpmYposGraphPanelIn;

        Border etchedBorder = BorderFactory.createEtchedBorder();
        border = BorderFactory.createTitledBorder(etchedBorder, graphNameIn);
        panel.setBorder(border);

        //Graph panel definition
        SimpleChartPopupMenu.addPopupMenuTo(GP);
        GP.setOffScreenImageDrawing(true);
        GP.setGraphBackGroundColor(Color.white);

        GP.setAxisNames("type index", "100*rms(z)");
        GP.setNumberFormatX(int_Format);
        GP.setNumberFormatY(dbl_Format);

        GP.setDraggingVerLinesGraphMode(true);
        GP.setLimitsAndTicksX(0., 1.0, 4, 0);
        GP.setLimitsAndTicksY(0., 50.0, 4, 4);

        //make the panel
        panel.setLayout(new BorderLayout());
        panel.setBackground(panel.getBackground().darker());

        JPanel tmp_panel_0 = new JPanel();
        tmp_panel_0.setBorder(etchedBorder);
        tmp_panel_0.setLayout(new BorderLayout());
        tmp_panel_0.add(explLabel, BorderLayout.CENTER);

        panel.add(GP, BorderLayout.CENTER);
        panel.add(tmp_panel_0, BorderLayout.SOUTH);
    }


    /**
     *  Sets the new font for all GUI elements
     *
     *@param  fnt  The new font
     */
    public void setAllFonts(Font fnt) {
        border.setTitleFont(fnt);
        explLabel.setFont(fnt);
    }


    /**
     *  Returns the panel
     *
     *@return    The panel
     */
    public JPanel getJPanel() {
        return panel;
    }


    /**
     *  Updates the graph data.
     */
    public void update() {

        Vector<BpmViewerPV> phV = bpmPhaseGraphPanel.getData();
        Vector<BpmViewerPV> xpV = bpmXposGraphPanel.getData();
        Vector<BpmViewerPV> ypV = bpmYposGraphPanel.getData();

        int nPhG = phV.size();
        int nXpG = xpV.size();
        int nYpG = ypV.size();
        int nTotal = nPhG + nXpG + nYpG;

        if (graphV.size() != nTotal) {
            graphV.removeAllElements();
            for (int i = 0, n = nTotal - graphV.size(); i < n; i++) {
                CurveData cd = new CurveData();
                cd.setLineWidth(4);
                graphV.add(cd);
            }
        }

        double phStep = 0.6 / (nPhG + 1);
        double phX0 = 2.7;
        double xpStep = 0.6 / (nXpG + 1);
        double xpX0 = 0.7;
        double ypStep = 0.6 / (nYpG + 1);
        double ypX0 = 1.7;

        double sigma = 0.;
        boolean isLimited = bpmPhaseGraphPanel.useLimits();
        double x_min = bpmPhaseGraphPanel.getMinLim();
        double x_max = bpmPhaseGraphPanel.getMaxLim();

        for (int i = 0, n = nPhG; i < n; i++) {
            BpmViewerPV bpmPV =  phV.get(i);
            CurveData cd = bpmPV.getGraphData();
            CurveData cdG = graphV.get(i);
            cdG.clear();
            if (!bpmPV.getArrayDataPV().getSwitchOn()) {
                continue;
            }
            sigma = calculateSigma(cd, isLimited, x_min, x_max);
            if (sigma == 0.) {
                continue;
            }
            cdG.addPoint(phX0 + phStep * (i + 1), 0.);
            cdG.addPoint(phX0 + phStep * (i + 1), sigma);
            cdG.setColor(cd.getColor());
        }

        sigma = 0.;
        isLimited = bpmXposGraphPanel.useLimits();
        x_min = bpmXposGraphPanel.getMinLim();
        x_max = bpmXposGraphPanel.getMaxLim();

        for (int i = 0, n = nXpG; i < n; i++) {
            BpmViewerPV bpmPV =  xpV.get(i);
            CurveData cd = bpmPV.getGraphData();
            CurveData cdG =  graphV.get(i + nPhG);
            cdG.clear();
            if (!bpmPV.getArrayDataPV().getSwitchOn()) {
                continue;
            }
            sigma = calculateSigma(cd, isLimited, x_min, x_max);
            if (sigma == 0.) {
                continue;
            }
            //sigma is [mm] than we move to 0-300 scale 
            cdG.addPoint(xpX0 + xpStep * (i + 1), 0.);
            cdG.addPoint(xpX0 + xpStep * (i + 1), sigma * 100.);
            cdG.setColor(cd.getColor());
        }

        sigma = 0.;
        isLimited = bpmYposGraphPanel.useLimits();
        x_min = bpmYposGraphPanel.getMinLim();
        x_max = bpmYposGraphPanel.getMaxLim();

        for (int i = 0, n = nYpG; i < n; i++) {
            BpmViewerPV bpmPV =  ypV.get(i);
            CurveData cd = bpmPV.getGraphData();
            CurveData cdG = graphV.get(i + nPhG + nXpG);
            cdG.clear();
            if (!bpmPV.getArrayDataPV().getSwitchOn()) {
                continue;
            }
            sigma = calculateSigma(cd, isLimited, x_min, x_max);
            if (sigma == 0.) {
                continue;
            }
            cdG.addPoint(ypX0 + ypStep * (i + 1), 0.);
            cdG.addPoint(ypX0 + ypStep * (i + 1), sigma * 100.);
            cdG.setColor(cd.getColor());
        }

        //add all Curves (vertical lines actually) to the graph
        Vector<CurveData> tmpV = new Vector<CurveData>(graphV);
        GP.setCurveData(tmpV);
    }


    /**
     *  Description of the Method
     *
     *@param  cd         CurveData instance
     *@param  isLimited  true or faulse
     *@param  x_min      min value for sigma calculations if limited
     *@param  x_max      max value for sigma calculations if limited
     *@return            sigma in the limits
     */
    private double calculateSigma(CurveData cd,
        boolean isLimited,
        double x_min,
        double x_max) {

        double sigma = 0.;

        double z_sum = 0.;
        double z2_sum = 0.;
        double z = 0.;
        double x = 0.;

        double n = cd.getSize();

        if (n == 0) {
            return sigma;
        }

        int count = 0;

        if (isLimited) {
            for (int i = 0; i < n; i++) {
                x = cd.getX(i);
                if (x >= x_min && x <= x_max) {
                    z = cd.getY(i);
                    z_sum += z;
                    z2_sum += z * z;
                    count++;
                }
            }
        } else {
            for (int i = 0; i < n; i++) {
                x = cd.getY(i);
                z = cd.getY(i);
                z_sum += z;
                z2_sum += z * z;
                count++;
            }
        }

        if (count <= 1) {
            return sigma;
        }

        z_sum /= count;

        sigma = (z2_sum - count * z_sum * z_sum) / (count);
        sigma = Math.sqrt(Math.abs(sigma));
        
        return sigma;
    }

}

