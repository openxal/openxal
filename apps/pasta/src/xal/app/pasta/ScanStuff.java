/*
 * PastaDocument.java
 *
 * Created on June 14, 2004
 */

package xal.app.pasta;

import java.awt.*;
import java.util.*;
import java.awt.event.*;
import java.text.*;

import xal.smf.*;
import xal.smf.impl.*;
import xal.smf.impl.qualify.*;
import xal.extension.scan.*;
import xal.tools.apputils.*;
import xal.extension.widgets.plot.*;
import xal.extension.widgets.apputils.SimpleChartPopupMenu;
import xal.tools.xml.*;
import xal.tools.data.*;
import xal.ca.*;
/**
 * This class contains the components internal to the Scanning procedure.
 * @author  jdg
 */
public class ScanStuff implements ConnectionListener{
  
    /** the parametric scan variable (cavity amplitude) */ 
    protected ScanVariable scanVariableParameter = null;
    /** the scan variable (cavity phase) */ 
    protected ScanVariable scanVariable = null;
    /** container for the measured variables (BPM phases + amplitudes) */ 
    protected Vector<MeasuredValue> measuredValuesV, measuredValuesOffV;
    /** the measured quantities for the Scan */
    protected MeasuredValue BPM1PhaseMV, BPM1AmpMV, BPM2PhaseMV, BPM2AmpMV, cavAmpRBMV, BCMMV;
    protected MeasuredValue BPM1PhaseOffMV,  BPM2PhaseOffMV, BCMOffMV;   
     /**scan controller for 2D scans*/
    protected ScanController2D scanController;
     /**scan controller for 1D scan*/
    protected ScanController1D scanController1D;    
    //averaging controller
    protected AvgController avgCntr, avgCntr1D;
    /**graph panel to display scanned data */
    protected FunctionGraphsJPanel graphScan, graphScan1D;
    /** validation controller - used with a BCM */
    protected ValidationController vldCntr, vldCntr1D;
   
   /** names of the PVs used */
   private String cavAmpPVName, cavPhasePVName, BPM1AmpPVName, BPM1PhasePVName, BPM2AmpPVName, BPM2PhasePVName, cavAmpRBPVName, BCMPVName;
   
   /** the document this scanstuff belongs to */
   PastaDocument theDoc;

    /** workaround to avoid jca context initialization exception */
    static{
	ChannelFactory.defaultFactory().init();
    }    
    protected String thePVText;
    
    private Map<String, Boolean> connectionMap;
    
    /** since this PV is not inthe xal xml file make it here */
    private Channel cavAmpRBChan, BCMChan;
 
    /** Create an object */
    public ScanStuff(PastaDocument doc) {
	theDoc = doc;
	scanController = new ScanController2D("SCAN CONTROL PANEL");
	scanController1D = new ScanController1D("Cav Off CONTROL PANEL");	
	avgCntr = new AvgController();
	graphScan = new FunctionGraphsJPanel(); 
	vldCntr = new ValidationController(2., 100.);
	avgCntr1D = new AvgController();
	graphScan1D     = new FunctionGraphsJPanel(); 
	vldCntr1D = new ValidationController(2., 100.);
	
	scanVariable = new ScanVariable("cavPhaseSP", "cavPhaseRB");
	scanVariableParameter = new ScanVariable("cavAmpSP", "cavAmpRB");
	BPM1PhaseMV = new MeasuredValue("BPM1Phase");
	BPM1AmpMV = new MeasuredValue("BPM1Amp");
	BPM2PhaseMV = new MeasuredValue("BPM2Phase");
	BPM2AmpMV = new MeasuredValue("BPM2Amp");
	cavAmpRBMV = new MeasuredValue("CavAmpRB");
	BCMMV = new MeasuredValue("BCMcurrent");
	
	BPM1PhaseOffMV = new MeasuredValue("BPM1PhaseOff");
	BPM2PhaseOffMV = new MeasuredValue("BPM2PhaseOff");
	BCMOffMV = new MeasuredValue("BCMcurrentOff");
	
	measuredValuesV = new Vector<MeasuredValue>();
	measuredValuesV.clear();
	measuredValuesV.add(BPM1PhaseMV);
	measuredValuesV.add(BPM1AmpMV);
	measuredValuesV.add(BPM2PhaseMV);
	measuredValuesV.add(BPM2AmpMV);
	measuredValuesV.add(cavAmpRBMV);
	measuredValuesV.add(BCMMV);
	
	measuredValuesOffV = new Vector<MeasuredValue>();
	measuredValuesOffV.clear();
	measuredValuesOffV.add(BPM1PhaseOffMV);
	measuredValuesOffV.add(BPM2PhaseOffMV);
	measuredValuesOffV.add(BCMOffMV);	
	
	Iterator<MeasuredValue> itr = measuredValuesV.iterator();
	while( itr.hasNext() ) {
		scanController.addMeasuredValue(itr.next());
	}
	
	itr = measuredValuesOffV.iterator();
	while( itr.hasNext() ) {
		scanController1D.addMeasuredValue(itr.next());
	}	
	    
	connectionMap = Collections.synchronizedMap(new HashMap<String, Boolean>());
	
	initScanController2D();
	
	initScanController1D();

	// overwrite the light green - hard to  see
	IncrementalColors.setColor(0, Color.blue);
	IncrementalColors.setColor(1, Color.red);
	IncrementalColors.setColor(2, Color.black);
	
    }
    
