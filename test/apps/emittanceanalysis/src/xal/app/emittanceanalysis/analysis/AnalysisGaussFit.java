package xal.app.emittanceanalysis.analysis;

import javax.swing.*;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.util.*;
import java.util.List;
import javax.swing.border.*;
import javax.swing.event.*;
import java.awt.event.*;

import xal.extension.widgets.plot.*;
import xal.extension.widgets.swing.*;
import xal.app.emittanceanalysis.phasespaceanalysis.*;
import xal.extension.solver.*;
import xal.extension.solver.hint.*;

/**
 *  This analysis finds parameters of a Gaussian phase space density by using
 *  the multi-parameter non-linear search
 *
 *@author     shishlo
 *@version    1.0
 */
class AnalysisGaussFit extends AnalysisBasic {

	//emittance data as ColorSurfaceData instance (for analysis only)
	private ColorSurfaceData emittance3Da = null;

	//threshold text field from common part of the left top corner panel
	private DoubleInputTextField threshold_Text = null;

	//bottom panel. It includes the graph panel (bottom left)
	//and the controll panel (bottom right)
	private JPanel bottomPanel = null;
	private JPanel graphPanel = new JPanel(new BorderLayout());
	private JPanel controllPanel = new JPanel(new BorderLayout());
	private JPanel leftControllPanel = new JPanel(new BorderLayout());
	private JPanel rightControllPanel = new JPanel(new BorderLayout());

	//usual graph with a cross-section graph
	private FunctionGraphsJPanel GP = new FunctionGraphsJPanel();

	//color surface graph with space phase data
	private FunctionGraphsJPanel GP_ep = null;

	//JRadioButtons for cross line types
	private JRadioButton cent_button = new JRadioButton();
	private JRadioButton vert_button = new JRadioButton();
	private JRadioButton horz_button = new JRadioButton();

	private ButtonGroup buttonGroup = new ButtonGroup();

	private JLabel cent_label = new JLabel("C", JLabel.CENTER);
	private JLabel vert_label = new JLabel("V", JLabel.CENTER);
	private JLabel horz_label = new JLabel("H", JLabel.CENTER);

	//sliders for cross line types
	private JScrollBar cent_slider = new JScrollBar(JScrollBar.VERTICAL, 0, 4, 0, 364);
	private JScrollBar vert_slider = new JScrollBar(JScrollBar.VERTICAL, 0, 4, 0, 200);
	private JScrollBar horz_slider = new JScrollBar(JScrollBar.VERTICAL, 0, 4, 0, 200);

	//GUI elements of left bottom control panel
	private TitledBorder localEmtParamBborder = null;

	private DoubleInputTextField alphaLocal_Text = new DoubleInputTextField(11);
	private DoubleInputTextField betaLocal_Text = new DoubleInputTextField(11);
	private DoubleInputTextField emtLocal_Text = new DoubleInputTextField(11);

	private JLabel alphaLocal_Label = new JLabel("Alpha", JLabel.CENTER);
	private JLabel betaLocal_Label = new JLabel("Beta", JLabel.CENTER);
	private JLabel emtLocal_Label = new JLabel("Emittance", JLabel.CENTER);

	private JLabel emtBounding_Label =
			new JLabel(" fitting region in [%]", JLabel.LEFT);
	private JSpinner emtBounding_Spinner =
			new JSpinner(new SpinnerNumberModel(300, 20, 10000, 20));

	private JLabel fitTime_Label =
			new JLabel(" fitting time [sec]", JLabel.LEFT);
	private JSpinner fitTime_Spinner =
			new JSpinner(new SpinnerNumberModel(10, 1, 60, 1));

	//calculation buttons
	private JButton fit_Button = new JButton("START GAUSS FITTING");

	//curve data for threshold and bounding rectangular
	private CurveData thresh_CurveData = new CurveData();
	private CurveData boundR_CurveData = new CurveData();
	private CurveData waveForm_CurveData = new CurveData();
	private CurveData section_CurveData = new CurveData();

	//curve data for ellipse on the color surface and
	private PhasePlaneEllipse phasePlaneEllipse = new PhasePlaneEllipse();

	//fitted gauss crossing
	private BasicGraphData gaussCrossingGR = new BasicGraphData();

	//boolean variable indicating that data are not empty
	private boolean isDataExist = false;

	//text fields borrowed from common panel
	private DoubleInputTextField alphaRMS_Text = null;
	private DoubleInputTextField betaRMS_Text = null;
	private DoubleInputTextField emtRMS_Text = null;
	private boolean is_ready_rms = false;

	private DoubleInputTextField alphaGAU_Text = null;
	private DoubleInputTextField betaGAU_Text = null;
	private DoubleInputTextField emtGAU_Text = null;
	private boolean is_ready_gau = false;

