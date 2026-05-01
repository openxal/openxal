package xal.extension.scan.analysis;

import java.util.*;
import java.awt.*;
import java.text.*;
import javax.swing.*;

import xal.tools.data.DataAdaptor;
import xal.extension.scan.*;
import xal.extension.widgets.plot.*;

/**
 *  This is a factory to produce different type of analysis.
 *
 *@author     A. Shishlo
 *@version    1.0
 */

public class AnalysisControllerFactory {

	private static String[] analysisTypes = {"MANAGEMENT",
			"FIND_MIN_MAX",
			"POLYNOMIAL_FITTING",
			"INTERSECTION_FINDING",
			"DTL_PHASE_SCAN",
			"DT_PROCEDURE"};


	/**
	 *  The constructor.
	 */
	private AnalysisControllerFactory() { }


	/**
	 *  Returns the specific analysis controller.
	 *
	 *@param  mainController         Description of the Parameter
	 *@param  analysisConf           Description of the Parameter
	 *@param  parentAnalysisPanel    Description of the Parameter
	 *@param  customControlPanel     Description of the Parameter
	 *@param  customGraphPanel       Description of the Parameter
	 *@param  globalButtonsPanel     Description of the Parameter
	 *@param  scanVariableParameter  Description of the Parameter
	 *@param  scanVariable           Description of the Parameter
	 *@param  measuredValuesV        Description of the Parameter
	 *@param  graphAnalysis          Description of the Parameter
	 *@param  messageTextLocal       Description of the Parameter
	 *@param  graphDataLocal         Description of the Parameter
	 *@return                        The aC value
	 */
	public static AnalysisController getAC(MainAnalysisController mainController,
			DataAdaptor analysisConf,
			JPanel parentAnalysisPanel,
			JPanel customControlPanel,
			JPanel customGraphPanel,
			JPanel globalButtonsPanel,
			ScanVariable scanVariableParameter,
			ScanVariable scanVariable,
			Vector<MeasuredValue> measuredValuesV,
			FunctionGraphsJPanel graphAnalysis,
			JTextField messageTextLocal,
			BasicGraphData graphDataLocal) {

		int analysisIndex = -1;
		for (int i = 0; i < analysisTypes.length; i++) {
			if (analysisTypes[i].equals(analysisConf.name())) {
				analysisIndex = i;
			}
		}

		AnalysisController AC = null;

		if (analysisIndex == 0) {
			AC = new AnalysisCntrlManagement(mainController,
					analysisConf,
					parentAnalysisPanel,
					customControlPanel,
					customGraphPanel,
					globalButtonsPanel,
					scanVariableParameter,
					scanVariable,
					measuredValuesV,
					graphAnalysis,
					messageTextLocal,
					graphDataLocal);
		} else if (analysisIndex == 1) {
			AC = new AnalysisCntrlFindMinMax(mainController,
					analysisConf,
					parentAnalysisPanel,
					customControlPanel,
					customGraphPanel,
					globalButtonsPanel,
					scanVariableParameter,
					scanVariable,
					measuredValuesV,
					graphAnalysis,
					messageTextLocal,
					graphDataLocal);
		} else if (analysisIndex == 2) {
			AC = new AnalysisCntrlPolynomFit(mainController,
					analysisConf,
					parentAnalysisPanel,
					customControlPanel,
					customGraphPanel,
					globalButtonsPanel,
					scanVariableParameter,
					scanVariable,
					measuredValuesV,
					graphAnalysis,
					messageTextLocal,
					graphDataLocal);
		} else if (analysisIndex == 3) {
			AC = new AnalysisCntrlFindIntersection(mainController,
					analysisConf,
					parentAnalysisPanel,
					customControlPanel,
					customGraphPanel,
					globalButtonsPanel,
					scanVariableParameter,
					scanVariable,
					measuredValuesV,
					graphAnalysis,
					messageTextLocal,
					graphDataLocal);
		} else if (analysisIndex == 4) {
			AC = new AnalysisCntrlDTLPhase(mainController,
					analysisConf,
					parentAnalysisPanel,
					customControlPanel,
					customGraphPanel,
					globalButtonsPanel,
					scanVariableParameter,
					scanVariable,
					measuredValuesV,
					graphAnalysis,
					messageTextLocal,
					graphDataLocal);
		} else if (analysisIndex == 5) {
			AC = new AnalysisCntrlTDProcedure(mainController,
					analysisConf,
					parentAnalysisPanel,
					customControlPanel,
					customGraphPanel,
					globalButtonsPanel,
					scanVariableParameter,
					scanVariable,
					measuredValuesV,
					graphAnalysis,
					messageTextLocal,
					graphDataLocal);
		}

		if (AC != null) {
			AC.setTypeName(analysisTypes[analysisIndex]);
		}

		return AC;
	}
}

