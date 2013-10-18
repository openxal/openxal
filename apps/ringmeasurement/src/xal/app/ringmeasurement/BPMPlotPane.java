/* BPMPlotPane.java
 *
 * Copyright (c) 2001-2005 Oak Ridge National Laboratory
 * Oak Ridge, Tenessee 37831, U.S.A.
 * All rights reserved.
 *
 * Created on February 23, 2005, 10:25 AM
 */

package xal.app.ringmeasurement;

import xal.extension.widgets.apputils.SimpleChartPopupMenu;
import xal.extension.widgets.plot.BasicGraphData;
import xal.extension.widgets.plot.FunctionGraphsJPanel;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;

import javax.swing.JPanel;

public class BPMPlotPane extends JPanel {
	static final long serialVersionUID = 0;

	double[] myBpmArray, xArray;

	protected FunctionGraphsJPanel bpmPlot;

	private BasicGraphData bpmData;

	private BasicGraphData data;

	private final int ind;

	/**
	 * BPM turn-by-turn data plot
	 * 
	 * @param ind
	 *            Hori./Vert. indicator. Hori.=0, Vert.=1
	 */
	public BPMPlotPane(int ind) {
		this.ind = ind;

		setLayout(new BorderLayout());
		setPreferredSize(new Dimension(350, 220));
		bpmPlot = new FunctionGraphsJPanel();

		switch (ind) {

		case 0:
			bpmPlot.setName("BPM Horizontal TBT Data");
			break;
		case 1:
			bpmPlot.setName("BPM Vertical TBT Data");
			break;
		case 2:
			bpmPlot.setName("BPM Horizontal Phase Plot");
			break;
		case 3:
			bpmPlot.setName("BPM Vertical Phase Plot");
			break;
		case 4:
			bpmPlot.setName("BPM Hori. Phase Diff From Design");
			break;
		case 5:
			bpmPlot.setName("BPM Vert. Phase Diff From Design");
			break;
		default:
			break;
		}

		if (ind == 0 || ind == 1)
			bpmPlot.setAxisNames("turn no.", "pos (mm)");
		else if (ind == 2 || ind == 3)
			bpmPlot.setAxisNames("s (m)", "phase (rad)");
		else if (ind == 4 || ind == 5)
			bpmPlot.setAxisNames("s (m)", "phase error (deg)");

		bpmPlot.addMouseListener(new SimpleChartPopupMenu(bpmPlot));
		add(bpmPlot, BorderLayout.CENTER);
	}

	public void setDataArray(double[] bpmArray) {
		myBpmArray = bpmArray;
		if (myBpmArray != null) {
			xArray = new double[myBpmArray.length];
			for (int i = 0; i < myBpmArray.length; i++)
				xArray[i] = i + 1.;
		}
	}

	public void setDataArray(double[] xArray, double[] bpmArray) {
		this.xArray = xArray;
		myBpmArray = bpmArray;
	}

	public void setFittedData(BasicGraphData graphData) {
		data = graphData;
	}

	public void plot() {
		bpmPlot.removeAllGraphData();

		if (myBpmArray != null) {
			// for BPM data
			bpmData = new BasicGraphData();
			bpmData.setDrawLinesOn(false);
			
			// Do not add point by point but the entire arrays.  This will improve the performance a lot.
			//for (int i = 0; i < myBpmArray.length; i++) {
			//	bpmData.addPoint(xArray[i], myBpmArray[i]);
			//}
			
			bpmData.addPoint(xArray, myBpmArray);
			
			bpmPlot.addGraphData(bpmData);

			// for fitted curve
			if (ind == 0 || ind == 1) {
				// clean up old fitted curve first
				if (bpmPlot.getAllGraphData().size() > 1)
					bpmPlot.removeCurveData(1);
				if (data != null) {
					data.setDrawPointsOn(false);
					data.setGraphColor(Color.BLUE);
					bpmPlot.addGraphData(data);
				}
			}
		}
	}

	// public void actionPerformed(ActionEvent ae) {
	// action for FFT

	// }
}
