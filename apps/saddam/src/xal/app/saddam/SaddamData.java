/*
 * SaddamDataTable.java
 *
 * Created on June 14, 2004
 */

package xal.app.saddam;

import java.util.*;
import java.net.*;
import java.io.*;

import xal.tools.data.*;

/**
 * This class contains the data table objects of the 
 * saddam application.
 *
 * @author  jdg
 */
public class SaddamData {
	
	
	
	/** the data table that holds the device/signal*/
	protected DataTable theTable;
	
	/** the data table that holds selected values that a signal is alloed to have */
	protected DataTable valueTable;
	
	/** a table that contains special substitutions for entire PV name*/
	protected HashMap<String, String> substitutionMap;
	
	public SaddamData() {
		makeTables();
		addRecords();		
		
	}
	
	/** set up the table schema */
	
	private void makeTables() {
		
		makeTheTable();
		makeValueTable();
		makeSubMap();
	}
	
	protected void makeTheTable() {
		ArrayList<DataAttribute> attributes = new ArrayList<DataAttribute>();
		attributes.add(new DataAttribute("device", new String("").getClass(), true));
		attributes.add(new DataAttribute("signal", new String("").getClass(), true));
		attributes.add(new DataAttribute("value", new String("").getClass(), false));
		attributes.add(new DataAttribute("oldString", new String("").getClass(), false));
		attributes.add(new DataAttribute("newString", new String("").getClass(), false));
		attributes.add(new DataAttribute("usePhaseWrap", new Boolean(false).getClass(), false));		
		theTable = new DataTable("Saddam Table", attributes);
	}
	
	protected void makeValueTable() {
		ArrayList<DataAttribute> attributes = new ArrayList<DataAttribute>();
		attributes.add(new DataAttribute("device", new String("").getClass(), true));
		attributes.add(new DataAttribute("signal", new String("").getClass(), true));
		attributes.add(new DataAttribute("setValue", new String("").getClass(), true));	
		valueTable = new DataTable("Value Table", attributes);		
	}

	protected void makeSubMap() {
		
		// known misspellings from assumed naming convention
		
		substitutionMap = new HashMap<String,String>();
		substitutionMap.put("MEBT_RF:Bnch01:CtlPhaseSet", "MEBT_LLRF:FCM1:CtlPhaseSet");
		substitutionMap.put("MEBT_RF:Bnch02:CtlPhaseSet", "MEBT_LLRF:FCM2:CtlPhaseSet");
		substitutionMap.put("MEBT_RF:Bnch03:CtlPhaseSet", "MEBT_LLRF:FCM3:CtlPhaseSet");
		substitutionMap.put("MEBT_RF:Bnch04:CtlPhaseSet", "MEBT_LLRF:FCM4:CtlPhaseSet");
		substitutionMap.put("RFQ:RF:CtlPhaseSet", "RFQ_LLRF:FCM1:CtlPhaseSet");
		substitutionMap.put("DTL1:CtlPhaseSet", "DTL_LLRF:FCM1:CtlPhaseSet");
		substitutionMap.put("DTL2:CtlPhaseSet", "DTL_LLRF:FCM2:CtlPhaseSet");
		substitutionMap.put("DTL3:CtlPhaseSet", "DTL_LLRF:FCM3:CtlPhaseSet");
		substitutionMap.put("DTL4:CtlPhaseSet", "DTL_LLRF:FCM4:CtlPhaseSet");
		substitutionMap.put("DTL5:CtlPhaseSet", "DTL_LLRF:FCM5:CtlPhaseSet");
		substitutionMap.put("DTL6:CtlPhaseSet", "DTL_LLRF:FCM6:CtlPhaseSet");
		substitutionMap.put("CCL1:CtlPhaseSet", "CCL_LLRF:FCM1:CtlPhaseSet");
		substitutionMap.put("CCL2:CtlPhaseSet", "CCL_LLRF:FCM2:CtlPhaseSet");
		substitutionMap.put("CCL3:CtlPhaseSet", "CCL_LLRF:FCM3:CtlPhaseSet");
		substitutionMap.put("CCL4:CtlPhaseSet", "CCL_LLRF:FCM4:CtlPhaseSet");
	}
	
