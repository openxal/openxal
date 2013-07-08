package xal.app.ringbpmviewer;

import javax.swing.*;
import java.awt.event.*;
import java.awt.*;

import xal.tools.scan.UpdatingEventController;

import xal.tools.plot.barchart.*;

/**
 *  The container of the three TrLineBPMpv instances fro x,y, and amplitude
 *  transfer line BPM PVs
 *
 *@author     shishlo
 */
public class TrLineBPM {

	private JRadioButton cntrlButton = new JRadioButton("None", false);

	private String trLineBPM_name = "None";

	private volatile int stackCapacity = 5;
	private volatile boolean switchedOn = false;

	private volatile boolean disabled = false;

	private TrLineBPMpv bpm_x = null;
	private TrLineBPMpv bpm_y = null;
	private TrLineBPMpv bpm_amp = null;

	private boolean listenToEPICS = false;

	//update controller for the redrawing the bar charts
	private UpdatingEventController uc = null;

	//update controller for the changing the set of bar columns in charts
	private UpdatingEventController ucContent = null;

	/**
	 *  Constructor for the TrLineBPM object
	 *
	 *@param  ucIn         The update controller for the redrawing the bar charts
	 *@param  ucContentIn  The update controller for the changing the set of bar
	 *      columns in chart
	 */
	public TrLineBPM(UpdatingEventController ucIn, UpdatingEventController ucContentIn) {

		uc = ucIn;
		ucContent = ucContentIn;

		bpm_x = new TrLineBPMpv(uc, ucContent);
		bpm_y = new TrLineBPMpv(uc, ucContent);
		bpm_amp = new TrLineBPMpv(uc, ucContent);

		bpm_x.setStackCapacity(stackCapacity);
		bpm_x.setBPMName(trLineBPM_name);
		bpm_x.setSwitchedOn(switchedOn);

		bpm_y.setStackCapacity(stackCapacity);
		bpm_y.setBPMName(trLineBPM_name);
		bpm_y.setSwitchedOn(switchedOn);

		bpm_amp.setStackCapacity(stackCapacity);
		bpm_amp.setBPMName(trLineBPM_name);
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
	 *  Gets the onButton attribute of the TrLineBPM object
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
		return trLineBPM_name;
	}


	/**
	 *  Sets the BPM name
	 *
	 *@param  trLineBPM_name  The new BPM name
	 */
	public void setBPMName(String trLineBPM_name) {
		this.trLineBPM_name = trLineBPM_name;
		bpm_x.setBPMName(trLineBPM_name);
		bpm_y.setBPMName(trLineBPM_name);
		bpm_amp.setBPMName(trLineBPM_name);
		cntrlButton.setText(trLineBPM_name + "     ");
	}

	/**
	 *  Sets the switched-On state for all x,y, and amplitudes TrLineBPMpv object
	 *
	 *@param  switchedOn  The new switched-On state
	 */
	public void setSwitchedOn(boolean switchedOn) {
		if(disabled){
			switchedOn = false;
		}
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
	 *  Returns the TBT BPM channel for X
	 *
	 *@return    The TBT BPM channel for X
	 */
	public TrLineBPMpv getBPM_X() {
		return bpm_x;
	}


	/**
	 *  Returns the TBT BPM channel for Y
	 *
	 *@return    The TBT BPM channel for Y
	 */
	public TrLineBPMpv getBPM_Y() {
		return bpm_y;
	}


	/**
	 *  Returns the TBT BPM channel for Amplitude
	 *
	 *@return    The TBT BPM channel for Amplitude
	 */
	public TrLineBPMpv getBPM_AMP() {
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


	/**
	 *  Sets the disabled attribute of the TrLineBPM object
	 *
	 *@param  disabled  The new disabled value
	 */
	public void setDisabled(boolean disabled) {
		this.disabled = disabled;
		if(disabled) {
			setListenToEPICS(false);
			clearStack();
			setSwitchedOn(false);
			cntrlButton.setEnabled(false);
		}
	}

	/**
	 *  Returns the disabled attribute of the TrLineBPM object
	 *
	 *@return    The disabled value
	 */
	public boolean isDisabled() {
		return disabled;
	}
}

