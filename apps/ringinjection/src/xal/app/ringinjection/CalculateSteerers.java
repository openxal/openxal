/*
 * CalculateFit.java
 *
 * Created on April 1, 2004
 */

package xal.app.ringinjection;
import java.util.*;
import java.io.*;
import java.awt.*;
import javax.swing.*;

import xal.extension.application.smf.*;
import xal.smf.data.*;
import xal.smf.*;
import xal.smf.impl.*;
import xal.smf.proxy.ElectromagnetPropertyAccessor;
import xal.model.*;
import xal.model.alg.*;
import xal.sim.scenario.*;
import xal.tools.beam.calc.*;
import xal.model.probe.*;
import xal.model.probe.traj.*;
import xal.sim.sync.SynchronizationException;
import xal.model.xml.*;
import xal.tools.optimizer.*;

import xal.tools.data.*;
import xal.tools.xml.XmlDataAdaptor;
import xal.tools.beam.*;
/**
 * Performs matching to find steerer strengths for desired injection
 * spot position and angle on the foil.
 * @author  cp3
 */

public class CalculateSteerers{
	private AcceleratorSeq accSeq;
	private Scenario scenario;
	private ParticleProbe myProbe;

	private ParameterProxy varDCH22;
	private ParameterProxy varDCH24;
	private ParameterProxy varDCH28;
	private ParameterProxy varDCH30;
	private ParameterProxy varSptm;

	private ParameterProxy varDCV29;
	private ParameterProxy varDCV31;

	// steerers to use for matching
	private String[] hebt2HSteerers = {"HEBT_Mag:DCH22","HEBT_Mag:DCH24","HEBT_Mag:DCH28", "HEBT_Mag:DCH30", "HEBT_Mag:InjSptm"};
	private String[] hebt2VSteerers = {"HEBT_Mag:DCV29", "HEBT_Mag:DCV31"};

	private String[] steerers = hebt2HSteerers;  // default to horizontal

	private Accelerator accl = new Accelerator();

	private double[] params_delta = new double[4];
	private double[] init_params = new double[4];
	private double[] foil_init = new double[4];
	private double[] target_params = new double[4];

	private HDipoleCorr[] hsteererNodes;
	private VDipoleCorr[] vsteererNodes;
	private double[] hlivefields = new double[4];
	private double[] vlivefields = new double[4];
	private double[] llimits = new double[4];
	private double[] ulimits = new double[2];

	private ArrayList xvariable_list = new ArrayList();
	private ArrayList yvariable_list = new ArrayList();
	private Solver xsolver = new Solver();
	private Solver ysolver = new Solver();
	private int xarg = 0;
	private int yarg = 1;

	private double[] final_spot = new double[4];
	private double[] final_steerers = new double[6];

	private String syncstate;
	private ArrayList<String> correctorlist;

	private double solvetime;
	private double sptmvalue;

	private boolean solvex = false;
	private boolean solvey = false;

	GenDocument doc;
	public CalculateSteerers(GenDocument aDocument, String state, ArrayList<String> list, double maxtime) {
		doc=aDocument;
		syncstate=state;
		correctorlist = list;
		solvetime=maxtime;
	}

