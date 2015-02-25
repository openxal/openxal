package xal.app.injdumpwizard.utils;


import java.util.*;
import xal.extension.solver.*;
import xal.extension.solver.algorithm.*;
import xal.extension.solver.hint.*;

import xal.extension.widgets.plot.BasicGraphData;

/**
 *  This is a accelerator sequence class. 
 *  The sequence includes a quad, two H and V correctors, two BPMs, and one WS.  
 *
 *@author     shishlo
*/
class  IDmpAccSeq{
	
	//dignostics
	AccElem bpm00 = new AccElem();	
	AccElem bpm01 = new AccElem();	
	AccElem bpm02 = new AccElem();	
	AccElem bpm03 = new AccElem();	
	AccElem ws01 = new AccElem();
	AccElem dump = new AccElem();	
	
	//usage of diagnostics
	boolean bpm00_use = true;
	boolean bpm01_use = true;
	boolean bpm02_use = true;
	boolean bpm03_use = true;
	boolean ws01_use = true;
	
	//magnets 0.673 is an effective length of magnets
	//QV01, DCH01, DCV01 in IDmp
	AccElem quad = new AccElemQuad(0.673);	
	AccElem dch = new AccElemCorrH(0.673);	
	AccElem dcv = new AccElemCorrV(0.673);	
	
	//Vector with elements
	Vector<AccElem> accElmV = new Vector<AccElem>();
			
	//coords of particle
	double[] coords_in = new double[4];
	double[] coords_out = new double[4];
	
	//Live data
	double bpm00_x = 0.;
	double bpm00_y = 0.;
	double bpm01_x = 0.;
	double bpm01_y = 0.;
	double bpm02_x = 0.;
	double bpm02_y = 0.;
	double bpm03_x = 0.;
	double bpm03_y = 0.;
	double ws01_x = 0.;
	double ws01_y = 0.;
	
	//Init. cond. (x,xp,y,yp)
	final private Vector<ValueRef> initProxyV = new Vector<>();
	private Problem _problem;


	public IDmpAccSeq(){
		
		//make sequence
		accElmV.add(bpm00);
		accElmV.add(new AccElemDrift(10.029 - 6.48));
		accElmV.add(quad);
		accElmV.add(new AccElemDrift(10.278 - 10.029));
		accElmV.add(bpm01);
		accElmV.add(new AccElemDrift(10.6825-10.278));
		accElmV.add(dch);
		accElmV.add(dcv);
		accElmV.add(new AccElemDrift(14.672-10.6825));
		accElmV.add(bpm02);
		accElmV.add(new AccElemDrift(16.612-14.672));
		accElmV.add(ws01);		
		accElmV.add(new AccElemDrift(17.380 - 16.612));
		accElmV.add(bpm03);
		accElmV.add(new AccElemDrift(12.998));
		accElmV.add(dump);
		
		
		//set positions
		bpm00.setPosition(6.48);
		for(int i = 1; i < accElmV.size(); i++){
			double end_pos = accElmV.get(i-1).getPosition() + accElmV.get(i-1).length/2.0;
			accElmV.get(i).setPosition(end_pos + accElmV.get(i).length/2.0);
		}

		// minimize inverse square of score
		_problem = new Problem();

		//variables for initial coordinates
		final String[] variableNames = { "x", "xp", "y", "yp" };
		for ( final String variableName : variableNames ) {
			final Variable variable = new Variable( variableName, 0.0, -10.0, 10.0 );
			_problem.addVariable( variable );
			initProxyV.add( _problem.getValueReference( variable ) );
		}
	}

	
	public void setMagnetCoefs(double quad_coef, double dch_coef, double dcv_coef){
		quad.field_coef = quad_coef;
		dch.field_coef = dch_coef;
		dcv.field_coef = dcv_coef;
	}
	
	public void setMagnetFields(double quad_f, double dch_f, double dcv_f){
		quad.field = quad_f;
		dch.field = dch_f;
		dcv.field = dcv_f;	
	}
	
	public void setMagnetOffsetsX(double quad_o, double dch_o, double dcv_o){
		quad.offSetX = quad_o*0.001;
		dch.offSetX = dch_o*0.001;
		dcv.offSetX = dcv_o*0.001;	
	}	
	
