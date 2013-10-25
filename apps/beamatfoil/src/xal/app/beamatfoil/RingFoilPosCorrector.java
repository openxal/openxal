/**
 *  This controller includes the panel for ring beam at foil positions and angle
 *  control.
 *
 *@author     shishlo
 */

package xal.app.beamatfoil;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.table.*;
import javax.swing.text.*;
import javax.swing.event.*;
import javax.swing.border.*;
import java.util.*;
import java.io.*;
import java.util.List;
import java.beans.*;

import xal.extension.widgets.swing.Wheelswitch;
import xal.tools.text.FortranNumberFormat;

import xal.ca.*;

import xal.extension.widgets.plot.*;
import xal.tools.apputils.*;
import xal.extension.widgets.swing.*;
import xal.tools.xml.*;

import xal.service.pvlogger.*;
import xal.tools.database.*;

import xal.smf.proxy.ElectromagnetPropertyAccessor;

import xal.smf.*;
import xal.smf.impl.*;
import xal.smf.impl.qualify.*;

import xal.model.*;
import xal.sim.scenario.*;
import xal.service.pvlogger.sim.PVLoggerDataSource.*;
import xal.model.probe.*;
//import xal.model.alg.resp.*;
//import xal.model.probe.resp.*;
import xal.model.probe.traj.MatrixTrajectory;
import xal.model.probe.traj.TransferMapTrajectory;
import xal.model.probe.traj.TransferMapState;
import xal.sim.sync.*;
//import xal.model.probe.resp.traj.*;

import xal.tools.beam.*;
import xal.model.alg.TransferMapTracker;

/**
 *  Description of the Class
 *
 *@author     shishlo
 */
public class RingFoilPosCorrector {
    
	//main panel
	private JPanel ringFoilPosCorrectorMainPanel = new JPanel();
    
	//The dipole correctors Vector
	private Vector<Corr_Element> corrV = new Vector<Corr_Element>();
	private RingBPM bpmStart = null;
	private RingBPM bpmEnd = null;
	private Marker foil = null;
	
	//coefficients for delta(pos)/delta(B) and delta(angle)/delta(B)
	//coeffArr[ind_chicane][foil or last BPM][position or angle]
	//ind_chicane = 0,..,3 DH_A10,DH_A11,DH_A12,DH_A13
    private double[][][] coeffArr = new double[4][2][2];
	private boolean coeffReady = false;
    
	//BPM10 to Foil matrix
	private PhaseMatrix BPM10_Foil_phm = new PhaseMatrix();
	//BPM10 t0 BPM_A13 matrix
	private PhaseMatrix BPM10_BPM13_phm = new PhaseMatrix();
	
	//left panel elements
	private TitledBorder corrTableBorder = null;
    
	private JTable corrTable = new JTable();
	private AbstractTableModel corrTableModel = null;
    
	//Controls elements
	private TitledBorder correctionControlBorder = null;
    
	private JButton memorizeCorrectorsButton = new JButton("== Memorize Existing B ==");
	private JButton restoreCorrectorsButton = new JButton("== Restore from Memory ==");
    
	private JLabel posWheelLabel =   new JLabel("Change in Pos.[mm]: ",JLabel.CENTER);
	private JLabel angleWheelLabel = new JLabel("Change in Angle [mrad]: ",JLabel.CENTER);
	
	private Wheelswitch posWheel = new Wheelswitch();
	private Wheelswitch angleWheel = new Wheelswitch();
	
	private JLabel posResLabel =   new JLabel("Solution Delta(pos.) [mm]: ",JLabel.CENTER);
	private JLabel angleResLabel = new JLabel("Solution Delta(angle) [mrad]: ",JLabel.CENTER);
	
	private DoubleInputTextField posResTextField = new DoubleInputTextField(14);
	private DoubleInputTextField angleResTextField = new DoubleInputTextField(14);
	
	//the beam position at the foil - Prediction
	private TitledBorder positionAtFoilBorder = null;
	
	private JLabel bpm10PosLabel =  new JLabel("<=x BPM10 [mm]",JLabel.LEFT);
	private JLabel bpm13PosLabel = new JLabel("<=x BPM13 [mm]",JLabel.LEFT);
	
