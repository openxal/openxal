package xal.app.ringbpmviewer;

import java.awt.*;
import java.util.*;
import java.awt.event.*;

import xal.ca.*;
import xal.tools.plot.*;
import xal.extension.scan.MonitoredPV;
import xal.extension.scan.MonitoredPVEvent;
import xal.extension.scan.UpdatingEventController;

import xal.tools.plot.barchart.*;

/**
 *  The average ring BPM (x,y, or amplitude) values stack. It implements the
 *  BarColumn interface to show data on a bar chart.
 *
 *@author     shishlo
 */

public class RingBPMtbtAvg implements BarColumn {

	private Color barColor = null;

	private Vector<Double> dataStack = new Vector<Double>();
	private volatile int stackCapacity = 5;

	private volatile boolean switchedOn = false;

	private String ringBPM_name = "None";

	private int startAvgInd = 0;
	private int stopAvgInd = 0;

	//waveform array
	private double[] x_arr = new double[0];
	private double[] y_arr = new double[0];

	private boolean analysisDone = false;

	private double[] arr_anl_x = new double[0];
	private double[] arr_anl_y = new double[0];
	private HashMap<String, Double> analysisResults = new HashMap<String,Double>();

	private String analysisResStr = new String("");

	private MonitoredPV mpv = null;

	private boolean listenToEPICS = false;

	static int ringBPMsCounter = 0;

	//two turns difference signal stack
	private RingBPMtsDiff ringBPMtsDiff = null;

	//update controller for the redrawing the bar charts
	private UpdatingEventController uc = null;

	//update controller for the changing the set of bar columns in charts
	private UpdatingEventController ucContent = null;


