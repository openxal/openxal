package xal.extension.scan.analysis;

import java.util.*;
import java.awt.*;
import java.text.*;
import javax.swing.*;
import java.io.*;
import java.awt.event.*;
import javax.swing.text.*;
import javax.swing.border.*;
import java.net.*;

import java.util.prefs.*;

import xal.tools.data.DataAdaptor;
import xal.extension.scan.*;
import xal.extension.widgets.plot.*;
import xal.extension.application.Application;
import xal.tools.apputils.VerticalLayout;
import xal.extension.widgets.swing.*;
import xal.tools.text.FortranNumberFormat;

/**
 *  This creates an ASCII file for dT procedure
 *
 *@author     A. Shishlo
 *@version    1.0
 */

public final class AnalysisCntrlTDProcedure extends AnalysisController {

	//readiness of the analysis results
	private boolean analysisDone = false;

	private static Border etchedBorder = BorderFactory.createEtchedBorder();

	//local control panel
	private JPanel localCntrlPanel = new JPanel();

	//Buttons
	private JButton exportDataButton = new JButton("EXPORT DATA TO LABVIEW");
	private JButton makeAnalysisButton = new JButton("PERFORM ANALYSIS");

	//vectors with graph data for B and C BPMs
	private Vector<BasicGraphData> gdV_B = new Vector<BasicGraphData>();
	private Vector<BasicGraphData> gdV_C = new Vector<BasicGraphData>();

	//Cavity of CCL index
	private int cavIndex = -1;

	//date and time
	private SimpleDateFormat dateFormat = null;
	private JFormattedTextField dateTimeField = null;

	//numbers format
	private DecimalFormat int_Format = new DecimalFormat("###0");
	private DecimalFormat dbl_Format = new DecimalFormat("###0.0###");

	//local data file
	private File dataFile = null;

	//default path for ascii file
	private String defaultPath = null;

	//alias in preferences
	private String defaultPathName = "default_export_file_path";

	//left custom control panel
	private JTextField leftTitle = new JTextField("===================SCAN DATA====================");
	private int verticalLeftGraphSize = 13;
	private JLabel verticalLeftGraphLabel_1 = new JLabel(" ");
	private JLabel verticalLeftGraphLabel_2 = new JLabel(" ");

	//TOP custom panel
	private JLabel moduleNameLabel = new JLabel("Name of the Module", JLabel.CENTER);
	private String bpm1_NameString = "BPM #1 name";
	private String bpm2_NameString = "BPM #2 name";
	private JLabel bpm1_NameLabel = new JLabel("  BPM #1 :  ", JLabel.CENTER);
	private JLabel bpm2_NameLabel = new JLabel("  BPM #2 :  ", JLabel.CENTER);
	private JRadioButton aMatrixSwitchButton = new JRadioButton("Use matrix A for Module Amplitude Deviation = 0      ");

	private JLabel rfPhaseLabel = new JLabel("  Recomended RF Phase, deg", JLabel.LEFT);
	private JLabel rfAmpLabel = new JLabel("  Recomended RF Amplitude", JLabel.LEFT);
	private JButton setToAccelButton = new JButton("  SET VALUES TO RF CAVITY  ");

	private JLabel inputEnergyDevLabel = new JLabel("  Input Energy Deviation, keV", JLabel.LEFT);
	private JLabel currentAmpLabel = new JLabel("  Current RF Amplitude", JLabel.LEFT);
	private JLabel expSlopeLabel = new JLabel("  Experimental Slope, deg", JLabel.LEFT);
	private JLabel energyStepLabel = new JLabel("  Step for Energy Markers, keV", JLabel.LEFT);

	private DoubleInputTextField rfPhase_Text = new DoubleInputTextField(10);
	private DoubleInputTextField rfAmp_Text = new DoubleInputTextField(10);
	private DoubleInputTextField inputEnergyDev_Text = new DoubleInputTextField(10);
	private DoubleInputTextField currentAmp_Text = new DoubleInputTextField(10);
	private DoubleInputTextField expSlope_Text = new DoubleInputTextField(10);
	private DoubleInputTextField energyStep_Text = new DoubleInputTextField(10);

	private DecimalFormat format = new FortranNumberFormat("G10.4");

	//main local analysis panel
	private JPanel localAnalysisPanel = new JPanel();
	private FunctionGraphsJPanel dphi12graphPanel = new FunctionGraphsJPanel();
	private FunctionGraphsJPanel ampDevGraphPanel = new FunctionGraphsJPanel();

	//left graphs with experimental data
	private FunctionGraphsJPanel graphLeftPanel_1 = new FunctionGraphsJPanel();
	private FunctionGraphsJPanel graphLeftPanel_2 = new FunctionGraphsJPanel();

	//graph data
	private BasicGraphData ampDev_vs_slope_th_gd = new CubicSplineGraphData();
	private BasicGraphData ampDev_vs_slope_ex_gd = new BasicGraphData();

	private BasicGraphData bpm1_ex_gd = new BasicGraphData();
	private BasicGraphData bpm1_interp_gd = new BasicGraphData();
	private BasicGraphData bpm2_ex_gd = new BasicGraphData();
	private BasicGraphData bpm2_interp_gd = new BasicGraphData();

	private BasicGraphData bpm12_ex_gd = new BasicGraphData();
	private BasicGraphData bpm12_interp_gd = new BasicGraphData();

	private BasicGraphData energy_line_gd = new BasicGraphData();
	private BasicGraphData energy_Pos_gd = new BasicGraphData();
	private BasicGraphData energy_Neg_gd = new BasicGraphData();

	//raw data from scan
	private BasicGraphData gd_bpm_B_on = null;
	private BasicGraphData gd_bpm_C_on = null;
	private BasicGraphData gd_bpm_B_off = null;
	private BasicGraphData gd_bpm_C_off = null;

	//the name of data file with A-matrices
	private String fileName_TheoryData = "";

	private DeltaTdata theoryData = new DeltaTdata();