	protected DoubleInputTextField bpm10PosTextField = new DoubleInputTextField(10);
	protected DoubleInputTextField bpm13PosTextField = new DoubleInputTextField(10);
    
	private JLabel xFoilPosLabel =  new JLabel("<=X at foil,mm",JLabel.LEFT);
	private JLabel xpFoilPosLabel = new JLabel("<=XP at foil,mrad",JLabel.LEFT);
	
	protected DoubleInputTextField xFoilPosTextField = new DoubleInputTextField(10);
	protected DoubleInputTextField xpFoilPosTextField = new DoubleInputTextField(10);
    
	private JButton recalcPositionButton = new JButton("== Calculate Position and Angle ==");
    
	//accelerator related objects
	private AcceleratorSeq accSeq = null;
    
	//current format
	private FortranNumberFormat frmt = new FortranNumberFormat("G10.3");
    
	//message text field. It is actually message text field from Window
	private JTextField messageTextLocal = new JTextField();
    
	private double min_sum = 10.0e+20;
	
	/**
	 *  Constructor for the RingFoilPosCorrector object
	 */
	public RingFoilPosCorrector(String borderTitle) {
        
		Border border = BorderFactory.createEtchedBorder();
        
		ringFoilPosCorrectorMainPanel.setLayout(new BorderLayout());
		corrTableBorder = BorderFactory.createTitledBorder(border, borderTitle);
		ringFoilPosCorrectorMainPanel.setBorder(corrTableBorder);
		
		correctionControlBorder = BorderFactory.createTitledBorder(border, "Beam at foil position control");
        
		//define tables models
		defineTableModels();
		
		//set the correctors table on panel
		JPanel tablePanel = new JPanel(new BorderLayout());
		
		JPanel tableButtonPanel = new JPanel(new GridLayout(1, 2, 1, 1));
		tableButtonPanel.add(memorizeCorrectorsButton);
		tableButtonPanel.add(restoreCorrectorsButton);
        
		JScrollPane scrollPane = new JScrollPane(corrTable);
        
		tablePanel.add(tableButtonPanel,BorderLayout.SOUTH);
		tablePanel.add(scrollPane,BorderLayout.CENTER);
		
        //set the fitting panel
		JPanel fittingPanel = new JPanel(new BorderLayout());
		fittingPanel.setBorder(correctionControlBorder);
		
		posWheel.setFormat("+##.#");
		angleWheel.setFormat("+##.#");
		JPanel ftUpPanel = new JPanel(new GridLayout(2, 2, 1, 1));
		ftUpPanel.add(posWheelLabel);
		ftUpPanel.add(angleWheelLabel);
		JPanel ftUp0Panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 1, 1));
		ftUp0Panel.add(posWheel);
		JPanel ftUp1Panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 1, 1));
		ftUp1Panel.add(angleWheel);
		ftUpPanel.add(ftUp0Panel);
		ftUpPanel.add(ftUp1Panel);
        
		JPanel ftCntPanel = new JPanel(new BorderLayout());
		
		positionAtFoilBorder = BorderFactory.createTitledBorder(border, "Absolute Baem Position at Foil");
		ftCntPanel.setBorder(corrTableBorder);
		
		JPanel ftCnt0Panel = new JPanel(new GridLayout(2, 2, 1, 1));
		ftCnt0Panel.add(bpm10PosTextField);
		ftCnt0Panel.add(bpm10PosLabel);
		ftCnt0Panel.add(bpm13PosTextField);
		ftCnt0Panel.add(bpm13PosLabel);
		ftCnt0Panel.setBorder(border);
		
		JPanel ftCnt1Panel = new JPanel(new GridLayout(2, 2, 1, 1));
		ftCnt1Panel.add(xFoilPosTextField);
		ftCnt1Panel.add(xFoilPosLabel);
		ftCnt1Panel.add(xpFoilPosTextField);
		ftCnt1Panel.add(xpFoilPosLabel);
		ftCnt1Panel.setBorder(border);
		
		JPanel ftCnt2Panel = new JPanel(new GridLayout(2, 1, 1, 1));
		ftCnt2Panel.add(ftCnt0Panel);
		ftCnt2Panel.add(ftCnt1Panel);
		
		JPanel ftCnt3Panel = new JPanel(new GridLayout(1, 1, 1, 1));
		ftCnt3Panel.add(recalcPositionButton);
		
		ftCntPanel.add(ftCnt2Panel,BorderLayout.CENTER);
		ftCntPanel.add(ftCnt3Panel,BorderLayout.SOUTH);
        
		JPanel ftDownPanel = new JPanel(new GridLayout(2, 2, 1, 1));
		ftDownPanel.add(posResLabel);
		ftDownPanel.add(angleResLabel);
		JPanel ftDown0Panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 1, 1));
		ftDown0Panel.add(posResTextField);
		JPanel ftDown1Panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 1, 1));
		ftDown1Panel.add(angleResTextField);
		ftDownPanel.add(ftDown0Panel);
		ftDownPanel.add(ftDown1Panel);
		
		posResTextField.setDecimalFormat(frmt);
		angleResTextField.setDecimalFormat(frmt);
		posResTextField.setHorizontalAlignment(JTextField.CENTER);
		angleResTextField.setHorizontalAlignment(JTextField.CENTER);
		
		bpm10PosTextField.setDecimalFormat(frmt);
		bpm13PosTextField.setDecimalFormat(frmt);
		xFoilPosTextField.setDecimalFormat(frmt);
		xpFoilPosTextField.setDecimalFormat(frmt);
		
		bpm10PosTextField.setHorizontalAlignment(JTextField.CENTER);
		bpm13PosTextField.setHorizontalAlignment(JTextField.CENTER);
		xFoilPosTextField.setHorizontalAlignment(JTextField.CENTER);
		xpFoilPosTextField.setHorizontalAlignment(JTextField.CENTER);
		
		fittingPanel.add(ftUpPanel, BorderLayout.NORTH);
		fittingPanel.add(ftDownPanel, BorderLayout.CENTER);
		fittingPanel.add(ftCntPanel, BorderLayout.SOUTH);
		
		JPanel rightPanel = new JPanel(new BorderLayout());
		rightPanel.add(fittingPanel, BorderLayout.NORTH);
		
		ringFoilPosCorrectorMainPanel.add(tablePanel, BorderLayout.WEST);
		ringFoilPosCorrectorMainPanel.add(rightPanel, BorderLayout.CENTER);
		
		restoreCorrectorsButton.setEnabled(false);
		
		memorizeCorrectorsButton.addActionListener(
                                                   new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                messageTextLocal.setText(null);
                if(memorizeCurrents()) {
                    restoreCorrectorsButton.setEnabled(true);
                    setUpCorrectorsCoeff();
                } else {
                    restoreCorrectorsButton.setEnabled(false);
                }
                posWheel.setValue(0.);
                angleWheel.setValue(0.);
                posResTextField.setValue(0.);
                angleResTextField.setValue(0.);
                corrTableModel.fireTableDataChanged();
            }
        });
		
		restoreCorrectorsButton.addActionListener(
                                                  new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                messageTextLocal.setText(null);
                restoreCurrents();
                coeffReady = false;
                posWheel.setValue(0.);
                angleWheel.setValue(0.);
                posResTextField.setValue(0.);
                angleResTextField.setValue(0.);
                corrTableModel.fireTableDataChanged();
            }
        });
		
		recalcPositionButton.addActionListener(
                                               new ActionListener() {
            public void actionPerformed(ActionEvent e) {
				messageTextLocal.setText(null);
                findBeamPosAtFoil();
            }
        });
		
		//whell actions
		PropertyChangeListener	wheelListener = new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent event) {
				messageTextLocal.setText(null);
				if(findCorrection()){
					applyCorrections();
					corrTableModel.fireTableDataChanged();
				}
			}
		};
		posWheel.addPropertyChangeListener("value", wheelListener);
		angleWheel.addPropertyChangeListener("value", wheelListener);
	}
    
    
	/**
	 *  Returns the panel attribute of the RingFoilPosCorrector object
	 *
	 *@return    The panel value
	 */
	public JPanel getPanel() {
		return ringFoilPosCorrectorMainPanel;
	}
    
	/**
	 *  Returns the corrV vector with correctors
	 *
	 *@return    The corrV value
	 */
	public Vector<Corr_Element> getCorrV() {
		return corrV;
	}
    
	/**
	 *  Description of the Method
	 */
	public void update() {
	}
    
	/**
	 *  Sets the accelSeq attribute of the RingFoilPosCorrector object
	 *
	 *@param  accSeq  The new accelSeq value
	 */
	public void setAccelSeq(AcceleratorSeq accSeq) {
		this.accSeq = accSeq;
		corrV.clear();
		Bend corr = (Bend) accSeq.getNodeWithId("Ring_Mag:DH_A10");
		//corr.getMagBucket().setBendAngle( 0.0 );
		Corr_Element corrElm = new Corr_Element(corr.getId(), corr);
		corrElm.setActive(true);
		corrV.add(corrElm);
		corr = (Bend) accSeq.getNodeWithId("Ring_Mag:DH_A11");
		//corr.getMagBucket().setBendAngle( 0.0 );
		corrElm = new Corr_Element(corr.getId(), corr);
		corrElm.setActive(true);
		corrV.add(corrElm);
		corr = (Bend) accSeq.getNodeWithId("Ring_Mag:DH_A12");
		//corr.getMagBucket().setBendAngle( 0.0 );
		corrElm = new Corr_Element(corr.getId(), corr);
		corrElm.setActive(true);
		corrV.add(corrElm);
		corr = (Bend) accSeq.getNodeWithId("Ring_Mag:DH_A13");
		//corr.getMagBucket().setBendAngle( 0.0 );
		corrElm = new Corr_Element(corr.getId(), corr);
		corrElm.setActive(true);
		corrV.add(corrElm);
		
        bpmStart = (RingBPM) accSeq.getNodeWithId("Ring_Diag:BPM_A10");
        bpmEnd = (RingBPM) accSeq.getNodeWithId("Ring_Diag:BPM_A13");
        foil = (Marker) accSeq.getNodeWithId("Ring_Inj:Foil");
	}
    
	/**
	 *  Description of the Method
	 *
	 *@return    The Return Value
	 */
	private boolean memorizeCurrents() {
		for(int i = 0, n = corrV.size(); i < n; i++) {
			Corr_Element corrElm = corrV.get(i);
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
	 *  It applys the changes in magnets
	 */
	private void applyCorrections() {
		for(int i = 0, n = corrV.size(); i < n; i++) {
			Corr_Element corrElm = corrV.get(i);
			if(corrElm.isActive()) {
				try {
					double val = corrElm.getLiveField();
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
	 *  Sets the fontForAll attribute of the RingFoilPosCorrector object
	 *
	 *@param  fnt  The new fontForAll value
	 */
	public void setFontForAll(Font fnt) {
        
		corrTableBorder.setTitleFont(fnt);
		correctionControlBorder.setTitleFont(fnt);
		positionAtFoilBorder.setTitleFont(fnt);
        
		corrTable.setFont(fnt);
        
		int font_width = corrTable.getFontMetrics(fnt).charWidth('U');
		int font_height = corrTable.getFontMetrics(fnt).getHeight();
        
		corrTable.setRowHeight((int) 1.1 * font_height);
        
		corrTable.getColumnModel().getColumn(0).setPreferredWidth(15 * font_width);
		corrTable.getColumnModel().getColumn(0).setMaxWidth(200);
		corrTable.getColumnModel().getColumn(0).setMinWidth(15 * font_width);
		corrTable.getColumnModel().getColumn(1).setMaxWidth(14 * font_width);
		corrTable.getColumnModel().getColumn(1).setMinWidth(14 * font_width);
		corrTable.getColumnModel().getColumn(2).setMaxWidth(14 * font_width);
		corrTable.getColumnModel().getColumn(2).setMinWidth(14 * font_width);
		corrTable.setPreferredScrollableViewportSize(new Dimension(1, 1));
		
		memorizeCorrectorsButton.setFont(fnt);
		restoreCorrectorsButton.setFont(fnt);
		
		posWheelLabel.setFont(fnt);
		angleWheelLabel.setFont(fnt);
		posResLabel.setFont(fnt);
		angleResLabel.setFont(fnt);
		
		bpm10PosLabel.setFont(fnt);
		bpm13PosLabel.setFont(fnt);
		xFoilPosLabel.setFont(fnt);
		xpFoilPosLabel.setFont(fnt);
		bpm10PosTextField.setFont(fnt);
		bpm13PosTextField.setFont(fnt);
		xFoilPosTextField.setFont(fnt);
		xpFoilPosTextField.setFont(fnt);
		recalcPositionButton.setFont(fnt);
		
		posResTextField.setFont(fnt);
		angleResTextField.setFont(fnt);
	}
    
	/**
	 *  Returns the messageText attribute of the RingFoilPosCorrector object
	 *
	 *@return    The messageText value
	 */
	public JTextField getMessageText() {
		return messageTextLocal;
	}
    
	/**
	 *  Sets the messageText attribute of the RingFoilPosCorrector object
	 */
	public void setMessageText(JTextField  messageTextLocal){
		this.messageTextLocal = messageTextLocal;
	}
	
	//==============================================
	//orbit correction part
	//==============================================
	/**
	 *  It sets up the new correctors coeffitiens for correctors
	 */
    private boolean setUpCorrectorsCoeff(){
        
        Scenario scenario = null;
        try {
            scenario = Scenario.newScenarioFor(accSeq);
        } catch(ModelException e) {
            messageTextLocal.setText("Can not create scenario for this sequence! Stop!");
            return false;
        }
        
        //scenario.setSynchronizationMode(Scenario.SYNC_MODE_DESIGN);
        //scenario.setSynchronizationMode(Scenario.SYNC_MODE_LIVE);
        scenario.setSynchronizationMode(Scenario.SYNC_MODE_RF_DESIGN);
        
        
        
        TransferMapTracker tracker = null;
        
        try {
            
            tracker = AlgorithmFactory.createTransferMapTracker(accSeq);
            
        } catch ( InstantiationException exception ) {
            System.err.println( "Instantiation exception creating tracker." );
            exception.printStackTrace();
        }
        
        
        TransferMapProbe probe = ProbeFactory.getTransferMapProbe(accSeq, tracker);
        probe.reset();
        
        scenario.setProbe(probe);
        scenario.resetProbe();
        
        for(int i = 0, n = corrV.size(); i < n; i++) {
            Corr_Element corrElm = corrV.get(i);
            Electromagnet corr = corrElm.getMagnet();
            // for debug only double B = corr.getDfltField();
            double B = corrElm.getFieldFromMemory();
            scenario.setModelInput(corr,ElectromagnetPropertyAccessor.PROPERTY_FIELD,B);
        }
        
        try {
            scenario.resync();
        } catch(SynchronizationException e) {
            messageTextLocal.setText("Can not synchronize scenario! Acc. is dead? Stop!");
            return false;
        }
        
        try {
            scenario.run();
        } catch(ModelException e) {
            messageTextLocal.setText("Can not run scenario! Stop!");
            return false;
        }
        
        TransferMapTrajectory trajectory = (TransferMapTrajectory) probe.getTrajectory();
        
        makePhaseMatrix(trajectory);
        
        //get BPM horizontal signals and calculate angles at the end and at the start BPMs
        double x_start = 0.;
        double x_end = 0.;
        try {
            x_start = 0.001*bpmStart.getXAvg();
            x_end = 0.001*bpmEnd.getXAvg();
        } catch(ConnectionException exp) {
            messageTextLocal.setText("Cannot connect to ring BPMs!");
            return false;
        } catch(GetException exp) {
            messageTextLocal.setText("Cannot connect to ring BPMs!");
            return false;
        }
        //x_start = 0.025;
        //x_end = 0.020;
        bpm10PosTextField.setValue(1000.*x_start);
        bpm13PosTextField.setValue(1000.*x_end);
        
        double a00 = BPM10_BPM13_phm.getElem(0,0);
        double a01 = BPM10_BPM13_phm.getElem(0,1);
        double a10 = BPM10_BPM13_phm.getElem(1,0);
        double a11 = BPM10_BPM13_phm.getElem(1,1);
        double c0 = BPM10_BPM13_phm.getElem(0,6);
        double c1 = BPM10_BPM13_phm.getElem(1,6);
        if(a01 == 0. || a11 == 0.){
            messageTextLocal.setText("The online model is wrong! Stop. Try to memorize again!");
            return false;
        }
        double xp_end = ((a01*a10-a11*a00)*x_start+a01*c1-a11*c0+a11*x_end)/a01;
        double xp_start = (xp_end - c1 - a10*x_start)/a11;
        
        PhaseVector bpmStartPHV = new PhaseVector(x_start,xp_start,0.,0.,0.,0.);
        //System.out.println("debug phStart="+bpmStartPHV.toString());
        PhaseVector bpmEndPHV = BPM10_BPM13_phm.times(bpmStartPHV);
        //System.out.println("debug phEnd="+bpmEndPHV.toString());
        PhaseVector foilPHV = BPM10_Foil_phm.times(bpmStartPHV);
        //System.out.println("debug ring beam at foil X[mm]="+1000*foilPHV.getElem(0)+" XP[mrad]="+1000*foilPHV.getElem(1));
        
        //let us find derivative on B in chicanes
        double coeff = 1.05;
        
        for(int i = 0, n = corrV.size(); i < n; i++) {
            Corr_Element corrElm = corrV.get(i);
            Electromagnet corr = corrElm.getMagnet();
            double B = scenario.getModelInput(corr,ElectromagnetPropertyAccessor.PROPERTY_FIELD).getDoubleValue();
            probe.reset();
            scenario.setModelInput(corr,ElectromagnetPropertyAccessor.PROPERTY_FIELD,B*coeff);
            try {
                scenario.resyncFromCache();
            } catch(SynchronizationException e) {
                messageTextLocal.setText("Can not synchronize scenario! Acc. is dead? Stop!");
                return false;
            }
            
            try {
                scenario.run();
            } catch(ModelException e) {
                messageTextLocal.setText("Can not run scenario! Stop!");
                return false;
            }
            trajectory = (TransferMapTrajectory) probe.getTrajectory();
            //System.out.println("debug i="+i + "======================================");
            makePhaseMatrix(trajectory);
            PhaseVector bpmEndPHV_new = BPM10_BPM13_phm.times(bpmStartPHV);
            PhaseVector foilPHV_new = BPM10_Foil_phm.times(bpmStartPHV);
            double dB = B*(coeff-1.0);
            //[chicane][foil][pos. or angle]
            coeffArr[i][0][0] = 1000.*(foilPHV_new.getElem(0) - foilPHV.getElem(0))/dB;
            coeffArr[i][0][1] = 1000.*(foilPHV_new.getElem(1) - foilPHV.getElem(1))/dB;
            //[chicane][BPM13][pos. or angle]
            coeffArr[i][1][0] = 1000.*(bpmEndPHV_new.getElem(0) - bpmEndPHV.getElem(0))/dB;
            coeffArr[i][1][1] = 1000.*(bpmEndPHV_new.getElem(1) - bpmEndPHV.getElem(1))/dB;
            //restore field
            scenario.setModelInput(corr,ElectromagnetPropertyAccessor.PROPERTY_FIELD,B);
            //System.out.println("debug i="+i+" foil pos coef   ="+coeffArr[i][0][0]);
            //System.out.println("debug i="+i+" foil angl coef  ="+coeffArr[i][0][1]);
            //System.out.println("debug i="+i+" bpm13 pos coef   ="+coeffArr[i][1][0]);
            //System.out.println("debug i="+i+" bpm13 angl coef  ="+coeffArr[i][1][1]);
            //System.out.println();
        }
        coeffReady = true;
        return true;
    }
    
	
    private void makePhaseMatrix(TransferMapTrajectory trajectory){
        //trajectory - it is from ring start to finish
        //BPM_A10 - DH_A10 - DH_A11 - Foil - finish
        //start - DH_A12 - DH_A13 - Ring_Diag:BPM_A13
        
        //BPM10 - end
        String node0 = bpmStart.getId();
        String node1 = trajectory.finalState().getElementId();
        PhaseMatrix phMatrBPM10_End = getTransferMatrix(trajectory, node0, node1);
        
        //start - BPM_A13
        node0 = trajectory.initialState().getElementId();
        node1 = bpmEnd.getId();
        PhaseMatrix phMatrStart_BPM13 = getTransferMatrix(trajectory, node0, node1);
        
        BPM10_BPM13_phm = phMatrStart_BPM13.times(phMatrBPM10_End);
        //System.out.println("debug BPM10_BPM13_phm="+BPM10_BPM13_phm.toString());
        
        //BPM10 - Foil
        node0 = bpmStart.getId();
        node1 = foil.getId();
        BPM10_Foil_phm = getTransferMatrix(trajectory, node0, node1);
        //System.out.println("debug BPM10_Foil_phm="+BPM10_Foil_phm.toString());
    }
    
    /** get the transfer matrix from the transfer map trajectory
     *
     *  copied/modified from orbitcorrect/CoordinateTransfer.java (07/19/2013)
     *  Method getTransferMatrix(String, String) was removed from TransferMapTrajectory
     *  Parameters changed to strings to fit this program
     */
	protected PhaseMatrix getTransferMatrix( final TransferMapTrajectory trajectory, final String fromNode, final String toNode ) {
		final PhaseMatrix fromMatrix = ((TransferMapState)trajectory.stateForElement( fromNode )).getTransferMap().getFirstOrder();
		final PhaseMatrix toMatrix = ((TransferMapState)trajectory.stateForElement( toNode )).getTransferMap().getFirstOrder();
		return getTransferMatrix( fromMatrix, toMatrix );
	}
    
    /** get the transfer matrix between the two response matricies */
	static protected PhaseMatrix getTransferMatrix( final PhaseMatrix fromMatrix, final PhaseMatrix toMatrix ) {
		return toMatrix.times( fromMatrix.inverse() );
	}
    
    
	/**
	 *  It finds the new correctors fields
	 */
	private boolean findCorrection() {
        
		if(coeffReady == false){
			messageTextLocal.setText("Model is not ready! Initialize model first!");
			return false;
		}
		
		//find the solution
		final double pos_goal = posWheel.getValue();
		final double angle_goal = angleWheel.getValue();
		double[] bArr = new double[4];
		
		//find B for A10 and A11 to position beam at foil
		double a00 = coeffArr[0][0][0];
		double a01 = coeffArr[1][0][0];
		double a10 = coeffArr[0][0][1];
		double a11 = coeffArr[1][0][1];
		double det = a00*a11 - a01*a10;
		bArr[0] = (a11*pos_goal-a01*angle_goal)/det;
		bArr[1] = (-a10*pos_goal+a00*angle_goal)/det;
        a00 = coeffArr[0][1][0];
        a01 = coeffArr[1][1][0];
        a10 = coeffArr[0][1][1];
        a11 = coeffArr[1][1][1];
		double pos_end_goal = - (a00*bArr[0]+a01*bArr[1]);
		double angle_end_goal = - (a10*bArr[0]+a11*bArr[1]);
		a00 = coeffArr[2][1][0];
		a01 = coeffArr[3][1][0];
		a10 = coeffArr[2][1][1];
		a11 = coeffArr[3][1][1];
		det = a00*a11 - a01*a10;
		bArr[2] = (a11*pos_end_goal-a01*angle_end_goal)/det;
		bArr[3] = (-a10*pos_end_goal+a00*angle_end_goal)/det;
		
		//foil position
		double pos_foil = 0.;
		double angle_foil = 0.;
		for(int i = 0; i < 4; i++){
			pos_foil += coeffArr[i][0][0]*bArr[i];
			angle_foil += coeffArr[i][0][1]*bArr[i];
		}
		//System.out.println("debug foil pos="+pos_foil+" angle="+angle_foil);
		
		//end (bpm13) position
		double pos_end = 0.;
		double angle_end = 0.;
		for(int i = 0; i < 4; i++){
			pos_end += coeffArr[i][1][0]*bArr[i];
			angle_end += coeffArr[i][1][1]*bArr[i];
		}
		//System.out.println("debug end pos="+pos_end+" angle="+angle_end);
		
        posResTextField.setValue(pos_foil);
        angleResTextField.setValue(angle_foil);
		
        for(int i = 0, n = corrV.size(); i < n; i++) {
			Corr_Element corrElm = corrV.get(i);
			corrElm.setLiveField(bArr[i]+corrElm.getFieldFromMemory());
		}
		
		return true;
	}
    
	/**
	 *  Finds the position at the foil
	 */
    private boolean findBeamPosAtFoil(){
        
        Scenario scenario = null;
        try {
            scenario = Scenario.newScenarioFor(accSeq);
        } catch(ModelException e) {
            messageTextLocal.setText("Can not create scenario for this sequence! Stop!");
            return false;
        }
        
        //scenario.setSynchronizationMode(Scenario.SYNC_MODE_DESIGN);
        //scenario.setSynchronizationMode(Scenario.SYNC_MODE_LIVE);
        scenario.setSynchronizationMode(Scenario.SYNC_MODE_RF_DESIGN);
        xal.model.alg.TransferMapTracker tracker = new xal.model.alg.TransferMapTracker();
        TransferMapProbe probe = ProbeFactory.getTransferMapProbe(accSeq, tracker);
        probe.reset();
        
        scenario.setProbe(probe);
        scenario.resetProbe();
        
        try {
            scenario.resync();
        } catch(SynchronizationException e) {
            messageTextLocal.setText("Can not synchronize scenario! Acc. is dead? Stop!");
            return false;
        }
        
        try {
            scenario.run();
        } catch(ModelException e) {
            messageTextLocal.setText("Can not run scenario! Stop!");
            return false;
        }
        
        TransferMapTrajectory trajectory = (TransferMapTrajectory) probe.getTrajectory();
        
        makePhaseMatrix(trajectory);
        
        //get BPM horizontal signals and calculate angles at the end and at the start BPMs
        double x_start = 0.;
        double x_end = 0.;
        try {
            x_start = 0.001*bpmStart.getXAvg();
            x_end = 0.001*bpmEnd.getXAvg();
        } catch(ConnectionException exp) {
            messageTextLocal.setText("Cannot connect to ring BPMs!");
            return false;
        } catch(GetException exp) {
            messageTextLocal.setText("Cannot connect to ring BPMs!");
            return false;
        }
        //x_start = 0.025;
        //x_end = 0.020;
        bpm10PosTextField.setValue(1000.*x_start);
        bpm13PosTextField.setValue(1000.*x_end);
        
        double a00 = BPM10_BPM13_phm.getElem(0,0);
        double a01 = BPM10_BPM13_phm.getElem(0,1);
        double a10 = BPM10_BPM13_phm.getElem(1,0);
        double a11 = BPM10_BPM13_phm.getElem(1,1);
        double c0 = BPM10_BPM13_phm.getElem(0,6);
        double c1 = BPM10_BPM13_phm.getElem(1,6);
        if(a01 == 0. || a11 == 0.){
            messageTextLocal.setText("The online model is wrong! Stop. Try to memorize again!");
            return false;
        }
        double xp_end = ((a01*a10-a11*a00)*x_start+a01*c1-a11*c0+a11*x_end)/a01;
        double xp_start = (xp_end - c1 - a10*x_start)/a11;
        
        PhaseVector bpmStartPHV = new PhaseVector(x_start,xp_start,0.,0.,0.,0.);
        PhaseVector foilPHV = BPM10_Foil_phm.times(bpmStartPHV);
        xFoilPosTextField.setValue(1000.*foilPHV.getElem(0));
        xpFoilPosTextField.setValue(1000.*foilPHV.getElem(1));
        return true;
    }	
    
	//=================================================
	//  Tables models definition
	//=================================================
    
	/**
	 *  Description of the Method
	 */
	private void defineTableModels() {
        
		//horizontal correctors table model
		corrTableModel =
        new AbstractTableModel() {
            
            /** ID for serializable version */
            private static final long serialVersionUID = 1L;
            
            public Class getColumnClass(int columnIndex) {
                return String.class;
            }
            
            public String getColumnName(int column) {
                if(column == 0) {
                    return "Chicane";
                } else if(column == 1) {
                    return "Guess B[T]";
                } 
                return "Memory B";
            }
            
            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return false;
            }
            
            public int getRowCount() {
                return corrV.size();
            }
            
            public int getColumnCount() {
                return 3;
            }
            
            public Object getValueAt(int row, int column) {
                Corr_Element elm = corrV.get(row);
                if(column == 0) {
                    return elm.getName();
                } else if(column == 1) {
                    return elm.format(elm.getLiveField());
                } 
                return elm.format(elm.getFieldFromMemory());
            }
            
            public void setValueAt(Object aValue, int row, int column) {
            }
        };
        
		corrTable.setModel(corrTableModel);
        
	}
    
}