	/**
	 *  Constructor for the RingBPMtbtAvg object
	 *
	 *@param  ucIn         The update controller for the redrawing the bar charts
	 *@param  ucContentIn  The update controller for the changing the set of bar
	 *      columns in charts
	 */
	public RingBPMtbtAvg(UpdatingEventController ucIn, UpdatingEventController ucContentIn) {
		uc = ucIn;
		ucContent = ucContentIn;

		mpv = MonitoredPV.getMonitoredPV("RingBPM_" + ringBPMsCounter);
		ringBPMsCounter++;

		ActionListener updateListener =
			new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					measure((MonitoredPVEvent) e);
				}
			};
		mpv.addValueListener(updateListener);
		mpv.addStateListener(updateListener);

		ringBPMtsDiff = new RingBPMtsDiff(uc, ucContent);
	}

	/**
	 *  Returns the two turns difference signal stack instance
	 */
	public RingBPMtsDiff getRingBPMtsDiff() {
		return ringBPMtsDiff;
	}

	/**
	 *  Sets the start index to calculate an average value from the waveform
	 *
	 *@param  startAvgInd  The new start index
	 */
	public void setStartIndex(int startAvgInd) {
		this.startAvgInd = startAvgInd;
	}


	/**
	 *  Sets the stop index to calculate an average value from the waveform
	 *
	 *@param  stopAvgInd  The new stop index
	 */
	public void setStopIndex(int stopAvgInd) {
		this.stopAvgInd = stopAvgInd;
	}


	/**
	 *  Returns the start index to calculate an average value from the waveform
	 *
	 *@return    The start index value
	 */
	public int getStartIndex() {
		return startAvgInd;
	}


	/**
	 *  Returns the stop index to calculate an average value from the waveform
	 *
	 *@return    The stop index value
	 */
	public int getStopIndex() {
		return stopAvgInd;
	}


	/**
	 *  Sets the switched-On attribute of the RingBPMtbtAvg object
	 *
	 *@param  switchedOn  The new switched-On value
	 */
	public void setSwitchedOn(boolean switchedOn) {
		this.switchedOn = switchedOn;
		if(ucContent != null) {
			ucContent.update();
		}
		ringBPMtsDiff.setSwitchedOn(switchedOn);
	}


	/**
	 *  Returns the switched-On boolean value
	 *
	 *@return    The switched-On value
	 */
	public boolean getSwitchedOn() {
		return switchedOn;
	}


	/**
	 *  This method calculates average value over the TBT array inside the certain
	 *  limits and updates the stack with history data.
	 *
	 *@param  mpvEvnt  The Parameter
	 */
	private void measure(MonitoredPVEvent mpvEvnt) {

		if(!listenToEPICS) {
			return;
		}
		double avgValue = 0.;
		double diffValue = 0.;

		ChannelRecord record = mpvEvnt.getChannelRecord();

		if(record != null) {
			double[] arr = record.doubleArray();
			int i_min = startAvgInd;
			int i_max = Math.min(arr.length, stopAvgInd);
			double s = 0;
			int count = 0;
			for(int i = i_min; i < i_max; i++) {
				if(Math.abs(arr[i]) > 1.0e-10) {
					s += arr[i];
					count++;
				}
			}
			if(count > 0) {
				s /= count;
			}
			avgValue = s;

			if(arr.length != y_arr.length) {
				y_arr = new double[arr.length];
				x_arr = new double[arr.length];
			}

			for(int i = 0; i < arr.length; i++) {
				y_arr[i] = arr[i];
				x_arr[i] = (double) i;
			}

			if(ringBPMtsDiff.getPlusIndex() < arr.length &&
				ringBPMtsDiff.getMinusIndex() < arr.length){
					diffValue = y_arr[ringBPMtsDiff.getPlusIndex()] - y_arr[ringBPMtsDiff.getMinusIndex()];
				}

		} else {
			y_arr = new double[0];
			x_arr = new double[0];
		}

		dataStack.add(new Double(avgValue));

		ringBPMtsDiff.addValue(diffValue);
		//System.out.println("debug stack="+dataStack.size() +" val="+avgValue);

		if(dataStack.size() > stackCapacity) {
			for(int i = 0, n = dataStack.size() - stackCapacity; i < n; i++) {
				Object obj = dataStack.firstElement();
				dataStack.removeElement(obj);
			}
		}
		uc.update();
	}


	/**
	 *  Returns the stack capacity
	 *
	 *@return    The stack capacity value
	 */
	public int getStackCapacity() {
		return stackCapacity;
	}


	/**
	 *  Sets the stack capacity
	 *
	 *@param  stackCapacity  The new stack capacity value
	 */
	public void setStackCapacity(int stackCapacity) {
		this.stackCapacity = stackCapacity;
		if(dataStack.size() > stackCapacity) {
			for(int i = 0, n = dataStack.size() - stackCapacity; i < n; i++) {
				Object obj = dataStack.lastElement();
				dataStack.removeElement(obj);
			}
			uc.update();
		}
		ringBPMtsDiff.setStackCapacity(stackCapacity);
	}


	/**
	 *  Clear the memory data stack
	 */
	public void clearStack() {
		dataStack.clear();
		uc.update();
		ringBPMtsDiff.clearStack();
	}

	/**
	 *  Clear results of analysis for all BPMs
	 */
	public void clearAnalysis() {
		analysisResStr = "";
		analysisDone = false;
	}

	/**
	 *  Returns the "analysis done" attribute of the RingBPMtbtAvg object
	 *
	 *@return    The "analysis done" boolean value
	 */
	public boolean isAnalysisDone() {
		return analysisDone;
	}

	/**
	 *  Sets the analysis results into the RingBPMtbtAvg object
	 *
	 *@param  anl_x            The fit result x-array
	 *@param  anl_y            The fit result y-array
	 *@param  analysisResults  The Hash Map with parameters
	 */
	public void setAnalysisResults(double[] anl_x, double[] anl_y, HashMap<String, Double> analysisResults) {

		if(anl_x.length == anl_y.length) {
			analysisDone = true;
			if(arr_anl_x.length != anl_x.length) {

				arr_anl_x = new double[anl_x.length];
				arr_anl_y = new double[anl_x.length];
			}
			for(int i = 0; i < anl_x.length; i++) {
				arr_anl_x[i] = anl_x[i];
				arr_anl_y[i] = anl_y[i];
			}
			this.analysisResults.clear();
			this.analysisResults.putAll(analysisResults);
		}
	}

	/**
	 *  Returns the analysis attribute for the provided key
	 *
	 *@param  key  The key for parameter
	 *@return      The analysis result object
	 */
	public Object getAnalysisResult(Object key) {
		return analysisResults.get(key);
	}

	/**
	 *  Adds an external data value to the stack
	 *
	 *@param  value  The feature to be added to the Data attribute
	 */
	public void addValue(double value) {
		dataStack.add(new Double(value));
		if(dataStack.size() > stackCapacity) {
			for(int i = 0, n = dataStack.size() - stackCapacity; i < n; i++) {
				Object obj = dataStack.firstElement();
				dataStack.removeElement(obj);
			}
		}
	}


	/**
	 *  Returns the BPM name
	 *
	 *@return    The BPM name
	 */
	public String getBPMName() {
		return ringBPM_name;
	}


	/**
	 *  Sets the BPM name
	 *
	 *@param  ringBPM_name  The new BPM name
	 */
	public void setBPMName(String ringBPM_name) {
		this.ringBPM_name = ringBPM_name;
		ringBPMtsDiff.setBPMName(ringBPM_name);
	}


	/**
	 *  Returns the name of array PV
	 *
	 *@return    The name of array PV
	 */
	public String getNamePV() {
		return mpv.getChannelName();
	}


	/**
	 *  Sets the name of array PV
	 *
	 *@param  pv_name  The new name of array PV
	 */
	public void setNamePV(String pv_name) {
		mpv.setChannelNameQuietly(pv_name);
	}


	/**
	 *@return    Returns the history stack size.
	 */
	public int size() {
		int size = dataStack.size();
		size = Math.min(size, stackCapacity);
		return size;
	}


	/**
	 *  Returns true if user wants to see the line with this index
	 *
	 *@param  index  The index of the line inside the bar
	 *@return        True (or false) if user (does not ) wants to see the line with
	 *      this index
	 */
	public boolean show(int index) {
		return true;
	}


	/**
	 *  Returns true if user wants to see this column at all
	 *
	 *@return    True (or false) if user (does not ) wants to see the column
	 */
	public boolean show() {
		return switchedOn;
	}


	/**
	 *  Returns the value for the line hight inside the bar
	 *
	 *@param  index  The index of the line inside the bar
	 *@return        The value for the line hight inside the bar
	 */
	public double value(int index) {
		if(index < dataStack.size()) {
			Double dI =  dataStack.get(index);
			return dI.doubleValue();
		}
		return 0.;
	}


	/**
	 *  Returns a marker for this bar in the bar chart
	 *
	 *@return    The string with marker
	 */
	public String marker() {
		return ringBPM_name;
	}


	/**
	 *  Sets the "listen to EPICS" attribute
	 *
	 *@param  listenToEPICS  The new listen to EPICS boolean value
	 */
	public void setListenToEPICS(boolean listenToEPICS) {
		this.listenToEPICS = listenToEPICS;

		if(listenToEPICS) {
			mpv.startMonitor();
		} else {
			mpv.stopMonitor();
		}
	}


	/**
	 *  Returns the color for the line inside the bar
	 *
	 *@param  index  The index of the line inside the bar
	 *@return        The value for the line hight inside the bar
	 */
	public Color getColor(int index) {
		return barColor;
	}


	/**
	 *  Sets the color attribute of the RingBPMtbtAvg object
	 *
	 *@param  barColor  The new color value
	 */
	public void setColor(Color barColor) {
		this.barColor = barColor;
		ringBPMtsDiff.setColor(barColor);
	}


	/**
	 *  Returns the array of values inside PV
	 *
	 *@return    The array of double values
	 */
	public double[] getArrY() {
		return y_arr;
	}

	/**
	 *  Returns the array of indexes for the waveform in PV
	 *
	 *@return    The array of indexes
	 */
	public double[] getArrX() {
		return x_arr;
	}

	/**
	 *  Returns the array of fitting waveform
	 *
	 *@return    The array of double values
	 */
	public double[] getAnalysisArrY() {
		return y_arr;
	}

	/**
	 *  Returns the array of indexes for the fitting waveform
	 *
	 *@return    The array of indexes
	 */
	public double[] getAnalysisArrX() {
		return x_arr;
	}

	/**
	 *  Returns the analysis string attribute of the RingBPMtbtAvg object
	 *
	 *@return    The analysis string
	 */
	public String getAnalysisString() {
		return analysisResStr;
	}

	/**
	 *  Sets the analysis string attribute of the RingBPMtbtAvg object
	 *
	 *@param  str  The new analysis string
	 */
	public void setAnalysisString(String str) {
		analysisResStr = str;
	}


	/**
	 *  Removes the monitored PV.
	 */
	protected void finalize() {
		MonitoredPV.removeMonitoredPV(mpv);
	}

}

