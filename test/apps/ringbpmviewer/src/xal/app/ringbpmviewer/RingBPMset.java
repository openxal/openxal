package xal.app.ringbpmviewer;

import xal.extension.scan.UpdatingEventController;
import xal.tools.xml.XmlDataAdaptor;
import xal.tools.data.DataAdaptor;
import xal.extension.widgets.plot.barchart.*;

import java.awt.Font;
import java.util.Vector;

/**
 *  The container of the RingBPM instances to provide group operation over the
 *  whole set
 *
 *@author     shishlo
 */
public class RingBPMset {

	private final Vector<RingBPM> bpmV = new Vector<RingBPM>();

	private int startAvgInd = 0;
	private int stopAvgInd = 0;

	private int plusInd = 0;
	private int minusInd = 1;

	private volatile int stackCapacity = 5;

	private boolean listenToEPICS = false;

	//update controller for the redrawing the bar charts
	private UpdatingEventController uc = null;

	//update controller for the changing the set of bar columns in charts
	private UpdatingEventController ucContent = null;


	/**
	 *  Constructor for the RingBPMset object
	 *
	 *@param  ucIn         The update controller for the redrawing the bar charts
	 *@param  ucContentIn  The update controller for the changing the set of bar
	 *      columns in charts
	 */
	public RingBPMset(UpdatingEventController ucIn, UpdatingEventController ucContentIn) {

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
	 *  Returns the ringBPM instance
	 *
	 *@param  index  Description of the Parameter
	 *@return        The ringBPM value
	 */
	public RingBPM getRingBPM(int index) {
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
			RingBPM rbpm = bpmV.get(i);
			v.add(rbpm.getBPM_X());
		}
		return v;
	}


	/**
	 *  Returns all ring bpms PVs for X-position difference
	 *
	 *@return    The vector with all ring bpms PVs for Xlitude
	 */
	public Vector<BarColumn> getAllBPMs_DiffX() {
		Vector<BarColumn> v = new Vector<BarColumn>();
		for(int i = 0, n = bpmV.size(); i < n; i++) {
			RingBPM rbpm = bpmV.get(i);
			v.add(rbpm.getBPM_X().getRingBPMtsDiff());
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
			RingBPM rbpm = bpmV.get(i);
			RingBPMtbtAvg rbpmPV = rbpm.getBPM_X();
			if(rbpmPV.show()) {
				v.add(rbpmPV);
			}
		}
		return v;
	}


