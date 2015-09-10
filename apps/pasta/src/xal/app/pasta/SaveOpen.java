package xal.app.pasta;

import xal.ca.Channel;
import xal.ca.ChannelFactory;
import xal.tools.StringJoiner;
import xal.tools.data.DataAdaptor;
import xal.extension.widgets.plot.BasicGraphData;
import xal.extension.widgets.plot.FunctionGraphsJPanel;
import xal.extension.scan.AvgController;
import xal.extension.scan.MeasuredValue;
import xal.extension.scan.ScanController1D;
import xal.extension.scan.ScanController2D;
import xal.extension.scan.ScanVariable;
import xal.tools.xml.XmlDataAdaptor;
import xal.smf.AcceleratorNode;
import xal.smf.AcceleratorSeqCombo;
import xal.smf.AcceleratorSeq;

import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;
/**
 * A class to save and restore pasta documents to an xml file
 * @author  J. Galambos
 */

public class SaveOpen {
    
    /** The pasta documentto deal with */
    private final PastaDocument theDoc;
    
    private XmlDataAdaptor xdaRead, xdaWrite;
    //child nodes names
    private final String paramsName_SR      = "app_params";
    private final String paramPV_SR         = "param_PV";
    private final String scanPV_SR          = "scan_PV";
    private final String measurePVs_SR      = "measure_PVs";
    private final String measureOffPVs_SR   = "measureOff_PVs";
    private final String validationPVs_SR   = "validation_PVs";
    
    private final String BPM1AmpName  = "BPM1_Amp";
    private final String BPM1PhaseName = "BPM1_Phase";
    private final String BPM2AmpName  = "BPM2_Amp";
    private final String BPM2PhaseName = "BPM2_Phase";
    private final String cavAmpRBName  = "cavAmpRB";
    private final String BCMName  = "BCMMV";
    
    private final String stringValue = "";
    private final StringJoiner joiner = new StringJoiner(",");
    /** constructor:
     * @param doc the XyPlot object
     */
    public SaveOpen(PastaDocument doc) {
        theDoc = doc;
    }
    
    /** save the object to a file
     * @param url the file to save it to
     */
    public void saveTo(URL url) {
        
        xdaWrite = XmlDataAdaptor.newEmptyDocumentAdaptor();
        DataAdaptor pastada = xdaWrite.createChild("PastaSetup");
        DataAdaptor daAccel = pastada.createChild("accelerator");
        daAccel.setValue("xmlFile", theDoc.getAcceleratorFilePath());
        
        // save the selected combo seq components:
        ArrayList<String> seqs;
        
        if(theDoc.getSelectedSequence() != null) {
            DataAdaptor daSeq = daAccel.createChild("sequence");
            daSeq.setValue("name", theDoc.getSelectedSequence().getId() );
            
            if(theDoc.getSelectedSequence().getClass() == AcceleratorSeqCombo.class) {
                AcceleratorSeqCombo asc = (AcceleratorSeqCombo)theDoc.getSelectedSequence();
                seqs = (ArrayList<String>)asc.getConstituentNames();
            }
            else {
                seqs = new ArrayList<String>();
                seqs.add(theDoc.getSelectedSequence().getId());
            }
            
            
            Iterator<String> itr = seqs.iterator();
            
            while (itr.hasNext()) {
                DataAdaptor daSeqComponents = daSeq.createChild("seq");
                daSeqComponents.setValue("name", itr.next());
            }
            
            // save the selected nodes:
            DataAdaptor daNodes = daSeq.createChild("Nodes");
            if (theDoc.BPM1 != null) daNodes.setValue("BPM1", theDoc.BPM1);
            if (theDoc.BPM2 != null) daNodes.setValue("BPM2", theDoc.BPM2);
            if (theDoc.theCavity != null) daNodes.setValue("cav", theDoc.theCavity);
            if (theDoc.theBCM != null) daNodes.setValue("BCM", theDoc.theBCM);
        }
        
        // save the scan data
        DataAdaptor daScan1d = pastada.createChild("scan1d");
        saveScan1D(daScan1d);
        DataAdaptor daScan2d = pastada.createChild("scan");
        saveScan2D(daScan2d);
        
        // model + solver stuff:
        
        DataAdaptor daAnalysis = pastada.createChild("analysis");
        
        // import from scan to analysis stuff
        DataAdaptor daImport = daAnalysis.createChild("import");
        daImport.setValue("DTLPhaseOffset", theDoc.DTLPhaseOffset);
        daImport.setValue("BPMPhaseOffset", theDoc.BPMPhaseDiffOffset);
        
        DataAdaptor daModel = daAnalysis.createChild("model");
        if(theDoc.analysisStuff.probeFile != null)
            daModel.setValue("probeFile", theDoc.analysisStuff.probeFile.getPath() );
        daModel.setValue("BPMAmpMin", theDoc.analysisStuff.minBPMAmp);
        daModel.setValue("nCalcPoints", theDoc.analysisStuff.nCalcPoints);
        daModel.setValue("timeout", theDoc.analysisStuff.timeoutPeriod);
        daModel.setValue("phaseModelMin", theDoc.analysisStuff.phaseModelMin);
        daModel.setValue("phaseModelMax", theDoc.analysisStuff.phaseModelMax);
        daModel.setValue("WIn", theDoc.analysisStuff.WIn);
        daModel.setValue("cavPhaseOffset", theDoc.analysisStuff.cavPhaseOffset);
        daModel.setValue("ampValueIndex", theDoc.analysisStuff.amplitudeVariableIndex);
        daModel.setValue("ampValue",theDoc.analysisStuff.cavityVoltage);
        daModel.setValue("fudgeValue",theDoc.fudgePhaseOffset);
        xdaWrite.writeToUrl(url);
        
    }
    
