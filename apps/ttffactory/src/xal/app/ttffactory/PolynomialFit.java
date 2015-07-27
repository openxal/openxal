/*
 * PolynomialFit.java Fits a 5th order polynomial to given x and y data
 * @author James Ghawaly Jr.
 * @author Doug Brown
 *
 * Copyright (c) 2015 Spallation Neutron Source
 * Oak Ridge National Laboratory
 * Oak Ridge, TN 37830
 */

package xal.app.ttffactory;


import xal.extension.solver.*;
import xal.extension.solver.algorithm.*;
import xal.tools.math.Rn.Rmxn;
import xal.tools.math.Rn.Rn;

import java.lang.*;
import java.util.*;

public class PolynomialFit{
    final double[] data;
    final double[] positionArray;
    
    public PolynomialFit(double[] x, double[] y){
    	data = x;
    	positionArray = y;
    	
    	double[] coeffs = initValues();
    	
    	for(double dbl:coeffs) {
    		System.out.println(Double.toString(dbl));
    	}
    	
    	double norm = norm(coeffs);

    	//run(coeffs,norm);

    }
    
    
    /*
     * this is runs the fit program
     */
    private void run(double[] vars, double norm){
        /*
         * Here is where you define your variables that the program will change when searchign for a solution
         * the order is very important!!! y = a0 + a1*x + a2*x^2 + ..., this equation will be used by order of creation, NOT by anything related to the name
         */
        ArrayList<Variable> variables = new ArrayList<Variable>();
        
        double a0 = vars[0];
        double a1 = vars[1];
        double a2 = vars[2];
        double a3 = vars[3];
        double a4 = vars[4]; 
        
        variables.add( new Variable("a0", a0, a0-norm, a0+norm));
        variables.add( new Variable("a1", a1, a1-norm, a1+norm));
        variables.add( new Variable("a2", a2, a2-norm, a2+norm));
        variables.add( new Variable("a3", a3, a3-norm, a3+norm));
        variables.add( new Variable("a4", a4, a4-norm, a4+norm)); 
        
        
        /*
         * here you can define the different ways with which you wish the program to stop searching for solution
         */
        //Stopper maxSolutionStopper = SolveStopperFactory.maxElapsedTimeStopper(15);
        
        // other options for stopping the program
        Stopper maxSolutionStopper = SolveStopperFactory.minMaxTimeSatisfactionStopper( 0, 100, 0.90000 );
        //        Stopper maxSolutionStopper = SolveStopperFactory.maxEvaluationsStopper( 200 );
        //        Stopper maxSolutionStopper = SolveStopperFactory.maxElapsedTimeStopper(3);
        //        Stopper maxSolutionStopper = SolveStopperFactory.minSatisfactionStopper(0.7);
        //        Stopper maxSolutionStopper = SolveStopperFactory.maxEvaluationsSatisfactionStopper(30000, 0.7)
        
        
        /*
         * first one will choose a variety of algorithms as it solves the problem
         * you can choose specifically from a different algorithms (which is the second line of code
         */
        Solver solver = new Solver(maxSolutionStopper );
        //Solver solver = new Solver(new RandomSrhinkSearch(), maxSolutionStopper); // this solver will solve the problem only using algorithm. Algorithms are under "algorithm" in solver folder
        
        // just a list of objectives, in this case there is only one objective
        ArrayList<Objective> objectives = new ArrayList<Objective>();
        objectives.add(new FitObjective("FitObjective"));
        
        // used for evaulating the trial point
        Evaluator fitEvaluator = new FitEvaluator(objectives, variables, data, positionArray );
        
        // simply defining the problem
        Problem problem = new Problem(objectives, variables, fitEvaluator);
        
        // tell the program to solve the problem
        solver.solve(problem);
        
        // print the results
        printResults(solver.getScoreBoard(), variables);
    }
    

    
    /*
     * the objetive desired is to fit a line to data.
     * the score is simply the error (leastSquaresFit value) between the fit line and the data
     */
    public class FitObjective extends Objective{
        public FitObjective(String name){
            super(name);
        }
        
        public double satisfaction(double score){
            return Math.exp(-score);
        }
    }
    
    
    /*
     * class which evaluates the individual trials
     */
    public class FitEvaluator implements Evaluator{
        private ArrayList<Objective> _objectives;
        private ArrayList<Variable> _variables;
        private double[] _data; // the data that was recorded
        private double[] _positionArray; // representing the "location" that the data was recorded
        
        // just instantiating
        public FitEvaluator(ArrayList<Objective> objectives, ArrayList<Variable> variables, double[] data, double[] positionArray){
            _objectives = objectives;
            _variables = variables;
            _data = data;
            _positionArray = positionArray;
        }
        
