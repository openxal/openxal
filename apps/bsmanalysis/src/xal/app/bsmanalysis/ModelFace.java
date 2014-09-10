/*
 * ModelFace.java
 *
 */
package xal.app.bsmanalysis;

import xal.extension.widgets.swing.*;
import xal.tools.apputils.EdgeLayout;
import xal.tools.messaging.*;
import xal.ca.*;
import xal.tools.data.*;
import java.text.NumberFormat;

import java.util.*;
import java.io.*;
import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;

import xal.extension.application.smf.*;
import xal.smf.data.*;
import xal.smf.*;
import xal.smf.impl.*;
import xal.model.*;
import xal.model.alg.*;
import xal.sim.scenario.*;
import xal.model.probe.*;
import xal.model.probe.traj.*;
import xal.model.xml.*;
//import gov.sns.tools.optimizer.*;
import xal.tools.beam.Twiss;
import xal.extension.widgets.plot.*;
import java.text.NumberFormat;
import xal.extension.widgets.swing.DecimalField;
import xal.tools.apputils.EdgeLayout;
import xal.tools.data.*;
import xal.tools.xml.XmlDataAdaptor;
import xal.tools.beam.*;
import java.text.DecimalFormat;
import xal.extension.solver.*;
//import xal.tools.formula.*;
import xal.extension.solver.hint.*;
import xal.extension.solver.algorithm.*;
import xal.extension.solver.market.*;
import xal.extension.solver.solutionjudge.*;
import xal.service.pvlogger.sim.PVLoggerDataSource;
import xal.extension.widgets.apputils.SimpleProbeEditor;
import xal.tools.beam.IConstants;
import xal.sim.scenario.ProbeFactory;
//import gov.sns.xal.smf.proxy.RFCavityPropertyAccessor;

/**
 * Performs matching to find steerer strengths for desired injection
 * spot position and angle on the foil.
 * @author  cp3
 */

public class ModelFace extends JPanel{
    /** ID for serializable version */
    private static final long serialVersionUID = 1L;
    
    private AcceleratorSeq seq;
    private Scenario scenario;
    private EnvelopeProbe probe;
	private EnvelopeProbe initprobe;
    private JButton probeeditbutton;
	private PVLoggerDataSource plds;
    private Accelerator accl = new Accelerator();
    private DecimalField[] zdat = new DecimalField[4];
	private DecimalField[] amp = new DecimalField[4];
	private DecimalField[] initamp = new DecimalField[4];
	private DecimalField satfield;
    private NumberFormat numFor;
	private NumberFormat numfor;
    private JPanel resultsPanel;
    private JPanel initPanel;
	private JPanel ampPanel;
    private FunctionGraphsJPanel plotpanel;
    
    //private JButton sendtoharpbutton;
    private JPanel mainPanel;
    JTable resultstable;
	JTable twisstable;
	JButton solvebutton;
    JButton loadbutton;
	JButton restoreampbutton;
	JButton singlepassbutton;
	JButton resetinitbutton;
	JButton setlimits;
    JScrollPane resultsscrollpane;
    ResultsTableModel resultstablemodel;
    JScrollPane twissscrollpane;
    ResultsTableModel twisstablemodel;
    EdgeLayout layout = new EdgeLayout();
    JLabel BSM107label = new JLabel("BSM107: ");
    JLabel BSM109label = new JLabel("BSM109: ");
    JLabel BSM111label = new JLabel("BSM111: ");
    JLabel BSM410label = new JLabel("BSM410: ");
    JLabel devicelabel = new JLabel("Device");
    JLabel zlabel = new JLabel("Z (deg) ");
    JLabel uselabel = new JLabel("Use");
	
	JLabel CCL1label = new JLabel("CCL1: ");
    JLabel CCL2label = new JLabel("CCL2: ");
    JLabel CCL3label = new JLabel("CCL3: ");
    JLabel CCL4label = new JLabel("CCL4: ");
	
	JLabel amplabel = new JLabel("Cavity Amp ");
    JLabel ccldevicelabel = new JLabel("Cavity");
    JLabel twisstablelabel = new JLabel("Twiss Matching Results: Reported at BSM107");
    JLabel resultstablelabel = new JLabel("Beam Size Results");
    JLabel beamcurrentlabel = new JLabel("Beam current (mA): ");
	JLabel satisfaction = new JLabel("Satisfaction: ");
    
    JCheckBox BSM107box = new JCheckBox();
    JCheckBox BSM109box = new JCheckBox();
    JCheckBox BSM111box = new JCheckBox();
    JCheckBox BSM410box = new JCheckBox();
    //JCheckBox harpbox = new JCheckBox();
    
    Object[][] tabledata = new Object[8][3];
    Object[][] twissdata = new Object[3][3];
    GridLimits limits = new GridLimits();
    
    //ArrayList xsigmas = new ArrayList();
    // ArrayList ysigmas = new ArrayList();
    ArrayList<Double> zdatalist = new ArrayList<Double>();
    ArrayList<String> namelist = new ArrayList<String>();
    // ArrayList fullnamelist = new ArrayList();
    //ArrayList xfulldatalist = new ArrayList();
    //ArrayList yfulldatalist = new ArrayList();
	
