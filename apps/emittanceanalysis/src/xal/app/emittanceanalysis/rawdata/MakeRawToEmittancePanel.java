package xal.app.emittanceanalysis.rawdata;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.text.*;
import java.util.*;
import javax.swing.border.*;
import javax.swing.event.*;
import java.awt.event.*;

import xal.extension.widgets.swing.*;
import xal.extension.widgets.plot.*;
import xal.extension.widgets.apputils.*;
import xal.tools.apputils.*;
import xal.tools.xml.*;
import xal.tools.text.*;

/**
 *  This is the sub-panel of the RawDataPanel. It includes several control
 *  panels and two color-surface graph sub-panels. This panel controls the
 *  initial emittance data generation.
 *
 *@author     A. Shishlo
 *@version    1.0
 */

public class MakeRawToEmittancePanel {

	private String xmlName = "MAKE_RAW_TO_EMITT_PANEL";

	//message text field
	private JTextField messageTextLocal = new JTextField();

	//panel by inself
	private JPanel panel = new JPanel();
	private TitledBorder border = null;

	//raw data object
	private WireRawData rawData = null;

	private ReadRawDataPanel readRawDataPanel = null;
	
	//filter raw data panel
	private FilterRawDataPanel filterRawDataPanel = null;

	//plot raw data panel
	private PlotRawDataPanel plotRawDataPanel = null;

	//wires' signal data
	private WireSignalData wireSignalData = new WireSignalData();

	//emittance raw data container - it is intermediate container that keeps emittance
	private EmittanceRawData emittanceRawData = new EmittanceRawData();

	//graph with 1D raw data panel
	private FunctionGraphsJPanel GP = null;

	//format for numbers of rows and channels
	//private DecimalFormat dbl_Format = new DecimalFormat("###0.0##");
	private NumberFormat dbl_Format = new ScientificNumberFormat(5);
	private DecimalFormat int_Format = new DecimalFormat("###0");

	//emittance data as ColorSurfaceData instance
	private ColorSurfaceData emittance3D = Data3DFactory.getData3D(1, 1, "linear");

	//emittance data as ColorSurfaceData instance (for analysis only)
	private ColorSurfaceData emittance3Da = Data3DFactory.getData3D(1, 1, "linear");
	//private ColorSurfaceData emittance3D = Data3DFactory.getData3D(1,1,"smooth");

	//temporary curve data to work with filter
	private CurveData tmp_CurveData = new CurveData();

	//parameters for diff. indexes
	//0 - MEBT emittance device
	//1 - DTL  emittance device
	//2 - new (fall 2004) MEBT device
	//3 - cvs input file for new (fall 2004) MEBT device
	private double[] harpSlitDist_arr = {102.5, 50.0, 35.3, 35.3};
	//in [cm]
	private double[] energy_arr = {2.5, 7.5, 2.5, 2.5};
	//in [MeV]
	private double[] wireStep_arr = {0.5, 0.7, 0.25, 1.0};
	//in [mm]

	private double[] gaussAmp_arr = {02.0, 0.0, 0.0, 0.0};
	//in [%]
	private double[] gaussWidth_arr = {16.0, 0.0, 0.0, 0.0};
	//in [mrad]
	private double[] threshold_arr = {0.0, 0.0, 0.0, 0.0};
	//threshold [%]

	private double harpSlitDist;
	private double energy;
	private double wireStep;
	private double gaussAmp;
	private double gaussWidth;
	private double threshold;
	private double gammaBeta;
	//velosity of particles param gamma*beta
	private double pMass = 938.78;
	// p- mass in MeV

	//-----------------------------------
	//GUI elements
	//-----------------------------------

	//1-st sub-panel
	private JRadioButton useFilter_Button = new JRadioButton("Use Filter");
	private JRadioButton useGraphData_Button = new JRadioButton("Use Graph Data");

	private JLabel useHarpPos_Label = new JLabel("Harp Pos.#:");
	private JSpinner useHarpPos_Spinner = new JSpinner(new SpinnerNumberModel(1, 1, 1, 1));

	private JButton makeWiresSignalPlot_Button = new JButton("PLOT WIRES' SIGNALS");

	//2-nd sub-panel
	private JLabel distHS_Label = new JLabel("Harp-Slit [cm]:");
	private JLabel energy_Label = new JLabel("Energy [MeV]:");
	private JLabel distWW_Label = new JLabel("Wire-Wire [mm]:");

	private DoubleInputTextField distHS_Text = new DoubleInputTextField(9);
	private DoubleInputTextField energy_Text = new DoubleInputTextField(9);
	private DoubleInputTextField distWW_Text = new DoubleInputTextField(9);

	private JLabel gaussAmp_Label = new JLabel("Scat. Amp[%]");
	private JLabel gaussWidth_Label = new JLabel("Scat. W[mrad]");
	private JLabel threshold_Label = new JLabel("Threshold[%]");

	private DoubleInputTextField gaussAmp_Text = new DoubleInputTextField(9);
	private DoubleInputTextField gaussWidth_Text = new DoubleInputTextField(9);
	private DoubleInputTextField threshold_Text = new DoubleInputTextField(9);

	private JLabel emScrResX_Label = new JLabel("Scr. X");
	private JLabel emScrResY_Label = new JLabel("Scr. Y");
	private JLabel emSizeX_Label = new JLabel("Emit. X");
	private JLabel emSizeY_Label = new JLabel("Emit. Y");

	private JSpinner emScrResX_Spinner = new JSpinner(new SpinnerNumberModel(200, 0, 400, 25));
	private JSpinner emScrResY_Spinner = new JSpinner(new SpinnerNumberModel(200, 0, 400, 25));
	private JSpinner emSizeX_Spinner = new JSpinner(new SpinnerNumberModel(200, 0, 400, 25));
	private JSpinner emSizeY_Spinner = new JSpinner(new SpinnerNumberModel(200, 0, 400, 25));

	private int emScrResX = 200;
	private int emScrResY = 200;
	private int emSizeX = 200;
	private int emSizeY = 200;

	private JButton makeEmittPlot_Button = new JButton("PLOT EMITTANCE");
	private JButton dumpPhaseSpacePlot_Button = new JButton("EXPORT DISTRIBUTION FILE");

	private TitledBorder emmParamPanelBorder = null;

	private JLabel alphaEm_Label = new JLabel("Alpha");
	private JLabel betaEm_Label = new JLabel("Beta [mm/mrad]");
	private JLabel rmsEm_Label = new JLabel("Emit.[mm mrad]");

	private DoubleInputTextField alphaEm_Text = new DoubleInputTextField(8);
	private DoubleInputTextField betaEm_Text = new DoubleInputTextField(8);
	private DoubleInputTextField rmsEm_Text = new DoubleInputTextField(8);

	//3-rd sub-panel (editing wires signal)
	private JLabel posX_Label = new JLabel("Pos. #");
	private JLabel posY_Label = new JLabel("Chan. #");
	private JLabel value_Label = new JLabel("Signal");

	private JTextField posX_Text = null;
	private JTextField posY_Text = null;
	private JTextField value_Text = null;

	private JButton setZero_Button = new JButton("SET SIGNAL TO ZERO");
	private JButton restore_Button = new JButton("RESTORE");

	//center coordinates
	private JLabel posCenterX_Label = new JLabel("X cent. [mm]");
	private JLabel posCenterXP_Label = new JLabel("XP cent. [mrad]");
	private DoubleInputTextField  posCenterX_Text = new DoubleInputTextField(12);
	private DoubleInputTextField  posCenterXP_Text = new DoubleInputTextField(12);

	//wires signal plot graph panel
	private FunctionGraphsJPanel GP_sp = new FunctionGraphsJPanel();
	private JScrollBar scrollBar_sp = new JScrollBar(Scrollbar.HORIZONTAL, 0, 0, 0, 100);
	private LocalColorGenerator colorGen_sp = new LocalColorGenerator();

	//emittance plot graph panel
	private FunctionGraphsJPanel GP_ep = new FunctionGraphsJPanel();
	private JScrollBar scrollBar_ep = new JScrollBar(Scrollbar.HORIZONTAL, 0, 0, 0, 100);
	private LocalColorGenerator colorGen_ep = new LocalColorGenerator();

	private boolean debug_gauss_generation = false;

	private SortObject[] sortObjects = new SortObject[0];


