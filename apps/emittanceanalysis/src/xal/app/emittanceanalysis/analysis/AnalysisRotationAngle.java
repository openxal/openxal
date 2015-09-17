package xal.app.emittanceanalysis.analysis;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.text.*;
import javax.swing.border.*;
import javax.swing.event.*;
import java.awt.event.*;

import xal.extension.widgets.plot.*;
import xal.extension.widgets.swing.*;
import xal.tools.text.*;
import xal.app.emittanceanalysis.phasespaceanalysis.*;
import xal.extension.solver.*;
import xal.extension.widgets.apputils.*;

/**
 *  This analysis finds the angle of rotation of the phase space distribution
 *
 *@author     shishlo
 *@version    1.0
 */
class AnalysisRotationAngle extends AnalysisBasic {

	//emittance data as ColorSurfaceData instance (for analysis only)
	private ColorSurfaceData emittance3Da = null;

	//normalized phase space density
	private ColorSurfaceData normPhaseSpace3D = Data3DFactory.getData3D(150, 150, "linear");

	//threshold text field from common part of the left top corner panel
	private DoubleInputTextField threshold_Text = null;

	//double values format
	private NumberFormat dbl_Format = new ScientificNumberFormat(5);

	//bottom panel. It includes the graph panel (bottom left)
	//and the controll panel (bottom right)
	private JPanel bottomPanel = null;
	private JPanel graphPanel = new JPanel(new BorderLayout());
	private JPanel controllPanel = new JPanel(new BorderLayout());
	private JPanel leftControllPanel = new JPanel(new BorderLayout());
	private JPanel rightControllPanel = new JPanel(new BorderLayout());

	//graph with a new color surface plot with transformed phase density
	private FunctionGraphsJPanel GP = new FunctionGraphsJPanel();

	//color surface graph with space phase data
	private FunctionGraphsJPanel GP_ep = null;

	//JLabels for cross line moving types
	private JLabel angle_label = new JLabel("A", JLabel.CENTER);
	private JLabel vert_label = new JLabel("Y", JLabel.CENTER);
	private JLabel horz_label = new JLabel("X", JLabel.CENTER);

	//sliders for cross line types
	private JScrollBar angle_slider = new JScrollBar(JScrollBar.VERTICAL, 0, 4, 0, 364);
	private JScrollBar vert_slider = new JScrollBar(JScrollBar.VERTICAL, 100, 4, 0, 200);
	private JScrollBar horz_slider = new JScrollBar(JScrollBar.VERTICAL, 100, 4, 0, 200);

	//GUI elements of left bottom control panel
	private DoubleInputTextField angleOfRot_Text = new DoubleInputTextField(10);

	private JLabel angleOfRot_Label =
			new JLabel(" angle of rotation, deg ", JLabel.LEFT);

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

	//calculation buttons
	private JButton plotNormDens_Button = new JButton("PLOT NORMALIZED PhSp DENSITY");

	//curve data for line in the color surface to measure angle
	private BasicGraphData section_CurveData = new BasicGraphData();

	//boolean variable indicating that data are not empty
	private boolean isDataExist = false;

	//initialization listener - it is called by
	//others analyses when necessary
	private ActionListener init_listener_aor = null;

	//text fields borrowed from common panel
	private DoubleInputTextField alphaRMS_Text = null;
	private DoubleInputTextField betaRMS_Text = null;
	private DoubleInputTextField emtRMS_Text = null;

	private DoubleInputTextField alphaGAU_Text = null;
	private DoubleInputTextField betaGAU_Text = null;
	private DoubleInputTextField emtGAU_Text = null;

	private boolean is_ready_rms = false;
	private boolean is_ready_gau = false;

	private boolean is_ready_ang = false;


