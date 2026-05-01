/*
 * SolutionJudgeListener.java
 *
 * Created Tuesday June 29 2004 12:29pm
 *
 * Copyright 2003, Spallation Neutron Source
 * Oak Ridge National Laboratory
 * Oak Ridge, TN 37830
 */
 
 package xal.extension.solver.solutionjudge;
 
 import xal.extension.solver.Trial;
 
 import java.util.*;
 
 /**
 * The interface implemented by listeners of solution judge events.
 *
 * @author ky6  
 */
 public interface SolutionJudgeListener {
	 /**
	 * Handle a message that a new optimal solution has been found.
	 * @param source The source of the new optimal solution.
	 * @param solutions The list of solutions.
	 * @param solution The new optimal solution.
	 */
	 public void foundNewOptimalSolution( SolutionJudge source, List<Trial> solutions, Trial solution );
 }