    /** initialize the 2-D scan controller used with the cavity on */
    private void initScanController2D() {
	scanController.setValuePhaseScanButtonVisible(true);
	scanController.setValuePhaseScanButtonOn(true);
	scanController.getParamUnitsLabel().setText(" AU ");
        scanController.setParamLowLimit(0.5);
        scanController.setParamUppLimit(0.55);
        scanController.setParamStep(0.1);
	scanController.getUnitsLabel().setText(" deg ");
        scanController.setUppLimit(175.0);
        scanController.setScanVariable(scanVariable);
	scanController.setParamVariable(scanVariableParameter);
        scanController.setAvgController(avgCntr);
	scanController.setValidationController(vldCntr);	
	scanController.addValidationValue(BCMMV);
	// what to do when new data point is posted during a scan
        scanController.addNewPointOfDataListener(new ActionListener(){
		public void actionPerformed(ActionEvent evn){
		    graphScan.refreshGraphJPanel();
		}
	    });
  
	
	// what to do when a new set of data is prescribed:
       scanController.addNewSetOfDataListener(new ActionListener(){
		public void actionPerformed(ActionEvent evn){
		    //new set of data	    
		    newSetOfData();
		}
       });

	scanController.addStartButtonListener(new ActionListener(){
		public void actionPerformed(ActionEvent evn){
		    initScan();
		}
	});
	// 
	scanController.setFontForAll(new Font("Monospaced",Font.PLAIN,12));
	avgCntr.setFontForAll(new Font("Monospaced",Font.PLAIN,12));
	
	
	SimpleChartPopupMenu.addPopupMenuTo(graphScan);
        graphScan.setOffScreenImageDrawing(true);
        graphScan.setName("SCAN : BPM Values vs. Cavity Phase");
        graphScan.setAxisNames("Cavity Phase (deg)","Measured Values");
        graphScan.setGraphBackGroundColor(Color.white);
	graphScan.setLegendButtonVisible(true);
	
    }
    