	//initialization listener - it is called by
	//others analyses when necessary
	private ActionListener init_listener_gau = null;

	//Gaussian phase space density
	private GaussianDensity gaussianDensity = new GaussianDensity();

	//scorer and solver for fitting
	private GaussScorer scorer = new GaussScorer();
	private Solver solver;
	private Problem problem;


	/**
	 *  Constructor for the AnalysisGaussFit object
	 *
	 *@param  crossParamMap         The HashMap with Parameters of the analyses
	 *@param  analysisTypeIndex_In  The type index of the analysis
	 */
	AnalysisGaussFit(int analysisTypeIndex_In, HashMap<String,Object> crossParamMap) {
		super( analysisTypeIndex_In, crossParamMap );

		analysisDescriptionString = " GAUSS FITTING" +
				System.getProperties().getProperty("line.separator").toString() +
				"RED - phase density measured" +
				System.getProperties().getProperty("line.separator").toString() +
				"MAGENTA - Gaussian fit" +
				System.getProperties().getProperty("line.separator").toString() +
				"BLUE - threshold" +
				System.getProperties().getProperty("line.separator").toString() +
				"BLACK - min value" +
				System.getProperties().getProperty("line.separator").toString() +
				"The threshold analysis should be done before to get " +
				"rms emittance, alpha, and beta parameters as initial " +
				"values for fitting";

		//graph panel properties
		GP.setLegendVisible(true);
		GP.setLegendButtonVisible(true);
		GP.setOffScreenImageDrawing(true);
		GP.setGraphBackGroundColor(Color.white);
		GP.setAxisNameX("position in section");
		GP.setAxisNameY("value");
		GP.setName("Phase Space Section");
		GP.removeAllGraphData();
		GP.addGraphData(gaussCrossingGR);

		//set graph properties for gauss fitting crossing
		gaussCrossingGR.setGraphColor(Color.magenta);
		gaussCrossingGR.setDrawPointsOn(false);
		gaussCrossingGR.removeAllPoints();
		gaussCrossingGR.setGraphProperty(GP.getLegendKeyString(),
				" Gaussian fitting ");
		gaussCrossingGR.setLineThick(2);
		gaussCrossingGR.setImmediateContainerUpdate(false);

		//phase plane ellipse
		phasePlaneEllipse.getCurveData().setColor(Color.white);
		phasePlaneEllipse.getCurveData().setLineWidth(2);
		phasePlaneEllipse.getCurveData().clear();

		//define buttons and labels
		cent_button.setSelected(true);
		vert_button.setSelected(false);
		horz_button.setSelected(false);

		buttonGroup.add(cent_button);
		buttonGroup.add(vert_button);
		buttonGroup.add(horz_button);

		cent_button.setToolTipText("centered line section");
		vert_button.setToolTipText("horizontal line section");
		horz_button.setToolTipText("vertical line section");

		cent_label.setToolTipText("centered line section");
		vert_label.setToolTipText("horizontal line section");
		horz_label.setToolTipText("vertical line section");

		//Panels
		Border etchedBorder = BorderFactory.createEtchedBorder();

		bottomPanel = getBottomPanel();
		bottomPanel.setBorder(etchedBorder);
		bottomPanel.setLayout(new BorderLayout());
		bottomPanel.add(graphPanel, BorderLayout.CENTER);
		bottomPanel.add(controllPanel, BorderLayout.EAST);

		controllPanel.setBorder(etchedBorder);
		controllPanel.add(leftControllPanel, BorderLayout.WEST);
		controllPanel.add(rightControllPanel, BorderLayout.CENTER);

		//left custom panel (bottom)
		JPanel crossTypeButtonPanel = new JPanel(new GridLayout(2, 3, 1, 1));
		crossTypeButtonPanel.add(cent_button);
		crossTypeButtonPanel.add(vert_button);
		crossTypeButtonPanel.add(horz_button);

		crossTypeButtonPanel.add(cent_label);
		crossTypeButtonPanel.add(vert_label);
		crossTypeButtonPanel.add(horz_label);

		JPanel crossTypeSliderPanel = new JPanel(new GridLayout(1, 3, 1, 1));
		crossTypeSliderPanel.add(cent_slider);
		crossTypeSliderPanel.add(vert_slider);
		crossTypeSliderPanel.add(horz_slider);

		JPanel crossTypePanel = new JPanel(new BorderLayout());
		crossTypePanel.add(crossTypeButtonPanel, BorderLayout.NORTH);
		crossTypePanel.add(crossTypeSliderPanel, BorderLayout.CENTER);
		crossTypePanel.setBorder(etchedBorder);

		JPanel emtParPanel = new JPanel(new GridLayout(2, 3, 1, 1));
		localEmtParamBborder =
				BorderFactory.createTitledBorder(etchedBorder, "initial fitting parameters");
		emtParPanel.setBorder(localEmtParamBborder);
		emtParPanel.add(alphaLocal_Label);
		emtParPanel.add(betaLocal_Label);
		emtParPanel.add(emtLocal_Label);
		emtParPanel.add(alphaLocal_Text);
		emtParPanel.add(betaLocal_Text);
		emtParPanel.add(emtLocal_Text);

		JPanel fitParPanel_0 = new JPanel(new GridLayout(2, 1, 1, 1));
		fitParPanel_0.add(emtBounding_Spinner);
		fitParPanel_0.add(fitTime_Spinner);

		JPanel fitParPanel_1 = new JPanel(new GridLayout(2, 1, 1, 1));
		fitParPanel_1.add(emtBounding_Label);
		fitParPanel_1.add(fitTime_Label);

		JPanel fitParPanel = new JPanel(new BorderLayout());
		fitParPanel.add(fitParPanel_0, BorderLayout.WEST);
		fitParPanel.add(fitParPanel_1, BorderLayout.CENTER);
		fitParPanel.setBorder(etchedBorder);

		JPanel calcFitPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 3, 3));
		calcFitPanel.add(fit_Button);

		fit_Button.setForeground(Color.blue.darker());
		//fit_Button.setBackground( Color.cyan );

		JPanel rightCntrPanel_0 = new JPanel(new BorderLayout());
		rightCntrPanel_0.add(emtParPanel, BorderLayout.NORTH);
		rightCntrPanel_0.add(fitParPanel, BorderLayout.CENTER);
		rightCntrPanel_0.add(calcFitPanel, BorderLayout.SOUTH);

		rightControllPanel.add(rightCntrPanel_0, BorderLayout.NORTH);

		leftControllPanel.add(crossTypePanel, BorderLayout.CENTER);

		graphPanel.setBorder(etchedBorder);
		graphPanel.add(GP, BorderLayout.CENTER);

		//define curve properties
		thresh_CurveData.setColor(Color.blue);
		boundR_CurveData.setColor(Color.black);
		waveForm_CurveData.setColor(Color.red);
		section_CurveData.setColor(Color.red);

		thresh_CurveData.setLineWidth(2);
		section_CurveData.setLineWidth(2);
		waveForm_CurveData.setLineWidth(2);
		boundR_CurveData.setLineWidth(1);

		//define sliders (scroll bars' listener)
		cent_slider.setEnabled(true);
		vert_slider.setEnabled(false);
		horz_slider.setEnabled(false);

		cent_slider.setUnitIncrement(1);
		vert_slider.setUnitIncrement(1);
		horz_slider.setUnitIncrement(1);

		cent_slider.setBlockIncrement(20);
		vert_slider.setBlockIncrement(20);
		horz_slider.setBlockIncrement(20);

		ActionListener radioButtonListener =
			new ActionListener() {
				public void actionPerformed(ActionEvent e) {

					if (cent_button.isSelected()) {
						cent_slider.setEnabled(true);
					} else {
						cent_slider.setEnabled(false);
					}

					if (vert_button.isSelected()) {
						vert_slider.setEnabled(true);
					} else {
						vert_slider.setEnabled(false);
					}

					if (horz_button.isSelected()) {
						horz_slider.setEnabled(true);
					} else {
						horz_slider.setEnabled(false);
					}

					plotSectionGraph(getScrollBarTypeIndex());
				}
			};

		cent_button.addActionListener(radioButtonListener);
		vert_button.addActionListener(radioButtonListener);
		horz_button.addActionListener(radioButtonListener);

		ChangeListener scrollBar_Listener =
			new ChangeListener() {
				public void stateChanged(ChangeEvent e) {
					plotSectionGraph(getScrollBarTypeIndex());
				}
			};

		cent_slider.getModel().addChangeListener(scrollBar_Listener);
		vert_slider.getModel().addChangeListener(scrollBar_Listener);
		horz_slider.getModel().addChangeListener(scrollBar_Listener);

		emtBounding_Spinner.addChangeListener(
			new ChangeListener() {
				public void stateChanged(ChangeEvent e) {
					if (isDataExist != true || is_ready_rms != true) {
						return;
					}
					phasePlaneEllipse.setEmtAlphaBeta(
							0.01 * emtLocal_Text.getValue() *
							(((Integer) emtBounding_Spinner.getValue()).intValue()),
							alphaLocal_Text.getValue(),
							betaLocal_Text.getValue());
					phasePlaneEllipse.calcCurvePoints();
					GP_ep.refreshGraphJPanel();
				}
			});

		//fitting calculation button action
		fit_Button.addActionListener(
			new ActionListener() {
				public void actionPerformed(ActionEvent e) {

					Double betaGamma_D = (Double) getParamsHashMap().get("GAMMA_BETA");
					double betaGamma = betaGamma_D.doubleValue();

					getTextMessage().setText(null);

					if (is_ready_rms == false) {
						getTextMessage().setText(
								"You have to carry out the " +
								"Threshold Analysis first.");
						Toolkit.getDefaultToolkit().beep();
						return;
					}

					//set initial values
					gaussianDensity.setEmtAlphaBeta( emtLocal_Text.getValue() / betaGamma, alphaLocal_Text.getValue(), betaLocal_Text.getValue() );

					scorer.init(emittance3Da, phasePlaneEllipse, gaussianDensity);

					final double maxSolveTime = ((Integer)fitTime_Spinner.getValue()).doubleValue();
					solver = new Solver( SolveStopperFactory.minMaxTimeSatisfactionStopper( 0.5, maxSolveTime, 0.99 ) );
					problem = makeProblem( gaussianDensity, scorer );

					//perform fitting
					solver.solve( problem );
					final ScoreBoard scoreBoard = solver.getScoreBoard();
					final Trial bestSolution = scoreBoard.getBestSolution();
					scorer.applyTrialPoint( bestSolution.getTrialPoint() );

					is_ready_gau = true;
					getParamsHashMap().put("IS_READY_GAU", new Boolean(is_ready_gau));

					emtLocal_Text.setValueQuietly(gaussianDensity.getEmt() * betaGamma);
					alphaLocal_Text.setValueQuietly(gaussianDensity.getAlpha());
					betaLocal_Text.setValueQuietly(gaussianDensity.getBeta());

					emtGAU_Text.setValue(gaussianDensity.getEmt() * betaGamma);
					alphaGAU_Text.setValue(gaussianDensity.getAlpha());
					betaGAU_Text.setValue(gaussianDensity.getBeta());

					phasePlaneEllipse.setEmtAlphaBeta(
							0.01 * emtLocal_Text.getValue() *
							(((Integer) emtBounding_Spinner.getValue()).intValue()),
							alphaLocal_Text.getValue(),
							betaLocal_Text.getValue());
					phasePlaneEllipse.calcCurvePoints();
					GP_ep.refreshGraphJPanel();

					System.out.println("===RESULTS of GAUSSIAN EMITTANCE FITTING===");
					System.out.println(scoreBoard.toString());

					plotSectionGraph(getScrollBarTypeIndex());

				}
			});

		//local text listeners to draw the ellipse
		ActionListener localTextFieldListener =
			new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					phasePlaneEllipse.setEmtAlphaBeta(
							0.01 * emtLocal_Text.getValue() *
							(((Integer) emtBounding_Spinner.getValue()).intValue()),
							alphaLocal_Text.getValue(),
							betaLocal_Text.getValue());
					phasePlaneEllipse.calcCurvePoints();
					GP_ep.refreshGraphJPanel();
				}
			};

		emtLocal_Text.addActionListener(localTextFieldListener);
		alphaLocal_Text.addActionListener(localTextFieldListener);
		betaLocal_Text.addActionListener(localTextFieldListener);

		//initialization listener - it is called by
		//others analyses when necessary
		init_listener_gau =
			new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					initialize();
				}
			};
	}


	/** 
	 * Generate a new problem.
	 * @param gaussianDensity density from which to get the initial problem parameters
	 * @param scorer the scorer to which to assign the variables
	 * @return the new problem
	 */
	private Problem makeProblem( final GaussianDensity gaussianDensity, final GaussScorer scorer ) {
		final Problem problem = ProblemFactory.getInverseSquareMinimizerProblem( new ArrayList<>(), scorer, 0.1 );

		final InitialDelta initialDeltaHint = new InitialDelta();
		problem.addHint( initialDeltaHint );

		final Variable emtVariable = new Variable( "emt", gaussianDensity.getEmt(), 0.0, Double.MAX_VALUE );
		problem.addVariable( emtVariable );
		initialDeltaHint.addInitialDelta( emtVariable, 0.01 );

		final Variable alphaVariable = new Variable( "alpha", gaussianDensity.getAlpha(), -Double.MAX_VALUE, Double.MAX_VALUE );
		problem.addVariable( alphaVariable );
		initialDeltaHint.addInitialDelta( alphaVariable, 0.05 );

		final Variable betaVariable = new Variable( "beta", gaussianDensity.getBeta(), 0.0, Double.MAX_VALUE );
		problem.addVariable( betaVariable );
		initialDeltaHint.addInitialDelta( betaVariable, 0.05 );

		//correction of the possible non-normalization in the experimental data
		final Variable maxValVariable = new Variable( "maxVal", gaussianDensity.getMaxVal(), -Double.MAX_VALUE, Double.MAX_VALUE );
		problem.addVariable( maxValVariable );
		initialDeltaHint.addInitialDelta( maxValVariable, 0.01 );

		// set the variables on the scorer
		scorer.setVariables( emtVariable, alphaVariable, betaVariable, maxValVariable );

		return problem;
	}


	/**
	 *  Description of the Method
	 *
	 *@param  typeIndex  Description of the Parameter
	 */
	private void plotSectionGraph(int typeIndex) {

		waveForm_CurveData.clear();

		if (!isDataExist) {
			return;
		}

		double z_max = emittance3Da.getMaxZ();

		double x_min = emittance3Da.getMinX();
		double x_max = emittance3Da.getMaxX();
		double xp_min = emittance3Da.getMinY();
		double xp_max = emittance3Da.getMaxY();

		double x_start = 0.;
		double xp_start = 0.;

		double x_stop = 0.;
		double xp_stop = 0.;

		double frac = 0.;

		int nPoints = 0;

		if (typeIndex == 0) {
			nPoints = cent_slider.getMaximum();
			frac = (double) cent_slider.getValue();

			double[] arr = CrossingProducer.getRotatedLimits(frac, 0., 0., emittance3Da);
			x_start = arr[0];
			xp_start = arr[1];
			x_stop = arr[2];
			xp_stop = arr[3];

		}
		if (typeIndex == 1) {
			nPoints = vert_slider.getMaximum();
			frac = ((double) vert_slider.getValue()) /
					(vert_slider.getMaximum() - vert_slider.getVisibleAmount());
			xp_start = xp_min;
			xp_stop = xp_max;
			x_start = x_max - (x_max - x_min) * frac;
			x_stop = x_start;
		}
		if (typeIndex == 2) {
			nPoints = horz_slider.getMaximum();
			frac = ((double) horz_slider.getValue()) /
					(horz_slider.getMaximum() - horz_slider.getVisibleAmount());
			x_start = x_min;
			x_stop = x_max;
			xp_start = xp_min + (xp_max - xp_min) * frac;
			xp_stop = xp_start;
		}

		double x = 0.;
		double xp = 0.;
		double pos = 0.;
		double val = 0.;

		gaussCrossingGR.removeAllPoints();

		for (int i = 0; i < nPoints; i++) {
			pos = ((double) i) / nPoints;
			x = x_start + (x_stop - x_start) * pos;
			xp = xp_start + (xp_stop - xp_start) * pos;
			pos = 2.0 * (pos - 0.5);
			val = 100.0 * emittance3Da.getValue(x, xp) / z_max;
			waveForm_CurveData.addPoint(pos, val);

			//place to plot gaussian distribution
			if (is_ready_rms) {
				gaussCrossingGR.addPoint(pos,
						100.0 * gaussianDensity.getDensity(x, xp));
			}
		}

		section_CurveData.clear();
		section_CurveData.addPoint(x_start, xp_start);
		section_CurveData.addPoint(x_stop, xp_stop);

		GP.refreshGraphJPanel();
		GP_ep.refreshGraphJPanel();
	}


	/**
	 *  Gets the scrollBarTypeIndex attribute of the AnalysisGaussFit object
	 *
	 *@return    The scrollBarTypeIndex value
	 */
	private int getScrollBarTypeIndex() {
		if (cent_button.isSelected()) {
			return 0;
		}
		if (vert_button.isSelected()) {
			return 1;
		}
		return 2;
	}


	/**
	 *  Performs actions before show the panel
	 */
	void goingShowUp() {

		getTextMessage().setText(null);

		emittance3Da = (ColorSurfaceData) getParamsHashMap().get("RawEmittanceData");
		is_ready_rms = ((Boolean) getParamsHashMap().get("IS_READY_RMS")).booleanValue();
		is_ready_gau = ((Boolean) getParamsHashMap().get("IS_READY_GAU")).booleanValue();

		double betaGamma = ((Double) getParamsHashMap().get("GAMMA_BETA")).doubleValue();

		isDataExist = true;

		fit_Button.setEnabled(true);

		//check that data exist
		double z_min = emittance3Da.getMinZ();
		double z_max = emittance3Da.getMaxZ();

		if (z_max <= 0. || z_min == z_max) {
			isDataExist = false;
			is_ready_rms = false;
			getTextMessage().setText(null);
			getTextMessage().setText("The data for analysis do not" +
					" exist");
			Toolkit.getDefaultToolkit().beep();
			fit_Button.setEnabled(false);
			return;
		}

		if (!is_ready_rms) {
			getTextMessage().setText(null);
			getTextMessage().setText("You have to perform Threshold Analysis" +
					" first and set RMS emittance, alpha, and beta.");
			Toolkit.getDefaultToolkit().beep();
			fit_Button.setEnabled(false);
		}

		if (is_ready_rms == true && emtRMS_Text.getValue() <= 0.) {
			is_ready_rms = false;
			getTextMessage().setText(null);
			getTextMessage().setText("You have to perform Threshold Analysis" +
					" again. The RMS emittance < 0 is not good.");
			Toolkit.getDefaultToolkit().beep();
			fit_Button.setEnabled(false);
		}

		if (is_ready_rms == true) {

			if (is_ready_gau == false) {
				alphaLocal_Text.setValueQuietly(alphaRMS_Text.getValue());
				betaLocal_Text.setValueQuietly(betaRMS_Text.getValue());
				emtLocal_Text.setValueQuietly(emtRMS_Text.getValue());

				gaussianDensity.setEmtAlphaBeta(
						emtRMS_Text.getValue() / betaGamma,
						alphaRMS_Text.getValue(),
						betaRMS_Text.getValue());

			}

			phasePlaneEllipse.setEmtAlphaBeta(
					0.01 * emtLocal_Text.getValue() *
					(((Integer) emtBounding_Spinner.getValue()).intValue()),
					alphaLocal_Text.getValue(),
					betaLocal_Text.getValue());
			phasePlaneEllipse.calcCurvePoints();

		}

		thresh_CurveData.clear();
		thresh_CurveData.addPoint(-1., threshold_Text.getValue());
		thresh_CurveData.addPoint(1., threshold_Text.getValue());

		boundR_CurveData.clear();
		boundR_CurveData.addPoint(-1.0, 100.);
		boundR_CurveData.addPoint(1.0, 100.);
		boundR_CurveData.addPoint(1.0, 100.0 * z_min / z_max);
		boundR_CurveData.addPoint(-1.0, 100.0 * z_min / z_max);
		boundR_CurveData.addPoint(-1.0, 100.);

		GP.removeAllCurveData();
		GP.addCurveData(waveForm_CurveData);
		GP.addCurveData(thresh_CurveData);
		GP.addCurveData(boundR_CurveData);

		GP_ep.addCurveData(phasePlaneEllipse.getCurveData());
		GP_ep.addCurveData(section_CurveData);

		plotSectionGraph(getScrollBarTypeIndex());

	}


	/**
	 *  Performs actions before close the panel
	 */
	void goingShowOff() {
		GP_ep.removeAllCurveData();
		getParamsHashMap().put("IS_READY_GAU", new Boolean(is_ready_gau));
	}


	/**
	 *  Sets all analyzes in the initial state with removing all temporary data
	 */
	void initialize() {
		getParamsHashMap().put("IS_READY_GAU", new Boolean(false));

		GP.clearZoomStack();

		alphaGAU_Text.setText(null);
		betaGAU_Text.setText(null);
		emtGAU_Text.setText(null);

		alphaGAU_Text.setBackground(Color.white);
		betaGAU_Text.setBackground(Color.white);
		emtGAU_Text.setBackground(Color.white);

		alphaLocal_Text.setText(null);
		betaLocal_Text.setText(null);
		emtLocal_Text.setText(null);

		alphaLocal_Text.setBackground(Color.white);
		betaLocal_Text.setBackground(Color.white);
		emtLocal_Text.setBackground(Color.white);

		phasePlaneEllipse.getCurveData().clear();
		gaussCrossingGR.removeAllPoints();
	}


	/**
	 *  Creates objects for the global HashMap using put method only
	 */
	void createHashMapObjects() {
		getParamsHashMap().put("INIT_LISTENER_GAU", init_listener_gau);
	}


	/**
	 *  Connects to the objects in the global HashMap using only get method of the
	 *  HashMap
	 */
	void connectToHashMapObjects() {
		threshold_Text = (DoubleInputTextField) getParamsHashMap().get("THRESHOLD_TEXT");
		GP_ep = (FunctionGraphsJPanel) getParamsHashMap().get("EMITTANCE_3D_PLOT");

		alphaRMS_Text = (DoubleInputTextField) getParamsHashMap().get("ALPHA_RMS");
		betaRMS_Text = (DoubleInputTextField) getParamsHashMap().get("BETA_RMS");
		emtRMS_Text = (DoubleInputTextField) getParamsHashMap().get("EMT_RMS");

		alphaGAU_Text = (DoubleInputTextField) getParamsHashMap().get("ALPHA_GAU");
		betaGAU_Text = (DoubleInputTextField) getParamsHashMap().get("BETA_GAU");
		emtGAU_Text = (DoubleInputTextField) getParamsHashMap().get("EMT_GAU");

		alphaLocal_Text.setNumberFormat(alphaGAU_Text.getNumberFormat());
		betaLocal_Text.setNumberFormat(betaGAU_Text.getNumberFormat());
		emtLocal_Text.setNumberFormat(emtGAU_Text.getNumberFormat());

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
		is_ready_gau = ((Boolean) getParamsHashMap().get("IS_READY_GAU")).booleanValue();
	}


	/**
	 *  Sets all fonts.
	 *
	 *@param  fnt  The new font
	 */
	void setFontForAll(Font fnt) {

		cent_button.setFont(fnt);
		vert_button.setFont(fnt);
		horz_button.setFont(fnt);

		cent_label.setFont(fnt);
		vert_label.setFont(fnt);
		horz_label.setFont(fnt);

		cent_slider.setFont(fnt);
		vert_slider.setFont(fnt);
		horz_slider.setFont(fnt);

		localEmtParamBborder.setTitleFont(fnt);

		alphaLocal_Text.setFont(fnt);
		betaLocal_Text.setFont(fnt);
		emtLocal_Text.setFont(fnt);

		alphaLocal_Label.setFont(fnt);
		betaLocal_Label.setFont(fnt);
		emtLocal_Label.setFont(fnt);

		emtBounding_Label.setFont(fnt);
		fitTime_Label.setFont(fnt);

		fit_Button.setFont(fnt);

		emtBounding_Spinner.setFont(fnt);
		((JSpinner.DefaultEditor) emtBounding_Spinner.getEditor()).getTextField().setFont(fnt);

		fitTime_Spinner.setFont(fnt);
		((JSpinner.DefaultEditor) fitTime_Spinner.getEditor()).getTextField().setFont(fnt);
	}

}



