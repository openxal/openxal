package xal.app.ringmeasurement;

import java.util.*;
import java.text.NumberFormat;
import java.awt.event.*;

import xal.smf.*;
import xal.smf.impl.MagnetMainSupply;
import xal.smf.impl.Quadrupole;
import xal.smf.impl.BPM;
import xal.smf.proxy.ElectromagnetPropertyAccessor;
import xal.model.*;
import xal.model.probe.TransferMapProbe;
import xal.sim.scenario.ProbeFactory;
import xal.model.alg.TransferMapTracker;
import xal.sim.scenario.Scenario;
import xal.sim.sync.SynchronizationException;
import xal.model.probe.traj.TransferMapTrajectory;
import xal.model.probe.traj.TransferMapState;
import xal.tools.beam.calc.CalculationsOnRing;
import xal.tools.math.r3.R3;
//import xal.tools.optimizer.*;
import xal.tools.solver.*;
import xal.tools.solver.algorithm.*;
import xal.tools.solver.hint.*;
import xal.sim.scenario.AlgorithmFactory;


public class CalcQuadSettings implements Runnable {
    
	ArrayList<Variable> variable_list = new ArrayList<Variable>();
    
	Ring theRing;
    
	Scenario scenario;
    
	SimpleEvaluator se;
    
	List<AcceleratorNode> quads;
    
	ArrayList<MagnetMainSupply> qPSs = new ArrayList<MagnetMainSupply>();
    
	ArrayList<String> bpms;
    
	HashMap<String, String> quad2PSMap = new HashMap<String, String>();
    
	Variable[] varQuadPSs;
    
    List<InitialDelta> varQuadPSsHints;
    
	TunePanel tp;
    
	double[] initVals;
    
	TransferMapProbe myProbe;
    
	TransferMapTrajectory traj;
    
	double stepSize = 0.1;
    
	float elapseTime = 120.f;
    
	double maxBestScore = 0.01;
    
	private int fractionComplete = 0;
    
	
    private Problem problem;
    
    public CalcQuadSettings(Ring ring, ArrayList<String> bpms, TunePanel tp) {
		theRing = ring;
		this.bpms = bpms;
		this.tp = tp;
		this.initVals = tp.qSetVals;
        
		quads = theRing.getAllNodesOfType("Q");
		Iterator<AcceleratorNode> it = quads.iterator();
		while (it.hasNext()) {
			Quadrupole quad = ((Quadrupole) it.next());
			MagnetMainSupply mps = quad.getMainSupply();
            
			quad2PSMap.put(quad.getId(), mps.getId());
            
			if (!qPSs.contains(mps)) {
				qPSs.add(mps);
			}
		}
        
		varQuadPSs = new Variable[qPSs.size()];
        varQuadPSsHints = new ArrayList<InitialDelta>(qPSs.size());
        problem = new Problem();
        
        
        
        
        
		for (int i = 0; i < qPSs.size(); i++) {
			// set parameter list
			varQuadPSs[i] = new Variable(qPSs.get(i).getId(),
                                         initVals[i], 0, 50.);
            
            //add a hint for step size. add hint to problem.
			variable_list.add(varQuadPSs[i]);
            
            varQuadPSsHints.add(new InitialDelta(stepSize));
            varQuadPSsHints.get(i).addInitialDelta(varQuadPSs[i], stepSize);
            problem.addHint(varQuadPSsHints.get(i));
		}
        
        problem.setVariables( variable_list );
        
        
        
		/*
		 * // use the "good BPM" list from TunePanel List<AcceleratorNode>
		 * bpmls = theRing.getAllNodesOfType("BPM"); bpmls =
		 * AcceleratorSeq.filterNodesByStatus(bpmls, true); Iterator<AcceleratorNode>
		 * bpmIt = bpmls.iterator(); while (bpmIt.hasNext()) { bpms.add((BPM)
		 * bpmIt.next()); }
		 */
	}
    