     /** initialize the 1-D scan controller used with the cavity off */
    private void initScanController1D() {
	scanController1D.setPhaseScanButtonVisible(true); 
	scanController1D.setPhaseScanButtonOn(true);    
	scanController1D.getUnitsLabel().setText(" deg ");
        scanController1D.setUppLimit(175.0);
        scanController1D.setScanVariable(scanVariable);
        scanController1D.setAvgController(avgCntr1D);
	scanController1D.setValidationController(vldCntr1D);	
	scanController1D.addValidationValue(BCMOffMV);
	// what to do when new data point is posted during a scan
        scanController1D.addNewPointOfDataListener(new ActionListener(){
		public void actionPerformed(ActionEvent evn){
		    graphScan1D.refreshGraphJPanel();
		}
	    });
  
	
	// what to do when a new set of data is prescribed:
       scanController1D.addNewSetOfDataListener(new ActionListener(){
		public void actionPerformed(ActionEvent evn){
		    //new set of data	    
		    newSetOfData1D();
		}
       });

	scanController1D.addStartListener(new ActionListener(){
		public void actionPerformed(ActionEvent evn){
		    initScan1D();
		}
	});
	// 
	scanController1D.setFontForAll(new Font("Monospaced",Font.PLAIN,12));
	avgCntr1D.setFontForAll(new Font("Monospaced",Font.PLAIN,12));
	
	
	SimpleChartPopupMenu.addPopupMenuTo(graphScan1D);
        graphScan1D.setOffScreenImageDrawing(true);
        graphScan1D.setName("BPM Values vs. Cavity Phase, Cav Off");
        graphScan1D.setAxisNames("Cavity Phase (deg)","Measured Values");
        graphScan1D.setGraphBackGroundColor(Color.white);
	graphScan1D.setLegendButtonVisible(true);
	
    }   
    /** set the scan  + measured PVs, based on the selected BPMs  = cavity */
    protected void updateScanVariables(BPM bpm1, BPM bpm2, RfCavity cav, CurrentMonitor bcm) {
	graphScan.removeAllGraphData();

	cavAmpPVName = (cav.getChannel(RfCavity.CAV_AMP_SET_HANDLE)).getId();
	cavPhasePVName = cav.getChannel(RfCavity.CAV_PHASE_SET_HANDLE).getId();
	BPM1AmpPVName = bpm1.getChannel(BPM.AMP_AVG_HANDLE).getId();
	BPM1PhasePVName = bpm1.getChannel(BPM.PHASE_AVG_HANDLE).getId();
	BPM2AmpPVName = bpm2.getChannel(BPM.AMP_AVG_HANDLE).getId();
	BPM2PhasePVName = bpm2.getChannel(BPM.PHASE_AVG_HANDLE).getId();
	cavAmpRBPVName = cavAmpPVName.replaceFirst("CtlAmpSet", "cavAmpAvg");
	if(bcm != null) BCMPVName = bcm.getId() + ":currentAvg";

	System.out.println("pv = " + cavAmpRBPVName + "   " + BCMPVName);
	cavAmpRBChan = ChannelFactory.defaultFactory().getChannel(cavAmpRBPVName);
	if(bcm != null) BCMChan = ChannelFactory.defaultFactory().getChannel(BCMPVName);
	
	scanVariable.setChannel(cav.getChannel(RfCavity.CAV_PHASE_SET_HANDLE));
	scanVariableParameter.setChannel(cav.getChannel(RfCavity.CAV_AMP_SET_HANDLE));
	BPM1PhaseMV.setChannel(bpm1.getChannel(BPM.PHASE_AVG_HANDLE));
	BPM1AmpMV.setChannel(bpm1.getChannel(BPM.AMP_AVG_HANDLE));
	BPM2PhaseMV.setChannel(bpm2.getChannel(BPM.PHASE_AVG_HANDLE));
	BPM2AmpMV.setChannel(bpm2.getChannel(BPM.AMP_AVG_HANDLE));
	cavAmpRBMV.setChannel(cavAmpRBChan);
	BCMMV.setChannel(BCMChan);

	BPM1PhaseOffMV.setChannel(bpm1.getChannel(BPM.PHASE_AVG_HANDLE));
	BPM2PhaseOffMV.setChannel(bpm2.getChannel(BPM.PHASE_AVG_HANDLE));
	BCMOffMV.setChannel(BCMChan);
	
	Channel.flushIO(); // flush the channel connection requests in que
	connectChannels(bpm1, bpm2, cav);

        scanController.setScanVariable(scanVariable);
	scanController1D.setScanVariable(scanVariable);
	scanController.setParamVariable(scanVariableParameter);
 	
// do not add again.
//	measuredValuesV.clear();
//	measuredValuesV.add(BPM1PhaseMV);
//	measuredValuesV.add(BPM1AmpMV);
//	measuredValuesV.add(BPM2PhaseMV);
//	measuredValuesV.add(BPM2AmpMV);	
	setPVText();
	
    }
    
    /** try and force a channel connection to the required channels */
    
