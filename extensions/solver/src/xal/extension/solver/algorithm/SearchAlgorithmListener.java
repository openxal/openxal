/*
 * SearchAlgorithmListener.java
 *
 * Created Thursday July 8, 2004 3:15pm
 *
 * Copyright 2003, Spallation Neutron Source
 * Oak Ridge National Laboratory
 * Oak Ridge, TN 37830
 */
  
 package xal.extension.solver.algorithm;
 
 import java.util.*;

 /**
 * The interface implemented by listeners of search algorithm events.
 *
 * @author  ky6
 */
 public interface SearchAlgorithmListener {
	 
	 /**
	 * Send a message that an algorithm is available.
	 * @param source The source of the available algorithm.
	 */
	 public void algorithmAvailable( SearchAlgorithm source );
	 
	 
	 /**
	 * Send a message that an algorithm is not available.
	 * @param source The source of the available algorithm.
	 */
	 public void algorithmUnavailable( SearchAlgorithm source );
 }