    /** restore xyplot object settings from a file
     * @param url the file to read from
     */
    public void readSetupFrom(URL url) {
        XmlDataAdaptor xdaWrite =  XmlDataAdaptor.adaptorForUrl(url, false);
        DataAdaptor pastada = xdaWrite.childAdaptor("PastaSetup");
        Double bigD;
        
        // get the accelerator file
        DataAdaptor daAccel = pastada.childAdaptor("accelerator");
        String acceleratorPath = daAccel.stringValue("xmlFile");
        
        if ( acceleratorPath.length() > 0 ) {
            theDoc.applySelectedAcceleratorWithDefaultPath(acceleratorPath);
            /** This part was replaced by only one method applySelectedAcceleratorWithDefaultPath
             It was done by Andrei Shishlo, March 09,2009
             theDoc.setAcceleratorFilePath(acceleratorPath);
             System.out.println("accelFile = " + theDoc.getAcceleratorFilePath());
             String accelUrl = "file://"+ theDoc.getAcceleratorFilePath();
             try {
             XMLDataManager  dMgr = new XMLDataManager(accelUrl);
             theDoc.setAccelerator(dMgr.getAccelerator(), theDoc.getAcceleratorFilePath());
             }
             catch(Exception exception) {
             JOptionPane.showMessageDialog(null, "Hey - I had trouble parsing the accelerator input xml file you fed me", "Xyz setup error",  JOptionPane.ERROR_MESSAGE);
             }
             */
            //theDoc.acceleratorChanged();
        }
        // set up the right sequence combo from selected primaries:
        List<DataAdaptor> temp = daAccel.childAdaptors("sequence");
        if(temp.isEmpty() ) return;
        
        ArrayList<AcceleratorSeq> seqs = new ArrayList<AcceleratorSeq>();
        DataAdaptor daSeq = daAccel.childAdaptor("sequence");
        String seqName = daSeq.stringValue("name");
        //System.out.println("seq name = "+ seqName);
        
        temp = daSeq.childAdaptors("seq");
        Iterator<DataAdaptor> itr = temp.iterator();
        while (itr.hasNext()) {
            DataAdaptor da = itr.next();
            seqs.add(theDoc.getAccelerator().getSequence(da.stringValue("name")));
            //System.out.println("component = " + da.stringValue("name"));
        }
        theDoc.setSelectedSequence(new AcceleratorSeqCombo(seqName, seqs));
        //theDoc.selectedSequenceChanged();
        AcceleratorNode bpm1, bpm2, cav, bcm;
        DataAdaptor daNodes = daSeq.childAdaptor("Nodes");
        if(daNodes.hasAttribute("BPM1")) {
            bpm1 = theDoc.getAccelerator().getNode(daNodes.stringValue("BPM1"));
            theDoc.myWindow().BPM1List.setSelectedValue(bpm1, true);
        }
        if(daNodes.hasAttribute("BPM2")) {
            bpm2 = theDoc.getAccelerator().getNode(daNodes.stringValue("BPM2"));
            theDoc.myWindow().BPM2List.setSelectedValue(bpm2, true);
        }
        if(daNodes.hasAttribute("cav")){
            cav = theDoc.getAccelerator().getNode(daNodes.stringValue("cav"));
            theDoc.myWindow().cavityList.setSelectedValue(cav, true);
        }
        if(daNodes.hasAttribute("BCM")) {
            bcm= theDoc.getAccelerator().getNode(daNodes.stringValue("BCM"));
            theDoc.myWindow().BCMList.setSelectedValue(bcm, true);
        }
        // add checks for nulls here!
        
        theDoc.myWindow().setAccelComponents();
        // reflush:
        Channel.flushIO();
        
        DataAdaptor daScan1d = pastada.childAdaptor("scan1d");
        if (daScan1d != null) readScan1D(daScan1d);
        DataAdaptor daScan2d = pastada.childAdaptor("scan");
        if (daScan2d != null)readScan(daScan2d);
        
        theDoc.scanStuff.setColors(-1);
        theDoc.scanStuff.setColors1D(-1);
        theDoc.scanStuff.updateGraphPanel();
        theDoc.scanStuff.updateGraph1DPanel();
        
        DataAdaptor daAnalysis = pastada.childAdaptor("analysis");
        DataAdaptor daModel = daAnalysis.childAdaptor("model");
        DataAdaptor daImport = daAnalysis.childAdaptor("import");
        if(daImport != null) {
            
            if(daImport.hasAttribute("DTLPhaseOffset")) {
                theDoc.DTLPhaseOffset = daImport.doubleValue("DTLPhaseOffset");
            }
            if(daImport.hasAttribute("BPMPhaseOffset")) {
                theDoc.BPMPhaseDiffOffset = daImport.doubleValue("BPMPhaseOffset");
            }
        }
        
        if(daModel != null) {
            String fname = daModel.stringValue("probeFile");
            if(fname != null) {
                theDoc.analysisStuff.probeFileName = fname;
            }
            
            if(daModel.hasAttribute("BPMAmpMin")) {
                theDoc.analysisStuff.minBPMAmp =  daModel.doubleValue("BPMAmpMin");
            }
            if(daModel.hasAttribute("nCalcPoints"))
                theDoc.analysisStuff.nCalcPoints = daModel.intValue("nCalcPoints");
            if(daModel.hasAttribute("timeout"))
                theDoc.analysisStuff.timeoutPeriod = daModel.doubleValue("timeout");
            if(daModel.hasAttribute("phaseModelMin"))
                theDoc.analysisStuff.phaseModelMin = daModel.doubleValue("phaseModelMin");
            if(daModel.hasAttribute("phaseModelMax"))
                theDoc.analysisStuff.phaseModelMax = daModel.doubleValue("phaseModelMax");
            if(daModel.hasAttribute("WIn"))
                theDoc.analysisStuff.WIn = daModel.doubleValue("WIn");
            if(daModel.hasAttribute("cavPhaseOffset"))
                theDoc.analysisStuff.cavPhaseOffset = daModel.doubleValue("cavPhaseOffset");
            if( daModel.hasAttribute("ampValueIndex") ) theDoc.analysisStuff.amplitudeVariableIndex = daModel.intValue("ampValueIndex");
            if( daModel.hasAttribute("ampValue") ) theDoc.analysisStuff.cavityVoltage = daModel.doubleValue("ampValue");
            if( daModel.hasAttribute("fudgeValue") ) theDoc.fudgePhaseOffset = daModel.doubleValue("fudgeValue");
        }
        
        // update the window:
        theDoc.myWindow().updateInputFields();
        
        //theDoc.analysisStuff.init();
		
        theDoc.setHasChanges(false);
    }
    
