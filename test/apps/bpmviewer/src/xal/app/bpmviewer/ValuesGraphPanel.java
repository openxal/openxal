/*
 *  ValuesGraphPanel.java
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
import java.awt.event.*;

import xal.extension.widgets.plot.*;
import xal.extension.widgets.apputils.SimpleChartPopupMenu;
import xal.tools.apputils.*;
import xal.extension.widgets.swing.*;
import xal.tools.xml.*;


/**
 *  The panel includes graph data and GUI elements for phase, x and y-positions
 *  for BPM's signals.
 *
 *@author     shishlo
 *@version    1.0
 */
public class ValuesGraphPanel {

	private JPanel panel = new JPanel();

	private TitledBorder border = null;

	private FunctionGraphsJPanel GP = null;

	private Vector<BpmViewerPV> bpmPVs = null;

	private CurveData[] gdCrvArr = new CurveData[0];

	private String graphName = null;

	private BpmViewerDocument bpmViewer = null;

	//GUI elements

	//format for numbers
	private DecimalFormat int_Format = new DecimalFormat("###0");
	private DecimalFormat dbl_Format = new DecimalFormat("0.00E0");

	private JLabel sampleStart_Label = new JLabel("From:");
	private JLabel sampleStop_Label = new JLabel("To:");

	private DoubleInputTextField sampleStart_Text = new DoubleInputTextField(4);
	private DoubleInputTextField sampleStop_Text = new DoubleInputTextField(4);

	private ActionListener dragVerLine_Listener = null;

	private JRadioButton use_Button = new JRadioButton("Use");


