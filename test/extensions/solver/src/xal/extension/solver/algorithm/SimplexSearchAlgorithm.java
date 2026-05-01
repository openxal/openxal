/*
 *  SimplexSearchAlgorithm.java
 *
 *  Created Wednesday July 5, 2005
 *
 *  Copyright 2003, Spallation Neutron Source
 *  Oak Ridge National Laboratory
 *  Oak Ridge, TN 37830
 */
package xal.extension.solver.algorithm;

import xal.tools.messaging.MessageCenter;

import xal.extension.solver.*;
import xal.extension.solver.solutionjudge.*;
import xal.extension.solver.market.*;
import xal.extension.solver.hint.*;

import java.util.*;

/**
 *  Simplex optimization search algorithm.
 *
 *@author    shishlo
 */
public class SimplexSearchAlgorithm extends SearchAlgorithm {

	/**
	 *  The current best point.
	 */
	private TrialPoint _bestPoint;

	private SimplexSearcher _searcher = new SimplexSearcher(this);

	private boolean algorithmChanged = false;


	/**
	 *  Empty constructor.
	 */
	public SimplexSearchAlgorithm() {
		super();
	}


	/**
	 *  Set the specified problem to solve. Override the inherited method to
	 *  initialize the sercher.
	 *
	 *@param  problem  the problem to solve
	 */
	public void setProblem(final Problem problem) {
		super.setProblem(problem);
		_searcher.setProblem(problem);
	}


	/**
	 *  Reset this algorithm.
	 */
	public void reset() {
		if (_bestPoint != null) {
			_searcher.reset(_bestPoint);
		} else {
			_searcher.setProblem(_problem);
		}
	}


	/**
	 *  Get the label for this search algorithm.
	 *
	 *@return    The label for this algorithm
	 */
	public String getLabel() {
		return "Simplex Search Algorithm";
	}


	/**
	 *  Calculate the next few trial points.
	 */
	public void performRun(AlgorithmSchedule algorithmSchedule) {
        int initialCount = getEvaluationsLeft();
        while(getEvaluationsLeft() > 0){
            if(algorithmSchedule.shouldStop()) return;
            if (algorithmChanged) {
                if (_bestPoint != null) {
                    _searcher.reset(_bestPoint);
                }
                algorithmChanged = false;
            }
            
            if (!_searcher.makeStep()) {
                _searcher.setWantToStop(true);
            }
            
            if(getEvaluationsLeft() == initialCount){
                break;
            }
            initialCount = getEvaluationsLeft();
        }
	}


	/**
	 *  Get the minimum number of evaluations per run.
	 *
	 *@return    the minimum number of evaluation per run.
	 */
	public int getMinEvaluationsPerRun() {
		if (_problem != null && !_searcher.getWantToStop()) {
			return 5*(_problem.getVariables().size() + 1) + 40;
		}
		return 0;
	}


	/**
	 *  Get the maximum number of evaluations per run.
	 *
	 *@return    the maximum number of evaluation per run.
	 */
	public int getMaxEvaluationsPerRun() {
		if (_problem != null && !_searcher.getWantToStop()) {
			return 8 * (_problem.getVariables().size() + 40);
		}
		return 0;
	}


	/**
	 *  Get the rating for this algorithm which in an integer between 0 and 10 and
	 *  indicates how well this algorithm performs on global searches.
	 *
	 *@return    The global search rating for this algorithm.
	 */
	public int globalRating() {
		return 3;
	}


	/**
	 *  Get the rating for this algorithm which in an integer between 0 and 10 and
	 *  indicates how well this algorithm performs on local searches.
	 *
	 *@return    The local search rating for this algorithm.
	 */
	public int localRating() {
		return 5;
	}


	/**
	 *  Handle a message that a trial has been scored.
	 *
	 *@param  schedule  Description of the Parameter
	 *@param  trial     Description of the Parameter
	 */
	public void trialScored(final AlgorithmSchedule schedule, Trial trial) {
		SearchAlgorithm s_a = trial.getAlgorithm();
		if (s_a.getLabel().equals(getLabel())) {
			SimplexSearchAlgorithm s_s_a = (SimplexSearchAlgorithm) s_a;
			if (s_s_a != this) {
				algorithmChanged = true;
			}
		} else {
			algorithmChanged = true;
		}

	}


