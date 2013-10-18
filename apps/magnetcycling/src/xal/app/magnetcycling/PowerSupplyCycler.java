package xal.app.magnetcycling;

import java.util.*;
import java.awt.*;
import java.awt.event.*;

import xal.extension.widgets.plot.*;
import xal.extension.scan.WrappedChannel;

/**
 *  This class keeps references to the power supply I_Set PV and read back PV, a
 *  time table for current during the cycling. It is also responsible for the
 *  keeping th track of the cycling.
 *
 *@author     shishlo
 */
public class PowerSupplyCycler {

	//defines inactivity period in the beginning of the cycling
	private double time_shift = 0.;

	//the accumulated running time
	private double run_time = 0.;

	//initial current in PS
	private double currentInit = 0.;

	//active true or false - will participate in steps
	private boolean active = true;

	//time table parameters
	//Number of cycles, current change rate in A/sec,
	//and time periods for low and max current
	private int nCycles = 1;
	private double maxCurrent = 0.0;
	private double changeRate = 10.0;
	private double minCurrTime = 60.;
	private double maxCurrTime = 60.;

	//set current and read back PVs wrappers
	private WrappedChannel setCurrentCh = new WrappedChannel();
	private WrappedChannel rbCurrentCh = new WrappedChannel();

	//set the PV value to 1 to indicate that the magnet was cycled
	private WrappedChannel setNeedCycleCh = new WrappedChannel();
	
	// Graph Data
	private int maxNumbOfPoints = 800;

	private BasicGraphData timeTableGD = new BasicGraphData();

	private BasicGraphData setPVtrackGD = new BasicGraphData();
	private BasicGraphData rbPVtrackGD = new BasicGraphData();

	private double[] arrX = new double[0];
	private double[] arrY = new double[0];

	/**
	 *  Constructor for the PowerSupplyCycler object.
	 */
	public PowerSupplyCycler() {

		timeTableGD.setImmediateContainerUpdate(false);
		setPVtrackGD.setImmediateContainerUpdate(false);
		rbPVtrackGD.setImmediateContainerUpdate(false);

		timeTableGD.setDrawPointsOn(true);

		setPVtrackGD.setDrawPointsOn(false);
		rbPVtrackGD.setDrawPointsOn(true);

		setPVtrackGD.setDrawLinesOn(true);
		rbPVtrackGD.setDrawLinesOn(false);

		setPVtrackGD.setLineThick(2);
		rbPVtrackGD.setGraphPointSize(3);

		setPVtrackGD.setGraphProperty("Legend", "Set PV");
		rbPVtrackGD.setGraphProperty("Legend", "ReadBack PV");
	}

	/**
	 *  Resize graph arrays because it is inefficient to have too many points
	 */
	private void resizegraphArr() {
		int n = setPVtrackGD.getNumbOfPoints();
		if(n > ((int) (1.4 * maxNumbOfPoints))) {
			double x_min = setPVtrackGD.getMinX();
			double x_max = setPVtrackGD.getMaxX();
			double step = (x_max - x_min) / (maxNumbOfPoints - 1);
			if(maxNumbOfPoints != arrX.length) {
				arrX = new double[maxNumbOfPoints];
				arrY = new double[maxNumbOfPoints];
			}
			for(int i = 0; i < maxNumbOfPoints; i++) {
				arrX[i] = x_min + i * step;
				arrY[i] = setPVtrackGD.getValueY(arrX[i]);
			}
			setPVtrackGD.removeAllPoints();
			setPVtrackGD.addPoint(arrX, arrY);
		}

		n = rbPVtrackGD.getNumbOfPoints();
		if(n > ((int) (1.4 * maxNumbOfPoints))) {
			double x_min = rbPVtrackGD.getMinX();
			double x_max = rbPVtrackGD.getMaxX();
			double step = (x_max - x_min) / (maxNumbOfPoints - 1);
			if(maxNumbOfPoints != arrX.length) {
				arrX = new double[maxNumbOfPoints];
				arrY = new double[maxNumbOfPoints];
			}
			for(int i = 0; i < maxNumbOfPoints; i++) {
				arrX[i] = x_min + i * step;
				arrY[i] = rbPVtrackGD.getValueY(arrX[i]);
			}
			rbPVtrackGD.removeAllPoints();
			rbPVtrackGD.addPoint(arrX, arrY);
		}
	}