	/**
	 *  Constructor for the ValuesGraphPanel object
	 *
	 *@param  graphNameIn  The name of the graph
	 *@param  bpmPVsIn     The vector with BpmViewerPV references
	 *@param  GPIn         The graph panel by itself
	 *@param  bpmViewerIn  Description of the Parameter
	 */
	public ValuesGraphPanel(
			String graphNameIn,
			Vector<BpmViewerPV> bpmPVsIn,
			FunctionGraphsJPanel GPIn,
			BpmViewerDocument bpmViewerIn) {

		graphName = graphNameIn;
		bpmPVs = bpmPVsIn;
		GP = GPIn;
		bpmViewer = bpmViewerIn;

		Border etchedBorder = BorderFactory.createEtchedBorder();
		border = BorderFactory.createTitledBorder(etchedBorder, graphNameIn);
		panel.setBorder(border);

		//GUI elements definition
		sampleStart_Text.setNumberFormat(int_Format);
		sampleStop_Text.setNumberFormat(int_Format);

		sampleStart_Text.setHorizontalAlignment(JTextField.CENTER);
		sampleStop_Text.setHorizontalAlignment(JTextField.CENTER);

		sampleStart_Text.setForeground(Color.blue);
		sampleStop_Text.setForeground(Color.red);

		sampleStart_Text.setBackground(Color.white);
		sampleStop_Text.setBackground(Color.white);

		sampleStart_Label.setHorizontalAlignment(SwingConstants.CENTER);
		sampleStop_Label.setHorizontalAlignment(SwingConstants.CENTER);

		sampleStart_Label.setForeground(Color.blue);
		sampleStop_Label.setForeground(Color.red);

		//Graph panel definition
		SimpleChartPopupMenu.addPopupMenuTo(GP);
		GP.setOffScreenImageDrawing(true);
		GP.setGraphBackGroundColor(Color.white);

		GP.setAxisNames("sample index", "signal");
		GP.setNumberFormatX(int_Format);
		GP.setNumberFormatY(dbl_Format);

		GP.setDraggingVerLinesGraphMode(true);

		GP.addVerticalLine(0., Color.blue);
		GP.addVerticalLine(200., Color.red);

		dragVerLine_Listener =
			new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					int ind = GP.getDraggedLineIndex();
					double pos = GP.getVerticalValue(ind);
					if (ind == 0) {
						sampleStart_Text.setValueQuietly(pos);
					} else {
						sampleStop_Text.setValueQuietly(pos);
					}

					if (use_Button.isSelected()) {
						updateAxis();
						bpmViewer.updateGraphPanels();
					}
				}
			};

		GP.addDraggedVerLinesListener(dragVerLine_Listener);
		GP.setDraggedVerLinesMotionListen(true);

		sampleStart_Text.addActionListener(
			new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					GP.addDraggedVerLinesListener(null);
					double pos = sampleStart_Text.getValue();
					GP.setVerticalLineValue(pos, 0);
					GP.addDraggedVerLinesListener(dragVerLine_Listener);
					if (use_Button.isSelected()) {
						updateAxis();
						bpmViewer.updateGraphPanels();
					}
				}
			});

		sampleStop_Text.addActionListener(
			new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					GP.addDraggedVerLinesListener(null);
					double pos = sampleStop_Text.getValue();
					GP.setVerticalLineValue(pos, 1);
					GP.addDraggedVerLinesListener(dragVerLine_Listener);
					if (use_Button.isSelected()) {
						updateAxis();
						bpmViewer.updateGraphPanels();
					}
				}
			});

		use_Button.setSelected(false);
		use_Button.addActionListener(
			new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					updateAxis();
					bpmViewer.updateGraphPanels();
				}
			});

		sampleStart_Text.setValue(0.0);
		sampleStop_Text.setValue(200.);

		//set GUI elements to panel
		panel.setLayout(new BorderLayout());
		panel.setBackground(panel.getBackground().darker());

		JPanel tmp_panel_0 = new JPanel();
		tmp_panel_0.setBorder(etchedBorder);
		tmp_panel_0.setBackground(tmp_panel_0.getBackground().darker());
		tmp_panel_0.setLayout(new GridLayout(1, 5, 1, 1));
		tmp_panel_0.add(sampleStart_Label);
		tmp_panel_0.add(sampleStart_Text);
		tmp_panel_0.add(sampleStop_Label);
		tmp_panel_0.add(sampleStop_Text);
		tmp_panel_0.add(use_Button);

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

		sampleStart_Label.setFont(fnt);
		sampleStop_Label.setFont(fnt);
		sampleStart_Text.setFont(fnt);
		sampleStop_Text.setFont(fnt);
		use_Button.setFont(fnt);
	}


	/**
	 *  Returns the vectors with BpmViewerPV instances
	 *
	 *@return    The vector with BpmViewerPV instances
	 */
	protected Vector<BpmViewerPV> getData() {
		return bpmPVs;
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
	 *  Returns true or false about using limits in sigma calculations.
	 *
	 *@return    True of false about using limits in sigma calculations
	 */
	public boolean useLimits() {
		return use_Button.isSelected();
	}


	/**
	 *  Returns the min limit value
	 *
	 *@return    The min limit value
	 */
	public double getMinLim() {
		double x1 = sampleStart_Text.getValue();
		double x2 = sampleStop_Text.getValue();
		return Math.min(x1, x2);
	}


	/**
	 *  Returns the max limit value
	 *
	 *@return    The max limit value
	 */
	public double getMaxLim() {
		double x1 = sampleStart_Text.getValue();
		double x2 = sampleStop_Text.getValue();
		return Math.max(x1, x2);
	}


	/**
	 *  Updates the graph data.
	 */
	public void update() {
		if (gdCrvArr.length < bpmPVs.size()) {
			gdCrvArr = new CurveData[bpmPVs.size()];
			for (int i = 0; i < gdCrvArr.length; i++) {
				gdCrvArr[i] = new CurveData();
			}
		}
		Vector<CurveData> vct = new Vector<CurveData>();
		for (int i = 0, n = bpmPVs.size(); i < n; i++) {
			BpmViewerPV bpmPV = bpmPVs.get(i);
			if (bpmPV.getArrayDataPV().getSwitchOn()) {
				CurveData gd = gdCrvArr[i];
				gd.clear();
				CurveData gdIni = bpmPV.getGraphData();
				gd.setColor(gdIni.getColor());
				gd.setLineWidth(gdIni.getLineWidth());
				if (use_Button.isSelected()) {
					double x_min = sampleStart_Text.getValue();
					double x_max = sampleStop_Text.getValue();
					double x = 0.;
					double y_avg = 0.;
					int nP = 0;
					for (int j = 0, jN = gdIni.getSize(); j < jN; j++) {
						x = gdIni.getX(j);
						if (x >= x_min && x <= x_max) {
							y_avg += gdIni.getY(j);
							nP++;
						}
					}
					if (nP > 0) {
						y_avg /= nP;
					}
					for (int j = 0, jN = gdIni.getSize(); j < jN; j++) {
						x = gdIni.getX(j);
						if (x >= x_min && x <= x_max) {
							gd.addPoint(x, gdIni.getY(j) - y_avg);
						}
					}
				} else {
					for (int j = 0, jN = gdIni.getSize(); j < jN; j++) {
						gd.addPoint(gdIni.getX(j), gdIni.getY(j));
					}
				}
				vct.add(gd);
			}
		}
		GP.setCurveData(vct);
	}


	/**
	 *  Set the scale axis on the graph.
	 */
	private void updateAxis() {
		if (use_Button.isSelected()) {
			double x1 = sampleStart_Text.getValue();
			double x2 = sampleStop_Text.getValue();
			if (x2 <= x1) {
				double tmp = x1;
				x1 = x2;
				x2 = x1 + 1;
			}
			double step = (x2 - x1) / 4.0;
			GP.setLimitsAndTicksX(x1, step, 4, 4);
		} else {
			GP.setExternalGL(null);
			GP.clearZoomStack();
		}
	}


	/**
	 *  Dumps information about the configuration of the ValuesGraphPanel instance
	 *  into the XmlDataAdaptor instance
	 *
	 *@param  da  The XmlDataAdaptor instance as a place to keep config information
	 */
	public void dumpConfig(XmlDataAdaptor da) {
		XmlDataAdaptor paramsDA = (XmlDataAdaptor) da.createChild("PARAMS");
		paramsDA.setValue("startInd", (int) sampleStart_Text.getValue());
		paramsDA.setValue("stopInd", (int) sampleStop_Text.getValue());
		paramsDA.setValue("useLimits", use_Button.isSelected());
	}


	/**
	 *  Configures the ValuesGraphPanel instance according the configuration
	 *  information in the XmlDataAdaptor instance
	 *
	 *@param  da  The data adapter with config information
	 */
	public void setConfig(XmlDataAdaptor da) {
		XmlDataAdaptor paramsDA = (XmlDataAdaptor) da.childAdaptor("PARAMS");
		sampleStart_Text.setValue(paramsDA.doubleValue("startInd"));
		sampleStop_Text.setValue(paramsDA.doubleValue("stopInd"));
		use_Button.setSelected(paramsDA.booleanValue("useLimits"));
		updateAxis();
	}

}