	/**
	 *  Handle a message that a trial has been vetoed.
	 *
	 *@param  schedule  Description of the Parameter
	 *@param  trial     Description of the Parameter
	 */
	public void trialVetoed(final AlgorithmSchedule schedule, final Trial trial) {
		_searcher.setWantToStop(true);
	}


	/**
	 *  Send a message that a new optimal solution has been found.
	 *
	 *@param  source     The source of the new optimal solution.
	 *@param  solutions  The list of solutions.
	 *@param  solution   The new optimal solution.
	 */
	public void foundNewOptimalSolution(final SolutionJudge source, final List<Trial> solutions, final Trial solution) {
		TrialPoint newPoint = solution.getTrialPoint();
		_bestPoint = newPoint;
		SearchAlgorithm s_a = solution.getAlgorithm();
		if (s_a.getLabel().equals(getLabel())) {
			SimplexSearchAlgorithm s_s_a = (SimplexSearchAlgorithm) s_a;
			if (s_s_a != this) {
				//the simplex should be moved to the new point
				//that was found by other algorithm
				//The simplex vertexes should be generated from the scratch.
				_searcher.reset(_bestPoint);
			}
		} else {
			//the simplex should be moved to the new point
			//that was found by other algorithm
			//The simplex vertexes should be generated from the scratch.
			_searcher.reset(_bestPoint);
		}
	}
}

/**
 *  This class implements the simplex algorithm
 *
 *@author    shishlo
 */

//----------------------------------------------------------------------------
//Simplex minimizes an arbitrary nonlinear function of N variables by the
//Nedler-Mead Simplex method as described in:
//
//Nedler, J.A. and Mead, R. "A Simplex Method for Function Minimization."
//    Computer Journal 7 (1965): 308-313.
//
//It makes no assumptions about the smoothness of the function being minimized.
//It converges to a local minimum which may or may not be the global minimum
//depending on the initial guess used as a starting point.
//Parameters of the algorithm:
//reflection  rho   = 1
//expansion   chi   = 2
//contraction gamma = 0.5
//shinkage    sigma = 0.5
//-----------------------------------------------------------------------------

class SimplexSearcher {


	private double bestScore = Double.MAX_VALUE;
	private volatile boolean shouldStop = true;

	private CompareVertex comparator = new CompareVertex();
    
    private SearchAlgorithm _simplexAlgorithm;

	//dimension
	private int nD = 0;

	//parameters
	private double rho = 1.0;
	private double chi = 2.0;
	private double gamma = 0.5;
	private double sigma = 0.5;

	//vertexes
	private Vector<Vertex> vertexesV = new Vector<Vertex>();
	private double[] stepArr = new double[0];

	private double[] coord_r = new double[0];
	private double[] coord_e = new double[0];
	private double[] coord_oc = new double[0];
	private double[] coord_ic = new double[0];

	private Vertex vt_r = new Vertex();
	private Vertex vt_e = new Vertex();
	private Vertex vt_oc = new Vertex();
	private Vertex vt_ic = new Vertex();

	//shrinkage limit and counter
	private int nShrinkMax = 20;
	private int shrinkCount = 0;

	//the state of the initial simplex
	private boolean iniSimplexReady = false;

	/**
	 *  the problem to solve
	 */
	private Problem _problem = null;


	/**
	 *  Creates a new instance of SimplexSearcher
	 */
	public SimplexSearcher(SearchAlgorithm algorithm) {
        _simplexAlgorithm = algorithm;
    }


	/**
	 *  Returns the wantToStop boolean attribute
	 *
	 *@return    The wantToStop value
	 */
	protected boolean getWantToStop() {
		return shouldStop;
	}


	/**
	 *  Sets the wantToStop boolean attribute
	 *
	 *@param  shouldStop  The new wantToStop value
	 */
	protected void setWantToStop(boolean shouldStop) {
		this.shouldStop = shouldStop;
	}


