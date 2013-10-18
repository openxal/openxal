package xal.extension.scan.analysis;

import java.util.*;
import java.awt.*;
import java.text.*;
import javax.swing.*;

import xal.tools.data.DataAdaptor;
import xal.extension.scan.*;
import xal.extension.widgets.plot.*;

/**
 * This class is a analysis class for general data reading and management.
 *
 * @version 1.0
 * @author  A. Shishlo
 */

final public class AnalysisCntrlManagement extends AnalysisController{


    /**  The constructor.*/   
    public AnalysisCntrlManagement(MainAnalysisController mainController_In,
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

	//call the superclass constructor
	super(mainController_In,
	      analysisConf,
	      parentAnalysisPanel_In,
	      customControlPanel_In,
	      customGraphPanel_In,
	      globalButtonsPanel_In,
	      scanVariableParameter_In,
	      scanVariable_In,
	      measuredValuesV_In,
	      graphAnalysis_In,
	      messageTextLocal_In,
	      graphDataLocal_In);

	String nameIn = "MANAGEMENT";
	DataAdaptor nameDA =   analysisConf.childAdaptor("ANALYSIS_NAME");
	if(nameDA != null){
	    nameIn = nameDA.stringValue("name");
	}
        setName(nameIn);

    }

    /**
     * Sets the configurations of the analysis.
     */
    public void dumpAnalysisConfig(DataAdaptor analysisConfig){
	super.dumpAnalysisConfig(analysisConfig);
    }

    /**  Sets fonts for all GUI elements.
     */  
    public void setFontsForAll(Font fnt){
	super.setFontsForAll(fnt);
    }

    /**  Does what necessary for close this analysis window. 
     */  
    public void ShutUp(){
	super.ShutUp();
	customControlPanel.removeAll();
	customGraphPanel.removeAll();
    }

    /**  Does what necessary for open this analysis window. 
     *   This method could be overridden, because it is empty here.
     */  
    public void ShowUp(){
	super.ShowUp();
	customControlPanel.add(dataReaderPanel,BorderLayout.NORTH);
        customGraphPanel.add(graphAnalysis,BorderLayout.CENTER);
        customGraphPanel.add(globalButtonsPanel,BorderLayout.SOUTH);
    }

    /**
     * Updates data on the analysis graph panel.  
     */
    public void updateDataSetOnGraphPanel(){
	super.updateDataSetOnGraphPanel();
    }
}