	/**
	 *  The constructor.
	 *
	 *@param  mainController_In         The MainAnalysisController reference
	 *@param  analysisConf              The DataAdaptor instance with
	 *      configuration data
	 *@param  parentAnalysisPanel_In    The parent panel for analysis
	 *@param  customControlPanel_In     The control panel for GUI elements specific
	 *      for this analysis
	 *@param  customGraphPanel_In       The graph panel for graphs specific for
	 *      this analysis
	 *@param  globalButtonsPanel_In     The global buttons panel
	 *@param  scanVariableParameter_In  The ScanParameter reference
	 *@param  scanVariable_In           The scan variable reference
	 *@param  measuredValuesV_In        The vector with measured values references
	 *@param  graphAnalysis_In          The graphAnalysis panel
	 *@param  messageTextLocal_In       The message text field
	 *@param  graphDataLocal_In         The external graph data for temporary graph
	 */
	public AnalysisCntrlTDProcedure(MainAnalysisController mainController_In,
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

		//reading data from data adaptor
		String nameIn = "DELTA-T PROCEDURE ANALYSIS";
		DataAdaptor nameDA =  analysisConf.childAdaptor("ANALYSIS_NAME");
		if (nameDA != null) {
			nameIn = nameDA.stringValue("name");
		}
		setName(nameIn);

		DataAdaptor cavInfoDA =  analysisConf.childAdaptor("CAVITY_INFO");
		if (cavInfoDA != null) {
			cavIndex = cavInfoDA.intValue("index");
			moduleNameLabel.setText(cavInfoDA.stringValue("cavity_name"));
			bpm1_NameString = cavInfoDA.stringValue("bpm1_name");
			bpm2_NameString = cavInfoDA.stringValue("bpm2_name");
			bpm1_NameLabel.setText(bpm1_NameLabel.getText() + bpm1_NameString + "  ");
			bpm2_NameLabel.setText(bpm2_NameLabel.getText() + bpm2_NameString + "  ");
			energyStep_Text.setValue(cavInfoDA.doubleValue("energy_step_kev"));
		}

		DataAdaptor thFileDA =  analysisConf.childAdaptor("THEORETICAL_FILE");
		if (thFileDA != null) {
			String fileName = thFileDA.stringValue("name");
			readTheoryData(fileName);
		}

		//get the preference
		Preferences pref = xal.tools.apputils.Preferences.nodeForPackage(this.getClass());
		defaultPath = pref.get(defaultPathName, null);

		//set action listener to button
		defineButtonActions();

		//date and time format definition
		dateFormat = new SimpleDateFormat(" MM/dd/yyyy HH:mm ");
		dateTimeField = new JFormattedTextField(dateFormat);

		//specify the left local panel
		JPanel tmp_l = new JPanel();
		tmp_l.setLayout(new FlowLayout(FlowLayout.CENTER));
		tmp_l.add(makeAnalysisButton);
		makeAnalysisButton.setForeground(Color.red);

		//specify the left local panel
		JPanel tmp_3 = new JPanel();
		tmp_3.setLayout(new FlowLayout(FlowLayout.CENTER));
		tmp_3.add(exportDataButton);
		exportDataButton.setForeground(Color.red);

		//left graph panels
		JPanel tmp_2 = new JPanel();
		tmp_2.setLayout(new GridLayout(2, 1));

		JPanel tmp_2_1 = new JPanel();
		tmp_2_1.setLayout(new BorderLayout());

		JPanel tmp_2_1_L = new JPanel();
		tmp_2_1_L.setLayout(new GridLayout(verticalLeftGraphSize, 1));
		tmp_2_1_L.add(verticalLeftGraphLabel_1);
		tmp_2_1.add(tmp_2_1_L, BorderLayout.WEST);
		tmp_2_1.add(graphLeftPanel_1, BorderLayout.CENTER);

		JPanel tmp_2_2 = new JPanel();
		tmp_2_2.setLayout(new BorderLayout());

		JPanel tmp_2_2_L = new JPanel();
		tmp_2_2_L.setLayout(new GridLayout(verticalLeftGraphSize, 1));
		tmp_2_2_L.add(verticalLeftGraphLabel_2);
		tmp_2_2.add(tmp_2_2_L, BorderLayout.WEST);
		tmp_2_2.add(graphLeftPanel_2, BorderLayout.CENTER);

		tmp_2.add(tmp_2_1);
		tmp_2.add(tmp_2_2);

		tmp_2_1.setBorder(etchedBorder);
		tmp_2_2.setBorder(etchedBorder);

		localCntrlPanel.setLayout(new VerticalLayout());
		localCntrlPanel.add(leftTitle);
		localCntrlPanel.add(tmp_l);
		localCntrlPanel.add(tmp_2);
		localCntrlPanel.add(tmp_3);

		graphLeftPanel_1.setGraphBackGroundColor(Color.BLACK);
		graphLeftPanel_2.setGraphBackGroundColor(Color.BLACK);
		graphLeftPanel_1.setGridLineColor(Color.gray);
		graphLeftPanel_2.setGridLineColor(Color.gray);
		graphLeftPanel_1.setOffScreenImageDrawing(true);
		graphLeftPanel_2.setOffScreenImageDrawing(true);

		graphLeftPanel_1.setAxisNames("Module Phase, deg", "D-Phi BPM #1,deg");
		graphLeftPanel_2.setAxisNames("Module Phase, deg", "D-Phi BPM #2,deg");

		//make local analysis panel
		makeLocalAnalysisPanel();

		//make graph data
		int nP = theoryData.getNumbPoints();
		for (int i = 0; i < nP; i++) {
			ampDev_vs_slope_th_gd.addPoint(theoryData.getSlope(i), 100 * (theoryData.getAmplitude(i) - 1.0));
		}
		ampDev_vs_slope_th_gd.setGraphProperty(ampDevGraphPanel.getLegendKeyString(), " Theory Data ");
		ampDev_vs_slope_ex_gd.setGraphProperty(ampDevGraphPanel.getLegendKeyString(), " This Scan ");

		ampDev_vs_slope_th_gd.setGraphColor(Color.green);
		ampDev_vs_slope_ex_gd.setGraphColor(Color.red);
		ampDev_vs_slope_ex_gd.setGraphPointSize(8);

		ampDevGraphPanel.addGraphData(ampDev_vs_slope_th_gd);
		ampDevGraphPanel.addGraphData(ampDev_vs_slope_ex_gd);

		//graph data properties
		bpm1_ex_gd.setGraphColor(Color.green);
		bpm1_interp_gd.setGraphColor(Color.green);
		bpm1_interp_gd.setDrawPointsOn(false);
		bpm2_ex_gd.setGraphColor(Color.green);
		bpm2_interp_gd.setGraphColor(Color.green);
		bpm2_interp_gd.setDrawPointsOn(false);

		bpm12_ex_gd.setGraphColor(Color.red);
		bpm12_ex_gd.setGraphProperty(dphi12graphPanel.getLegendKeyString(), " Scan Data ");

		bpm12_interp_gd.setGraphColor(Color.blue);
		bpm12_interp_gd.setDrawPointsOn(false);
		bpm12_interp_gd.setGraphProperty(dphi12graphPanel.getLegendKeyString(), " Fit of Scan Data ");

		energy_line_gd.setGraphColor(Color.green);
		energy_line_gd.setDrawPointsOn(false);
		energy_line_gd.setGraphProperty(dphi12graphPanel.getLegendKeyString(), " Variable Energy Line ");

		Rectangle shape = new Rectangle(-4, -4, 8, 8);
		energy_Pos_gd.setGraphPointShape(shape);
		energy_Neg_gd.setGraphPointShape(shape);
		energy_Pos_gd.setGraphProperty(dphi12graphPanel.getLegendKeyString(), " Markers Delta-E > 0 ");
		energy_Neg_gd.setGraphProperty(dphi12graphPanel.getLegendKeyString(), " Markers Delta-E < 0 ");

		energy_Pos_gd.setGraphColor(Color.red);
		energy_Pos_gd.setDrawLinesOn(false);

		energy_Neg_gd.setGraphColor(Color.cyan);
		energy_Neg_gd.setDrawLinesOn(false);

		graphLeftPanel_1.addGraphData(bpm1_ex_gd);
		graphLeftPanel_1.addGraphData(bpm1_interp_gd);
		graphLeftPanel_2.addGraphData(bpm2_ex_gd);
		graphLeftPanel_2.addGraphData(bpm2_interp_gd);

		dphi12graphPanel.addGraphData(bpm12_ex_gd);
		dphi12graphPanel.addGraphData(bpm12_interp_gd);
		dphi12graphPanel.addGraphData(energy_line_gd);
		dphi12graphPanel.addGraphData(energy_Pos_gd);
		dphi12graphPanel.addGraphData(energy_Neg_gd);

		//set analysis into initial state
		clearResultsOfAnalysis();

	}



