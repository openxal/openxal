package xal.app.ringbpmviewer;

import javax.swing.*;
import java.awt.event.*;
import java.awt.*;

import xal.extension.scan.UpdatingEventController;

import xal.tools.plot.barchart.*;

/**
 *  The container of the three RingBPMtbtAvg instances fro x,y, and amplitude
 *  TBT ring BPM PVs
 *
 *@author     shishlo
 */
public class RingBPM {

  private JRadioButton cntrlButton = new JRadioButton("None", false);

  private String ringBPM_name = "None";

  private volatile int stackCapacity = 5;
  private volatile boolean switchedOn = false;

  private RingBPMtbtAvg bpm_x = null;
  private RingBPMtbtAvg bpm_y = null;
  private RingBPMtbtAvg bpm_amp = null;

  private int startAvgInd = 0;
  private int stopAvgInd = 0;

	private int plusInd = 0;
	private int minusInd = 1;

  private boolean listenToEPICS = false;

  //update controller for the redrawing the bar charts
  private UpdatingEventController uc = null;

  //update controller for the changing the set of bar columns in charts
  private UpdatingEventController ucContent = null;


  /**
   *  Constructor for the RingBPM object
   *
   *@param  ucIn         The update controller for the redrawing the bar charts
   *@param  ucContentIn  The update controller for the changing the set of bar
   *      columns in chart
   */
  public RingBPM(UpdatingEventController ucIn, UpdatingEventController ucContentIn) {

    uc = ucIn;
    ucContent = ucContentIn;

    bpm_x = new RingBPMtbtAvg(uc, ucContent);
    bpm_y = new RingBPMtbtAvg(uc, ucContent);
    bpm_amp = new RingBPMtbtAvg(uc, ucContent);

    bpm_x.setStackCapacity(stackCapacity);
    bpm_x.setBPMName(ringBPM_name);
    bpm_x.setStartIndex(startAvgInd);
    bpm_x.setStopIndex(stopAvgInd);
    bpm_x.setSwitchedOn(switchedOn);

    bpm_y.setStackCapacity(stackCapacity);
    bpm_y.setBPMName(ringBPM_name);
    bpm_y.setStartIndex(startAvgInd);
    bpm_y.setStopIndex(stopAvgInd);
    bpm_y.setSwitchedOn(switchedOn);

    bpm_amp.setStackCapacity(stackCapacity);
    bpm_amp.setBPMName(ringBPM_name);
    bpm_amp.setStartIndex(startAvgInd);
    bpm_amp.setStopIndex(stopAvgInd);
    bpm_amp.setSwitchedOn(switchedOn);

    cntrlButton.addActionListener(
      new ActionListener() {
        public void actionPerformed(ActionEvent evn) {
          switchedOn = cntrlButton.isSelected();
          bpm_x.setSwitchedOn(switchedOn);
          bpm_y.setSwitchedOn(switchedOn);
          bpm_amp.setSwitchedOn(switchedOn);
        }
      });
  }


  /**
   *  Gets the onButton attribute of the RingBPM object
   *
   *@return    The onButton value
   */
  public JRadioButton getOnButton() {
    return cntrlButton;
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
    bpm_x.setBPMName(ringBPM_name);
    bpm_y.setBPMName(ringBPM_name);
    bpm_amp.setBPMName(ringBPM_name);
    cntrlButton.setText(ringBPM_name + "     ");
  }


  /**
   *  Sets the start index to calculate an average value from the waveform
   *
   *@param  startAvgInd  The new start index
   */
  public void setStartIndex(int startAvgInd) {
    this.startAvgInd = startAvgInd;
    bpm_x.setStartIndex(startAvgInd);
    bpm_y.setStartIndex(startAvgInd);
    bpm_amp.setStartIndex(startAvgInd);
  }


  /**
   *  Sets the stop index to calculate an average value from the waveform
   *
   *@param  stopAvgInd  The new stop index
   */
  public void setStopIndex(int stopAvgInd) {
    this.stopAvgInd = stopAvgInd;
    bpm_x.setStopIndex(stopAvgInd);
    bpm_y.setStopIndex(stopAvgInd);
    bpm_amp.setStopIndex(stopAvgInd);
  }

