package xal.app.emittanceanalysis.analysis;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.text.*;
import javax.swing.border.*;
import java.awt.event.*;

import xal.extension.widgets.plot.*;
import xal.extension.widgets.swing.*;
import xal.app.emittanceanalysis.phasespaceanalysis.*;
import xal.tools.text.*;

/**
 *  This analysis should be done after the threshold analysis, because it uses
 *  alpha and beta parameters to plot the fraction of the beam bounded by
 *  certain emittance value as a function of this emittance value as independent
 *  variable
 *
 *@author     shishlo
 *@version    1.0
 */
class AnalysisFindEmitByFit extends AnalysisBasic {

	//emittance data as ColorSurfaceData instance (for analysis only)
	private ColorSurfaceData emittance3Da = null;

	//threshold text field from common part of the left top corner panel
	private DoubleInputTextField threshold_Text = null;

	//double values format
	private NumberFormat dbl_Format = new ScientificNumberFormat(5);

	//bottom panel. It includes the graph panel (bottom left)
	//and the controll panel (bottom right)
	JPanel bottomPanel = null;
	JPanel graphPanel = new JPanel(new BorderLayout());
	JPanel controllPanel = new JPanel(new BorderLayout());

	private FunctionGraphsJPanel GP = new FunctionGraphsJPanel();

	private EmittanceEquation emittanceEquation = new EmittanceEquation();

	private PhasePlaneEllipse phasePlaneEllipse = new PhasePlaneEllipse();

	private BasicGraphData gdFracFitted = new BasicGraphData();

	private BasicGraphData gdFrac = new BasicGraphData();
	private BasicGraphData gdAlpha = new BasicGraphData();
	private BasicGraphData gdBeta = new BasicGraphData();
	private BasicGraphData gdGamma = new BasicGraphData();
	private BasicGraphData[] gdArr = new BasicGraphData[4];

	private int dataIndex = 0;

	private Color[] colorArr = {
			Color.black,
			Color.blue,
			Color.cyan,
			Color.magenta};

	private String[] graphNames = {
			"Fraction of the beam",
			"Alpha parameter [a.u.]",
			"Beta parameter [mm mrad]",
			"Gamma parameter [mrad/mm]"};

	private String[] xAxisNames = {
			"ln(1/(1-f)), f - fraction",
			"[%] of rms emit.",
			"[%] of rms emit.",
			"[%] of rms emit."};

	private String[] yAxisNames = {
			"[%] of rms emittance",
			"alpha [ ]",
			"beta [mm mrad]",
			"gamma [mrad/mm]"};

	//----------------------------------
	//GUI elements of the controll panel
	//----------------------------------

	//radio-buttons panel
	private JPanel buttonPanel = new JPanel(new GridLayout(4, 1));

	private JRadioButton frac_Button = new JRadioButton(" fraction ", true);
	private JRadioButton alpha_Button = new JRadioButton(" alpha ", false);
	private JRadioButton beta_Button = new JRadioButton(" beta ", false);
	private JRadioButton gamma_Button = new JRadioButton(" gamma ", false);
	private ButtonGroup buttonGroup = new ButtonGroup();
	private JRadioButton[] buttonArr = new JRadioButton[4];

	//GUI elements of left bottom control panel
	private TitledBorder localEmtParamBborder = null;

	private DoubleInputTextField alphaLocal_Text = new DoubleInputTextField(11);
	private DoubleInputTextField betaLocal_Text = new DoubleInputTextField(11);
	private DoubleInputTextField emtLocal_Text = new DoubleInputTextField(11);

	private JLabel alphaLocal_Label = new JLabel("Alpha", JLabel.CENTER);
	private JLabel betaLocal_Label = new JLabel("Beta", JLabel.CENTER);
	private JLabel emtLocal_Label = new JLabel("Emittance", JLabel.CENTER);

	private JButton copyEmtFromRMS_Button = new JButton("COPY from RMS");
	private JButton copyEmtFromGAU_Button = new JButton("COPY from GAU");

	//threshold scan parameters
	private JPanel thresholdScanPanel =
			new JPanel(new BorderLayout());

	private JLabel emtCalculation_Label =
			new JLabel("=== EMITTANCE SCAN [%] of  Init. RMS emit. ===", JLabel.CENTER);

