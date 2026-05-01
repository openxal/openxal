package xal.app.ema;

import java.util.*;
import java.io.*;
import java.net.*;
import javax.swing.*;
import java.awt.event.*;
import java.text.SimpleDateFormat;
import javax.swing.table.TableCellRenderer;

import xal.smf.*;
import xal.smf.impl.*;
import xal.ca.*;
import xal.model.probe.*;

/**
 * A class to controll the actions needed to control the BPM energy calc. actions
 * @author  J. Galambos
 */

public class BPMController {
	
    /** flag indicating top level action status. amRunning indicates monitoring is happening */
    boolean amRunning = false;  
    
    /** The slacs document to deal with */
    protected EmaDocument theDoc;
    
    /** the correlator to grab related sets of BPM readings */
    protected BPMCorrelator bpmCorrelator;
    
    /** the calculator to find the energies from the correlated BPM phase data */
    protected BPMEnergyCalculator bpmCalculator;
    
    /** a map containing the user selected pairs */
    protected HashMap <String, BPMPair> selectedPairs;
    
    /** an array containing the labels only for the user selected pairs */
    protected ArrayList <String> selectedPairNames;

    /** a list of preset bpm pair names to choose from */
    private static ArrayList <String> defaultPairs = new ArrayList <String> ();
    
    /** list of BCMs to pick from for beam current filter */ 
    protected ArrayList <String> theBCMs = new ArrayList <String> (); 
   
    protected BPMTableModel bpmTableModel;
    protected JTable bpmResultsTable;
        
    /** constructor     */
    public BPMController(EmaDocument doc) { 
     theDoc = doc;
     if(defaultPairs.size() < 1) makeDefaultPairs();
     selectedPairs = new HashMap <String, BPMPair>();
     selectedPairNames = new ArrayList  <String>();
     bpmCorrelator = new BPMCorrelator();
     theBCMs.add("None");
     theBCMs.add("MEBT_Diag:BCM02");
     theBCMs.add("DTL_Diag:BCM200");
    }
   
    /** initialize containers used to manage the scan progress */
    protected void initialize() {
	    bpmCalculator = new BPMEnergyCalculator(theDoc.theProbe, this);
	    bpmCorrelator.setCalculator(bpmCalculator);
    }
   	    
    /** start the action */
    protected void startAction() {
		amRunning = true;
    }
    
    
    /** kill the action, empty all pending cavity actions*/
    protected void stopAction() {
	amRunning = false;
    }

	/** pass an error message to the main document from a client */
	protected void dumpErr(String str) {theDoc.dumpErr(str);}
	
	private void makeDefaultPairs() {
		defaultPairs.add("SCL_Diag:BPM23, SCL_Diag:BPM24");
		defaultPairs.add("SCL_Diag:BPM25, SCL_Diag:BPM26");
		defaultPairs.add("SCL_Diag:BPM27, SCL_Diag:BPM28");
		defaultPairs.add("SCL_Diag:BPM29, SCL_Diag:BPM30");
		defaultPairs.add("SCL_Diag:BPM31, SCL_Diag:BPM32");
	}
	
	protected ArrayList <String> getDefaultPairs() {return defaultPairs;}
	
	/** add the user selected pairs to the active correlation list */
	
	protected void addSelectedPairs(Object[] selectedLabels) {
		// do selections from reset list
		for (int i = 0; i< selectedLabels.length; i++) {
			String label = (String) selectedLabels[i];
			if(!selectedPairs.containsKey(label)) {
				int delimit = label.indexOf(",");
				String BPM1name = label.substring(0,delimit);
				String BPM2name = label.substring(delimit+2);
				//System.out.println(BPM1name + " " + BPM2name);
				addPair(BPM1name, BPM2name, label);
			}
		}
		// individual combos
		if(theDoc.myWindow().BPM1List.getSelectedIndex() > -1 && theDoc.myWindow().BPM2List.getSelectedIndex() > 0) {
			String BPM1 = ((BPM) (theDoc.myWindow().BPM1List.getSelectedValue())).getId();
			String BPM2 = ((BPM) (theDoc.myWindow().BPM2List.getSelectedValue())).getId();
			String label = BPM1 + ", " + BPM2;
			if(!selectedPairs.containsKey(label))
				addPair(BPM1, BPM2, label);
		}
		updateBPMTable();
		addToCorrelator();
	}
	