	public void setMagnetOffsetsY(double quad_o, double dch_o, double dcv_o){
		quad.offSetY = quad_o*0.001;
		dch.offSetY = dch_o*0.001;
		dcv.offSetY = dcv_o*0.001;	
	}	
	
	public void setDiagUsage(boolean bpm00_u, boolean bpm01_u, boolean bpm02_u, boolean bpm03_u, boolean ws01_u){
		bpm00_use = bpm00_u;
		bpm01_use = bpm01_u;
		bpm02_use = bpm02_u;
		bpm03_use = bpm03_u;
	  ws01_use = ws01_u;
	}
	
	public void setLiveOrbitX(double bpm00_u, double bpm01_u, double bpm02_u, double bpm03_u, double ws01_u){
		bpm00_x = bpm00_u*0.001;
		bpm01_x = bpm01_u*0.001;
		bpm02_x = bpm02_u*0.001;
		bpm03_x = bpm03_u*0.001;
	  ws01_x = ws01_u*0.001;
	}
	
	public void setLiveOrbitY(double bpm00_u, double bpm01_u, double bpm02_u, double bpm03_u, double ws01_u){
		bpm00_y = bpm00_u*0.001;
		bpm01_y = bpm01_u*0.001;
		bpm02_y = bpm02_u*0.001;
		bpm03_y = bpm03_u*0.001;
	  ws01_y = ws01_u*0.001;
	}
	
	private void track(){
		coords_in[0] = initProxyV.get(0).getValue();
		coords_in[1] = initProxyV.get(1).getValue();
		coords_in[2] = initProxyV.get(2).getValue();
		coords_in[3] = initProxyV.get(3).getValue();
		for(int i = 0, n = accElmV.size(); i < n; i++){
			accElmV.get(i).track(coords_in,coords_out);
//			System.out.println("debug track from IDmpAccSeq ind="+i+" init x,xp,y,yp="+coords_in[0]+" "+coords_in[1]+" "+coords_in[2]+" "+coords_in[3]+" ");
//			System.out.println("debug track from IDmpAccSeq ind="+i+" out  x,xp,y,yp="+coords_out[0]+" "+coords_out[1]+" "+coords_out[2]+" "+coords_out[3]+" ");
			for(int j = 0; j < 4; j++){
				coords_in[j] = coords_out[j];
			}
		}
		//System.out.println("debug track from IDmpAccSeq ========================");
	}
	
	public void findOrbit() {
		// minimize inverse square of score
		initProxyV.clear();
		
		_problem = new Problem();
		final double tolerance = 0.1;	// 90% tolerance level for score of 1.0 mm error
		final Objective objective = new MinimizingObjective( tolerance );
		_problem.addObjective( objective );
		_problem.addHint( new InitialDelta( 0.01 ) );	// initial delta for all variables

		//variables for initial coordinates
		final String[] variableNames = { "x", "xp", "y", "yp" };
		for ( final String variableName : variableNames ) {
			final Variable variable = new Variable( variableName, 0.0, -50.0, 50.0 );
			_problem.addVariable( variable );
			initProxyV.add( _problem.getValueReference( variable ) );
		}

		final Evaluator evaluator = new ScoringEvaluator( new OrbitScorer(), _problem.getVariables(), objective );
		_problem.setEvaluator( evaluator );

		Solver solver = new Solver( new SimplexSearchAlgorithm(), SolveStopperFactory.maxEvaluationsStopper( 5000 ) );
		solver.solve( _problem );

		final Trial bestSolution = solver.getScoreBoard().getBestSolution();
		_problem.evaluate( bestSolution );	// force the variable references to take the optimal values

		track();
	}


