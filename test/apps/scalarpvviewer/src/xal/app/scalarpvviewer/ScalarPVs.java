/*
 *  ScalarPVs.java
 *
 *  Created on May 24, 2005
 */
package xal.app.scalarpvviewer;

import java.awt.Color;
import java.text.*;
import java.util.*;
import javax.swing.*;
import java.io.*;
import java.awt.event.*;

import xal.ca.*;
import xal.extension.widgets.plot.*;
import xal.tools.text.ScientificNumberFormat;
import xal.tools.xml.*;

import xal.extension.scan.MonitoredPV;
import xal.extension.scan.UpdatingEventController;

/**
 *  Keeps the references to the ScalarPV objects.
 *
 *@author    shishlo
 */
public class ScalarPVs {

	private Vector<ScalarPV> spvV = new Vector<ScalarPV>();

	private UpdatingEventController uc = null;

	private JTextField messageTextLocal = new JTextField();

	/** count of pulses used in averaging (effective since we are using exponential weighted averaging) */
	private int _averagingPulseCount = ScalarPV.DEFAULT_AVERAGING_PULSE_COUNT;


	/**
	 *  Constructor for the ScalarPVs object
	 *
	 *@param  ucIn  Update controller
	 */
	public ScalarPVs(UpdatingEventController ucIn) {
		uc = ucIn;
	}


	/** set the averaging pulse count */
	public int getAveragingPulseCount() {
		return _averagingPulseCount;
	}


	/** set the averaging pulse count */
	public void setAveragingPulseCount( final int pulseCount ) {
		_averagingPulseCount = pulseCount;

		for ( final ScalarPV scalarPV : spvV ) {
			scalarPV.setAveragingPulseCount( pulseCount );
		}
	}


	/**
	 *  Sets the messageTextField attribute of the ScalarPVs object
	 *
	 *@param  messageTextLocal  The new messageTextField value
	 */
	public void setMessageTextField(JTextField messageTextLocal) {
		this.messageTextLocal = messageTextLocal;
	}


	/**
	 *  Sets the color to the values graph data.
	 *
	 *@param  color  The new color.
	 */
	public void setValColor(Color color) {
		for (int i = 0, n = spvV.size(); i < n; i++) {
			(spvV.get(i)).setValColor(color);
		}
	}


	/**
	 *  Sets the color to the reference graph data.
	 *
	 *@param  color  The new color.
	 */
	public void setRefColor(Color color) {
		for (int i = 0, n = spvV.size(); i < n; i++) {
			( spvV.get(i)).setRefColor(color);
		}
	}


	/**
	 *  Sets the color to the chart graph data.
	 */
	public void setChartColor() {
		for (int i = 0, n = spvV.size(); i < n; i++) {
			Color color = IncrementalColors.getColor(i);
			( spvV.get(i)).setChartColor(color);
		}
	}


	/**
	 *  Returns the chart color of the ScalarPV object
	 *
	 *@param  ind  The ScalarPV index
	 *@return      The chart color
	 */
	public Color getChartColor(int ind) {
		return ( spvV.get(ind)).getChartColor();
	}


	/**
	 *  Returns the ScalarPV object.
	 *
	 *@param  ind  The ScalarPV index
	 *@return      The scalarPV object
	 */
	public ScalarPV getScalarPV(int ind) {
		return ( spvV.get(ind));
	}


	/**
	 *  Returns the number of ScalarPV objects
	 *
	 *@return    The number of ScalarPV objects.
	 */
	public int getSize() {
		return spvV.size();
	}


	/**
	 *  Clears the chart
	 */
	public void clearChart() {
		for (int i = 0, n = spvV.size(); i < n; i++) {
			( spvV.get(i)).clearChart();
		}
	}


	/**
	 *  Removes all data
	 */
	public void clearAll() {
		spvV.clear();
	}