	public void run(double[] delta) {

		params_delta = delta;
		accl = doc.getAccelerator();
		AcceleratorSeq hebt1=accl.getSequence("HEBT1");
		AcceleratorSeq hebt2=accl.getSequence("HEBT2");
		ArrayList<AcceleratorSeq> lst = new ArrayList<>();
		lst.add(hebt1);
		lst.add(hebt2);

		accSeq = new AcceleratorSeqCombo("HEBT", lst);

		// set up the Scenario and initial probe
		try {
			ParticleTracker tracker = new ParticleTracker();
			myProbe = ProbeFactory.createParticleProbe(accSeq, tracker);
			scenario = Scenario.newScenarioFor(accSeq);
			scenario.setProbe(myProbe);
		}
		catch(Exception exception){
			exception.printStackTrace();
		}
		if(syncstate == "Live"){
			System.out.println("Switching to Live Lattice");
			scenario.setSynchronizationMode( Scenario.SYNC_MODE_RF_DESIGN );
		}
		else{
			System.out.println("Switching to Design Lattice");
			scenario.setSynchronizationMode( Scenario.SYNC_MODE_DESIGN );
		}
		try{
			scenario.resync();
		}
		catch(Exception exception){
			exception.printStackTrace();
		}

		calculateInitConditions();

		hsteererNodes = new HDipoleCorr[4];
		hsteererNodes[0] = (HDipoleCorr)accSeq.getNodeWithId(hebt2HSteerers[0]);
		hsteererNodes[1] = (HDipoleCorr)accSeq.getNodeWithId(hebt2HSteerers[1]);
		hsteererNodes[2] = (HDipoleCorr)accSeq.getNodeWithId(hebt2HSteerers[2]);
		hsteererNodes[3] = (HDipoleCorr)accSeq.getNodeWithId(hebt2HSteerers[3]);

		vsteererNodes = new VDipoleCorr[2];
		vsteererNodes[0] = (VDipoleCorr)accSeq.getNodeWithId(hebt2VSteerers[0]);
		vsteererNodes[1] = (VDipoleCorr)accSeq.getNodeWithId(hebt2VSteerers[1]);

		try{
			hlivefields[0] = hsteererNodes[0].getField();
			hlivefields[1] = hsteererNodes[1].getField();
			hlivefields[2] = hsteererNodes[2].getField();
			hlivefields[3] = hsteererNodes[3].getField();
			vlivefields[0] = vsteererNodes[0].getField();
			vlivefields[1] = vsteererNodes[1].getField();
		}
		catch(Exception e){
			e.printStackTrace();
		}

		varDCH22 = new ParameterProxy(hebt2HSteerers[0]+"_field", hlivefields[0], 0.0001, -0.027, 0.027);
		varDCH24 = new ParameterProxy(hebt2HSteerers[1]+"_field", hlivefields[1], 0.0001, -0.027, 0.027);
		varDCH28 = new ParameterProxy(hebt2HSteerers[2]+"_field", hlivefields[2], 0.0001, -0.027, 0.027);
		varDCH30 = new ParameterProxy(hebt2HSteerers[3]+"_field", hlivefields[3], 0.0001, -0.027, 0.027);
		varDCV29 = new ParameterProxy(hebt2VSteerers[0]+"_field", vlivefields[0], 0.0001, -0.027, 0.027);
		varDCV31 = new ParameterProxy(hebt2VSteerers[1]+"_field", vlivefields[1], 0.0001, -0.027, 0.027);

		for(int j = 0; j<4; j++) final_steerers[j] = hlivefields[j];
		for(int j = 0; j<2; j++) final_steerers[j] = vlivefields[j];

		if(correctorlist.contains((String)hebt2HSteerers[0])){
			xvariable_list.add(varDCH22);
			solvex=true;
		}
		if(correctorlist.contains((String)hebt2HSteerers[1])){
			xvariable_list.add(varDCH24);
			solvex=true;
		}
		if(correctorlist.contains((String)hebt2HSteerers[2])){
			xvariable_list.add(varDCH28);
			solvex=true;
		}
		if(correctorlist.contains((String)hebt2HSteerers[3])){
			xvariable_list.add(varDCH30);
			solvex=true;
		}
		if(correctorlist.contains((String)hebt2VSteerers[0])){
			yvariable_list.add(varDCV29);
			solvey=true;
		}
		if(correctorlist.contains((String)hebt2VSteerers[1])){
			yvariable_list.add(varDCV31);
			solvey=true;
		}

		System.out.println("Varlist " + yvariable_list + " " + varDCV29);
		//Set up and run the X solver.

		final SimplexSearchAlgorithm algorithm = new SimplexSearchAlgorithm();
		if(solvex==true){
			xsolver.setSearchAlgorithm(algorithm);
			xsolver.setVariables(xvariable_list);

			SimpleEvaluatorX sex = new CalculateSteerers.SimpleEvaluatorX();
			xsolver.setScorer(sex);
			xsolver.setStopper(SolveStopperFactory.targetStopperWithMaxTime(0.00001, solvetime));

			xsolver.solve();
			Scoreboard xscoreboard = xsolver.getScoreboard();
			System.out.println(xscoreboard.toString());
			//Calculate and store final values.
			updateModel();
			final_steerers[0]=varDCH22.getValue();
			final_steerers[1]=varDCH24.getValue();
			final_steerers[2]=varDCH28.getValue();
			final_steerers[3]=varDCH30.getValue();
			calcError(xarg, 1);
		}

		if(solvey==true){
			//Set up and run the Y solver.
			ysolver.setSearchAlgorithm(algorithm);
			ysolver.setVariables(yvariable_list);

			SimpleEvaluatorY sey = new CalculateSteerers.SimpleEvaluatorY();
			ysolver.setScorer(sey);
			ysolver.setStopper(SolveStopperFactory.targetStopperWithMaxTime(0.00001, solvetime));

			ysolver.solve();
			Scoreboard yscoreboard = ysolver.getScoreboard();
			System.out.println(yscoreboard.toString());
			//Calculate and store final values.
			updateModel();
			final_steerers[4]=varDCV29.getValue();
			final_steerers[5]=varDCV31.getValue();
			calcError(yarg, 1);
		}
	}


