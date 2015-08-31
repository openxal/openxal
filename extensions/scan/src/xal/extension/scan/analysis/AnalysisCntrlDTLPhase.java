package xal.extension.scan.analysis;

import java.awt.event.*;
import java.util.*;
import java.awt.*;
import java.text.*;
import javax.swing.*;
import javax.swing.border.*;
import java.net.*;
import java.io.*;

import xal.tools.data.DataAdaptor;
import xal.tools.apputils.*;
import xal.extension.application.Application;
import xal.extension.scan.*;
import xal.extension.widgets.apputils.SimpleChartPopupMenu;
import xal.extension.widgets.plot.*;
import xal.extension.widgets.swing.*;

/**
 *  This class is a DTL Phase Scan analysis.
 *
 *@author     A. Shishlo
 *@version    1.0
 */

public final class AnalysisCntrlDTLPhase extends AnalysisController {

	private JPanel dtlPS_AnalysisPanel = new JPanel();

	private String theoryWvsADataFileName = "NONE";
	private String theoryKSvsADataFileName = "NONE";

	//common part of the   dtlPS_AnalysisPanel
	private JPanel commonPanel = new JPanel();

	private JLabel designEnrgDevLabel = new JLabel("Energy Dlt [%] = ");
	private DoubleInputTextField designEnrgDevText = new DoubleInputTextField(8);

	private DecimalFormat ampFormat = new DecimalFormat("####.###");
	private DecimalFormat phaseFormat = new DecimalFormat("###.#");

	private JComboBox<String> operationChooser = null;

	//child panels
	private JPanel[] childControlPanels = new JPanel[2];
	private JPanel[] childGraphPanels = new JPanel[2];
	private String[] panelNameList = {"FIND WIDTH FOR 1D SCAN     ",
			"PLOT WIDTH VS. AMPLITUDE   "};

	//--------------------------------------------------
	//EXTERNAL DATA
	//--------------------------------------------------

	//vectors include BasicGraphData instances with
	//"ENERGY_DELTA" as properties key with delta energy in percent parameter
	private String ENERGY_DLT = "ENERGY_DELTA";
	private Vector<BasicGraphData> extWidthVsAmpDataV = new Vector<BasicGraphData>();
	private Vector<BasicGraphData> extAmpVsWidthDataV = new Vector<BasicGraphData>();

	//vectors include BasicGraphData instances with
	//k_shift vs (amp/design_amp) for different "ENERGY_DELTA"
	//as properties key with delta energy in percent parameter
	private Vector<BasicGraphData> extKShiftVsAmpDataV = new Vector<BasicGraphData>();
	private Vector<BasicGraphData> extAmpVsKShiftDataV = new Vector<BasicGraphData>();

	//--------------------------------------------------
	//PANEL #0  name = "FIND WIDTH FOR 1D SCAN     "
	//--------------------------------------------------
	private JLabel paramPV_Label = new JLabel(" Cavity Ampl.    :");
	private JLabel paramPV_RB_Label = new JLabel(" Cavity Ampl. RB :");

	private DoubleInputTextField paramPV_ValueText = new DoubleInputTextField(8);
	private DoubleInputTextField paramPV_RB_ValueText = new DoubleInputTextField(8);

	private JLabel widthP0_Label = new JLabel("Width [dgr] :");
	private JLabel guessAmpP0_Label = new JLabel("Guess Ampl  :");
	private JLabel guessPhaseP0_Label = new JLabel("Guess Phase :");

	private DoubleInputTextField widthP0_Text = new DoubleInputTextField(8);
	private DoubleInputTextField guessAmpP0_Text = new DoubleInputTextField(8);
	private DoubleInputTextField guessPhaseP0_Text = new DoubleInputTextField(8);

	private JButton findWidthP0_Button = new JButton("FIND WIDTH AND GUESS AMPL. & PHASE");
	private JButton setGuessAmpP0_Button = new JButton("SET GUESS AMPL. & PHASE TO CAVITY");

	private ActionListener graphChooserListener = null;

	private MouseAdapter graphChooserMouseAdapter = null;

	//--------------------------------------------------
	//PANEL #1  name = "PLOT WIDTH VS. AMPLITUDE   "
	//--------------------------------------------------

	//graph data with functions for certain value of the energy delta
	//from the predefined table:
	//width vs amplitude and
	//amplitude vs width
	private BasicGraphData gdP1_wFa = new BasicGraphData();
	private BasicGraphData gdP1_aFw = new BasicGraphData();

	//k_shift (ks) coeff vs. normalized amplitude
	//def. of k_shift :  phi_guess = phi_left + k_shift * ( phi_right - phi_left)
	private BasicGraphData gdP1_ksFa = new BasicGraphData();
	private BasicGraphData gdP1_aFks = new BasicGraphData();

	//graph data width vs amplitude from measured data
	private BasicGraphData gdP1_Exp_wFa = new BasicGraphData();

	//data with information (max in phase scan) vs normalize amplitude
	private BasicGraphData gdP1_maxVsA = new BasicGraphData();

	//GUI elements for panel #1
	private JLabel enrgDltP1_Label = new JLabel("Energy Dlt [%]   :");
	private JLabel guessAmpP1_Label = new JLabel("Guess Ampl       :");
	private JLabel guessPhaseP1_Label = new JLabel("Guess Phase [dgr]:");

	private DoubleInputTextField enrgDltP1_Text = new DoubleInputTextField(8);
	private DoubleInputTextField guessAmpP1_Text = new DoubleInputTextField(8);
	private DoubleInputTextField guessPhaseP1_Text = new DoubleInputTextField(8);

	private JButton setEnrgDltP1_Button = new JButton("MEMORIZE ENERGY DELTA");
	private JButton setGuessAmpP1_Button = new JButton("SET GUESS AMPL. & PHASE TO CAVITY");

	//graphs panels that are placed on PANEL #1 graph part
	private FunctionGraphsJPanel widthVsAmpGraph = new FunctionGraphsJPanel();
	private FunctionGraphsJPanel maxValVsAmpGraph = new FunctionGraphsJPanel();

	//vertical line listener
	private ActionListener dragVerLine_Listener = null;