	/**
	 *  Memorizes the current value as the reference one.
	 */
	public void memorizeRef() {
		for (int i = 0, n = spvV.size(); i < n; i++) {
			ScalarPV spv = spvV.get(i);
			if (spv.getMonitoredPV().isGood() && (spv.showValue() || spv.showDif())) {
				spv.memorizeRef();
			}
		}
	}


	/**
	 *  Sets the current values from PV
	 */
	public void measure() {
		for (int i = 0, n = spvV.size(); i < n; i++) {
			(spvV.get(i)).measure();
		}
	}


	/**
	 *  Adds one point to the chart graph
	 */
	public void memorize() {
		for (int i = 0, n = spvV.size(); i < n; i++) {
			( spvV.get(i)).memorize();
		}
	}


	/**
	 *  Adds a feature to the ScalarPV attribute of the ScalarPVs object
	 *
	 *@param  pvName  The feature to be added to the ScalarPV attribute
	 *@param  refVal  The feature to be added to the ScalarPV attribute
	 */
	public void addScalarPV(String pvName, double refVal) {
		ScalarPV spv = new ScalarPV(uc);
		spv.setAveragingPulseCount( _averagingPulseCount );
		spv.getMonitoredPV().setChannelName(pvName);
		spv.getMonitoredPV().startMonitor();
		int n = spvV.size();
		spv.setIndex(n);
		spv.setRefValue(refVal);
		spvV.add(spv);
		setChartColor();
	}


	/**
	 *  Reads chart data from file
	 *
	 *@param  in  Description of the Parameter
	 */
	public void readChart(BufferedReader in) {
		try {
			String line = in.readLine();

			if (line == null) {
				return;
			}

			StringTokenizer st = new StringTokenizer(line, " ");

			int nElm = st.countTokens();
			int nPVs = spvV.size();

			while (nElm == (nPVs + 1)) {
				double time = Double.parseDouble(st.nextToken());
				for (int i = 0, n = nPVs; i < n; i++) {
					( spvV.get(i)).addChartPoint(time, Double.parseDouble(st.nextToken()));
				}
				line = in.readLine();
				if (line == null) {
					return;
				}
				st = new StringTokenizer(line, " ");
				nElm = st.countTokens();
			}
		} catch (IOException exception) {
			messageTextLocal.setText(null);
			messageTextLocal.setText("Fatal error. Something wrong with input file. Stop.");
		}

	}


	/**
	 *  Dump the chart data to the output stream
	 *
	 *@param  out  The writer
	 */
	public void writeChart(BufferedWriter out) {

		ScientificNumberFormat fmtT = new ScientificNumberFormat( 14, 20, false );
		ScientificNumberFormat fmtY = new ScientificNumberFormat( 4, 10 , false );

		try {

			int nPVs = spvV.size();
			if (nPVs <= 0) {
				return;
			}

			out.newLine();
			String names = "% ";
			for (int i = 0, n = nPVs; i < n; i++) {
				names = names + " " + ( spvV.get(i)).getMonitoredPV().getChannelName();
			}

			out.write(names);
			out.newLine();

			out.write("%==================time chart data=================================");
			out.newLine();

			int nL = (spvV.get(0)).getValueChartGraphData().getSize();
			for (int il = 0; il < nL; il++) {
				double time = ( spvV.get(0)).getValueChartGraphData().getX(il);
				out.write(" " + fmtT.format(time) + " ");
				for (int i = 0, n = nPVs; i < n; i++) {
					CurveData cv = ( spvV.get(i)).getValueChartGraphData();
					double y = cv.getY(il);
					out.write(" " + fmtY.format(y));
				}
				out.newLine();
			}

		} catch (IOException exception) {
			messageTextLocal.setText(null);
			messageTextLocal.setText("Fatal error. Something wrong with output file. Stop.");
		}
	}

	/**
	* Finds min and max for all graph data for all ScalarPVs.
	*/
	public void findMinMax(){
		for (int i = 0, n = spvV.size(); i < n; i++) {
			( spvV.get(i)).findMinMax();
		}
	}


}