	/**
	 *  Sets the configurations of the analysis.
	 *
	 *@param  analysisConfig  The DataAdaptor instance with configuration data
	 */
	public void dumpAnalysisConfig(DataAdaptor analysisConfig) {
		super.dumpAnalysisConfig(analysisConfig);

		DataAdaptor cavInfoDA =  analysisConfig.createChild("CAVITY_INFO");
		cavInfoDA.setValue("index", cavIndex);
		cavInfoDA.setValue("cavity_name", moduleNameLabel.getText());
		cavInfoDA.setValue("bpm1_name", bpm1_NameString);
		cavInfoDA.setValue("bpm2_name", bpm2_NameString);
		cavInfoDA.setValue("energy_step_kev", energyStep_Text.getValue());

		DataAdaptor thFileDA =  analysisConfig.createChild("THEORETICAL_FILE");
		thFileDA.setValue("name", fileName_TheoryData);
	}


	/**
	 *  Sets fonts for all GUI elements.
	 *
	 *@param  fnt  The new font
	 */
	public void setFontsForAll(Font fnt) {
		super.setFontsForAll(fnt);
		exportDataButton.setFont(fnt);
		makeAnalysisButton.setFont(fnt);

		//verticalLeftGraphLabel_1.setFont(fnt);
		//verticalLeftGraphLabel_2.setFont(fnt);
		leftTitle.setFont(fnt);

		moduleNameLabel.setFont(fnt);
		bpm1_NameLabel.setFont(fnt);
		bpm2_NameLabel.setFont(fnt);

		aMatrixSwitchButton.setFont(fnt);

		rfPhaseLabel.setFont(fnt);
		rfAmpLabel.setFont(fnt);

		setToAccelButton.setFont(fnt);

		inputEnergyDevLabel.setFont(fnt);
		currentAmpLabel.setFont(fnt);
		expSlopeLabel.setFont(fnt);
		energyStepLabel.setFont(fnt);

		rfPhase_Text.setFont(fnt);
		rfAmp_Text.setFont(fnt);
		inputEnergyDev_Text.setFont(fnt);
		currentAmp_Text.setFont(fnt);
		expSlope_Text.setFont(fnt);
		energyStep_Text.setFont(fnt);

	}


	/**
	 *  Does what necessary to close this analysis window.
	 */
	public void ShutUp() {
		super.ShutUp();
		customControlPanel.removeAll();
		customGraphPanel.removeAll();
	}


	/**
	 *  Does what necessary to open this analysis window. This method could be
	 *  overridden, because it is empty here.
	 */
	public void ShowUp() {
		super.ShowUp();

		customControlPanel.add(localCntrlPanel, BorderLayout.NORTH);
		customGraphPanel.add(localAnalysisPanel, BorderLayout.CENTER);

		clearResultsOfAnalysis();

		//repaint
		parentAnalysisPanel.validate();
		parentAnalysisPanel.repaint();

	}


	/**
	 *  Updates data on the analysis graph panel.
	 */
	public void updateDataSetOnGraphPanel() {
		super.updateDataSetOnGraphPanel();
		if (analysisDone) {
			clearResultsOfAnalysis();
		}
	}


	/**
	 *  Clears all data on the analysis panel
	 */
	private void clearResultsOfAnalysis() {
		analysisDone = false;

		rfPhase_Text.setText(null);
		rfAmp_Text.setText(null);
		inputEnergyDev_Text.setText(null);
		currentAmp_Text.setText(null);
		expSlope_Text.setText(null);
		rfPhase_Text.setText(null);

		rfPhase_Text.setBackground(Color.white);
		rfAmp_Text.setBackground(Color.white);
		inputEnergyDev_Text.setBackground(Color.white);
		currentAmp_Text.setBackground(Color.white);
		expSlope_Text.setBackground(Color.white);
		rfPhase_Text.setBackground(Color.white);

		ampDev_vs_slope_ex_gd.removeAllPoints();

		bpm1_ex_gd.removeAllPoints();
		bpm1_interp_gd.removeAllPoints();
		bpm2_ex_gd.removeAllPoints();
		bpm2_interp_gd.removeAllPoints();

		bpm12_ex_gd.removeAllPoints();
		bpm12_interp_gd.removeAllPoints();
		energy_line_gd.removeAllPoints();
		energy_Pos_gd.removeAllPoints();
		energy_Neg_gd.removeAllPoints();
	}