/**
 *  This class calculates a Gaussian phase space density.
 *
 *@author     shishlo
 *@version    1.0
 */
class GaussianDensity {
	private double emt;
	private double alpha;
	private double beta;
	private double maxVal;


	/**
	 *  Constructor for the GaussianDensity object
	 */
	GaussianDensity() {
		this.emt = 0.1;
		this.alpha = 1.0;
		this.beta = 2.0;
		this.maxVal = 1.0;
	}


	/**
	 *  Returns the emittance parameter of the Gaussian phase space density
	 *
	 *@return    The emittance parameter of the Gaussian phase space density
	 */
	double getEmt() {
		return emt;
	}


	/**
	 *  Returns the alpha parameter of the Gaussian phase space density
	 *
	 *@return    The alpha parameter of the Gaussian phase space density
	 */
	double getAlpha() {
		return alpha;
	}


	/**
	 *  Returns the beta parameter of the Gaussian phase space density
	 *
	 *@return    The beta parameter of the Gaussian phase space density
	 */
	double getBeta() {
		return beta;
	}


	/** get the max val */
	double getMaxVal() {
		return this.maxVal;
	}


	/**
	 * Sets the parameters of the Gaussian density with maxVal set to 1.0
	 * @param  emt    The emittance parameter of the Gaussian phase space density
	 * @param  alpha  The alpha parameter of the Gaussian phase space density
	 * @param  beta   The beta parameter of the Gaussian phase space density
	 */
	void setEmtAlphaBeta( double emt, double alpha, double beta ) {
		setEmtAlphaBetaMaxVal( emt, alpha, beta, 1.0 );
	}