	/**
	 *  reset for searching from scratch; forget history
	 *
	 *@param  tr_point  Description of the Parameter
	 */
	protected void reset(TrialPoint tr_point) {

		iniSimplexReady = false;

		if (_problem == null) {
			setWantToStop(true);
			return;
		}
		shouldStop = false;
		bestScore = Double.MAX_VALUE;
		shrinkCount = 0;

		nD = _problem.getVariables().size();

		if (nD == 0) {
			setWantToStop(true);
			return;
		}

		//create all arrays
		stepArr = new double[nD];

		coord_r = new double[nD];
		coord_e = new double[nD];
		coord_oc = new double[nD];
		coord_ic = new double[nD];

		vt_r.setCoords(coord_r);
		vt_e.setCoords(coord_e);
		vt_oc.setCoords(coord_oc);
		vt_ic.setCoords(coord_ic);

		vt_r.setProblem(_problem);
		vt_e.setProblem(_problem);
		vt_oc.setProblem(_problem);
		vt_ic.setProblem(_problem);

		//create vertixes
		vertexesV.clear();
		for (int i = 0; i <= nD; i++) {
			Vertex vt = new Vertex();
			vt.setProblem(_problem);
			vertexesV.add(vt);
		}

		//define steps and cooordinates for first vertex
		Vertex centerVertex = vertexesV.firstElement();
		//use initial delta hint
		InitialDelta hint = (InitialDelta) _problem.getHint(InitialDelta.TYPE);
		int ind = 0;
		for ( final Variable variable : _problem.getVariables() ) {
			double value = variable.getInitialValue();
			centerVertex.getCoords()[ind] = value;
			//this is our approach - it is guess only
			double step = Math.abs((variable.getUpperLimit() - variable.getLowerLimit())) / 20.;
			if (hint != null) {
				double[] range = hint.getRange(variable);
				step = Math.abs(range[0] - range[1]) / 2.0;
			}
			stepArr[ind] = step;
			ind++;
		}

		//prepare the coordinates of the others vertexes
		for (int iv = 1; iv <= nD; iv++) {
			Vertex vr = vertexesV.get(iv);
			for (int i = 0; i < nD; i++) {
				vr.getCoords()[i] = 0.;
				for (int ivf = 0; ivf < iv; ivf++) {
					Vertex vrf = vertexesV.get(ivf);
					vr.getCoords()[i] = vr.getCoords()[i] + vrf.getCoords()[i] / iv;
				}
			}
			vr.getCoords()[iv - 1] = vr.getCoords()[iv - 1] + stepArr[iv - 1];
		}

		//check that simplex is inside limits
		if (!acceptSimplex()) {
			setWantToStop(true);
			return;
		}

		setWantToStop(false);
	}


	/**
	 * Sets the problem
	 * @param  problem  The problem to solve
	 */
	protected void setProblem( final Problem problem ) {
		_problem = problem;

		final List<Variable> variables = _problem.getVariables();
		final MutableTrialPoint trialPoint = new MutableTrialPoint(variables.size());
        
        for ( final Variable variable : variables ) {
			final double value = variable.getInitialValue();
			trialPoint.setValue( variable, value );
		}

		reset( trialPoint.getTrialPoint() );
	}