    /** method to save scan stuff to the xml file */
    
    private void saveScan2D(DataAdaptor scan2D_Adaptor) {
        
        // local copies of scanStuff members - since I copied the code from a place
        // with everything local.
        
        XmlDataAdaptor params_scan2D = (XmlDataAdaptor) scan2D_Adaptor.createChild(paramsName_SR);
        XmlDataAdaptor paramPV_scan2D = (XmlDataAdaptor) scan2D_Adaptor.createChild(paramPV_SR);
        XmlDataAdaptor scanPV_scan2D = (XmlDataAdaptor) scan2D_Adaptor.createChild(scanPV_SR);
        XmlDataAdaptor measurePVs_scan2D = (XmlDataAdaptor) scan2D_Adaptor.createChild(measurePVs_SR);
        
        ScanVariable scanVariable = theDoc.scanStuff.scanVariable;
        ScanVariable scanVariableParameter = theDoc.scanStuff.scanVariableParameter;
        ScanController2D scanController = theDoc.scanStuff.scanController;
        AvgController avgCntr = theDoc.scanStuff.avgCntr;
        Vector<MeasuredValue> measuredValuesV = theDoc.scanStuff.measuredValuesV ;
        FunctionGraphsJPanel graphScan = theDoc.scanStuff.graphScan;
        
        //      XmlDataAdaptor params_UseTimeStamp = (XmlDataAdaptor) params_scan2D.createChild("UseTimeStamp");
        //     params_UseTimeStamp.setValue("yes",useTimeStampButton.isSelected());
        
        //dump lowLimits uppLimits Step time_delay
        
        XmlDataAdaptor params_limits = (XmlDataAdaptor) params_scan2D.createChild("limits_step_delay");
        params_limits.setValue("paramLow",scanController.getParamLowLimit());
        params_limits.setValue("paramUpp",scanController.getParamUppLimit());
        params_limits.setValue("paramStep",scanController.getParamStep());
        params_limits.setValue("low",scanController.getLowLimit());
        params_limits.setValue("upp",scanController.getUppLimit());
        params_limits.setValue("step",scanController.getStep());
        params_limits.setValue("delay",scanController.getSleepTime());
        
        //dump beam trigger state and time delay
        XmlDataAdaptor params_trigger = (XmlDataAdaptor) params_scan2D.createChild("beam_trigger");
        params_trigger.setValue("on",scanController.getBeamTriggerState());
        params_trigger.setValue("delay",scanController.getBeamTriggerDelay());
        //params_trigger.setValue("PV",scanController.getBeamTriggerChannelName());
        
        //average controller
        XmlDataAdaptor params_averg = (XmlDataAdaptor) params_scan2D.createChild("averaging");
        params_averg.setValue("on",avgCntr.isOn());
        params_averg.setValue("N",avgCntr.getAvgNumber());
        params_averg.setValue("delay",avgCntr.getTimeDelay());
        
        //dump scan PVs
        if(scanVariable.getChannel() != null){
            XmlDataAdaptor scan_PV_name  = (XmlDataAdaptor) scanPV_scan2D.createChild("PV");
            scan_PV_name.setValue("name",scanVariable.getChannelName());
        }
        if(scanVariable.getChannelRB() != null){
            XmlDataAdaptor scan_PV_RB_name   = (XmlDataAdaptor) scanPV_scan2D.createChild("PV_RB");
            scan_PV_RB_name.setValue("name",scanVariable.getChannelNameRB());
        }
        writeMeasuredValue(theDoc.scanStuff.BPM1PhaseMV, measurePVs_scan2D, BPM1PhaseName);
        writeMeasuredValue(theDoc.scanStuff.BPM1AmpMV, measurePVs_scan2D, BPM1AmpName);
        writeMeasuredValue(theDoc.scanStuff.BPM2PhaseMV, measurePVs_scan2D, BPM2PhaseName);
        writeMeasuredValue(theDoc.scanStuff.BPM2AmpMV, measurePVs_scan2D, BPM2AmpName);
        writeMeasuredValue(theDoc.scanStuff.cavAmpRBMV, measurePVs_scan2D, cavAmpRBName);
        writeMeasuredValue(theDoc.scanStuff.BCMMV, measurePVs_scan2D, BCMName);
        
    }
    