    private void connectChannels(BPM bpm1, BPM bpm2, RfCavity cav) {
	    
	    connectionMap.clear();
	    connectionMap.put(cavAmpPVName, new Boolean(false));
	    connectionMap.put(cavPhasePVName, new Boolean(false));
	    connectionMap.put(BPM1AmpPVName, new Boolean(false));
	    connectionMap.put(BPM1PhasePVName, new Boolean(false));
	    connectionMap.put(BPM2AmpPVName, new Boolean(false));
	    connectionMap.put(BPM2PhasePVName, new Boolean(false));
	    connectionMap.put(cavAmpRBPVName, new Boolean(false));
	    connectionMap.put(BCMPVName, new Boolean(false));	    
	    
	    // remove any 
	    cav.getChannel(RfCavity.CAV_PHASE_SET_HANDLE).addConnectionListener(this);
	    cav.getChannel(RfCavity.CAV_AMP_SET_HANDLE).addConnectionListener(this);
	    bpm1.getChannel(BPM.PHASE_AVG_HANDLE).addConnectionListener(this);
	    bpm2.getChannel(BPM.PHASE_AVG_HANDLE).addConnectionListener(this);
	    bpm1.getChannel(BPM.AMP_AVG_HANDLE).addConnectionListener(this);
	    bpm2.getChannel(BPM.AMP_AVG_HANDLE).addConnectionListener(this);
	    if(cavAmpRBChan != null) cavAmpRBChan.addConnectionListener(this);
	    if(BCMChan!= null) BCMChan.addConnectionListener(this);
	    
	    cav.getChannel(RfCavity.CAV_PHASE_SET_HANDLE).requestConnection();
	    cav.getChannel(RfCavity.CAV_AMP_SET_HANDLE).requestConnection();
	    bpm1.getChannel(BPM.PHASE_AVG_HANDLE).requestConnection();
	    bpm2.getChannel(BPM.PHASE_AVG_HANDLE).requestConnection();
	    bpm1.getChannel(BPM.AMP_AVG_HANDLE).requestConnection();
	    bpm2.getChannel(BPM.AMP_AVG_HANDLE).requestConnection();
	    if(cavAmpRBChan != null)cavAmpRBChan.requestConnection();
	    if(BCMChan!= null) BCMChan.requestConnection();
	    
	    Channel.flushIO(); // flush the channel connection requests in que

	    // wait a bit to see if they connect:
	    
	    int i=0;
	    int nDisconnects = 6;
	    while (nDisconnects > 0 && i < 5) {
		try {
			Thread.sleep(200);
		} catch (InterruptedException e) {
			System.out.println("Sleep interrupted during connection check");
			System.err.println( e.getMessage() );
			e.printStackTrace();
		}
		
		nDisconnects = 0;
		Set<Map.Entry<String, Boolean>> set = connectionMap.entrySet();
		Iterator<Map.Entry<String, Boolean>> itr = set.iterator();
		while (itr.hasNext()){
			Map.Entry<String, Boolean> me = itr.next();
			Boolean tf = me.getValue();
			if( !(tf.booleanValue()) ) nDisconnects++;
		}
		i++;
	    }
	    if( nDisconnects > 0) {
		Toolkit.getDefaultToolkit().beep();
		theDoc.myWindow().errorText.setText((new Integer(nDisconnects)).toString() + " PVs were not able to connect");		    
		System.out.println(nDisconnects + " PVs were not able to connect");
	    }
	    
    }
    
    /** sets the text describing which PVs will be used */
    private void setPVText() {
	    String scanpv1 = "RF phase scan PV: " + scanVariable.getChannelName() + "  " + connectionMap.get(cavAmpPVName) + "\n";
	    String scanpv2 = "RF amplitude scan PV: " + scanVariableParameter.getChannelName() + "  " + connectionMap.get(cavPhasePVName) + "\n";
	    Iterator<MeasuredValue> itr = measuredValuesV.iterator();
	    int i = 1;
	    String mvpvs = "\n";
	    while (itr.hasNext()){
		String name = (itr.next()).getChannelName();
		mvpvs += "BPM monitor PV " + (new Integer(i)).toString() + " : " + name +  "  " + connectionMap.get(name) + "\n";
		     i++;
	    }
	    
	    thePVText = scanpv1 + scanpv2 + mvpvs;
	    
    }
    
    /** method to update the graphPanel data (for raw scanned data  only)*/
    
     protected void updateGraphPanel(){

	graphScan.removeAllGraphData();
	
	// do not plot the cavity amplitude readback value (should be
	// last one - but this was added later. Can not assume it is always there, 
	// if early file is opened
	
	int nCurves = 4;
	if(measuredValuesV.size() < 4) nCurves=measuredValuesV.size();
	for(int i = 0, n=nCurves; i < n; i++){
		MeasuredValue mv_tmp = measuredValuesV.get(i);
		graphScan.addGraphData(mv_tmp.getDataContainers());
	}
	graphScan.refreshGraphJPanel();
	//graphScan.repaint();
     }
 