	/** add records to the table */
	private void addRecords() {
		
		// BPM timing:
		GenericRecord record = new GenericRecord(theTable);
		record.setValueForKey("BPM", "device");
		record.setValueForKey("Delay00", "signal");
		theTable.add(record);

		record = new GenericRecord(theTable);
		record.setValueForKey("BPM", "device");
		record.setValueForKey("pulNum", "signal");
		theTable.add(record);
		
		record = new GenericRecord(theTable);
		record.setValueForKey("BPM", "device");
		record.setValueForKey("chopFreq", "signal");
		theTable.add(record);

		record = new GenericRecord(theTable);
		record.setValueForKey("BPM", "device");
		record.setValueForKey("IntgrlLength", "signal");
		theTable.add(record);

		record = new GenericRecord(theTable);
		record.setValueForKey("BPM", "device");
		record.setValueForKey("pulAv", "signal");
		theTable.add(record);

		record = new GenericRecord(theTable);
		record.setValueForKey("BPM", "device");
		record.setValueForKey("t0Dly", "signal");
		theTable.add(record);
		
		record = new GenericRecord(theTable);
		record.setValueForKey("BPM", "device");
		record.setValueForKey("avgStart", "signal");
		theTable.add(record);

		record = new GenericRecord(theTable);
		record.setValueForKey("BPM", "device");
		record.setValueForKey("avgStop", "signal");
		theTable.add(record);
		
		// WS timing:
		record = new GenericRecord(theTable);
		record.setValueForKey("WS", "device");
		record.setValueForKey("uSecDly", "signal");
		record.setValueForKey(":WS", "oldString");
		record.setValueForKey(":Gate_WS", "newString");
		theTable.add(record);
		
		// WS tigger:
		record = new GenericRecord(theTable);
		record.setValueForKey("WS", "device");
		record.setValueForKey("TrigSel", "signal");
		record.setValueForKey(":WS", "oldString");
		record.setValueForKey(":Gate_WS", "newString");
		theTable.add(record);	
		
		// ND high voltage setting
		record = new GenericRecord(theTable);		record.setValueForKey("ND", "device");
		record.setValueForKey("VDAC:SET", "signal");
		theTable.add(record);

		// WS trigger values
		record = new GenericRecord(theTable);
		record.setValueForKey("WS", "device");
		record.setValueForKey("NumSamples", "signal");
		valueTable.add(record);	
		
		record = new GenericRecord(theTable);
		record.setValueForKey("WS", "device");
		record.setValueForKey("TrigSel", "signal");
		record.setValueForKey("Beam-On", "setValue");		
		valueTable.add(record);			
		
		// WS timing:
		record = new GenericRecord(theTable);
		record.setValueForKey("WS", "device");
		record.setValueForKey("TrigSel", "signal");
		record.setValueForKey("Snap-Shot", "setValue");		
		valueTable.add(record);
		
		// WS timing:
		record = new GenericRecord(theTable);
		record.setValueForKey("WS", "device");
		record.setValueForKey("TrigSel", "signal");
		record.setValueForKey("Slow-Trigger", "setValue");		
		valueTable.add(record);

		// WS timing:
		record = new GenericRecord(theTable);
		record.setValueForKey("WS", "device");
		record.setValueForKey("TrigSel", "signal");
		record.setValueForKey("Fast-Trigger", "setValue");		
		valueTable.add(record);			
		
		// RF phase
		record = new GenericRecord(theTable);
		record.setValueForKey("RF", "device");
		record.setValueForKey("CtlPhaseSet", "signal");
		record.setValueForKey("L_RF:Cav", "oldString");
		record.setValueForKey("L_LLRF:FCM", "newString");
		record.setValueForKey(new Boolean(true), "usePhaseWrap");
		theTable.add(record);

		// RF HPM limit 0:SCL_LLRF:HPM14d:UsrRFLim0_Pwr
		record = new GenericRecord(theTable);
		record.setValueForKey("RF", "device");
		record.setValueForKey("UsrRFLim0_Pwr", "signal");
		record.setValueForKey("RF:Cav", "oldString");
		record.setValueForKey("LLRF:HPM", "newString");
		theTable.add(record);

		// RF HPM limit 1:SCLLRF:HPM14d:UsrRFLim0_Pwr
		record = new GenericRecord(theTable);
		record.setValueForKey("RF", "device");
		record.setValueForKey("UsrRFLim1_Pwr", "signal");
		record.setValueForKey("RF:Cav", "oldString");
		record.setValueForKey("LLRF:HPM", "newString");
		theTable.add(record);


		// RF HPM limit 2:SCLLRF:HPM14d:UsrRFLim0_Pwr
		record = new GenericRecord(theTable);
		record.setValueForKey("RF", "device");
		record.setValueForKey("UsrRFLim2_Pwr", "signal");
		record.setValueForKey("RF:Cav", "oldString");
		record.setValueForKey("LLRF:HPM", "newString");
		theTable.add(record);

		// RF HPM limit 3:SCLLRF:HPM14d:UsrRFLim0_Pwr
		record = new GenericRecord(theTable);
		record.setValueForKey("RF", "device");
		record.setValueForKey("UsrRFLim3_Pwr", "signal");
		record.setValueForKey("RF:Cav", "oldString");
		record.setValueForKey("LLRF:HPM", "newString");
		theTable.add(record);

		// RF HPM limit 4:SCLLRF:HPM14d:UsrRFLim0_Pwr
		record = new GenericRecord(theTable);
		record.setValueForKey("RF", "device");
		record.setValueForKey("UsrRFLim4_Pwr", "signal");
		record.setValueForKey("RF:Cav", "oldString");
		record.setValueForKey("LLRF:HPM", "newString");
		theTable.add(record);	


		// RF HPM limit 5:SCLLRF:HPM14d:UsrRFLim0_Pwr
		record = new GenericRecord(theTable);
		record.setValueForKey("RF", "device");
		record.setValueForKey("UsrRFLim5_Pwr", "signal");
		record.setValueForKey("RF:Cav", "oldString");
		record.setValueForKey("LLRF:HPM", "newString");
		theTable.add(record);

		// RF HPM limit 6:SCLLRF:HPM14d:UsrRFLim0_Pwr
		record = new GenericRecord(theTable);
		record.setValueForKey("RF", "device");
		record.setValueForKey("UsrRFLim6_Pwr", "signal");
		record.setValueForKey("RF:Cav", "oldString");
		record.setValueForKey("LLRF:HPM", "newString");
		theTable.add(record);

	}
	
}

