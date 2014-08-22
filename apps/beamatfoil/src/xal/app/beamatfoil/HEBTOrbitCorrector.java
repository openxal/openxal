package xal.app.beamatfoil;

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
import xal.service.pvlogger.sim.PVLoggerDataSource;
import xal.model.probe.*;
//import xal.model.alg.resp.*;
//import xal.model.probe.resp.*;
import xal.model.probe.traj.*;
import xal.sim.sync.*;
import xal.tools.beam.*;
import xal.tools.beam.calc.CalculationsOnBeams;
//import xal.tools.optimizer.*;
import xal.extension.solver.*;
import xal.extension.solver.algorithm.*;
import xal.extension.solver.hint.InitialDelta;
// TODO: CKA - Over half the imports are unused

/**
 *  Description of the Class
 *
 *@author     shishlo
 */
public class HEBTOrbitCorrector {

	//main panel
	private JPanel HEBTOrbitCorrectorMainPanel = new JPanel();

	//The dipole correctors Vector
	private Vector<Corr_Element> corrV = new Vector<Corr_Element>();
	private Vector<Corr_Element> corrOptV = new Vector<Corr_Element>();	
	private Vector<Variable> corrProxyV = new Vector<Variable>();	

	//left panel elements
	private TitledBorder corrTableBorder = null;

	private JTable corrTable = new JTable();
	private AbstractTableModel corrTableModel = null;

	//Controls elements
	private TitledBorder correctionControlBorder = null;

	private JButton memorizeCorrectorsButton = new JButton("== Memorize Existing B ==");
	private JButton restoreCorrectorsButton = new JButton("== Restore from Memory ==");

	private JButton initModelButton = new JButton("Initialize Model");
	private JButton findCorrectionButton = new JButton("Find Solution");
	private JButton applyCorrectionButton = new JButton("Apply Guess B");
	
	private JRadioButton automateButton = new JRadioButton("Automate Find-Apply",false);
	
	private JLabel posWheelLabel =   new JLabel("Change in Pos.[mm]: ",JLabel.CENTER);
	private JLabel angleWheelLabel = new JLabel("Change in Angle [mrad]: ",JLabel.CENTER);
	
	private Wheelswitch posWheel = new Wheelswitch();
	private Wheelswitch angleWheel = new Wheelswitch();
	
	private JLabel posResLabel =   new JLabel("Solution Delta(pos.) [mm]: ",JLabel.CENTER);
	private JLabel angleResLabel = new JLabel("Solution Delta(angle) [mrad]: ",JLabel.CENTER);	
	
	private DoubleInputTextField posResTextField = new DoubleInputTextField(14);	
	private DoubleInputTextField angleResTextField = new DoubleInputTextField(14);	
	
	private DoubleInputTextField sign_TextField = new DoubleInputTextField(5);
	
	//fitting coefficients
	private JLabel fieldWeightLabel =   new JLabel("<== Fitting Weight of B",JLabel.LEFT);
	protected DoubleInputTextField fieldWeightTextField = new DoubleInputTextField(14);
	private JRadioButton useRealtiveToInitButton = new JRadioButton("Use B close to Mem. B",false);
	
	private JLabel posWeightLabel = new JLabel("<== Position Weight  ",JLabel.LEFT);
	private JLabel angleWeightLabel =  new JLabel("<== Angle Weight",JLabel.LEFT);
	
	protected DoubleInputTextField posWeightTextField = new DoubleInputTextField(14);
	protected DoubleInputTextField angleWeightTextField = new DoubleInputTextField(14);
	
	//accelerator related objects
	private AcceleratorSeq accSeq = null;

	//current format
	private FortranNumberFormat frmt = new FortranNumberFormat("G10.3");

	//message text field. It is actually message text field from Window
	private JTextField messageTextLocal = new JTextField();

	private double min_sum = 10.0e+20;     // TODO: CKA - NEVER USED
    
    
    //create a problem for solver
    private Problem problem;
	