	/**
	 *  Defines if the scan data have right form
	 *
	 *@return    True if the scan data can be used as input for delta-t procedure
	 */
	private boolean canSaveASDTtable() {

		if (cavIndex <= 0) {
			return false;
		}

		int nMeasuredValues = measuredValuesV.size();
		if (nMeasuredValues < 2) {
			return false;
		}

		MeasuredValue bpmB_mv = measuredValuesV.get(0);
		MeasuredValue bpmC_mv = measuredValuesV.get(1);

		gdV_B = bpmB_mv.getDataContainers();
		gdV_C = bpmC_mv.getDataContainers();

		if (gdV_B.size() != 2 || gdV_C.size() != 2) {
			return false;
		}

		gd_bpm_B_on = gdV_B.get(0);
		gd_bpm_C_on = gdV_C.get(0);

		gd_bpm_B_off = gdV_B.get(1);
		gd_bpm_C_off = gdV_C.get(1);

		if (gd_bpm_B_on.getNumbOfPoints() != gd_bpm_C_on.getNumbOfPoints() ||
				gd_bpm_B_off.getNumbOfPoints() != gd_bpm_C_off.getNumbOfPoints()) {
			return false;
		}

		int nP = gd_bpm_B_on.getNumbOfPoints();
		for (int i = 0; i < nP; i++) {
			if (Math.abs(gd_bpm_B_on.getX(i) - gd_bpm_C_on.getX(i)) > 0.0001) {
				return false;
			}
		}

		nP = gd_bpm_B_off.getNumbOfPoints();
		for (int i = 0; i < nP; i++) {
			if (Math.abs(gd_bpm_B_off.getX(i) - gd_bpm_C_off.getX(i)) > 0.0001) {
				return false;
			}
		}

		return true;
	}