	/**
	 * Sets the parameters of the Gaussian density
	 * @param  emt    The emittance parameter of the Gaussian phase space density
	 * @param  alpha  The alpha parameter of the Gaussian phase space density
	 * @param  beta   The beta parameter of the Gaussian phase space density
	 * @param  maxVal Correction of the possible non-normalization in the experimental data
	 */
	void setEmtAlphaBetaMaxVal( final double emt, final double alpha, final double beta, final double maxVal ) {
		this.emt = emt;
		this.alpha = alpha;
		this.beta = beta;
		this.maxVal = maxVal;
	}


	/**
	 *  Returns the Gaussian density value at particular point of the phase space plane
	 *@param  x   The coordinate on the phase space plane
	 *@param  xp  The momentum on the phase space plane
	 *@return     The density value
	 */
	double getDensity(double x, double xp) {
		double val = alpha * x + beta * xp;
		val = val * val + x * x;
		val = Math.exp(-val / (2 * beta * emt));
		val *= maxVal;
		return val;
	}
}

/**
 *  This is an implementation of the Scorer interface for our fitting
 *
 *@author     shishlo
 *@version    August 23, 2004
 */
class GaussScorer implements Scorer {
	//curve data for ellipse on the color surface
	//it is used to define the area on the phase space
	private PhasePlaneEllipse phasePlaneEllipse = null;