	// internal class providing the score for problem to minimize the RMS orbit difference in millimeters
	private class OrbitScorer implements Scorer {
		public double score( final Trial trial, final List<Variable> variables ) {
			track();
			double diff = 0.;
			if(bpm00_use){
				diff = diff + (bpm00.outX - bpm00_x)*(bpm00.outX - bpm00_x);
				diff = diff + (bpm00.outY - bpm00_y)*(bpm00.outY - bpm00_y);
			}
			if(bpm01_use){
				diff = diff + (bpm01.outX - bpm01_x)*(bpm01.outX - bpm01_x);
				diff = diff + (bpm01.outY - bpm01_y)*(bpm01.outY - bpm01_y);
			}
			if(bpm02_use){
				diff = diff + (bpm02.outX - bpm02_x)*(bpm02.outX - bpm02_x);
				diff = diff + (bpm02.outY - bpm02_y)*(bpm02.outY - bpm02_y);
			}
			if(bpm03_use){
				diff = diff + (bpm03.outX - bpm03_x)*(bpm03.outX - bpm03_x);
				diff = diff + (bpm03.outY - bpm03_y)*(bpm03.outY - bpm03_y);
			}
			if(ws01_use){
				diff = diff + (ws01.outX - ws01_x)*(ws01.outX - ws01_x);
				diff = diff + (ws01.outY - ws01_y)*(ws01.outY - ws01_y);
			}
			return Math.sqrt( diff*1000. );
		}
	}


	/**
	* Sets the momentum of the protons in eV/c.
	*/	
	public void setMomentum(double momentum){
		for(int i = 0; i < accElmV.size(); i++){
			accElmV.get(i).momentum = momentum;
		}
	}	
	
	public double getDumpX(){
		return dump.outX*1000.0;
	}
	
	public double getDumpY(){
		return dump.outY*1000.0;
	}
	
	public void makeGraphsX(BasicGraphData expGr,BasicGraphData modelGr){
		expGr.removeAllPoints();
		modelGr.removeAllPoints();
		if(bpm00_use == true){ expGr.addPoint(bpm00.position,bpm00_x*1000.0);}
		if(bpm01_use == true){ expGr.addPoint(bpm01.position,bpm01_x*1000.0);}
		if(bpm02_use == true){ expGr.addPoint(bpm02.position,bpm02_x*1000.0);}
		if(bpm03_use == true){ expGr.addPoint(bpm03.position,bpm03_x*1000.0);}
		if(ws01_use == true){ expGr.addPoint(ws01.position,ws01_x*1000.0);}
		
		
		modelGr.addPoint(bpm00.position,bpm00.outX*1000.0);
		modelGr.addPoint(bpm01.position,bpm01.outX*1000.0);
		modelGr.addPoint(bpm02.position,bpm02.outX*1000.0);
		modelGr.addPoint(bpm03.position,bpm03.outX*1000.0);
		modelGr.addPoint(ws01.position,ws01.outX*1000.0);
		modelGr.addPoint(dump.position,dump.outX*1000.0);
		
		modelGr.addPoint(quad.position,quad.outX*1000.0);
		modelGr.addPoint(dch.position,dch.outX*1000.0);
	}
	
	public void makeGraphsY(BasicGraphData expGr,BasicGraphData modelGr){
		expGr.removeAllPoints();
		modelGr.removeAllPoints();
		if(bpm00_use == true){ expGr.addPoint(bpm00.position,bpm00_y*1000.0);}
		if(bpm01_use == true){ expGr.addPoint(bpm01.position,bpm01_y*1000.0);}
		if(bpm02_use == true){ expGr.addPoint(bpm02.position,bpm02_y*1000.0);}
		if(bpm03_use == true){ expGr.addPoint(bpm03.position,bpm03_y*1000.0);}
		if(ws01_use == true){ expGr.addPoint(ws01.position,ws01_y*1000.0);}
		
		modelGr.addPoint(bpm00.position,bpm00.outY*1000.0);
		modelGr.addPoint(bpm01.position,bpm01.outY*1000.0);
		modelGr.addPoint(bpm02.position,bpm02.outY*1000.0);
		modelGr.addPoint(bpm03.position,bpm03.outY*1000.0);
		modelGr.addPoint(ws01.position,ws01.outY*1000.0);
		modelGr.addPoint(dump.position,dump.outY*1000.0);
				
		modelGr.addPoint(quad.position,quad.outY*1000.0);
		modelGr.addPoint(dch.position,dch.outY*1000.0);
	}
	
	
	class AccElem{
		double field_coef = 1.0;
		double field = 0.0;
		//------it is 4x4 matrix and add value
		double [][] trM = new double[4][5];
		double offSetX = 0.;
		double offSetY = 0.;
		double eff_length = 0.;
		double length = 0.;
		double position = 0.;
		double inX = 0.;
		double inY = 0.;
		double outX = 0.;
		double outY = 0.;
		double c = 2.997924e+8;
		// momentum of the parcticle in eV/c
		double momentum = 0.00001;		
		
