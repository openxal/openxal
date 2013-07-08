package xal.app.ringbpmviewer;
import java.util.*;
import java.awt.*;

import xal.tools.xml.*;
import xal.tools.scan.UpdatingEventController;
import xal.tools.data.DataAdaptor;
import xal.smf.impl.BPM;

import xal.tools.plot.barchart.*;

/**
 *  The container of the TrLineBPM instances to provide group operation over the
 *  whole set
 *
 *@author     shishlo
 */
public class TrLineBPMset {

	private Vector<TrLineBPM> bpmV = new Vector<TrLineBPM>();

	private volatile int stackCapacity = 5;

	private boolean listenToEPICS = false;

	//update controller for the redrawing the bar charts
	private UpdatingEventController uc = null;

	//update controller for the changing the set of bar columns in charts
	private UpdatingEventController ucContent = null;


	/**
	 *  Constructor for the TrLineBPMset object
	 *
	 *@param  ucIn         The update controller for the redrawing the bar charts
	 *@param  ucContentIn  The update controller for the changing the set of bar
	 *      columns in charts
	 */
	public TrLineBPMset(UpdatingEventController ucIn, UpdatingEventController ucContentIn) {

		uc = ucIn;
		ucContent = ucContentIn;

	}


	/**
	 *  Returns the number of BPMs instances
	 *
	 *@return    The number of BPMs instances
	 */
	public int size() {
		return bpmV.size();
	}


	/**
	 *  Removes all BPMs instances
	 */
	public void clear() {
		bpmV.clear();
	}


	/**
	 *  Returns the trLineBPM instance
	 *
	 *@param  index  Description of the Parameter
	 *@return        The trLineBPM value
	 */
	public TrLineBPM getTrLineBPM(int index) {
		return bpmV.get(index);
	}


	/**
	 *  Returns all ring bpms PVs for x-position
	 *
	 *@return    The vector with all ring bpms PVs for x-position
	 */
	public Vector<BarColumn> getAllBPMs_X() {
		Vector<BarColumn> v = new Vector<BarColumn>();
		for(int i = 0, n = bpmV.size(); i < n; i++) {
			TrLineBPM rbpm = bpmV.get(i);
			v.add(rbpm.getBPM_X());
		}
		return v;
	}


	/**
	 *  Returns all ring bpms PVs with show=true for x-position
	 *
	 *@return    The vector with all ring bpms PVs with show=true for x-position
	 */
	public Vector<BarColumn> getAllForShowBPMs_X() {
		Vector<BarColumn> v = new Vector<BarColumn>();
		for(int i = 0, n = bpmV.size(); i < n; i++) {
			TrLineBPM rbpm = bpmV.get(i);
			TrLineBPMpv rbpmPV = rbpm.getBPM_X();
			if(rbpmPV.show()) {
				v.add(rbpmPV);
			}
		}
		return v;
	}


	/**
	 *  Returns all ring bpms PVs for y-position
	 *
	 *@return    The vector with all ring bpms PVs for y-position
	 */
	public Vector<BarColumn> getAllBPMs_Y() {
		Vector<BarColumn> v = new Vector<BarColumn>();
		for(int i = 0, n = bpmV.size(); i < n; i++) {
			TrLineBPM rbpm =  bpmV.get(i);
			v.add(rbpm.getBPM_Y());
		}
		return v;
	}


	/**
	 *  Returns all ring bpms PVs with show=true for y-position
	 *
	 *@return    The vector with all ring bpms PVs with show=true for y-position
	 */
	public Vector<BarColumn> getAllForShowBPMs_Y() {
		Vector<BarColumn> v = new Vector<BarColumn>();
		for(int i = 0, n = bpmV.size(); i < n; i++) {
			TrLineBPM rbpm =  bpmV.get(i);
			TrLineBPMpv rbpmPV = rbpm.getBPM_Y();
			if(rbpmPV.show()) {
				v.add(rbpmPV);
			}
		}
		return v;
	}


	/**
	 *  Returns all ring bpms PVs for amplitude
	 *
	 *@return    The vector with all ring bpms PVs for amplitude
	 */
	public Vector<BarColumn> getAllBPMs_AMP() {
		Vector<BarColumn> v = new Vector<BarColumn>();
		for(int i = 0, n = bpmV.size(); i < n; i++) {
			TrLineBPM rbpm = bpmV.get(i);
			v.add(rbpm.getBPM_AMP());
		}
		return v;
	}