	/**
	 *  Returns all ring bpms PVs with show=true for X-position difference
	 *
	 *@return    The vector with all ring bpms PVs with show=true for X-position
	 */
	public Vector<BarColumn> getAllForShowBPMs_DiffX() {
		Vector<BarColumn> v = new Vector<BarColumn>();
		for(int i = 0, n = bpmV.size(); i < n; i++) {
			RingBPM rbpm = bpmV.get(i);
			RingBPMtbtAvg rbpmPV = rbpm.getBPM_X();
			if(rbpmPV.show()) {
				v.add(rbpmPV.getRingBPMtsDiff());
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
			RingBPM rbpm = bpmV.get(i);
			v.add(rbpm.getBPM_Y());
		}
		return v;
	}

	/**
	 *  Returns all ring bpms PVs for y-position difference
	 *
	 *@return    The vector with all ring bpms PVs for y-position
	 */
	public Vector<BarColumn> getAllBPMs_DiffY() {
		Vector<BarColumn> v = new Vector<BarColumn>();
		for(int i = 0, n = bpmV.size(); i < n; i++) {
			RingBPM rbpm = bpmV.get(i);
			v.add(rbpm.getBPM_Y().getRingBPMtsDiff());
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
			RingBPM rbpm = bpmV.get(i);
			RingBPMtbtAvg rbpmPV = rbpm.getBPM_Y();
			if(rbpmPV.show()) {
				v.add(rbpmPV);
			}
		}
		return v;
	}


	/**
	 *  Returns all ring bpms PVs with show=true for Y-position difference
	 *
	 *@return    The vector with all ring bpms PVs with show=true for Y-position
	 */
	public Vector<BarColumn> getAllForShowBPMs_DiffY() {
		Vector<BarColumn> v = new Vector<BarColumn>();
		for(int i = 0, n = bpmV.size(); i < n; i++) {
			RingBPM rbpm = bpmV.get(i);
			RingBPMtbtAvg rbpmPV = rbpm.getBPM_Y();
			if(rbpmPV.show()) {
				v.add(rbpmPV.getRingBPMtsDiff());
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
			RingBPM rbpm = bpmV.get(i);
			v.add(rbpm.getBPM_AMP());
		}
		return v;
	}

	/**
	 *  Returns all ring bpms PVs for amplitude difference
	 *
	 *@return    The vector with all ring bpms PVs for amplitude
	 */
	public Vector<BarColumn> getAllBPMs_DiffAMP() {
		Vector<BarColumn> v = new Vector<BarColumn>();
		for(int i = 0, n = bpmV.size(); i < n; i++) {
			RingBPM rbpm = bpmV.get(i);
			v.add(rbpm.getBPM_AMP().getRingBPMtsDiff());
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
			RingBPM rbpm = bpmV.get(i);
			RingBPMtbtAvg rbpmPV = rbpm.getBPM_AMP();
			if(rbpmPV.show()) {
				v.add(rbpmPV);
			}
		}
		return v;
	}

	/**
	 *  Returns all ring bpms PVs with show=true for amplitude difference
	 *
	 *@return    The vector with all ring bpms PVs with show=true for amplitude
	 */
	public Vector<BarColumn> getAllForShowBPMs_DiffAMP() {
		Vector<BarColumn> v = new Vector<BarColumn>();
		for(int i = 0, n = bpmV.size(); i < n; i++) {
			RingBPM rbpm = bpmV.get(i);
			RingBPMtbtAvg rbpmPV = rbpm.getBPM_AMP();
			if(rbpmPV.show()) {
				v.add(rbpmPV.getRingBPMtsDiff());
			}
		}
		return v;
	}


	/**
	 *  Adds the RingBPM instance to the set
	 *
	 *@param  rbpm  The RingBPM instance
	 */
	public void addRingBPM(RingBPM rbpm) {
		bpmV.add(rbpm);
	}


	/**
	 *  Sets the start index to calculate an average value from the waveform
	 *
	 *@param  startAvgInd  The new start index
	 */
	public void setStartIndex(int startAvgInd) {
		this.startAvgInd = startAvgInd;
		for(int i = 0, n = bpmV.size(); i < n; i++) {
			( bpmV.get(i)).setStartIndex(startAvgInd);
		}
	}


	/**
	 *  Sets the stop index to calculate an average value from the waveform
	 *
	 *@param  stopAvgInd  The new stop index
	 */
	public void setStopIndex(int stopAvgInd) {
		this.stopAvgInd = stopAvgInd;
		for(int i = 0, n = bpmV.size(); i < n; i++) {
			(bpmV.get(i)).setStopIndex(stopAvgInd);
		}
	}

	/**
	 *  Sets the plus index to calculate a difference between two turns
	 *
	 *@param  plusInd  The new index
	 */
	public void setPlusDiffIndex(int plusInd) {
		this.plusInd = plusInd;
		for(int i = 0, n = bpmV.size(); i < n; i++) {
			(bpmV.get(i)).setPlusDiffIndex(plusInd);
		}
	}


	/**
	 *  Sets the minus index to calculate a difference between two turns
	 *
	 *@param  minusInd    The new minusDiffIndex value
	 */
	public void setMinusDiffIndex(int minusInd) {
		this.minusInd = minusInd;
		for(int i = 0, n = bpmV.size(); i < n; i++) {
			(bpmV.get(i)).setMinusDiffIndex(minusInd);
		}
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
	 *  Clear results of analysis for all BPMs
	 */
	public void clearAnalysis() {
		for(int i = 0, n = bpmV.size(); i < n; i++) {
			( bpmV.get(i)).clearAnalysis();
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
			(bpmV.get(i)).setSwitchedOn(switchedOn);
		}
	}


	/**
	 *  Sets the font for RingBPM buttons
	 *
	 *@param  fnt  The new font
	 */
	public void setFont(Font fnt) {
		for(int i = 0, n = bpmV.size(); i < n; i++) {
			(bpmV.get(i)).setFont(fnt);
		}
	}


	/**
	 *  Initializes the set of ring BPM instances from the XML Data Adapter
	 *
	 *@param  bpms_da  The XML Data Adapter
	 */
	public void init(DataAdaptor bpms_da) {
		for (final DataAdaptor bpm_da : bpms_da.childAdaptors("RING_BPM")){
			DataAdaptor bpm_pvs_da =  bpm_da.childAdaptor("PV_NAMES");
			DataAdaptor bpm_x_da = bpm_pvs_da.childAdaptor("xTBT");
			DataAdaptor bpm_y_da = bpm_pvs_da.childAdaptor("yTBT");
			DataAdaptor bpm_amp_da = bpm_pvs_da.childAdaptor("ampTBT");
			String bpm_name = bpm_da.stringValue("name");
			String xPV_name = bpm_x_da.stringValue("name");
			String yPV_name = bpm_y_da.stringValue("name");
			String ampPV_name = bpm_amp_da.stringValue("name");

			RingBPM bpm = new RingBPM(uc, ucContent);
			bpm.setBPMName(bpm_name);
			bpm.setNamesPV(xPV_name, yPV_name, ampPV_name);
			bpm.setSwitchedOn(false);
			bpm.setListenToEPICS(false);
			addRingBPM(bpm);
		}
	}

}