    /** method to update the graphPanel data (for raw scanned data  only)*/
    
     protected void updateGraph1DPanel(){

	graphScan1D.removeAllGraphData();
	
	// do not plot the cavity amplitude readback value (should be
	// last one - but this was added later. Can not assume it is always there, 
	// if early file is opened
	
	int nCurves = 2;
	if(measuredValuesOffV.size() < 2) nCurves=measuredValuesOffV.size();
	for(int i = 0, n=nCurves; i < n; i++){
		MeasuredValue mv_tmp = measuredValuesOffV.get(i);
		graphScan1D.addGraphData(mv_tmp.getDataContainers());
	}
	graphScan1D.refreshGraphJPanel();
	//graphScan.repaint();
     }
     
     /** Set colors of raw data scans */
   
    protected void setColors(int deleteIndex){
	for(int i = 0, n = measuredValuesV.size(); i < n; i++){
		MeasuredValue mv_tmp = measuredValuesV.get(i);		
		mv_tmp.setColor(IncrementalColor.getColor(i));
	}
	graphScan.refreshGraphJPanel();
    }
 
     protected void setColors1D(int deleteIndex){
	for(int i = 0, n = measuredValuesOffV.size(); i < n; i++){
		MeasuredValue mv_tmp = measuredValuesOffV.get(i);		
		mv_tmp.setColor(IncrementalColor.getColor(i));
	}
	graphScan1D.refreshGraphJPanel();
    }
    /** clear all data and start over */

    private void initScan() {
	graphScan.removeAllGraphData();
	for(int i = 0, n = measuredValuesV.size(); i < n; i++){
		MeasuredValue mv_tmp = measuredValuesV.get(i);
		mv_tmp.removeAllDataContainers();
	}
	setColors(1);
    }
    
    /** clear all data and start over */

    private void initScan1D() {
	graphScan1D.removeAllGraphData();
	for(int i = 0, n = measuredValuesOffV.size(); i < n; i++){
		MeasuredValue mv_tmp = measuredValuesOffV.get(i);		
		mv_tmp.removeAllDataContainers();
	}
	setColors1D(1);
    }   
    
    /** Initialize the scan, when the start button is pressed */
    private void newSetOfData() {
	    DecimalFormat valueFormat     = new DecimalFormat("###.###");
	    String paramPV_string   = "";
	    String scanPV_string    = "";
	    String measurePV_string = "";
	    String legend_string    = "";
	    Double paramValue   = new Double(scanController.getParamValue());
	    Double paramValueRB = new Double(scanController.getParamValueRB());
	    
	    if(scanVariableParameter.getChannel() != null){
		String paramValString  = valueFormat.format(scanVariableParameter.getValue());
		
		paramPV_string = paramPV_string + " par.PV : " 
		    + scanVariableParameter.getChannel().getId()+"="
		    + paramValString;
		paramValue   = new Double(scanVariableParameter.getValue());
		
	    }
	    else{
		paramPV_string = paramPV_string + " param.= " + paramValue;
	    }
	    
	    if(scanVariable.getChannel() != null){
		scanPV_string = "xPV="+scanVariable.getChannel().getId();
	    }

	    for(int i = 0, n = measuredValuesV.size(); i < n; i++){
		    MeasuredValue mv_tmp = measuredValuesV.get(i);
		    BasicGraphData gd = mv_tmp.getDataContainer();
		    if(mv_tmp.getChannel() != null){
			measurePV_string = mv_tmp.getChannel().getId();
		    }
		    legend_string = measurePV_string+paramPV_string+" ";
		    if(gd != null ){
			gd.removeAllPoints();	
			gd.setGraphProperty(graphScan.getLegendKeyString(),legend_string);
			if(paramValue   != null) gd.setGraphProperty("PARAMETER_VALUE",paramValue);
			if(paramValueRB != null) gd.setGraphProperty("PARAMETER_VALUE_RB",paramValueRB);
		    }

		    if(scanVariable.getChannelRB() != null){
		       gd = mv_tmp.getDataContainerRB();
		       if(gd != null){
			   if(paramValue   != null) gd.setGraphProperty("PARAMETER_VALUE",paramValue);
			   if(paramValueRB != null) gd.setGraphProperty("PARAMETER_VALUE_RB",paramValueRB);
		       }
		    }
	    }
	    updateGraphPanel();
	    theDoc.setHasChanges(true);
	}
	