	public void calculateInitConditions(){
		try {
			scenario.run();
		} catch (ModelException e) {
			System.out.println(e);
		}

		Trajectory<ParticleProbeState> traj = myProbe.getTrajectory();
		final SimpleSimResultsAdaptor resultsAdaptor = new SimpleSimResultsAdaptor( traj );
		ParticleProbeState finalstate = traj.stateForElement("Ring_Inj:Foil");

		PhaseVector finalcoords = resultsAdaptor.computeCoordinatePosition( finalstate );
		foil_init[0] = finalcoords.getx(); foil_init[1] = finalcoords.getxp();
		foil_init[2] = finalcoords.gety(); foil_init[3] = finalcoords.getyp();
		target_params[0] = foil_init[0] + params_delta[0]/1000.0;
		target_params[1] = foil_init[1] + params_delta[1]/1000.0;
		target_params[2] = foil_init[2] + params_delta[2]/1000.0;
		target_params[3] = foil_init[3] + params_delta[3]/1000.0;

		System.out.println("foil_inits[0] = " + foil_init[0]);
		System.out.println("foil_inits[1] = " + foil_init[1]);
		System.out.println("params_delta[0] = " + params_delta[0]);
		System.out.println("params_delta[1] = " + params_delta[1]);
		System.out.println("target_params[0] = " + target_params[0]);
		System.out.println("target_params[1] = " + target_params[1]);
		System.out.println("foil_inits[2] = " + foil_init[2]);
		System.out.println("foil_inits[3] = " + foil_init[3]);
		System.out.println("params_delta[2] = " + params_delta[2]);
		System.out.println("params_delta[3] = " + params_delta[3]);
		System.out.println("target_params[2] = " + target_params[2]);
		System.out.println("target_params[3] = " + target_params[3]);
	}


