package xal.app.quadshaker;

import java.text.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.table.*;
import javax.swing.text.*;
import javax.swing.event.*;
import javax.swing.border.*;
import java.util.*;
import java.io.*;


import xal.ca.*;

import xal.extension.widgets.plot.*;
import xal.extension.widgets.swing.*;
import xal.extension.widgets.apputils.SimpleChartPopupMenu;
import xal.tools.apputils.*;
import xal.tools.text.ScientificNumberFormat;
import xal.tools.xml.*;

import xal.service.pvlogger.*;
import xal.tools.database.*;

import xal.smf.proxy.ElectromagnetPropertyAccessor;

import xal.smf.*;
import xal.smf.impl.*;
import xal.smf.impl.qualify.*;

import xal.model.*;
import xal.sim.scenario.*;
import xal.service.pvlogger.sim.PVLoggerDataSource;
import xal.model.probe.*;
//import xal.model.alg.resp.*;
//import xal.model.probe.resp.*;
import xal.model.probe.traj.*;
import xal.sim.sync.*;
//import xal.model.probe.resp.traj.*;
import xal.tools.beam.*;

import xal.extension.fit.lsm.*;

/**
 *  Description of the Class
 *
 *@author     shishlo
 */
public class OrbitCorrector {

	//main panel
	private JPanel OrbitCorrectorMainPanel = new JPanel();

	//Tables and List models for BPMs and Quads
	private QuadsTable quadsTableModel = null;

	//The correctors Vector for H(X) and V(Y)
	private Vector<Corr_Element> corrXV = new Vector<Corr_Element>();
	private Vector<Corr_Element> corrYV = new Vector<Corr_Element>();

	//the graphs panel to show the scan results
	private FunctionGraphsJPanel graphXPanel = new FunctionGraphsJPanel();
	private FunctionGraphsJPanel graphYPanel = new FunctionGraphsJPanel();

	private BasicGraphData graphPosXData = new BasicGraphData();
	private BasicGraphData graphPosYData = new BasicGraphData();

	private BasicGraphData graphCorrPosXData = new BasicGraphData();
	private BasicGraphData graphCorrPosYData = new BasicGraphData();

	//left panel elements
	private TitledBorder corrHorTableBorder = null;
	private TitledBorder corrVerTableBorder = null;

	private JTable corrHorTable = new JTable();
	private JTable corrVerTable = new JTable();
	private AbstractTableModel corrHorTableModel = null;
	private AbstractTableModel corrVerTableModel = null;

	private JButton selectCorrXButton = new JButton("Select All");
	private JButton unSelectCorrXButton = new JButton("Unselect All");
	private JButton selectCorrYButton = new JButton("Select All");
	private JButton unSelectCorrYButton = new JButton("Unselect All");

	//Controls elements
	private TitledBorder correctionControlBorder = null;

	private JButton memorizeCorrectorsButton = new JButton("Memorize Corrs. I");
	private JButton restoreCorrectorsButton = new JButton("Restore from Memory");

	private JButton findCorrectionXButton = new JButton("Find Hor. Correction");
	private JButton findCorrectionYButton = new JButton("Find Ver. Correction");

	private JButton applyCorrectionXButton = new JButton("Apply");
	private JButton applyCorrectionYButton = new JButton("Apply");

	private JButton dumpOrbitButton = new JButton("Dump Orbit");

	private DoubleInputTextField signX_TextField = new DoubleInputTextField(5);
	private DoubleInputTextField signY_TextField = new DoubleInputTextField(5);

	//PV Logger Id - defined by the ShakeAnalysis class
	private long pvLoggerID = 0L;

	//accelerator related objects
	private AcceleratorSeqCombo accSeq = null;

	//current format
	private ScientificNumberFormat frmt = new ScientificNumberFormat( 6, 10, false );

	//message text field. It is actually message text field from Window
	private JTextField messageTextLocal = new JTextField();

