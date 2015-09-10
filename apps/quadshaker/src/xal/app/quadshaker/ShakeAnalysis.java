package xal.app.quadshaker;

import java.text.*;
import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.text.*;
import javax.swing.event.*;
import javax.swing.border.*;

import java.util.*;

import xal.ca.*;
import xal.extension.widgets.plot.*;
import xal.tools.apputils.*;
import xal.extension.widgets.swing.*;
import xal.extension.widgets.apputils.SimpleChartPopupMenu;
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
import xal.tools.beam.calc.CalculationsOnBeams;
// TODO: CKA - Many Unused Imports


/**
 *  Description of the Class
 *
 *@author     shishlo
 */
public class ShakeAnalysis {
    
	//main panel
	private JPanel shakeAnalysisMainPanel = new JPanel();
    
	//Tables and List models for BPMs and Quads
	private QuadsTable quadsTableModel = null;
	private BPMsTable bpmsTableModel = null;       // TODO: CKA - NEVER USED
    
	//Orbit corrector controller
	//The data will be initialized after analysis
	private OrbitCorrector orbitCorrector = null;
    
	//the graphs panel to show the scan results
	private FunctionGraphsJPanel graphXPanel = new FunctionGraphsJPanel();
	private FunctionGraphsJPanel graphYPanel = new FunctionGraphsJPanel();
    
	private HashMap<BPM_Element, BasicGraphData> graphXDataMap = new HashMap<BPM_Element, BasicGraphData>();
	private HashMap<BPM_Element, BasicGraphData> graphYDataMap = new HashMap<BPM_Element, BasicGraphData>();
    
	private BasicGraphData graphPosXData = new BasicGraphData();
	private BasicGraphData graphPosYData = new BasicGraphData();
    
	//left panel elements
	private TitledBorder quadTableBorder = null;
	private TitledBorder bpmTableBorder = null;
    
	private JTable quadsTable = new JTable();
	private JTable bpmsTable = new JTable();
    
	//Controls elements
	private TitledBorder analysisControlBorder = null;
    
	private JRadioButton usePVLoggerButton = new JRadioButton("Use PV Logger Data with ID: ", false);
	private DoubleInputTextField pvLoggerIdTextField = new DoubleInputTextField(8);
    
	private JButton analysisButton = new JButton("START ANALYSIS");
	private JButton rePlotButton = new JButton("RE-PLOT DATA");
    
	//validates measured offsets
	private OffSetValidationController validationCntr = new OffSetValidationController();
    
	//accelerator related objects
	private AcceleratorSeqCombo accSeq = null;
	private HashMap<String, BPM> bpmIdMap = new HashMap<String, BPM>();
	private HashMap<String, Quadrupole> quadIdMap = new HashMap<String, Quadrupole>();
    
	//message text field. It is actually message text field from Window
	private JTextField messageTextLocal = new JTextField();
    