    GenDocument doc;
    JComboBox<String> syncstate;
	JComboBox<String> plotunitbox;
	String[] plotunits = {"Show in mm", "Show in degrees"};
    String latticestate = "PVLogger";
    String[] syncstates = {"Model Live Lattice", "Model PV Logger Lattice"};
    JTextField pvloggerfield;
    JTextField solvertimefield;
	JTextField beamcurrentfield;
    JLabel usepvlabel;
    JLabel solvertime;
    Integer pvloggerid = new Integer(0);
    //double alphaz0, betaz0;
    boolean firstpass=true;
	double alphax0;
    double betax0;
    double alphay0;
    double betay0;
	double alphaz0;
	double betaz0;
	double emitz0;
	double emitx0;
	double emity0;
	double alphaZmin=0.0;
	double alphaZmax=4.0;
    double emitZmax=1;
    double emitZmin=0.0;
    double betaZmin=0;
    double betaZmax=40;
    boolean limitsWereSet = false;
    double currentenergy = 1e9;
    //double c = 2.99e8;
    double c = IConstants.LightSpeed;
	double currentI = 1.0;
    double currentcharge = 0.0;
    double[] currenttwiss = new double[3];
    double windowratio = 0.0;
    double targetratio = 0.0;
    double harparea=0.0;
	double rffrequency = 805e6;
    DataTable fitsdatabase;
    
    public ModelFace(GenDocument aDocument, JPanel mainpanel) {
		doc=aDocument;
		setPreferredSize(new Dimension(950,850));
		setLayout(layout);
		init();
		setAction();
		addcomponents();
    }
    
	public void addcomponents(){
        
		layout.setConstraints(mainPanel, 0, 0, 0, 0, EdgeLayout.ALL_SIDES, EdgeLayout.GROW_BOTH);
		this.add(mainPanel);
		
		EdgeLayout newlayout = new EdgeLayout();
		mainPanel.setLayout(newlayout);
		GridLayout initgrid = new GridLayout(6, 4);
		GridLayout ampgrid = new GridLayout(6, 3);
		newlayout.setConstraints(syncstate, 17, 20, 100, 10, EdgeLayout.LEFT, EdgeLayout.NO_GROWTH);
		/*
         mainPanel.add(syncstate);
         newlayout.setConstraints(usepvlabel, 20, 220, 100, 10, EdgeLayout.LEFT, EdgeLayout.NO_GROWTH);
         mainPanel.add(usepvlabel);
         newlayout.setConstraints(pvloggerfield, 20, 310, 100, 10, EdgeLayout.LEFT, EdgeLayout.NO_GROWTH);
         mainPanel.add(pvloggerfield);
         */
		
		initPanel.setLayout(initgrid);
		initPanel.add(devicelabel); initPanel.add(zlabel); initPanel.add(uselabel);
		initPanel.add(BSM107label); initPanel.add(zdat[0]); initPanel.add(BSM107box);
		initPanel.add(BSM109label); initPanel.add(zdat[1]); initPanel.add(BSM109box);
		initPanel.add(BSM111label); initPanel.add(zdat[2]); initPanel.add(BSM111box);
		initPanel.add(BSM410label); initPanel.add(zdat[3]); initPanel.add(BSM410box);
		newlayout.setConstraints(initPanel, 0, 145, 100, 10, EdgeLayout.LEFT, EdgeLayout.NO_GROWTH);
		mainPanel.add(initPanel);
		newlayout.setConstraints(loadbutton, 50, 5, 150, 10, EdgeLayout.LEFT, EdgeLayout.NO_GROWTH);
		mainPanel.add(loadbutton);
		
		ampPanel.setLayout(ampgrid);
		ampPanel.add(ccldevicelabel); ampPanel.add(amplabel);
		ampPanel.add(CCL1label); ampPanel.add(amp[0]);
		ampPanel.add(CCL2label); ampPanel.add(amp[1]);
		ampPanel.add(CCL3label); ampPanel.add(amp[2]);
		ampPanel.add(CCL4label); ampPanel.add(amp[3]);
		newlayout.setConstraints(ampPanel, 0, 500, 100, 10, EdgeLayout.LEFT, EdgeLayout.NO_GROWTH);
		mainPanel.add(ampPanel);
		newlayout.setConstraints(restoreampbutton, 145, 485, 150, 10, EdgeLayout.LEFT, EdgeLayout.NO_GROWTH);
		mainPanel.add(restoreampbutton);
        
		//newlayout.setConstraints(probeeditbutton, 205, 0, 100, 10, EdgeLayout.LEFT, EdgeLayout.NO_GROWTH);
		//mainPanel.add(probeeditbutton);
		newlayout.setConstraints(beamcurrentlabel, 140, 10, 150, 10, EdgeLayout.LEFT, EdgeLayout.NO_GROWTH);
		mainPanel.add(beamcurrentlabel);
		newlayout.setConstraints(beamcurrentfield, 135, 140, 150, 10, EdgeLayout.LEFT, EdgeLayout.NO_GROWTH);
		mainPanel.add(beamcurrentfield);
		newlayout.setConstraints(setlimits, 165, 0, 150, 10, EdgeLayout.LEFT, EdgeLayout.NO_GROWTH);
		mainPanel.add(setlimits);
		newlayout.setConstraints(singlepassbutton, 195, 0, 150, 10, EdgeLayout.LEFT, EdgeLayout.NO_GROWTH);
		mainPanel.add(singlepassbutton);
		newlayout.setConstraints(solvertime, 240, 10, 100, 10, EdgeLayout.LEFT, EdgeLayout.NO_GROWTH);
		mainPanel.add(solvertime);
		newlayout.setConstraints(solvertimefield, 235, 110, 100, 10, EdgeLayout.LEFT, EdgeLayout.NO_GROWTH);
		mainPanel.add(solvertimefield);
		newlayout.setConstraints(solvebutton, 235, 230, 150, 10, EdgeLayout.LEFT, EdgeLayout.NO_GROWTH);
		mainPanel.add(solvebutton);
		newlayout.setConstraints(satisfaction, 240, 360, 150, 10, EdgeLayout.LEFT, EdgeLayout.NO_GROWTH);
		mainPanel.add(satisfaction);
		newlayout.setConstraints(satfield, 235, 450, 150, 10, EdgeLayout.LEFT, EdgeLayout.NO_GROWTH);
		mainPanel.add(satfield);
		newlayout.setConstraints(resetinitbutton, 275, 0, 100, 10, EdgeLayout.LEFT, EdgeLayout.NO_GROWTH);
		mainPanel.add(resetinitbutton);
		
		resultsPanel.add(twisstablelabel);
		resultsPanel.add(twissscrollpane);
		newlayout.setConstraints(resultsPanel, 350, 5, 200, 10, EdgeLayout.LEFT, EdgeLayout.NO_GROWTH);
		mainPanel.add(resultsPanel);
		
		newlayout.setConstraints(plotpanel, 550, 5, 100, 10, EdgeLayout.LEFT, EdgeLayout.NO_GROWTH);
		mainPanel.add(plotpanel);
		newlayout.setConstraints(plotunitbox, 800, 5, 10, 10, EdgeLayout.LEFT, EdgeLayout.NO_GROWTH);
		mainPanel.add(plotunitbox);
    }
    