	/**
	 *  Sets the channel name for the Set PV of the PowerSupplyCycler object
	 *
	 *@param  chanNameSetPV  The new channel name for the Set PV
	 */
	public void setChannelNameSet(String chanNameSetPV) {
		setCurrentCh.setChannelName(chanNameSetPV);
		setPVtrackGD.setGraphProperty("Legend", chanNameSetPV);
		timeTableGD.setGraphProperty("Legend", chanNameSetPV);
	}
	/**
	 *  Sets the channel name for the ReadBack PV of the PowerSupplyCycler object
	 *
	 *@param  chanNameRBPV  The new channel name for the ReadBack P
	 */
	public void setChannelNameRB(String chanNameRBPV) {
		rbCurrentCh.setChannelName(chanNameRBPV);
		rbPVtrackGD.setGraphProperty("Legend", chanNameRBPV);
	}	
	
	/**
	 * Sets the channel name for the needCycle PV of the PowerSupplyCycler object
	 * @param  chanNameNeedCyclePV  The new channel name for the needCycle PV
	 */	
	public void setChannelNameNeedCycle(String chanNameNeedCyclePV) {
	   setNeedCycleCh.setChannelName(chanNameNeedCyclePV);
	}
	
	/**
	 *  Returns the channel name of the Set PV
	 *
	 *@return    The channel name
	 */
	public String getChannelName() {
		return setCurrentCh.getChannelName();
	}

	/**
	 *  Returns the channel name of the Read Back PV
	 *
	 *@return    The channel name
	 */
	public String getChannelNameRB() {
		return rbCurrentCh.getChannelName();
	}

	/**
	 *  Returns the channel name of the needCycle PV
	 *
	 *@return    The channel name
	 */
	public String getChannelNameNeedCycle() {	
	   return setNeedCycleCh.getChannelName();
	}
	
	/**
	 *  Initializes PowerSupplyCycler instance.
	 */
	public void init() {
		setPVtrackGD.removeAllPoints();
		rbPVtrackGD.removeAllPoints();

		run_time = 0.;

		//===== create time table ======
		createTimeTable();
	}


	/**
	 *  Creates time table by using internal parameters.
	 */
	public void createTimeTable() {
		//memorize the initial current
		currentInit = setCurrentCh.getValue();

		//===== create time table ======
		timeTableGD.removeAllPoints();
		timeTableGD.addPoint(0., currentInit);
		timeTableGD.addPoint(currentInit / changeRate, 0.);
		double loop_time = timeTableGD.getMaxX();
		double changeTime = maxCurrent / changeRate;
		double goToGoalTime = Math.abs((maxCurrent - currentInit) / changeRate);
		for(int i = 0; i < nCycles; i++) {
			timeTableGD.addPoint(timeTableGD.getMaxX() + minCurrTime, 0.);
			timeTableGD.addPoint(timeTableGD.getMaxX() + changeTime, maxCurrent);
			timeTableGD.addPoint(timeTableGD.getMaxX() + maxCurrTime, maxCurrent);
			if(i != (nCycles - 1)) {
				timeTableGD.addPoint(timeTableGD.getMaxX() + changeTime, 0.);
			}
		}
		timeTableGD.addPoint(timeTableGD.getMaxX() + goToGoalTime, currentInit);
	}


	/**
	 *  Restores initial currents in power supply that was memorized in init().
	 */
	public void restoreInitialCurrents() {
		setCurrentCh.setValue(currentInit);
	}

	/**
	 *  Sets the number of cycles in cycling for PowerSupplyCycler object
	 *
	 *@param  nCycles  The new number of cycles
	 */
	public void setnCycles(int nCycles) {
		if(nCycles < 1) {
			nCycles = 1;
		}
		this.nCycles = nCycles;
	}

	/**
	 *  Returns the number of cycles in cycling for PowerSupplyCycler object
	 *
	 *@return    The Return Value
	 */
	public int getnCycles() {
		return nCycles;
	}

	/**
	 *  Sets the maximal current in Ampers for PowerSupplyCycler object
	 *
	 *@param  maxCurrent  The new maximal current in Ampers
	 */
	public void setMaxCurrent(double maxCurrent) {
		this.maxCurrent = maxCurrent;
	}

	/**
	 *  Returns the maximal current in Ampers for PowerSupplyCycler object
	 *
	 *@return    The maxCurrent value
	 */
	public double getMaxCurrent() {
		return maxCurrent;
	}

	/**
	 *  Sets the change rate in Ampers/sec for PowerSupplyCycler object
	 *
	 *@param  changeRate  The new change rate in Ampers/sec
	 */
	public void setChangeRate(double changeRate) {
		this.changeRate = changeRate;
	}