	/**
	 *  Constructor for the MakeRawToEmittancePanel object
	 *
	 *@param  GP_in  The FunctionGraphsJPanel with waveform wire signals
	 */
	public MakeRawToEmittancePanel(FunctionGraphsJPanel GP_in) {

		GP = GP_in;

		//signal plot graph panel definition
		SimpleChartPopupMenu.addPopupMenuTo(GP_sp);
		GP_sp.setOffScreenImageDrawing(true);
		GP_sp.setGraphBackGroundColor(Color.black);
		GP_sp.setGridLinesVisibleX(false);
		GP_sp.setGridLinesVisibleY(false);

		GP_sp.setName("Wires' Signal Contour Plot");
		GP_sp.setAxisNames("position #", "channel #");
		GP_sp.setNumberFormatX(int_Format);
		GP_sp.setNumberFormatY(int_Format);

		GP_sp.setLimitsAndTicksX(0., 50., 2, 4);
		GP_sp.setLimitsAndTicksY(0., 5.0, 6, 4);

		posX_Text = GP_sp.getClickedPointObject().xValueText;
		posY_Text = GP_sp.getClickedPointObject().yValueText;
		value_Text = GP_sp.getClickedPointObject().zValueText;

		GP_sp.getClickedPointObject().xValueFormat = new DecimalFormat(int_Format.toPattern());
		GP_sp.getClickedPointObject().yValueFormat = new ScientificNumberFormat(2);
		//GP_sp.getClickedPointObject().zValueFormat.applyPattern(dbl_Format.toPattern());
		GP_sp.getClickedPointObject().zValueFormat = dbl_Format;
		GP_sp.getClickedPointObject().pointColor = Color.white;

		wireSignalData.getPlotData(0).setColorGenerator(colorGen_sp);
		GP_sp.setColorSurfaceData(wireSignalData.getPlotData(0));

		//emittance contour plot graph panel
		SimpleChartPopupMenu.addPopupMenuTo(GP_ep);
		GP_ep.setOffScreenImageDrawing(true);
		GP_ep.setGraphBackGroundColor(Color.black);
		GP_ep.setGridLinesVisibleX(false);
		GP_ep.setGridLinesVisibleY(false);

		GP_ep.setName("Emittance Contour Plot");
		GP_ep.setAxisNames("x, [mm]", "xp, [mrad]");
		//GP_ep.setNumberFormatX( dbl_Format );
		//GP_ep.setNumberFormatY( dbl_Format );

		//GP_ep.setLimitsAndTicksX( -10., 5., 4, 4 );
		//GP_ep.setLimitsAndTicksY( -10., 5., 4, 4 );

		emittance3D.setColorGenerator(colorGen_ep);
		GP_ep.setColorSurfaceData(emittance3D);

		emittance3D.setScreenResolution(emScrResX, emScrResY);
		emittance3D.setSize(emSizeX, emSizeY);

		emittance3Da.setScreenResolution(emScrResX, emScrResY);
		emittance3Da.setSize(emSizeX, emSizeY);

		useFilter_Button.setSelected(false);
		useGraphData_Button.setSelected(false);

		useHarpPos_Spinner.setAlignmentX(JSpinner.CENTER_ALIGNMENT);

		distHS_Label.setHorizontalAlignment(SwingConstants.CENTER);
		energy_Label.setHorizontalAlignment(SwingConstants.CENTER);
		distWW_Label.setHorizontalAlignment(SwingConstants.CENTER);

		distHS_Text.setNumberFormat(dbl_Format);
		energy_Text.setNumberFormat(dbl_Format);
		distWW_Text.setNumberFormat(dbl_Format);

		distHS_Text.setHorizontalAlignment(JTextField.CENTER);
		energy_Text.setHorizontalAlignment(JTextField.CENTER);
		distWW_Text.setHorizontalAlignment(JTextField.CENTER);

		distHS_Text.setBackground(Color.white);
		energy_Text.setBackground(Color.white);
		distWW_Text.setBackground(Color.white);

		distHS_Text.setEditable(false);
		energy_Text.setEditable(false);
		distWW_Text.setEditable(true);

		gaussAmp_Label.setHorizontalAlignment(SwingConstants.CENTER);
		gaussWidth_Label.setHorizontalAlignment(SwingConstants.CENTER);
		threshold_Label.setHorizontalAlignment(SwingConstants.CENTER);

		gaussAmp_Text.setNumberFormat(dbl_Format);
		gaussWidth_Text.setNumberFormat(dbl_Format);
		gaussWidth_Text.setEditable(false);
		gaussWidth_Text.setBackground(Color.white);
		threshold_Text.setNumberFormat(dbl_Format);

		gaussAmp_Text.setHorizontalAlignment(JTextField.CENTER);
		gaussWidth_Text.setHorizontalAlignment(JTextField.CENTER);
		threshold_Text.setHorizontalAlignment(JTextField.CENTER);

		gaussAmp_Text.setBackground(Color.white);
		gaussWidth_Text.setBackground(Color.white);
		threshold_Text.setBackground(Color.white);

		posX_Label.setHorizontalAlignment(SwingConstants.CENTER);
		posY_Label.setHorizontalAlignment(SwingConstants.CENTER);
		value_Label.setHorizontalAlignment(SwingConstants.CENTER);

		posX_Text.setHorizontalAlignment(JTextField.CENTER);
		posY_Text.setHorizontalAlignment(JTextField.CENTER);
		value_Text.setHorizontalAlignment(JTextField.CENTER);

		emScrResX_Label.setHorizontalAlignment(SwingConstants.CENTER);
		emScrResY_Label.setHorizontalAlignment(SwingConstants.CENTER);
		emSizeX_Label.setHorizontalAlignment(SwingConstants.CENTER);
		emSizeY_Label.setHorizontalAlignment(SwingConstants.CENTER);

		posCenterX_Label.setHorizontalAlignment(SwingConstants.CENTER);
		posCenterXP_Label.setHorizontalAlignment(SwingConstants.CENTER);

		posCenterX_Text.setHorizontalAlignment(JTextField.CENTER);
		posCenterXP_Text.setHorizontalAlignment(JTextField.CENTER);

		posCenterX_Text.setEditable(false);
		posCenterXP_Text.setEditable(false);

		posCenterX_Text.setText(null);
		posCenterXP_Text.setText(null);

		posCenterX_Text.setBackground(Color.white);
		posCenterXP_Text.setBackground(Color.white);

		posCenterX_Text.setNumberFormat(dbl_Format);
		posCenterXP_Text.setNumberFormat(dbl_Format);

		emScrResX_Spinner.setAlignmentX(JSpinner.CENTER_ALIGNMENT);
		emScrResY_Spinner.setAlignmentX(JSpinner.CENTER_ALIGNMENT);
		emSizeX_Spinner.setAlignmentX(JSpinner.CENTER_ALIGNMENT);
		emSizeY_Spinner.setAlignmentX(JSpinner.CENTER_ALIGNMENT);

		alphaEm_Label.setHorizontalAlignment(SwingConstants.CENTER);
		betaEm_Label.setHorizontalAlignment(SwingConstants.CENTER);
		rmsEm_Label.setHorizontalAlignment(SwingConstants.CENTER);

		alphaEm_Text.setNumberFormat(dbl_Format);
		betaEm_Text.setNumberFormat(dbl_Format);
		rmsEm_Text.setNumberFormat(dbl_Format);

		alphaEm_Text.setHorizontalAlignment(JTextField.CENTER);
		betaEm_Text.setHorizontalAlignment(JTextField.CENTER);
		rmsEm_Text.setHorizontalAlignment(JTextField.CENTER);

		alphaEm_Text.setEditable(false);
		betaEm_Text.setEditable(false);
		rmsEm_Text.setEditable(false);

		alphaEm_Text.setText(null);
		betaEm_Text.setText(null);
		rmsEm_Text.setText(null);

		alphaEm_Text.setBackground(Color.white);
		betaEm_Text.setBackground(Color.white);
		rmsEm_Text.setBackground(Color.white);

		//text actions definitions

		distHS_Text.addActionListener(
			new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					harpSlitDist = distHS_Text.getValue();
				}
			});

		energy_Text.addActionListener(
			new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					energy = energy_Text.getValue();
					double p = Math.sqrt((energy + pMass) * (energy + pMass) - pMass * pMass);
					gammaBeta = p / pMass;
				}
			});

		distWW_Text.addActionListener(
			new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					wireStep = distWW_Text.getValue();
				}
			});

		gaussAmp_Text.addActionListener(
			new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					gaussAmp = gaussAmp_Text.getValue();
				}
			});

		gaussWidth_Text.addActionListener(
			new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					gaussWidth = gaussWidth_Text.getValue();
				}
			});

		threshold_Text.addActionListener(
			new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					threshold = threshold_Text.getValue();
				}
			});

		//define spinner actions
		emScrResX_Spinner.addChangeListener(
			new ChangeListener() {
				public void stateChanged(ChangeEvent e) {
					emScrResX = ((Integer) emScrResX_Spinner.getValue()).intValue();
					if (emScrResX == 0) {
						emScrResX = 1;
					}
					emittance3D.setScreenResolution(emScrResX, emScrResY);
					emittance3Da.setScreenResolution(emScrResX, emScrResY);
					plotEmittanceData();
				}
			});

		emScrResY_Spinner.addChangeListener(
			new ChangeListener() {
				public void stateChanged(ChangeEvent e) {
					emScrResY = ((Integer) emScrResY_Spinner.getValue()).intValue();
					if (emScrResY == 0) {
						emScrResY = 1;
					}
					emittance3D.setScreenResolution(emScrResX, emScrResY);
					emittance3Da.setScreenResolution(emScrResX, emScrResY);
					plotEmittanceData();
				}
			});

		emSizeX_Spinner.addChangeListener(
			new ChangeListener() {
				public void stateChanged(ChangeEvent e) {
					emSizeX = ((Integer) emSizeX_Spinner.getValue()).intValue();
					if (emSizeX == 0) {
						emSizeX = 1;
					}
				}
			});

		emSizeY_Spinner.addChangeListener(
			new ChangeListener() {
				public void stateChanged(ChangeEvent e) {
					emSizeY = ((Integer) emSizeY_Spinner.getValue()).intValue();
					if (emSizeY == 0) {
						emSizeY = 1;
					}
				}
			});

		//define buttons actions

		useFilter_Button.addActionListener(
			new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					if (useFilter_Button.isSelected()) {
						useGraphData_Button.setSelected(false);
					}
				}
			});

		useGraphData_Button.addActionListener(
			new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					if (useGraphData_Button.isSelected()) {
						useFilter_Button.setSelected(false);
					}
				}
			});

		makeWiresSignalPlot_Button.setForeground(Color.blue.darker());
		//makeWiresSignalPlot_Button.setBackground( Color.cyan );
		makeWiresSignalPlot_Button.setBorder(BorderFactory.createRaisedBevelBorder());

		makeWiresSignalPlot_Button.addActionListener(
			new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					makeWiresSignalsData();
					plotWiresSignalsData();
				}
			});

		useHarpPos_Spinner.addChangeListener(
			new ChangeListener() {
				public void stateChanged(ChangeEvent e) {
					int posHarp = ((Integer) useHarpPos_Spinner.getValue()).intValue();
					wireSignalData.getPlotData(posHarp - 1).setColorGenerator(colorGen_sp);
					GP_sp.clearZoomStack();
					GP_sp.setColorSurfaceData(wireSignalData.getPlotData(posHarp - 1));
				}
			});

		makeEmittPlot_Button.setForeground(Color.blue.darker());
		dumpPhaseSpacePlot_Button.setForeground(Color.blue.darker());
		//makeEmittPlot_Button.setBackground( Color.cyan );
		makeEmittPlot_Button.setBorder(BorderFactory.createRaisedBevelBorder());
		dumpPhaseSpacePlot_Button.setBorder(BorderFactory.createRaisedBevelBorder());

		makeEmittPlot_Button.addActionListener(
			new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					makeEmittanceData();
					plotEmittanceData();
				}
			});

		dumpPhaseSpacePlot_Button.addActionListener(
			new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					dumpDistributionData();
				}
			});
		
		restore_Button.setForeground(Color.blue.darker());
		//restore_Button.setBackground( Color.cyan );
		restore_Button.setBorder(BorderFactory.createRaisedBevelBorder());

		restore_Button.addActionListener(
			new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					wireSignalData.restoreData3D();
					GP_sp.refreshGraphJPanel();
				}
			});

		setZero_Button.setForeground(Color.blue.darker());
		//setZero_Button.setBackground( Color.cyan );
		setZero_Button.setBorder(BorderFactory.createRaisedBevelBorder());

		setZero_Button.addActionListener(
			new ActionListener() {
				public void actionPerformed(ActionEvent e) {

					int nChan = wireSignalData.getChannelsNumber();
					int nPosSlit = wireSignalData.getPositionsNumberSlit();
					int nPosHarp = wireSignalData.getPositionsNumberHarp();
					int posHarp = ((Integer) useHarpPos_Spinner.getValue()).intValue();
					if (nPosSlit * nChan <= 1) {
						return;
					}

					int xMin = (int) (GP_sp.getCurrentMinX() + 0.5);
					int xMax = (int) (GP_sp.getCurrentMaxX() + 0.5);
					int yMin = (int) (GP_sp.getCurrentMinY() + 0.5);
					int yMax = (int) (GP_sp.getCurrentMaxY() + 0.5);
					if (xMin >= nPosSlit) {
						xMin = nPosSlit;
					}
					if (xMin < 0) {
						xMin = 0;
					}
					if (xMax >= nPosSlit) {
						xMax = nPosSlit;
					}
					if (xMax < 0) {
						xMax = 0;
					}

					if (yMin >= nChan) {
						yMin = nChan;
					}
					if (yMin < 0) {
						yMin = 0;
					}
					if (yMax >= nChan) {
						yMax = nChan;
					}
					if (yMax < 0) {
						yMax = 0;
					}

					for (int ip = xMin; ip < xMax; ip++) {
						for (int ic = yMin; ic < yMax; ic++) {
							wireSignalData.setValue(ip, posHarp - 1, ic, 0.);
						}
					}
					wireSignalData.getPlotData(posHarp - 1).calcMaxMinZ();
					GP_sp.clearZoomStack();
					GP_sp.setColorSurfaceData(wireSignalData.getPlotData(posHarp - 1));
					//GP_sp.refreshGraphJPanel();
				}
			});

		//Scroll Bars definitions
		scrollBar_sp.setBlockIncrement((scrollBar_sp.getMaximum() - scrollBar_sp.getMinimum()) / 10);
		scrollBar_sp.getModel().addChangeListener(
			new ChangeListener() {
				public void stateChanged(ChangeEvent e) {
					int i_val = scrollBar_sp.getValue();
					double val = ((double) i_val) / scrollBar_sp.getMaximum();
					colorGen_sp.setUpperLimit(val);
					int posHarp = ((Integer) useHarpPos_Spinner.getValue()).intValue();
					wireSignalData.getPlotData(posHarp - 1).setColorGenerator(colorGen_sp);
					GP_sp.refreshGraphJPanel();
				}
			});

		scrollBar_ep.setBlockIncrement((scrollBar_ep.getMaximum() - scrollBar_ep.getMinimum()) / 10);
		scrollBar_ep.getModel().addChangeListener(
			new ChangeListener() {
				public void stateChanged(ChangeEvent e) {
					int i_val = scrollBar_ep.getValue();
					double val = ((double) i_val) / scrollBar_ep.getMaximum();
					colorGen_ep.setUpperLimit(val);
					GP_ep.refreshGraphJPanel();
				}
			});

		scrollBar_sp.setValue(100);
		scrollBar_ep.setValue(100);

		//panel border
		Border etchedBorder = BorderFactory.createEtchedBorder();
		border = BorderFactory.createTitledBorder(etchedBorder, "plot emittance from raw data");
		panel.setBorder(border);
		panel.setLayout(new BorderLayout());
		panel.setBackground(panel.getBackground().darker());

		Border etchedBorderBlack = BorderFactory.createEtchedBorder(panel.getBackground().darker(), panel.getBackground().brighter());

		//1-st sub-panel
		JPanel tmp_panel_0_0 = new JPanel();
		tmp_panel_0_0.setLayout(new GridLayout(1, 3, 1, 1));

		JPanel tmp_panel_0_0_1 = new JPanel();
		tmp_panel_0_0_1.setLayout(new FlowLayout(FlowLayout.CENTER, 1, 1));
		tmp_panel_0_0_1.add(useHarpPos_Label);
		tmp_panel_0_0_1.add(useHarpPos_Spinner);

		tmp_panel_0_0.add(useFilter_Button);
		tmp_panel_0_0.add(useGraphData_Button);
		tmp_panel_0_0.add(tmp_panel_0_0_1);

		JPanel tmp_panel_0_1 = new JPanel();
		tmp_panel_0_1.setLayout(new FlowLayout(FlowLayout.CENTER, 1, 1));
		tmp_panel_0_1.add(makeWiresSignalPlot_Button);

		JPanel tmp_panel_0 = new JPanel();
		tmp_panel_0.setLayout(new BorderLayout());
		tmp_panel_0.setBorder(etchedBorderBlack);
		tmp_panel_0.add(tmp_panel_0_0, BorderLayout.NORTH);
		tmp_panel_0.add(tmp_panel_0_1, BorderLayout.CENTER);

		//2-nd sub-panel
		JPanel tmp_panel_1_0 = new JPanel();
		tmp_panel_1_0.setLayout(new GridLayout(2, 3, 1, 1));
		tmp_panel_1_0.setBorder(etchedBorderBlack);
		tmp_panel_1_0.add(distHS_Label);
		tmp_panel_1_0.add(energy_Label);
		tmp_panel_1_0.add(distWW_Label);
		tmp_panel_1_0.add(distHS_Text);
		tmp_panel_1_0.add(energy_Text);
		tmp_panel_1_0.add(distWW_Text);

		JPanel tmp_panel_1_1 = new JPanel();
		tmp_panel_1_1.setLayout(new GridLayout(2, 3, 1, 1));
		tmp_panel_1_1.setBorder(etchedBorderBlack);
		tmp_panel_1_1.add(gaussAmp_Label);
		tmp_panel_1_1.add(gaussWidth_Label);
		tmp_panel_1_1.add(threshold_Label);
		tmp_panel_1_1.add(gaussAmp_Text);
		tmp_panel_1_1.add(gaussWidth_Text);
		tmp_panel_1_1.add(threshold_Text);

		JPanel tmp_panel_1_2 = new JPanel();
		tmp_panel_1_2.setLayout(new GridLayout(2, 4, 1, 1));
		tmp_panel_1_2.setBorder(etchedBorderBlack);
		tmp_panel_1_2.add(emScrResX_Label);
		tmp_panel_1_2.add(emScrResY_Label);
		tmp_panel_1_2.add(emSizeX_Label);
		tmp_panel_1_2.add(emSizeY_Label);
		tmp_panel_1_2.add(emScrResX_Spinner);
		tmp_panel_1_2.add(emScrResY_Spinner);
		tmp_panel_1_2.add(emSizeX_Spinner);
		tmp_panel_1_2.add(emSizeY_Spinner);

		JPanel tmp_panel_1_3 = new JPanel();
		tmp_panel_1_3.setLayout(new GridLayout(3, 1, 2, 2));
		tmp_panel_1_3.setBorder(new javax.swing.border.EmptyBorder(2, 2, 2, 2));
		tmp_panel_1_3.add(tmp_panel_1_0);
		tmp_panel_1_3.add(tmp_panel_1_1);
		tmp_panel_1_3.add(tmp_panel_1_2);

		JPanel tmp_panel_1_4 = new JPanel();
		tmp_panel_1_4.setLayout(new FlowLayout(FlowLayout.CENTER, 1, 1));
		tmp_panel_1_4.add(makeEmittPlot_Button);

		JPanel tmp_panel_1_5 = new JPanel();
		tmp_panel_1_5.setLayout(new GridLayout(2, 3, 2, 2));
		emmParamPanelBorder = BorderFactory.createTitledBorder(etchedBorderBlack, "rms emittance parameters");
		tmp_panel_1_5.setBorder(emmParamPanelBorder);
		tmp_panel_1_5.add(alphaEm_Label);
		tmp_panel_1_5.add(betaEm_Label);
		tmp_panel_1_5.add(rmsEm_Label);
		tmp_panel_1_5.add(alphaEm_Text);
		tmp_panel_1_5.add(betaEm_Text);
		tmp_panel_1_5.add(rmsEm_Text);
		
		JPanel tmp_panel_1_6 = new JPanel();
		tmp_panel_1_6.setLayout(new FlowLayout(FlowLayout.CENTER, 1, 1));
		tmp_panel_1_6.add(dumpPhaseSpacePlot_Button);
		
		JPanel tmp_panel_1_7 = new JPanel();
		tmp_panel_1_7.setLayout(new BorderLayout());
		tmp_panel_1_7.add(tmp_panel_1_5, BorderLayout.NORTH);
		tmp_panel_1_7.add(tmp_panel_1_6, BorderLayout.CENTER);
		
		JPanel tmp_panel_1 = new JPanel();
		tmp_panel_1.setLayout(new BorderLayout());
		tmp_panel_1.setBorder(etchedBorderBlack);
		tmp_panel_1.add(tmp_panel_1_3, BorderLayout.NORTH);
		tmp_panel_1.add(tmp_panel_1_4, BorderLayout.CENTER);
		tmp_panel_1.add(tmp_panel_1_7, BorderLayout.SOUTH);

		//3-rd sub-panel (editing wires signal)
		JPanel tmp_panel_2_0 = new JPanel();
		tmp_panel_2_0.setLayout(new GridLayout(2, 3, 1, 1));
		tmp_panel_2_0.setBorder(new javax.swing.border.EmptyBorder(2, 2, 2, 2));
		tmp_panel_2_0.add(posX_Label);
		tmp_panel_2_0.add(posY_Label);
		tmp_panel_2_0.add(value_Label);
		tmp_panel_2_0.add(posX_Text);
		tmp_panel_2_0.add(posY_Text);
		tmp_panel_2_0.add(value_Text);

		JPanel tmp_panel_2_1 = new JPanel();
		tmp_panel_2_1.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 1));
		tmp_panel_2_1.add(setZero_Button);
		tmp_panel_2_1.add(restore_Button);

		JPanel tmp_panel_2 = new JPanel();
		tmp_panel_2.setLayout(new BorderLayout());
		tmp_panel_2.setBorder(etchedBorderBlack);
		tmp_panel_2.add(tmp_panel_2_0, BorderLayout.NORTH);
		tmp_panel_2.add(tmp_panel_2_1, BorderLayout.CENTER);

		//buttons panel
		JPanel tmp_buttons_panel = new JPanel();
		tmp_buttons_panel.setLayout(new VerticalLayout());
		tmp_buttons_panel.add(tmp_panel_0);
		tmp_buttons_panel.add(tmp_panel_2);
		tmp_buttons_panel.add(tmp_panel_1);

		//graphs panel
		JPanel tmp_graphs_panel = new JPanel();
		tmp_graphs_panel.setLayout(new GridLayout(1, 2, 5, 5));

		JPanel tmp_graph_0_panel = new JPanel();
		tmp_graph_0_panel.setLayout(new BorderLayout());
		tmp_graph_0_panel.setBorder(etchedBorderBlack);
		tmp_graph_0_panel.add(GP_sp, BorderLayout.CENTER);
		tmp_graph_0_panel.add(scrollBar_sp, BorderLayout.SOUTH);


		//upper part of the emittance graph panel
		JPanel tmp_graph_1_upp_panel = new JPanel();
		tmp_graph_1_upp_panel.setLayout(new GridLayout(2, 2, 2, 2));
		tmp_graph_1_upp_panel.add(posCenterX_Label);
		tmp_graph_1_upp_panel.add(posCenterXP_Label);
		tmp_graph_1_upp_panel.add(posCenterX_Text);
		tmp_graph_1_upp_panel.add(posCenterXP_Text);

		JPanel tmp_graph_1_panel = new JPanel();
		tmp_graph_1_panel.setLayout(new BorderLayout());
		tmp_graph_1_panel.setBorder(etchedBorderBlack);
		tmp_graph_1_panel.add(tmp_graph_1_upp_panel, BorderLayout.NORTH);
		tmp_graph_1_panel.add(GP_ep, BorderLayout.CENTER);
		tmp_graph_1_panel.add(scrollBar_ep, BorderLayout.SOUTH);

		tmp_graphs_panel.add(tmp_graph_0_panel);
		tmp_graphs_panel.add(tmp_graph_1_panel);

		panel.add(tmp_buttons_panel, BorderLayout.WEST);
		panel.add(tmp_graphs_panel, BorderLayout.CENTER);
	}


	/**
	 *  Sets the font for all elements on the panel
	 *
	 *@param  fnt  The new font
	 */
	public void setFontForAll(Font fnt) {
		border.setTitleFont(fnt);
		emmParamPanelBorder.setTitleFont(fnt);

		useFilter_Button.setFont(fnt);
		useGraphData_Button.setFont(fnt);
		useHarpPos_Label.setFont(fnt);
		useHarpPos_Spinner.setFont(fnt);
		((JSpinner.DefaultEditor) useHarpPos_Spinner.getEditor()).getTextField().setFont(fnt);

		makeWiresSignalPlot_Button.setFont(fnt);

		distHS_Label.setFont(fnt);
		energy_Label.setFont(fnt);
		distWW_Label.setFont(fnt);

		distHS_Text.setFont(fnt);
		energy_Text.setFont(fnt);
		distWW_Text.setFont(fnt);

		gaussAmp_Label.setFont(fnt);
		gaussWidth_Label.setFont(fnt);
		threshold_Label.setFont(fnt);

		emScrResX_Label.setFont(fnt);
		emScrResY_Label.setFont(fnt);
		emSizeX_Label.setFont(fnt);
		emSizeY_Label.setFont(fnt);

		emScrResX_Spinner.setFont(fnt);
		((JSpinner.DefaultEditor) emScrResX_Spinner.getEditor()).getTextField().setFont(fnt);
		emScrResY_Spinner.setFont(fnt);
		((JSpinner.DefaultEditor) emScrResY_Spinner.getEditor()).getTextField().setFont(fnt);
		emSizeX_Spinner.setFont(fnt);
		((JSpinner.DefaultEditor) emSizeX_Spinner.getEditor()).getTextField().setFont(fnt);
		emSizeY_Spinner.setFont(fnt);
		((JSpinner.DefaultEditor) emSizeY_Spinner.getEditor()).getTextField().setFont(fnt);

		gaussAmp_Text.setFont(fnt);
		gaussWidth_Text.setFont(fnt);
		threshold_Text.setFont(fnt);

		makeEmittPlot_Button.setFont(fnt);
		dumpPhaseSpacePlot_Button.setFont(fnt);

		alphaEm_Label.setFont(fnt);
		betaEm_Label.setFont(fnt);
		rmsEm_Label.setFont(fnt);

		alphaEm_Text.setFont(fnt);
		betaEm_Text.setFont(fnt);
		rmsEm_Text.setFont(fnt);

		posX_Label.setFont(fnt);
		posY_Label.setFont(fnt);
		value_Label.setFont(fnt);

		posX_Text.setFont(fnt);
		posY_Text.setFont(fnt);
		value_Text.setFont(fnt);

		posCenterX_Label.setFont(fnt);
		posCenterXP_Label.setFont(fnt);

		posCenterX_Text.setFont(fnt);
		posCenterXP_Text.setFont(fnt);

		setZero_Button.setFont(fnt);
		restore_Button.setFont(fnt);
	}


	/**
	 *  Returns the JPanel of this class
	 *
	 *@return    The panel with all elements inside
	 */
	public JPanel getJPanel() {
		return panel;
	}



	/**
	 *  Performs what has to be done after reading raw data
	 */
	void initAfterReading() {
		useHarpPos_Spinner.setModel(new SpinnerNumberModel(1, 1, rawData.getPositionsNumberHarp(), 1));
	}


	/**
	 *  Sets the wireRawData reference
	 *
	 *@param  rawDataIn  The wireRawData reference
	 */
	public void setWireRawData(WireRawData rawDataIn) {
		rawData = rawDataIn;
	}


	/**
	 *  Sets the filterRawDataPanel instance object
	 *
	 *@param  filterRawDataPanelIn  The filterRawDataPanel reference
	 */
	public void setFilterRawDataPanel(FilterRawDataPanel filterRawDataPanelIn) {
		filterRawDataPanel = filterRawDataPanelIn;
	}
	
	/**
	 *  Sets the readRawDataPanel instance object
	 *
	 *@param  readRawDataPanelIn  The readRawDataPanel reference
	 */
	public void setReadRawDataPanel(	ReadRawDataPanel readRawDataPanelIn) {
		readRawDataPanel = readRawDataPanelIn;
	}
	

	/**
	 *  Sets the plotRawDataPanel reference object
	 *
	 *@param  plotRawDataPanel  The plotRawDataPanel vreference
	 */
	public void setPlotRawDataPanel(PlotRawDataPanel plotRawDataPanel) {
		this.plotRawDataPanel = plotRawDataPanel;
	}


	/**
	 *  Sets the type of device
	 *
	 *@param  ind  The device type index
	 */
	public void setTypeDataIndex(int ind) {
		distHS_Text.setValue(harpSlitDist_arr[ind]);
		energy_Text.setValue(energy_arr[ind]);
		distWW_Text.setValue(wireStep_arr[ind]);

		distHS_Text.setBackground(Color.white);
		energy_Text.setBackground(Color.white);
		distWW_Text.setBackground(Color.white);

		gaussAmp_Text.setValue(gaussAmp_arr[ind]);
		gaussWidth_Text.setValue(gaussWidth_arr[ind]);
		gaussWidth_Text.setBackground(Color.white);
		threshold_Text.setValue(threshold_arr[ind]);
	}


	/**
	 *  Create wires' signals data for editing
	 */
	public void makeWiresSignalsData() {
		// the curveData from GP are packed in the vector: (chInd-1)+chMax*(posInd-1)
		//posSlitInd + nPosSlit*posHarpInd + nPosSlit*nPosHarp*chInd

		messageTextLocal.setText(null);

		int chMax = rawData.getChannelsNumber();
		int posMax = rawData.getPositionsNumberSlit();
		int posHMax = rawData.getPositionsNumberHarp();
		int nSamples = rawData.getSamplesNumber();

		if (chMax * posMax <= 0) {
			messageTextLocal.setText(null);
			messageTextLocal.setText("The raw data do not exist.");
			Toolkit.getDefaultToolkit().beep();
			return;
		}

		//return case - use data from graph panel
		if (useFilter_Button.isSelected() == false && useGraphData_Button.isSelected() == true) {
			Vector<CurveData> cdV = GP.getAllCurveData();
			if (cdV.size() != chMax * posMax * posHMax) {
				messageTextLocal.setText(null);
				messageTextLocal.setText("You have to plot all raw data if you want to use this feature.");
				Toolkit.getDefaultToolkit().beep();
				return;
			}
		}

		int[] smplLimits = plotRawDataPanel.getLimits();
		if (smplLimits[0] > nSamples || smplLimits[0] < 1) {
			smplLimits[0] = 1;
		}
		if (smplLimits[1] < 1) {
			smplLimits[1] = 1;
		}
		if (smplLimits[1] > nSamples) {
			smplLimits[1] = nSamples;
		}
		if (smplLimits[2] < 1) {
			smplLimits[2] = 1;
		}
		if (smplLimits[2] > nSamples) {
			smplLimits[2] = nSamples;
		}
		if (smplLimits[1] > smplLimits[2]) {
			int tmp = smplLimits[1];
			smplLimits[1] = smplLimits[2];
			smplLimits[2] = tmp;
		}
		smplLimits[0] = smplLimits[0] - 1;
		smplLimits[1] = smplLimits[1] - 1;
		smplLimits[2] = smplLimits[2] - 1;

		//set wireSignal data to zero and define sizes of arrays
		wireSignalData.setSizeParameters(posMax, posHMax, chMax);

		//screen resolution grid step
		setGridLimitsGP_sp(chMax, posMax);

		double val;
		double sum;
		double sum_bckg;

		for (int ip = 0; ip < posMax; ip++) {
			val = rawData.getSlitPos(ip);
			wireSignalData.setSlitPos(ip, val);
			for (int ih = 0; ih < posHMax; ih++) {
				val = rawData.getHarpPos(ip, ih);
				wireSignalData.setHarpPos(ip, ih, val);
			}
		}

		//case - will use WireRawData instance - rawData
		if (useFilter_Button.isSelected() == false && useGraphData_Button.isSelected() == false) {

			for (int ip = 0; ip < posMax; ip++) {
				for (int ih = 0; ih < posHMax; ih++) {
					for (int ic = 0; ic < chMax; ic++) {
						sum = 0.;
						sum_bckg = 0.;
						for (int is = 0; is < smplLimits[0]; is++) {
							sum_bckg += rawData.getValue(is, ip, ih, ic);
						}
						for (int is = smplLimits[1]; is <= smplLimits[2]; is++) {
							sum += rawData.getValue(is, ip, ih, ic);
						}
						if (smplLimits[0] > 0) {
							sum_bckg /= smplLimits[0];
						}
						if (smplLimits[2] - smplLimits[1] > 0) {
							sum /= (smplLimits[2] - smplLimits[1]);
						}
						sum = sum_bckg - sum;
						//if(sum < 0.) sum = 0.;
						wireSignalData.setValue(ip, ih, ic, sum);
					}
				}
			}

		}

		//case - use filter from
		if (useFilter_Button.isSelected() == true && useGraphData_Button.isSelected() == false) {

			for (int ip = 0; ip < posMax; ip++) {
				for (int ih = 0; ih < posHMax; ih++) {
					for (int ic = 0; ic < chMax; ic++) {
						tmp_CurveData.clear();
						for (int is = 0; is < nSamples; is++) {
							val = rawData.getValue(is, ip, ih, ic);
							tmp_CurveData.addPoint((double) is, val);
						}

						filterRawDataPanel.filterCurveData(tmp_CurveData);

						sum = 0.;
						sum_bckg = 0.;
						for (int is = 0; is < smplLimits[0]; is++) {
							sum_bckg += tmp_CurveData.getY(is);
						}
						for (int is = smplLimits[1]; is <= smplLimits[2]; is++) {
							sum += tmp_CurveData.getY(is);
						}
						if (smplLimits[0] > 0) {
							sum_bckg /= smplLimits[0];
						}
						if (smplLimits[2] - smplLimits[1] > 0) {
							sum /= (smplLimits[2] - smplLimits[1]);
						}
						sum = sum_bckg - sum;
						//if(sum < 0.) sum = 0.;
						wireSignalData.setValue(ip, ih, ic, sum);
					}
				}
			}

		}

		//case - use data from graph panel
		if (useFilter_Button.isSelected() == false && useGraphData_Button.isSelected() == true) {
			Vector<CurveData> cdV = GP.getAllCurveData();
			if (cdV.size() != chMax * posMax * posHMax) {
				messageTextLocal.setText(null);
				messageTextLocal.setText("You have to plot all raw data if you want to use this feature.");
				Toolkit.getDefaultToolkit().beep();
				return;
			}

			int ind;
			CurveData cd = null;

			for (int ip = 0; ip < posMax; ip++) {
				for (int ih = 0; ih < posHMax; ih++) {
					for (int ic = 0; ic < chMax; ic++) {
						ind = ip + posMax * ih + posMax * posHMax * ic;
						cd = cdV.get(ind);

						sum = 0.;
						sum_bckg = 0.;
						for (int is = 0; is < smplLimits[0]; is++) {
							sum_bckg += cd.getY(is);
						}
						for (int is = smplLimits[1]; is <= smplLimits[2]; is++) {
							sum += cd.getY(is);
						}
						if (smplLimits[0] > 0) {
							sum_bckg /= smplLimits[0];
						}
						if (smplLimits[2] - smplLimits[1] > 0) {
							sum /= (smplLimits[2] - smplLimits[1]);
						}
						sum = sum_bckg - sum;
						//if(sum < 0.) sum = 0.;
						wireSignalData.setValue(ip, ih, ic, sum);
					}
				}
			}
		}

		double maxZ = wireSignalData.getPlotData(0).getMaxZ();
		for (int ih = 1; ih < posHMax; ih++) {
			if (maxZ < wireSignalData.getPlotData(ih).getMaxZ()) {
				maxZ = wireSignalData.getPlotData(ih).getMaxZ();
			}
		}
		if (maxZ > 0.) {
			for (int ih = 0; ih < posHMax; ih++) {
				//the surface plot will reflect the real data
				//wireSignalData.getPlotData(ih).multiplyBy(1.0 / maxZ);
			}
		}
		for (int ih = 0; ih < posHMax; ih++) {
			wireSignalData.getPlotData(ih).calcMaxMinZ();
		}
		wireSignalData.memorizeData3D();

		int posHarp = ((Integer) useHarpPos_Spinner.getValue()).intValue();
		wireSignalData.getPlotData(posHarp - 1).setColorGenerator(colorGen_sp);

	}


	/**
	 *  Plots wires' signals data
	 */
	public void plotWiresSignalsData() {
		int posIndHarp = ((Integer) useHarpPos_Spinner.getValue()).intValue();
		GP_sp.setColorSurfaceData(wireSignalData.getPlotData(posIndHarp - 1));
		//GP_sp.refreshGraphJPanel();
	}


	/**
	 *  Sets the grid limits for signal color-surface graphs
	 *
	 *@param  chMax   The number of channels (vertical axis)
	 *@param  posMax  The number of slit positions (horizontal axis)
	 */
	private void setGridLimitsGP_sp(int chMax, int posMax) {
		//screen resolution grid step
		int screenStepX = 20;
		int screenStepY = 5;
		int screenX = posMax / screenStepX;
		if (posMax % screenStepX != 0) {
			screenX += 1;
		}
		screenX = screenX * screenStepX;
		int screenY = chMax / screenStepY;
		if (chMax % screenStepY != 0) {
			screenY += 1;
		}
		screenY = screenY * screenStepY;
		GP_sp.setLimitsAndTicksX(0., (double) screenStepX, screenX / screenStepX, 3);
		GP_sp.setLimitsAndTicksY(0., (double) screenStepY, screenY / screenStepY, 4);

		//set size
		int posHMax = wireSignalData.getPositionsNumberHarp();
		for (int ih = 0; ih < posHMax; ih++) {
			wireSignalData.getPlotData(ih).setScreenResolution(screenX, screenY);
		}
	}



	/**
	 *  Make emittance data
	 */
	private void makeEmittanceData() {

		alphaEm_Text.setText(null);
		betaEm_Text.setText(null);
		rmsEm_Text.setText(null);

		int nCh = wireSignalData.getChannelsNumber();
		int nPS = wireSignalData.getPositionsNumberSlit();
		int nPH = wireSignalData.getPositionsNumberHarp();
		if (nCh * nPS <= 1) {
			return;
		}

		emittanceRawData.resize(nPS, nCh * nPH);

		double val = 0.;
		double pos = 0.;
		double posH = 0.;
		double angl = 0.;

		if (sortObjects.length != nCh * nPH) {
			sortObjects = new SortObject[nCh * nPH];
			for (int i = 0; i < nCh * nPH; i++) {
				sortObjects[i] = new SortObject();
			}
		}

		int i_count = 0;

		//angle in mrad
		for (int ip = 0; ip < nPS; ip++) {
			pos = wireSignalData.getSlitPos(ip);
			i_count = 0;
			for (int ih = 0; ih < nPH; ih++) {
				posH = wireSignalData.getHarpPos(ip, ih);
				for (int ic = 0; ic < nCh; ic++) {
					angl = (posH - wireSignalData.getSlitPos(ip) -
							wireStep * ic) / (harpSlitDist * 10.0);
					val = wireSignalData.getValue(ip, ih, ic);
					angl = angl * 1000.0;
					sortObjects[i_count].setParam(val, angl);
					i_count++;
				}
			}

			Arrays.sort(sortObjects);

			for (int i = 0; i < nPH * nCh; i++) {
				angl = sortObjects[i].getAngle();
				val = sortObjects[i].getValue();
				emittanceRawData.setRawData(ip, i, pos, angl, val);
			}

		}

		emittanceRawData.setInitialized(true);
		emittance3D.setSize(emSizeX, emSizeY);
		emittance3Da.setSize(emSizeX, emSizeY);
		emittanceRawData.makeColorSurfaceData(emittance3D);
		emittanceRawData.makeColorSurfaceData(emittance3Da);

		int nX = emittance3D.getSizeX();
		int nY = emittance3D.getSizeY();

		//make emittance3D data without any threshold
		makeEmittanceData(emittance3D);
		makeEmittanceData(emittance3Da);

		//debug purpose only - generate Gaussian emittance
		//emt, alpha, beta
		if (debug_gauss_generation) {
			makeGaussianEmittance(0.1 / gammaBeta, 1.0, 2.0, emittance3D);
			makeGaussianEmittance(0.1 / gammaBeta, 1.0, 2.0, emittance3Da);
		}

		//apply threshold
		emittance3D.calcMaxMinZ();
		emittance3Da.calcMaxMinZ();
		double val_max = threshold * emittance3D.getMaxZ() / 100.0;
		for (int ix = 0; ix < nX; ix++) {
			for (int iy = 0; iy < nY; iy++) {
				val = emittance3D.getValue(ix, iy);
				if (val < val_max) {
					emittance3D.setValue(ix, iy, 0.);
				}
			}
		}

		emittance3D.calcMaxMinZ();

		//define grids on emittance graph
		double posMin = emittance3D.getMinX();
		double posMax = emittance3D.getMaxX();
		posMax = Math.max(Math.abs(posMax), Math.abs(posMin));

		double anglMin = emittance3D.getMinY();
		double anglMax = emittance3D.getMaxY();
		anglMax = Math.max(Math.abs(anglMax), Math.abs(anglMin));

		double stepPos = 5.0;
		// 5 mm
		double stepAngl = 5.0;
		// 5 mrad

		int nStepX = (int) (posMax / stepPos);
		nStepX += 1;

		int nStepY = (int) (anglMax / stepAngl);
		nStepY += 1;

		//GP_ep.setLimitsAndTicksX( -nStepX * stepPos, stepPos, 2 * nStepX, 4 );
		//GP_ep.setLimitsAndTicksY( -nStepY * stepAngl, stepAngl, 2 * nStepY, 4 );

		//calculate rms emittance

		double em_rms_x0 = 0;
		double nSum = 0.;
		double x_avg = 0.;
		double xp_avg = 0.;

		for (int ix = 0; ix < nX; ix++) {
			pos = emittance3D.getX(ix);
			for (int iy = 0; iy < nY; iy++) {
				angl = emittance3D.getY(iy);
				val = emittance3D.getValue(ix, iy);
				nSum += val;
				x_avg += val * pos;
				xp_avg += val * angl;
			}
		}

		if (nSum == 0.) {
			nSum = 1.0;
		}

		x_avg /= nSum;
		xp_avg /= nSum;

		double x2_avg = 0.;
		double xp2_avg = 0.;
		double x_xp_avg = 0.;

		for (int ix = 0; ix < nX; ix++) {
			pos = emittance3D.getX(ix) - x_avg;
			for (int iy = 0; iy < nY; iy++) {
				angl = emittance3D.getY(iy) - xp_avg;
				val = emittance3D.getValue(ix, iy);
				x2_avg += val * pos * pos;
				xp2_avg += val * angl * angl;
				x_xp_avg += val * angl * pos;
			}
		}

		x2_avg /= nSum;
		xp2_avg /= nSum;
		x_xp_avg /= nSum;

		em_rms_x0 = Math.sqrt(Math.abs(x2_avg * xp2_avg - x_xp_avg * x_xp_avg));

		if (x2_avg * xp2_avg - x_xp_avg * x_xp_avg < 0.) {
			em_rms_x0 = -em_rms_x0;
		}

		double em_rms_x0_n = em_rms_x0 * gammaBeta;
		double alpha = -x_xp_avg / em_rms_x0;
		double beta = x2_avg / em_rms_x0;

		alphaEm_Text.setValue(alpha);
		betaEm_Text.setValue(beta);
		rmsEm_Text.setValue(em_rms_x0_n);

		alphaEm_Text.setBackground(Color.white);
		betaEm_Text.setBackground(Color.white);
		rmsEm_Text.setBackground(Color.white);

		if (nSum != 0.) {
			emittance3D.multiplyBy(1.0 / nSum);
		}

		posCenterX_Text.setValue( x_avg  );
		posCenterXP_Text.setValue( xp_avg  );

		posCenterX_Text.setBackground(Color.white);
		posCenterXP_Text.setBackground(Color.white);

	}


	/**
	 *  Calculates emittance phase space without any thresholds.
	 *
	 *@param  emittance3D_ext  Description of the Parameter
	 */
	public void makeEmittanceData(ColorSurfaceData emittance3D_ext) {

		double val = 0.;
		double pos = 0.;
		double angl = 0.;

		emittance3D_ext.setSize(emSizeX, emSizeY);
		emittanceRawData.makeColorSurfaceData(emittance3D_ext);

		int nX = emittance3D_ext.getSizeX();
		int nY = emittance3D_ext.getSizeY();

		double val_max = 0.;
		double angl_max = 0.;

		//substract scattering
		if (gaussAmp != 0. && gaussWidth != 0.) {
			for (int ix = 0; ix < nX; ix++) {
				pos = emittance3D_ext.getX(ix);
				val_max = emittance3D_ext.getValue(ix, 0);
				angl_max = emittance3D_ext.getY(0);
				for (int iy = 0; iy < nY; iy++) {
					val = emittance3D_ext.getValue(ix, iy);
					if (val_max < val) {
						val_max = val;
						angl_max = emittance3D_ext.getY(iy);
					}
				}
				val_max = val_max * gaussAmp / 100.0;
				for (int iy = 0; iy < nY; iy++) {
					angl = emittance3D_ext.getY(iy);
					val = emittance3D_ext.getValue(ix, iy);
					val -= val_max * Math.exp(-(angl - angl_max) * (angl - angl_max) / (2.0 * gaussWidth * gaussWidth));
					emittance3D_ext.setValue(ix, iy, val);
				}
			}
		}
		emittance3D.calcMaxMinZ();
	}
	
	/**
	 *  This method will show the dialog to save the 2D distribution.
	 */	
	 public void dumpDistributionData(){
		 
		 DecimalFormat dataFormat = new DecimalFormat("0.00000E00");
		 
		 int NUMBERWIDTH = 12;
		 
		 EmittanceRawData data = emittanceRawData;
		 
		 if (data.isInitialized()) {	
			 File rawFile = readRawDataPanel.getFile();
			 File dataFile;
			 
			 
			 JFileChooser ch = new JFileChooser(rawFile);
			 ch.setDialogTitle("Save Emittance 2D Data");
			 if (rawFile != null) {
				 String path = rawFile.getAbsolutePath();
				 
				 dataFile = new File(path.substring(0, path.lastIndexOf(".")) + ".dat");
				 ch.setSelectedFile(dataFile);
			 }
			 
			 int returnVal = ch.showSaveDialog(panel);
			 
			 if (returnVal == JFileChooser.APPROVE_OPTION) {
				 dataFile = ch.getSelectedFile();
			 } else {
				 return;
			 }
			 
			 BufferedWriter bw = null;
			 try {
				 
				 bw = new BufferedWriter(new FileWriter(dataFile));
				 
				 
				 int nx = emittance3D.getSizeX();
				 int nxp = emittance3D.getSizeY();
				 
				 StringBuilder result = new StringBuilder();
				 result.append("# ");
				 result.append(" "+nx+" ");
				 for (int i = 0; i < nx; i++) {   
					 double x = emittance3D.getX(i);
					 spaceFormat(result, NUMBERWIDTH, dataFormat.format(x));
				 }		
				 result.append("\n");
				 result.append("# ");
				 result.append(" "+nxp+" ");
				 for (int i = 0; i < nxp; i++) {   
					 double y = emittance3D.getY(i);
					 spaceFormat(result, NUMBERWIDTH, dataFormat.format(y));
				 }			 
				 result.append("\n");
				 
				 for (int i = 0; i < nx; i++) { 
					 for (int j = 0; j < nxp; j++) { 
						 double val = emittance3D.getValue(i,j);
						 spaceFormat(result, NUMBERWIDTH, dataFormat.format(val));
					 }
					result.append("\n");
				 }
				 
				 bw.write(result.toString());
				 bw.close();		 
				 
			 } catch (Exception ex) {
				 return;
			 } 
		 }
	 }
	 
	 private static StringBuilder spaceFormat(StringBuilder sb, int size, String addedString) {
		 if (sb == null || addedString == null) {
			 return sb;
		 }
		 int strLen = addedString.length();
		 int spaces = size - strLen;
		 spaces = (spaces > 0) ? spaces : 0;
		 
		 switch (spaces) {
		 case 0:
			 break;
		 case 1:
			 sb.append(" ");
			 break;
		 case 2:
			 sb.append("  ");
			 break;
		 case 3:
			 sb.append("   ");
			 break;
		 default:
			 for (int i = 0; i < spaces; i++) {
				 sb.append(" ");
			 }
			 
		 }
		 sb.append(addedString);
		 return sb;
	 }

	/**
	 *  This method will generate gaussian distribution with specified parameters
	 *
	 *@param  emt_rms          The rms emittance
	 *@param  alpha            The alpha parameter
	 *@param  beta             The beta parameter
	 *@param  emittance3D_ext  The emittance 3D data
	 */
	public void makeGaussianEmittance(double emt_rms,
			double alpha,
			double beta,
			ColorSurfaceData emittance3D_ext) {

		double val = 0.;
		double pos = 0.;
		double angl = 0.;

		double gamma = (1.0 + alpha * alpha) / beta;

		emittance3D_ext.setSize(emSizeX, emSizeY);
		emittanceRawData.makeColorSurfaceData(emittance3D_ext);
		int nX = emittance3D_ext.getSizeX();
		int nY = emittance3D_ext.getSizeY();

		for (int ix = 0; ix < nX; ix++) {
			for (int iy = 0; iy < nY; iy++) {
				pos = emittance3D_ext.getX(ix);
				angl = emittance3D_ext.getY(iy);
				val = pos * pos * gamma + 2 * alpha * pos * angl + angl * angl * beta;
				val = -val / (2 * emt_rms);
				val = Math.exp(val);
				emittance3D_ext.setValue(ix, iy, val);
			}
		}
	}


	/**
	 *  Returns the gammaBeta value for choosen type of emittance device
	 *
	 *@return    The gammaBeta value
	 */
	public double getGammaBeta() {
		return gammaBeta;
	}



	/**
	 *  Refreshes the emittance plot.
	 */
	public void plotEmittanceData() {
		GP_ep.refreshGraphJPanel();
	}


	/**
	 *  Performs actions before displaying the panel
	 */
	public void goingShowUp() {
		emittance3D.setColorGenerator(colorGen_ep);
	}


	/**
	 *  Returns emittance data for analysis.
	 *
	 *@return    The emittance data for analysis
	 */
	public ColorSurfaceData getEmittanceData() {
		return emittance3Da;
	}


	/**
	 *  Returns the screen X and Y resolutions and emittance data size on positions
	 *  and angles.
	 *
	 *@return    The resolutions values packed in an array
	 */
	public int[] getResolutions() {
		int[] res = new int[4];
		res[0] = emScrResX;
		res[1] = emScrResY;
		res[2] = emSizeX;
		res[3] = emSizeY;
		return res;
	}


	/**
	 *  Sets the screen X and Y resolutions and emittance data size on positions
	 *  and angles.
	 *
	 *@param  emScrResX  The screen X-axis size
	 *@param  emScrResY  The screen Y-axis size
	 *@param  emSizeX    The emittance's coordinate resolution
	 *@param  emSizeY    The emittance's momentum resolution
	 */
	public void setResolutions(int emScrResX, int emScrResY, int emSizeX, int emSizeY) {
		emScrResX_Spinner.setValue(new Integer(emScrResX));
		emScrResY_Spinner.setValue(new Integer(emScrResY));
		emSizeX_Spinner.setValue(new Integer(emSizeX));
		emSizeY_Spinner.setValue(new Integer(emSizeY));
	}


	/**
	 *  Returns the string identifier in the XML structure.
	 *
	 *@return    The name of the XML sub-structure
	 */
	public String getNameXMLData() {
		return xmlName;
	}


	/**
	 *  Defines the XML data file.
	 *
	 *@param  rawDataPanelData  The XML data adapter to keep configuration
	 *      information
	 */
	public void dumpDataToXML(XmlDataAdaptor rawDataPanelData) {
		XmlDataAdaptor makeRawToEmittPanelData = (XmlDataAdaptor) rawDataPanelData.createChild(getNameXMLData());
		XmlDataAdaptor params = (XmlDataAdaptor) makeRawToEmittPanelData.createChild("PARAMS");
		params.setValue("useFilter", useFilter_Button.isSelected());
		params.setValue("useGraphData", useGraphData_Button.isSelected());
		params.setValue("distHS", distHS_Text.getValue());
		params.setValue("energy", energy_Text.getValue());
		params.setValue("distWW", distWW_Text.getValue());
		params.setValue("gaussAmp", gaussAmp_Text.getValue());
		params.setValue("gaussWidth", gaussWidth_Text.getValue());
		params.setValue("threshold", threshold_Text.getValue());
		params.setValue("emScrResX", emScrResX);
		params.setValue("emScrResY", emScrResY);
		params.setValue("emSizeX", emSizeX);
		params.setValue("emSizeY", emSizeY);
		wireSignalData.dumpDataToXML(makeRawToEmittPanelData);
	}


	/**
	 *  Configures the panel from the XML data file.
	 *
	 *@param  rawDataPanelData  The XML data adapter with configuration information
	 */
	public void setDataFromXML(XmlDataAdaptor rawDataPanelData) {
		XmlDataAdaptor makeRawToEmittPanelData = (XmlDataAdaptor) rawDataPanelData.childAdaptor(getNameXMLData());
		XmlDataAdaptor params = (XmlDataAdaptor) makeRawToEmittPanelData.childAdaptor("PARAMS");

		//set GUI values
		useFilter_Button.setSelected(params.booleanValue("useFilter"));
		useGraphData_Button.setSelected(params.booleanValue("useGraphData"));
		distHS_Text.setValue(params.doubleValue("distHS"));
		energy_Text.setValue(params.doubleValue("energy"));
		distWW_Text.setValue(params.doubleValue("distWW"));
		gaussAmp_Text.setValue(params.doubleValue("gaussAmp"));
		gaussWidth_Text.setValue(params.doubleValue("gaussWidth"));
		threshold_Text.setValue(params.doubleValue("threshold"));
		emScrResX_Spinner.setValue(new Integer(params.intValue("emScrResX")));
		emScrResY_Spinner.setValue(new Integer(params.intValue("emScrResY")));
		emSizeX_Spinner.setValue(new Integer(params.intValue("emSizeX")));
		emSizeY_Spinner.setValue(new Integer(params.intValue("emSizeY")));

		debug_gauss_generation = false;
		XmlDataAdaptor debug_gauss_genD = (XmlDataAdaptor) makeRawToEmittPanelData.childAdaptor("GAUSS_GENERATOR");
		if (debug_gauss_genD != null) {
			debug_gauss_generation = true;
		}

		//make wire signal data
		wireSignalData.setDataFromXML(makeRawToEmittPanelData);
		setGridLimitsGP_sp(wireSignalData.getChannelsNumber(), wireSignalData.getPositionsNumberSlit());

		//initialization of the harp position index spinner
		useHarpPos_Spinner.setModel(new SpinnerNumberModel(1, 1, wireSignalData.getPositionsNumberHarp(), 1));

		plotWiresSignalsData();

		//plot emitance data
		makeEmittanceData();
		plotEmittanceData();

	}


	/**
	 *  Returns the message text field.
	 *
	 *@return    The message text field
	 */
	public JTextField getMessageTextField() {
		return messageTextLocal;
	}


	/**
	 *  Sets the message text field.
	 *
	 *@param  messageTextLocal  The new message text field
	 */
	public void setMessageTextField(JTextField messageTextLocal) {
		this.messageTextLocal = messageTextLocal;
	}


	/**
	 *  Sets the initialization listener from analysis controller
	 *
	 *@param  al  The new action listener
	 */
	public void setInitializationAnalysisListener(ActionListener al) {
		makeEmittPlot_Button.addActionListener(al);
	}



	/**
	 *  Description of the Class
	 *
	 *@author    shishlo
	 */
	public class SortObject implements Comparable<SortObject> {

		private double value = 0.;
		private double angle = 0.;


		/**
		 *  Constructor for the SortObject object
		 */
		public SortObject() { }


		/**
		 *  Sets the param attribute of the SortObject object
		 *
		 *@param  valueIn  The new param value
		 *@param  angleIn  The new param value
		 */
		public void setParam(double valueIn, double angleIn) {
			value = valueIn;
			angle = angleIn;
		}


		/**
		 *  Gets the value attribute of the SortObject object
		 *
		 *@return    The value value
		 */
		public double getValue() {
			return value;
		}


		/**
		 *  Gets the angle attribute of the SortObject object
		 *
		 *@return    The angle value
		 */
		public double getAngle() {
			return angle;
		}


		/**
		 *  Description of the Method
		 *
		 *@param  o  Description of the Parameter
		 *@return    Description of the Return Value
		 */
		public int compareTo( final SortObject ob ) {
			if (angle < ob.getAngle()) {
				return -1;
			} else if (angle > ob.getAngle()) {
				return 1;
			}
			return 0;
		}

	}

}