   /** Initialize the scan, when the start button is pressed */
    private void newSetOfData1D() {
	    DecimalFormat valueFormat     = new DecimalFormat("###.###");
	    String scanPV_string    = "";
	    String measurePV_string = "";
	    String legend_string    = "";
	    
	    if(scanVariable.getChannel() != null){
		scanPV_string = "xPV="+scanVariable.getChannel().getId();
	    }

	    for(int i = 0, n = measuredValuesOffV.size(); i < n; i++){
		    MeasuredValue mv_tmp = measuredValuesOffV.get(i);
		    BasicGraphData gd = mv_tmp.getDataContainer();
		    if(mv_tmp.getChannel() != null){
			measurePV_string = mv_tmp.getChannel().getId();
		    }
		    legend_string = measurePV_string+" ";
		    if(gd != null ){
			gd.removeAllPoints();		gd.setGraphProperty(graphScan.getLegendKeyString(),legend_string);
		    }

		    if(scanVariable.getChannelRB() != null){
		       gd = mv_tmp.getDataContainerRB();
		    }
	    }
	    updateGraph1DPanel();
	    theDoc.setHasChanges(true);
	}
	
	/** returns whether data is taken yet for the cavity off case */
	protected boolean haveCavOffData() {
	     if(BPM1PhaseOffMV == null ||  BPM1PhaseOffMV == null)
		     return false;
	     if(BPM1PhaseOffMV.getDataContainer(0) == null ||  BPM1PhaseOffMV.getDataContainer(0) == null)
		     return false;
	     
	     if(BPM1PhaseOffMV == null || BPM1PhaseOffMV.getDataContainer(0).getNumbOfPoints() == 0 || BPM1PhaseOffMV == null || BPM1PhaseOffMV.getDataContainer(0).getNumbOfPoints() == 0)
		     return false;
	     else
		     return true;
	}
	
    /** get BPM1 phase measured with the cavity off 
    * @param cavPhase = the cavity phase (deg)
    */
    protected double BPM1CavOffPhase(double cavPhase) {
	if (BPM1PhaseOffMV == null || BPM1PhaseOffMV.getDataContainer(0) == null) {
	    Toolkit.getDefaultToolkit().beep();
	    String errText = "Requested bpm1 phase with cav off data does not exist, ";
	    theDoc.myWindow().errorText.setText(errText);
	    System.err.println(errText);
	    return 0.;		
	}

	BasicGraphData BPM1CavOffBGD = BPM1PhaseOffMV.getDataContainer(0);
	
	if (BPM1CavOffBGD.getNumbOfPoints() < 1) {
	    Toolkit.getDefaultToolkit().beep();
	    String errText = "Requested bpm1 phase with cav off data has too few points, ";
	    theDoc.myWindow().errorText.setText(errText);
	    System.err.println(errText);
	    return 0.;		
	}
	return BPM1CavOffBGD.getValueY(cavPhase);
}	
     /** get BPM1 phase measured with the cavity off 
    * @param cavPhase = the cavity phase (deg)
    */
    protected double BPM2CavOffPhase(double cavPhase) {
	if(BPM2PhaseOffMV == null || BPM2PhaseOffMV.getDataContainer(0) == null) {
	    Toolkit.getDefaultToolkit().beep();
	    String errText = "Requested bpm2 phase with cav off data does not exist, ";
	    theDoc.myWindow().errorText.setText(errText);
	    System.err.println(errText);
	    return 0.;		
	}

	BasicGraphData BPM2CavOffBGD = BPM2PhaseOffMV.getDataContainer(0);
	
	if( BPM2CavOffBGD.getNumbOfPoints() < 1) {
	    Toolkit.getDefaultToolkit().beep();
	    String errText = "Requested bpm2 phase with cav off data has too few points, ";
	    theDoc.myWindow().errorText.setText(errText);
	    System.err.println(errText);
	    return 0.;		
	}
	
	return BPM2CavOffBGD.getValueY(cavPhase);
    }

        /** ConnectionListener interface methods:*/
	  
	  public void connectionMade(Channel chan) {
		  String name = chan.getId();
		  connectionMap.put(name, new Boolean(true));
	  
	  }
	  
	  public void connectionDropped(Channel chan) { }
    }
    