	/**
	 *  Constructor for the ShakeAnalysis object
	 */
	public ShakeAnalysis() {
        
		Border border = BorderFactory.createEtchedBorder();
        
		shakeAnalysisMainPanel.setLayout(new BorderLayout());
        
		//define graph panel's properties
		SimpleChartPopupMenu.addPopupMenuTo(graphXPanel);
		graphXPanel.setOffScreenImageDrawing(true);
		graphXPanel.setName("Quads' X Offsets");
		graphXPanel.setAxisNames("Quad #", "offset X, mm");
		graphXPanel.setGraphBackGroundColor(Color.white);
		graphXPanel.setLegendButtonVisible(true);
		graphXPanel.setChooseModeButtonVisible(true);
		graphXPanel.setLegendBackground(Color.white);
		graphXPanel.setSmartGL(true);
        
		SimpleChartPopupMenu.addPopupMenuTo(graphYPanel);
		graphYPanel.setOffScreenImageDrawing(true);
		graphYPanel.setName("Quads' Y Offsets");
		graphYPanel.setAxisNames("Quad #", "offset Y, mm");
		graphYPanel.setGraphBackGroundColor(Color.white);
		graphYPanel.setLegendButtonVisible(true);
		graphYPanel.setChooseModeButtonVisible(true);
		graphYPanel.setLegendBackground(Color.white);
		graphYPanel.setSmartGL(true);
        
		graphPosXData.setDrawLinesOn(true);
		graphPosXData.setDrawPointsOn(true);
		graphPosXData.setGraphColor(Color.blue);
		graphPosXData.setGraphPointSize(5);
		graphPosXData.setLineThick(3);
		graphPosXData.setImmediateContainerUpdate(false);
		graphPosXData.setGraphProperty(graphXPanel.getLegendKeyString(), "Average Trajectory X");
        
		graphPosYData.setDrawLinesOn(true);
		graphPosYData.setDrawPointsOn(true);
		graphPosYData.setGraphColor(Color.red);
		graphPosYData.setGraphPointSize(5);
		graphPosYData.setLineThick(3);
		graphPosYData.setImmediateContainerUpdate(false);
		graphPosYData.setGraphProperty(graphXPanel.getLegendKeyString(), "Average Trajectory Y");
        
		graphXPanel.addGraphData(graphPosXData);
		graphYPanel.addGraphData(graphPosYData);
        
		JPanel panel_innG = new JPanel(new GridLayout(2, 1, 0, 0));
		panel_innG.add(graphXPanel);
		panel_innG.add(graphYPanel);
        
		JPanel panel_G = new JPanel(new BorderLayout());
		panel_G.setBorder(border);
		panel_G.add(panel_innG, BorderLayout.CENTER);
		shakeAnalysisMainPanel.add(panel_G, BorderLayout.CENTER);
        
		quadTableBorder = BorderFactory.createTitledBorder(border, "quads table");
		bpmTableBorder = BorderFactory.createTitledBorder(border, "bpms table");
		analysisControlBorder = BorderFactory.createTitledBorder(border, "analysis control");
        
		JPanel panel_PVLog = new JPanel(new GridLayout(1, 2, 1, 1));
		panel_PVLog.setBorder(border);
		panel_PVLog.add(usePVLoggerButton);
		panel_PVLog.add(pvLoggerIdTextField);
		pvLoggerIdTextField.setEnabled(usePVLoggerButton.isSelected());
        
		JPanel panel_StartAnal = new JPanel(new FlowLayout(FlowLayout.LEFT, 1, 1));
		panel_StartAnal.add(analysisButton);
		panel_StartAnal.add(rePlotButton);
        
		JPanel panel_Cntrl = new JPanel(new BorderLayout());
		panel_Cntrl.add(panel_PVLog, BorderLayout.NORTH);
		panel_Cntrl.add(panel_StartAnal, BorderLayout.CENTER);
		panel_Cntrl.add(validationCntr.getPanel(), BorderLayout.SOUTH);
		panel_Cntrl.setBorder(analysisControlBorder);
        
		JPanel panel_lT = new JPanel(new GridLayout(2, 1, 1, 1));
        
		JPanel panel_l = new JPanel(new BorderLayout());
		panel_l.add(panel_Cntrl, BorderLayout.NORTH);
		panel_l.add(panel_lT, BorderLayout.CENTER);
        
		shakeAnalysisMainPanel.add(panel_l, BorderLayout.WEST);
        
		JPanel panel_lT0 = new JPanel(new BorderLayout());
		JPanel panel_lT1 = new JPanel(new BorderLayout());
        
		panel_lT0.setBorder(quadTableBorder);
		panel_lT1.setBorder(bpmTableBorder);
        
		panel_lT.add(panel_lT0);
		panel_lT.add(panel_lT1);
        
		JScrollPane scrollPane0 = new JScrollPane(quadsTable);
		JScrollPane scrollPane1 = new JScrollPane(bpmsTable);
		panel_lT0.add(scrollPane0);
		panel_lT1.add(scrollPane1);
        
		quadsTable.setPreferredScrollableViewportSize(new Dimension(1, 1));
		bpmsTable.setPreferredScrollableViewportSize(new Dimension(1, 1));
        
		usePVLoggerButton.addActionListener(
                                            new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if(usePVLoggerButton.isSelected()) {
                    pvLoggerIdTextField.setEnabled(true);
                } else {
                    pvLoggerIdTextField.setEnabled(false);
                }
            }
        });
        
		analysisButton.addActionListener(
                                         new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                messageTextLocal.setText(null);
                performAnalysis();
                addPointsToGraph();
            }
        });
        
		rePlotButton.addActionListener(
                                       new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                messageTextLocal.setText(null);
                addPointsToGraph();
            }
        });
	}
    
    
	/**
	 *  Returns the panel attribute of the ShakeAnalysis object
	 *
	 *@return    The panel value
	 */
	public JPanel getPanel() {
		return shakeAnalysisMainPanel;
	}
    
    
	/**
	 *  Constructor for the setPVLoggerId object
	 *
	 *@param  pvLoggerId  The Parameter
	 */
	public void setPVLoggerId(long pvLoggerId) {
		pvLoggerIdTextField.setValue((double) pvLoggerId);
	}
    
	/**
	 *  Sets the tableModels attribute of the ShakeAnalysis object
	 *
	 *@param  quadsTableModel  The new tableModels value
	 *@param  bpmsTableModel   The new tableModels value
	 */
	public void setTableModels(QuadsTable quadsTableModel, BPMsTable bpmsTableModel) {
		this.quadsTableModel = quadsTableModel;
		this.bpmsTableModel = bpmsTableModel;
        
		quadsTable.setModel(quadsTableModel);
		bpmsTable.setModel(bpmsTableModel);
	}
    
    
	/**
	 *  Sets the orbitCorrector attribute of the ShakeAnalysis object
	 *
	 *@param  orbitCorrector  The new orbitCorrector value
	 */
	public void setOrbitCorrector(OrbitCorrector orbitCorrector) {
		this.orbitCorrector = orbitCorrector;
	}
    
    
	/**
	 *  Clear the graph region of the sub-panel.
	 *  
	 *  TODO: CKA - NEVER USED
	 */
	private void clearGraphs() {
		clearGraphData();
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
	 *  Sets the accelSeq attribute of the ShakeAnalysis object
	 *
	 *@param  accSeq  The new accelSeq value
	 */
	public void setAccelSeq(AcceleratorSeqCombo accSeq) {
		this.accSeq = accSeq;
		bpmIdMap.clear();
		quadIdMap.clear();
        
		java.util.List<AcceleratorNode> quadList = accSeq.getAllNodesWithQualifier((new OrTypeQualifier()).or(Quadrupole.s_strType));
		Iterator<AcceleratorNode> quad_itr = quadList.iterator();
		while(quad_itr.hasNext()) {
			Magnet mag = (Magnet) quad_itr.next();
			if(!mag.isPermanent()) {
				Quadrupole quad = (Quadrupole) mag;
				quadIdMap.put(quad.getId(), quad);
			}
		}
        
		java.util.List<AcceleratorNode> bpmList = accSeq.getAllNodesOfType("BPM");
		Iterator<AcceleratorNode> bpm_itr = bpmList.iterator();
		while(bpm_itr.hasNext()) {
			BPM bpm = (BPM) bpm_itr.next();
			bpmIdMap.put(bpm.getId(), bpm);
		}
	}
    
	/**
	 *  Description of the Method
	 */
	private void performAnalysis() {
        
		Scenario scenario = null;
		try {
			scenario = Scenario.newScenarioFor(accSeq);
		} catch(ModelException e) {
			messageTextLocal.setText("Can not create scenario for this sequence! Stop!");
			return;
		}
        
		//custom part - depends on "Use Logger" button state
		if(!usePVLoggerButton.isSelected()) {
            //scenario.setSynchronizationMode(Scenario.SYNC_MODE_DESIGN);
			//scenario.setSynchronizationMode(Scenario.SYNC_MODE_LIVE);
			scenario.setSynchronizationMode(Scenario.SYNC_MODE_RF_DESIGN);
		} else {
			long pvLogID = (long) pvLoggerIdTextField.getValue();
			PVLoggerDataSource plds = new PVLoggerDataSource(pvLogID);
			scenario = plds.setModelSource(accSeq, scenario);
			orbitCorrector.setPVLoggerID(pvLogID);
		}
        
        IAlgorithm tracker = null;
        
        try {
            
            tracker = AlgorithmFactory.createEnvTrackerAdapt( accSeq);
            
        } catch ( InstantiationException exception ) {
            System.err.println( "Instantiation exception creating tracker." );
            exception.printStackTrace();
        }
        
        EnvelopeProbe probe = ProbeFactory.getEnvelopeProbe(accSeq, tracker);
        
		scenario.setProbe(probe);
		scenario.resetProbe();
        //probe.setBeamCharge(0.0);
        
		try {
			scenario.resync();
		} catch(SynchronizationException e) {
			messageTextLocal.setText("Can not synchronize scenario! Acc. is dead? Stop!");
			return;
		}
        
		try {
			scenario.run();
		} catch(ModelException e) {
			messageTextLocal.setText("Can not run scenario! Stop!");
			return;
		}
        
		Trajectory<EnvelopeProbeState> trajectory = probe.getTrajectory();
		CalculationsOnBeams            cobCalcEng = new CalculationsOnBeams(trajectory);
        
		for(int i = 0, n = quadsTableModel.getListModel().size(); i < n; i++) {
			Quad_Element quadElm =  quadsTableModel.getListModel().get(i);
			Quadrupole quad =  quadIdMap.get(quadElm.getName());
			//double field = 0.;
			//if(!usePVLoggerButton.isSelected()) {
			//	field = quadElm.getField();
			//} else {
			//	field = scenario.getModelInput(quad, ElectromagnetPropertyAccessor.PROPERTY_FIELD).getDoubleValue();
			//}
			EnvelopeProbeState probeState = trajectory.statesForElement(quadElm.getName()).get(0);
			//-------------------------------------------
			//W0,TK in eV
			//L in [m]
			//offset = - (dX/dG)*(W0*beta*gamma)/(L*c*m[0,1])
			//our (dX/dG) is in [mm/(T/m)]
			//offset  will be in [mm]
			//We do not need field!!!
			//--------------------------------------------
			double W0 = probeState.getSpeciesRestEnergy();
			double gamma = probeState.getGamma();
			double beta = Math.sqrt(1.0 - 1.0 / (gamma * gamma));
			double TK = probeState.getKineticEnergy();       // TODO: CKA - NEVER USED
			double L = quad.getEffLength();
			double c = 2.997924E+8;
			double res_coeff = (W0 * beta * gamma / (L * c));
			if(quadElm.getName().indexOf(":QV") > 0 || quadElm.getName().indexOf(":QTV") > 0) {
				res_coeff = -res_coeff;
			}
			//System.out.println("debug quad=" + quadElm.getName() +
			//" w0="+W0+" TK="+TK+" L="+L+" gamma="+gamma);
			double quad_pos = accSeq.getPosition(quad);
			HashMap<BPM_Element, Double> coeffMapX = quadElm.getSensitivityCoefsX();
			HashMap<BPM_Element, Double> coeffMapY = quadElm.getSensitivityCoefsY();
			HashMap<BPM_Element, Double> coeffErrMapX = quadElm.getSensitivityCoefsErrX();
			HashMap<BPM_Element, Double> coeffErrMapY = quadElm.getSensitivityCoefsErrY();
			HashMap<BPM_Element, Double> offsetMapX = quadElm.getOffsetMapX();       // TODO: CKA - NEVER USED
			HashMap<BPM_Element, Double> offsetMapY = quadElm.getOffsetMapY();       // TODO: CKA - NEVER USED
			quadElm.clearOffsetData();
			Iterator<BPM_Element> bpm_itr = coeffMapX.keySet().iterator();
			while(bpm_itr.hasNext()) {
				BPM_Element bpmElm =  bpm_itr.next();
				BPM bpm = bpmIdMap.get(bpmElm.getName());
				double bpm_pos = accSeq.getPosition(bpm);
				if(bpm_pos > quad_pos) {
					//coefX(Y) is d(X,Y)/d(B) X,Y in mm, B in Tesla
					double coefX = (coeffMapX.get(bpmElm)).doubleValue();
					double coefY = (coeffMapY.get(bpmElm)).doubleValue();
					double coefErrX = (coeffErrMapX.get(bpmElm)).doubleValue();
					double coefErrY = (coeffErrMapY.get(bpmElm)).doubleValue();

					// CKA 8/22/2014
//					PhaseMatrix phMatr = probe.stateResponse(quadElm.getName(), bpmElm.getName());
					PhaseMatrix phMatr = cobCalcEng.computeTransferMatrix(quadElm.getName(), bpmElm.getName());
					
					double m01 = phMatr.getElem(0, 1);
					double m23 = phMatr.getElem(2, 3);
					if(coefErrX > 0. && coefErrY > 0. && Math.abs(m01) > 0. && Math.abs(m23) > 0.) {
						//place to calculate offsets
						double offSetX = - coefX * res_coeff / m01;
						double offSetY = coefY * res_coeff / m23;
						double ratioX = Math.abs(coefX) / coefErrX;
						double ratioY = Math.abs(coefY) / coefErrY;
						quadElm.addOffsetData(bpmElm, offSetX, offSetY, ratioX, ratioY, m01, m23);
						//System.out.println("debug quad=" + quadElm.getName() +
						//" bpm=" + bpmElm.getName() + " m01=" + m01 + " m23=" + m23);
					}
				}
			}
		}
        
		//The initialization of the correctors coefficients
		//coeff = Q*L/(m*gamma*velocity) = c*L/(m[in eV]*gamma*beta)
		Vector<Corr_Element> corrV = new Vector<Corr_Element>();
		corrV.addAll(orbitCorrector.getCorrXV());
		corrV.addAll(orbitCorrector.getCorrYV());
		for(int i = 0, n = corrV.size(); i < n; i++) {
			Corr_Element corrElm =  corrV.get(i);
			corrElm.clearCoeffsMap();
			Electromagnet corr_mag = corrElm.getMagnet();
			EnvelopeProbeState probeState = trajectory.statesForElement(corr_mag.getId()).get(0);
			double W0 = probeState.getSpeciesRestEnergy();
			double gamma = probeState.getGamma();
			double beta = Math.sqrt(1.0 - 1.0 / (gamma * gamma));
			double L = corr_mag.getEffLength();
			double c = 2.997924E+8;
			double res_coeff = (L * c) / (W0 * beta * gamma);
			double corr_pos = accSeq.getPosition(corr_mag);
            
			for(int j = 0, m = quadsTableModel.getListModel().size(); j < m; j++) {
				Quad_Element quadElm = quadsTableModel.getListModel().get(j);
				Quadrupole quad =  quadIdMap.get(quadElm.getName());
				double quad_pos = accSeq.getPosition(quad);
				if(quad_pos > corr_pos) {
				    
				    // CKA  8/22/2014
//					PhaseMatrix phMatr = probe.stateResponse(corrElm.getName(), quadElm.getName());
					PhaseMatrix phMatr = cobCalcEng.computeTransferMatrix(corrElm.getName(), quadElm.getName());
					
					double me = 0.;
					if(corrElm.getName().indexOf(":DCH") > 0) {
						me = phMatr.getElem(0, 1);
					}
					if(corrElm.getName().indexOf(":DCV") > 0) {
						me = phMatr.getElem(2, 3);
					}
					//result [mm/T]
					double coeff = res_coeff * me * 1000.;
					corrElm.setCoeff(coeff, quadElm);
				} else {
					corrElm.setCoeff(0., quadElm);
				}
			}
		}
	}
    
    
	/**
	 *  Adds a feature to the PointsToGraph attribute of the ShakeAnalysis object
	 */
	private void addPointsToGraph() {
		clearGraphData();
		for(int i = 0, n = quadsTableModel.getListModel().size(); i < n; i++) {
			Quad_Element quadElm =  quadsTableModel.getListModel().get(i);
			quadElm.setPosX(0., 0.);
			quadElm.setPosY(0., 0.);
			quadElm.clearPosData();
			if(quadElm.isActive()) {
				double x = (double) i;
				double posX = 0.;
				double posY = 0.;
				double posErrX = 0.;
				double posErrY = 0.;
				int nPointsX = 0;
				int nPointsY = 0;
				HashMap<BPM_Element, Double> offsetMapX = quadElm.getOffsetMapX();
				HashMap<BPM_Element, Double> offsetMapY = quadElm.getOffsetMapY();
				Iterator<BPM_Element> bpm_itr = offsetMapX.keySet().iterator();
				while(bpm_itr.hasNext()) {
					BPM_Element bpmElm =  bpm_itr.next();
					if(bpmElm.isActive()) {
						double offSetX = ( offsetMapX.get(bpmElm)).doubleValue();
						double offSetY = (offsetMapY.get(bpmElm)).doubleValue();
						addGraphData(bpmElm);
						if(validationCntr.validateX(quadElm, bpmElm)) {
							addGraphPointX(bpmElm, x, offSetX);
							posX += offSetX;
							posErrX += offSetX * offSetX;
							nPointsX++;
						}
						if(validationCntr.validateY(quadElm, bpmElm)) {
							addGraphPointY(bpmElm, x, offSetY);
							posY += offSetY;
							posErrY += offSetY * offSetY;
							nPointsY++;
						}
					}
				}
				if(nPointsX > 0) {
					quadElm.setPosXReady(true);
					if(nPointsX == 1) {
						quadElm.setPosX(posX, 0.);
						posErrX = 0.;
					} else {
						posX = posX / nPointsX;
						posErrX = Math.sqrt((posErrX - nPointsX * posX * posX) / (nPointsX * (nPointsX - 1)));
						quadElm.setPosX(posX, posErrX);
					}
					graphPosXData.addPoint(x, posX, posErrX);
				}
				if(nPointsY > 0) {
					quadElm.setPosYReady(true);
					if(nPointsY == 1) {
						quadElm.setPosY(posY, 0.);
						posErrY = 0.;
					} else {
						posY = posY / nPointsY;
						posErrY = Math.sqrt((posErrY - nPointsY * posY * posY) / (nPointsY * (nPointsY - 1)));
						quadElm.setPosY(posY, posErrY);
					}
					graphPosYData.addPoint(x, posY, posErrY);
				}
			}
		}
		graphXPanel.refreshGraphJPanel();
		graphYPanel.refreshGraphJPanel();
	}
    
    
	/**
	 *  Sets the fontForAll attribute of the ShakeAnalysis object
	 *
	 *@param  fnt  The new fontForAll value
	 */
	public void setFontForAll(Font fnt) {
        
		quadTableBorder.setTitleFont(fnt);
		bpmTableBorder.setTitleFont(fnt);
		analysisControlBorder.setTitleFont(fnt);
        
		quadsTable.setFont(fnt);
		bpmsTable.setFont(fnt);
        
		int font_width = quadsTable.getFontMetrics(fnt).charWidth('U');
		int font_height = quadsTable.getFontMetrics(fnt).getHeight();
        
		quadsTable.setRowHeight((int) 1.1 * font_height);
		bpmsTable.setRowHeight((int) 1.1 * font_height);
        
		quadsTable.getColumnModel().getColumn(0).setPreferredWidth(30 * font_width);
		quadsTable.getColumnModel().getColumn(0).setMaxWidth(2000);
		quadsTable.getColumnModel().getColumn(0).setMinWidth(20 * font_width);
		quadsTable.getColumnModel().getColumn(1).setMaxWidth(6 * font_width);
		quadsTable.getColumnModel().getColumn(1).setMinWidth(6 * font_width);
		quadsTable.getColumnModel().getColumn(2).setMaxWidth(6 * font_width);
		quadsTable.getColumnModel().getColumn(2).setMinWidth(6 * font_width);
        
		bpmsTable.getColumnModel().getColumn(0).setPreferredWidth(30 * font_width);
		bpmsTable.getColumnModel().getColumn(0).setMaxWidth(2000);
		bpmsTable.getColumnModel().getColumn(0).setMinWidth(20 * font_width);
		bpmsTable.getColumnModel().getColumn(1).setMaxWidth(6 * font_width);
		bpmsTable.getColumnModel().getColumn(1).setMinWidth(6 * font_width);
        
		usePVLoggerButton.setFont(fnt);
		pvLoggerIdTextField.setFont(fnt);
		analysisButton.setFont(fnt);
		rePlotButton.setFont(fnt);
        
		validationCntr.setFontForAll(fnt);
	}
    
	/**
	 *  Adds a feature to the GraphData attribute of the ShakeAnalysis object
	 *
	 *@param  bpm  The feature to be added to the GraphData attribute
	 */
	private void addGraphData(BPM_Element bpm) {
		if(!graphXDataMap.containsKey(bpm)) {
			Color color = IncrementalColors.getColor(graphXDataMap.size());
            
			BasicGraphData graphXData = new BasicGraphData();
			graphXData.setDrawLinesOn(false);
			graphXData.setGraphColor(color);
			graphXData.setGraphPointSize(5);
			graphXData.setImmediateContainerUpdate(false);
			graphXData.setGraphProperty(graphXPanel.getLegendKeyString(), bpm.getName());
            
			graphXDataMap.put(bpm, graphXData);
			graphXPanel.addGraphData(graphXData);
            
			BasicGraphData graphYData = new BasicGraphData();
			graphYData.setDrawLinesOn(false);
			graphYData.setGraphColor(color);
			graphYData.setGraphPointSize(5);
			graphYData.setImmediateContainerUpdate(false);
			graphYData.setGraphProperty(graphYPanel.getLegendKeyString(), bpm.getName());
            
			graphYDataMap.put(bpm, graphYData);
			graphYPanel.addGraphData(graphYData);
		}
	}
    
	/**
	 *  Description of the Method
	 */
	private void clearGraphData() {
		Iterator<BasicGraphData> x_itr = graphXDataMap.values().iterator();
		while(x_itr.hasNext()) {
			BasicGraphData grd =  x_itr.next();
			grd.removeAllPoints();
		}
        
		Iterator<BasicGraphData> y_itr = graphYDataMap.values().iterator();
		while(y_itr.hasNext()) {
			BasicGraphData grd =y_itr.next();
			grd.removeAllPoints();
		}
        
		graphPosXData.removeAllPoints();
		graphPosYData.removeAllPoints();
	}
    
    
	/**
	 *  Adds a feature to the GraphPointX attribute of the ShakeAnalysis object
	 *
	 *@param  bpmElm   The feature to be added to the GraphPointX attribute
	 *@param  x        The feature to be added to the GraphPointX attribute
	 *@param  offSetX  The feature to be added to the GraphPointX attribute
	 */
	private void addGraphPointX(BPM_Element bpmElm, double x, double offSetX) {
		BasicGraphData grdX = graphXDataMap.get(bpmElm);
		grdX.addPoint(x, offSetX, 0.);
	}
    
	/**
	 *  Adds a feature to the GraphPointY attribute of the ShakeAnalysis object
	 *
	 *@param  bpmElm   The feature to be added to the GraphPointY attribute
	 *@param  x        The feature to be added to the GraphPointY attribute
	 *@param  offSetY  The feature to be added to the GraphPointY attribute
	 */
	private void addGraphPointY(BPM_Element bpmElm, double x, double offSetY) {
		BasicGraphData grdY =  graphYDataMap.get(bpmElm);
		grdY.addPoint(x, offSetY, 0.);
	}
    
    
	/**
	 *  Description of the Method
	 */
	public void clearAllGraphContent() {
		graphXDataMap.clear();
		graphYDataMap.clear();
		graphXPanel.removeAllGraphData();
		graphYPanel.removeAllGraphData();
		graphPosXData.removeAllPoints();
		graphPosYData.removeAllPoints();
		graphXPanel.addGraphData(graphPosXData);
		graphYPanel.addGraphData(graphPosYData);
	}
    
	/**
	 *  Returns the messageText attribute of the ShakeAnalysis object
	 *
	 *@return    The messageText value
	 */
	public JTextField getMessageText() {
		return messageTextLocal;
	}
    
}