	/**
	 *  Returns the change rate in Ampers/sec for PowerSupplyCycler object
	 *
	 *@return    The changeRate value
	 */
	public double getChangeRate() {
		return changeRate;
	}

	/**
	 *  Sets the time to keep PS at minimal current in second
	 *
	 *@param  minCurrTime  The new time to keep PS at minimal current in second
	 */
	public void setMinCurrTime(double minCurrTime) {
		this.minCurrTime = minCurrTime;
	}

	/**
	 *  Returns the time to keep PS at minimal current in second
	 *
	 *@return    The minCurrTime value
	 */
	public double getMinCurrTime() {
		return minCurrTime;
	}

	/**
	 *  Sets the time to keep PS at maximal current in second
	 *
	 *@param  maxCurrTime  The new time to keep PS at minimal current in second
	 */
	public void setMaxCurrTime(double maxCurrTime) {
		this.maxCurrTime = maxCurrTime;
	}

	/**
	 *  Returns the time to keep PS at maximal current in second
	 *
	 *@return    The maxCurrTime value
	 */
	public double getMaxCurrTime() {
		return maxCurrTime;
	}


	/**
	 *  Sets the color attribute of the PowerSupplyCycler object
	 *
	 *@param  cl  The new color value
	 */
	public void setColor(Color cl) {
		timeTableGD.setGraphColor(cl);
		setPVtrackGD.setGraphColor(cl);
		rbPVtrackGD.setGraphColor(cl);
	}


	/**
	 *  Returns the graphSetPV attribute of the PowerSupplyCycler object
	 *
	 *@return    The graphSetPV value
	 */
	public BasicGraphData getGraphSetPV() {
		return setPVtrackGD;
	}

	/**
	 *  Returns the graphReadBackPV attribute of the PowerSupplyCycler object
	 *
	 *@return    The graphReadBackPV value
	 */
	public BasicGraphData getGraphReadBackPV() {
		return rbPVtrackGD;
	}

	/**
	 *  Returns the graphTimeTable attribute of the PowerSupplyCycler object
	 *
	 *@return    The graphTimeTable value
	 */
	public BasicGraphData getGraphTimeTable() {
		return timeTableGD;
	}

	/**
	 *  Description of the Method
	 */
	public void cleanTimeTable() {
		timeTableGD.removeAllPoints();
	}


	/**
	 *  Returns the time shift in seconds
	 *
	 *@return    The time shift value in seconds
	 */
	public double getTimeShift() {
		return time_shift;
	}


	/**
	 *  Sets the time shift in seconds
	 *
	 *@param  time_shift  The new time shift value in seconds
	 */
	public void setTimeShift(double time_shift) {
		this.time_shift = time_shift;
	}

	/**
	 *  Returns the max time in seconds
	 *
	 *@return    The max time value in seconds
	 */
	public double getMaxTime() {
		return (time_shift + timeTableGD.getMaxX());
	}

	/**
	 *  Makes the step in time. The value in seconds
	 *
	 *@param  time_step  The time step in seconds
	 */
	public void makeTimeStep(double time_step) {
		if(!active) {
			return;
		}

		run_time = run_time + time_step;
		if(run_time < time_shift) {
			return;
		}
		if((run_time - time_shift) >= timeTableGD.getMinX() &&
				(run_time - time_shift) < timeTableGD.getMaxX()) {
			double pvValue = timeTableGD.getValueY(run_time - time_shift);
			setCurrentCh.setValue(pvValue);
			return;
		}
		if((run_time - time_shift) >= timeTableGD.getMaxX()) {
			double pvValue = timeTableGD.getY(timeTableGD.getNumbOfPoints() - 1);
			setCurrentCh.setValue(pvValue);
			setNeedCycleCh.setValue(0.);
			//System.out.println("debug =========================== PV="+setNeedCycleCh.getChannelName()+" is set to 0");
			return;
		}
		return;
	}

	/**
	 *  Adds a graph point to the graph of set PV
	 */
	public void accountGraphPoint() {
		if(!active) {
			return;
		}
		setPVtrackGD.addPoint(run_time, setCurrentCh.getValue());
		rbPVtrackGD.addPoint(run_time, rbCurrentCh.getValue());
		resizegraphArr();
	}

	/**
	 *  Sets the active attribute of the PowerSupplyCycler object
	 *
	 *@param  active  The new active value
	 */
	public void setActive(boolean active) {
		this.active = active;
	}

	/**
	 *  Returns the active attribute of the PowerSupplyCycler object
	 *
	 *@return    The active value
	 */
	public boolean getActive() {
		return active;
	}

}

