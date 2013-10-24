/*
 * BeamPositionFace.java
 *
 */
package xal.app.rtbtwizard;

import xal.tools.swing.*;
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

import xal.smf.application.*;
import xal.smf.data.*;
import xal.smf.*;
import xal.smf.impl.*;
import xal.smf.proxy.ElectromagnetPropertyAccessor;
import xal.model.*;
import xal.model.alg.*;
import xal.sim.scenario.*;
import xal.model.probe.*;
import xal.model.probe.traj.*;
import xal.model.xml.*;
//import xal.tools.optimizer.*;
import xal.tools.beam.Twiss;
import xal.tools.plot.*;
import java.text.NumberFormat;
import xal.tools.swing.DecimalField;
import xal.tools.apputils.EdgeLayout;
import xal.tools.data.*;
import xal.tools.xml.XmlDataAdaptor;
import xal.tools.beam.*;
import java.text.DecimalFormat;
import xal.tools.solver.*;
//import xal.tools.formula.*;
import xal.tools.solver.hint.*;
import xal.tools.solver.algorithm.*;
import xal.tools.solver.market.*;
import xal.tools.solver.solutionjudge.*;
import xal.sim.sync.PVLoggerDataSource;
import xal.tools.apputils.SimpleProbeEditor;
/**
 * Performs matching to find steerer strengths for desired injection
 * spot position and angle on the foil.
 * @author  cp3
 */

public class BeamSizeFace extends JPanel{
    
    /** ID for serializable version */
    private static final long serialVersionUID = 1L;
    
    private AcceleratorSeq seq;
    private Scenario scenario;
    private EnvelopeProbe probe;
    private JButton probeeditbutton;
    
    private Accelerator accl = new Accelerator();
    private DecimalField[] xdat = new DecimalField[5];
    private DecimalField[] ydat = new DecimalField[5];
    private NumberFormat numFor;
    private JPanel resultsPanel;
    private JPanel initPanel;
    private FunctionGraphsJPanel plotpanel;
    private JButton solvebutton;
    private JButton sendtoharpbutton;
    public JPanel mainPanel;
    public JTable resultstable;
    public JTable twisstable;
    
    JButton loadbutton;
    JScrollPane resultsscrollpane;
    ResultsTableModel resultstablemodel;
    JScrollPane twissscrollpane;
    ResultsTableModel twisstablemodel;
    EdgeLayout layout = new EdgeLayout();
    JLabel ws21label = new JLabel("WS20: ");
    JLabel ws22label = new JLabel("WS21: ");
    JLabel ws23label = new JLabel("WS23: ");
    JLabel ws24label = new JLabel("WS24: ");
    JLabel harplabel = new JLabel("Harp:");
    JLabel devicelabel = new JLabel("Device");
    JLabel xlabel = new JLabel("X (mm) ");
    JLabel ylabel = new JLabel("Y (mm) ");
    JLabel uselabel = new JLabel("Use");
    JLabel twisstablelabel = new JLabel("Twiss Matching Results at First Wirescanner");
    JLabel resultstablelabel = new JLabel("Beam Size Results");
    
    JCheckBox ws21box = new JCheckBox();
    JCheckBox ws22box = new JCheckBox();
    JCheckBox ws23box = new JCheckBox();
    JCheckBox ws24box = new JCheckBox();
    JCheckBox harpbox = new JCheckBox();
    
    Object[][] tabledata = new Object[8][3];
    Object[][] twissdata = new Object[3][3];
    GridLimits limits = new GridLimits();
    
    //ArrayList xsigmas = new ArrayList();
    //ArrayList ysigmas = new ArrayList();
    ArrayList<Object> xdatalist = new ArrayList<Object>();
    ArrayList<Object> ydatalist = new ArrayList<Object>();
    ArrayList<String> namelist = new ArrayList<String>();
    ArrayList<String> fullnamelist = new ArrayList<String>();
    ArrayList<Object> xfulldatalist = new ArrayList<Object>();
    ArrayList<Object> yfulldatalist = new ArrayList<Object>();
    
    GenDocument doc;
    JComboBox<String> syncstate;
    String latticestate = "Live";
    String[] syncstates = {"Model Live Lattice", "Model PV Logger Lattice"};
    JTextField pvloggerfield;
    JTextField solvertimefield;
    JLabel usepvlabel;
    JLabel solvertime;
    Integer pvloggerid = new Integer(0);
    double alphaz0, betaz0;
    double currentenergy = 1e9;
    double currentI = 1.0;
    double currentcharge = 0.0;
    double[] currenttwiss = new double[6];
    double windowratio = 0.0;
    double targetratio = 0.0;
    double harparea=0.0;
    int istart=0;
    DataTable fitsdatabase;
    HashMap<String, Double> beamarearatios = new HashMap<String, Double>();
    HashMap<String, Double> beamwindowratios = new HashMap<String, Double>();
    