		public AccElem(){
			init();
		}
		
		void init(){
			for(int i = 0; i < 4; i++){
				for(int j = 0; j < 5; j++){
					if(i == j){
						trM[i][j] = 1.;
					} else {
						trM[i][j] = 0.;
					}
				}
			}
		}
		
		void setPosition(double pos){
			position = pos;
		}
		
		double getPosition(){
			return position;
		}
		
			void setMomentum(double P){
			momentum = P;
		}
		void setField(double f){
			field = f;
		}
    void setFieldCoeff(double coeff){
			field_coef = coeff;
		}	
		
		double getInX(){
			return inX;
		}
		
		double getInY(){
			return inY;
		}
		double getOutX(){
			return outX;
		}
		
		double getOutY(){
			return outY;
		}
		
		void makeTrM(){
		}
		
		void track(double[] coords_in, double[] coords_out){
			makeTrM();
			inX = coords_in[0];
			inY = coords_in[2];
			coords_in[0] = coords_in[0] + offSetX;
			coords_in[2] = coords_in[2] + offSetY;
			for(int i = 0; i < 4; i++){
				coords_out[i] = 0.;
				for(int j = 0; j < 4; j++){
					coords_out[i] = coords_out[i] + trM[i][j]*coords_in[j];
				}
				coords_out[i] = coords_out[i] + trM[i][4];
			}			
			coords_in[0] = coords_in[0] - offSetX;
			coords_in[2] = coords_in[2] - offSetY;
			outX = coords_out[0];
			outY = coords_out[2];			
		}
	}
	
	class AccElemDrift extends AccElem{
		public AccElemDrift(double L){
			init();
			length = L;
			trM[0][1] = L;
			trM[2][3] = L;
		}
	}
	
	class AccElemQuad extends AccElem{		
		public AccElemQuad(double effL){
			init();
			eff_length = effL;
		}		
		void makeTrM(){
			double k2 = field_coef*field/(3.33564*momentum/1.0e+9);
			double k = Math.sqrt(k2);
			double sinKL = Math.sin(k*eff_length);
			double sinhKL = Math.sinh(k*eff_length);
			trM[3][2] = -k*sinKL;
			trM[1][0] = k*sinhKL;
		}	
		
	}	
	
	class AccElemCorrH extends AccElem{		
		public AccElemCorrH(double effL){
			init();
			eff_length = effL;
		}	
		
		void makeTrM(){
			trM[1][4] = field*field_coef*c*eff_length/momentum;
		}
	}	
	
	class AccElemCorrV extends AccElem{		
		public AccElemCorrV(double effL){
			init();
			eff_length = effL;
		}	
		
		void makeTrM(){
			trM[3][4] = field*field_coef*c*eff_length/momentum;
		}
	}
}



/** objective for minimization with tolerance */
class MinimizingObjective extends Objective {
	/** tolerance for offset from minimum */
	private final double TOLERANCE;


	/** Constructor */
	public MinimizingObjective( final double tolerance ) {
		super( "MinimizingObjective" );
		TOLERANCE = tolerance;
	}


	/** satisfaction with the specifies score */
	public double satisfaction( final double score ) {
		final double satisfaction = SatisfactionCurve.inverseSatisfaction( score, TOLERANCE );
		return satisfaction;
	}
}



/** evaluator for scoring the objectives */
class ScoringEvaluator implements Evaluator {
	/** scorer */
	final private Scorer SCORER;

	/** objective to score */
	final private Objective OBJECTIVE;

	/** variables */
	final List<Variable> VARIABLES;

	
	/** Constructor */
	public ScoringEvaluator( final Scorer scorer, final List<Variable> variables, final Objective objective ) {
		SCORER = scorer;
		VARIABLES = variables;
		OBJECTIVE = objective;
	}


	/** evaluate the trial */
	public void evaluate( final Trial trial ) {
		final double score = SCORER.score( trial, VARIABLES );
		trial.setScore( OBJECTIVE, score );
	}
}