	private JLabel emtStart_Label = new JLabel("Start", JLabel.CENTER);
	private JLabel emtStep_Label = new JLabel("Step", JLabel.CENTER);
	private JLabel emtStop_Label = new JLabel("Stop", JLabel.CENTER);

	private JSpinner emtStart_Spinner =
			new JSpinner(new SpinnerNumberModel(5, 1, 600, 1));
	private JSpinner emtStep_Spinner =
			new JSpinner(new SpinnerNumberModel(5, 1, 25, 1));
	private JSpinner emtStop_Spinner =
			new JSpinner(new SpinnerNumberModel(300, 5, 2400, 5));

	//calculation buttons
	private JButton plotGraphs_Button = new JButton("PLOT GRAPHS");
	private JButton calcEmitButton =
			new JButton("FIT & SET FITTED EMITTANCE");

	//text fields borrowed from common panel
	private DoubleInputTextField alphaRMS_Text = null;
	private DoubleInputTextField betaRMS_Text = null;
	private DoubleInputTextField emtRMS_Text = null;
	private boolean is_ready_rms = false;

	private DoubleInputTextField alphaFIT_Text = null;
	private DoubleInputTextField betaFIT_Text = null;
	private DoubleInputTextField emtFIT_Text = null;
	private boolean is_ready_fit = false;

	private DoubleInputTextField alphaGAU_Text = null;
	private DoubleInputTextField betaGAU_Text = null;
	private DoubleInputTextField emtGAU_Text = null;
	private boolean is_ready_gau = false;

	//initialization listener - it is called by
	//others analyses when necessary
	private ActionListener init_listener_fit = null;