    private void saveScan1D(DataAdaptor scan1D_Adaptor) {
        
        // local copies of scanStuff members - since I copied the code from a place
        // with everything local.
        
        XmlDataAdaptor scanPV_scan1D = (XmlDataAdaptor) scan1D_Adaptor.createChild(scanPV_SR);
        XmlDataAdaptor measurePVs_scan1D = (XmlDataAdaptor) scan1D_Adaptor.createChild(measureOffPVs_SR);
        
        ScanVariable scanVariable = theDoc.scanStuff.scanVariable;
        ScanVariable scanVariableParameter = theDoc.scanStuff.scanVariableParameter;
        ScanController1D scanController = theDoc.scanStuff.scanController1D;
        AvgController avgCntr = theDoc.scanStuff.avgCntr1D;
        Vector<MeasuredValue> measuredValuesV = theDoc.scanStuff.measuredValuesOffV ;
        FunctionGraphsJPanel graphScan = theDoc.scanStuff.graphScan1D;
        
        //      XmlDataAdaptor params_UseTimeStamp = (XmlDataAdaptor) params_scan2D.createChild("UseTimeStamp");
        //     params_UseTimeStamp.setValue("yes",useTimeStampButton.isSelected());
        
        
        XmlDataAdaptor scan_params_DA = (XmlDataAdaptor) scan1D_Adaptor.createChild("scan_params");
        
        XmlDataAdaptor scan_limits_DA = (XmlDataAdaptor) scan_params_DA.createChild("limits_step_delay");
        scan_limits_DA.setValue("low",scanController.getLowLimit());
        scan_limits_DA.setValue("upp",scanController.getUppLimit());
        scan_limits_DA.setValue("step",scanController.getStep());
        scan_limits_DA.setValue("delay",scanController.getSleepTime());
        
        //dump beam trigger state and time delay
        
        XmlDataAdaptor params_trigger = (XmlDataAdaptor) scan_params_DA.createChild("beam_trigger");
        params_trigger.setValue("on",scanController.getBeamTriggerState());
        params_trigger.setValue("delay",scanController.getBeamTriggerDelay());
        
        //average controller
        XmlDataAdaptor params_averg = (XmlDataAdaptor) scan_params_DA.createChild("averaging");
        params_averg.setValue("on",avgCntr.isOn());
        params_averg.setValue("N",avgCntr.getAvgNumber());
        params_averg.setValue("delay",avgCntr.getTimeDelay());
        
        //dump scan PVs
        if(scanVariable.getChannel() != null){
            XmlDataAdaptor scan_PV_name  = (XmlDataAdaptor) scanPV_scan1D.createChild("PV");
            scan_PV_name.setValue("name",scanVariable.getChannelName());
        }
        if(scanVariable.getChannelRB() != null){
            XmlDataAdaptor scan_PV_RB_name   = (XmlDataAdaptor) scanPV_scan1D.createChild("PV_RB");
            scan_PV_RB_name.setValue("name",scanVariable.getChannelNameRB());
        }
        writeMeasuredValue(theDoc.scanStuff.BPM1PhaseOffMV, measurePVs_scan1D, BPM1PhaseName);
        writeMeasuredValue(theDoc.scanStuff.BPM2PhaseOffMV, measurePVs_scan1D, BPM2PhaseName);
        writeMeasuredValue(theDoc.scanStuff.BCMOffMV, measurePVs_scan1D, BCMName);
    }
    
