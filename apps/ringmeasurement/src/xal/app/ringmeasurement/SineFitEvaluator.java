/*
 * SineFitEveluator.java
 *
 * Created on February 17, 2005, 2:13 PM
 */

package xal.app.ringmeasurement;

import java.util.*;

import xal.extension.solver.*;

/**
 *
 * @author  Paul Chu
 */
public class SineFitEvaluator implements Evaluator {
    
    /** Creates a new instance of SineFitEveluator */
    public SineFitEvaluator() {
    }
    
    public void evaluate(Trial trial) {
        final Map<String, Double> inputs = new HashMap<String, Double>( trial.getProblem().getVariables().size() );

        final Iterator<Variable> variableIter = trial.getProblem().getVariables().iterator();
        while ( variableIter.hasNext() ) {
                final Variable variable = variableIter.next();
                final double value = trial.getTrialPoint().getValue( variable );
                inputs.put( variable.getName(), new Double( value ) );
        }

        final Iterator<Objective> objectiveIter = trial.getProblem().getObjectives().iterator();
        while ( objectiveIter.hasNext() ) {
                SineFitObjective objective = (SineFitObjective)objectiveIter.next();
                double score = objective.score( inputs );
                trial.setScore( objective, score );
        }
    }
    
}