	/** add a single pair of BPMs */
	private void addPair(String BPM1name, String BPM2name, String label) {
		BPM BPM1 = (BPM) theDoc.theSeq.getNodeWithId(BPM1name);
		BPM BPM2 = (BPM) theDoc.theSeq.getNodeWithId(BPM2name);
		double dist = theDoc.theSeq.getPosition(BPM2) - theDoc.theSeq.getPosition(BPM1);
		if(dist <= 0) {
			dumpErr("Hey - you tried to add a pair whith BPM2 not downstream of BPM1!");
			return;
		}
		
		double energy = Double.parseDouble(theDoc.myWindow().energyGuessField.getValue().toString());
		BPMPair pair = new BPMPair(BPM1name, BPM2name);
		pair.setLength(dist);
		pair.setWGuess(energy);
		if(!selectedPairs.containsKey(label)) {
			selectedPairs.put(label, pair);
			selectedPairNames.add(label);
		}
	}
			
	/** add selected BPM pairs to the correlator list */
	protected void addToCorrelator() {
		Collection <BPMPair> pairs = selectedPairs.values();
		for (BPMPair bpmPair : pairs) {
			bpmCorrelator.addPair(bpmPair);
			//String name1 = bpmPair.getBPM1Name();
			//String name2 = bpmPair.getBPM2Name();
			//bpmCorrelator.addName(bpmPair.getChannel1());
			//bpmCorrelator.addName(bpmPair.getChannel2());
		}
		if (theDoc.myWindow().BCMList.getSelectedIndex() > -1 && 
		theDoc.myWindow().BCMList.getSelectedValue() != "None" ) {
			String bcmPVName = (String) theDoc.myWindow().BCMList.getSelectedValue() + ":averageCur";
			bpmCorrelator.addMinCurrentFilter(bcmPVName, Double.parseDouble(theDoc.myWindow().minCurField.getValue().toString()));
		}
	}
	
	/** make a table to display results */
	
	protected void makeBPMResultsTable() {
	    bpmTableModel = new BPMTableModel(theDoc);
	    bpmResultsTable = new JTable(bpmTableModel);
	    //cavScaleTable.setRowSelectionAllowed(true);
	    bpmResultsTable.getColumnModel().getColumn(0).setPreferredWidth(210);
    }
    
   /** update the table data view */
    protected void updateBPMTable() { 
	   theDoc.myWindow().linacBPMDate.setText((new Date()).toString());
	    ((BPMTableModel)bpmResultsTable.getModel()).fireTableDataChanged();
    }
    
    /** start the bpm energy meter */
    protected void startBPMMeter() {
	    bpmCalculator.start();
	    bpmCorrelator.startMonitor();
    }
    
    /** start the bpm energfy meter */
    protected void stopBPMMeter() {
	    // bpmCalculator.start(); // leave this running incase it has more to do
	    bpmCorrelator.stopMonitor(); // just stop posting
    }
    
         /** dump the contents of the avg. energy table to a text file */
    protected void exportAvgTable() {
	    File file;
	    JFileChooser chooser = new JFileChooser();
	    int returnVal = chooser.showOpenDialog(theDoc.myWindow());
	    if(returnVal == JFileChooser.APPROVE_OPTION) {
		   file = chooser.getSelectedFile();
		   try {
			   OutputStream ofStream = new FileOutputStream(file);
			   String tableText = bpmTableModel.getText();
			   ofStream.write(tableText.getBytes());
			   ofStream.close();
		   }
		   catch (Exception exc) {
			   System.out.println("Problem exporting linac avg energy table " + exc.getMessage());
		   }
	    }
    }   
    
    /** reset all bpm pair statistics */
    
    protected void resetStats() {
	    boolean doRestart = false;
	    if(amRunning) {
		    doRestart = true;
		    stopBPMMeter();
	    }
	    Collection <BPMPair> pairs = selectedPairs.values();
	    for (BPMPair bpmPair : pairs) {
		    bpmPair.stats.clear();
		    bpmPair.setEnergy(0.);
	    }
	    if(doRestart) startBPMMeter();
	    updateBPMTable();
    }
    
    /** remove the pairs that are selected in the table display */
    
   
    protected void removeSelectedPairs() {
	    boolean doRestart = false;
	    if(amRunning) {
		    doRestart = true;
		    stopBPMMeter();
	    }
	    
	    int rows[] = bpmResultsTable.getSelectedRows();
	    for (int i = rows.length; i > 0; i--) {
		    String name = selectedPairNames.get(rows[i-1]);
		    if (name != null) {
			    BPMPair pair = selectedPairs.get(name);
			    bpmCorrelator.removePair(pair);
			    selectedPairs.remove(name);
			    selectedPairNames.remove(name);
		    }
	    }
	    updateBPMTable();
    }
}