    /** write a specific measuredValue to a data adaptor */
    private void writeMeasuredValue(MeasuredValue mv_tmp, XmlDataAdaptor measurePVs_scan2D, String name) {
        FunctionGraphsJPanel graphScan = theDoc.scanStuff.graphScan;
        XmlDataAdaptor measuredPV_DA = (XmlDataAdaptor) measurePVs_scan2D.createChild("MeasuredPV");
        measuredPV_DA.setValue("name",name);
        measuredPV_DA.setValue("unWrapped",new Boolean(mv_tmp.generateUnwrappedDataOn()));
        Vector<BasicGraphData> dataV = mv_tmp.getDataContainers();
        for(int j = 0, nd = dataV.size(); j < nd; j++){
            BasicGraphData gd = dataV.get(j);
            if(gd.getNumbOfPoints() > 0){
                XmlDataAdaptor graph_DA = (XmlDataAdaptor) measuredPV_DA.createChild("Graph_For_scanPV");
                graph_DA.setValue("legend",(String) gd.getGraphProperty(graphScan.getLegendKeyString()));
                
                Double paramValue = (Double) gd.getGraphProperty("PARAMETER_VALUE");
                if(paramValue != null){
                    XmlDataAdaptor paramDataValue = (XmlDataAdaptor) graph_DA.createChild("parameter_value");
                    paramDataValue.setValue("value",paramValue.doubleValue());
                }
                
                Double paramValueRB = (Double) gd.getGraphProperty("PARAMETER_VALUE_RB");
                if(paramValueRB != null){
                    XmlDataAdaptor paramDataValueRB = (XmlDataAdaptor) graph_DA.createChild("parameter_value_RB");
                    paramDataValueRB.setValue("value",paramValueRB.doubleValue());
                }
                
                for(int k = 0, np = gd.getNumbOfPoints(); k < np; k++){
                    XmlDataAdaptor point_DA = (XmlDataAdaptor) graph_DA.createChild("XYErr");
                    point_DA.setValue("x",gd.getX(k));
                    point_DA.setValue("y",gd.getY(k));
                    point_DA.setValue("err",gd.getErr(k));
                }
            }
        }
        
        dataV = mv_tmp.getDataContainersRB();
        for(int j = 0, nd = dataV.size(); j < nd; j++){
            BasicGraphData gd = dataV.get(j);
            if(gd.getNumbOfPoints() > 0){
                XmlDataAdaptor graph_DA = (XmlDataAdaptor) measuredPV_DA.createChild("Graph_For_scanPV_RB");
                graph_DA.setValue("legend",(String) gd.getGraphProperty(graphScan.getLegendKeyString()));
                for(int k = 0, np = gd.getNumbOfPoints(); k < np; k++){
                    XmlDataAdaptor point_DA = (XmlDataAdaptor) graph_DA.createChild("XYErr");
                    point_DA.setValue("x",gd.getX(k));
                    point_DA.setValue("y",gd.getY(k));
                    point_DA.setValue("err",gd.getErr(k));
                }
            }
        }
    }
    
    
    /** A method to parse the scan parameters and set up the scanStuff */
    