	/**
	 *  Makes one step in the search. It returns ShouldStop boolean.
	 *
	 *@return               The ShouldStop boolean.
	 */
	protected boolean makeStep() {

		if (getWantToStop()) {
			return false;
		}

		//prepare scores for the initial simplex
		if (!iniSimplexReady) {
			//find score for vertexes and sort them
			if (!findScores()) {
				setWantToStop(true);
				return false;
			}
			iniSimplexReady = true;
		}

		//for debug purposes
		//printSimplex();

		//Stage 1. Sort ==============
		Collections.sort(vertexesV, comparator);

		//sort simplex, set best solution
		bestScore = vertexesV.get(0).getScore();

		//Stage 2. Reflect ==============
		reflectVertex(rho, coord_r);
		vt_r.setCoords(coord_r);

		if (!findScore(vt_r)) {
			//for debug purposes only
			//printVertex(vt_r, "vt_r");
			return false;
		}
		double score_r = vt_r.getScore();

		//if f_r < f(n) i.e. f(n) - it is not last
		if (score_r < vertexesV.get(nD - 1).getScore()) {
			//Stage 3. Expand
			reflectVertex(rho * chi, coord_e);
			vt_e.setCoords(coord_e);

			if (!findScore(vt_e)) {
				return false;
			}
			double score_e = vt_e.getScore();

			if (score_r < vertexesV.get(0).getScore() && score_e <= score_r) {
				setLastVertex(coord_e, score_e);
				//System.out.println("debug  ==Stage 3. Expand coord_e");
			} else {
				setLastVertex(coord_r, score_r);
				//System.out.println("debug  ==Stage 3. Expand coord_r");
			}
		} else {

			boolean goToShrink = false;

			if (score_r < vertexesV.get(nD).getScore()) {
				//Stage 4.a Contract
				reflectVertex(rho * gamma, coord_oc);
				vt_oc.setCoords(coord_oc);

				if (!findScore(vt_oc)) {
					return false;
				}
				double score_oc = vt_oc.getScore();

				if (score_oc < score_r) {
					setLastVertex(coord_oc, score_oc);
					//System.out.println("debug  ==Stage 4a. Contract");
				} else {
					goToShrink = true;
				}
			} else {
				//Stage 4.b Contract
				reflectVertex(-gamma, coord_ic);
				vt_ic.setCoords(coord_ic);

				if (!findScore(vt_ic)) {
					return false;
				}
				double score_ic = vt_ic.getScore();

				if (score_ic < vertexesV.get(nD).getScore()) {
					setLastVertex(coord_ic, score_ic);
					//System.out.println("debug  ==Stage 4b. Contract");
				} else {
					goToShrink = true;
				}
			}
			if (goToShrink) {
				shrinkSimplex();
				//System.out.println("debug  ==Stage Shrink");
				shrinkCount++;
				if (getWantToStop() || !findScores0()) {
					return false;
				}
			}
		}
		//-------------------------------------
		//final actions
		//-------------------------------------

		//sort simplex, set best solution and coord. steps
		Collections.sort(vertexesV, comparator);
		bestScore = vertexesV.get(0).getScore();

		if (shrinkCount > nShrinkMax) {
			setWantToStop(true);
		}

		//for debug purposes
		//printSimplex();

		return (!getWantToStop());
	}


	/**
	 *  returns the best score after step
	 *
	 *@return    The bestScore value
	 */
	protected double getBestScore() {
		return bestScore;
	}


	//------------------------------------------
	//private methods specific to the simplex algorithm
	//------------------------------------------
	/**
	 *  Description of the Method
	 *
	 *@param  vr            Description of the Parameter
	 *@return               Description of the Return Value
	 */
	private boolean findScore(Vertex vr) {
		if (getWantToStop()) {
			return false;
		}
		boolean res = vr.findScore(_simplexAlgorithm);
		if (getWantToStop()) {
			return false;
		}
		if (!res) {
			setWantToStop(true);
		}
		return res;
	}


	/**
	 *  Description of the Method
	 *
	 *@return               Description of the Return Value
	 */
	private boolean findScores() {
		for (int iv = 0; iv <= nD; iv++) {
			Vertex vr = vertexesV.get(iv);
			if (getWantToStop() || !findScore(vr)) {
				return false;
			}
		}
		Collections.sort(vertexesV, comparator);
		return true;
	}


	//do not include first vertex
	/**
	 *  Description of the Method
	 *
	 *@return               Description of the Return Value
	 */
	private boolean findScores0() {
		for (int iv = 1; iv <= nD; iv++) {
			Vertex vr = vertexesV.get(iv);
			if (getWantToStop() || !findScore(vr)) {
				return false;
			}
		}
		Collections.sort(vertexesV, comparator);
		return true;
	}


	/**
	 *  Description of the Method
	 *
	 *@return    Description of the Return Value
	 */
	private boolean acceptSimplex() {
		int nVars = _problem.getVariables().size();
		if (nVars != nD || (nD + 1) != vertexesV.size()) {
			return false;
		}
		return true;
	}


	/**
	 *  Description of the Method
	 */
	private void shrinkSimplex() {
		Vertex vr0 = vertexesV.get(0);
		for (int iv = 1; iv <= nD; iv++) {
			Vertex vr = vertexesV.get(iv);
			for (int i = 0; i < nD; i++) {
				vr.getCoords()[i] = sigma * vr.getCoords()[i] + (1.0 - sigma) * vr0.getCoords()[i];
			}
		}
	}


	//calculates x = x_avg + coeff*(x_avg - x_last)
	/**
	 *  Description of the Method
	 *
	 *@param  coeff   Description of the Parameter
	 *@param  resArr  Description of the Parameter
	 */
	private void reflectVertex(double coeff, double[] resArr) {
		double coeff0 = (1.0 + coeff) / nD;
		Vertex vrN = vertexesV.get(nD);
		for (int i = 0; i < nD; i++) {
			resArr[i] = 0.;
			for (int iv = 0; iv < nD; iv++) {
				Vertex vr = vertexesV.get(iv);
				resArr[i] += coeff0 * vr.getCoords()[i];
			}
			resArr[i] -= coeff * vrN.getCoords()[i];
		}
	}


