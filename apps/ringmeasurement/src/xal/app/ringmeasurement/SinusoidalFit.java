/*
 * SinusoidalFit.java
 *
 * Created on February 16, 2005, 2:04 PM
 */

package xal.app.ringmeasurement;

import java.util.*;

import xal.tools.solver.*;
import xal.tools.solver.algorithm.*;
import xal.tools.plot.BasicGraphData;

/**
 *
 * @author  Paul Chu
 */
public class SinusoidalFit {
    
    double[] y;
    
    static double amp = 0.;
    static double offset = 0.;
    static double tune = 0.;
    static double exp = 0.;
    static double phi = 0.;
    double maxTime = 0.;
    long len = 40;
    
    /** Creates a new instance of SinusoidalFit */
    public SinusoidalFit(double[] array) {
        y = new double[array.length];
        System.arraycopy(array, 0, y, 0, array.length);
    }
    
    public void setFitParameters(double A, double c, double w, 
    		double b, double d, double maxTime, long len) {
    	amp = A;
    	exp = c;
    	tune = w;
    	phi = b;
    	offset = d;
    	this.maxTime = maxTime;
    	this.len = len;
    }
    
    public void fit() {
        /* Setup the variable 
         * the function will look like A*sin(w*x) + c
         */
        List<Variable> variables = new ArrayList<Variable>();
        Variable b = new Variable( "b", phi, -0.5, 0.5 );
        variables.add( b );
        Variable A = new Variable( "A", amp, 12., 22. );
        variables.add( A );
        Variable w = new Variable( "w", tune, 0., 0.5 );
        variables.add( w );
        Variable c = new Variable( "c", exp, 0., 0.1 );
        variables.add( c );
        Variable d = new Variable( "d", offset, 0., -0. );
        variables.add( d );

        /** Setup a stopper */
        Stopper maxSolutionStopper = SolveStopperFactory.minMaxTimeSatisfactionStopper( 1, maxTime, 0.99 );

        /** Setup the solver */
        Solver solver = new Solver( new RandomShrinkSearch(), maxSolutionStopper );

        SineFitObjective objective = new SineFitObjective("Exponentially Decayed Sine Wave", y, 0.);
        objective.setDataLength(len);
        List<Objective> objectives = new ArrayList<Objective>();
        objectives.add(objective);
        
        SineFitEvaluator evaluator = new SineFitEvaluator();
        
        Problem problem = new Problem(objectives, variables, evaluator);
        
        solver.solve(problem);
        
        // get results
        amp = solver.getScoreBoard().getBestSolution().getTrialPoint().getValue(A);
        offset = solver.getScoreBoard().getBestSolution().getTrialPoint().getValue(d);
        tune = solver.getScoreBoard().getBestSolution().getTrialPoint().getValue(w);
        exp = solver.getScoreBoard().getBestSolution().getTrialPoint().getValue(c);
        phi = solver.getScoreBoard().getBestSolution().getTrialPoint().getValue(b);
        System.out.println("A = " + amp);
        System.out.println("offset = " + offset);
        System.out.println("tune = " + tune);
        System.out.println("exp = " + exp);
        System.out.println("phi = " + phi);
        System.out.println("Score = " + solver.getScoreBoard().getBestSolution().getScore(objective));
        
    }
    
    public BasicGraphData getFittedData() {
    	BasicGraphData graphData = new BasicGraphData();
    	double[] x = new double[1000];
    	double[] y = new double[1000];
    	double del = len/1000.;
    	
    	for (int i=0; i<x.length; i++) {
    		x[i] = i*del;
    		y[i] = amp*Math.exp(-1.*exp*x[i])*Math.sin(2.*Math.PI*(tune*x[i] + phi)) + offset;
//    		y[i] = amp*Math.exp(-1.*exp*x[i])*Math.sin(2.*Math.PI*(tune*x[i] + phi));
    	}
		graphData.addPoint(x, y);
    	
    	return graphData;
    }
    
    /**
     * @return Amplitude
     */
    public double getAmp() {
        
        return amp;
    }
    
    /**
     * @return overall offset
     */
    public double getOffset() {
        
        return offset;
    }
    
    /**
     * @return tune
     */
    public double getTune() {
        
        return tune;
    }
    
    /**
     * @return exponential decay factor
     */
    public double getExp() {
        
        return exp;
    }
    
    /**
     * @return phase offset
     */
    public double getPhiOffset() {
        
        return phi;
    }
}
