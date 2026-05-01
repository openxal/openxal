package xal.extension.scan.analysis;

import java.util.*;
import java.awt.*;
import java.text.*;
import javax.swing.*;
import java.awt.event.*;

import xal.tools.data.DataAdaptor;
import xal.extension.scan.*;
import xal.extension.widgets.plot.*;

/**
 * This class is a analysis class for polynomial fitting.
 *
 * @version 1.0
 * @author  A. Shishlo
 */

final public class AnalysisCntrlPolynomFit extends AnalysisController{

    //DEFINITION  "POLYNOMIAL FITTING" PANEL
    private JPanel polynomFitMaxPanel = new JPanel();
    private JButton fittingPanel_2_Button = new JButton("START FITTING");
    private JSpinner rankPanel_2_Spinner  = new JSpinner(new SpinnerNumberModel(0,0,3,1)); 
    private JLabel spinnerPanel_2_Label   = new JLabel(" Order of Fitting :",JLabel.LEFT);
    private DecimalFormat coeffPanel_2_Format = new DecimalFormat("0.000E0");

    /**  The constructor.*/   
    public AnalysisCntrlPolynomFit(MainAnalysisController mainController_In,
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


	String nameIn = "POLYNOMIAL FITTING";
	DataAdaptor nameDA =   analysisConf.childAdaptor("ANALYSIS_NAME");
	if(nameDA != null){
	    nameIn = nameDA.stringValue("name");
	}
        setName(nameIn);

	//create fitting panel
	makePolynomFittingPanel();
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

	//FITTING PANEL ELEMENTS
	fittingPanel_2_Button.setFont(fnt);
	rankPanel_2_Spinner.setFont(fnt);
	((JSpinner.DefaultEditor) rankPanel_2_Spinner.getEditor()).getTextField().setFont(fnt);
	spinnerPanel_2_Label.setFont(fnt);
    }

    /**  Does what necessary for close this analysis window. 
     */  
    public void ShutUp(){
	super.ShutUp();
	customControlPanel.removeAll();
    }

    /**  Does what necessary for open this analysis window. 
     *   This method could be overridden, because it is empty here.
     */  
    public void ShowUp(){
	super.ShowUp();
	customControlPanel.add(dataReaderPanel,BorderLayout.NORTH);
	customControlPanel.add(polynomFitMaxPanel,BorderLayout.CENTER);
        customGraphPanel.add(graphAnalysis,BorderLayout.CENTER);
        customGraphPanel.add(globalButtonsPanel,BorderLayout.SOUTH);
    }

    /**
     * Updates data on the analysis graph panel.  
     */
    public void updateDataSetOnGraphPanel(){
	super.updateDataSetOnGraphPanel();
    }

    //-----------------------------------------------------
    //PANEL DEFINITION
    //-----------------------------------------------------
    private void makePolynomFittingPanel(){

	rankPanel_2_Spinner.setAlignmentX(JSpinner.CENTER_ALIGNMENT);

	JPanel tmp_0 = new JPanel();
        tmp_0.setLayout(new GridLayout(1,2,1,1));
        tmp_0.add(spinnerPanel_2_Label);
        tmp_0.add(rankPanel_2_Spinner);
        

	JPanel tmp_1 = new JPanel();
        tmp_1.setLayout(new GridLayout(2,1,1,1));
        tmp_1.add(tmp_0);
        tmp_1.add(fittingPanel_2_Button);

        polynomFitMaxPanel.setLayout(new BorderLayout());
        polynomFitMaxPanel.add(tmp_1,BorderLayout.NORTH);

	fittingPanel_2_Button.addActionListener(new ActionListener(){
		public void actionPerformed(ActionEvent e){
		    BasicGraphData gd = mainController.getChoosenDraphData();
                    if(gd != null){
			graphAnalysis.removeGraphData(graphDataLocal);
			graphDataLocal.removeAllPoints();
			double xMin = graphAnalysis.getCurrentMinX();
			double xMax = graphAnalysis.getCurrentMaxX();
			double yMin = graphAnalysis.getCurrentMinY();
			double yMax = graphAnalysis.getCurrentMaxY();
                        int order = ((Integer) rankPanel_2_Spinner.getValue()).intValue();
			GraphDataOperations.polynomialFit(gd,graphDataLocal,xMin,xMax,order,10);

			double[][] coeff =  GraphDataOperations.polynomialFit(gd,xMin,xMax,order);
                        if(coeff != null && coeff[0].length > 0){
                            String formula = "Fitting: y = ";
			    for(int i = 0, n=coeff[0].length; i < n; i++){
				formula = formula + "("+coeffPanel_2_Format.format(coeff[0][i]) +")*X^"+i;
                                if(i != (n-1)){
				    formula = formula +" + ";
				}
			    }
			    messageTextLocal.setText(null);
			    messageTextLocal.setText(formula);
			}
			else{
			    messageTextLocal.setText(null);
			    messageTextLocal.setText("Cannot do fitting.");
			    Toolkit.getDefaultToolkit().beep();
			}
			graphAnalysis.addGraphData(graphDataLocal);
		    }
		    else{
			messageTextLocal.setText(null);
			messageTextLocal.setText("Please choose graph and point first. Use S-button on the graph panel.");
			Toolkit.getDefaultToolkit().beep();
		    }
		}
	    }); 

	fittingPanel_2_Button.setForeground(Color.blue);
    }

}