	/**
	 *  The constructor.
	 *
	 *@param  mainController_In         Description of the Parameter
	 *@param  analysisConf              Description of the Parameter
	 *@param  parentAnalysisPanel_In    Description of the Parameter
	 *@param  customControlPanel_In     Description of the Parameter
	 *@param  customGraphPanel_In       Description of the Parameter
	 *@param  globalButtonsPanel_In     Description of the Parameter
	 *@param  scanVariableParameter_In  Description of the Parameter
	 *@param  scanVariable_In           Description of the Parameter
	 *@param  measuredValuesV_In        Description of the Parameter
	 *@param  graphAnalysis_In          Description of the Parameter
	 *@param  messageTextLocal_In       Description of the Parameter
	 *@param  graphDataLocal_In         Description of the Parameter
	 */
	public AnalysisCntrlDTLPhase(MainAnalysisController mainController_In,
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

		String nameIn = "DTL PHASE SCAN";
		DataAdaptor nameDA =  analysisConf.childAdaptor("ANALYSIS_NAME");
		if (nameDA != null) {
			nameIn = nameDA.stringValue("name");
		}
		setName(nameIn);

		DataAdaptor designEnrgDA =  analysisConf.childAdaptor("DESIGN_ENERGY_DELTA");
		double designEnrg_tmp = 0.0;
		if (designEnrgDA != null) {
			designEnrg_tmp = designEnrgDA.doubleValue("value");
		}

		DataAdaptor theoryDataDA =  analysisConf.childAdaptor("THEORY_SCAN_DATA");
		if (theoryDataDA != null) {
			DataAdaptor theoryDataDA_dphi_vs_amp =  theoryDataDA.childAdaptor("DPHI_VS_AMP");
			DataAdaptor theoryDataDA_kShift_vs_amp =  theoryDataDA.childAdaptor("KSHIFT_VS_AMP");
			if (theoryDataDA_dphi_vs_amp != null && theoryDataDA_kShift_vs_amp != null) {
				readTheoryData(theoryDataDA_dphi_vs_amp.stringValue("file_name"),
						theoryDataDA_kShift_vs_amp.stringValue("file_name"));
			}
		}

		//create main panel
		dtlPS_AnalysisPanel.setLayout(new BorderLayout());

		//==================================================
		//create common panel
		//==================================================
		SimpleChartPopupMenu.addPopupMenuTo(widthVsAmpGraph);
		SimpleChartPopupMenu.addPopupMenuTo(maxValVsAmpGraph);

		commonPanel.setLayout(new BorderLayout());

		Border etchedBorder = BorderFactory.createEtchedBorder();
		commonPanel.setBorder(etchedBorder);
		commonPanel.setBackground(commonPanel.getBackground().darker());

		designEnrgDevText.setNormalBackground(Color.white);
		designEnrgDevText.setNumberFormat(ampFormat);
		designEnrgDevText.setHorizontalAlignment(JTextField.CENTER);
		designEnrgDevText.setValue(designEnrg_tmp);

		JPanel tmp_0 = new JPanel();
		tmp_0.setLayout(new GridLayout(1, 2, 1, 1));
		tmp_0.add(designEnrgDevLabel);
		tmp_0.add(designEnrgDevText);

		operationChooser = new JComboBox<>( panelNameList );
		operationChooser.setBackground(Color.cyan);
		operationChooser.addActionListener(
			new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					int index = operationChooser.getSelectedIndex();
					showPanel(index);
				}
			});

		commonPanel.add(tmp_0, BorderLayout.NORTH);
		commonPanel.add(operationChooser, BorderLayout.SOUTH);

		//create PANEL #0 name = "FIND WIDTH FOR 1D SCAN     "
		createPanelFIND_WIDTH();

		//create PANEL #1 name = "PLOT WIDTH VS. AMPLITUDE   "
		createPanelWIDTH_VS_AMP();

		//create listener for vertical line - phase marker
		dragVerLine_Listener =
			new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					int ind = graphAnalysis.getDraggedLineIndex();
					double markerPos = graphAnalysis.getVerticalValue(ind);
					double phase_shift = MainAnalysisController.getPhaseShift(graphAnalysis.getAllGraphData());
					markerPos -= phase_shift;
					if (phase_shift != 0.) {
						markerPos += 180.;
						while (markerPos < 0.) {
							markerPos += 360.;
						}
						markerPos = markerPos % 360.;
						markerPos -= 180.;
					}
					guessPhaseP0_Text.setValueQuietly(markerPos);
					guessPhaseP1_Text.setValueQuietly(markerPos);
				}
			};

		guessPhaseP0_Text.addActionListener(
			new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					graphAnalysis.addDraggedVerLinesListener(null);
					double markerPos = guessPhaseP0_Text.getValue();
					guessPhaseP1_Text.setValueQuietly(markerPos);
					double phase_shift = MainAnalysisController.getPhaseShift(graphAnalysis.getAllGraphData());
					markerPos += phase_shift;
					if (phase_shift != 0.) {
						markerPos += 180.;
						while (markerPos < 0.) {
							markerPos += 360.;
						}
						markerPos = markerPos % 360.;
						markerPos -= 180.;
					}
					graphAnalysis.setVerticalLineValue(markerPos, 0);
					graphAnalysis.addDraggedVerLinesListener(dragVerLine_Listener);
				}
			});

		graphAnalysis.addDraggedVerLinesListener(null);
		graphAnalysis.removeVerticalValue(0);

	}


	/**
	 *  Sets the configurations of the analysis.
	 *
	 *@param  analysisConfig  Description of the Parameter
	 */
	public void dumpAnalysisConfig(DataAdaptor analysisConfig) {
		super.dumpAnalysisConfig(analysisConfig);

		DataAdaptor designEnrgDA =  analysisConfig.createChild("DESIGN_ENERGY_DELTA");
		designEnrgDA.setValue("value", designEnrgDevText.getValue());

		DataAdaptor theoryDataDA =  analysisConfig.createChild("THEORY_SCAN_DATA");

		DataAdaptor theoryDataDA_dphi_vs_amp =  theoryDataDA.createChild("DPHI_VS_AMP");
		theoryDataDA_dphi_vs_amp.setValue("file_name", theoryWvsADataFileName);

		DataAdaptor theoryDataDA_kShift_vs_amp =  theoryDataDA.createChild("KSHIFT_VS_AMP");
		theoryDataDA_kShift_vs_amp.setValue("file_name", theoryKSvsADataFileName);

	}


	/**
	 *  Sets fonts for all GUI elements.
	 *
	 *@param  fnt  The new fontsForAll value
	 */
	public void setFontsForAll(Font fnt) {
		super.setFontsForAll(fnt);

		//common panel
		designEnrgDevLabel.setFont(fnt);
		designEnrgDevText.setFont(fnt);

		operationChooser.setFont(fnt);
		((JTextField) operationChooser.getEditor().getEditorComponent()).setFont(fnt);
		operationChooser.setPreferredSize(new Dimension(1, fnt.getSize() + 10));

		//panel #0
		paramPV_Label.setFont(fnt);
		paramPV_RB_Label.setFont(fnt);
		paramPV_ValueText.setFont(fnt);
		paramPV_RB_ValueText.setFont(fnt);
		widthP0_Label.setFont(fnt);
		guessAmpP0_Label.setFont(fnt);
		guessPhaseP0_Label.setFont(fnt);
		widthP0_Text.setFont(fnt);
		guessAmpP0_Text.setFont(fnt);
		guessPhaseP0_Text.setFont(fnt);
		findWidthP0_Button.setFont(fnt);
		setGuessAmpP0_Button.setFont(fnt);

		//panel #1
		enrgDltP1_Label.setFont(fnt);
		guessAmpP1_Label.setFont(fnt);
		guessPhaseP1_Label.setFont(fnt);
		enrgDltP1_Text.setFont(fnt);
		guessAmpP1_Text.setFont(fnt);
		guessPhaseP1_Text.setFont(fnt);
		setEnrgDltP1_Button.setFont(fnt);
		setGuessAmpP1_Button.setFont(fnt);
	}


	/**
	 *  Does what necessary for close this analysis window.
	 */
	public void ShutUp() {
		super.ShutUp();
		customControlPanel.removeAll();
		customGraphPanel.removeAll();
		graphAnalysis.addDraggedVerLinesListener(null);
		graphAnalysis.removeVerticalValue(0);

		graphAnalysis.addChooseListener(null);
		graphAnalysis.removeMouseListener(graphChooserMouseAdapter);
	}


	/**
	 *  Does what necessary for open this analysis window.
	 */
	public void ShowUp() {
		super.ShowUp();

		graphAnalysis.addVerticalLine(guessPhaseP0_Text.getValue(), Color.red);
		graphAnalysis.addDraggedVerLinesListener(dragVerLine_Listener);
		graphAnalysis.setDraggedVerLinesMotionListen(true);

		graphAnalysis.addChooseListener(graphChooserListener);
		graphAnalysis.addMouseListener(graphChooserMouseAdapter);

		//for panel #0
		childGraphPanels[0].removeAll();
		childGraphPanels[0].add(graphAnalysis, BorderLayout.CENTER);
		childGraphPanels[0].add(globalButtonsPanel, BorderLayout.SOUTH);

		showPanel(0);
	}


	/**
	 *  Updates data on the analysis graph panel.
	 */
	public void updateDataSetOnGraphPanel() {
		super.updateDataSetOnGraphPanel();
	}


	/**
	 *  Shows panel with certain index.
	 *
	 *@param  panelIndex  Description of the Parameter
	 */
	private void showPanel(int panelIndex) {
		customControlPanel.removeAll();
		customGraphPanel.removeAll();
		dtlPS_AnalysisPanel.removeAll();

		//clear the message text
		messageTextLocal.setText(null);

		operationChooser.setSelectedIndex(panelIndex);

		if (panelIndex == 0) {
			paramPV_ValueText.setText(null);
			paramPV_ValueText.setBackground(Color.white);
			paramPV_RB_ValueText.setText(null);
			paramPV_RB_ValueText.setBackground(Color.white);
		}

		if (panelIndex == 1) {

			double[] params = getBestAmpAndPhase();
			if (params != null) {
				guessAmpP1_Text.setValue(params[0]);
				enrgDltP1_Text.setValue(params[2]);
				double phase_shift = MainAnalysisController.getPhaseShift(graphAnalysis.getAllGraphData());
				double markerPos = params[3] - phase_shift;
				if (phase_shift != 0.) {
					markerPos += 180.;
					while (markerPos < 0.) {
						markerPos += 360.;
					}
					markerPos = markerPos % 360.;
					markerPos -= 180.;
				}
				guessPhaseP0_Text.setValue(markerPos);
				widthVsAmpGraph.refreshGraphJPanel();
				maxValVsAmpGraph.refreshGraphJPanel();
				messageTextLocal.setText("debug amp=" + ampFormat.format(params[0]) +
						" err=" + ampFormat.format(params[1]) +
						" enrgDlt=" + ampFormat.format(params[2]) +
						" phase=" + phaseFormat.format(params[3]) +
						" +- " + phaseFormat.format(params[4]) +
						"  phase_shift=" + phaseFormat.format(phase_shift));
			} else {
				Toolkit.getDefaultToolkit().beep();
				messageTextLocal.setText(null);
				messageTextLocal.setText("Do not have enough data for analysis.");
			}
		}

		dtlPS_AnalysisPanel.add(commonPanel, BorderLayout.NORTH);
		dtlPS_AnalysisPanel.add(childControlPanels[panelIndex], BorderLayout.CENTER);

		customControlPanel.add(dtlPS_AnalysisPanel, BorderLayout.NORTH);
		customGraphPanel.add(childGraphPanels[panelIndex], BorderLayout.CENTER);

		//repaint
		parentAnalysisPanel.validate();
		parentAnalysisPanel.repaint();
	}


	//create PANEL #0 name = "FIND WIDTH FOR 1D SCAN     "
	/**
	 *  Description of the Method
	 */
	private void createPanelFIND_WIDTH() {
		childGraphPanels[0] = new JPanel();
		childGraphPanels[0].setLayout(new BorderLayout());

		childControlPanels[0] = new JPanel();
		childControlPanels[0].setLayout(new BorderLayout());

		//GUI elements
		paramPV_ValueText.setEditable(false);
		paramPV_RB_ValueText.setEditable(false);

		paramPV_ValueText.setNumberFormat(ampFormat);
		paramPV_RB_ValueText.setNumberFormat(ampFormat);

		paramPV_ValueText.setHorizontalAlignment(JTextField.CENTER);
		paramPV_RB_ValueText.setHorizontalAlignment(JTextField.CENTER);

		paramPV_ValueText.removeInnerFocusListener();
		paramPV_RB_ValueText.removeInnerFocusListener();

		paramPV_ValueText.setText(null);
		paramPV_ValueText.setBackground(Color.white);
		paramPV_RB_ValueText.setText(null);
		paramPV_RB_ValueText.setBackground(Color.white);

		widthP0_Text.setEditable(false);
		guessAmpP0_Text.setEditable(false);
		guessPhaseP0_Text.setEditable(false);

		widthP0_Text.setNumberFormat(ampFormat);
		guessAmpP0_Text.setNumberFormat(ampFormat);
		guessPhaseP0_Text.setNumberFormat(phaseFormat);

		widthP0_Text.setHorizontalAlignment(JTextField.CENTER);
		guessAmpP0_Text.setHorizontalAlignment(JTextField.CENTER);
		guessPhaseP0_Text.setHorizontalAlignment(JTextField.CENTER);

		widthP0_Text.removeInnerFocusListener();
		guessAmpP0_Text.removeInnerFocusListener();
		guessPhaseP0_Text.removeInnerFocusListener();

		widthP0_Text.setText(null);
		widthP0_Text.setBackground(Color.white);
		guessAmpP0_Text.setText(null);
		guessAmpP0_Text.setBackground(Color.white);
		guessPhaseP0_Text.setText(null);
		guessPhaseP0_Text.setBackground(Color.white);

		findWidthP0_Button.setForeground(Color.blue);
		setGuessAmpP0_Button.setForeground(Color.blue);

		graphChooserListener =
			new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					Integer Ind = graphAnalysis.getGraphChosenIndex();
					if (Ind != null && Ind.intValue() >= 0) {
						int ind = Ind.intValue();
						BasicGraphData gd = graphAnalysis.getInstanceOfGraphData(ind);

						Double parD = (Double) gd.getGraphProperty("PARAMETER_VALUE");
						if (parD != null) {
							paramPV_ValueText.setValue(parD.doubleValue());
						} else {
							paramPV_ValueText.setText(null);
							paramPV_ValueText.setBackground(Color.white);
						}
						parD = (Double) gd.getGraphProperty("PARAMETER_VALUE_RB");
						if (parD != null) {
							paramPV_RB_ValueText.setValue(parD.doubleValue());
						} else {
							paramPV_RB_ValueText.setText(null);
							paramPV_RB_ValueText.setBackground(Color.white);
						}
					} else {
						paramPV_ValueText.setText(null);
						paramPV_ValueText.setBackground(Color.white);
						paramPV_RB_ValueText.setText(null);
						paramPV_RB_ValueText.setBackground(Color.white);
					}

					widthP0_Text.setText(null);
					widthP0_Text.setBackground(Color.white);
					guessAmpP0_Text.setText(null);
					guessAmpP0_Text.setBackground(Color.white);
					guessPhaseP0_Text.setText(null);
					guessPhaseP0_Text.setBackground(Color.white);

				}
			};

		graphChooserMouseAdapter =
			new MouseAdapter() {
				public void mouseClicked(MouseEvent e) {
					Integer Ind = graphAnalysis.getGraphChosenIndex();
					if (Ind == null || Ind.intValue() < 0) {
						paramPV_ValueText.setText(null);
						paramPV_ValueText.setBackground(Color.white);
						paramPV_RB_ValueText.setText(null);
						paramPV_RB_ValueText.setBackground(Color.white);
					}
				}
			};

		findWidthP0_Button.addActionListener(
			new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					BasicGraphData gd = mainController.getChoosenDraphData();
					if (gd != null) {
						Double[] resArr = findWidthAndPlot(gd);
						Double widthD = resArr[0];
						Double phaseLD = resArr[1];
						Double phaseRD = resArr[2];

						if (widthD != null && phaseLD != null && phaseRD != null) {
							double energyDlt = designEnrgDevText.getValue();
							makeForwardAndBackWardGraphs(energyDlt);
							//normalized amplitude from ampl_vs_width graph
							double newAmpNorm = gdP1_aFw.getValueY(widthD.doubleValue());
							double amp = ((Double) gd.getGraphProperty("PARAMETER_VALUE")).doubleValue();
							amp = amp / newAmpNorm;
							//get phase_guess
							double k_shift = gdP1_ksFa.getValueY(newAmpNorm);
							double phase_guess = phaseLD.doubleValue() + k_shift * (phaseRD.doubleValue() - phaseLD.doubleValue());
							double phase_shift = MainAnalysisController.getPhaseShift(gd);
							double markerPos = phase_guess - phase_shift;
							if (phase_shift != 0.) {
								markerPos += 180.;
								while (markerPos < 0.) {
									markerPos += 360.;
								}
								markerPos = markerPos % 360.;
								markerPos -= 180.;
							}
							//DEBUG print ----------------------------------------------------
							System.out.println("debug new point  newAmpNorm=" + ampFormat.format(newAmpNorm) +
									" Edlt=" + ampFormat.format(energyDlt) +
									" w=" + ampFormat.format(widthD.doubleValue()) +
									" amp/guessA=" + ampFormat.format(amp) +
									" phiL=" + ampFormat.format(phaseLD.doubleValue()) +
									" phiR=" + ampFormat.format(phaseRD.doubleValue()) +
									" phi=" + ampFormat.format(phase_guess) +
									" k_s=" + ampFormat.format(k_shift) +
									" phase_shift=" + ampFormat.format(phase_shift));
							//DEBUG print ----------------------------------------------------

							guessAmpP0_Text.setValue(amp);
							guessPhaseP0_Text.setValue(markerPos);
						}
					} else {
						widthP0_Text.setText(null);
						widthP0_Text.setBackground(Color.white);
						guessAmpP0_Text.setText(null);
						guessAmpP0_Text.setBackground(Color.white);
						Toolkit.getDefaultToolkit().beep();
						messageTextLocal.setText(null);
						messageTextLocal.setText("Please choose the graph first. Use S-button on the graph panel.");
					}
				}
			});

		setGuessAmpP0_Button.addActionListener(
			new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					double ampVal = guessAmpP0_Text.getValue();
					double phaseVal = guessPhaseP0_Text.getValue();
					if (scanVariableParameter.getChannel() != null &&
							scanVariable.getChannel() != null) {
						scanVariableParameter.setValue(ampVal);
						scanVariable.setValue(phaseVal);
					} else {
						messageTextLocal.setText(null);
						messageTextLocal.setText("The parameter or Scan Variable PV channel does not exist.");
						Toolkit.getDefaultToolkit().beep();
					}
				}
			});

		JPanel tmp_0 = new JPanel();
		tmp_0.setLayout(new GridLayout(2, 2, 1, 1));

		Border etchedBorder = BorderFactory.createEtchedBorder();
		tmp_0.setBorder(etchedBorder);
		tmp_0.setBackground(tmp_0.getBackground().darker());

		tmp_0.add(paramPV_Label);
		tmp_0.add(paramPV_ValueText);
		tmp_0.add(paramPV_RB_Label);
		tmp_0.add(paramPV_RB_ValueText);

		JPanel tmp_1 = new JPanel();
		tmp_1.setLayout(new BorderLayout());
		tmp_1.setBorder(etchedBorder);
		tmp_1.setBackground(tmp_0.getBackground().darker());

		JPanel tmp_2 = new JPanel();
		tmp_2.setLayout(new GridLayout(3, 2, 1, 1));
		tmp_2.add(widthP0_Label);
		tmp_2.add(widthP0_Text);
		tmp_2.add(guessAmpP0_Label);
		tmp_2.add(guessAmpP0_Text);
		tmp_2.add(guessPhaseP0_Label);
		tmp_2.add(guessPhaseP0_Text);

		tmp_1.add(findWidthP0_Button, BorderLayout.NORTH);
		tmp_1.add(tmp_2, BorderLayout.CENTER);
		tmp_1.add(setGuessAmpP0_Button, BorderLayout.SOUTH);

		//add elements to analysis control child - 0
		childControlPanels[0].add(tmp_0, BorderLayout.NORTH);
		childControlPanels[0].add(tmp_1, BorderLayout.CENTER);

		//the graph panel will be done at the ShowUp() method
	}


	//create PANEL #1 name = "PLOT WIDTH VS. AMPLITUDE   "
	/**
	 *  Description of the Method
	 */
	private void createPanelWIDTH_VS_AMP() {
		childGraphPanels[1] = new JPanel();
		childGraphPanels[1].setLayout(new BorderLayout());

		childControlPanels[1] = new JPanel();
		childControlPanels[1].setLayout(new BorderLayout());

		//graph panels
		widthVsAmpGraph.setOffScreenImageDrawing(true);
		widthVsAmpGraph.setName("WIDTH vs. NORMALIZED AMPLITUDE");
		widthVsAmpGraph.setAxisNames("Ampl./Design Ampl", "Phase Width [grd]");
		widthVsAmpGraph.setGraphBackGroundColor(Color.white);
		widthVsAmpGraph.setLegendButtonVisible(true);
		widthVsAmpGraph.setLegendBackground(Color.white);
		widthVsAmpGraph.setLegendVisible(true);

		maxValVsAmpGraph.setOffScreenImageDrawing(true);
		maxValVsAmpGraph.setName("MAX. TRANSMISSION vs. NORMALIZED AMPLITUDE");
		maxValVsAmpGraph.setAxisNames("Ampl./Design Ampl", "Transmission");
		maxValVsAmpGraph.setGraphBackGroundColor(Color.white);
		maxValVsAmpGraph.setLegendButtonVisible(true);
		maxValVsAmpGraph.setLegendBackground(Color.white);
		maxValVsAmpGraph.setLegendVisible(true);

		//graph data properties
		widthVsAmpGraph.addGraphData(gdP1_wFa);
		widthVsAmpGraph.addGraphData(gdP1_Exp_wFa);

		maxValVsAmpGraph.addGraphData(gdP1_maxVsA);

		gdP1_wFa.setGraphProperty(graphAnalysis.getLegendKeyString(), "THEORY");
		gdP1_Exp_wFa.setGraphProperty(graphAnalysis.getLegendKeyString(), "MEASUREMENTS");
		gdP1_maxVsA.setGraphProperty(graphAnalysis.getLegendKeyString(), "MEASUREMENTS");

		gdP1_wFa.setImmediateContainerUpdate(false);
		gdP1_Exp_wFa.setImmediateContainerUpdate(false);
		gdP1_maxVsA.setImmediateContainerUpdate(false);

		gdP1_wFa.setDrawLinesOn(true);
		gdP1_wFa.setDrawPointsOn(false);
		gdP1_wFa.setLineThick(3);
		gdP1_wFa.setGraphColor(Color.blue);

		//text elements
		enrgDltP1_Text.setEditable(false);
		enrgDltP1_Text.setNumberFormat(ampFormat);
		enrgDltP1_Text.setHorizontalAlignment(JTextField.CENTER);
		enrgDltP1_Text.removeInnerFocusListener();
		enrgDltP1_Text.setText(null);
		enrgDltP1_Text.setBackground(Color.white);

		guessAmpP1_Text.setEditable(false);
		guessAmpP1_Text.setNumberFormat(ampFormat);
		guessAmpP1_Text.setHorizontalAlignment(JTextField.CENTER);
		guessAmpP1_Text.removeInnerFocusListener();
		guessAmpP1_Text.setText(null);
		guessAmpP1_Text.setBackground(Color.white);

		guessPhaseP1_Text.setEditable(false);
		guessPhaseP1_Text.setNumberFormat(phaseFormat);
		guessPhaseP1_Text.setHorizontalAlignment(JTextField.CENTER);
		guessPhaseP1_Text.removeInnerFocusListener();
		guessPhaseP1_Text.setText(null);
		guessPhaseP1_Text.setBackground(Color.white);

		setEnrgDltP1_Button.setForeground(Color.blue);
		setGuessAmpP1_Button.setForeground(Color.blue);

		setEnrgDltP1_Button.addActionListener(
			new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					double val = enrgDltP1_Text.getValue();
					if (enrgDltP1_Text.getText().length() > 0) {
						designEnrgDevText.setValue(val);
					}
				}
			});

		setGuessAmpP1_Button.addActionListener(
			new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					double ampVal = guessAmpP1_Text.getValue();
					double phaseVal = guessPhaseP0_Text.getValue();
					if (scanVariableParameter.getChannel() != null && scanVariable.getChannel() != null) {
						scanVariableParameter.setValue(ampVal);
						scanVariable.setValue(phaseVal);
					} else {
						messageTextLocal.setText(null);
						messageTextLocal.setText("The parameter PV channel does not exist.");
						Toolkit.getDefaultToolkit().beep();
					}
				}
			});

		JPanel tmp_0 = new JPanel();
		tmp_0.setLayout(new GridLayout(3, 2, 1, 1));

		Border etchedBorder = BorderFactory.createEtchedBorder();
		tmp_0.setBorder(etchedBorder);
		tmp_0.add(enrgDltP1_Label);
		tmp_0.add(enrgDltP1_Text);
		tmp_0.add(guessAmpP1_Label);
		tmp_0.add(guessAmpP1_Text);
		tmp_0.add(guessPhaseP1_Label);
		tmp_0.add(guessPhaseP1_Text);

		JPanel tmp_1 = new JPanel();
		tmp_1.setLayout(new BorderLayout());
		tmp_1.setBorder(etchedBorder);
		tmp_1.setBackground(tmp_0.getBackground().darker());

		tmp_1.add(setEnrgDltP1_Button, BorderLayout.NORTH);
		tmp_1.add(tmp_0, BorderLayout.CENTER);
		tmp_1.add(setGuessAmpP1_Button, BorderLayout.SOUTH);

		childControlPanels[1].add(tmp_1, BorderLayout.NORTH);

		//graph panel
		JPanel tmp_10 = new JPanel();
		tmp_10.setLayout(new GridLayout(2, 1, 1, 1));
		tmp_10.add(widthVsAmpGraph);
		tmp_10.add(maxValVsAmpGraph);
		childGraphPanels[1].add(tmp_10, BorderLayout.CENTER);
	}


	//find width, left and right phases and plot it
	/**
	 *  Description of the Method
	 *
	 *@param  gd  Description of the Parameter
	 *@return     Description of the Return Value
	 */
	private Double[] findWidthAndPlot(BasicGraphData gd) {

		Double[] resultArr = new Double[3];
		resultArr[0] = null;
		resultArr[1] = null;
		resultArr[2] = null;

		Double widthD = null;

		graphAnalysis.removeGraphData(graphDataLocal);
		graphDataLocal.removeAllPoints();

		if (gd != null && gd.getNumbOfPoints() > 0) {
			double[] x_cross = findWidth(gd);

			if (x_cross != null) {
				graphDataLocal.addPoint(gd.getMinX(), gd.getMinY());
				graphDataLocal.addPoint(x_cross[0], gd.getMinY());
				graphDataLocal.addPoint(x_cross[0] + 0.00000001, gd.getMaxY());
				graphDataLocal.addPoint(x_cross[1] - 0.00000001, gd.getMaxY());
				graphDataLocal.addPoint(x_cross[1], gd.getMinY());
				graphDataLocal.addPoint(gd.getMaxX(), gd.getMinY());

				widthP0_Text.setText(null);
				widthP0_Text.setValue(x_cross[1] - x_cross[0]);
				widthD = new Double(x_cross[1] - x_cross[0]);
				resultArr[0] = widthD;
				resultArr[1] = new Double(x_cross[0]);
				resultArr[2] = new Double(x_cross[1]);
				messageTextLocal.setText(null);
			} else {
				widthP0_Text.setText(null);
				widthP0_Text.setBackground(Color.white);
				guessAmpP0_Text.setText(null);
				guessAmpP0_Text.setBackground(Color.white);
				guessPhaseP0_Text.setText(null);
				guessPhaseP0_Text.setBackground(Color.white);
				Toolkit.getDefaultToolkit().beep();
				messageTextLocal.setText(null);
				messageTextLocal.setText("Can not find the width.");
			}
		} else {
			widthP0_Text.setText(null);
			widthP0_Text.setBackground(Color.white);
			guessAmpP0_Text.setText(null);
			guessAmpP0_Text.setBackground(Color.white);
			guessPhaseP0_Text.setText(null);
			guessPhaseP0_Text.setBackground(Color.white);
			Toolkit.getDefaultToolkit().beep();
			messageTextLocal.setText(null);
			messageTextLocal.setText("Can not find the width. Select the curve with N points != 0");
		}

		graphAnalysis.addGraphData(graphDataLocal);
		return resultArr;
	}


	//find width, returns array with left and right points
	/**
	 *  Description of the Method
	 *
	 *@param  gd  Description of the Parameter
	 *@return     Description of the Return Value
	 */
	private double[] findWidth(BasicGraphData gd) {

		double[] wArr = null;

		if (gd != null && gd.getNumbOfPoints() > 0) {
			double y_min = gd.getMinY();
			double y_max = gd.getMaxY();
			double y_avg = y_min + (y_max - y_min) / 2.0;
			int count = 0;
			int[] index_cross = new int[2];
			double y;
			double y1;

			for (int i = 0; i < (gd.getNumbOfPoints() - 1); i++) {
				y = gd.getY(i);
				y1 = gd.getY(i + 1);
				if (y_avg != y1 && (y_avg - y) * (y_avg - y1) <= 0.) {
					if (count < 2) {
						index_cross[count] = i;
					}
					count++;
				}
			}

			if (count == 2) {
				wArr = new double[2];
				double coef = (y_avg - gd.getY(index_cross[0])) / (gd.getY(index_cross[0] + 1) - gd.getY(index_cross[0]));
				wArr[0] = gd.getX(index_cross[0]) + coef * (gd.getX(index_cross[0] + 1) - gd.getX(index_cross[0]));

				coef = (y_avg - gd.getY(index_cross[1])) / (gd.getY(index_cross[1] + 1) - gd.getY(index_cross[1]));
				wArr[1] = gd.getX(index_cross[1]) + coef * (gd.getX(index_cross[1] + 1) - gd.getX(index_cross[1]));
			}
		}
		return wArr;
	}


	//calculate the best guess about design value of the cavity amplitude and phase
	//At this moment phase calculation is empty
	/**
	 *  Gets the bestAmpAndPhase attribute of the AnalysisCntrlDTLPhase object
	 *
	 *@return    The bestAmpAndPhase value
	 */
	private double[] getBestAmpAndPhase() {

		gdP1_wFa.removeAllPoints();
		gdP1_aFw.removeAllPoints();
		gdP1_Exp_wFa.removeAllPoints();
		gdP1_maxVsA.removeAllPoints();

		double[] results = null;
		Vector<BasicGraphData> gdV_tmp = graphAnalysis.getAllGraphData();
		BasicGraphData gd = null;
		Vector<BasicGraphData> gdV = new Vector<BasicGraphData>(20);
		double[] x_gr = null;
		for (int i = 0; i < gdV_tmp.size(); i++) {
			gd = gdV_tmp.get(i);
			x_gr = findWidth(gd);
			Double ampD = (Double) gd.getGraphProperty("PARAMETER_VALUE");
			if (x_gr != null && ampD != null) {
				gdV.add(gd);
				gd.setGraphProperty("PHASE_WIDTH", new Double(x_gr[1] - x_gr[0]));
				gd.setGraphProperty("PHASE_LEFT", new Double(x_gr[0]));
				gd.setGraphProperty("PHASE_RIGHT", new Double(x_gr[1]));
			}
		}

		int nMeasurements = gdV.size();

		if (nMeasurements < 2) {
			return results;
		}

		int nEnergies = extWidthVsAmpDataV.size();

		if (nEnergies <= 0) {
			return results;
		}

		double[] guessAmp = new double[nEnergies];
		double[] guessAmp2 = new double[nEnergies];
		for (int i = 0; i < nEnergies; i++) {
			guessAmp[i] = 0.;
			guessAmp2[i] = 0.;
		}

		for (int i = 0; i < nEnergies; i++) {
			double w;
			double amp;
			double ampG;
			double ampGNorm;
			BasicGraphData gdR = extAmpVsWidthDataV.get(i);
			for (int j = 0; j < nMeasurements; j++) {
				gd = gdV.get(j);
				w = ((Double) gd.getGraphProperty("PHASE_WIDTH")).doubleValue();
				amp = ((Double) gd.getGraphProperty("PARAMETER_VALUE")).doubleValue();
				ampGNorm = gdR.getValueY(w);
				ampG = amp / ampGNorm;
				guessAmp[i] += ampG;
				guessAmp2[i] += ampG * ampG;
			}
		}

		for (int i = 0; i < nEnergies; i++) {
			guessAmp[i] /= nMeasurements;
			guessAmp2[i] = Math.sqrt(Math.abs(guessAmp2[i] - nMeasurements * guessAmp[i] * guessAmp[i]));
			guessAmp2[i] *= Math.sqrt(1.0 / (nMeasurements * (nMeasurements - 1)));

			//DEBUG print ----------------------------------------------------
			//double enrgPar = ((Double) ((BasicGraphData) extAmpVsWidthDataV.get(i)).getGraphProperty(ENERGY_DLT)).doubleValue();
			//System.out.println("debug i=" + i +
			//		" delta[%]=" + ampFormat.format(enrgPar) +
			//		" guessAmp=" + guessAmp[i] +
			//		" err=" + guessAmp2[i]);
			//DEBUG print ----------------------------------------------------
		}

		double min_err = guessAmp2[0];
		int min_ind = 0;
		for (int i = 0; i < nEnergies; i++) {
			if (min_err > guessAmp2[i]) {
				min_err = guessAmp2[i];
				min_ind = i;
			}
		}

		double bestGuessAmp = guessAmp[min_ind];
		double bestGuessAmpErr = guessAmp2[min_ind];

		gd = extWidthVsAmpDataV.get(min_ind);
		double energyDlt = ((Double) gd.getGraphProperty(ENERGY_DLT)).doubleValue();

		gdP1_wFa.removeAllPoints();
		gdP1_aFw.removeAllPoints();

		gd = extWidthVsAmpDataV.get(min_ind);
		double x;
		double y;
		for (int i = 0; i < gd.getNumbOfPoints(); i++) {
			x = gd.getX(i);
			y = gd.getY(i);
			gdP1_wFa.addPoint(x, y);
			gdP1_aFw.addPoint(y, x);
		}

		gdP1_Exp_wFa.removeAllPoints();
		gdP1_maxVsA.removeAllPoints();

		double amp;

		double w;
		for (int j = 0; j < nMeasurements; j++) {
			gd = gdV.get(j);
			amp = ((Double) gd.getGraphProperty("PARAMETER_VALUE")).doubleValue();
			w = ((Double) gd.getGraphProperty("PHASE_WIDTH")).doubleValue();
			gdP1_Exp_wFa.addPoint(amp / bestGuessAmp, w);
			gdP1_maxVsA.addPoint(amp / bestGuessAmp, gd.getMaxY());
		}

		//calculate guess phase
		double guessPhase = 0.;
		double guessPhase2 = 0.;

		if (extKShiftVsAmpDataV.size() < 1) {
			return results;
		}

		int graph_ind = 0;
		gd = extKShiftVsAmpDataV.get(0);
		double energyDlt_tmp = ((Double) gd.getGraphProperty(ENERGY_DLT)).doubleValue();
		double energyDlt_nearest = Math.abs(energyDlt - energyDlt_tmp);
		for (int j = 0; j < extKShiftVsAmpDataV.size(); j++) {
			gd = extKShiftVsAmpDataV.get(j);
			energyDlt_tmp = ((Double) gd.getGraphProperty(ENERGY_DLT)).doubleValue();
			if (energyDlt_nearest > Math.abs(energyDlt - energyDlt_tmp)) {
				energyDlt_nearest = Math.abs(energyDlt - energyDlt_tmp);
				graph_ind = j;
			}
		}

		double k_shift = 0;
		double phi_left = 0.;
		double phi_right = 0.;
		double phase_tmp = 0.;

		for (int j = 0; j < nMeasurements; j++) {
			gd = gdV.get(j);
			amp = ((Double) gd.getGraphProperty("PARAMETER_VALUE")).doubleValue();
			phi_left = ((Double) gd.getGraphProperty("PHASE_LEFT")).doubleValue();
			phi_right = ((Double) gd.getGraphProperty("PHASE_RIGHT")).doubleValue();
			double phase_shift = MainAnalysisController.getPhaseShift(gd);
			gd = extKShiftVsAmpDataV.get(graph_ind);
			k_shift = gd.getValueY(amp / bestGuessAmp);
			phase_tmp = phi_left + k_shift * (phi_right - phi_left);
			//DEBUG print ----------------------------------------------------
			System.out.println("debug j=" + j +
					" amp=" + ampFormat.format(amp) +
					" delta[%]=" + ampFormat.format(energyDlt) +
					" guessAmp=" + ampFormat.format(bestGuessAmp) +
					" amp/guessAmp=" + ampFormat.format(amp / bestGuessAmp) +
					" phi_left=" + ampFormat.format(phi_left) +
					" phi_right=" + ampFormat.format(phi_right) +
					" phi=" + ampFormat.format(phase_tmp) +
					" k_shift=" + ampFormat.format(k_shift) +
					" phase_shift=" + ampFormat.format(phase_shift));
			//DEBUG print ----------------------------------------------------


			guessPhase += phase_tmp;
			guessPhase2 += phase_tmp * phase_tmp;
		}

		guessPhase /= nMeasurements;
		guessPhase2 = Math.sqrt(Math.abs(guessPhase2 - nMeasurements * guessPhase * guessPhase));
		guessPhase2 *= Math.sqrt(1.0 / (nMeasurements * (nMeasurements - 1)));

		//set results and return
		results = new double[5];
		results[0] = bestGuessAmp;
		results[1] = bestGuessAmpErr;
		results[2] = energyDlt;
		results[3] = guessPhase;
		results[4] = guessPhase2;

		return results;
	}


	//read theory data from data file
	/**
	 *  Description of the Method
	 *
	 *@param  fNameWvsA   Description of the Parameter
	 *@param  fNameKSvsA  Description of the Parameter
	 */
	private void readTheoryData(String fNameWvsA, String fNameKSvsA) {

		theoryWvsADataFileName = fNameWvsA;
		theoryKSvsADataFileName = fNameKSvsA;

		//============READ DPHI Vs. AMPLITUDE =====================================================

		URL dataURL = Application.getAdaptor().getResourceURL( "data/" + fNameWvsA );

		try {
			InputStream inps = dataURL.openStream();
			BufferedReader in = new BufferedReader(new InputStreamReader(inps));

			extWidthVsAmpDataV.clear();
			extAmpVsWidthDataV.clear();

			String lineIn = in.readLine();
			//this array includes n+1 tokens because first element is string with length=0
			String[] dataS = lineIn.split("[,\\s]+");

			for (int i = 0; i < dataS.length; i++) {
				if (dataS[i].length() > 0) {
					BasicGraphData gd = new BasicGraphData();
					gd.setGraphProperty(ENERGY_DLT, Double.valueOf(dataS[i]));
					extWidthVsAmpDataV.add(gd);
					//reverse data
					BasicGraphData gdR = new BasicGraphData();
					gdR.setGraphProperty(ENERGY_DLT, Double.valueOf(dataS[i]));
					extAmpVsWidthDataV.add(gdR);

				}
			}

			extWidthVsAmpDataV.remove(0);
			extAmpVsWidthDataV.remove(0);

			int nEnergyPoints = extWidthVsAmpDataV.size();

			lineIn = in.readLine();
			while (lineIn != null) {

				dataS = lineIn.split("[,\\s]+");
				if (dataS.length == (nEnergyPoints + 2)) {
					double x = Double.valueOf(dataS[1]).doubleValue();
					for (int i = 0; i < nEnergyPoints; i++) {
						double y = Double.valueOf(dataS[i + 2]).doubleValue();
						extWidthVsAmpDataV.get(i).addPoint(x, y);
						extAmpVsWidthDataV.get(i).addPoint(y, x);
					}
				} else {
					break;
				}

				lineIn = in.readLine();
			}

			in.close();

		} catch (IOException exception) {
			messageTextLocal.setText(null);
			messageTextLocal.setText("Fatal error. Can not open file =" + fNameWvsA +
					". Stop execution all analysis will be wrong");
		}

		//============READ k shift coeff. Vs. AMPLITUDE =====================================================

		dataURL = Application.getAdaptor().getResourceURL( "data/" + fNameKSvsA );

		try {
			InputStream inps = dataURL.openStream();
			BufferedReader in = new BufferedReader(new InputStreamReader(inps));

			extKShiftVsAmpDataV.clear();
			extAmpVsKShiftDataV.clear();

			String lineIn = in.readLine();
			//this array includes n+1 tokens because first element is string with length=0
			String[] dataS = lineIn.split("[,\\s]+");

			for (int i = 0; i < dataS.length; i++) {
				if (dataS[i].length() > 0) {
					BasicGraphData gd = new BasicGraphData();
					gd.setGraphProperty(ENERGY_DLT, Double.valueOf(dataS[i]));
					extKShiftVsAmpDataV.add(gd);
					//reverse data
					BasicGraphData gdR = new BasicGraphData();
					gdR.setGraphProperty(ENERGY_DLT, Double.valueOf(dataS[i]));
					extAmpVsKShiftDataV.add(gdR);

				}
			}

			extKShiftVsAmpDataV.remove(0);
			extAmpVsKShiftDataV.remove(0);

			int nEnergyPoints = extKShiftVsAmpDataV.size();

			lineIn = in.readLine();
			while (lineIn != null) {

				dataS = lineIn.split("[,\\s]+");
				if (dataS.length == (nEnergyPoints + 2)) {
					double x = Double.valueOf(dataS[1]).doubleValue();
					for (int i = 0; i < nEnergyPoints; i++) {
						double y = Double.valueOf(dataS[i + 2]).doubleValue();
						extKShiftVsAmpDataV.get(i).addPoint(x, y);
						extAmpVsKShiftDataV.get(i).addPoint(y, x);
					}
				} else {
					break;
				}

				lineIn = in.readLine();
			}

			in.close();

		} catch (IOException exception) {
			messageTextLocal.setText(null);
			messageTextLocal.setText("Fatal error. Can not open file =" + fNameKSvsA +
					". Stop execution all analysis will be wrong");
		}

	}


	//make predefined forward and backward graphs for width vs. amplitude
	/**
	 *  Description of the Method
	 *
	 *@param  energyDlt  Description of the Parameter
	 */
	private void makeForwardAndBackWardGraphs(double energyDlt) {

		//definition of amplitude vs width and backward
		gdP1_wFa.removeAllPoints();
		gdP1_aFw.removeAllPoints();

		BasicGraphData gd = extWidthVsAmpDataV.get(0);
		double energyDlt_grph = ((Double) gd.getGraphProperty(ENERGY_DLT)).doubleValue();
		double min_dev = Math.abs(energyDlt - energyDlt_grph);
		int index_grph = 0;
		for (int i = 0; i < extWidthVsAmpDataV.size(); i++) {
			gd = extWidthVsAmpDataV.get(i);
			energyDlt_grph = ((Double) gd.getGraphProperty(ENERGY_DLT)).doubleValue();
			double dev = Math.abs(energyDlt - energyDlt_grph);
			if (min_dev > dev) {
				min_dev = dev;
				index_grph = i;
			}
		}

		gd = extWidthVsAmpDataV.get(index_grph);
		double x;
		double y;
		for (int i = 0; i < gd.getNumbOfPoints(); i++) {
			x = gd.getX(i);
			y = gd.getY(i);
			gdP1_wFa.addPoint(x, y);
			gdP1_aFw.addPoint(y, x);
		}

		//definition of the k_shift (ks) coeff vs. normalized amplitude and backward
		gdP1_ksFa.removeAllPoints();
		gdP1_aFks.removeAllPoints();

		gd = extKShiftVsAmpDataV.get(0);
		energyDlt_grph = ((Double) gd.getGraphProperty(ENERGY_DLT)).doubleValue();
		min_dev = Math.abs(energyDlt - energyDlt_grph);
		index_grph = 0;
		for (int i = 0; i < extKShiftVsAmpDataV.size(); i++) {
			gd = extKShiftVsAmpDataV.get(i);
			energyDlt_grph = ((Double) gd.getGraphProperty(ENERGY_DLT)).doubleValue();
			double dev = Math.abs(energyDlt - energyDlt_grph);
			if (min_dev > dev) {
				min_dev = dev;
				index_grph = i;
			}
		}

		gd = extKShiftVsAmpDataV.get(index_grph);
		for (int i = 0; i < gd.getNumbOfPoints(); i++) {
			x = gd.getX(i);
			y = gd.getY(i);
			gdP1_ksFa.addPoint(x, y);
			gdP1_aFks.addPoint(y, x);
		}

	}

}