	public void run() {
        
		// prepare for online model run
		
        try {
            
            myProbe = ProbeFactory.getTransferMapProbe(theRing, AlgorithmFactory.createTransferMapTracker(theRing));
            
        } catch ( InstantiationException exception ) {
            System.err.println( "Instantiation exception creating probe." );
            exception.printStackTrace();
        }
        
        
		try {
			scenario = Scenario.newScenarioFor(theRing);
			scenario.setProbe(myProbe);
			// set the model synch mode to "DESIGN", then override with live
			// quad snapshot.
			scenario.setSynchronizationMode(Scenario.SYNC_MODE_DESIGN);
		} catch (ModelException e) {
			System.out.println(e);
		}
        
		// variable_list.clear();
        
        
        /**
         
         
         Problem problem = ProblemFactory.getInverseSquareMinimizerProblem( new ArrayList<Variable>( variableList.values()), theScorer, 0.001 );
         solver = new Solver(SolveStopperFactory.minMaxTimeSatisfactionStopper(0, timeoutPeriod, 0.00089919072));
         
         //solver
         */
        
		// set up solver
		Solver solver = new Solver(SolveStopperFactory.minMaxTimeSatisfactionStopper(0, elapseTime, 0.001 ));
		//final SimplexSearchAlgorithm algorithm = new SimplexSearchAlgorithm();
		//solver.setSearchAlgorithm(algorithm);
        //timeOut = maxBestScore = 0.01
		problem.setVariables(variable_list);
        
		se = new SimpleEvaluator(this);
		//solver.setScorer(se);
		//solver.setStopper(SolveStopperFactory.targetStopperWithMaxTime(maxBestScore, elapseTime));
        
		tp.progBar.setMaximum(Math.round(elapseTime));
		// for the progress bar:
		final int delay = 2000; // milliseconds
		fractionComplete = 0;
		ActionListener taskPerformer = new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				fractionComplete += delay / 1000;
				tp.progBar.setValue(fractionComplete);
			}
		};
        
		// progress bar timer
		javax.swing.Timer timer;
		timer = new javax.swing.Timer(delay, taskPerformer);
		timer.start();
		// solve the problem
		solver.solve(problem);
		timer.stop();
        
		tp.progBar.setValue(tp.progBar.getMaximum());
        
		ScoreBoard scoreboard = solver.getScoreBoard();
		System.out.println(scoreboard.toString());
        
		// rerun with the solution and look at results:
		HashMap<Variable, Number> solutionMap = (HashMap<Variable, Number>) scoreboard.getBestSolution().getTrialPoint().getValueMap();
		//solver.setProxyFromMap(solutionMap);
        
		NumberFormat numberFormat = NumberFormat.getNumberInstance();
		numberFormat.setMaximumFractionDigits(4);
        
		for (int i = 0; i < qPSs.size(); i++) {
			System.out.println(varQuadPSs[i].getName() + " = " + varQuadPSs[i].getInitialValue());
			tp.quadTableModel.setValueAt(numberFormat.format(varQuadPSs[i].getInitialValue()), i, 3);
            
			// calculate new set points
			double errRatio = tp.designMap.get(qPSs.get(i)).doubleValue() / varQuadPSs[i].getInitialValue();
			double newSetPt = Math.abs(errRatio * initVals[i]);
			tp.quadTableModel.setValueAt(numberFormat.format(newSetPt), i, 4);
		}
        
		tp.quadTableModel.fireTableDataChanged();
		tp.setQuadBtn.setEnabled(true);
	}
    
	public void updateModel() {
		// set updated quad value
		for (int i = 0; i < quads.size(); i++) {
			double quadVal = 0.;
			AcceleratorNode quad = quads.get(i);
			String quadPSId = quad2PSMap.get(quads.get(i).getId());
            
			for (int j = 0; j < qPSs.size(); j++) {
				if (varQuadPSs[j].getName().equals(quadPSId)) {
					quadVal = ((Quadrupole) quad).getPolarity() * varQuadPSs[j].getInitialValue();
				}
			}
            
			scenario.setModelInput(quad,
                                   ElectromagnetPropertyAccessor.PROPERTY_FIELD, quadVal);
		}
        
		try {
			// myProbe.reset();
			scenario.resetProbe();
			scenario.resync();
		} catch (SynchronizationException e) {
			System.out.println(e);
		}
        
		try {
			// scenario.setStartElementId("Ring_Inj:Foil");
			scenario.run();
		} catch (ModelException e) {
			System.out.println(e);
		}
        
	}
    
	/**
     * <p>
     * No comments were provided.
     * </p>
     * <p>
     * <h4>CKA NOTES:</h4>
     * &middot; I have refactored some of the machine parameter calculations
     * to work with the new machine parameter calculation component of the
     * the online model.  This are marked in the code.
     * </p>
     *
     * @author cp3  
     * @author Christopher K. Allen
     * @since  Jun 30, 2010 (estimated)
     * @version  Oct 30, 2013
	 */
	protected double calcError() {
		double error = 10000.;
        
		// get BPM measured phases
		double[] bpmXPhs = tp.xPhaseDiff;
		double[] bpmYPhs = tp.yPhaseDiff;
        
		// get the online model calculated BPM phase
		myProbe = (TransferMapProbe) scenario.getProbe();
		traj = (TransferMapTrajectory) myProbe.getTrajectory();
        
		// CKA - this resource is no longer needed
//		// get the 1st BPM betatron phase as the reference
//		TransferMapState state0 = (TransferMapState) traj.stateForElement(bpms
//                                                                          .get(0));
		
		// CKA - Create a machine parameter processor for the ring to replace
		//    the machine parameters that were previously processed in the
		//    simulation data itself.
		CalculationsOnRing     cmpRingParams = new CalculationsOnRing(traj);
		
		// CKA - This
		R3    vecPhase0 = cmpRingParams.ringBetatronPhaseAdvance();
		
		double xPhase0 = vecPhase0.getx();
		double yPhase0 = vecPhase0.gety();
		
		// Replaces this
//		double xPhase0 = state0.getBetatronPhase().getx();
//		double yPhase0 = state0.getBetatronPhase().gety();
        
		double sum = 0.;
		for (int i = 1; i < bpms.size(); i++) {
			TransferMapState state = (TransferMapState) traj
            .stateForElement(bpms.get(i));
			if (!tp.badBPMs.contains(new Integer(i))) {
			    
			    // CKA - This
			    R3   vecPhase = cmpRingParams.computeBetatronPhase(state);
			    
			    double xPhase = vecPhase.getx() - xPhase0;
			    double yPhase = vecPhase.gety() - yPhase0;
			    
			    // Replaces this
//				double xPhase = state.getBetatronPhase().getx() - xPhase0;
//				double yPhase = state.getBetatronPhase().gety() - yPhase0;
				
				if (xPhase < 0.)
					xPhase = xPhase + 2. * Math.PI;
				if (yPhase < 0.)
					yPhase = yPhase + 2. * Math.PI;
                
				sum = sum + (bpmXPhs[i] - xPhase) * (bpmXPhs[i] - xPhase)
                + (bpmYPhs[i] - yPhase) * (bpmYPhs[i] - yPhase);
			}
		}
        
		error = sum;
        
		System.out.println("error = " + error);
        
		return error;
	}
    
}