        // evaluate the trial
        public void evaluate(Trial trial){
            for(Objective objective: _objectives){
                
                TrialPoint trialPoint = trial.getTrialPoint();
                double error = calcError(trialPoint, _variables, _data, _positionArray);
                trial.setScore(objective, error);
            }
        }
    }
    
    
    /*
     * used to caluclate the error of the specific trial
     */
    public double calcError(TrialPoint trialPoint, ArrayList<Variable> variables, double[] data, double[] positionArray){
        double error = 0;
        
        double numOfPoints = positionArray.length;
        
        // hold the constants that were generated by the trial
        double[] constants = new double[variables.size()];
        
        int i = 0;
        for(Variable variable: variables){
            constants[i] = trialPoint.getValue(variable);
            i++;
        }
        
        // constant is the individual value generated by the trial
        // testData is the resulting data point from the constants
        // position is the point at which the data was recorded
        double constant;
        double testData = 0;
        double position;
        
        for(int j = 0; j < numOfPoints; j++){
            position = positionArray[j];
            
            for(i = 0; i < constants.length; i++){
                constant = constants[i];
                
                // generate the testData point from the constants
                testData += constant * Math.pow(position, i);
            }
            
            // get the difference squared
            error += Math.pow((data[j] - testData), 2);
            testData = 0;
        }
        
        return error;
    }

    
    
    /*
     * Used to print out the general results of the run
     */
    public void printResults(ScoreBoard scoreBoard, ArrayList<Variable> variables){

        Trial trial = scoreBoard.getBestSolution();
        TrialPoint trialPoint = trial.getTrialPoint();
        
        int evaluations = scoreBoard.getEvaluations();
        double satisfaction = scoreBoard.getBestSolution().getSatisfaction();
        double elapsedTime = scoreBoard.getElapsedTime();
        Map<Variable,Number> valueMap = trialPoint.getValueMap();

        String label = trial.getAlgorithm().getLabel();
        
        
        System.out.printf("\n          ##################### Overall Results #####################\n");
        System.out.printf("%20s --> ElapsedTime: %7.3f || Evaluations: %8d || Satisfaction: %11.9f\n", label, elapsedTime, evaluations, satisfaction);
        
        /*
         * Print out the variables and their coresponding values
         */
        
        System.out.printf("###### Variables ######\n");
        //System.out.printf("%20-s: \n", label);
        valueMap = trialPoint.getValueMap();
        
        System.out.printf("     ----> ");
        for(Variable variable: variables){
            Number value = valueMap.get(variable);
            System.out.printf("%5s: %8.4f || ", variable.getName(), value);
        }
        System.out.printf("\n");
    }
    
    public double[] initValues() {
    	Tools tools = new Tools();
    	
    	double[][] x2D = new double[data.length][5];
    	
    	/*
    	 * Create a 2D Nx5 matrix from the Beta values
    	 * where N is the length of data: e.g. the number of betas
    	 * 
    	 * [1 B1 B1^2 B1^3 B1^4]
    	 * [1 B2 B2^2 B2^3 B2^4]
    	 * [...                ]
    	 * [1 BN BN^2 BN^3 BN^4]
    	 */
    	int i =0;
    	for(double dbl:data){
    		x2D[i][0] = 1.0;
    		x2D[i][1] = dbl;
    		x2D[i][2] = Math.pow(dbl, 2.0);
    		x2D[i][3] = Math.pow(dbl, 3.0);
    		x2D[i][4] = Math.pow(dbl, 4.0);
    		i++;
    	}
    	
    	Rmxn xMat = new Rmxn(x2D.clone());               //K
    	
    	Rmxn yMat = new Rmxn(positionArray.length,1);    //T
    	
    	i = 0;
    	for(double dbl:positionArray) {
    		yMat.setElem(i, 0, dbl);
    	}
    	
    	Rmxn xT = xMat.transpose();                  //KT
    	
    	Rmxn xTx = xT.times(xMat);                   //KT K

    	Rmxn xTy = xT.times(yMat);                   //KT T
    
    	Rmxn xTxI = xTx.inverse();                   //(KT K)^-1

    	Rmxn c = xTxI.times(xTy);                 //polynomial coefficients

    	double[] init = new double[5];
    	
    	for(int rowIndex = 0;rowIndex<c.getRowCnt();rowIndex++) {
    		init[rowIndex] = c.getElem(rowIndex, 0);
    	}
    	
		return init;
    	
    }
    
    public double norm(double[] c) {
    	double total = 0.0;
    	for(double dbl: c) {
    		total+=Math.pow(dbl,2.0);
    	}
    	return Math.sqrt(total);
    }
    
}