	/**
	 *  Constructor for the OrbitCorrector object
	 */
	public OrbitCorrector() {

		Border border = BorderFactory.createEtchedBorder();

		OrbitCorrectorMainPanel.setLayout(new BorderLayout());

		//define graph panel's properties
		SimpleChartPopupMenu.addPopupMenuTo(graphXPanel);
		graphXPanel.setOffScreenImageDrawing(true);
		graphXPanel.setName("Orbit X");
		graphXPanel.setAxisNames("Quad #", "X, mm");
		graphXPanel.setGraphBackGroundColor(Color.white);
		graphXPanel.setLegendButtonVisible(true);
		graphXPanel.setChooseModeButtonVisible(true);
		graphXPanel.setLegendBackground(Color.white);
		graphXPanel.setSmartGL(true);

		SimpleChartPopupMenu.addPopupMenuTo(graphYPanel);
		graphYPanel.setOffScreenImageDrawing(true);
		graphYPanel.setName("Orbit Y");
		graphYPanel.setAxisNames("Quad #", "Y, mm");
		graphYPanel.setGraphBackGroundColor(Color.white);
		graphYPanel.setLegendButtonVisible(true);
		graphYPanel.setChooseModeButtonVisible(true);
		graphYPanel.setLegendBackground(Color.white);
		graphYPanel.setSmartGL(true);

		graphPosXData.setDrawLinesOn(true);
		graphPosXData.setDrawPointsOn(true);
		graphPosXData.setGraphColor(Color.blue);
		graphPosXData.setGraphPointSize(7);
		graphPosXData.setLineThick(3);
		graphPosXData.setImmediateContainerUpdate(false);
		graphPosXData.setGraphProperty(graphXPanel.getLegendKeyString(), "Average Trajectory X");

		graphPosYData.setDrawLinesOn(true);
		graphPosYData.setDrawPointsOn(true);
		graphPosYData.setGraphColor(Color.red);
		graphPosYData.setGraphPointSize(7);
		graphPosYData.setLineThick(3);
		graphPosYData.setImmediateContainerUpdate(false);
		graphPosYData.setGraphProperty(graphXPanel.getLegendKeyString(), "Average Trajectory Y");

		graphCorrPosXData.setDrawLinesOn(true);
		graphCorrPosXData.setDrawPointsOn(false);
		graphCorrPosXData.setGraphColor(Color.black);
		graphCorrPosXData.setLineThick(3);
		graphCorrPosXData.setImmediateContainerUpdate(false);
		graphCorrPosXData.setGraphProperty(graphXPanel.getLegendKeyString(), "Corrected Trajectory X");

		graphCorrPosYData.setDrawLinesOn(true);
		graphCorrPosYData.setDrawPointsOn(false);
		graphCorrPosYData.setGraphColor(Color.black);
		graphCorrPosYData.setLineThick(3);
		graphCorrPosYData.setImmediateContainerUpdate(false);
		graphCorrPosYData.setGraphProperty(graphXPanel.getLegendKeyString(), "Corrected Trajectory Y");

		graphXPanel.addGraphData(graphPosXData);
		graphYPanel.addGraphData(graphPosYData);

		graphXPanel.addGraphData(graphCorrPosXData);
		graphYPanel.addGraphData(graphCorrPosYData);

		signX_TextField.setValue(1.0);
		signY_TextField.setValue(1.0);

		JPanel panel_innG = new JPanel(new GridLayout(2, 1, 0, 0));
		panel_innG.add(graphXPanel);
		panel_innG.add(graphYPanel);

		JPanel panel_G = new JPanel(new BorderLayout());
		panel_G.setBorder(border);
		panel_G.add(panel_innG, BorderLayout.CENTER);
		OrbitCorrectorMainPanel.add(panel_G, BorderLayout.CENTER);

		corrVerTableBorder = BorderFactory.createTitledBorder(border, "Horizontal Correctors");
		corrHorTableBorder = BorderFactory.createTitledBorder(border, "Vertical Correctors");
		correctionControlBorder = BorderFactory.createTitledBorder(border, "orbit correction");

		JPanel panel_StartCorr = new JPanel(new FlowLayout(FlowLayout.LEFT, 1, 1));
		panel_StartCorr.add(memorizeCorrectorsButton);
		panel_StartCorr.add(restoreCorrectorsButton);

		JPanel panel_Xcorr = new JPanel(new FlowLayout(FlowLayout.LEFT, 1, 1));
		panel_Xcorr.add(findCorrectionXButton);
		panel_Xcorr.add(applyCorrectionXButton);
		//panel_Xcorr.add(dumpOrbitButton);
		//panel_Xcorr.add(signX_TextField);

		JPanel panel_Ycorr = new JPanel(new FlowLayout(FlowLayout.LEFT, 1, 1));
		panel_Ycorr.add(findCorrectionYButton);
		panel_Ycorr.add(applyCorrectionYButton);
		//panel_Ycorr.add(signY_TextField);

		JPanel panel_corr = new JPanel(new GridLayout(2, 1, 1, 1));
		panel_corr.add(panel_Xcorr);
		panel_corr.add(panel_Ycorr);

		JPanel panel_Cntrl = new JPanel(new BorderLayout());
		panel_Cntrl.add(panel_StartCorr, BorderLayout.CENTER);
		panel_Cntrl.add(panel_corr, BorderLayout.SOUTH);
		panel_Cntrl.setBorder(correctionControlBorder);

		JPanel panel_lT = new JPanel(new GridLayout(2, 1, 1, 1));

		JPanel panel_l = new JPanel(new BorderLayout());
		panel_l.add(panel_Cntrl, BorderLayout.NORTH);
		panel_l.add(panel_lT, BorderLayout.CENTER);

		OrbitCorrectorMainPanel.add(panel_l, BorderLayout.WEST);

		JPanel panel_lT0 = new JPanel(new BorderLayout());
		JPanel panel_lT1 = new JPanel(new BorderLayout());

		panel_lT0.setBorder(corrVerTableBorder);
		panel_lT1.setBorder(corrHorTableBorder);

		panel_lT.add(panel_lT0);
		panel_lT.add(panel_lT1);

		JScrollPane scrollPane0 = new JScrollPane(corrHorTable);
		JScrollPane scrollPane1 = new JScrollPane(corrVerTable);
		panel_lT0.add(scrollPane0, BorderLayout.CENTER);
		panel_lT1.add(scrollPane1, BorderLayout.CENTER);

		JPanel panel_bT0 = new JPanel(new GridLayout(1, 2, 1, 1));
		JPanel panel_bT1 = new JPanel(new GridLayout(1, 2, 1, 1));
		panel_bT0.add(selectCorrXButton);
		panel_bT0.add(unSelectCorrXButton);
		panel_bT1.add(selectCorrYButton);
		panel_bT1.add(unSelectCorrYButton);

		panel_lT0.add(panel_bT0, BorderLayout.NORTH);
		panel_lT1.add(panel_bT1, BorderLayout.NORTH);

		corrHorTable.setPreferredScrollableViewportSize(new Dimension(1, 1));
		corrVerTable.setPreferredScrollableViewportSize(new Dimension(1, 1));

		memorizeCorrectorsButton.addActionListener(
			new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					clearGraphs();
					if(memorizeCurrents()) {
						restoreCorrectorsButton.setEnabled(true);
					} else {
						restoreCorrectorsButton.setEnabled(false);
					}
					corrHorTableModel.fireTableDataChanged();
					corrVerTableModel.fireTableDataChanged();
				}
			});

		restoreCorrectorsButton.addActionListener(
			new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					clearGraphs();
					restoreCurrents();
					corrHorTableModel.fireTableDataChanged();
					corrVerTableModel.fireTableDataChanged();
				}
			});
		restoreCorrectorsButton.setEnabled(false);

		//update graph data from analysis
		OrbitCorrectorMainPanel.addComponentListener(
			new ComponentAdapter() {
				public void componentShown(ComponentEvent e) {
					plotMeasuredOrbit();
				}
			});

		findCorrectionXButton.addActionListener(
			new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					messageTextLocal.setText(null);
					findCorrection(corrXV, 0);
					corrHorTableModel.fireTableDataChanged();
					corrVerTableModel.fireTableDataChanged();
					graphXPanel.refreshGraphJPanel();
					graphYPanel.refreshGraphJPanel();
				}
			});

		findCorrectionYButton.addActionListener(
			new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					messageTextLocal.setText(null);
					findCorrection(corrYV, 1);
					corrHorTableModel.fireTableDataChanged();
					corrVerTableModel.fireTableDataChanged();
					graphXPanel.refreshGraphJPanel();
					graphYPanel.refreshGraphJPanel();
				}
			});

		applyCorrectionXButton.addActionListener(
			new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					messageTextLocal.setText(null);
					applyCorrections(corrXV, 0);
					corrHorTableModel.fireTableDataChanged();
					corrVerTableModel.fireTableDataChanged();
				}
			});


		dumpOrbitButton.addActionListener(
			new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					JFileChooser ch = new JFileChooser();
					ch.setDialogTitle("Save orbit to ASCII");
					int returnVal = ch.showSaveDialog(OrbitCorrectorMainPanel);
					if (returnVal == JFileChooser.APPROVE_OPTION) {
						try {
							File dataFile = ch.getSelectedFile();
							BufferedWriter out = new BufferedWriter(new FileWriter(dataFile));
							out.write(""+pvLoggerID);
							out.newLine();
							for(int i = 0, n = quadsTableModel.getListModel().size(); i < n; i++) {
								Quad_Element quadElm = quadsTableModel.getListModel().get(i);
								if(quadElm.isActive()) {
									out.write(quadElm.getName());
									if(quadElm.isPosXReady()) {
										out.write(" " + frmt.format(quadElm.getPosX()) + " " + frmt.format(quadElm.getPosErrX()));
									} else {
										out.write(" nan   nan ");
									}
									if(quadElm.isPosYReady()) {
										out.write(" " + frmt.format(quadElm.getPosY()) + " " + frmt.format(quadElm.getPosErrY()));
									}	else {
										out.write(" nan   nan ");
									}
									out.write(" " );
									out.newLine();
								}
							}
							out.flush();
							out.close();
						} catch (IOException exp) {
							Toolkit.getDefaultToolkit().beep();
							System.out.println(exp.toString());
						}
					}
				}
			});


		applyCorrectionYButton.addActionListener(
			new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					messageTextLocal.setText(null);
					applyCorrections(corrYV, 1);
					corrHorTableModel.fireTableDataChanged();
					corrVerTableModel.fireTableDataChanged();
				}
			});

		//define tables models
		defineTableModels();

		//select and unselect buttons actions
		selectCorrXButton.addActionListener(
			new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					for(int i = 0, n = corrXV.size(); i < n; i++) {
						Corr_Element corrElm =  corrXV.get(i);
						corrElm.setActive(true);
					}
					corrHorTableModel.fireTableDataChanged();
				}
			});

		selectCorrYButton.addActionListener(
			new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					for(int i = 0, n = corrYV.size(); i < n; i++) {
						Corr_Element corrElm =  corrYV.get(i);
						corrElm.setActive(true);
					}
					corrVerTableModel.fireTableDataChanged();
				}
			});

		unSelectCorrXButton.addActionListener(
			new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					for(int i = 0, n = corrXV.size(); i < n; i++) {
						Corr_Element corrElm =  corrXV.get(i);
						corrElm.setActive(false);
					}
					corrHorTableModel.fireTableDataChanged();
				}
			});

		unSelectCorrYButton.addActionListener(
			new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					for(int i = 0, n = corrYV.size(); i < n; i++) {
						Corr_Element corrElm =  corrYV.get(i);
						corrElm.setActive(false);
					}
					corrVerTableModel.fireTableDataChanged();
				}
			});
	}


	/**
	 *  Returns the panel attribute of the OrbitCorrector object
	 *
	 *@return    The panel value
	 */
	public JPanel getPanel() {
		return OrbitCorrectorMainPanel;
	}

	/**
	 *  Sets the tableModels attribute of the OrbitCorrector object
	 *
	 *@param  quadsTableModel  The new tableModels value
	 */
	public void setTableModel(QuadsTable quadsTableModel) {
		this.quadsTableModel = quadsTableModel;
	}

	/**
	*  Sets the PV Logger ID
	*
	*@param  pvLoggerID  The new ID
	*/
	public void setPVLoggerID(long pvLoggerID){
		this.pvLoggerID = pvLoggerID;
	}

	/**
	 *  Returns the corrXV attribute of the OrbitCorrector object
	 *
	 *@return    The corrXV value
	 */
	public Vector<Corr_Element> getCorrXV() {
		return corrXV;
	}

	/**
	 *  Returns the corrYV attribute of the OrbitCorrector object
	 *
	 *@return    The corrYV value
	 */
	public Vector<Corr_Element> getCorrYV() {
		return corrYV;
	}

	/**
	 *  Clear the graph region of the sub-panel.
	 */
	private void clearGraphs() {
		graphCorrPosXData.removeAllPoints();
		graphCorrPosYData.removeAllPoints();

		graphXPanel.refreshGraphJPanel();
		graphYPanel.refreshGraphJPanel();

		messageTextLocal.setText(null);
		messageTextLocal.setForeground(Color.red);
	}


	/**
	 *  Description of the Method
	 */
	public void update() {
	}


	/**
	 *  Sets the accelSeq attribute of the OrbitCorrector object
	 *
	 *@param  accSeq  The new accelSeq value
	 */
	public void setAccelSeq(AcceleratorSeqCombo accSeq) {
		this.accSeq = accSeq;
		corrXV.clear();
		corrYV.clear();
		java.util.List<AcceleratorNode> corrs = accSeq.getAllNodesOfType(HDipoleCorr.s_strType);
		for(int i = 0, n = corrs.size(); i < n; i++) {
			HDipoleCorr corr = (HDipoleCorr) corrs.get(i);
			Corr_Element corrElm = new Corr_Element(corr.getId(), corr);
			corrElm.setActive(true);
			corrXV.add(corrElm);
		}

		corrs = accSeq.getAllNodesOfType(VDipoleCorr.s_strType);
		for(int i = 0, n = corrs.size(); i < n; i++) {
			VDipoleCorr corr = (VDipoleCorr) corrs.get(i);
			Corr_Element corrElm = new Corr_Element(corr.getId(), corr);
			corrElm.setActive(true);
			corrYV.add(corrElm);
		}
	}


	/**
	 *  Adds a feature to the PointsToGraph attribute of the OrbitCorrector object
	 */
	private void plotMeasuredOrbit() {
		graphPosXData.removeAllPoints();
		graphPosYData.removeAllPoints();
		for(int i = 0, n = quadsTableModel.getListModel().size(); i < n; i++) {
			Quad_Element quadElm =  quadsTableModel.getListModel().get(i);
			if(quadElm.isActive()) {
				double x = (double) i;
				if(quadElm.isPosXReady()) {
					graphPosXData.addPoint(x, quadElm.getPosX(), quadElm.getPosErrX());
				}
				if(quadElm.isPosYReady()) {
					graphPosYData.addPoint(x, quadElm.getPosY(), quadElm.getPosErrY());
				}
			}
		}
		graphXPanel.refreshGraphJPanel();
		graphYPanel.refreshGraphJPanel();
		messageTextLocal.setText(null);
		messageTextLocal.setForeground(Color.red);
	}


	/**
	 *  Description of the Method
	 *
	 *@return    The Return Value
	 */
	private boolean memorizeCurrents() {
		Vector<Corr_Element> corrV = new Vector<Corr_Element>();
		corrV.addAll(corrXV);
		corrV.addAll(corrYV);
		for(int i = 0, n = corrV.size(); i < n; i++) {
			Corr_Element corrElm =  corrV.get(i);
			try {
				corrElm.memorizeField();
			} catch(ConnectionException exp) {
				messageTextLocal.setText("Cannot read Current PV from: " + corrElm.getName());
				return false;
			} catch(GetException exp) {
				messageTextLocal.setText("Cannot read Current PV from: " + corrElm.getName());
				return false;
			}
		}
		return true;
	}

	/**
	 *  Description of the Method
	 */
	private void restoreCurrents() {
		Vector<Corr_Element> corrV = new Vector<Corr_Element>();
		corrV.addAll(corrXV);
		corrV.addAll(corrYV);
		for(int i = 0, n = corrV.size(); i < n; i++) {
			Corr_Element corrElm = corrV.get(i);
			try {
				corrElm.restoreField();
			} catch(ConnectionException exp) {
				messageTextLocal.setText("Cannot set Current PV for: " + corrElm.getName());
				return;
			} catch(PutException exp) {
				messageTextLocal.setText("Cannot set Current PV for: " + corrElm.getName());
				return;
			}
		}
		return;
	}

	/**
	 *  Description of the Method
	 *
	 *@param  corrV      The Parameter
	 *@param  direction  The Parameter
	 */
	private void applyCorrections(Vector<Corr_Element> corrV, int direction) {
		if(!restoreCorrectorsButton.isEnabled()) {
			clearGraphs();
			messageTextLocal.setText("Sorry. You have to memorize the Corrs.' state first!");
			return;
		}
		for(int i = 0, n = corrV.size(); i < n; i++) {
			Corr_Element corrElm = corrV.get(i);
			if(corrElm.isActive()) {
				try {
					double val = corrElm.getLiveField() - corrElm.getFieldFromMemory();
					if(direction == 0) {
						val = corrElm.getFieldFromMemory() + signX_TextField.getValue() * val;
					} else {
						val = corrElm.getFieldFromMemory() + signY_TextField.getValue() * val;
					}
					corrElm.setLiveField(val);
					corrElm.putLiveFieldToCA(corrElm.getLiveField());
				} catch(ConnectionException exp) {
					messageTextLocal.setText("Cannot set Current PV for: " + corrElm.getName());
					return;
				} catch(PutException exp) {
					messageTextLocal.setText("Cannot set Current PV for: " + corrElm.getName());
					return;
				}
			}
		}
	}


	/**
	 *  Sets the fontForAll attribute of the OrbitCorrector object
	 *
	 *@param  fnt  The new fontForAll value
	 */
	public void setFontForAll(Font fnt) {

		corrVerTableBorder.setTitleFont(fnt);
		corrHorTableBorder.setTitleFont(fnt);
		correctionControlBorder.setTitleFont(fnt);

		corrHorTable.setFont(fnt);
		corrVerTable.setFont(fnt);

		int font_width = corrHorTable.getFontMetrics(fnt).charWidth('U');
		int font_height = corrHorTable.getFontMetrics(fnt).getHeight();

		corrHorTable.setRowHeight((int) 1.1 * font_height);
		corrVerTable.setRowHeight((int) 1.1 * font_height);

		corrHorTable.getColumnModel().getColumn(0).setPreferredWidth(30 * font_width);
		corrHorTable.getColumnModel().getColumn(0).setMaxWidth(2000);
		corrHorTable.getColumnModel().getColumn(0).setMinWidth(20 * font_width);
		corrHorTable.getColumnModel().getColumn(1).setMaxWidth(14 * font_width);
		corrHorTable.getColumnModel().getColumn(1).setMinWidth(14 * font_width);
		corrHorTable.getColumnModel().getColumn(2).setMaxWidth(6 * font_width);
		corrHorTable.getColumnModel().getColumn(2).setMinWidth(6 * font_width);

		corrVerTable.getColumnModel().getColumn(0).setPreferredWidth(30 * font_width);
		corrVerTable.getColumnModel().getColumn(0).setMaxWidth(2000);
		corrVerTable.getColumnModel().getColumn(0).setMinWidth(20 * font_width);
		corrVerTable.getColumnModel().getColumn(1).setMaxWidth(14 * font_width);
		corrVerTable.getColumnModel().getColumn(1).setMinWidth(14 * font_width);
		corrVerTable.getColumnModel().getColumn(2).setMaxWidth(6 * font_width);
		corrVerTable.getColumnModel().getColumn(2).setMinWidth(6 * font_width);

		selectCorrXButton.setFont(fnt);
		unSelectCorrXButton.setFont(fnt);
		selectCorrYButton.setFont(fnt);
		unSelectCorrYButton.setFont(fnt);

		memorizeCorrectorsButton.setFont(fnt);
		restoreCorrectorsButton.setFont(fnt);
		findCorrectionXButton.setFont(fnt);
		findCorrectionYButton.setFont(fnt);
		applyCorrectionXButton.setFont(fnt);
		applyCorrectionYButton.setFont(fnt);
		signX_TextField.setFont(fnt);
		signY_TextField.setFont(fnt);
	}

	/**
	* Returns the text field of the sign coefficient for Horizontal direction.
	*
	*@return    The sign coefficient X text value
	*/
	public DoubleInputTextField getSignXText(){
		return signX_TextField;
	}

	/**
	* Returns the text field of the sign coefficient for Vertical direction.
	*
	*@return    The sign coefficient Y text value
	*/

	public DoubleInputTextField getSignYText(){
		return signY_TextField;
	}


	/**
	 *  Returns the messageText attribute of the OrbitCorrector object
	 *
	 *@return    The messageText value
	 */
	public JTextField getMessageText() {
		return messageTextLocal;
	}


	//==============================================
	//orbit correction part
	//==============================================
	/**
	 *  Description of the Method
	 *
	 *@param  direction  0 - means X(Horizontal) 1 - Y(Vertical)
	 *@param  corrV_in   The Parameter
	 */
	private void findCorrection(Vector<Corr_Element> corrV_in, final int direction) {

		if(direction == 0) {
			graphCorrPosXData.removeAllPoints();
		} else {
			graphCorrPosYData.removeAllPoints();
		}

		Vector<Quad_Element> quadV = new Vector<Quad_Element>();
		Vector<Double> posV = new Vector<Double>();
		Vector<Double> posErrV = new Vector<Double>();

		for(int i = 0, n = quadsTableModel.getListModel().size(); i < n; i++) {
			Quad_Element quadElm =  quadsTableModel.getListModel().get(i);
			if(direction == 0) {
				if(quadElm.isPosXReady()) {
					quadV.add(quadElm);
					posV.add(new Double(quadElm.getPosX()));
					posErrV.add(new Double(quadElm.getPosErrX()));
				}
			} else {
				if(quadElm.isPosYReady()) {
					quadV.add(quadElm);
					posV.add(new Double(quadElm.getPosY()));
					posErrV.add(new Double(quadElm.getPosErrY()));
				}
			}
		}

		final Corr_Element[] corrArr = new Corr_Element[corrV_in.size()];
		final Quad_Element[] quadArr = new Quad_Element[quadV.size()];

		for(int i = 0, n = corrV_in.size(); i < n; i++) {
			corrArr[i] = corrV_in.get(i);
		}
		for(int i = 0, n = quadV.size(); i < n; i++) {
			quadArr[i] =  quadV.get(i);
		}

		final int nEqs = quadArr.length;
		final int nVars = corrArr.length;
		final double[] posArr = new double[nEqs];
		final double[] posErrArr = new double[nEqs];
		for(int i = 0; i < nEqs; i++) {
			posArr[i] = ( posV.get(i)).doubleValue();
			posErrArr[i] = ( posErrV.get(i)).doubleValue();
		}

		final double[] weightBArr = new double[nVars];
		final double[] initBArr = new double[nVars];
		for(int j = 0; j < nVars; j++) {
			weightBArr[j] = 0.01;
			initBArr[j] = corrArr[j].getFieldFromMemory();
		}

		final double[][] coeffArr = new double[nEqs][nVars];
		for(int i = 0; i < nEqs; i++) {
			Quad_Element quadElm = quadArr[i];
			for(int j = 0; j < nVars; j++) {
				Corr_Element corrElm = corrArr[j];
				if(corrElm.hasQuad(quadElm)) {
					coeffArr[i][j] = corrElm.getCoeff(quadElm);
				} else {
					coeffArr[i][j] = 0.;
				}
			}
		}

		if(nVars == 0) {
			if(direction == 0) {
				graphCorrPosXData.removeAllPoints();
				graphXPanel.refreshGraphJPanel();
			} else {
				graphCorrPosYData.removeAllPoints();
				graphYPanel.refreshGraphJPanel();
			}
			messageTextLocal.setText("Cannot find the corrected orbit");
			return;
		}

		//model function
		ModelFunction1D mf =
			new ModelFunction1D() {

				public double getValue(double x, double[] a) {
					int ind = (int) x;
					double res = 0.;
					if(ind < nEqs) {
						for(int i = 0; i < nVars; i++) {
							res += (a[i] - initBArr[i]) * coeffArr[ind][i];
						}
					} else {
						ind = ind - nEqs;
						res = (a[ind] - initBArr[ind]) * weightBArr[ind];
					}
					return res;
				}

				public double getDerivative(double x, double[] a, int a_index) {
					int ind = (int) x;
					if(ind < nEqs) {
						return coeffArr[ind][a_index];
					}
					ind = ind - nEqs;
					if(ind == a_index) {
						return weightBArr[ind];
					}
					return 0.;
				}
			};

		DataStore ds = new DataStore();

		for(int i = 0; i < nEqs; i++) {
			double x = (double) i;
			double pos = -posArr[i];
			double posErr = posErrArr[i];
			ds.addRecord(pos, x);
		}

		for(int i = 0; i < nVars; i++) {
			double x = (double) (i + nEqs);
			ds.addRecord(0., x);
		}

		SolverLSM solver = new SolverLSM();

		boolean[] mask = new boolean[nVars];
		double[] a = new double[nVars];
		double[] a_err = new double[nVars];
		for(int i = 0; i < nVars; i++) {
			a_err[i] = 0.;
			if(corrArr[i].isActive()) {
				a[i] = initBArr[i];
				mask[i] = true;
			} else {
				a[i] = corrArr[i].getLiveField();
				mask[i] = false;
			}
		}

		boolean res = true;
		boolean valid = false;
		int countMax = 10;
		int count = 0;

		while(!valid && res) {
			res = solver.solve(ds, mf, a, a_err, mask);
			count++;
			if(count > countMax) {
				res = false;
			}
			if(res) {
				//check validation and increase the weight of the bad corrector
				valid = true;
				for(int i = 0; i < nVars; i++) {
					if(corrArr[i].isActive() && !corrArr[i].isInLimits(a[i])) {
						valid = false;
						weightBArr[i] = weightBArr[i] * 3.0;
					}
				}
			}
		}

		if(res) {
			for(int i = 0; i < nEqs; i++) {
				Quad_Element quadElm = quadArr[i];
				int ind = quadsTableModel.getListModel().indexOf(quadElm);
				double x = (double) ind;
				double pos = posArr[i] + mf.getValue((double) i, a);
				if(direction == 0) {
					graphCorrPosXData.addPoint(x, pos);
				} else {
					graphCorrPosYData.addPoint(x, pos);
				}
			}

			for(int i = 0; i < nVars; i++) {
				corrArr[i].setLiveField(a[i]);
				System.out.println("Corr:" + corrArr[i].getName() +
						" dB[T] =" + frmt.format(a[i]) +
						" +- " + frmt.format(a_err[i]));
			}
		} else {
			clearGraphs();
			messageTextLocal.setText("Cannot find the corrected orbit");
		}
	}


	//=================================================
	//  Tables models definition
	//=================================================

	/**
	 *  Description of the Method
	 */
	private void defineTableModels() {

		//horizontal correctors table model
		corrHorTableModel =
			new AbstractTableModel() {
                
                /** ID for serializable version */
                private static final long serialVersionUID = 1L;
                
				public Class<?> getColumnClass(int columnIndex) {
					if(columnIndex == 0 || columnIndex == 1) {
						return String.class;
					}
					return Boolean.class;
				}

				public String getColumnName(int column) {
					if(column == 0) {
						return "Corrector";
					} else if(column == 1) {
						return "B, [T]";
					}
					return "Use";
				}

				public boolean isCellEditable(int rowIndex, int columnIndex) {
					if(columnIndex == 2) {
						return true;
					}
					return false;
				}

				public int getRowCount() {
					return corrXV.size();
				}

				public int getColumnCount() {
					return 3;
				}

				public Object getValueAt(int row, int column) {
					Corr_Element elm =  corrXV.get(row);
					if(column == 0) {
						return elm.getName();
					} else if(column == 1) {
						return elm.getLiveFieldAsString();
					}
					return elm.isActiveObj();
				}

				public void setValueAt(Object aValue, int row, int column) {
					if(column == 2) {
						Corr_Element elm = corrXV.get(row);
						elm.setActive(!elm.isActive());
						fireTableCellUpdated(row, column);
					}
				}
			};

		corrHorTable.setModel(corrHorTableModel);

		//vertical correctors table model
		corrVerTableModel =
			new AbstractTableModel() {
                
                /** ID for serializable version */
                private static final long serialVersionUID = 1L;
                
				public Class<?> getColumnClass(int columnIndex) {
					if(columnIndex == 0 || columnIndex == 1) {
						return String.class;
					}
					return Boolean.class;
				}

				public String getColumnName(int column) {
					if(column == 0) {
						return "Corrector";
					} else if(column == 1) {
						return "B, [T]";
					}
					return "Use";
				}

				public boolean isCellEditable(int rowIndex, int columnIndex) {
					if(columnIndex == 2) {
						return true;
					}
					return false;
				}

				public int getRowCount() {
					return corrXV.size();
				}

				public int getColumnCount() {
					return 3;
				}

				public Object getValueAt(int row, int column) {
					Corr_Element elm =  corrYV.get(row);
					if(column == 0) {
						return elm.getName();
					} else if(column == 1) {
						return elm.getLiveFieldAsString();
					}
					return elm.isActiveObj();
				}

				public void setValueAt(Object aValue, int row, int column) {
					if(column == 2) {
						Corr_Element elm =  corrYV.get(row);
						elm.setActive(!elm.isActive());
						fireTableCellUpdated(row, column);
					}
				}
			};

		corrVerTable.setModel(corrVerTableModel);
	}

}