	/**
	 *  Constructor for the AnalysisFindEmitByFit object
	 *
	 *@param  crossParamMap         The HashMap with Parameters of the analyses
	 *@param  analysisTypeIndex_In  The type index of the analysis
	 */
	AnalysisFindEmitByFit(int analysisTypeIndex_In, HashMap<String,Object> crossParamMap) {
		super(analysisTypeIndex_In, crossParamMap);

		colorArr[2] = new Color(128, 128, 255);

		analysisDescriptionString =
				" FITTING ANALYSIS" +
				System.getProperties().getProperty("line.separator").toString() +
				"This analysis plots the fraction " +
				"of the beam, alpha, beta, and gamma as a function of emittance " +
				"(defined as area/PI of phase space) to find emittance of the " +
				"measured beam phase space distribution." +
				System.getProperties().getProperty("line.separator").toString() +
				"A white ellipse on the color surface plot is the ellipse " +
				"with area/PI = 3*(Init. emittance).";

		//graph panel properties
		GP.setLegendVisible(true);
		GP.setLegendButtonVisible(true);
		GP.setOffScreenImageDrawing(true);
		GP.setLegendKeyString("Legend");
		GP.setGraphBackGroundColor(Color.white);
		GP.removeAllGraphData();

		//buttons look and feel
		plotGraphs_Button.setForeground(Color.blue.darker());
		//plotGraphs_Button.setBackground( Color.cyan );

		//set panels layout
		Border etchedBorder = BorderFactory.createEtchedBorder();

		bottomPanel = getBottomPanel();
		bottomPanel.setLayout(new BorderLayout());
		bottomPanel.add(graphPanel, BorderLayout.CENTER);

		JPanel rightBottomPanel = new JPanel(new BorderLayout());
		rightBottomPanel.add(controllPanel, BorderLayout.NORTH);

		bottomPanel.add(rightBottomPanel, BorderLayout.EAST);

		graphPanel.setBorder(etchedBorder);
		controllPanel.setBorder(etchedBorder);
		buttonPanel.setBorder(etchedBorder);

		graphPanel.add(GP, BorderLayout.CENTER);

		JPanel calcEmiButtonPanel =
				new JPanel(new FlowLayout(FlowLayout.CENTER, 3, 3));
		calcEmiButtonPanel.add(calcEmitButton);

		calcEmitButton.setForeground(Color.blue.darker());
		//calcEmitButton.setBackground( Color.cyan );

		controllPanel.add(buttonPanel, BorderLayout.WEST);
		controllPanel.add(thresholdScanPanel, BorderLayout.CENTER);
		controllPanel.add(calcEmiButtonPanel, BorderLayout.SOUTH);

		//set elements of the radio-buttons panel
		buttonPanel.add(frac_Button);
		buttonPanel.add(alpha_Button);
		buttonPanel.add(beta_Button);
		buttonPanel.add(gamma_Button);

		buttonGroup.add(frac_Button);
		buttonGroup.add(alpha_Button);
		buttonGroup.add(beta_Button);
		buttonGroup.add(gamma_Button);

		//threshold scan control parameter panel
		JPanel emtScanSubPanel_0 = new JPanel(new GridLayout(2, 3, 1, 1));
		emtScanSubPanel_0.setBorder(etchedBorder);
		emtScanSubPanel_0.add(emtStart_Label);
		emtScanSubPanel_0.add(emtStep_Label);
		emtScanSubPanel_0.add(emtStop_Label);
		emtScanSubPanel_0.add(emtStart_Spinner);
		emtScanSubPanel_0.add(emtStep_Spinner);
		emtScanSubPanel_0.add(emtStop_Spinner);

		JPanel emtScanSubPanel_1 = new JPanel();
		emtScanSubPanel_1.setLayout(
				new FlowLayout(FlowLayout.CENTER, 1, 1));
		emtScanSubPanel_1.add(plotGraphs_Button);

		JPanel emtScanSubPanel_2 = new JPanel(new BorderLayout());
		emtScanSubPanel_2.setBorder(etchedBorder);
		emtScanSubPanel_2.add(emtCalculation_Label, BorderLayout.NORTH);
		emtScanSubPanel_2.add(emtScanSubPanel_0, BorderLayout.CENTER);
		emtScanSubPanel_2.add(emtScanSubPanel_1, BorderLayout.SOUTH);

		//initial fitting parameters panel
		JPanel emtParamPanel =
				new JPanel(new BorderLayout());

		localEmtParamBborder =
				BorderFactory.createTitledBorder(etchedBorder, "Initial Emittance parameters");
		emtParamPanel.setBorder(localEmtParamBborder);

		JPanel emtParamPanel_0 = new JPanel(new GridLayout(1, 2, 10, 1));
		emtParamPanel_0.add(copyEmtFromRMS_Button);
		emtParamPanel_0.add(copyEmtFromGAU_Button);

		JPanel emtParamPanel_1 = new JPanel(new GridLayout(2, 3, 1, 1));
		emtParamPanel_1.setBorder(etchedBorder);
		emtParamPanel_1.add(alphaLocal_Label);
		emtParamPanel_1.add(betaLocal_Label);
		emtParamPanel_1.add(emtLocal_Label);
		emtParamPanel_1.add(alphaLocal_Text);
		emtParamPanel_1.add(betaLocal_Text);
		emtParamPanel_1.add(emtLocal_Text);

		emtParamPanel.add(emtParamPanel_0, BorderLayout.NORTH);
		emtParamPanel.add(emtParamPanel_1, BorderLayout.CENTER);

		thresholdScanPanel.add(emtParamPanel, BorderLayout.NORTH);
		thresholdScanPanel.add(emtScanSubPanel_2, BorderLayout.SOUTH);

		alphaLocal_Text.setNumberFormat(dbl_Format);
		betaLocal_Text.setNumberFormat(dbl_Format);
		emtLocal_Text.setNumberFormat(dbl_Format);

		//set init emittance param from RMS or GAU listeners
		copyEmtFromRMS_Button.addActionListener(
			new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					alphaLocal_Text.setValueQuietly(alphaRMS_Text.getValue());
					betaLocal_Text.setValueQuietly(betaRMS_Text.getValue());
					emtLocal_Text.setValueQuietly(emtRMS_Text.getValue());
					plotEllipse();
				}
			});

		copyEmtFromGAU_Button.addActionListener(
			new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					alphaLocal_Text.setValueQuietly(alphaGAU_Text.getValue());
					betaLocal_Text.setValueQuietly(betaGAU_Text.getValue());
					emtLocal_Text.setValueQuietly(emtGAU_Text.getValue());
					plotEllipse();
				}
			});


		gdArr[0] = gdFrac;
		gdArr[1] = gdAlpha;
		gdArr[2] = gdBeta;
		gdArr[3] = gdGamma;

		buttonArr[0] = frac_Button;
		buttonArr[1] = alpha_Button;
		buttonArr[2] = beta_Button;
		buttonArr[3] = gamma_Button;

		ActionListener radioButtonListener =
			new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					JRadioButton source = (JRadioButton) e.getSource();
					int ind = -1;
					for (int i = 0; i < buttonArr.length; i++) {
						if (source == buttonArr[i]) {
							ind = i;
						}
					}
					if (ind < 0) {
						return;
					}
					setDataIndex(ind);
				}
			};

		for (int i = 0; i < colorArr.length; i++) {
			gdArr[i].setGraphColor(colorArr[i]);
			gdArr[i].setDrawPointsOn(true);
			gdArr[i].removeAllPoints();
			gdArr[i].setGraphProperty(GP.getLegendKeyString(),
					buttonArr[i].getText());
			gdArr[i].setLineThick(2);
			buttonArr[i].setForeground(colorArr[i]);
			buttonArr[i].addActionListener(radioButtonListener);
		}

		gdFracFitted.setGraphColor(Color.red);
		gdFracFitted.setDrawPointsOn(false);
		gdFracFitted.removeAllPoints();
		gdFracFitted.setGraphProperty(GP.getLegendKeyString(),
				buttonArr[0].getText() + "fitting ");
		gdFracFitted.setLineThick(2);

		//default index of graph is 0
		GP.addGraphData(gdArr[dataIndex]);
		GP.setAxisNameX(xAxisNames[dataIndex]);
		GP.setAxisNameY(yAxisNames[dataIndex]);
		GP.setName(graphNames[dataIndex]);

		//graph calculations button action
		plotGraphs_Button.addActionListener(
			new ActionListener() {
				public void actionPerformed(ActionEvent e) {

					getTextMessage().setText(null);

					emtFIT_Text.setText(null);
					emtFIT_Text.setBackground(Color.white);

					is_ready_fit = false;
					getParamsHashMap().put("IS_READY_FIT", new Boolean(is_ready_fit));

					//disable all buttons during the plotting
					for (int i = 0; i < buttonArr.length; i++) {
						buttonArr[i].setEnabled(false);

					}

					//clear all old information
					for (int i = 0; i < gdArr.length; i++) {
						gdArr[i].removeAllPoints();
					}
					gdFracFitted.removeAllPoints();

					emtFIT_Text.setText(null);
					emtFIT_Text.setBackground(Color.white);

					GP.clearZoomStack();

					//plotting the graphs
					calculateGraphs();

					//activate all buttons
					for (int i = 0; i < buttonArr.length; i++) {
						buttonArr[i].setEnabled(true);
					}
				}
			});

		calcEmitButton.addActionListener(
			new ActionListener() {
				public void actionPerformed(ActionEvent e) {

					getTextMessage().setText(null);

					emtFIT_Text.setText(null);
					emtFIT_Text.setBackground(Color.white);

					is_ready_fit = false;
					getParamsHashMap().put("IS_READY_FIT", new Boolean(is_ready_fit));

					if (getDataIndex() != 0) {
						getTextMessage().setText(
								"You have to display the fraction(emittance)" +
								" graph and zoom region.");
						Toolkit.getDefaultToolkit().beep();
						return;
					}

					Double betaGamma_D = (Double) getParamsHashMap().get("GAMMA_BETA");
					double betaGamma = betaGamma_D.doubleValue();

					double emt_rms = emtLocal_Text.getValue() / betaGamma;

					if (emt_rms < 0.) {
						getTextMessage().setText(
								"The Init emittance < 0. There is nothing " +
								"I can do.");
						Toolkit.getDefaultToolkit().beep();
						return;
					}

					//place for emittance calculations
					double x_min = GP.getCurrentMinX();
					double x_max = GP.getCurrentMaxX();
					double y_min = GP.getCurrentMinY();
					double y_max = GP.getCurrentMaxY();

					double yx_avg = 0.;
					double x2_avg = 0.;

					double x = 0.;
					double y = 0.;

					int nPoints = gdFrac.getNumbOfPoints();

					for (int i = 0; i < nPoints; i++) {
						x = gdFrac.getX(i);
						y = gdFrac.getY(i);
						if (x >= x_min &&
								x <= x_max &&
								y >= y_min &&
								y <= y_max) {

							y *= emt_rms / 100.;
							yx_avg += x * y;
							x2_avg += x * x;
						}
					}

					if (yx_avg <= 0. || x2_avg <= 0.) {
						getTextMessage().setText(
								"Cannot get fit for emittance." +
								" Make right zoom region.");
						Toolkit.getDefaultToolkit().beep();
						return;
					}

					double emt_fit = 1. / (2. * (x2_avg / yx_avg));

					gdFracFitted.removeAllPoints();
					gdFracFitted.addPoint(0., 0.);
					gdFracFitted.addPoint(
							gdFrac.getX(nPoints - 1),
							gdFrac.getX(nPoints - 1) * 1. / (emt_rms / (2 * 100 * emt_fit)));

					GP.clearZoomStack();
					GP.removeGraphData(gdFracFitted);
					GP.addGraphData(gdFracFitted);

					emtFIT_Text.setValue(emt_fit * betaGamma);
				        alphaFIT_Text.setValue(alphaLocal_Text.getValue());
				        betaFIT_Text.setValue(betaLocal_Text.getValue());
					
					is_ready_fit = true;
					getParamsHashMap().put("IS_READY_FIT", new Boolean(is_ready_fit));

				}
			});

		//phase plane ellipse
		phasePlaneEllipse.getCurveData().setColor(Color.white);
		phasePlaneEllipse.getCurveData().setLineWidth(2);
		phasePlaneEllipse.getCurveData().clear();

		//initialization listener - it is called by
		//others analyses when necessary
		init_listener_fit =
			new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					initialize();
				}
			};
	}


	/**
	 *  Sets the new data index. See above for index meaning
	 *
	 *@param  dataIndex_new  The new new data index
	 */
	private void setDataIndex(int dataIndex_new) {
		int dataIndex_old = dataIndex;

		//set pressed button
		buttonArr[dataIndex_new].setSelected(true);

		//set graph panel decoration
		GP.removeAllGraphData();
		GP.addGraphData(gdArr[dataIndex_new]);
		GP.setAxisNameX(xAxisNames[dataIndex_new]);
		GP.setAxisNameY(yAxisNames[dataIndex_new]);
		GP.setName(graphNames[dataIndex_new]);

		if (dataIndex_new == 0) {
			GP.addGraphData(gdFracFitted);
		}

		GP.clearZoomStack();

	}


	/**
	 *  Gets the dataIndex attribute of the AnalysisFindEmitByFit object
	 *
	 *@return    The dataIndex value
	 */
	private int getDataIndex() {
		for (int i = 0; i < buttonArr.length; i++) {
			if (buttonArr[i].isSelected()) {
				return i;
			}
		}
		return -1;
	}



	/**
	 *  Performs actions before show the panel
	 */
	void goingShowUp() {
		is_ready_rms = ((Boolean) getParamsHashMap().get("IS_READY_RMS")).booleanValue();
		is_ready_gau = ((Boolean) getParamsHashMap().get("IS_READY_GAU")).booleanValue();
		is_ready_fit = ((Boolean) getParamsHashMap().get("IS_READY_FIT")).booleanValue();
		emittance3Da = (ColorSurfaceData) getParamsHashMap().get("RawEmittanceData");
		if (!is_ready_rms) {
			getTextMessage().setText(null);
			getTextMessage().setText("You have to perform Threshold Analysis" +
					" first and set RMS emittance, alpha, and beta.");
			Toolkit.getDefaultToolkit().beep();

			alphaLocal_Text.setText(null);
			betaLocal_Text.setText(null);
			emtLocal_Text.setText(null);

			alphaLocal_Text.setBackground(Color.white);
			betaLocal_Text.setBackground(Color.white);
			emtLocal_Text.setBackground(Color.white);

			copyEmtFromRMS_Button.setEnabled(false);
			copyEmtFromGAU_Button.setEnabled(false);

			plotGraphs_Button.setEnabled(false);
			calcEmitButton.setEnabled(false);

		} else {

			if (!is_ready_fit) {
				alphaLocal_Text.setValueQuietly(alphaRMS_Text.getValue());
				betaLocal_Text.setValueQuietly(betaRMS_Text.getValue());
				emtLocal_Text.setValueQuietly(emtRMS_Text.getValue());
			}

			if (is_ready_rms) {
				copyEmtFromRMS_Button.setEnabled(true);
			} else {
				plotGraphs_Button.setEnabled(false);
			}

			if (is_ready_gau) {
				copyEmtFromGAU_Button.setEnabled(true);
			} else {
				copyEmtFromGAU_Button.setEnabled(false);
			}

			plotGraphs_Button.setEnabled(true);
			calcEmitButton.setEnabled(true);

			plotEllipse();
		}
	}


	/**
	 *  Performs actions before close the panel
	 */
	void goingShowOff() {
		getParamsHashMap().put("IS_READY_FIT", new Boolean(is_ready_fit));
		FunctionGraphsJPanel GP_ep = (FunctionGraphsJPanel)
				getParamsHashMap().get("EMITTANCE_3D_PLOT");
		GP_ep.removeAllCurveData();
	}


	/**
	 *  Sets all analyzes in the initial state with removing all temporary data
	 */
	void initialize() {
		//set data index in the initial state
		setDataIndex(0);

		for (int i = 0; i < gdArr.length; i++) {
			gdArr[i].removeAllPoints();
		}

		gdFracFitted.removeAllPoints();

		GP.clearZoomStack();

		alphaFIT_Text.setText(null);
		betaFIT_Text.setText(null);
		emtFIT_Text.setText(null);

		alphaFIT_Text.setBackground(Color.white);
		betaFIT_Text.setBackground(Color.white);
		emtFIT_Text.setBackground(Color.white);

		alphaLocal_Text.setText(null);
		betaLocal_Text.setText(null);
		emtLocal_Text.setText(null);

		alphaLocal_Text.setBackground(Color.white);
		betaLocal_Text.setBackground(Color.white);
		emtLocal_Text.setBackground(Color.white);

		is_ready_fit = false;
		getParamsHashMap().put("IS_READY_FIT", new Boolean(is_ready_fit));
	}


	/**
	 *  Creates objects for the global HashMap using put method only
	 */
	void createHashMapObjects() {
		getParamsHashMap().put("INIT_LISTENER_FIT", init_listener_fit);
	}


	/**
	 *  Connects to the objects in the global HashMap using only get method of the
	 *  HashMap
	 */
	void connectToHashMapObjects() {

		threshold_Text = (DoubleInputTextField) getParamsHashMap().get("THRESHOLD_TEXT");

		alphaRMS_Text = (DoubleInputTextField) getParamsHashMap().get("ALPHA_RMS");
		betaRMS_Text = (DoubleInputTextField) getParamsHashMap().get("BETA_RMS");
		emtRMS_Text = (DoubleInputTextField) getParamsHashMap().get("EMT_RMS");

		alphaFIT_Text = (DoubleInputTextField) getParamsHashMap().get("ALPHA_FIT");
		betaFIT_Text = (DoubleInputTextField) getParamsHashMap().get("BETA_FIT");
		emtFIT_Text = (DoubleInputTextField) getParamsHashMap().get("EMT_FIT");

		alphaGAU_Text = (DoubleInputTextField) getParamsHashMap().get("ALPHA_GAU");
		betaGAU_Text = (DoubleInputTextField) getParamsHashMap().get("BETA_GAU");
		emtGAU_Text = (DoubleInputTextField) getParamsHashMap().get("EMT_GAU");

		alphaLocal_Text.setText(null);
		betaLocal_Text.setText(null);
		emtLocal_Text.setText(null);

		alphaLocal_Text.setBackground(Color.white);
		betaLocal_Text.setBackground(Color.white);
		emtLocal_Text.setBackground(Color.white);

		alphaLocal_Text.setHorizontalAlignment(JTextField.CENTER);
		betaLocal_Text.setHorizontalAlignment(JTextField.CENTER);
		emtLocal_Text.setHorizontalAlignment(JTextField.CENTER);

		is_ready_rms = ((Boolean) getParamsHashMap().get("IS_READY_RMS")).booleanValue();
		is_ready_fit = ((Boolean) getParamsHashMap().get("IS_READY_FIT")).booleanValue();
		is_ready_gau = ((Boolean) getParamsHashMap().get("IS_READY_GAU")).booleanValue();

		//local text listeners to draw the ellipse
		ActionListener localTextFieldListener =
			new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					plotEllipse();
				}
			};

		emtLocal_Text.addActionListener(localTextFieldListener);
		alphaLocal_Text.addActionListener(localTextFieldListener);
		betaLocal_Text.addActionListener(localTextFieldListener);
	}


	/**
	 *  Sets all fonts.
	 *
	 *@param  fnt  The new font
	 */
	void setFontForAll(Font fnt) {
		for (int i = 0; i < buttonArr.length; i++) {
			buttonArr[i].setFont(fnt);
		}

		alphaLocal_Text.setFont(fnt);
		betaLocal_Text.setFont(fnt);
		emtLocal_Text.setFont(fnt);
		alphaLocal_Label.setFont(fnt);
		betaLocal_Label.setFont(fnt);
		emtLocal_Label.setFont(fnt);
		copyEmtFromRMS_Button.setFont(fnt);
		copyEmtFromGAU_Button.setFont(fnt);

		localEmtParamBborder.setTitleFont(fnt);

		emtCalculation_Label.setFont(fnt);
		emtStart_Label.setFont(fnt);
		emtStep_Label.setFont(fnt);
		emtStop_Label.setFont(fnt);

		plotGraphs_Button.setFont(fnt);
		calcEmitButton.setFont(fnt);

		emtStart_Spinner.setFont(fnt);
		((JSpinner.DefaultEditor) emtStart_Spinner.getEditor()).getTextField().setFont(fnt);
		emtStep_Spinner.setFont(fnt);
		((JSpinner.DefaultEditor) emtStep_Spinner.getEditor()).getTextField().setFont(fnt);
		emtStop_Spinner.setFont(fnt);
		((JSpinner.DefaultEditor) emtStop_Spinner.getEditor()).getTextField().setFont(fnt);

	}


	/**
	 *  Calculates all graphs data - fraction, emittance, alpha, beta, gamma
	 */
	private void calculateGraphs() {

		int start = ((Integer) emtStart_Spinner.getValue()).intValue();
		int step = ((Integer) emtStep_Spinner.getValue()).intValue();
		int stop = ((Integer) emtStop_Spinner.getValue()).intValue();

		Double betaGamma_D = (Double) getParamsHashMap().get("GAMMA_BETA");
		if (betaGamma_D == null) {
			getTextMessage().setText(null);
			getTextMessage().setText("The emittance data are not ready.");
			Toolkit.getDefaultToolkit().beep();
			return;
		}

		double betaGamma = betaGamma_D.doubleValue();

		double emt_rms = emtLocal_Text.getValue() / betaGamma;

		if (emt_rms <= 0.) {
			getTextMessage().setText(null);
			getTextMessage().setText("The rms emittance is negative. " +
					"There is nothing I can do about it.");
			Toolkit.getDefaultToolkit().beep();
			return;
		}

		double alpha_rms = alphaLocal_Text.getValue();
		double beta_rms = betaLocal_Text.getValue();

		double thresh = threshold_Text.getValue();

		//avg_arr - average x and xp
		double[] avg_x_xp = EmtCalculations.getAvgXandXP(thresh, emittance3Da);

		if (avg_x_xp == null) {
			getTextMessage().setText(null);
			getTextMessage().setText("The threshold could be wrong.");
			Toolkit.getDefaultToolkit().beep();
			return;
		}

		//array elem. 0 - fraction [%]
		//array elem. 1 - alpha
		//array elem. 2 - beta
		//array elem. 3 - gamma
		double[] resArr = null;

		for (int i = start; i < stop; i += step) {

			double emt = emt_rms * i / 100.;
			emittanceEquation.setPrams(emt, alpha_rms, beta_rms);
			resArr = EmtCalculations.getFracEmtAlphaBetaGamma(
					thresh,
					avg_x_xp[0],
					avg_x_xp[1],
					emittanceEquation,
					emittance3Da);

			if (resArr != null) {
				resArr[0] /= 100.;
				if (resArr[0] > 0. && resArr[0] < 1.0) {
					resArr[0] = Math.log(1.0 / (1.0 - resArr[0]));
					gdArr[0].addPoint(resArr[0], (double) i);
				}
			}

			for (int j = 1; j < gdArr.length; j++) {
				gdArr[j].addPoint((double) i, resArr[j]);
			}
		}
	}


	/**
	 *  Plots the ellipse on the phasespace graph.
	 */
	private void plotEllipse() {
		FunctionGraphsJPanel GP_ep = (FunctionGraphsJPanel)
				getParamsHashMap().get("EMITTANCE_3D_PLOT");
		GP_ep.removeAllCurveData();
		if (emtLocal_Text.getValue() > 0.) {
			phasePlaneEllipse.setEmtAlphaBeta(
					3.0 * emtLocal_Text.getValue(),
					alphaLocal_Text.getValue(),
					betaLocal_Text.getValue());
			phasePlaneEllipse.calcCurvePoints();
			GP_ep.addCurveData(phasePlaneEllipse.getCurveData());
		}
	}

}

