/*
 * ParetoOptimalJudge.java
 *
 * Created Wednesday June 30 2004 3:02pm
 *
 * Copyright 2003, Spallation Neutron Source
 * Oak Ridge National Laboratory
 * Oak Ridge, TN 37830
 */
 
 package xal.extension.solver.solutionjudge;
  
 import xal.tools.messaging.MessageCenter;
  
 import xal.extension.solver.Objective;
 import xal.extension.solver.Trial;
 
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
	 protected List<Trial> _optimalSolutions;
	 
	 
	 /** Reset the pareto optimal judge. */
	 public void reset() {
		 _optimalSolutions.clear();
	 }
	 
	 	 
	 /**
	 * Get the optimal solutions.
	 * @return The optimal solutions as a List.
	 */
	 public List<Trial> getOptimalSolutions() {
		 return _optimalSolutions;
	 }
		 
			 
	 /**
	 * Test to see if the first solution/trial is better than the second solution/trial for the specified objective.  Return true if the first objective's satisfaction is better than the second objective's satisfaction.
	 * @param firstSolution The solution being tested.
	 * @param secondSolution The solution the first solution is tested against.
	 * @return True if the first solution is better than the second solution.
	 */
	 private boolean isBetter( final Trial firstSolution, final Trial secondSolution, final List<Objective> objectives ) {
         for ( final Objective objective : objectives ) {
			 final double firstSatisfaction = firstSolution.getSatisfaction( objective );
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
		 if ( solution.isVetoed() ) {
			 solution.setSatisfaction( 0.0 );
		 }
		 else {
			 final Iterator<Trial> optimalSolutionIter = _optimalSolutions.iterator();
			 final List<Objective> objectives = solution.getProblem().getObjectives();
			 boolean foundOptimal = false;
			 
			 /**Determine if the new solution is optimal*/
			 while( optimalSolutionIter.hasNext() ) {
				 final Trial optimalSolution = optimalSolutionIter.next();
				 if( isBetter( solution, optimalSolution, objectives ) ) {
					 foundOptimal = true;
					 break;
				 }
			 }
			 
			 /**Determine if an existing optimal solutions should be removed*/
			 if( foundOptimal ) {
				 while( optimalSolutionIter.hasNext() ) {
					 final Trial optimalSolution = optimalSolutionIter.next();
					 if( !isBetter( optimalSolution, solution, objectives ) ) {
						 _optimalSolutions.remove( optimalSolution );
					 }
				 }
				 _optimalSolutions.add( solution );
				 _eventProxy.foundNewOptimalSolution( this, _optimalSolutions, solution );
			 }
		 }
	 }
 }
