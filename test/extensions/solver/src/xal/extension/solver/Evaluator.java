/*
 *  Evaluator.java
 *
 *  Created Wednesday June 9, 2004 2:39 pm
 *
 *  Copyright 2003, Spallation Neutron Source
 *  Oak Ridge National Laboratory
 *  Oak Ridge, TN 37830
 */
package xal.extension.solver;

import java.util.*;

/**
 * Evaluator is an interface to a custom evaluator for a specific problem.
 *
 * @author   ky6
 */
public interface Evaluator {

	/**
	 * Score the trial.
	 *
	 * @param trial  The trial to evaluate.
	 */
	public void evaluate( Trial trial );
}