	//emittance data as ColorSurfaceData instance (for analysis only)
	private ColorSurfaceData emittance3Da = null;

	//Gaussian phase space density
	private GaussianDensity gaussianDensity = null;

	private int i_x_min = 0;
	private int i_x_max = 0;

	private int i_xp_min = 0;
	private int i_xp_max = 0;

	private double z_max = 0.;

	// problem variables
	private Variable emtVariable;
	private Variable alphaVariable;
	private Variable betaVariable;
	private Variable maxValVariable;


	/** set the variables */
	void setVariables( final Variable emtVariable, final Variable alphaVariable, final Variable betaVariable, final Variable maxValVariable ) {
		this.emtVariable = emtVariable;
		this.alphaVariable = alphaVariable;
		this.betaVariable = betaVariable;
		this.maxValVariable = maxValVariable;
	}


	/**
	 *  This method initializes all data for fitting. It should be called before
	 *  optimization starts
	 *
	 *@param  emittance3Da       The experimental phase space density
	 *@param  phasePlaneEllipse  The ellipse on the phase space
	 *@param  gaussianDensity    The theoretical phase space density
	 */
	void init(ColorSurfaceData emittance3Da, PhasePlaneEllipse phasePlaneEllipse, GaussianDensity gaussianDensity) {
		this.emittance3Da = emittance3Da;
		this.phasePlaneEllipse = phasePlaneEllipse;
		this.gaussianDensity = gaussianDensity;

		int nX = emittance3Da.getSizeX();
		int nXP = emittance3Da.getSizeY();

		double x_max = phasePlaneEllipse.getMaxX();
		double xp_max = phasePlaneEllipse.getMaxX();

		double x_min = -x_max;
		double xp_min = -xp_max;

		i_x_min = 0;
		i_x_max = nX - 1;

		i_xp_min = 0;
		i_xp_max = nXP - 1;

		for (int i = 0; i < (nX - 1); i++) {
			if (x_min < emittance3Da.getX(i + 1) &&
					x_min >= emittance3Da.getX(i)) {
				i_x_min = i - 1;
			}

			if (x_max <= emittance3Da.getX(i + 1) &&
					x_max > emittance3Da.getX(i)) {
				i_x_max = i + 1;
			}
		}

		i_x_min = Math.max(0, i_x_min);
		i_x_min = Math.min(nX - 1, i_x_min);

		i_x_max = Math.max(0, i_x_max);
		i_x_max = Math.min(nX - 1, i_x_max);

		for (int i = 0; i < (nXP - 1); i++) {
			if (xp_min < emittance3Da.getY(i + 1) &&
					xp_min >= emittance3Da.getY(i)) {
				i_xp_min = i - 1;
			}

			if (xp_max <= emittance3Da.getY(i + 1) &&
					xp_max > emittance3Da.getY(i)) {
				i_xp_max = i + 1;
			}
		}

		i_xp_min = Math.max(0, i_xp_min);
		i_xp_min = Math.min(nXP - 1, i_xp_min);

		i_xp_max = Math.max(0, i_xp_max);
		i_xp_max = Math.min(nXP - 1, i_xp_max);

		z_max = emittance3Da.getMaxZ();
	}