    private void readScan(DataAdaptor scan2D_Adaptor) {
        // local copies of scanStuff members - since I copied the code from a place
        // with everything local.
  	    ScanVariable scanVariable = theDoc.scanStuff.scanVariable;
  	    ScanVariable scanVariableParameter = theDoc.scanStuff.scanVariableParameter;
	    ScanController2D scanController = theDoc.scanStuff.scanController;
	    AvgController avgCntr = theDoc.scanStuff.avgCntr;
	    Vector<MeasuredValue> measuredValuesV = theDoc.scanStuff.measuredValuesV ;
	    FunctionGraphsJPanel graphScan = theDoc.scanStuff.graphScan;
	    
        XmlDataAdaptor params_scan2D = (XmlDataAdaptor)   scan2D_Adaptor.childAdaptor(paramsName_SR);
	    XmlDataAdaptor paramPV_scan2D = (XmlDataAdaptor) scan2D_Adaptor.childAdaptor(paramPV_SR);
	    XmlDataAdaptor scanPV_scan2D = (XmlDataAdaptor) scan2D_Adaptor.childAdaptor(scanPV_SR);
	    XmlDataAdaptor measurePVs_scan2D = (XmlDataAdaptor) scan2D_Adaptor.childAdaptor(measurePVs_SR);
	    
	    
	    //set UseTimeStamp parameter
        //XmlDataAdaptor params_UseTimeStamp = (XmlDataAdaptor) //params_scan2D.childAdaptor("UseTimeStamp");
	    //if( params_UseTimeStamp != null && params_UseTimeStamp.hasAttribute("yes")){
		//useTimeStampButton.setSelected(params_UseTimeStamp.booleanValue("yes"));
	    //}
	    
        
	    //set lowLimits uppLimits Step time_delay
        XmlDataAdaptor params_limits = (XmlDataAdaptor) params_scan2D.childAdaptor("limits_step_delay");
        scanController.setLowLimit(params_limits.doubleValue("low"));
        scanController.setUppLimit(params_limits.doubleValue("upp"));
        scanController.setStep(params_limits.doubleValue("step"));
        scanController.setParamLowLimit(params_limits.doubleValue("paramLow"));
        scanController.setParamUppLimit(params_limits.doubleValue("paramUpp"));
        scanController.setParamStep(params_limits.doubleValue("paramStep"));
        scanController.setSleepTime(params_limits.doubleValue("delay"));
        
	    //set beam trigger state and time delay
        XmlDataAdaptor params_trigger = (XmlDataAdaptor) params_scan2D.childAdaptor("beam_trigger");
	    if(params_trigger != null){
            //scanController.setBeamTriggerChannelName(params_trigger.stringValue("PV"));
            scanController.setBeamTriggerDelay(params_trigger.doubleValue("delay"));
            scanController.setBeamTriggerState(params_trigger.booleanValue("on"));
	    }
        
	    //set averaging parameter
        XmlDataAdaptor params_averg =(XmlDataAdaptor) params_scan2D.childAdaptor("averaging");
        avgCntr.setOnOff(params_averg.booleanValue("on"));
        avgCntr.setAvgNumber(params_averg.intValue("N"));
        avgCntr.setTimeDelay(params_averg.doubleValue("delay"));
        
	    //set scan PVs
	    XmlDataAdaptor scan_PV_name_DA = (XmlDataAdaptor) scanPV_scan2D.childAdaptor("PV");
	    
	    if(scan_PV_name_DA != null){
            String scan_PV_name = scan_PV_name_DA.stringValue("name");
            Channel channel = ChannelFactory.defaultFactory().getChannel(scan_PV_name);
            scanVariable.setChannel(channel);
	    }
        
	    XmlDataAdaptor scan_PV_RB_name_DA = (XmlDataAdaptor) scanPV_scan2D.childAdaptor("PV_RB");
	    if(scan_PV_RB_name_DA != null){
            String scan_PV_RB_name = scan_PV_RB_name_DA.stringValue("name");
            Channel channel = ChannelFactory.defaultFactory().getChannel(scan_PV_RB_name);
            scanVariable.setChannelRB(channel);
	    }
        
	    //set measured PVs and graph's data
        
        
	    MeasuredValue mv_tmp;
        for(DataAdaptor measuredPV_DA : measurePVs_scan2D.childAdaptors()) {
            String name = measuredPV_DA.stringValue("name");
            boolean onOff = measuredPV_DA.booleanValue("on");
            boolean unWrappedData = false;
            if(measuredPV_DA.stringValue("unWrapped") != null){
                unWrappedData = measuredPV_DA.booleanValue("unWrapped");
            }
            
            if(name.equals(BPM1PhaseName)) {
                mv_tmp = theDoc.scanStuff.BPM1PhaseMV;
            }
            else if(name.equals(BPM1AmpName)) {
                mv_tmp = theDoc.scanStuff.BPM1AmpMV;
            }
            else if(name.equals(BPM2PhaseName)) {
                mv_tmp = theDoc.scanStuff.BPM2PhaseMV;
            }
            else if(name.equals(BPM2AmpName)){
                mv_tmp = theDoc.scanStuff.BPM2AmpMV;
            }
            else if(name.equals(cavAmpRBName)){
                mv_tmp = theDoc.scanStuff.cavAmpRBMV;
            }
            else if(name.equals(BCMName)){
                mv_tmp = theDoc.scanStuff.BCMMV;
            }
            else {
                String errText = "Oh no!, an unidentified set of measured data was encountered while reading the setup file";
                theDoc.myWindow().errorText.setText(errText);
                System.err.println(errText);
                return;
            }
            
            //Channel channel = ChannelFactory.defaultFactory().getChannel(name);
            //MeasuredValue mv_tmp = new MeasuredValue(channel.getId());
            mv_tmp.generateUnwrappedData(unWrappedData);
            //mv_tmp.setChannel(channel);
            //measuredValuesV.add(mv_tmp);
            //scanController.addMeasuredValue(mv_tmp);
            
            for(DataAdaptor data : measuredPV_DA.childAdaptors("Graph_For_scanPV")) {
                BasicGraphData gd = new BasicGraphData();
                mv_tmp.addNewDataConatainer(gd);
                
                String legend = data.stringValue("legend");
                
                XmlDataAdaptor paramDataValue = (XmlDataAdaptor) data.childAdaptor("parameter_value");
                if(paramDataValue != null){
                    double parameter_value = paramDataValue.doubleValue("value");
                    gd.setGraphProperty("PARAMETER_VALUE",new Double(parameter_value));
                }
                
                XmlDataAdaptor paramDataValueRB = (XmlDataAdaptor) data.childAdaptor("parameter_value_RB");
                if(paramDataValueRB != null){
                    double parameter_value_RB = paramDataValueRB.doubleValue("value");
                    gd.setGraphProperty("PARAMETER_VALUE_RB",new Double(parameter_value_RB));
                }
                
                gd.setGraphProperty(graphScan.getLegendKeyString(),legend);
                for(DataAdaptor xyerr : data.childAdaptors("XYErr")) {
                    gd.addPoint(xyerr.doubleValue("x"),
                                xyerr.doubleValue("y"),
                                xyerr.doubleValue("err"));
                }
                
            }
            
            for(DataAdaptor data : measuredPV_DA.childAdaptors("Graph_For_scanPV_RB")) {
                String legend = data.stringValue("legend");
                BasicGraphData gd = new BasicGraphData();
                mv_tmp.addNewDataConatainerRB(gd);
                if(gd != null){
                    //gd.setGraphProperty(graphScan.getLegendKeyString(),legend);
                    for(DataAdaptor xyerr : data.childAdaptors("XYErr")) {
                        gd.addPoint(xyerr.doubleValue("x"),
                                    xyerr.doubleValue("y"),
                                    xyerr.doubleValue("err"));
                    }
                }
            }
            
	    }
    }
    