    public BeamSizeFace(GenDocument aDocument, JPanel mainpanel) {
        doc=aDocument;
        setPreferredSize(new Dimension(950,600));
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
        
        newlayout.setConstraints(syncstate, 17, 20, 100, 10, EdgeLayout.LEFT, EdgeLayout.NO_GROWTH);
        mainPanel.add(syncstate);
        
        newlayout.setConstraints(usepvlabel, 20, 220, 100, 10, EdgeLayout.LEFT, EdgeLayout.NO_GROWTH);
        mainPanel.add(usepvlabel);
        newlayout.setConstraints(pvloggerfield, 20, 310, 100, 10, EdgeLayout.LEFT, EdgeLayout.NO_GROWTH);
        mainPanel.add(pvloggerfield);
        
        initPanel.setLayout(initgrid);
        initPanel.add(devicelabel); initPanel.add(xlabel); initPanel.add(ylabel); initPanel.add(uselabel);
        initPanel.add(ws21label); initPanel.add(xdat[0]); initPanel.add(ydat[0]); initPanel.add(ws21box);
        initPanel.add(ws22label); initPanel.add(xdat[1]); initPanel.add(ydat[1]); initPanel.add(ws22box);
        initPanel.add(ws23label); initPanel.add(xdat[2]); initPanel.add(ydat[2]); initPanel.add(ws23box);
        initPanel.add(ws24label); initPanel.add(xdat[3]); initPanel.add(ydat[3]); initPanel.add(ws24box);
        initPanel.add(harplabel); initPanel.add(xdat[4]); initPanel.add(ydat[4]); initPanel.add(harpbox);
        
        newlayout.setConstraints(initPanel, 60, 50, 100, 10, EdgeLayout.LEFT, EdgeLayout.NO_GROWTH);
        mainPanel.add(initPanel);
        
        //GridLayout resultsgrid = new GridLayout(1, 2);
        //resultsPanel.setLayout(resultsgrid);
        resultsPanel.add(twisstablelabel);
        resultsPanel.add(twissscrollpane);
        resultsPanel.add(resultstablelabel);
        resultsPanel.add(resultsscrollpane);
        newlayout.setConstraints(resultsPanel, 0, 470, 200, 10, EdgeLayout.LEFT, EdgeLayout.NO_GROWTH);
        mainPanel.add(resultsPanel);
        
        newlayout.setConstraints(solvertime, 195, 10, 100, 10, EdgeLayout.LEFT, EdgeLayout.NO_GROWTH);
        mainPanel.add(solvertime);
        newlayout.setConstraints(solvertimefield, 215, 10, 100, 10, EdgeLayout.LEFT, EdgeLayout.NO_GROWTH);
        mainPanel.add(solvertimefield);
        newlayout.setConstraints(probeeditbutton, 205, 150, 100, 10, EdgeLayout.LEFT, EdgeLayout.NO_GROWTH);
        mainPanel.add(probeeditbutton);
        newlayout.setConstraints(loadbutton, 105, 335, 150, 10, EdgeLayout.LEFT, EdgeLayout.NO_GROWTH);
        mainPanel.add(loadbutton);
        newlayout.setConstraints(solvebutton, 205, 335, 150, 10, EdgeLayout.LEFT, EdgeLayout.NO_GROWTH);
        mainPanel.add(solvebutton);
        newlayout.setConstraints(sendtoharpbutton, 305, 590, 150, 10, EdgeLayout.LEFT, EdgeLayout.NO_GROWTH);
        mainPanel.add(sendtoharpbutton);
        
        newlayout.setConstraints(plotpanel, 370, 50, 100, 10, EdgeLayout.LEFT, EdgeLayout.NO_GROWTH);
        mainPanel.add(plotpanel);
        
    }
    
    public void init(){
        
        mainPanel = new JPanel();
        mainPanel.setPreferredSize(new Dimension(950,600));
        
        resultsPanel = new JPanel();
        resultsPanel.setPreferredSize(new Dimension(400,300));
        resultsPanel.setBorder(BorderFactory.createTitledBorder("Results"));
        
        initPanel = new JPanel();
        initPanel.setPreferredSize(new Dimension(280,110));
        initPanel.setBorder(BorderFactory.createTitledBorder("Measured RMS Values"));
        
        plotpanel = new FunctionGraphsJPanel();
        plotpanel.setPreferredSize(new Dimension(800, 230));
        plotpanel.setGraphBackGroundColor(Color.WHITE);
        
        solvebutton = new JButton("Solve");
        sendtoharpbutton = new JButton("Send Results to Harp");
        loadbutton = new JButton("Load Fits");
        
        numFor = NumberFormat.getNumberInstance();
        numFor.setMinimumFractionDigits(4);
        
        accl = doc.getAccelerator();
        seq=accl.getSequence("RTBT2");
        
        ws21box.setSelected(true);
        ws22box.setSelected(true);
        ws23box.setSelected(true);
        ws24box.setSelected(true);
        harpbox.setSelected(true);
        
        for(int i = 0; i<5; i++){
            xdat[i] = new DecimalField(0.0, 4, numFor);
            ydat[i] = new DecimalField(0.0, 4, numFor);
        }
        usepvlabel = new JLabel("PV Logger ID: ");
        pvloggerfield = new JTextField(8);
        solvertime = new JLabel("Solver time (s): ");
        solvertimefield = new JTextField(8);
        solvertimefield.setText("60");
        syncstate = new JComboBox<String>(syncstates);
        probeeditbutton = new JButton("Edit Model Probe");
        
        //Set up a few model items
        
        EnvelopeTracker etracker = null;
        
        try {
            
            etracker = AlgorithmFactory.createEnvelopeTracker (seq);
            
        } catch ( InstantiationException exception ) {
            System.err.println( "Instantiation exception creating tracker." );
            exception.printStackTrace();
        }
        
        try {
            probe=ProbeFactory.getEnvelopeProbe(seq, etracker);
            currentenergy = probe.getKineticEnergy();
            currentI = probe.getBeamCurrent();
            //currentcharge = probe.getBeamCharge();
            scenario = Scenario.newScenarioFor(seq);
            scenario.setProbe(probe);
            scenario.setSynchronizationMode(Scenario.SYNC_MODE_RF_DESIGN);
            //scenario.resync();
            //scenario.run();
        }
        catch(Exception exception){
            exception.printStackTrace();
        }
        
        makeResultsTable();
        tabledata[0][0] = new String("RTBT_Diag:WS20");
        tabledata[1][0] = new String("RTBT_Diag:WS21");
        tabledata[2][0] = new String("RTBT_Diag:WS23");
        tabledata[3][0] = new String("RTBT_Diag:WS24");
        tabledata[4][0] = new String("RTBT_Diag:Harp30");
        tabledata[5][0] = new String("Window");
        tabledata[6][0] = new String("Target");
        tabledata[7][0] = new String("Target (with scattering)");
        resultstablemodel.setTableData(tabledata);
        
        makeTwissTable();
        twisstablemodel.setValueAt(new String("Alpha"),0,0);
        twisstablemodel.setValueAt(new String("Beta"),1,0);
        twisstablemodel.setValueAt(new String("Emit"),2,0);
        
        fullnamelist.add("RTBT_Diag:WS20");
        fullnamelist.add("RTBT_Diag:WS21");
        fullnamelist.add("RTBT_Diag:WS23");
        fullnamelist.add("RTBT_Diag:WS24");
        fullnamelist.add("RTBT_Diag:Harp30");
        
        /*
         pvloggerfield.setText("2059640");
         xdat[0].setValue(25.02); xdat[1].setValue(18); xdat[2].setValue(25.0);
         xdat[3].setValue(10.1); xdat[4].setValue(38.0);
         ydat[0].setValue(18.3); ydat[1].setValue(18.3); ydat[2].setValue(10.2);
         ydat[3].setValue(20.8); ydat[4].setValue(22);
         */
        
    }
    
    
    