	/**
	 *  Description of the Method
	 */
	private void defineButtonActions() {

		//make analysis button
		makeAnalysisButton.addActionListener(
			new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					makeDeltaTimeAnalysis();
				}
			});

		//set data to EPICS
		setToAccelButton.addActionListener(
			new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					setAmplitudeAndPhaseToEPICS();
				}
			});

		//"EXPORT ASCII"
		exportDataButton.addActionListener(
			new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					if (canSaveASDTtable()) {
						//save data as table
						JFileChooser ch = new JFileChooser();
						ch.setDialogTitle("Export data into dT procedure file");
						if (dataFile != null) {
							ch.setSelectedFile(dataFile);
						} else {
							if (defaultPath != null) {
								File path = new File(defaultPath);
								if (path != null && path.exists()) {
									ch.setSelectedFile(path);
								}
							}
						}
						int returnVal = ch.showSaveDialog(parentAnalysisPanel);
						if (returnVal == JFileChooser.APPROVE_OPTION) {
							try {
								dataFile = ch.getSelectedFile();
								defaultPath = dataFile.getAbsolutePath();
								BufferedWriter out = new BufferedWriter(new FileWriter(dataFile));

								Preferences pref = xal.tools.apputils.Preferences.nodeForPackage(this.getClass());
								try {
									defaultPath = dataFile.getAbsolutePath();
									pref.put(defaultPathName, defaultPath);
									pref.flush();
								} catch (BackingStoreException exp) {}

								//place to write data into acsii file
								gd_bpm_B_on = gdV_B.get(0);
								gd_bpm_C_on = gdV_C.get(0);

								gd_bpm_B_off = gdV_B.get(1);
								gd_bpm_C_off = gdV_C.get(1);

								double amplitude = ((Double) gd_bpm_B_on.getGraphProperty("PARAMETER_VALUE")).doubleValue();

								//1-st line - set date and time
								dateTimeField.setValue(new Date());

								String line = "Dt module ";
								line = line + cavIndex + dateTimeField.getText();

								line = line + " ";
								out.write(line);
								out.newLine();

								//2-nd line (nPoints for On) (Amplitude) (nPoints for Off)
								line = " ";
								line = line + int_Format.format(gd_bpm_B_on.getNumbOfPoints()) + " ";
								line = line + dbl_Format.format(amplitude) + " ";
								line = line + int_Format.format(gd_bpm_B_off.getNumbOfPoints()) + " ";
								out.write(line);
								out.newLine();

								//3-rd line - x-array (phase values)
								line = " ";
								int nP = gd_bpm_B_on.getNumbOfPoints();
								for (int i = 0; i < nP; i++) {
									line = line + dbl_Format.format(gd_bpm_B_on.getX(i)) + " ";
								}
								line = line + " ";
								out.write(line);
								out.newLine();

								//4-th line B-bpm phases for On
								line = " ";
								for (int i = 0; i < nP; i++) {
									line = line + dbl_Format.format(gd_bpm_B_on.getY(i)) + " ";
								}
								line = line + " ";
								out.write(line);
								out.newLine();

								//5-th line C-bpm phases for On
								line = " ";
								for (int i = 0; i < nP; i++) {
									line = line + dbl_Format.format(gd_bpm_C_on.getY(i)) + " ";
								}
								line = line + " ";
								out.write(line);
								out.newLine();

								//6-th line B-bpm phases for Off
								nP = gd_bpm_B_off.getNumbOfPoints();
								line = " ";
								for (int i = 0; i < nP; i++) {
									line = line + dbl_Format.format(gd_bpm_B_off.getY(i)) + " ";
								}
								line = line + " ";
								out.write(line);
								out.newLine();

								//7-th line C-bpm phases for Off
								line = " ";
								for (int i = 0; i < nP; i++) {
									line = line + dbl_Format.format(gd_bpm_C_off.getY(i)) + " ";
								}
								line = line + " ";
								out.write(line);
								out.newLine();

								out.flush();
								out.close();

							} catch (IOException exp) {
								Toolkit.getDefaultToolkit().beep();
								System.out.println(exp.toString());
							}
						}
						messageTextLocal.setText(null);
					} else {
						Toolkit.getDefaultToolkit().beep();
						messageTextLocal.setText(null);
						messageTextLocal.setText("Cannot save data as an ASCII file for dT procedure." +
								" Clean and measure again!");
						Toolkit.getDefaultToolkit().beep();
					}
				}
			});

	}


	/**
	 *  Creates local analysis panel
	 */
	private void makeLocalAnalysisPanel() {

		setToAccelButton.setForeground(Color.red);

		rfPhase_Text.setDecimalFormat(format);
		rfAmp_Text.setDecimalFormat(format);
		inputEnergyDev_Text.setDecimalFormat(format);
		currentAmp_Text.setDecimalFormat(format);
		expSlope_Text.setDecimalFormat(format);
		energyStep_Text.setDecimalFormat(format);

		rfPhase_Text.setHorizontalAlignment(JTextField.CENTER);
		rfAmp_Text.setHorizontalAlignment(JTextField.CENTER);
		inputEnergyDev_Text.setHorizontalAlignment(JTextField.CENTER);
		currentAmp_Text.setHorizontalAlignment(JTextField.CENTER);
		expSlope_Text.setHorizontalAlignment(JTextField.CENTER);
		rfPhase_Text.setHorizontalAlignment(JTextField.CENTER);
		energyStep_Text.setHorizontalAlignment(JTextField.CENTER);

		rfPhase_Text.setEditable(false);
		rfAmp_Text.setEditable(false);
		inputEnergyDev_Text.setEditable(false);
		currentAmp_Text.setEditable(false);
		expSlope_Text.setEditable(false);
		rfPhase_Text.setEditable(false);
		energyStep_Text.setEditable(false);

		rfPhase_Text.setBackground(Color.white);
		rfAmp_Text.setBackground(Color.white);
		inputEnergyDev_Text.setBackground(Color.white);
		currentAmp_Text.setBackground(Color.white);
		expSlope_Text.setBackground(Color.white);
		rfPhase_Text.setBackground(Color.white);
		energyStep_Text.setBackground(Color.white);

		dphi12graphPanel.setAxisNameX("Delta Phi BPM #1, deg");
		ampDevGraphPanel.setAxisNameX("Slope of Phase Scan Line, deg");
		dphi12graphPanel.setAxisNameY("Delta Phi BPM #2, deg");
		ampDevGraphPanel.setAxisNameY("RF Ampl. Deviation, %");
		dphi12graphPanel.setOffScreenImageDrawing(true);
		ampDevGraphPanel.setOffScreenImageDrawing(true);

		ampDevGraphPanel.setGraphBackGroundColor(Color.BLACK);
		ampDevGraphPanel.setGridLineColor(Color.gray);

		dphi12graphPanel.setGraphBackGroundColor(Color.white);
		dphi12graphPanel.setGridLineColor(Color.gray);

		dphi12graphPanel.setLegendButtonVisible(true);
		ampDevGraphPanel.setLegendButtonVisible(true);

		//compose panels
		JPanel tmp_name = new JPanel(new GridLayout(3, 1));

		tmp_name.add(moduleNameLabel);
		tmp_name.add(bpm1_NameLabel);
		tmp_name.add(bpm2_NameLabel);
		tmp_name.setBorder(etchedBorder);

		JPanel tmp_0 = new JPanel(new GridLayout(2, 1));
		tmp_0.add(rfPhase_Text);
		tmp_0.add(rfAmp_Text);

		JPanel tmp_1 = new JPanel(new GridLayout(2, 1));
		tmp_1.add(rfPhaseLabel);
		tmp_1.add(rfAmpLabel);

		JPanel tmp_2 = new JPanel(new FlowLayout(FlowLayout.CENTER));
		tmp_2.add(setToAccelButton);

		JPanel tmp_3 = new JPanel(new BorderLayout());
		tmp_3.add(tmp_0, BorderLayout.WEST);
		tmp_3.add(tmp_1, BorderLayout.CENTER);
		tmp_3.add(tmp_2, BorderLayout.SOUTH);

		JPanel tmp_res = new JPanel(new BorderLayout());
		tmp_res.add(tmp_3, BorderLayout.WEST);
		tmp_res.setBorder(etchedBorder);

		JPanel tmp_top = new JPanel(new BorderLayout());
		tmp_top.add(tmp_name, BorderLayout.WEST);
		tmp_top.add(tmp_res, BorderLayout.CENTER);

		JPanel tmp_10 = new JPanel(new GridLayout(4, 1));
		tmp_10.add(inputEnergyDevLabel);
		tmp_10.add(currentAmpLabel);
		tmp_10.add(expSlopeLabel);
		tmp_10.add(energyStepLabel);

		JPanel tmp_11 = new JPanel(new GridLayout(4, 1));
		tmp_11.add(inputEnergyDev_Text);
		tmp_11.add(currentAmp_Text);
		tmp_11.add(expSlope_Text);
		tmp_11.add(energyStep_Text);

		JPanel tmp_left_top = new JPanel(new BorderLayout());
		tmp_left_top.setBorder(etchedBorder);
		tmp_left_top.add(aMatrixSwitchButton, BorderLayout.NORTH);
		tmp_left_top.add(tmp_10, BorderLayout.CENTER);
		tmp_left_top.add(tmp_11, BorderLayout.WEST);

		JPanel tmp_20 = new JPanel(new BorderLayout());
		tmp_20.setBorder(etchedBorder);
		tmp_20.add(ampDevGraphPanel, BorderLayout.CENTER);

		JPanel tmp_left = new JPanel(new BorderLayout());
		tmp_left.add(tmp_left_top, BorderLayout.NORTH);
		tmp_left.add(tmp_20, BorderLayout.CENTER);

		JPanel tmp_30 = new JPanel(new BorderLayout());
		tmp_30.setBorder(etchedBorder);
		tmp_30.add(dphi12graphPanel, BorderLayout.CENTER);

		JPanel tmp_center = new JPanel(new BorderLayout());
		tmp_center.add(tmp_30, BorderLayout.CENTER);
		tmp_center.add(tmp_left, BorderLayout.EAST);

		localAnalysisPanel.setLayout(new BorderLayout());

		localAnalysisPanel.add(tmp_top, BorderLayout.NORTH);
		localAnalysisPanel.add(tmp_center, BorderLayout.CENTER);

	}



	/**
	 *  Reads theory data from data file
	 *
	 *@param  fileName_TheoryData_In  Description of the Parameter
	 */
	private void readTheoryData(String fileName_TheoryData_In) {
		theoryData.clean();
		fileName_TheoryData = fileName_TheoryData_In;

		URL dataURL = Application.getAdaptor().getResourceURL( "data/delta_t/" + fileName_TheoryData );

		try {
			InputStream inps = dataURL.openStream();
			BufferedReader in = new BufferedReader(new InputStreamReader(inps));

			String lineIn = in.readLine();
			String[] dataS = null;

			while (lineIn != null) {
				if (!lineIn.startsWith("%")) {
					dataS = lineIn.split("\\s+");
					theoryData.setDeltaPhiIni(Double.parseDouble(dataS[1]),
							Double.parseDouble(dataS[2]));
					break;
				} else {
					lineIn = in.readLine();
				}
			}

			double[] a = new double[6];
			lineIn = in.readLine();
			while (lineIn != null) {
				dataS = lineIn.split("\\s+");
				if (dataS.length > 0 &&
						!lineIn.startsWith("%") &&
						dataS.length == 7) {
					for (int i = 0; i < 6; i++) {
						a[i] = Double.parseDouble(dataS[i + 1]);
					}
					theoryData.addData(a);
				}
				lineIn = in.readLine();
			}
			in.close();

		} catch (IOException exception) {
			Toolkit.getDefaultToolkit().beep();
			messageTextLocal.setText(null);
			messageTextLocal.setText("Fatal error. Can not read file =" +
					fileName_TheoryData +
					". Stop execution. Call the developer.");
		}

	}


	/**
	 *  Performs analysis
	 */
	private void makeDeltaTimeAnalysis() {

		if (!canSaveASDTtable()) {
			Toolkit.getDefaultToolkit().beep();
			messageTextLocal.setText(null);
			messageTextLocal.setText("Insufficient or wrong data." +
					" Clean and measure again!");
			clearResultsOfAnalysis();
			return;
		}

		//set all graph data as non-active in updating graphs panel
		//It will speed up the method
		//at the end of this method this property should be restored
		bpm1_ex_gd.setImmediateContainerUpdate(false);
		bpm1_interp_gd.setImmediateContainerUpdate(false);
		bpm2_ex_gd.setImmediateContainerUpdate(false);
		bpm2_interp_gd.setImmediateContainerUpdate(false);

		bpm12_ex_gd.setImmediateContainerUpdate(false);
		bpm12_interp_gd.setImmediateContainerUpdate(false);
		energy_line_gd.setImmediateContainerUpdate(false);
		energy_Pos_gd.setImmediateContainerUpdate(false);
		energy_Neg_gd.setImmediateContainerUpdate(false);

		boolean analysis_exists = false;
		if (bpm12_ex_gd.getNumbOfPoints() > 1) {
			analysis_exists = true;
		}

		double y_1 = 0.;
		double y_2 = 0.;
		double x_1 = 0.;
		double x_2 = 0.;

		//interpolation coefficients of experimental data for bpm1 and bpm2
		double[][] coeff_1 = null;
		double[][] coeff_2 = null;

		if (!analysis_exists) {
			//we have to calculate experimental data first
			bpm1_ex_gd.removeAllPoints();
			bpm2_ex_gd.removeAllPoints();
			bpm12_ex_gd.removeAllPoints();

			//calculate average phase for "off" state
			double phi_1_avg_off = 0.;
			double phi_2_avg_off = 0.;
			for (int i = 0; i < gd_bpm_B_off.getNumbOfPoints(); i++) {
				phi_1_avg_off += gd_bpm_B_off.getY(i) / gd_bpm_B_off.getNumbOfPoints();
				phi_2_avg_off += gd_bpm_C_off.getY(i) / gd_bpm_C_off.getNumbOfPoints();
			}

			for (int i = 0; i < gd_bpm_B_on.getNumbOfPoints(); i++) {
				y_1 = -(gd_bpm_B_on.getY(i) - phi_1_avg_off) - theoryData.getDeltaPhiIni1();
				y_2 = -(gd_bpm_C_on.getY(i) - phi_2_avg_off) - theoryData.getDeltaPhiIni2();

				y_1 += 180.;
				while (y_1 < 0.) {
					y_1 += 360.;
				}
				y_1 = y_1 % 360.;
				y_1 -= 180.;

				y_2 += 180.;
				while (y_2 < 0.) {
					y_2 += 360.;
				}
				y_2 = y_2 % 360.;
				y_2 -= 180.;

				bpm1_ex_gd.addPoint(gd_bpm_B_on.getX(i), y_1, gd_bpm_B_on.getErr(i));
				bpm2_ex_gd.addPoint(gd_bpm_C_on.getX(i), y_2, gd_bpm_C_on.getErr(i));
			}

            GraphDataOperations.unwrapData(bpm1_ex_gd);
            GraphDataOperations.unwrapData(bpm2_ex_gd);
            for (int i = 0; i < bpm1_ex_gd.getNumbOfPoints(); i++) {
              bpm12_ex_gd.addPoint(bpm1_ex_gd.getY(i), bpm2_ex_gd.getY(i));
            }


			//define interpolation from the beginning
			coeff_1 = GraphDataOperations.polynomialFit(bpm1_ex_gd,
					bpm1_ex_gd.getMinX(),
					bpm1_ex_gd.getMaxX(),
					1);

			coeff_2 = GraphDataOperations.polynomialFit(bpm2_ex_gd,
					bpm2_ex_gd.getMinX(),
					bpm2_ex_gd.getMaxX(),
					1);

		} else {
			//experimental data already exist, but we need to
			//recalculate coefficients within the new limits
			coeff_1 = GraphDataOperations.polynomialFit(bpm1_ex_gd,
					graphLeftPanel_1.getCurrentMinX(),
					graphLeftPanel_1.getCurrentMaxX(),
					1);

			coeff_2 = GraphDataOperations.polynomialFit(bpm2_ex_gd,
					graphLeftPanel_2.getCurrentMinX(),
					graphLeftPanel_2.getCurrentMaxX(),
					1);
		}

		if (coeff_1 == null || coeff_2 == null) {
			Toolkit.getDefaultToolkit().beep();
			messageTextLocal.setText(null);
			messageTextLocal.setText("Insufficient or wrong data." +
					" Clean and measure again!");
			clearResultsOfAnalysis();
			return;
		}

		//set all coefficients
		double coeff_1_k = coeff_1[0][1];
		double coeff_1_b = coeff_1[0][0];

		double coeff_2_k = coeff_2[0][1];
		double coeff_2_b = coeff_2[0][0];

		double coeff_k = coeff_2_k / coeff_1_k;
		double coeff_b = (coeff_2_b * coeff_1_k - coeff_1_b * coeff_2_k) / coeff_1_k;

		double[] coeff = new double[2];
		coeff[0] = coeff_b;
		coeff[1] = coeff_k;

		//define linear approximations
		bpm1_interp_gd.removeAllPoints();
		bpm2_interp_gd.removeAllPoints();
		bpm12_interp_gd.removeAllPoints();
		int nApp = 10;
		for (int i = 0; i < nApp; i++) {
			//bpm1 data approximation
			x_1 = bpm1_ex_gd.getMinX() + i * (bpm1_ex_gd.getMaxX() - bpm1_ex_gd.getMinX()) / (nApp - 1);
			y_1 = GraphDataOperations.polynom(x_1, coeff_1[0]);
			bpm1_interp_gd.addPoint(x_1, y_1);

			//bpm1 data approximation
			x_1 = bpm2_ex_gd.getMinX() + i * (bpm2_ex_gd.getMaxX() - bpm2_ex_gd.getMinX()) / (nApp - 1);
			y_1 = GraphDataOperations.polynom(x_1, coeff_2[0]);
			bpm2_interp_gd.addPoint(x_1, y_1);

			//bpm1-2 data approximation
			x_1 = bpm12_ex_gd.getMinX() + i * (bpm12_ex_gd.getMaxX() - bpm12_ex_gd.getMinX()) / (nApp - 1);
			y_1 = GraphDataOperations.polynom(x_1, coeff);
			bpm12_interp_gd.addPoint(x_1, y_1);
		}

		//ampl_dev in [%] of deviation from nominal
		double slope = Math.atan(coeff_k) * 180. / Math.PI;
		double amp_dev = ampDev_vs_slope_th_gd.getValueY(slope);
		ampDev_vs_slope_ex_gd.removeAllPoints();
		ampDev_vs_slope_ex_gd.addPoint(slope, amp_dev);

		//real amplitude - what we have at this moment
		double ampl_cuurent = ((Double) gd_bpm_B_on.getGraphProperty("PARAMETER_VALUE")).doubleValue();
		double ampl_recommended = ampl_cuurent / (1.0 + 0.01 * amp_dev);
		currentAmp_Text.setValue(ampl_cuurent);
		expSlope_Text.setValue(slope);
		rfAmp_Text.setValue(ampl_recommended);

		//make energy graphs
		double amp_dev_tmp = 1.0;
		if (!aMatrixSwitchButton.isSelected()) {
			amp_dev_tmp = amp_dev * 0.01 + 1.0;
		}

		//data a21 and a22 in eV/rad -> eV/grad
		double a11 = theoryData.getA(1, 1, amp_dev_tmp);
		double a12 = theoryData.getA(1, 2, amp_dev_tmp);
		double a21 = theoryData.getA(2, 1, amp_dev_tmp) * Math.PI / 180.;
		double a22 = theoryData.getA(2, 2, amp_dev_tmp) * Math.PI / 180.;

		x_1 = bpm12_ex_gd.getMinX();
		x_2 = bpm12_ex_gd.getMaxX();
		y_1 = -(a11 / a12) * x_1;
		y_2 = -(a11 / a12) * x_2;
		energy_line_gd.removeAllPoints();
		energy_line_gd.addPoint(x_1, y_1);
		energy_line_gd.addPoint(x_2, y_2);

		double energy_min = Math.min(a21 * x_1 + a22 * y_1, a21 * x_2 + a22 * y_2);
		double energy_max = Math.max(a21 * x_1 + a22 * y_1, a21 * x_2 + a22 * y_2);

		//step from [keV] to [eV]
		double energy_step = energyStep_Text.getValue() * 1000.;

		int n_ep_min = ((int) (energy_min / energy_step)) - 1;
		int n_ep_max = ((int) (energy_max / energy_step)) + 1;

		double enrg = 0.;
		energy_Pos_gd.removeAllPoints();
		energy_Neg_gd.removeAllPoints();
        if(Math.abs( n_ep_max - n_ep_min) < 100){
          for (int i = n_ep_min; i <= n_ep_max; i++) {
			enrg = i * energy_step;
			if (i != 0) {
              x_1 = enrg / (a21 - a22 * a11 / a12);
              y_1 = -(a11 / a12) * x_1;
              if (y_1 <= energy_line_gd.getMaxY() &&
              y_1 >= energy_line_gd.getMinY()) {
                if (enrg > 0.) {
                  energy_Pos_gd.addPoint(x_1, y_1);
                } else {
                  energy_Neg_gd.addPoint(x_1, y_1);
                }
              }
			}
          }
        }


		//calculate recommendet phase for the RF
		double phase_recom = -(coeff_1_b * a11 + coeff_2_b * a12) /
				(coeff_1_k * a11 + coeff_2_k * a12);

		double phase_shift = MainAnalysisController.getPhaseShift(gd_bpm_B_on);
		phase_recom -= phase_shift;

		phase_recom += 180.;
		while (phase_recom < 0.) {
			phase_recom += 360.;
		}
		phase_recom = phase_recom % 360.;
		phase_recom -= 180.;
		rfPhase_Text.setValue(phase_recom);

		//calculation of the energy
		double energy_delta = ((coeff_2_b * coeff_1_k - coeff_1_b * coeff_2_k) /
				(coeff_1_k * a11 + coeff_2_k * a12)) *
				(a11 * a22 - a12 * a21);
		energy_delta = 0.001 * energy_delta;
		inputEnergyDev_Text.setValue(energy_delta);

		//update graphs
		bpm1_ex_gd.setImmediateContainerUpdate(true);
		bpm2_ex_gd.setImmediateContainerUpdate(true);

		bpm12_ex_gd.setImmediateContainerUpdate(true);

		dphi12graphPanel.clearZoomStack();
		ampDevGraphPanel.clearZoomStack();

		graphLeftPanel_1.clearZoomStack();
		graphLeftPanel_2.clearZoomStack();

		analysisDone = true;
	}


	/**
	 *  Sets the amplitudeAndPhaseToEPICS attribute of the AnalysisCntrlTDProcedure
	 *  object
	 */
	private void setAmplitudeAndPhaseToEPICS() {
		if (analysisDone) {
			double ampVal = rfAmp_Text.getValue();
			double phaseVal = rfPhase_Text.getValue();
			if (scanVariableParameter.getChannel() != null &&
					scanVariable.getChannel() != null) {
				scanVariableParameter.setValue(ampVal);
				scanVariable.setValue(phaseVal);
			} else {
				messageTextLocal.setText(null);
				messageTextLocal.setText("The parameter PV channel does not exist.");
				Toolkit.getDefaultToolkit().beep();
			}

		} else {
			Toolkit.getDefaultToolkit().beep();
			messageTextLocal.setText(null);
			messageTextLocal.setText("Perform analysis first!");
			clearResultsOfAnalysis();
		}
	}


	/**
	 *  The data container for theoretical data for delta-t procedure
	 *
	 *@author    shishlo
	 */
	class DeltaTdata {

		private CubicSplineGraphData slopeData = new CubicSplineGraphData();
		private CubicSplineGraphData a11Data = new CubicSplineGraphData();
		private CubicSplineGraphData a12Data = new CubicSplineGraphData();
		private CubicSplineGraphData a21Data = new CubicSplineGraphData();
		private CubicSplineGraphData a22Data = new CubicSplineGraphData();

		private double delta_phi_ini_bpm_1 = 0.0;
		private double delta_phi_ini_bpm_2 = 0.0;


		/**
		 *  Constructor for the DeltaTdata object
		 */
		DeltaTdata() { }


		/**
		 *  Sets the phases for default amplitude
		 *
		 *@param  delta_phi_ini_bpm_1  The new phi value for bmp1
		 *@param  delta_phi_ini_bpm_2  The new phi value for bpm2
		 */
		void setDeltaPhiIni(double delta_phi_ini_bpm_1, double delta_phi_ini_bpm_2) {
			this.delta_phi_ini_bpm_1 = delta_phi_ini_bpm_1;
			this.delta_phi_ini_bpm_2 = delta_phi_ini_bpm_2;
		}


		/**
		 *  Adds slope and matrix elements of A to the DeltaTdata object
		 *
		 *@param  a  The data array [amplitude,slope,a11,12,a21,a22]
		 */
		void addData(double[] a) {
			if (a.length == 6) {
				slopeData.addPoint(a[0], a[1]);
				a11Data.addPoint(a[0], a[2]);
				a12Data.addPoint(a[0], a[3]);
				a21Data.addPoint(a[0], a[4]);
				a22Data.addPoint(a[0], a[5]);
			}
		}


		/**
		 *  Gets the deltaPhiIni1 attribute of the DeltaTdata object
		 *
		 *@return    The deltaPhiIni1 value
		 */
		double getDeltaPhiIni1() {
			return delta_phi_ini_bpm_1;
		}


		/**
		 *  Gets the deltaPhiIni2 attribute of the DeltaTdata object
		 *
		 *@return    The deltaPhiIni2 value
		 */
		double getDeltaPhiIni2() {
			return delta_phi_ini_bpm_2;
		}


		/**
		 *  Gets the numbPoints attribute of the DeltaTdata object
		 *
		 *@return    The numbPoints value
		 */
		int getNumbPoints() {
			return slopeData.getNumbOfPoints();
		}


		/**
		 *  Gets the amplitude attribute of the DeltaTdata object
		 *
		 *@param  index  Description of the Parameter
		 *@return        The amplitude value
		 */
		double getAmplitude(int index) {
			return slopeData.getX(index);
		}


		/**
		 *  Gets the slope attribute of the DeltaTdata object
		 *
		 *@param  index  Description of the Parameter
		 *@return        The slope value
		 */
		double getSlope(int index) {
			return slopeData.getY(index);
		}


		/**
		 *  Gets the slope attribute of the DeltaTdata object
		 *
		 *@param  amp  Description of the Parameter
		 *@return      The slope value
		 */
		double getSlope(double amp) {
			return slopeData.getValueY(amp);
		}


		/**
		 *  Gets the a attribute of the DeltaTdata object
		 *
		 *@param  i      Description of the Parameter
		 *@param  j      Description of the Parameter
		 *@param  index  Description of the Parameter
		 *@return        The a value
		 */
		double getA(int i, int j, int index) {
			if (i == 1 && j == 1) {
				return a11Data.getY(index);
			}
			if (i == 1 && j == 2) {
				return a12Data.getY(index);
			}
			if (i == 2 && j == 1) {
				return a21Data.getY(index);
			}
			if (i == 2 && j == 2) {
				return a22Data.getY(index);
			}
			return 0.;
		}


		/**
		 *  Gets the a attribute of the DeltaTdata object
		 *
		 *@param  i    Description of the Parameter
		 *@param  j    Description of the Parameter
		 *@param  amp  Description of the Parameter
		 *@return      The a value
		 */
		double getA(int i, int j, double amp) {
			if (i == 1 && j == 1) {
				return a11Data.getValueY(amp);
			}
			if (i == 1 && j == 2) {
				return a12Data.getValueY(amp);
			}
			if (i == 2 && j == 1) {
				return a21Data.getValueY(amp);
			}
			if (i == 2 && j == 2) {
				return a22Data.getValueY(amp);
			}
			return 0.;
		}


		/**
		 *  Remove all data
		 */
		void clean() {
			slopeData.removeAllPoints();
			a11Data.removeAllPoints();
			a12Data.removeAllPoints();
			a21Data.removeAllPoints();
			a22Data.removeAllPoints();

			delta_phi_ini_bpm_1 = 0.0;
			delta_phi_ini_bpm_2 = 0.0;
		}

	}

}