	/**
	 *  Constructor for the HEBTOrbitCorrector object
	 */
	public HEBTOrbitCorrector(String borderTitle) {
		
		sign_TextField.setValue(1.0);
		fieldWeightTextField.setValue(0.0);
		posWeightTextField.setValue(1.0);
		angleWeightTextField.setValue(1.0);
		
		
		fieldWeightTextField.setDecimalFormat(frmt);
		posWeightTextField.setDecimalFormat(frmt);
		angleWeightTextField.setDecimalFormat(frmt);

		fieldWeightTextField.setHorizontalAlignment(JTextField.CENTER);
		posWeightTextField.setHorizontalAlignment(JTextField.CENTER); 
		angleWeightTextField.setHorizontalAlignment(JTextField.CENTER); 
		
		Border border = BorderFactory.createEtchedBorder();

		HEBTOrbitCorrectorMainPanel.setLayout(new BorderLayout());
		corrTableBorder = BorderFactory.createTitledBorder(border, borderTitle);
		HEBTOrbitCorrectorMainPanel.setBorder(corrTableBorder);
		
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
		
		//fitting parameters panel
		JPanel fittingParamsPanel = new JPanel(new GridLayout(2, 1, 1, 1));
		
		JPanel wFieldPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 1, 1));
		wFieldPanel.add(fieldWeightTextField);
		wFieldPanel.add(fieldWeightLabel);
		wFieldPanel.add(useRealtiveToInitButton);
		
		JPanel wPosAnglePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 1, 1));		
		wPosAnglePanel.add(posWeightTextField);
		wPosAnglePanel.add(posWeightLabel);	
		wPosAnglePanel.add(angleWeightTextField);
		wPosAnglePanel.add(angleWeightLabel);

		fittingParamsPanel.add(wFieldPanel);
		fittingParamsPanel.add(wPosAnglePanel);
		
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
				
		JPanel ftCntPanel = new JPanel(new GridLayout(2, 1, 1, 1));
		
		JPanel ftCnt0Panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 1, 1));
		ftCnt0Panel.add(automateButton);
		
		JPanel ftCnt1Panel = new JPanel(new GridLayout(1, 3, 1, 1));
		ftCnt1Panel.add(initModelButton);
		ftCnt1Panel.add(findCorrectionButton);
		ftCnt1Panel.add(applyCorrectionButton);
			
		ftCntPanel.add(ftCnt0Panel);
		ftCntPanel.add(ftCnt1Panel);
		
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
		
		fittingPanel.add(ftUpPanel, BorderLayout.NORTH);		
		fittingPanel.add(ftDownPanel, BorderLayout.CENTER);
		fittingPanel.add(ftCntPanel, BorderLayout.SOUTH);
		
		JPanel rightPanel = new JPanel(new BorderLayout());
		rightPanel.add(fittingParamsPanel, BorderLayout.NORTH);
		rightPanel.add(fittingPanel, BorderLayout.CENTER);
		
		HEBTOrbitCorrectorMainPanel.add(tablePanel, BorderLayout.WEST);
		HEBTOrbitCorrectorMainPanel.add(rightPanel, BorderLayout.CENTER);
		
		//actions and initial state
		initModelButton.setEnabled(false);
		findCorrectionButton.setEnabled(false);
		applyCorrectionButton.setEnabled(false);
		
		restoreCorrectorsButton.setEnabled(false);
		
		memorizeCorrectorsButton.addActionListener(
			new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					messageTextLocal.setText(null);
					if(memorizeCurrents()) {
						restoreCorrectorsButton.setEnabled(true);
						initModelButton.setEnabled(true);
					} else {
						restoreCorrectorsButton.setEnabled(false);
					}
					posWheel.setValue(0.);
					angleWheel.setValue(0.);
					corrTableModel.fireTableDataChanged();
				}
			});
		
		restoreCorrectorsButton.addActionListener(
			new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					messageTextLocal.setText(null);
					restoreCurrents();
					initModelButton.setEnabled(false);
					findCorrectionButton.setEnabled(false);
					applyCorrectionButton.setEnabled(false);
					automateButton.setEnabled(false);
					posWheel.setValue(0.);
					angleWheel.setValue(0.);
					corrTableModel.fireTableDataChanged();
				}
			});

		initModelButton.addActionListener(
			new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					messageTextLocal.setText(null);
					if(setUpCorrectorsCoeff()){
						findCorrectionButton.setEnabled(true);
						automateButton.setEnabled(true);
						corrTableModel.fireTableDataChanged();
					}
				}
			});
		
		findCorrectionButton.addActionListener(
			new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					messageTextLocal.setText(null);
					if(findCorrection()){
						applyCorrectionButton.setEnabled(true);
						corrTableModel.fireTableDataChanged();
					}
				}
			});
		
    applyCorrectionButton.addActionListener(
			new ActionListener() {
				public void actionPerformed(ActionEvent e) {
				 applyCorrections();
				 corrTableModel.fireTableDataChanged();
				}
			});
		
		//whell actions
		PropertyChangeListener	wheelListener = new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent event) {
				if(automateButton.isSelected()){
					findCorrection();
					corrTableModel.fireTableDataChanged();
					applyCorrections();
				}
			}
		};
		posWheel.addPropertyChangeListener("value", wheelListener); 	
		angleWheel.addPropertyChangeListener("value", wheelListener); 		
	}


	/**
	 *  Returns the panel attribute of the HEBTOrbitCorrector object
	 *
	 *@return    The panel value
	 */
	public JPanel getPanel() {
		return HEBTOrbitCorrectorMainPanel;
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
	 *  Sets the accelSeq attribute of the HEBTOrbitCorrector object
	 *
	 *@param  accSeq  The new accelSeq value
	 */
	public void setAccelSeq(AcceleratorSeq accSeq,int dir) {
		this.accSeq = accSeq;
		corrV.clear();
		java.util.List<AcceleratorNode> corrs = null;
		if(dir == 0) {corrs = accSeq.getAllNodesOfType(HDipoleCorr.s_strType);}
		if(dir == 1) {corrs = accSeq.getAllNodesOfType(VDipoleCorr.s_strType);}
		for(int i = corrs.size() - 6, n = corrs.size(); i < n; i++) {
			if(i > 0){
				Electromagnet corr = (Electromagnet) corrs.get(i);
				Corr_Element corrElm = new Corr_Element(corr.getId(), corr);
				corrElm.setActive(true);
				corrV.add(corrElm);
			}
		}
		for(int i = 0, n = corrV.size(); i < n; i++){
			Corr_Element corrElm = corrV.get(i);
			if(i < n-2){
				corrElm.setActive(false);
			}
		}
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
					double val = corrElm.getLiveField() - corrElm.getFieldFromMemory();
					val = corrElm.getFieldFromMemory() + sign_TextField.getValue() * val;
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
	 *  Sets the fontForAll attribute of the HEBTOrbitCorrector object
	 *
	 *@param  fnt  The new fontForAll value
	 */
	public void setFontForAll(Font fnt) {

		corrTableBorder.setTitleFont(fnt);
		correctionControlBorder.setTitleFont(fnt);

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
		corrTable.getColumnModel().getColumn(3).setMaxWidth(6 * font_width);
		corrTable.getColumnModel().getColumn(3).setMinWidth(6 * font_width);
		corrTable.setPreferredScrollableViewportSize(new Dimension(1, 1));
		
		memorizeCorrectorsButton.setFont(fnt);
		restoreCorrectorsButton.setFont(fnt);
		
		initModelButton.setFont(fnt);
		findCorrectionButton.setFont(fnt);
		applyCorrectionButton.setFont(fnt);
		automateButton.setFont(fnt);
		
		posWheelLabel.setFont(fnt);
		angleWheelLabel.setFont(fnt);
		posResLabel.setFont(fnt);
		angleResLabel.setFont(fnt);
		
		posResTextField.setFont(fnt);
		angleResTextField.setFont(fnt);
		
		fieldWeightLabel.setFont(fnt);
		fieldWeightTextField.setFont(fnt); 
		useRealtiveToInitButton.setFont(fnt);
		posWeightLabel.setFont(fnt);
		angleWeightLabel.setFont(fnt);
		posWeightTextField.setFont(fnt);
		angleWeightTextField.setFont(fnt);
		
		sign_TextField.setFont(fnt);
	}

	/**
	* Returns the text field of the sign coefficient.
	*
	*@return    The sign coefficient X text value
	*/
	public DoubleInputTextField getSignText(){
		return sign_TextField;
	}

	/**
	 *  Returns the messageText attribute of the HEBTOrbitCorrector object
	 *
	 *@return    The messageText value
	 */
	public JTextField getMessageText() {
		return messageTextLocal;
	}

	/**
	 *  Sets the messageText attribute of the HEBTOrbitCorrector object
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
		 scenario.setSynchronizationMode(Scenario.SYNC_MODE_LIVE);
		 //scenario.setSynchronizationMode(Scenario.SYNC_MODE_RF_DESIGN);		 
		 
		 IAlgorithm tracker = null;
         
         try {
         
          tracker = AlgorithmFactory.createEnvTrackerAdapt(accSeq);
		 
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
			 return false;
		 }
		 
		 try {
			 scenario.run();
		 } catch(ModelException e) {
			 messageTextLocal.setText("Can not run scenario! Stop!");
			 return false;
		 }
		 
		 Trajectory<EnvelopeProbeState> trajectory = probe.getTrajectory();
         CalculationsOnBeams            cobCalcEng = new CalculationsOnBeams(trajectory);
		 
		 AcceleratorNode foil = accSeq.getNodeWithId("Ring_Inj:Foil");
		 
		 for(int i = 0, n = corrV.size(); i < n; i++) {
			 Corr_Element corrElm = corrV.get(i);
			 Electromagnet corr_mag = corrElm.getMagnet();
			 EnvelopeProbeState probeState = trajectory.statesForElement(corr_mag.getId()).get(0);
			 double W0 = probeState.getSpeciesRestEnergy();
			 double gamma = probeState.getGamma();
			 double beta = Math.sqrt(1.0 - 1.0 / (gamma * gamma));
			 double L = corr_mag.getEffLength();
			 double c = 2.997924E+8;
			 double res_coeff = (L * c) / (W0 * beta * gamma);
			 
			// CKA 8/22/2014
//			 PhaseMatrix phMatr = probe.stateResponse(corrElm.getName(),foil.getId()); 
			 PhaseMatrix phMatr = cobCalcEng.computeTransferMatrix(corrElm.getName(), foil.getId());
			 
			 double mPos = 0.;
			 double mAngle = 0.;
			 if(corrElm.getName().indexOf(":DCH") > 0) {
				 mPos = phMatr.getElem(0, 1);
				 mAngle = phMatr.getElem(1, 1);
			 }
			 if(corrElm.getName().indexOf(":DCV") > 0) {
				 mPos = phMatr.getElem(2, 3);
				 mAngle = phMatr.getElem(3, 3);
			 }
			 //result [mm/T] and [mrad/T]
			 double coeffPos = res_coeff * mPos * 1000.;
			 double coeffAngle = res_coeff * mAngle * 1000.;
			 corrElm.setPosCoeff(coeffPos);
			 corrElm.setAngleCoeff(coeffAngle);
			 corrElm.setIntermedField(corrElm.getFieldFromMemory());	
			 System.out.println("debug dc="+corrElm.getName()+" cP="+coeffPos+" cA="+coeffAngle);
		 }
		 return true;
	 }
	 
	/**
	 *  It finds the new correctors fields
	 */
	private boolean findCorrection() {
	
		//find the solution
		final double pos_goal = posWheel.getValue();
		final double angle_goal = angleWheel.getValue();

    corrOptV.clear();	
		corrProxyV.clear();
        
        problem = new Problem();
        
		
		for(int i = 0, n = corrV.size(); i < n; i++) {
			Corr_Element corrElm = corrV.get(i);
			if(corrElm.isActive()){
				double initVal = corrElm.getIntermedField();
				double lowerLimit = corrElm.getLowerFieldLimit();
				double upperLimit = corrElm.getUpperFieldLimit();
				//Variable proxy = new Variable(corrElm.getName(),initVal,upperLimit/20.0);
				Variable proxy = new Variable(corrElm.getName(), initVal, lowerLimit , upperLimit);
                
                InitialDelta hint = new InitialDelta( upperLimit/20.0 );
                
                problem.addVariable(proxy);
                problem.addHint(hint);
                
                
                corrProxyV.add(proxy);
				corrOptV.add(corrElm);
			}
		}		
		
		if(corrOptV.size() == 0){
			messageTextLocal.setText("No correctors to use!");
			return false;
		}
		
		final boolean useRelativeToInit = useRealtiveToInitButton.isSelected();
		
		min_sum = 10.0e+20;
		
		Scorer scorer = new Scorer(){     // TODO: CKA - The value is NEVER USED
			public double score(Trial trial, List<Variable> scoreVariables){
				//sum calculations
				double sum_fields = 0.;
				double sum_limits = 0.;
				double pos_new = 0.;
				double angle_new = 0.;
				for(int i = 0, n = corrProxyV.size(); i < n; i++) {
					Variable proxy = corrProxyV.get(i);
					Corr_Element corrElm = corrOptV.get(i);
					double value = proxy.getInitialValue();
					double upperLimit = corrElm.getUpperFieldLimit();
					double lowerLimit = corrElm.getLowerFieldLimit();
					if(value > 0.95*upperLimit){
						double r = (value - 0.95*upperLimit)*10.0e+5;
						sum_limits += r*r;
					}
					if(value < 0.95*lowerLimit){
						double r = (0.95*lowerLimit - value)*10.0e+5;
						sum_limits += r*r;
					}
					double rel_val = 0.;
					if(useRelativeToInit){
						rel_val = (value - corrElm.getFieldFromMemory())/upperLimit;
					} else {
						rel_val = (value)/upperLimit;
					}
					sum_fields +=  rel_val*rel_val;
					//System.out.println("debug value="+value+" live="+corrElm.getLiveField());
					value = value - corrElm.getFieldFromMemory();
					pos_new += corrElm.getPosCoeff()*value;
					angle_new += corrElm.getAngleCoeff()*value;
				}
				sum_fields *= fieldWeightTextField.getValue()/corrOptV.size();
				double d_pos = pos_new - pos_goal;
				double d_angle = angle_new - angle_goal;
				//System.out.println("debug angle_new="+angle_new+" angle_goal="+angle_goal);
				d_pos = d_pos*d_pos*posWeightTextField.getValue();
				d_angle = d_angle*d_angle*angleWeightTextField.getValue();
				double sum = sum_limits + d_pos + d_angle + sum_fields;	
				//System.out.println("debug sum="+sum);
				//if(min_sum > sum){
				//	min_sum = sum;
				//	System.out.println("debug sum="+sum);
				//	for(int i = 0, n = corrProxyV.size(); i < n; i++) {
				//		Variable proxy = corrProxyV.get(i);
				//		double value = proxy.getValue();	
				//		System.out.println("debug var="+proxy.getName()+" val="+value);
				//	}
				//}
				return sum;
			}
		};
		
		int maxIter = 1000;
		//SolveStopper stopper = SolveStopperFactory.maxIterationStopper(maxIter);
		//SearchAlgorithm algorithm = new SimplexSearchAlgorithm();
	  Solver solver = new Solver(SolveStopperFactory.maxEvaluationsStopper(maxIter));
	  //solver.setScorer(scorer);
	  //solver.setSearchAlgorithm(algorithm);
	  //solver.setStopper(stopper);
		//solver.setVariables(corrProxyV);
		
		solver.solve(problem);
		

		double pos_new = 0.;
		double angle_new = 0.;
		for(int i = 0, n = corrProxyV.size(); i < n; i++) {
					Variable proxy = corrProxyV.get(i);
					Corr_Element corrElm = corrOptV.get(i);
					double value = proxy.getInitialValue();
					corrElm.setIntermedField(value);				
					//System.out.println("debug best var="+proxy.getName()+" val="+value);
					pos_new += corrElm.getPosCoeff()*(value - corrElm.getFieldFromMemory());
					angle_new += corrElm.getAngleCoeff()*(value - corrElm.getFieldFromMemory());
					corrElm.setLiveField(value);
		}
		posResTextField.setValue(pos_new);
		angleResTextField.setValue(angle_new);
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
                
				public Class getColumnClass(int columnIndex) {  // TODO: CKA - Unchecked Conversion
					if(columnIndex == 0 || columnIndex == 1 || columnIndex == 2) {
						return String.class;
					}
					return Boolean.class;
				}

				public String getColumnName(int column) {
					if(column == 0) {
						return "Corrector";
					} else if(column == 1) {
						return "Guess B[T]";
					} else if(column == 2) {
						return "Memory B";
					}
					return "Use";
				}

				public boolean isCellEditable(int rowIndex, int columnIndex) {
					if(columnIndex == 3) {
						return true;
					}
					return false;
				}

				public int getRowCount() {
					return corrV.size();
				}

				public int getColumnCount() {
					return 4;
				}

				public Object getValueAt(int row, int column) {
					Corr_Element elm = corrV.get(row);
					if(column == 0) {
						return elm.getName();
					} else if(column == 1) {
						return elm.format(elm.getLiveField());
					} else if(column == 2) {
						return elm.format(elm.getFieldFromMemory());
					}
					return elm.isActiveObj();
				}

				public void setValueAt(Object aValue, int row, int column) {
					if(column == 3) {
						Corr_Element elm = corrV.get(row);
						elm.setActive(!elm.isActive());
						fireTableCellUpdated(row, column);
					}
				}
			};

		corrTable.setModel(corrTableModel);

	}

}

