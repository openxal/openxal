/*
 * AlgorithmPoolListener.java
 *
 * Created Monday July 12, 2004 12:03pm
 *
 * Copyright 2003, Spallation Neutron Source
 * Oak Ridge National Laboratory
 * Oak Ridge, TN 37830
 */
  
 package xal.extension.solver;
 
 import xal.extension.solver.algorithm.SearchAlgorithm;

 import java.util.*;

 /**
 * AlgorithmPool keeps track of the available algorithms.
 *
 * @author  ky6
 */
 public interface AlgorithmPoolListener {
	 
	 /**
	 * Send a message that an algorithm. was added to the
	 * pool.
	 * @param source The source of the added algorithm.
	 * @param algorithm The added algorithm.
	 */
	 public void algorithmAdded(AlgorithmPool source,  SearchAlgorithm algorithm);
	 
	 
	 /**
	 * Send a message that an algorithm was removed from the
	 * pool.
	 * @param source The source of the removed algorithm.
	 * @param algorithm The removed algorithm.
	 */
	 public void algorithmRemoved(AlgorithmPool source, SearchAlgorithm algorithm);
	 
	 
	 /**
	 * Send a message that an algorithm is available.
	 * @param source The source of the available algorithm.
	 * @param algorithm The algorithm made available.
	 */
	 public void algorithmAvailable(AlgorithmPool source, SearchAlgorithm algorithm);
	 
	 
	 /**
	 * Send a message that an algorithm is unvavailable.
	 * @param source The source of the unavailable algorithm.
	 * @param algorithm which is unavailable.
	 */
	 public void algorithmUnavailable(AlgorithmPool source, SearchAlgorithm algorithm);
 }