    public void setAction(){
        
        solvebutton.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent ae){
                solve();
            }
        });
        sendtoharpbutton.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent ae){
                sendtoharp();
            }
        });
        
        loadbutton.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent ae){
                loadFits();
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
                SimpleProbeEditor spe = new SimpleProbeEditor(doc.myWindow(), scenario.getProbe());
                //scenario.resetProbe();
                //spe.createSimpleProbeEditor(scenario.getProbe());
                
                // update the model probe with the one from probe editor
                //if (spe.probeHasChanged()){
                scenario.setProbe(spe.getProbe());
                currentenergy=probe.getKineticEnergy();
                currentI=probe.getBeamCurrent();
                //currentcharge=probe.getBeamCharge();
                //}
                
            }
        });
    }
    
	
    public void solve() {
		
		xfulldatalist.add(xdat[0].getValue());
		yfulldatalist.add(ydat[0].getValue());
		xfulldatalist.add(xdat[1].getValue());
		yfulldatalist.add(ydat[1].getValue());
		xfulldatalist.add(xdat[2].getValue());
		yfulldatalist.add(ydat[2].getValue());
		xfulldatalist.add(xdat[3].getValue());
		yfulldatalist.add(ydat[3].getValue());
		xfulldatalist.add(xdat[4].getValue());
		yfulldatalist.add(ydat[4].getValue());
		
		xdatalist.clear();
		ydatalist.clear();
		namelist.clear();
        
		//for(int i=0; i<4; i++){
		if(ws21box.isSelected() == true){
			namelist.add("RTBT_Diag:WS20");
			xdatalist.add(xdat[0].getValue());
			ydatalist.add(ydat[0].getValue());
		}
		if(ws22box.isSelected() == true){
			namelist.add("RTBT_Diag:WS21");
			xdatalist.add(xdat[1].getValue());
			ydatalist.add(ydat[1].getValue());
		}
		if(ws23box.isSelected() == true){
			namelist.add("RTBT_Diag:WS23");
			xdatalist.add(xdat[2].getValue());
			ydatalist.add(ydat[2].getValue());
		}
		if(ws24box.isSelected() == true){
			namelist.add("RTBT_Diag:WS24");
			xdatalist.add(xdat[3].getValue());
			ydatalist.add(ydat[3].getValue());
		}
		if(harpbox.isSelected() == true){
			namelist.add("RTBT_Diag:Harp30");
			xdatalist.add(xdat[4].getValue());
			ydatalist.add(ydat[4].getValue());
		}
		if(namelist.get(0) == "RTBT_Diag:WS20") istart=0;
		if(namelist.get(0) == "RTBT_Diag:WS21") istart=1;
		if(namelist.get(0) == "RTBT_Diag:WS23") istart=2;
		if(namelist.get(0) == "RTBT_Diag:WS24") istart=3;
		if(namelist.get(0) == "RTBT_Diag:Harp30") istart=4;
        
		System.out.println("Wires in use: " + namelist);
		
		try {
			if(latticestate.equals("Live")){
				scenario = Scenario.newScenarioFor(seq);
				scenario.setProbe(probe);
				scenario.setSynchronizationMode(Scenario.SYNC_MODE_LIVE);
				System.out.println("Loading live machine state.");
			}
			if(latticestate.equals("PVLogger")){
				String id = pvloggerfield.getText();
				pvloggerid = new Integer(Integer.parseInt(id));
				scenario = Scenario.newScenarioFor(seq);
				scenario.setProbe(probe);
				scenario.setSynchronizationMode(Scenario.SYNC_MODE_DESIGN);
				System.out.println("Loading PVlogger state.");
                if(pvloggerid !=0){
                    PVLoggerDataSource plds = new PVLoggerDataSource(pvloggerid.intValue());
                    scenario = plds.setModelSource(seq, scenario);
                }
                else{
                    System.out.println("No PV Logger ID Found!");
				}
			}
			scenario.resync();
			scenario.run();
		}
		catch(Exception exception){
			exception.printStackTrace();
		}
        
        
        String time = solvertimefield.getText();
        double solvetime= new Double(Double.parseDouble(time));
        
        EnvelopeTrajectory traj= (EnvelopeTrajectory)probe.getTrajectory();
        EnvelopeProbeState state = (EnvelopeProbeState)traj.statesForElement(namelist.get(0))[0];
        //EnvelopeProbeState state =(EnvelopeProbeState)traj.statesForElement("RTBT_Diag:WS20")[0];
       
        
        CovarianceMatrix covarianceMatrix = state.getCovarianceMatrix();
        Twiss[] twiss = covarianceMatrix.computeTwiss();
        
        // set up some variables to use in the optimization:
        // initial guesses
        double alphax0=twiss[0].getAlpha();
        //alphax0 = -2.5;
        double betax0=twiss[0].getBeta();
        //betax0 = 21.;
        double alphay0=twiss[1].getAlpha();
        //alphay0 = 1.;
        double betay0=twiss[1].getBeta();
        //betay0 = 9.;
        alphaz0 = twiss[2].getAlpha();
        betaz0 = twiss[2].getBeta();
        
        //double emitx0 = 20.;
        //double emity0 = 13.;
        double emitx0 = twiss[0].getEmittance();
        double emity0 = twiss[1].getEmittance();
        
        System.out.println("Starting with ax, bx, ex = " + alphax0 +  " " + betax0 + " " + emitx0);
        System.out.println("Starting with ay, by, ey = " + alphay0 +  " " + betay0 + " " + emity0);
        
        try{
            scenario.setStartNode(namelist.get(0));
            //scenario.setStartNode("RTBT_Diag:WS20");
        }
        catch(Exception exception){
            exception.printStackTrace();
        }
        Twiss xTwiss = new Twiss(alphax0, betax0, emitx0);
        Twiss yTwiss = new Twiss(alphay0, betay0, emity0);
        Twiss zTwiss = new Twiss(alphaz0, betaz0, 11.4e-3);
        Twiss[] tw = new Twiss[3];
        tw[0]=xTwiss;
        tw[1]=yTwiss;
        tw[2]=zTwiss;
        
        resetProbe();
        probe.initFromTwiss(tw);
        //probe.setPosition(seq.getPosition(seq.getNodeWithId("RTBT_Diag:WS20")));
        //probe.setPosition(seq.getPosition(seq.getNodeWithId((String)namelist.get(0))));
        
        ArrayList<Variable> variables =  new ArrayList<Variable>();
        variables.add(new Variable("alphaX",alphax0, -4., 4.0));
        variables.add(new Variable("betaX",betax0, 2, 40));
        variables.add(new Variable("alphaY", alphay0, -4., 4.0));
        variables.add(new Variable("betaY",betay0, 2, 40));
        variables.add(new Variable("emitX",emitx0, 1, 40));
        variables.add(new Variable("emitY",emity0, 1, 40));
        
        ArrayList<Objective> objectives = new ArrayList<Objective>();
        objectives.add(new TargetObjective( "diff", 0.0 ) );
        
        Evaluator1 evaluator = new Evaluator1( objectives, variables );
        
        Problem problem = new Problem( objectives, variables, evaluator );
        problem.addHint(new InitialDelta( 0.1 ) );
        
        Stopper maxSolutionStopper = SolveStopperFactory.minMaxTimeSatisfactionStopper( 1, solvetime, 0.999 );
        Solver solver = new Solver(new RandomShrinkSearch(), maxSolutionStopper );
        
        solver.solve( problem );
        System.out.println("score is " + solver.getScoreBoard());
        Trial best = solver.getScoreBoard().getBestSolution();
        
        // rerun with solution to populate results table
        calcError(variables, best);
        updateProbe(variables, best);
        try{
            scenario.run();
        }
        catch(Exception exception){
            exception.printStackTrace();
        }
        //Print the final results at the desired locations
        
        resetTwissTable(variables, best);
        resetResultsTable();
        resetPlot(variables,best);
        resetInitProbe(namelist.get(0));
        
    }
    
    
    public double calcError(ArrayList<Variable> vars, Trial trial){
        updateProbe(vars, trial);
        try{
            scenario.run();
        }
        catch(Exception exception){
            exception.printStackTrace();
        }
        
        EnvelopeTrajectory traj= (EnvelopeTrajectory)probe.getTrajectory();
        double error = 0.0;
        int size = namelist.size();
        double rx=0;
        double ry=0;
        //System.out.println("namelist is " + namelist);
        //System.out.println("size is " + size);
        //System.out.println("first is " + namelist.get(0));
        
        for(int i =0; i<size; i++){
            String name = namelist.get(i);
            //EnvelopeProbeState state = (EnvelopeProbeState)traj.statesForElement("RTBT_Diag:WS21")[0];
            EnvelopeProbeState state = (EnvelopeProbeState)traj.statesForElement(namelist.get(i))[0];
            
            CovarianceMatrix covarianceMatrix = state.getCovarianceMatrix();
            Twiss[] twiss = covarianceMatrix.computeTwiss();
            rx =  twiss[0].getEnvelopeRadius();
            ry =  twiss[1].getEnvelopeRadius();
            error += Math.pow( (rx*1000. - ((Double)xdatalist.get(i)).doubleValue()), 2.);
            error += Math.pow( (ry*1000. - ((Double)ydatalist.get(i)).doubleValue()), 2.);
            //System.out.println(namelist.get(i) + " " + rx*1000 + " " + xdatalist.get(i));
        }
        error = Math.sqrt(error);
        return error;
    }
    
    void updateProbe(ArrayList<Variable> vars, Trial trial){
        double alphax=0.0;
        double alphay=0.0;
        double betax=0.0;
        double betay=0.0;
        double emitx=0.0;
        double emity=0.0;
        
        probe = (EnvelopeProbe)scenario.getProbe();
        resetProbe();
        //probe.setPosition(seq.getPosition(seq.getNodeWithId((String)namelist.get(0))));
        Iterator<Variable> itr = vars.iterator();
        while(itr.hasNext()){
            Variable variable = itr.next();
            double value = trial.getTrialPoint().getValue(variable);
            String name = variable.getName();
            if(name.equalsIgnoreCase("alphaX")) alphax = value;
            if(name.equalsIgnoreCase("alphaY")) alphay = value;
            if(name.equalsIgnoreCase("betaX")) betax = value;
            if(name.equalsIgnoreCase("betaY")) betay = value;
            if(name.equalsIgnoreCase("emitY")) emity = value * 1.e-6;
            if(name.equalsIgnoreCase("emitX")) emitx = value * 1.e-6;
        }
        Twiss xTwiss = new Twiss(alphax, betax, emitx);
        Twiss yTwiss = new Twiss(alphay, betay, emity);
        Twiss zTwiss = new Twiss(alphaz0,betaz0, 11.4e-3);
        
        Twiss[] tw= new Twiss[3];
        tw[0]=xTwiss;
        tw[1]=yTwiss;
        tw[2]=zTwiss;
        
        probe.initFromTwiss(tw);
    }
    
    void resetProbe(){
        probe.reset();
        //probe.setPosition(seq.getPosition(seq.getNodeWithId("RTBT_Diag:WS20")));
        probe.setKineticEnergy(currentenergy);
        probe.setBeamCurrent(currentI);
        //probe.setBeamCharge(currentcharge);
    }
    
    void resetInitProbe(String currentElem){
        
        scenario.setStartElementId("Begin_Of_RTBT2");
        probe.reset();
        probe.setKineticEnergy(currentenergy);
        probe.setBeamCurrent(currentI);
        //probe.setBeamCharge(currentcharge);
        
        double[] inittwiss = findTwissAtSeqStart(currentElem);
        Twiss xTwiss = new Twiss(inittwiss[0], inittwiss[1], inittwiss[2]);
        Twiss yTwiss = new Twiss(inittwiss[3], inittwiss[4], inittwiss[5]);
        Twiss zTwiss = new Twiss(alphaz0,betaz0, 11.4e-3);
        
        Twiss[] tw= new Twiss[3];
        tw[0]=xTwiss;
        tw[1]=yTwiss;
        tw[2]=zTwiss;
        
        probe.reset();
        System.out.println("New init Twiss are = " + xTwiss + " " + yTwiss + " " + zTwiss);
        probe.initFromTwiss(tw);
    }
    
    double[] findTwissAtSeqStart(String currentElem){
        
        double[] twiss = new double[6];
        String refStart = new String("Begin_Of_RTBT2");
        
        try{
            scenario.run();
        }
        catch(Exception exception){
            exception.printStackTrace();
        }
        
        PhaseMatrix matRef = (probe).stateResponse(currentElem, refStart);
        
        //PhaseMatrix matRef = ((EnvelopeProbe)probe).stateResponse(currentElem, "RTBT_Mag:QH20");
        double Rs11 = matRef.getElem(0, 0);
        double Rs12 = matRef.getElem(0, 1);
        double Rs16 = matRef.getElem(0, 5);
        double Rs21 = matRef.getElem(1, 0);
        double Rs22 = matRef.getElem(1, 1);
        double Rs26 = matRef.getElem(1, 5);
        double Rs33 = matRef.getElem(2, 2);
        double Rs34 = matRef.getElem(2, 3);
        double Rs36 = matRef.getElem(2, 5);
        double Rs43 = matRef.getElem(3, 2);
        double Rs44 = matRef.getElem(3, 3);
        double Rs46 = matRef.getElem(3, 5);
        
        double alphax=currenttwiss[0]; double betax=currenttwiss[1];
        double alphay=currenttwiss[3]; double betay=currenttwiss[4];
        double gammax=(1+(alphax*alphax))/betax;
        double gammay=(1+(alphay*alphay))/betay;
        
        twiss[0] = -1.*Rs11*Rs21*betax + (Rs11*Rs22 + Rs12*Rs21)*alphax-Rs12*Rs22*gammax;
        twiss[1] = Rs11*Rs11*betax - 2.*Rs11*Rs12*alphax + Rs12*Rs12*gammax;
        twiss[2] = currenttwiss[2];
        twiss[3] = -1.*Rs33*Rs43*betay + (Rs33*Rs44 + Rs34*Rs43)*alphay - Rs34*Rs44*gammay;
        twiss[4] = Rs33*Rs33*betay - 2.*Rs33*Rs34*alphay + Rs34*Rs34*gammay;
        twiss[5] = currenttwiss[5];
        
        return twiss;
    }
    
    public void makeResultsTable(){
        String[] colnames = {"Location", "X (mm)", "Y (mm)"};
        
        resultstablemodel = new ResultsTableModel(colnames,8);
        
        resultstable = new JTable(resultstablemodel);
        resultstable.getColumnModel().getColumn(0).setMinWidth(141);
        resultstable.getColumnModel().getColumn(1).setMinWidth(112);
        resultstable.getColumnModel().getColumn(2).setMinWidth(112);
        
        resultstable.setPreferredScrollableViewportSize(resultstable.getPreferredSize());
        resultstable.setRowSelectionAllowed(false);
        resultstable.setColumnSelectionAllowed(false);
        resultstable.setCellSelectionEnabled(true);
        
        resultsscrollpane = new JScrollPane(resultstable,JScrollPane.VERTICAL_SCROLLBAR_NEVER, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        resultsscrollpane.setColumnHeaderView(resultstable.getTableHeader());
        resultstable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        resultsscrollpane.setPreferredSize(new Dimension(365, 145));
    }
    
    public void makeTwissTable(){
        String[] colnames = {"Parameter", "Horizontal", "Vertical"};
        
        twisstablemodel = new ResultsTableModel(colnames,4);
        
        twisstable = new JTable(twisstablemodel);
        twisstable.getColumnModel().getColumn(0).setMinWidth(130);
        twisstable.getColumnModel().getColumn(1).setMinWidth(115);
        twisstable.getColumnModel().getColumn(2).setMinWidth(115);
        
        twisstable.setPreferredScrollableViewportSize(resultstable.getPreferredSize());
        twisstable.setRowSelectionAllowed(false);
        twisstable.setColumnSelectionAllowed(false);
        twisstable.setCellSelectionEnabled(true);
        twissscrollpane = new JScrollPane(twisstable,JScrollPane.VERTICAL_SCROLLBAR_NEVER, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        twissscrollpane.setColumnHeaderView(twisstable.getTableHeader());
        twisstable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        twissscrollpane.setPreferredSize(new Dimension(360, 64));
    }
    
    public void resetTwissTable(ArrayList<Variable> vars, Trial trial){
        Iterator<Variable> itr = vars.iterator();
        while(itr.hasNext()){
            Variable variable = itr.next();
            double value = trial.getTrialPoint().getValue(variable);
            System.out.println("value is " + value);
            String name = variable.getName();
            DecimalFormat decfor =  new DecimalFormat("###.000");
            if(name.equalsIgnoreCase("alphaX")){
                twisstablemodel.setValueAt(new Double(decfor.format(value)),0,1);
                currenttwiss[0]=value;
            }
            if(name.equalsIgnoreCase("alphaY")){
                twisstablemodel.setValueAt(new Double(decfor.format(value)),0,2);
                currenttwiss[3]=value;
            }
            if(name.equalsIgnoreCase("betaX")){
                twisstablemodel.setValueAt(new Double(decfor.format(value)),1,1);
                currenttwiss[1]=value;
            }
            if(name.equalsIgnoreCase("betaY")){
                twisstablemodel.setValueAt(new Double(decfor.format(value)),1,2);
                currenttwiss[4]=value;
            }
            if(name.equalsIgnoreCase("emitY")){
                twisstablemodel.setValueAt(new Double(decfor.format(value)),2,2);
                currenttwiss[5]=value;
            }
            if(name.equalsIgnoreCase("emitX")){
                twisstablemodel.setValueAt(new Double(decfor.format(value)),2,1);
                currenttwiss[2]=value;
            }
        }
        twisstablemodel.fireTableDataChanged();
    }
	
	
	
    public void resetResultsTable(){
		DecimalFormat decfor =  new DecimalFormat("###.000");
		int size = fullnamelist.size();
        
		EnvelopeTrajectory traj= (EnvelopeTrajectory)probe.getTrajectory();
		EnvelopeProbeState newstate;
		Twiss[] newtwiss;
		double rx, ry;
        
		for(int i = 0; i<istart; i++){
			tabledata[i][1] = new Double(0.0);
			tabledata[i][2] = new Double(0.0);
		}
		for(int i = istart; i<size; i++){
			newstate = (EnvelopeProbeState)traj.statesForElement(fullnamelist.get(i))[0];
            
            CovarianceMatrix covarianceMatrix = newstate.getCovarianceMatrix();
            newtwiss = covarianceMatrix.computeTwiss();
            
			rx = 1000*newtwiss[0].getEnvelopeRadius();
			ry = 1000*newtwiss[1].getEnvelopeRadius();
			tabledata[i][1] = new Double(decfor.format(rx));
			tabledata[i][2] = new Double(decfor.format(ry));
			System.out.println("Horizontal Alpha and Beta for " + fullnamelist.get(i) + " is " + newtwiss[0].getAlpha() + " and " + newtwiss[0].getBeta());
			System.out.println("Vertical Alpha and Beta for " + fullnamelist.get(i) + " is " + newtwiss[1].getAlpha() + " and " + newtwiss[1].getBeta());
		}
        
		harparea=((Double)tabledata[4][1]).doubleValue()*((Double)tabledata[4][2]).doubleValue();;
		newstate = (EnvelopeProbeState)traj.statesForElement("RTBT_Vac:VIW")[0];
        
        CovarianceMatrix covarianceMatrix = newstate.getCovarianceMatrix();
        newtwiss = covarianceMatrix.computeTwiss();
		rx = 1000*newtwiss[0].getEnvelopeRadius();
		ry = 1000*newtwiss[1].getEnvelopeRadius();
		tabledata[5][1] = new Double(decfor.format(rx));
		tabledata[5][2] = new Double(decfor.format(ry));
		newstate = (EnvelopeProbeState)traj.statesForElement("RTBT:Tgt")[0];
        
        covarianceMatrix = newstate.getCovarianceMatrix();
        newtwiss = covarianceMatrix.computeTwiss();
        
		rx = 1000*newtwiss[0].getEnvelopeRadius();
		ry = 1000*newtwiss[1].getEnvelopeRadius();
		tabledata[6][1] = new Double(decfor.format(rx));
		tabledata[6][2] = new Double(decfor.format(ry));
		double xtargetwin = Math.sqrt(rx*rx + 11.0*11.0);
		double ytargetwin = Math.sqrt(ry*ry + 11.0*11.0);
		tabledata[7][1] = new Double(decfor.format(xtargetwin));
		tabledata[7][2] = new Double(decfor.format(ytargetwin));
        
		resultstablemodel.setTableData(tabledata);
        
        //Build size ratio HashMap
        
		int rows = resultstablemodel.getRowCount();
		double xwindowvalue = ((Double)tabledata[5][1]).doubleValue();
		double ywindowvalue = ((Double)tabledata[5][2]).doubleValue();
		double windowarea = xwindowvalue*ywindowvalue;
		double xtargetvalue = ((Double)tabledata[7][1]).doubleValue();
		double ytargetvalue = ((Double)tabledata[7][2]).doubleValue();
		double targetarea = xtargetvalue*ytargetvalue;
        
		System.out.println("target area is " + targetarea);
		for(int i=0; i< rows-2; i++){
			String label = (String)resultstablemodel.getValueAt(i,0);
			double xvalue = ((Double)tabledata[i][1]).doubleValue();
			double yvalue = ((Double)tabledata[i][2]).doubleValue();
			double area = xvalue*yvalue;
			double tratio = area/targetarea;
			double wratio = area/windowarea;
			//System.out.println("for wire " + label);
			//System.out.println("xvalue, yvalue, area " + xvalue + " " + yvalue + " " + area);
			//System.out.println("window area = " + windowarea);
			//System.out.println("target area = " + targetarea);
			//System.out.println(" and window ratio is " + wratio);
			//System.out.println(" and target ratio is " + tratio);
			beamarearatios.put(new String(label), tratio);
			beamwindowratios.put(new String(label), wratio);
		}
		targetratio = harparea/targetarea;
		windowratio = harparea/windowarea;
        
		System.out.println("targetratio= " + targetratio + " windowratio= " + windowratio);
		doc.beamarearatios=beamarearatios;
		doc.windowarearatios=beamwindowratios;
		doc.xsize = ((Double)tabledata[rows-1][1]).doubleValue();
		doc.ysize = ((Double)tabledata[rows-1][2]).doubleValue();
    }
    
    @SuppressWarnings ("unchecked") //had to suppress becayse valueForKey returns object and does not allow for specific casting
    private void loadFits(){
		fitsdatabase = doc.wireresultsdatabase;
		//ArrayList tabledata = new ArrayList();
		if(fitsdatabase.records().size() == 0){
			System.out.println("No data available to load!");
		}
		else{
			Collection<GenericRecord> records = fitsdatabase.records();
			Iterator<GenericRecord> itr = records.iterator();
            
			while(itr.hasNext()){
				//tabledata.clear();
				GenericRecord record = itr.next();
                
				String wire = (String)record.valueForKey("wire");
				String direction = (String)record.valueForKey("direction");
				if(wire.equals("RTBT_Diag:WS20")){
					if(direction.equals("H")){
						ArrayList<double[]> results = (ArrayList<double[]>)record.valueForKey("data");
						double[] fitparams = results.get(0);
						xdat[0].setValue(fitparams[4]);
					}
					if(direction.equals("V")){
						ArrayList<double[]> results = (ArrayList<double[]>)record.valueForKey("data");
						double[] fitparams = results.get(0);
						ydat[0].setValue(fitparams[4]);
					}
				}
				if(wire.equals("RTBT_Diag:WS21")){
					if(direction.equals("H")){
						ArrayList<double[]> results = (ArrayList<double[]>)record.valueForKey("data");
						double[] fitparams = results.get(0);
						xdat[1].setValue(fitparams[4]);
					}
					if(direction.equals("V")){
						ArrayList<double[]> results = (ArrayList<double[]>)record.valueForKey("data");
						double[] fitparams = results.get(0);
						ydat[1].setValue(fitparams[4]);
					}
				}
				if(wire.equals("RTBT_Diag:WS23")){
					if(direction.equals("H")){
						ArrayList<double[]> results = (ArrayList<double[]>)record.valueForKey("data");
						double[] fitparams = results.get(0);
						xdat[2].setValue(fitparams[4]);
					}
					if(direction.equals("V")){
						ArrayList<double[]> results = (ArrayList<double[]>)record.valueForKey("data");
						double[] fitparams = results.get(0);
						ydat[2].setValue(fitparams[4]);
					}
				}
				if(wire.equals("RTBT_Diag:WS24")){
					if(direction.equals("H")){
						ArrayList<double[]> results = (ArrayList<double[]>)record.valueForKey("data");
						double[] fitparams = results.get(0);
						xdat[3].setValue(fitparams[4]);
					}
					if(direction.equals("V")){
						ArrayList<double[]> results = (ArrayList<double[]>)record.valueForKey("data");
						double[] fitparams = results.get(0);
						ydat[3].setValue(fitparams[4]);
					}
				}
				if(wire.equals("RTBT_Diag:Harp30")){
					if(direction.equals("H")){
						ArrayList<double[]> results = (ArrayList<double[]>)record.valueForKey("data");
						double[] fitparams = results.get(0);
						xdat[4].setValue(fitparams[4]);
					}
					if(direction.equals("V")){
						ArrayList<double[]> results = (ArrayList<double[]>)record.valueForKey("data");
						double[] fitparams = results.get(0);
						ydat[4].setValue(fitparams[4]);
					}
				}
			}
		}
    }
    
    public void sendtoharp(){
		System.out.println("Sending targetratio as " + targetratio);
	    System.out.println("Sending windowratio as " + windowratio);
		Channel windowch;
		Channel targetch;
		windowch = ChannelFactory.defaultFactory().getChannel("RTBT_Diag:Harp30:WindowSizeRatio_Set");
		targetch = ChannelFactory.defaultFactory().getChannel("RTBT_Diag:Harp30:TargetSizeRatio_Set");
		windowch.connectAndWait();
		targetch.connectAndWait();
		try {
			windowch.putVal(windowratio);
			targetch.putVal(targetratio);
			System.out.println("Sending targetratio as " + targetratio);
			System.out.println("Sending windowratio as " + windowratio);
			Channel.flushIO();
		}
		catch (ConnectionException e){
			System.err.println("Unable to connect to channel access.");
		}
		catch (PutException e){
			System.err.println("Unable to set process variables.");
		}
    }
    
    
    public void resetPlot(ArrayList<Variable> vars, Trial trial){
        
		plotpanel.removeAllGraphData();
		BasicGraphData hgraphdata = new BasicGraphData();
		BasicGraphData vgraphdata = new BasicGraphData();
		BasicGraphData xgraphdata = new BasicGraphData();
		BasicGraphData ygraphdata = new BasicGraphData();
        
		ArrayList<Double> sdata = new ArrayList<Double>();
		ArrayList<Double>  hdata = new ArrayList<Double>();
		ArrayList<Double>  vdata = new ArrayList<Double>();
        
		//probe.reset();
		resetProbe();
		//scenario.setStartElementId("RTBT_Diag:WS20");
		scenario.setStartElementId(namelist.get(0));
        
		try{
			scenario.resync();
			scenario.run();
		}
		catch(Exception exception){
			exception.printStackTrace();
		}
        
		//probe.reset();
		EnvelopeTrajectory traj= (EnvelopeTrajectory)probe.getTrajectory();
		//System.out.println("trajectory is = " + traj);
		Iterator<ProbeState> iterState= traj.stateIterator();
        
		while(iterState.hasNext()){
			EnvelopeProbeState state= (EnvelopeProbeState)iterState.next();
			sdata.add(state.getPosition());
            
            CovarianceMatrix covarianceMatrix = state.getCovarianceMatrix();
            Twiss[] twiss = covarianceMatrix.computeTwiss();
            
			double rx =  1000.0*twiss[0].getEnvelopeRadius();
			double ry =  1000.0*twiss[1].getEnvelopeRadius();
			hdata.add(rx);
			vdata.add(ry);
		}
        
		int size = sdata.size() - 1;
        
		double[] s = new double[size];
		double[] x = new double[size];
		double[] y = new double[size];
        
		for(int i=0; i<size; i++){
			s[i]=(sdata.get(i)).doubleValue();
			x[i]=(hdata.get(i)).doubleValue();
			y[i]=(vdata.get(i)).doubleValue();
		}
        
		hgraphdata.addPoint(s, x);
		hgraphdata.setDrawPointsOn(false);
		hgraphdata.setDrawLinesOn(true);
		hgraphdata.setGraphProperty("Legend", new String("Horizontal"));
		hgraphdata.setGraphColor(Color.RED);
		vgraphdata.addPoint(s, y);
		vgraphdata.setDrawPointsOn(false);
		vgraphdata.setDrawLinesOn(true);
		vgraphdata.setGraphProperty("Legend", new String("Vertical"));
		vgraphdata.setGraphColor(Color.BLUE);
		//limits.setSmartLimits();
		//limits.setXmax(14.0);
		plotpanel.setExternalGL(limits);
        
		plotpanel.addGraphData(hgraphdata);
		plotpanel.addGraphData(vgraphdata);
        
		int datasize = namelist.size();
		//int size = namelist.size();
		double[] srdata = new double[datasize];
		double[] xrdata = new double[datasize];
		double[] yrdata = new double[datasize];
        
		traj= (EnvelopeTrajectory)probe.getTrajectory();
		EnvelopeProbeState newstate;
		Twiss[] newtwiss;
		double rx, ry;
        
		for(int i =0; i<datasize; i++){
			newstate = (EnvelopeProbeState)traj.statesForElement(namelist.get(i))[0];
			srdata[i]=newstate.getPosition();
			xrdata[i]=((Double)xdatalist.get(i)).doubleValue();
			yrdata[i]=((Double)ydatalist.get(i)).doubleValue();
			System.out.println("For " + namelist.get(i) + "pos is " + newstate.getPosition());
		}
		xgraphdata.addPoint(srdata, xrdata);
		xgraphdata.setDrawPointsOn(true);
		xgraphdata.setDrawLinesOn(false);
		xgraphdata.setGraphColor(Color.RED);
		ygraphdata.addPoint(srdata, yrdata);
		ygraphdata.setDrawPointsOn(true);
		ygraphdata.setDrawLinesOn(false);
		ygraphdata.setGraphColor(Color.BLUE);
        
		plotpanel.addGraphData(xgraphdata);
		plotpanel.addGraphData(ygraphdata);
		limits.setSmartLimits();
		limits.setXmin(srdata[0] - 2.0);
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