	public void updateModel() {

		//Update the model with new steerer values.
		myProbe.reset();

		double valueDCH22 = varDCH22.getValue();
		double valueDCH24 = varDCH24.getValue();
		double valueDCH28 = varDCH28.getValue();
		double valueDCH30 = varDCH30.getValue();
		double valueDCV29 = varDCV29.getValue();
		double valueDCV31 = varDCV31.getValue();

		//System.out.println("new varDCH22 = " + valueDCH22);
		//System.out.println("new varDCH24 = " + valueDCH24);
		//System.out.println("new varDCH28 = " + valueDCH28);
		//System.out.println("new varDCH30 = " + valueDCH30);
		//System.out.println("new varDCV29 = " + valueDCV29);
		//System.out.println("new varDCV31 = " + valueDCV31);

		//Set the steerer values.
		scenario.setModelInput(hsteererNodes[0], ElectromagnetPropertyAccessor.PROPERTY_FIELD, valueDCH22);
		scenario.setModelInput(hsteererNodes[1], ElectromagnetPropertyAccessor.PROPERTY_FIELD, valueDCH24);
		scenario.setModelInput(hsteererNodes[2], ElectromagnetPropertyAccessor.PROPERTY_FIELD, valueDCH28);
		scenario.setModelInput(hsteererNodes[3], ElectromagnetPropertyAccessor.PROPERTY_FIELD, valueDCH30);
		scenario.setModelInput(vsteererNodes[0], ElectromagnetPropertyAccessor.PROPERTY_FIELD, valueDCV29);
		scenario.setModelInput(vsteererNodes[1], ElectromagnetPropertyAccessor.PROPERTY_FIELD, valueDCV31);

		//Resync the model.
		try {
			scenario.resync();
		} catch (SynchronizationException e) {
			System.out.println(e);
		}

		//Run the model.
		try {
			scenario.run();
		} catch (ModelException e) {
			System.out.println(e);
		}

	}

	public double calcError(int arg, int farg) {

		//Calculate parameters at the foil

		Trajectory<ParticleProbeState> traj = myProbe.getTrajectory();
		final SimpleSimResultsAdaptor resultsAdaptor = new SimpleSimResultsAdaptor( traj );
		ParticleProbeState finalstate = traj.stateForElement("Ring_Inj:Foil");
		PhaseVector finalcoords = resultsAdaptor.computeCoordinatePosition( finalstate );

		double xfoil;
		double xpfoil;
		double yfoil;
		double ypfoil;
		double error = 1000.;

		//System.out.println("traj = " + traj);
		//System.out.println("finalcoords = " + finalcoords);

		if(arg==0){
			xfoil  = finalcoords.getx();
			xpfoil = finalcoords.getxp();
			//System.out.println("targetparams = " +target_params[0] + " " + target_params[1]);
			error = Math.pow((xfoil - target_params[0]), 2.) + Math.pow((xpfoil - target_params[1]), 2.);
			error = Math.sqrt(error);
			//System.out.println("delta_x = " + 1000.0*(xfoil - foil_init[0]));
			//System.out.println("delta_xp = " + 1000.0*(xpfoil - foil_init[1]));
			if(farg == 1){
				final_spot[0] = 1000.0*(xfoil - foil_init[0]);
				final_spot[1] = 1000.0*(xpfoil - foil_init[1]);
			}
		}
		else{
			yfoil  = finalcoords.gety();
			ypfoil = finalcoords.getyp();
			//System.out.println("targetparams = " +target_params[2] + " " + target_params[3]);
			error = Math.pow((yfoil - target_params[2]), 2.) + Math.pow((ypfoil - target_params[3]), 2.);
			error = Math.sqrt(error);
			//System.out.println("delta_y = " + 1000.0*(yfoil - foil_init[2]));
			//System.out.println("delta_yp = " + 1000.0*(ypfoil - foil_init[3]));
			if(farg == 1){
				final_spot[2] = 1000.0*(yfoil - foil_init[2]);
				final_spot[3] = 1000.0*(ypfoil - foil_init[3]);
			}
		}

		return error;
	}


	class SimpleEvaluatorX implements Scorer{
		public double score() {
			updateModel();
			double myScore = calcError(xarg, 0);
			return myScore;
		}

	}

	class SimpleEvaluatorY implements Scorer{
		public double score() {
			updateModel();
			double myScore = calcError(yarg, 0);
			return myScore;
		}

	}

	public double[] getFinalSpot(){
		return final_spot;
	}

	public double[] getFinalSteerers(){
		return final_steerers;
	}

}