	//calculates x = x_avg + coeff*(x_avg - x_last)
	//               for last vertex of the simplex
	/**
	 *  Description of the Method
	 *
	 *@param  coeff   Description of the Parameter
	 *@param  resArr  Description of the Parameter
	 */
	private void reflectSimplex(double coeff, double[] resArr) {
		reflectVertex(coeff, resArr);
		Vertex vrN = vertexesV.get(nD);
		for (int i = 0; i < nD; i++) {
			vrN.getCoords()[i] = resArr[i];
		}
	}


	/**
	 *  Sets the lastVertex attribute of the SimplexSearcher object
	 *
	 *@param  resArr  The new lastVertex value
	 *@param  score   The new lastVertex value
	 */
	private void setLastVertex(double[] resArr, double score) {
		Vertex vrN = vertexesV.get(nD);
		for (int i = 0; i < nD; i++) {
			vrN.getCoords()[i] = resArr[i];
		}
		vrN.setScore(score);
	}


	/**
	 *  Gets the firstVertexCoords attribute of the SimplexSearcher object
	 *
	 *@param  resArr  Description of the Parameter
	 *@return         The firstVertexCoords value
	 */
	private double getFirstVertexCoords(double[] resArr) {
		Vertex vrI = vertexesV.get(0);
		for (int i = 0; i < nD; i++) {
			resArr[i] = vrI.getCoords()[i];
		}
		return vrI.getScore();
	}


	/**
	 *  Description of the Method
	 */
	private void printSimplex() {
		System.out.println("----simplex----");
		for (int iv = 0; iv <= nD; iv++) {
			Vertex vr = vertexesV.get(iv);
			String str = "n=" + iv + " ";
			for (int i = 0; i < nD; i++) {
				str = str + " i=" + i + " coor=" + vr.getCoords()[i] + "  ";
			}
			str = str + "  score=" + vr.getScore();
			System.out.println(str);
		}
		System.out.println("----simplex end----");
	}


	/**
	 *  Description of the Method
	 *
	 *@param  vr    Description of the Parameter
	 *@param  name  Description of the Parameter
	 */
	private void printVertex(Vertex vr, String name) {
		System.out.println("----vertex---start---- name=" + name);
		String str = " ";
		for (int i = 0; i < nD; i++) {
			str = str + " i=" + i + " coor=" + vr.getCoords()[i] + "  ";
		}
		str = str + "  score=" + vr.getScore();
		System.out.println(str);
		System.out.println("----vertex---end------ name=" + name);
	}


	//----------------------------------------------
	//public methods specific for simplex algorithm
	//----------------------------------------------

	/**
	 *  Sets the reflection factor for this instance of the simplex algorithm.
	 *
	 *@param  rho  The new reflection value
	 */
	public void setReflection(double rho) {
		this.rho = rho;
	}


	/**
	 *  Sets the expansion factor for this instance of the simplex algorithm.
	 *
	 *@param  chi  The new expansion value
	 */
	public void setExpansion(double chi) {
		this.chi = chi;
	}


	/**
	 *  Sets the contraction factor for this instance of the simplex algorithm.
	 *
	 *@param  gamma  The new contraction value
	 */
	public void setContraction(double gamma) {
		this.gamma = gamma;
	}


	/**
	 *  Sets the shinkage factor for this instance of the simplex algorithm.
	 *
	 *@param  sigma  The new shinkage value
	 */
	public void setShinkage(double sigma) {
		this.sigma = sigma;
	}


	/**
	 *  Returns the reflection factor for this instance of the simplex algorithm.
	 *
	 *@return    The reflection value
	 */
	public double getReflection() {
		return rho;
	}


	/**
	 *  Returns the expansion factor for this instance of the simplex algorithm.
	 *
	 *@return    The expansion value
	 */
	public double getExpansion() {
		return chi;
	}


	/**
	 *  Returns the contraction factor for this instance of the simplex algorithm.
	 *
	 *@return    The contraction value
	 */
	public double getContraction() {
		return gamma;
	}