	/** apply the trial point to configure the model */
	void applyTrialPoint( final TrialPoint trialPoint ) {
		final double emt = trialPoint.getValue( emtVariable );
		final double alpha = trialPoint.getValue( alphaVariable );
		final double beta = trialPoint.getValue( betaVariable );
		final double maxVal = trialPoint.getValue( maxValVariable );
		gaussianDensity.setEmtAlphaBetaMaxVal( emt, alpha, beta, maxVal	);
	}


	/**
	 *  The score method implementation of the Scorer interface
	 *
	 *@return    The score
	 */
	public double score( final Trial trial, final List<Variable> variables ) {
		double sum2 = 0.;
		double x = 0.;
		double xp = 0.;
		double val_exp = 0.;
		double val_th = 0.;

		applyTrialPoint( trial.getTrialPoint() );

		for (int ix = i_x_min; ix <= i_x_max; ix++) {
			x = emittance3Da.getX(ix);
			for (int ixp = i_xp_min; ixp <= i_xp_max; ixp++) {
				xp = emittance3Da.getY(ixp);
				val_exp = emittance3Da.getValue(ix, ixp) / z_max;
				val_th = gaussianDensity.getDensity(x, xp);
				sum2 += (val_th - val_exp) * (val_th - val_exp);
			}
		}

		if ( Double.isNaN( sum2 ) ) {
			sum2 = Double.POSITIVE_INFINITY;
		}

		return sum2;
	}
}

