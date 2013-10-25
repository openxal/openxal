/*
 * Stopper.java
 *
 * Created Monday June 14, 2004 9:37am
 *
 * Copyright 2003, Spallation Neutron Source
 * Oak Ridge National Laboratory
 * Oak Ridge, TN 37830
 */
   
 package xal.extension.solver; 
 
 import java.util.*;
 
 /**
 * Stopper is an interface for an object than can be used to stop a solver.
 *
 * @author  ky6
 */
 public interface Stopper {
	 
	 /**
	 * Signal whether the solver should stop.
	 * @param aSolver The solver the stopper was generated for.
	 * @return true to stop the solver and false to continue solving.
	 */
     public boolean shouldStop(Solver aSolver);
 }