    public void init(){
        
		mainPanel = new JPanel();
		mainPanel.setPreferredSize(new Dimension(950,850));
		
		resultsPanel = new JPanel();
		resultsPanel.setPreferredSize(new Dimension(400,130));
		resultsPanel.setBorder(BorderFactory.createTitledBorder("Results"));
		
		initPanel = new JPanel();
		initPanel.setPreferredSize(new Dimension(250,130));
		initPanel.setBorder(BorderFactory.createTitledBorder("Measured RMS Values"));
		
		ampPanel = new JPanel();
		ampPanel.setPreferredSize(new Dimension(180,130));
		ampPanel.setBorder(BorderFactory.createTitledBorder("Cavity Amp Settings"));
		
		plotpanel = new FunctionGraphsJPanel();
		plotpanel.setPreferredSize(new Dimension(800, 230));
		plotpanel.setGraphBackGroundColor(Color.WHITE);
		
		solvebutton = new JButton("Solve");
		singlepassbutton = new JButton("Single Pass");
		loadbutton = new JButton("Load Fits");
		restoreampbutton = new JButton("Restore Design Amplitudes");
		resetinitbutton = new JButton("Reset Initial Twiss to Design");
		setlimits = new JButton("Set Initial Twiss");
		plotunitbox = new JComboBox<String>(plotunits);
		numFor = NumberFormat.getNumberInstance();
		numFor.setMinimumFractionDigits(4);
		numfor = NumberFormat.getNumberInstance();
		numfor.setMinimumFractionDigits(10);
        
		accl = XMLDataManager.loadDefaultAccelerator();
		seq=accl.getComboSequence("CCL");
		amp[0] = new DecimalField(((RfCavity)(seq.getNodeWithId("CCL1"))).getDfltCavAmp(), 4, numFor);
		amp[1] = new DecimalField(((RfCavity)(seq.getNodeWithId("CCL2"))).getDfltCavAmp(), 4, numFor);
		amp[2] = new DecimalField(((RfCavity)(seq.getNodeWithId("CCL3"))).getDfltCavAmp(), 4, numFor);
		amp[3] = new DecimalField(((RfCavity)(seq.getNodeWithId("CCL4"))).getDfltCavAmp(), 4, numFor);
		initamp[0] = new DecimalField((amp[0].getDoubleValue()), 4, numFor);
		initamp[1] = new DecimalField((amp[1].getDoubleValue()), 4, numFor);
		initamp[2] = new DecimalField((amp[2].getDoubleValue()), 4, numFor);
		initamp[3] = new DecimalField((amp[3].getDoubleValue()), 4, numFor);
		
		BSM107box.setSelected(true);
		BSM109box.setSelected(true);
		BSM111box.setSelected(true);
		BSM410box.setSelected(true);
        
		for(int i = 0; i<4; i++){
			zdat[i] = new DecimalField(0.0, 4, numFor);
		}
		satfield = new DecimalField(0.0, 4, numFor);
		firstpass = true;
		usepvlabel = new JLabel("PV Logger ID: ");
		pvloggerfield = new JTextField(8);
		solvertime = new JLabel("Solver time (s): ");
		solvertimefield = new JTextField(8);
		solvertimefield.setText("20");
		beamcurrentfield = new JTextField(8);
		beamcurrentfield.setText("30.0");
		syncstate = new JComboBox<String>(syncstates);
		probeeditbutton = new JButton("Edit Model Probe");
		probeeditbutton.setEnabled(false);
		
		makeTwissTable();
		twisstablemodel.setValueAt(new String("Alpha"),0,0);
		twisstablemodel.setValueAt(new String("Beta"),1,0);
		twisstablemodel.setValueAt(new String("Emit"),2,0);
        
    }
    
    
    
