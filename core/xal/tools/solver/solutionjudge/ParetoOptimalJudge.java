/*
 * ParetoOptimalJudge.java
 *
 * Created Wednesday June 30 2004 3:02pm
 *
 * Copyright 2003, Spallation Neutron Source
 * Oak Ridge National Laboratory
 * Oak Ridge, TN 37830
 */
 
 package xal.tools.solver.solutionjudge;
  
 import xal.tools.messaging.MessageCenter;
  
 import xal.tools.solver.Objective;
 import xal.tools.solver.Trial;
 
 import java.util.*;
 
 /**
 * ParetoOptimalJudge is a subclass of SolutionJudge.  A solution is 
 * pareto optimal if one cannot improve an objective's satisfaction
 * without lowering the satisfaction of another objective.
 *
 * @author ky6
 * @author t6p
 */
 public class ParetoOptimalJudge extends SolutionJudge {
	 protected List _optimalSolution;
	 
	 
	 /** Reset the pareto optimal judge. */
	 public void reset() {
		 _optimalSolution.clear();
	 }
	 
	 	 
	 /**
	 * Get the optimal solutions.
	 * @return The optimal solutions as a List.
	 */
	 public List getOptimalSolutions() {
		 return _optimalSolution;
	 }
		 
			 
	 /**
	 * Test to see if the first solution/trial is better than the second solution/trial for the specified objective.  Return true if the
	 * first objective's satisfaction is better than the second objective's satisfaction.
	 * @param firstSolution The solution being tested.
	 * @param secondSolution The solution the first solution is tested against.
	 * @return True if the first solution is better than the second solution.
	 */
	 private boolean isBetter( final Trial firstSolution, final Trial secondSolution, final List objectives ) {			 
		 final Iterator objectiveIter = objectives.iterator();		 
		 while( objectiveIter.hasNext() ) {
			 final Objective objective = (Objective)objectiveIter.next();
			 final double firstSatisfaction = firstSolution.getScore( objective ).getSatisfaction();
			 final double secondSatisfaction = secondSolution.getScore( objective ).getSatisfaction();
			 if( firstSatisfaction >= secondSatisfaction ) {
				 return true;
			 }
		 }
		 return false;
	 }
	 
	 
	 /**
	 * Judge the trial.
	 * @param solution The new solution to
	 * update the pareto optimal judge with. 
	 */
	 public void judge( final Trial solution ) {
		 final Iterator optimalSolutionIter = _optimalSolution.iterator();
		 final List objectives = solution.getProblem().getObjectives();
		 boolean foundOptimal = false;
		 
		 /**Determine if the new solution is optimal*/
		 while( optimalSolutionIter.hasNext() ) {
			 final Trial optimalSolution = (Trial)optimalSolutionIter.next();
			 if( isBetter( solution, optimalSolution, objectives ) ) {
				 foundOptimal = true;
				 break;
			 }
		 }
		 
		 /**Determine if an existing optimal solutions should be removed*/
		 if( foundOptimal ) {
			 while( optimalSolutionIter.hasNext() ) {
				 final Trial optimalSolution = (Trial)optimalSolutionIter.next();
				 if( !isBetter( optimalSolution, solution, objectives ) ) {
					 _optimalSolution.remove( optimalSolution );
				 }
			 }
			 _optimalSolution.add( solution );
			 _eventProxy.foundNewOptimalSolution( this, _optimalSolution, solution );
		 }
	 }
 }
