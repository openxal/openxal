package xal.app.ringbpmviewer;

import java.awt.*;
import java.util.*;

import xal.extension.scan.UpdatingEventController;

import xal.extension.widgets.plot.barchart.*;

/**
 *  The differences of ring BPM (x,y, or amplitude) values stack for two turn
 *  numbers. It implements the BarColumn interface to show data on a bar chart.
 *
 *@author     shishlo
 */

public class RingBPMtsDiff implements BarColumn {

	private Color barColor = null;

	private Vector<Double> dataStack = new Vector<Double>();
	private volatile int stackCapacity = 5;

	private volatile boolean switchedOn = false;

	private String ringBPM_name = "None";

	private int plusInd = 0;
	private int minusInd = 1;

	//update controller for the redrawing the bar charts
	private UpdatingEventController uc = null;

	//update controller for the changing the set of bar columns in charts
	private UpdatingEventController ucContent = null;

	/**
	 *  Constructor for the RingBPMtsDiff object
	 *
	 *@param  ucIn         The Parameter
	 *@param  ucContentIn  The Parameter
	 */
	public RingBPMtsDiff(UpdatingEventController ucIn, UpdatingEventController ucContentIn) {
		uc = ucIn;
		ucContent = ucContentIn;
	}


	/**
	 *  Sets the turn index for positive sign in difference formula
	 *
	 *@param  plusInd  The new turn index
	 */
	public void setPlusIndex(int plusInd) {
		this.plusInd = plusInd;
	}


	/**
	 *  Sets the turn index for negative sign in difference formula
	 *
	 *@param  minusInd  The new turn index
	 */
	public void setMinusIndex(int minusInd) {
		this.minusInd = minusInd;
	}


	/**
	 *  Returns the turn index for positive sign in difference formula
	 *
	 *@return    The index value
	 */
	public int getPlusIndex() {
		return plusInd;
	}


	/**
	 *  Returns the turn index for negative sign in difference formula
	 *
	 *@return    The index value
	 */
	public int getMinusIndex() {
		return minusInd;
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
	}

	/**
	 *  Clear the memory data stack
	 */
	public void clearStack() {
		dataStack.clear();
		uc.update();
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
	 *  Sets the switched-On attribute of the RingBPMtbtAvg object
	 *
	 *@param  switchedOn  The new switched-On value
	 */
	public void setSwitchedOn(boolean switchedOn) {
		this.switchedOn = switchedOn;
		if(ucContent != null) {
			ucContent.update();
		}
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
			Double dI = dataStack.get(index);
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
	 *  Returns the color for the line inside the bar
	 *
	 *@param  index  The index of the line inside the bar
	 *@return        The value for the line hight inside the bar
	 */
	public Color getColor(int index) {
		return barColor;
	}


	/**
	 *  Sets the color attribute of the RingBPMtsDiff object
	 *
	 *@param  barColor  The new color value
	 */
	public void setColor(Color barColor) {
		this.barColor = barColor;
	}

}