	/**
	 *  Constructor for the AnalysisRotationAngle object
	 *
	 *@param  crossParamMap         The HashMap with Parameters of the analyses
	 *@param  analysisTypeIndex_In  The type index of the analysis
	 */
	AnalysisRotationAngle(int analysisTypeIndex_In, HashMap<String,Object> crossParamMap) {
		super(analysisTypeIndex_In, crossParamMap);
		analysisDescriptionString = " PHASE SPACE ROTATION " +
				System.getProperties().getProperty("line.separator").toString() +
				"RED - line to measure the angle" +
				System.getProperties().getProperty("line.separator").toString() +
				"Transformation:" +
				System.getProperties().getProperty("line.separator").toString() +
				"Y = y / sqrt(beta*emt)" +
				System.getProperties().getProperty("line.separator").toString() +
				"YP = (y*alpha + beta*yp)*(Y/y)" +
				System.getProperties().getProperty("line.separator").toString() +
				"The threshold analysis should be done before to get " +
				"rms emittance, alpha, and beta parameters that will be used here.";

		//text field
		angleOfRot_Text.setHorizontalAlignment(JTextField.CENTER);
		angleOfRot_Text.setEditable(false);
		angleOfRot_Text.setText(null);
		angleOfRot_Text.setBackground(Color.white);

		//graph panel properties
		GP.setLegendButtonVisible(false);
		GP.setOffScreenImageDrawing(true);
		GP.setGraphBackGroundColor(Color.white);
		GP.setAxisNameX("Y");
		GP.setAxisNameY("Y'");
		GP.setName("Transformed Phase Space Density");
		GP.setGridLinesVisibleX(false);
		GP.setGridLinesVisibleY(false);
		GP.removeAllGraphData();
		GP.addGraphData(section_CurveData);
		GP.setColorSurfaceData(normPhaseSpace3D);

		//set graph properties for angle line
		section_CurveData.setGraphColor(Color.red);
		section_CurveData.setDrawPointsOn(false);
		section_CurveData.removeAllPoints();
		section_CurveData.setLineThick(2);
		section_CurveData.setImmediateContainerUpdate(false);

		//define labels
		angle_label.setToolTipText("rotation angle of the line");
		vert_label.setToolTipText("line's horizontal position");
		horz_label.setToolTipText("line's vertical position");

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
		JPanel crossTypeButtonPanel = new JPanel(new GridLayout(1, 3, 1, 1));
		crossTypeButtonPanel.add(angle_label);
		crossTypeButtonPanel.add(vert_label);
		crossTypeButtonPanel.add(horz_label);

		JPanel crossTypeSliderPanel = new JPanel(new GridLayout(1, 3, 1, 1));
		crossTypeSliderPanel.add(angle_slider);
		crossTypeSliderPanel.add(vert_slider);
		crossTypeSliderPanel.add(horz_slider);

		JPanel crossTypePanel = new JPanel(new BorderLayout());
		crossTypePanel.add(crossTypeButtonPanel, BorderLayout.NORTH);
		crossTypePanel.add(crossTypeSliderPanel, BorderLayout.CENTER);
		crossTypePanel.setBorder(etchedBorder);

		JPanel fitParPanel_0 = new JPanel(new BorderLayout());
		fitParPanel_0.add(angleOfRot_Text, BorderLayout.WEST);
		fitParPanel_0.add(angleOfRot_Label, BorderLayout.CENTER);

		JPanel fitParPanel = new JPanel(new BorderLayout());
		fitParPanel.add(fitParPanel_0, BorderLayout.CENTER);
		fitParPanel.setBorder(etchedBorder);

		JPanel emtParamPanel =
				new JPanel(new BorderLayout());

		localEmtParamBborder =
				BorderFactory.createTitledBorder(etchedBorder, "Emittance parameters");
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

		JPanel calcFitPanel =
				new JPanel(new FlowLayout(FlowLayout.CENTER, 3, 3));
		calcFitPanel.add(plotNormDens_Button);

		plotNormDens_Button.setForeground(Color.blue.darker());
		//plotNormDens_Button.setBackground( Color.cyan );

		JPanel rightCntrPanel_0 = new JPanel(new BorderLayout());
		rightCntrPanel_0.add(fitParPanel, BorderLayout.NORTH);
		rightCntrPanel_0.add(emtParamPanel, BorderLayout.CENTER);
		rightCntrPanel_0.add(calcFitPanel, BorderLayout.SOUTH);

		rightControllPanel.add(rightCntrPanel_0, BorderLayout.NORTH);

		leftControllPanel.add(crossTypePanel, BorderLayout.CENTER);

		graphPanel.setBorder(etchedBorder);
		graphPanel.add(GP, BorderLayout.CENTER);

		alphaLocal_Text.setNumberFormat(dbl_Format);
		betaLocal_Text.setNumberFormat(dbl_Format);
		emtLocal_Text.setNumberFormat(dbl_Format);

		//define sliders (scroll bars' listener)
		angle_slider.setEnabled(true);
		vert_slider.setEnabled(true);
		horz_slider.setEnabled(true);

		angle_slider.setUnitIncrement(1);
		vert_slider.setUnitIncrement(1);
		horz_slider.setUnitIncrement(1);

		angle_slider.setBlockIncrement(20);
		vert_slider.setBlockIncrement(20);
		horz_slider.setBlockIncrement(20);

		ChangeListener scrollBar_Listener =
			new ChangeListener() {
				public void stateChanged(ChangeEvent e) {
					angleOfRot_Text.setValue((double) angle_slider.getValue());
					plotSectionGraph();
				}
			};

		angle_slider.getModel().addChangeListener(scrollBar_Listener);
		vert_slider.getModel().addChangeListener(scrollBar_Listener);
		horz_slider.getModel().addChangeListener(scrollBar_Listener);

		//fitting calculation button action
		plotNormDens_Button.addActionListener(
			new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					plotNormalizedDensity();
				}
			});

		//initialization listener - it is called by
		//others analyses when necessary
		init_listener_aor =
			new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					initialize();
				}
			};

		//local text listeners to draw the ellipse
		ActionListener localTextFieldListener =
			new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					plotNormalizedDensity();
				}
			};

		emtLocal_Text.addActionListener(localTextFieldListener);
		alphaLocal_Text.addActionListener(localTextFieldListener);
		betaLocal_Text.addActionListener(localTextFieldListener);

		//set init emittance param from RMS or GAU listeners
		copyEmtFromRMS_Button.addActionListener(
			new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					alphaLocal_Text.setValueQuietly(alphaRMS_Text.getValue());
					betaLocal_Text.setValueQuietly(betaRMS_Text.getValue());
					emtLocal_Text.setValueQuietly(emtRMS_Text.getValue());
				}
			});

		copyEmtFromGAU_Button.addActionListener(
			new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					alphaLocal_Text.setValueQuietly(alphaGAU_Text.getValue());
					betaLocal_Text.setValueQuietly(betaGAU_Text.getValue());
					emtLocal_Text.setValueQuietly(emtGAU_Text.getValue());
				}
			});
	}


	/**
	 *  Description of the Method
	 */
	private void plotNormalizedDensity() {
		getTextMessage().setText(null);

		if (is_ready_rms == false || isDataExist == false) {
			getTextMessage().setText(
					"You have to carry out the " +
					"Threshold Analysis first.");
			Toolkit.getDefaultToolkit().beep();
			return;
		}

		//plot phase space normalized density
		Double betaGamma_D = (Double) getParamsHashMap().get("GAMMA_BETA");
		double betaGamma = betaGamma_D.doubleValue();

		double beta = betaLocal_Text.getValue();
		double alpha = alphaLocal_Text.getValue();
		double emt = emtLocal_Text.getValue() / betaGamma;

		int nX = emittance3Da.getSizeX();
		int nY = emittance3Da.getSizeY();
		normPhaseSpace3D.setSize(nX, nY);
		normPhaseSpace3D.setScreenResolution(
				emittance3Da.getScreenSizeX(),
				emittance3Da.getScreenSizeY());
		normPhaseSpace3D.setMinMaxX(
				emittance3Da.getMinX() / Math.sqrt(beta * emt),
				emittance3Da.getMaxX() / Math.sqrt(beta * emt));

		double yp_max = Math.max(Math.abs(emittance3Da.getMinY()),
				Math.abs(emittance3Da.getMaxY()));
		double y_max = Math.max(Math.abs(emittance3Da.getMinX()),
				Math.abs(emittance3Da.getMaxX()));
		double y_yp_max = (Math.abs(beta) * yp_max + Math.abs(alpha) * y_max)
				 / Math.sqrt(beta * emt);

		normPhaseSpace3D.setMinMaxY(-y_yp_max, y_yp_max);

		double Y;
		double YP;
		double y;
		double yp;
		double val;

		for (int i = 0; i < nX; i++) {
			for (int j = 0; j < nY; j++) {
				Y = normPhaseSpace3D.getX(i) * Math.sqrt(beta * emt);
				YP = normPhaseSpace3D.getY(j) * Math.sqrt(beta * emt);
				y = Y;
				yp = (YP - alpha * Y) / beta;
				val = emittance3Da.getValue(y, yp);
				normPhaseSpace3D.setValue(i, j, val);
			}
		}
		normPhaseSpace3D.calcMaxMinZ();

		plotSectionGraph();
	}


	/**
	 *  Description of the Method
	 */
	private void plotSectionGraph() {

		section_CurveData.removeAllPoints();

		if (!isDataExist) {
			return;
		}

		double x_min = normPhaseSpace3D.getMinX();
		double x_max = normPhaseSpace3D.getMaxX();
		double xp_min = normPhaseSpace3D.getMinY();
		double xp_max = normPhaseSpace3D.getMaxY();

		double frac_y = ((double) vert_slider.getValue()) /
				(vert_slider.getMaximum() - vert_slider.getVisibleAmount());

		double frac_x = ((double) horz_slider.getValue()) /
				(horz_slider.getMaximum() - horz_slider.getVisibleAmount());

		double x_c = x_min + (x_max - x_min) * frac_x;
		double y_c = xp_max + (xp_min - xp_max) * frac_y;

		//??????????????
		//plot line on bottom color plot
		double frac = (double) angle_slider.getValue();
		double[] arr = CrossingProducer.getRealRotatedLimits(frac, x_c, y_c, normPhaseSpace3D);
		double x_start = arr[0];
		double xp_start = arr[1];
		double x_stop = arr[2];
		double xp_stop = arr[3];

		int nP = 10;
		for (int i = 0; i < nP; i++) {
			x_c = x_start + i * (x_stop - x_start) / (nP - 1.0);
			y_c = xp_start + i * (xp_stop - xp_start) / (nP - 1.0);
			section_CurveData.addPoint(x_c, y_c);
		}

		GP.refreshGraphJPanel();
		GP_ep.refreshGraphJPanel();
	}


	/**
	 *  Performs actions before show the panel
	 */
	void goingShowUp() {

		getTextMessage().setText(null);

		emittance3Da = (ColorSurfaceData) getParamsHashMap().get("RawEmittanceData");
		is_ready_rms = ((Boolean) getParamsHashMap().get("IS_READY_RMS")).booleanValue();
		is_ready_gau = ((Boolean) getParamsHashMap().get("IS_READY_GAU")).booleanValue();

		copyEmtFromRMS_Button.setEnabled(false);
		copyEmtFromGAU_Button.setEnabled(false);

		isDataExist = true;

		plotNormDens_Button.setEnabled(true);

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
			plotNormDens_Button.setEnabled(false);
			return;
		}

		if (!is_ready_rms) {
			getTextMessage().setText(null);
			getTextMessage().setText("You have to perform Threshold Analysis" +
					" first and set RMS emittance, alpha, and beta.");
			Toolkit.getDefaultToolkit().beep();
			plotNormDens_Button.setEnabled(false);
		}

		if (is_ready_rms == true && emtRMS_Text.getValue() <= 0.) {
			is_ready_rms = false;
			getTextMessage().setText(null);
			getTextMessage().setText("You have to perform Threshold Analysis" +
					" again. The RMS emittance < 0 is not good.");
			Toolkit.getDefaultToolkit().beep();
			plotNormDens_Button.setEnabled(false);
		}

		if (is_ready_rms) {
			if (!is_ready_ang) {
				alphaLocal_Text.setValueQuietly(alphaRMS_Text.getValue());
				betaLocal_Text.setValueQuietly(betaRMS_Text.getValue());
				emtLocal_Text.setValueQuietly(emtRMS_Text.getValue());
				is_ready_ang = true;
			}
			copyEmtFromRMS_Button.setEnabled(true);
		}

		if (is_ready_gau) {
			copyEmtFromGAU_Button.setEnabled(true);
		}

		plotSectionGraph();
	}


	/**
	 *  Performs actions before close the panel
	 */
	void goingShowOff() {
		GP_ep.removeAllCurveData();
	}


	/**
	 *  Sets all analyzes in the initial state with removing all temporary data
	 */
	void initialize() {

		is_ready_ang = false;

		alphaLocal_Text.setText(null);
		betaLocal_Text.setText(null);
		emtLocal_Text.setText(null);

		alphaLocal_Text.setBackground(Color.white);
		betaLocal_Text.setBackground(Color.white);
		emtLocal_Text.setBackground(Color.white);

		GP.clearZoomStack();
		section_CurveData.removeAllPoints();
		normPhaseSpace3D.setZero();
		GP.refreshGraphJPanel();
	}


	/**
	 *  Creates objects for the global HashMap using put method only
	 */
	void createHashMapObjects() {
		getParamsHashMap().put("INIT_LISTENER_AOR", init_listener_aor);
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

		angleOfRot_Text.setNumberFormat(threshold_Text.getNumberFormat());

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
	}


	/**
	 *  Sets all fonts.
	 *
	 *@param  fnt  The new font
	 */
	void setFontForAll(Font fnt) {

		angle_label.setFont(fnt);
		vert_label.setFont(fnt);
		horz_label.setFont(fnt);

		angle_slider.setFont(fnt);
		vert_slider.setFont(fnt);
		horz_slider.setFont(fnt);

		angleOfRot_Text.setFont(fnt);
		angleOfRot_Label.setFont(fnt);

		alphaLocal_Text.setFont(fnt);
		betaLocal_Text.setFont(fnt);
		emtLocal_Text.setFont(fnt);
		alphaLocal_Label.setFont(fnt);
		betaLocal_Label.setFont(fnt);
		emtLocal_Label.setFont(fnt);
		copyEmtFromRMS_Button.setFont(fnt);
		copyEmtFromGAU_Button.setFont(fnt);

		localEmtParamBborder.setTitleFont(fnt);

		plotNormDens_Button.setFont(fnt);
	}

}


