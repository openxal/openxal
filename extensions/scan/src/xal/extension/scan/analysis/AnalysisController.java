package xal.extension.scan.analysis;

import java.util.*;
import java.awt.*;
import java.text.*;
import javax.swing.*;
import javax.swing.border.*;

import xal.tools.data.DataAdaptor;
import xal.tools.xml.*;
import xal.extension.scan.*;
import xal.extension.widgets.plot.*;

/**
 * This class is a base class for different analysis of the scan data.
 *
 * @version 1.0
 * @author  A. Shishlo
 */

public class AnalysisController{

    protected String name = "EMPTY";
    protected String typeName = "EMPTY_TYPE";

    //variables from upper level application
    protected boolean scanPV_ShowState    = false;
    protected boolean scanPV_RB_ShowState = false;

    protected MainAnalysisController mainController = null;

    protected JPanel parentAnalysisPanel  = null;
    protected JPanel customControlPanel   = null;
    protected JPanel customGraphPanel     = null;
    protected JPanel globalButtonsPanel   = null;

    protected ScanVariable scanVariableParameter = null;
    protected ScanVariable scanVariable          = null;
    protected Vector<MeasuredValue> measuredValuesV             = null;
    protected FunctionGraphsJPanel graphAnalysis = null;
    protected JTextField messageTextLocal        = null;

    //data reader panel
    protected JPanel dataReaderPanel = null;

    //local temporary draph data 
    protected BasicGraphData graphDataLocal = null;

    /**  The constructor.*/   
    public AnalysisController( MainAnalysisController mainController_In,
			       DataAdaptor analysisConf,
			       JPanel parentAnalysisPanel_In,
			       JPanel customControlPanel_In,
			       JPanel customGraphPanel_In,
			       JPanel globalButtonsPanel_In,
			       ScanVariable scanVariableParameter_In,
			       ScanVariable scanVariable_In,
			       Vector<MeasuredValue> measuredValuesV_In,
			       FunctionGraphsJPanel graphAnalysis_In,
			       JTextField messageTextLocal_In,
			       BasicGraphData graphDataLocal_In){


	mainController = mainController_In;
	parentAnalysisPanel = parentAnalysisPanel_In;
	customControlPanel = customControlPanel_In;
	customGraphPanel = customGraphPanel_In;
	globalButtonsPanel = globalButtonsPanel_In;
	scanVariableParameter = scanVariableParameter_In;
	scanVariable = scanVariable_In;
	measuredValuesV = measuredValuesV_In;
	graphAnalysis = graphAnalysis_In;
	messageTextLocal = messageTextLocal_In;
	graphDataLocal = graphDataLocal_In;
        dataReaderPanel = mainController.getDataReaderPanel();

    }

    /**  Sets the name of the analysis.*/  
    public void setName(String name){
	this.name = name;
    }

    /**  Sets the type name of the analysis.*/  
    public void setTypeName(String typeName){
	this.typeName = typeName;
    }

    /**  Returns the name of the analysis.*/  
    public String getName(){
	return name;
    }

    /**  Returns the type name of the analysis.*/  
    public String getTypeName(){
	return typeName;
    }

    /**
     * Sets mask specifying if the data for scan PV  scan read back PV should be shown.
     */
    public void setScanPVandScanPV_RB_State(boolean scanPV_ShowState,boolean scanPV_RB_ShowState) {
        this.scanPV_ShowState = scanPV_ShowState;
        this.scanPV_RB_ShowState = scanPV_RB_ShowState;
    }

    /**
     * Sets the configuration of the analysis.
     * The subclasses should call the super-class method in this method.
     */
    public void dumpAnalysisConfig(DataAdaptor analysisConfig){
       DataAdaptor nameDA = analysisConfig.createChild("ANALYSIS_NAME");
       nameDA.setValue("name",getName());
    }

    /**  Sets fonts for all GUI elements.
     *   The subclasses should call the super-class method in this method.
     */  
    public void setFontsForAll(Font fnt){

    }

    /**  Does what necessary for close this analysis window. 
     *   This method could be overridden, because it is empty here.
     */  
    public void ShutUp(){
    }

    /**  Does what necessary for open this analysis window. 
     *   This method could be overridden, because it is empty here.
     */  
    public void ShowUp(){
    }

    /**
     * Updates data on the analysis graph panel. 
     * This method will be called outside of this analysis controller,
     * and should update data only related to the inner business for 
     * this analysis.
     * This method could be overridden, because it is empty here. 
     */
    public void updateDataSetOnGraphPanel(){
    }

    /**
     * Sets local message text field.
     */  
    public void setMessageTextField(JTextField messageTextLocal) {
	this.messageTextLocal = messageTextLocal;
    }


}
