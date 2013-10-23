package xal.extension.scan.analysis;

import java.util.*;
import java.awt.*;
import java.text.*;
import javax.swing.*;

import xal.tools.data.DataAdaptor;
import xal.extension.scan.*;
import xal.extension.widgets.plot.*;

/**
 *  This class is an empty analysis class. It analyzes the scan data.
 *
 *@author     A. Shishlo
 *@version    1.0
 */

public final class AnalysisCntrlEmpty extends AnalysisController {

    /**
     *  The constructor.
     *
     *@param  mainController_In         The MainAnalysisController reference
     *@param  analysisConf              The DataAdaptor instance with
     *      configuration data
     *@param  parentAnalysisPanel_In    The parent panel for analysis
     *@param  customControlPanel_In     The control panel for GUI elements
     *      specific for this analysis
     *@param  customGraphPanel_In       The graph panel for graphs specific for
     *      this analysis
     *@param  globalButtonsPanel_In     The global buttons panel
     *@param  scanVariableParameter_In  The ScanParameter reference
     *@param  scanVariable_In           The scan variable reference
     *@param  measuredValuesV_In        The vector with measured values
     *      references
     *@param  graphAnalysis_In          The graphAnalysis panel
     *@param  messageTextLocal_In       The message text field
     *@param  graphDataLocal_In         The external graph data for temporary
     *      graph
     */
    public AnalysisCntrlEmpty(MainAnalysisController mainController_In,
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
        BasicGraphData graphDataLocal_In) {

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

        String nameIn = "DATA READER";
        DataAdaptor nameDA =  analysisConf.childAdaptor("ANALYSIS_NAME");
        if (nameDA != null) {
            nameIn = nameDA.stringValue("name");
        }
        setName(nameIn);

    }


    /**
     *  Sets the configurations of the analysis.
     *
     *@param  analysisConfig  The DataAdaptor instance with configuration
     *      data
     */
    public void dumpAnalysisConfig(DataAdaptor analysisConfig) {
        super.dumpAnalysisConfig(analysisConfig);
    }


    /**
     *  Sets fonts for all GUI elements.
     *
     *@param  fnt  The new font
     */
    public void setFontsForAll(Font fnt) {
        super.setFontsForAll(fnt);
    }


    /**
     *  Does what necessary to close this analysis window.
     */
    public void ShutUp() {
        super.ShutUp();
    }


    /**
     *  Does what necessary to open this analysis window. This method could be
     *  overridden, because it is empty here.
     */
    public void ShowUp() {
        super.ShowUp();
        customControlPanel.add(dataReaderPanel, BorderLayout.NORTH);
        customGraphPanel.add(graphAnalysis, BorderLayout.CENTER);
        customGraphPanel.add(globalButtonsPanel, BorderLayout.SOUTH);
    }


    /**
     *  Updates data on the analysis graph panel.
     */
    public void updateDataSetOnGraphPanel() {
        super.updateDataSetOnGraphPanel();
    }
}