    /** A method to parse the scan parameters and set up the scanStuff */
    
    private void readScan1D(DataAdaptor scan1D_Adaptor) {
        // local copies of scanStuff members - since I copied the code from a place
        // with everything local.
  	    ScanVariable scanVariable = theDoc.scanStuff.scanVariable;
	    ScanController1D scanController = theDoc.scanStuff.scanController1D;
	    AvgController avgCntr = theDoc.scanStuff.avgCntr1D;
	    Vector<MeasuredValue> measuredValuesV = theDoc.scanStuff.measuredValuesOffV ;
	    FunctionGraphsJPanel graphScan = theDoc.scanStuff.graphScan1D;
	    
	    XmlDataAdaptor scanPV_scan1D = (XmlDataAdaptor) scan1D_Adaptor.childAdaptor(scanPV_SR);
	    XmlDataAdaptor measurePVs_scan1D = (XmlDataAdaptor) scan1D_Adaptor.childAdaptor(measureOffPVs_SR);
        
        XmlDataAdaptor scan_params_DA = (XmlDataAdaptor) scan1D_Adaptor.childAdaptor("scan_params");
	    
	    if( scan_params_DA!= null){
		    XmlDataAdaptor scan_limits_DA = (XmlDataAdaptor) scan_params_DA.childAdaptor("limits_step_delay");
		    if(scan_limits_DA != null) {
			    scanController.setLowLimit(scan_limits_DA.doubleValue("low")); scanController.setUppLimit(scan_limits_DA.doubleValue("upp")); scanController.setStep(scan_limits_DA.doubleValue("step")); scanController.setSleepTime(scan_limits_DA.doubleValue("delay"));
		    }
		    
		    //set beam trigger state and time delay
		    XmlDataAdaptor params_trigger = (XmlDataAdaptor) scan_params_DA.childAdaptor("beam_trigger");
		    if(params_trigger != null){
                scanController.setBeamTriggerDelay(params_trigger.doubleValue("delay"));  scanController.setBeamTriggerState(params_trigger.booleanValue("on"));
		    }
            
		    //set averaging parameter
		    XmlDataAdaptor params_averg =(XmlDataAdaptor) scan_params_DA.childAdaptor("averaging");
		    avgCntr.setOnOff(params_averg.booleanValue("on"));
		    avgCntr.setAvgNumber(params_averg.intValue("N"));
		    avgCntr.setTimeDelay(params_averg.doubleValue("delay"));
	    }
        
	    //set scan PVs
	    XmlDataAdaptor scan_PV_DA = (XmlDataAdaptor) scan1D_Adaptor.childAdaptor("scan_PV");
	    
	    if(scan_PV_DA != null){
            XmlDataAdaptor scan_PV_name_DA = (XmlDataAdaptor) scan_PV_DA.childAdaptor("PV");
            if(scan_PV_name_DA != null){
                String scan_PV_name = scan_PV_name_DA.stringValue("name");
                if(scan_PV_name != null) {
                    Channel channel = ChannelFactory.defaultFactory().getChannel(scan_PV_name);
                    scanVariable.setChannel(channel);
                }
            }
            XmlDataAdaptor scan_PV_RB_name_DA = (XmlDataAdaptor) scan_PV_DA.childAdaptor("PV_RB");
            if(scan_PV_RB_name_DA != null){
                String scan_PV_RB_name = scan_PV_RB_name_DA.stringValue("name");
                Channel channel = ChannelFactory.defaultFactory().getChannel(scan_PV_RB_name);
                scanVariable.setChannelRB(channel);
            }
	    }
        
        
        
	    //set measured PVs and graph's data
        MeasuredValue mv_tmp;
        for(final DataAdaptor measuredPV_DA : measurePVs_scan1D.childAdaptors()) {
            String name = measuredPV_DA.stringValue("name");
            boolean onOff = measuredPV_DA.booleanValue("on");
            boolean unWrappedData = false;
            if(measuredPV_DA.stringValue("unWrapped") != null){
                unWrappedData = measuredPV_DA.booleanValue("unWrapped");
            }
            
            if(name.equals(BPM1PhaseName)) {
                mv_tmp = theDoc.scanStuff.BPM1PhaseOffMV;
            }
            else if(name.equals(BPM2PhaseName)) {
                mv_tmp = theDoc.scanStuff.BPM2PhaseOffMV;
            }
            else if(name.equals(BCMName)){
                mv_tmp = theDoc.scanStuff.BCMOffMV;
            }
            else {
                String errText = "Oh no!, an unidentified set of measured data was encountered while reading the setup file";
                theDoc.myWindow().errorText.setText(errText);
                System.err.println(errText);
                return;
            }
            
            mv_tmp.generateUnwrappedData(unWrappedData);
            
            for(final DataAdaptor data : measuredPV_DA.childAdaptors("Graph_For_scanPV")) {
                BasicGraphData gd = new BasicGraphData();
                mv_tmp.addNewDataConatainer(gd);
                
                String legend = data.stringValue("legend");
                
                XmlDataAdaptor paramDataValue = (XmlDataAdaptor) data.childAdaptor("parameter_value");
                if(paramDataValue != null){
                    double parameter_value = paramDataValue.doubleValue("value");
                    gd.setGraphProperty("PARAMETER_VALUE",new Double(parameter_value));
                }
                
                XmlDataAdaptor paramDataValueRB = (XmlDataAdaptor) data.childAdaptor("parameter_value_RB");
                if(paramDataValueRB != null){
                    double parameter_value_RB = paramDataValueRB.doubleValue("value");
                    gd.setGraphProperty("PARAMETER_VALUE_RB",new Double(parameter_value_RB));
                }
                
                gd.setGraphProperty(graphScan.getLegendKeyString(),legend);
                for(DataAdaptor xyerr : data.childAdaptors("XYErr")) {
                    gd.addPoint(xyerr.doubleValue("x"),
                                xyerr.doubleValue("y"),
                                xyerr.doubleValue("err"));
                }
                
            }
            
            for(final DataAdaptor data : measuredPV_DA.childAdaptors("Graph_For_scanPV_RB")) {
                String legend = data.stringValue("legend");
                BasicGraphData gd = new BasicGraphData();
                mv_tmp.addNewDataConatainerRB(gd);
                if(gd != null){
                    gd.setGraphProperty(graphScan.getLegendKeyString(),legend);
                    for(DataAdaptor xyerr : data.childAdaptors("XYErr")) {
                        gd.addPoint(xyerr.doubleValue("x"),
                                    xyerr.doubleValue("y"),
                                    xyerr.doubleValue("err"));
                    }
                }
            }
            
	    }
    }
    
}