    public void setAction(){
        
		solvebutton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent ae){
                solve();
			}
		});
		
		loadbutton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent ae){
                loadFits();
			}
		});
		
		restoreampbutton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent ae){
                restoreDesignAmps();
            }
        });
        
		resetinitbutton.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent ae){
                firstpass = true;
                limitsWereSet = false;
            }
        });
		
		syncstate.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
                if(syncstate.getSelectedIndex() == 0){
                    latticestate="Live";
                    System.out.println("Using live.");
                }
                if(syncstate.getSelectedIndex() == 1){
                    latticestate="PVLogger";
                    System.out.println("Using PVlogger.");
                }
			}
		});
		
		probeeditbutton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
                SimpleProbeEditor spe = new SimpleProbeEditor(null, scenario.getProbe());
                //spe.createSimpleProbeEditor(scenario.getProbe());
                
			}
		});
		
		singlepassbutton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent ae){
                singlePass();
            }
        });
		
		setlimits.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
                final JDialog limitdialog = new JDialog();
                limitdialog.setLayout(new GridLayout(5,4));
                limitdialog.add(new JLabel("Twiss"));
                limitdialog.add(new JLabel("Inital"));
                limitdialog.add(new JLabel("Lower"));
                limitdialog.add(new JLabel("Upper"));
                
                final JTextField alphaZinitial = new JTextField(10);
                final JTextField alphaZlower = new JTextField(10);
                final JTextField alphaZupper = new JTextField(10);
                final JTextField betaZinitial = new JTextField(10);
                final JTextField betaZlower = new JTextField(10);
                final JTextField betaZupper = new JTextField(10);
                final JTextField emitZinitial = new JTextField(10);
                final JTextField emitZlower = new JTextField(10);
                final JTextField emitZupper = new JTextField(10);
                alphaZinitial.setText(numfor.format(alphaz0)); alphaZlower.setText(numfor.format(alphaZmin)); alphaZupper.setText(numfor.format(alphaZmax));
                betaZinitial.setText(numfor.format(betaz0)); betaZlower.setText(numfor.format(betaZmin)); betaZupper.setText(numfor.format(betaZmax));
                emitZinitial.setText(numfor.format(emitz0)); emitZlower.setText(numfor.format(emitZmin)); emitZupper.setText(numfor.format(emitZmax));
                limitdialog.add(new JLabel("Alphaz"));
                limitdialog.add(alphaZinitial);
                limitdialog.add(alphaZlower);
                limitdialog.add(alphaZupper);
                limitdialog.add(new JLabel("Betaz [m]         "));
                limitdialog.add(betaZinitial);
                limitdialog.add(betaZlower);
                limitdialog.add(betaZupper);
                limitdialog.add(new JLabel("Emitz [m radians]"));
                limitdialog.add(emitZinitial);
                limitdialog.add(emitZlower);
                limitdialog.add(emitZupper);
                JButton set = new JButton("Set");
                limitdialog.add(set);
                limitdialog.pack();
                limitdialog.setVisible(true);
                set.addActionListener(new ActionListener(){
                    public void actionPerformed(ActionEvent e){
                        alphaz0=Double.parseDouble(alphaZinitial.getText());
                        alphaZmin = Double.parseDouble(alphaZlower.getText());
                        alphaZmax = Double.parseDouble(alphaZupper.getText());
                        betaz0=Double.parseDouble(betaZinitial.getText());
                        betaZmin = Double.parseDouble(betaZlower.getText());
                        betaZmax = Double.parseDouble(betaZupper.getText());
                        emitz0 = Double.parseDouble(emitZinitial.getText());
                        emitZmin = Double.parseDouble(emitZlower.getText());
                        emitZmax = Double.parseDouble(emitZupper.getText());
                        limitsWereSet = true;
                        limitdialog.setVisible(false);
                    }
                });
            }
        });
		
	}
    
    
    
	public void singlePass() {
		initModel();
		probe = EnvelopeProbe.newInstance(initprobe);
		try {
			//scenario = Scenario.newScenarioFor(seq);
			scenario.setProbe(probe);
			scenario.setSynchronizationMode(Scenario.SYNC_MODE_DESIGN);
			//scenario.setStartNode((String)namelist.get(0));
			scenario.resync();
			scenario.run();
		}
		catch(Exception exception){
			exception.printStackTrace();
		}
        
		resetPlot();
        
        
    }
	
	
    public void solve() {
		initModel();
		ArrayList<Variable> variables =  new ArrayList<Variable>();
		if(limitsWereSet){
			variables.add(new Variable("alphaZ", alphaz0, alphaZmin, alphaZmax));
			variables.add(new Variable("betaZ", betaz0, betaZmin, betaZmax));
			variables.add(new Variable("emitZ", emitz0, emitZmin, emitZmax));
		}
		else{
			variables.add(new Variable("alphaZ", alphaz0, -4.0, 4.0));
			variables.add(new Variable("betaZ", betaz0, 0, 20));
			variables.add(new Variable("emitZ", emitz0, 0, emitz0*10.0));
		}
		
		ArrayList<Objective> objectives = new ArrayList<Objective>();
		objectives.add(new TargetObjective( "diff", 0.0 ) );
        
		Evaluator1 evaluator = new Evaluator1( objectives, variables );
        
		Problem problem = new Problem( objectives, variables, evaluator );
		problem.addHint(new InitialDelta( 0.1 ) );
		
		double solvetime= new Double(Double.parseDouble(solvertimefield.getText()));
		Stopper maxSolutionStopper = SolveStopperFactory.minMaxTimeSatisfactionStopper( 1, solvetime, 0.99999 );
		Solver solver = new Solver(new RandomShrinkSearch(), maxSolutionStopper );
        
		solver.solve( problem );
		System.out.println("score is " + solver.getScoreBoard());
		Trial best = solver.getScoreBoard().getBestSolution();
		
		// rerun with solution to populate results table
		calcError(variables, best);
		updateProbe(variables, best);
		
		System.out.println("Done with solve");
		try{
			scenario.setProbe(probe);
			//scenario.setStartNode((String)namelist.get(0));
			scenario.resync();
			scenario.run();
		}
		catch(Exception exception){
			exception.printStackTrace();
		}
		//Print the final results at the desired locations
        
		//resetTwissTable(variables, best);
		satfield.setValue(best.getSatisfaction());
		//resetResultsTable();
		resetPlot();
		firstpass = false;
		limitsWereSet = false;
		resetTwissTable(variables, best);
		alphaz0 = currenttwiss[0];
		betaz0 = currenttwiss[1];
		emitz0 = currenttwiss[2];
		
        
        
    }
    
    public void initModel(){
        
		zdatalist.clear();
		namelist.clear();
		
		if(BSM107box.isSelected() == true){
			namelist.add("CCL_Mag:QTV107");
			//namelist.add("CCL_Mag:DCH106");
			zdatalist.add(zdat[0].getDoubleValue());
		}
		if(BSM109box.isSelected() == true){
			namelist.add("CCL_Mag:QTV109");
			zdatalist.add(zdat[1].getDoubleValue());
		}
		if(BSM111box.isSelected() == true){
			namelist.add("CCL_Mag:QTV111");
			zdatalist.add(zdat[2].getDoubleValue());
		}
		if(BSM410box.isSelected() == true){
			namelist.add("CCL_Mag:QV411");
			//namelist.add("CCL_Diag:BLM408");
			zdatalist.add(zdat[3].getDoubleValue());
		}
		
        IAlgorithm etracker = null;
        
        try {
            
            etracker = AlgorithmFactory.createEnvTrackerAdapt(seq);
            
        } catch ( InstantiationException exception ) {
            System.err.println( "Instantiation exception creating tracker." );
            exception.printStackTrace();
        }
        
        
        probe = ProbeFactory.getEnvelopeProbe(seq, etracker);
		initprobe = ProbeFactory.getEnvelopeProbe(seq, etracker);
		//Run the model once to get the Twiss state at the first BSM.
		try {
			scenario = Scenario.newScenarioFor(seq);
			setCavityAmps();
			scenario.setProbe(probe);
			scenario.setSynchronizationMode(Scenario.SYNC_MODE_DESIGN);
			scenario.resync();
			scenario.run();
		}
		catch(Exception exception){
			exception.printStackTrace();
		}
		Trajectory<EnvelopeProbeState> traj= probe.getTrajectory();
		//resetPlot();
		
		//Convert units to meters for each BSM rms
		ArrayList<Double> templist = new ArrayList<Double>(zdatalist);
		Iterator<Double> itr = templist.iterator();
		System.out.println("namelist is " + namelist);
		for(int i=0; i<namelist.size(); i++){
			EnvelopeProbeState state = traj.statesForElement(namelist.get(i)).get(0);
			double T = state.getKineticEnergy();
			double m = state.getSpeciesRestEnergy();
			double gamma = T/m + 1;
			double beta = Math.abs(Math.sqrt(1-1/(gamma*gamma)));
			double v = beta*c;
			double sigma_rms = zdat[i].getDoubleValue();
			double tm = sigma_rms*Math.PI/180/(rffrequency*2*Math.PI);
			double d = v*tm;
			zdatalist.set(i, d);
			//System.out.println("zdata is at " + i + " is " + zdatalist.get(i) + " at element " + namelist.get(i) + " with energy " + T + " and v " + v);
		}
		EnvelopeProbeState state = traj.statesForElement("Begin_Of_CCL").get(0);
        
        CovarianceMatrix covarianceMatrix = state.getCovarianceMatrix();
        Twiss[] twiss = covarianceMatrix.computeTwiss();
		
		alphax0=twiss[0].getAlpha();
		betax0=twiss[0].getBeta();
		alphay0=twiss[1].getAlpha();
		betay0=twiss[1].getBeta();
		emitx0 = twiss[0].getEmittance();
		emity0 = twiss[1].getEmittance();
		if(!limitsWereSet){
			alphaz0 = twiss[2].getAlpha();
			betaz0 = twiss[2].getBeta();
			emitz0 = twiss[2].getEmittance();
		}
		
		if(!firstpass && !limitsWereSet){
			alphaz0 = currenttwiss[0];
			betaz0 = currenttwiss[1];
			emitz0 = currenttwiss[2];
			System.out.println("starting with Twiss " + alphaz0 + " " + betaz0 + " " + emitz0);
		}
		
		Twiss xTwiss = new Twiss(alphax0, betax0, emitx0);
		Twiss yTwiss = new Twiss(alphay0, betay0, emity0);
		Twiss zTwiss = new Twiss(alphaz0, betaz0, emitz0);
		Twiss[] tw = new Twiss[3];
		tw[0]=xTwiss;
		tw[1]=yTwiss;
		tw[2]=zTwiss;
		//System.out.println("twiss is " + twiss[0].getBeta() + " " + twiss[0].getAlpha() + " for " + state.getElementId() + " and E " + state.getKineticEnergy());
		System.out.println("Starting with az, bz, ez = " + alphaz0 +  " " + betaz0 + " " + emitz0 + " and radius " + twiss[1].getEnvelopeRadius() );
		
		Double current = new Double(Double.parseDouble(beamcurrentfield.getText()));
		double charge = current.doubleValue()/1000.0/(rffrequency/2);
		
		initprobe.setBeamCurrent(current.doubleValue());
		//initprobe.setBeamCharge(charge);
		System.out.println("Current is " + initprobe.getBeamCurrent());
		initprobe.initFromTwiss(tw);
		probe = EnvelopeProbe.newInstance(initprobe);
        
	}
    
    
    public double calcError(ArrayList<Variable> vars, Trial trial){
        updateProbe(vars, trial);
        probe.reset();
        try{
			scenario.setProbe(probe);
			scenario.resync();
            scenario.run();
		}
		catch(Exception exception){
			exception.printStackTrace();
		}
        
		Trajectory<EnvelopeProbeState> traj= probe.getTrajectory();
		double error = 0.0;
		int size = namelist.size();
		double rz=0;
        for(int i =0; i<size; i++){
			String name = namelist.get(i);
			EnvelopeProbeState state = traj.statesForElement(name).get(0);
            
            CovarianceMatrix covarianceMatrix = state.getCovarianceMatrix();
            Twiss[] twiss = covarianceMatrix.computeTwiss();
            
            
			rz =  twiss[2].getEnvelopeRadius();
			//System.out.println("rz is " + rz + " compared to " + zdatalist.get(i));
			error += Math.pow( 1000*(rz - (zdatalist.get(i)).doubleValue()), 2.);
		}
		error = Math.sqrt(error);
		//System.out.println("Error is " + error);
		return error;
    }
    
    void updateProbe(ArrayList<Variable> vars, Trial trial){
        
		double alphaz=0.0;
		double betaz=0.0;
		double emitz=0.0;
		probe = EnvelopeProbe.newInstance(initprobe);
		
		Iterator<Variable> itr = vars.iterator();
		while(itr.hasNext()){
			Variable variable = itr.next();
			double value = trial.getTrialPoint().getValue(variable);
			String name = variable.getName();
			if(name.equalsIgnoreCase("alphaZ")) alphaz = value;
			if(name.equalsIgnoreCase("betaZ")) betaz = value;
			if(name.equalsIgnoreCase("emitZ")) emitz = value;
		}
		Twiss xTwiss = new Twiss(alphax0, betax0, emitx0);
		Twiss yTwiss = new Twiss(alphay0, betay0, emity0);
		Twiss zTwiss = new Twiss(alphaz, betaz, emitz);
        
        Twiss[] tw= new Twiss[3];
        tw[0]=xTwiss;
        tw[1]=yTwiss;
        tw[2]=zTwiss;
        
        probe.initFromTwiss(tw);
		
    }
    
    
    public void makeTwissTable(){
		String[] colnames = {"Parameter", "Results (MKS)", "Results (Parmilla)"};
        
		twisstablemodel = new ResultsTableModel(colnames,3);
		
		twisstable = new JTable(twisstablemodel);
		twisstable.getColumnModel().getColumn(0).setMinWidth(130);
		twisstable.getColumnModel().getColumn(1).setMinWidth(145);
		twisstable.getColumnModel().getColumn(2).setMinWidth(145);
		
		twisstable.setPreferredScrollableViewportSize(twisstable.getPreferredSize());
		twisstable.setRowSelectionAllowed(false);
		twisstable.setColumnSelectionAllowed(false);
		twisstable.setCellSelectionEnabled(true);
		twissscrollpane = new JScrollPane(twisstable,JScrollPane.VERTICAL_SCROLLBAR_NEVER, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		twissscrollpane.setColumnHeaderView(twisstable.getTableHeader());
		twisstable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		twissscrollpane.setPreferredSize(new Dimension(375, 68));
    }
    
    public void resetTwissTable(ArrayList<Variable> vars, Trial trial){
		Iterator<Variable> itr = vars.iterator();
		double T = initprobe.getKineticEnergy();
		double m = initprobe.getSpeciesRestEnergy();
		double gamma = T/m + 1;
		double beta = Math.abs(Math.sqrt(1-1/(gamma*gamma)));
		double v = beta*c;
		
        while(itr.hasNext()){
			Variable variable = itr.next();
            double value = trial.getTrialPoint().getValue(variable);
			//System.out.println("value is " + value);
            String name = variable.getName();
			DecimalFormat decfor =  new DecimalFormat("###.000");
			if(name.equalsIgnoreCase("alphaz")){
				//twisstablemodel.setValueAt(new Double(decfor.format(value)),0,1);
				//twisstablemodel.setValueAt(new Double(decfor.format(value)),0,2);
				currenttwiss[0]=value;
			}
			if(name.equalsIgnoreCase("betaz")){
				//twisstablemodel.setValueAt(new Double(decfor.format(value)),1,1);
				currenttwiss[1]=value;
				//double parmvalue = value * (rffrequency*2*Math.PI) / v * 180.0/Math.PI;
				//twisstablemodel.setValueAt(new Double(decfor.format(parmvalue)),1,2);
			}
			if(name.equalsIgnoreCase("emitz")){
				//twisstablemodel.setValueAt(new Double(decfor.format(value*1e6)),2,1);
				currenttwiss[2]=value;
				//double parmvalue = value * (rffrequency*2*Math.PI) * 180.0/Math.PI / 1e6;
				//twisstablemodel.setValueAt(new Double(decfor.format(parmvalue)),2,2);
			}
		}
		twisstablemodel.fireTableDataChanged();
    }
	
	
	public void resetResultsTable(Twiss[] twiss){
		
		DecimalFormat decfor =  new DecimalFormat("###.000");
		double T = initprobe.getKineticEnergy();
		double m = initprobe.getSpeciesRestEnergy();
		double gamma = T/m + 1;
		double beta = Math.abs(Math.sqrt(1-1/(gamma*gamma)));
		double v = beta*c;
        
		twisstablemodel.setValueAt(new Double(decfor.format(twiss[2].getAlpha())),0,1);
		twisstablemodel.setValueAt(new Double(decfor.format(twiss[2].getAlpha())),0,2);
		currenttwiss[0]=twiss[2].getAlpha();
        
		twisstablemodel.setValueAt(new Double(decfor.format(twiss[2].getBeta())),1,1);
		currenttwiss[1]=twiss[2].getBeta();
		double parmvalue = twiss[2].getBeta() * (rffrequency*2*Math.PI) / v * 180.0/Math.PI;
		twisstablemodel.setValueAt(new Double(decfor.format(parmvalue)),1,2);
        
		twisstablemodel.setValueAt(new Double(decfor.format(twiss[2].getEmittance()*1e6)),2,1);
		currenttwiss[2]=twiss[2].getEmittance();
		parmvalue = twiss[2].getEmittance() * (rffrequency*2*Math.PI) * 180.0/Math.PI / 1e6;
		twisstablemodel.setValueAt(new Double(decfor.format(parmvalue)),2,2);
        
		twisstablemodel.fireTableDataChanged();
    }
	
	
	
    
    private void loadFits(){
		ArrayList<String> tabledata = new ArrayList<String>();
		if(doc.resultMap.size() == 0){
			System.out.println("No data available to load!");
		}
		else{
			Set<String> keys = doc.resultMap.keySet();
			Iterator<String> itr = keys.iterator();
            
			while(itr.hasNext()){
                //tabledata.clear();
                double sigma_rms;
                String tempfilename = itr.next();
                String bsmname = (String)((HashMap)doc.resultMap.get(tempfilename)).get("name");
                Boolean statrms = (Boolean)((HashMap)doc.resultMap.get(tempfilename)).get("statrms");
                if(statrms)
                    sigma_rms = ((Double)((HashMap)doc.resultMap.get(tempfilename)).get("rms")).doubleValue();
                else
                    sigma_rms = ((Double)((HashMap)doc.resultMap.get(tempfilename)).get("sigma")).doubleValue();
                
                if(bsmname.equals("107"))
                    zdat[0].setValue(sigma_rms);
                if(bsmname.equals("109"))
                    zdat[1].setValue(sigma_rms);
                if(bsmname.equals("111"))
                    zdat[2].setValue(sigma_rms);
                if(bsmname.equals("410"))
                    zdat[3].setValue(sigma_rms);
            }
		}
    }
    
    private void setCavityAmps(){
		((RfCavity)(seq.getNodeWithId("CCL1"))).setDfltCavAmp(amp[0].getDoubleValue());
		((RfCavity)(seq.getNodeWithId("CCL2"))).setDfltCavAmp(amp[1].getDoubleValue());
		((RfCavity)(seq.getNodeWithId("CCL3"))).setDfltCavAmp(amp[2].getDoubleValue());
		((RfCavity)(seq.getNodeWithId("CCL4"))).setDfltCavAmp(amp[3].getDoubleValue());
	}
	
	private void restoreDesignAmps(){
		amp[0].setValue(initamp[0].getValue());
		amp[1].setValue(initamp[1].getValue());
		amp[2].setValue(initamp[2].getValue());
		amp[3].setValue(initamp[3].getValue());
	}
    
    public void resetPlot(){
        
		plotpanel.removeAllGraphData();
		BasicGraphData modelgraphdata = new BasicGraphData();
		BasicGraphData zgraphdata = new BasicGraphData();
        
		ArrayList<Double> sdata = new ArrayList<Double>();
		ArrayList<Double> zdata = new ArrayList<Double>();
        
		Trajectory<EnvelopeProbeState> traj = probe.getTrajectory();
		Iterator<EnvelopeProbeState> iterState = traj.stateIterator();
        
		while(iterState.hasNext()){
			EnvelopeProbeState state = iterState.next();
			sdata.add(state.getPosition());
            
            CovarianceMatrix covarianceMatrix = state.getCovarianceMatrix();
            Twiss[] twiss = covarianceMatrix.computeTwiss();
			double rz =  1000.0*twiss[2].getEnvelopeRadius();
			if((state.getElementId()).equals(new String("ELEMENT_CENTER:" + namelist.get(0)))) resetResultsTable(twiss);
            
			if(plotunitbox.getSelectedIndex() == 1){
				double T = state.getKineticEnergy();
				double m = probe.getSpeciesRestEnergy();
				double gamma = T/m + 1;
				double beta = Math.abs(Math.sqrt(1-1/(gamma*gamma)));
				double v = beta*c;
				rz *=  (rffrequency*2*Math.PI) / v / 1000.0 * 180/Math.PI;
				System.out.println(state.getElementId() + " " + state.getPosition() + " " + rz);
			}
			zdata.add(rz);
		}
        
		int size = sdata.size() - 1;
		
		double[] s = new double[size];
		double[] z = new double[size];
		
		for(int i=0; i<size; i++){
			s[i]=(sdata.get(i)).doubleValue();
			z[i]=(zdata.get(i)).doubleValue();
		}
		
		modelgraphdata.addPoint(s, z);
		modelgraphdata.setDrawPointsOn(false);
		modelgraphdata.setDrawLinesOn(true);
		modelgraphdata.setGraphProperty("Legend", new String("Z RMS (meters)"));
		modelgraphdata.setGraphColor(Color.BLUE);
		plotpanel.addGraphData(modelgraphdata);
		
		int datasize = namelist.size();
		//int size = namelist.size();
		double[] srdata = new double[datasize];
		double[] zrdata = new double[datasize];
		
		//traj = probe.getTrajectory();
		EnvelopeProbeState newstate;
		Twiss[] newtwiss;
		double rz;
		
		for(int i =0; i<datasize; i++){
			newstate = traj.statesForElement(namelist.get(i)).get(0);
			srdata[i]=newstate.getPosition();
			zrdata[i]=1000.0*(zdatalist.get(i)).doubleValue();
			if(plotunitbox.getSelectedIndex() == 1){
				double T = newstate.getKineticEnergy();
				double m = probe.getSpeciesRestEnergy();
				double gamma = T/m + 1;
				double beta = Math.abs(Math.sqrt(1-1/(gamma*gamma)));
				double v = beta*c;
				zrdata[i] *=  (rffrequency*2*Math.PI) / v / 1000.0 * 180.0/Math.PI;
			}
			//System.out.println("For " + (String)namelist.get(i) + "pos is " + newstate.getPosition());
        }
		zgraphdata.addPoint(srdata, zrdata);
		zgraphdata.setDrawPointsOn(true);
		zgraphdata.setDrawLinesOn(false);
		zgraphdata.setGraphColor(Color.RED);
        
		plotpanel.addGraphData(zgraphdata);
		plotpanel.addGraphData(zgraphdata);
		limits.setSmartLimits();
		plotpanel.setExternalGL(limits);
    }
    
    
    
    //Evaluates beam properties for a trial point
    class Evaluator1 implements Evaluator{
        
        protected ArrayList<Objective> _objectives;
        protected ArrayList<Variable> _variables;
        public Evaluator1( final ArrayList<Objective> objectives, final ArrayList<Variable> variables ) {
            _objectives = objectives;
            _variables = variables;
        }
        
        public void evaluate(final Trial trial){
            double error =0.0;
            Iterator<Objective> itr = _objectives.iterator();
            while(itr.hasNext()){
                TargetObjective objective = (TargetObjective)itr.next();
                error = calcError(_variables, trial);
                trial.setScore(objective, error);
            }
            
        }
    }
    
    
    // objective class for solver.
    class TargetObjective extends Objective{
        
        protected final double _target;
        
        public TargetObjective(final String name, final double target){
            super(name);
            _target=target;
        }
        
        
        public double satisfaction(double value){
            double error = _target - value;
            return 1.0/(1+error*error);
        }
    }
	
}