	/**
	 *  Returnss the shinkage factor for this instance of the simplex algorithm.
	 *
	 *@return    The shinkage value
	 */
	public double getShinkage() {
		return sigma;
	}


	/**
	 *  Sets the maximal number of shrinkage for this instance of the simplex
	 *  algorithm.
	 *
	 *@param  nShrinkMax  The new maximal number of shrinkage
	 */
	public void setShrinkageMax(int nShrinkMax) {
		this.nShrinkMax = nShrinkMax;
	}


	/**
	 *  Returns the maximal number of shrinkage for this instance of the simplex
	 *  algorithm.
	 *
	 *@return    The maximal number of shrinkage
	 */
	public int getShrinkageMax() {
		return nShrinkMax;
	}


	/**
	 *  This class describes a vertex in the simplex
	 *
	 *@author    shishlo
	 */
	class Vertex {

		private double[] coords = null;

		private int nDim = 0;

		private double score = Double.MAX_VALUE;

		/**
		 *  the problem to solve
		 */
		private Problem _problem = null;


		/**
		 *  Constructor for the Vertex object
		 */
		protected Vertex() {
			_problem = null;
			nDim = 0;
			coords = new double[nDim];
		}


		/**
		 *  Sets the problem
		 *
		 *@param  problem  The problem to solve
		 */
		protected void setProblem(Problem problem) {
			if (problem != null) {
				_problem = problem;
				nDim = problem.getVariables().size();
				coords = new double[nDim];
			}
		}



		/**
		 *  Sets the score value for this vertex
		 *
		 *@return               Description of the Return Value
		 */
		protected boolean findScore(SearchAlgorithm algorithm) {
			score = Double.MAX_VALUE;
			if (_problem == null) {
				return false;
			}
			int n = _problem.getVariables().size();
			if (n == nDim) {

				final List<Variable> variables = _problem.getVariables();
				MutableTrialPoint trialPoint = new MutableTrialPoint(variables.size());

				int i = 0;
				for ( final Variable variable : variables ) {
					double value = coords[i];
					if(variable.getLowerLimit() > value || variable.getUpperLimit() < value){
						return false;
					}
					trialPoint.setValue(variable, value);
					i++;
				}
				double satisfaction = 0.;

				try {
					Trial trial = algorithm.evaluateTrialPoint(trialPoint.getTrialPoint());
					satisfaction = trial.getSatisfaction();
				} catch (RunTerminationException exept) {
				}

				if (satisfaction > 0.) {
					score = 1. / satisfaction;
					return true;
				} else {
					if (satisfaction == 0.) {
						score = Double.MAX_VALUE;
						return true;
					} else {
						return false;
					}
				}
			} else {
				return false;
			}
		}


		/**
		 *  Returns the score value
		 *
		 *@return    The score value
		 */
		protected double getScore() {
			return score;
		}


		/**
		 *  Sets the score for this vertex
		 *
		 *@param  score  The new score value
		 */
		protected void setScore(double score) {
			this.score = score;
		}


		/**
		 *  Returns the array of coordinates
		 *
		 *@return    The coords value
		 */
		protected double[] getCoords() {
			return coords;
		}


		/**
		 *  Sets the coords attribute of the Vertex object
		 *
		 *@param  coordsIn  The new coords value
		 */
		protected void setCoords(double[] coordsIn) {
			if (coordsIn.length != nDim) {
				nDim = coordsIn.length;
				coords = new double[nDim];
			}
			for (int i = 0; i < nDim; i++) {
				coords[i] = coordsIn[i];
			}
		}


		/**
		 *  Returns the number of dimensions
		 *
		 *@return    The dim value
		 */
		protected int getDim() {
			return nDim;
		}

	}


	/**
	 *  This is the vertex comparator
	 *
	 *@author    shishlo
	 */
	class CompareVertex implements Comparator<Vertex> {

		/**
		 *  Compare two vertexes
		 *
		 *@param  obj1  The first vertex
		 *@param  obj2  The second vertex
		 *@return       result of comparison
		 */
		public int compare( final Vertex obj1, final Vertex obj2 ) {
			if ( obj1.getScore() > obj2.getScore() ) {
				return 1;
			}
			else if ( obj1.getScore() < obj2.getScore() ) {
				return -1;
			}
			return 0;
		}
	}

}