  /**
   *  Sets the plus index to calculate a difference between two turns
   *
   *@param  plusInd  The new index
   */
  public void setPlusDiffIndex(int plusInd) {
    this.plusInd = plusInd;
    bpm_x.getRingBPMtsDiff().setPlusIndex(plusInd);
    bpm_y.getRingBPMtsDiff().setPlusIndex(plusInd);
    bpm_amp.getRingBPMtsDiff().setPlusIndex(plusInd);
  }


  /**
   *  Sets the minus index to calculate a difference between two turns
   *
   *@param  minusInd  The new index
   */
  public void setMinusDiffIndex(int minusInd) {
    this.minusInd = minusInd;
    bpm_x.getRingBPMtsDiff().setMinusIndex(minusInd);
    bpm_y.getRingBPMtsDiff().setMinusIndex(minusInd);
    bpm_amp.getRingBPMtsDiff().setMinusIndex(minusInd);
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
	 *  Returns the plus index to calculate a difference between two turns
	 *
	 *@return    The index
	 */
	public int getPlusDiffIndex() {
		return plusInd;
	}


	/**
	 *  Returns the minus index to calculate a difference between two turns
	 *
	 *@return    The index
	 */
	public int getMinusDiffIndex() {
		return minusInd;
	}

  /**
   *  Sets the switched-On state for all x,y, and amplitudes RingBPMtbtAvg object
   *
   *@param  switchedOn  The new switched-On state
   */
  public void setSwitchedOn(boolean switchedOn) {
    this.switchedOn = switchedOn;
    bpm_x.setSwitchedOn(switchedOn);
    bpm_y.setSwitchedOn(switchedOn);
    bpm_amp.setSwitchedOn(switchedOn);
    cntrlButton.setSelected(switchedOn);
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
	 *  Clear results of analysis for all BPMs
	 */
	public void clearAnalysis() {
    bpm_x.clearAnalysis();
    bpm_y.clearAnalysis();
    bpm_amp.clearAnalysis();
	}

  /**
   *  Returns the TBT BPM channel for X
   *
   *@return    The TBT BPM channel for X
   */
  public RingBPMtbtAvg getBPM_X() {
    return bpm_x;
  }


  /**
   *  Returns the TBT BPM channel for Y
   *
   *@return    The TBT BPM channel for Y
   */
  public RingBPMtbtAvg getBPM_Y() {
    return bpm_y;
  }


  /**
   *  Returns the TBT BPM channel for Amplitude
   *
   *@return    The TBT BPM channel for Amplitude
   */
  public RingBPMtbtAvg getBPM_AMP() {
    return bpm_amp;
  }


  /**
   *  Sets the names for x,y, and amplitude TBT ring BPM's PVs
   *
   *@param  xPV    The new name x-TBT-PV
   *@param  yPV    The new name y-TBT-PV
   *@param  ampPV  The new name amp-TBT-PV
   */
  public void setNamesPV(String xPV, String yPV, String ampPV) {
    bpm_x.setNamePV(xPV);
    bpm_y.setNamePV(yPV);
    bpm_amp.setNamePV(ampPV);
  }


  /**
   *  Sets the font for the radio button
   *
   *@param  fnt  The new font
   */
  public void setFont(Font fnt) {
    cntrlButton.setFont(fnt);
  }


  /**
   *  Sets the stack capacity
   *
   *@param  stackCapacity  The new stack capacity value
   */
  public void setStackCapacity(int stackCapacity) {
    this.stackCapacity = stackCapacity;
    bpm_x.setStackCapacity(stackCapacity);
    bpm_y.setStackCapacity(stackCapacity);
    bpm_amp.setStackCapacity(stackCapacity);
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
   *  Clear the memory data stack
   */
  public void clearStack() {
    bpm_x.clearStack();
    bpm_y.clearStack();
    bpm_amp.clearStack();
  }


  /**
   *  Sets the "listen to EPICS" attribute
   *
   *@param  listenToEPICS  The new listen to EPICS boolean value
   */
  public void setListenToEPICS(boolean listenToEPICS) {
    this.listenToEPICS = listenToEPICS;
    bpm_x.setListenToEPICS(listenToEPICS);
    bpm_y.setListenToEPICS(listenToEPICS);
    bpm_amp.setListenToEPICS(listenToEPICS);
  }


}