	/**
	 *  Returns all ring bpms PVs with show=true for amplitude
	 *
	 *@return    The vector with all ring bpms PVs with show=true for amplitude
	 */
	public Vector<BarColumn> getAllForShowBPMs_AMP() {
		Vector<BarColumn> v = new Vector<BarColumn>();
		for(int i = 0, n = bpmV.size(); i < n; i++) {
			TrLineBPM rbpm =  bpmV.get(i);
			TrLineBPMpv rbpmPV = rbpm.getBPM_AMP();
			if(rbpmPV.show()) {
				v.add(rbpmPV);
			}
		}
		return v;
	}


	/**
	 *  Adds the TrLineBPM instance to the set
	 *
	 *@param  rbpm  The TrLineBPM instance
	 */
	public void addTrLineBPM(TrLineBPM rbpm) {
		bpmV.add(rbpm);
	}


	/**
	 *  Sets the stack capacity
	 *
	 *@param  stackCapacity  The new stack capacity value
	 */
	public void setStackCapacity(int stackCapacity) {
		this.stackCapacity = stackCapacity;
		for(int i = 0, n = bpmV.size(); i < n; i++) {
			( bpmV.get(i)).setStackCapacity(stackCapacity);
		}
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
		for(int i = 0, n = bpmV.size(); i < n; i++) {
			(bpmV.get(i)).clearStack();
		}
	}

	/**
	 *  Sets the "listen to EPICS" attribute
	 *
	 *@param  listenToEPICS  The new listen to EPICS boolean value
	 */
	public void setListenToEPICS(boolean listenToEPICS) {
		this.listenToEPICS = listenToEPICS;
		for(int i = 0, n = bpmV.size(); i < n; i++) {
			(bpmV.get(i)).setListenToEPICS(listenToEPICS);
		}
	}


	/**
	 *  Returns "true" or "false" for the "listen to EPICS"
	 *
	 *@return    The listen to EPICS boolean property
	 */
	public boolean getListenToEPICS() {
		return listenToEPICS;
	}


	/**
	 *  Sets the switched-On state for all BPMs
	 *
	 *@param  switchedOn  The new switched-On state
	 */
	public void setSwitchedOn(boolean switchedOn) {
		for(int i = 0, n = bpmV.size(); i < n; i++) {
			( bpmV.get(i)).setSwitchedOn(switchedOn);
		}
	}


	/**
	 *  Sets the font for TrLineBPM buttons
	 *
	 *@param  fnt  The new font
	 */
	public void setFont(Font fnt) {
		for(int i = 0, n = bpmV.size(); i < n; i++) {
			( bpmV.get(i)).setFont(fnt);
		}
	}


	/**
	 *  Initializes the set of ring BPM instances from the XML Data Adapter
	 *
	 *@param  bpms_da  The Parameter
	 */
	public void init(DataAdaptor bpms_da) {
        for (final DataAdaptor bpm_da : bpms_da.childAdaptors("TRANSF_LINE_BPM")) {
			DataAdaptor bpm_pvs_da =  bpm_da.childAdaptor("PV_NAMES");
			DataAdaptor bpm_x_da =  bpm_pvs_da.childAdaptor("xAvg");
			DataAdaptor bpm_y_da =  bpm_pvs_da.childAdaptor("yAvg");
			DataAdaptor bpm_amp_da =  bpm_pvs_da.childAdaptor("amplitudeAvg");
			String bpm_name = bpm_da.stringValue("name");
			String xPV_name = bpm_x_da.stringValue("name");
			String yPV_name = bpm_y_da.stringValue("name");
			String ampPV_name = bpm_amp_da.stringValue("name");

			TrLineBPM bpm = new TrLineBPM(uc, ucContent);
			bpm.setBPMName(bpm_name);
			bpm.setNamesPV(xPV_name, yPV_name, ampPV_name);
			bpm.setSwitchedOn(false);
			bpm.setListenToEPICS(false);

			if(bpm_da.hasAttribute("disabled")) {
				if(bpm_da.booleanValue("disabled")) {
					bpm.setDisabled(bpm_da.booleanValue("disabled"));
				}
			}

			addTrLineBPM(bpm);
		}
	}

}

