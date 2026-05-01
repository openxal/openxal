//
//  Scorer.java
//  xal
//
//  Created by Thomas Pelaia on 6/27/06.
//  Copyright 2006 Oak Ridge National Lab. All rights reserved.
//

package xal.extension.solver;

import java.util.List;


/** Score a trial. */
public interface Scorer {
	/**
	 * Score the trial.
	 * @param trial the trial to score
	 * @return the score for the trial
	 */
	public double score( final Trial trial, final List<Variable> variables );
}
