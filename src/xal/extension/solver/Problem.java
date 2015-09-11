/*
 *  Problem.java
 *
 *  Created Wednesday June 9, 2004 2:30pm
 *
 *  Copyright 2003, Spallation Neutron Source
 *  Oak Ridge National Laboratory
 *  Oak Ridge, TN 37830
 */
package xal.extension.solver;

import xal.extension.solver.constraint.Constraint;
import xal.extension.solver.hint.Hint;

import java.util.*;

/**
 * Problem is the primary class for holding the user's problem information.
 *
 * @author   ky6
 * @author   t6p
 */
public class Problem {
	/** The objectives we are trying to optimize. */
	protected List<Objective> _objectives;
	
	/** The variables which identify the controls for optimizing the solution. */
	protected final List<Variable> _variables;
	
	/** Constraints which identify unacceptable solutions. */
	protected List<Constraint> _constraints;
	
	/** A table of hints the algorithms may use to adjust their search. */
	protected Map<String, Hint> _hints;
	
	/** The user provided object which evaluates the trial solutions. */
	protected Evaluator _evaluator;
	
	/** A table of reference values keyed by variable */
	protected Map<Variable,ValueRef> _valueRefs;
	
	
	/**
	 * Construct a problem using an objective list, variable list, constraint list, hint list, and an evaluator.
	 */
	public Problem( final List<Objective> objectives, final List<Variable> variables, final Evaluator evaluator, final List<Constraint> constraints, final List<Hint> hints ) {
		_valueRefs = new HashMap<Variable,ValueRef>();
		_variables = new ArrayList<Variable>();
		_hints = new HashMap<String,Hint>();

		setObjectives( objectives );
		setVariables( variables );
		setEvaluator( evaluator );
		setConstraints( constraints );
		setHints( hints );
	}


	/**
	 * Construct a problem using an objective list, variable list, and an evaluator.
	 */
	public Problem( final List<Objective> objectives, final List<Variable> variables, final Evaluator evaluator ) {
		this( objectives, variables, evaluator, new ArrayList<Constraint>(), new ArrayList<Hint>() );
	}


	/** Construct a problem where everything is null. */
	public Problem() {
		this( new ArrayList<Objective>(), new ArrayList<Variable>(), null );
	}


	/**
	 * Adds an Objective to objectiveList.
	 * @param anObjective  The objective of the problem.
	 */
	public void addObjective( Objective anObjective ) {
		_objectives.add( anObjective );
	}


	/**
	 * Set the objectives.
	 * @param objectives  The new objectives value
	 */
	public void setObjectives( final List<? extends Objective> objectives ) {
		_objectives = new ArrayList<Objective>( objectives.size() );
		for ( Objective objective : objectives ) {
			addObjective( objective );
		}
	}


	/**
	 * Get the list of objectives.
	 * @return   objectiveList.
	 */
	public List<Objective> getObjectives() {
		return _objectives;
	}


	/**
	 * Adds a Variable object to variableList.
	 * @param variable   The feature to be added to the Variable attribute
	 */
	public void addVariable( final Variable variable ) {
		_variables.add( variable );
		_valueRefs.put( variable, new ValueRef() );
	}


	/**
	 * Set the variables.
	 * @param variables  The new variables value
	 */
	public void setVariables( final List<Variable> variables ) {
		_variables.clear();

		for ( Variable variable : variables ) {
			addVariable( variable );
		}
	}


	/**
	 * Get the value reference which is mapped to a variable.
	 * @param variable   Description of the Parameter
	 * @return           A double representing the value.
	 */
	public ValueRef getValueReference( final Variable variable ) {
		return _valueRefs.get( variable );
	}


	/**
	 * Get the list of variables.
	 * @return   The list of variables.
	 */
	public final List<Variable> getVariables() {
		return _variables;
	}


	/**
	 * Add a Constraint object to constraintList.
	 * @param aConstraint  One constraint of the problem.
	 */
	public void addConstraint( final Constraint aConstraint ) {
		_constraints.add( aConstraint );
	}


	/**
	 * Set the constraints.
	 * @param constraints  The new constraints value
	 */
	public void setConstraints( final List<Constraint> constraints ) {
		_constraints = new ArrayList<Constraint>( constraints.size() );
		for ( Constraint constraint : constraints ) {
			addConstraint( constraint );
		}
	}


	/**
	 * Get the list of constraints.
	 * @return   constraintList The list of constraints.
	 */
	public List<Constraint> getConstraints() {
		return _constraints;
	}


	/**
	 * Add a Hint object to the hintList.
	 * @param aHint  One hint for the problem.
	 */
	public void addHint( final Hint aHint ) {
		_hints.put( aHint.getType(), aHint );
	}
	
	
	/**
	 * Add to this problem, the list of hints.
	 * @param hints the list of hints to add to this problem.
	 */
	public void addHints( final List<Hint> hints ) {
		for ( Hint hint : hints ) {
			addHint( hint );
		}
	}
	

	/**
	 * Set this problem's hints to the ones specified.
	 * @param hints  The new hints to set for this problem.
	 */
	public void setHints( final List<Hint> hints ) {
		_hints.clear();
		addHints( hints );
	}


	/**
	 * Get the hint corresponding to the specified type.
	 * @param type the type identifier for which to fetch a hint.
	 * @return The hint corresponding to the specified type.
	 */
	public Hint getHint( final String type ) {
		return _hints.get( type );
	}


	/**
	 * Set the evaluator to use in scoring the trial points. The evaluator defines the problem we
	 * are trying to optimize.
	 * @param anEvaluator  The new evaluator value
	 */
	public void setEvaluator( final Evaluator anEvaluator ) {
		_evaluator = anEvaluator;
	}


	/**
	 * Get the evaluator.
	 * @return   The evaluator.
	 */
	public Evaluator getEvaluator() {
		return _evaluator;
	}
	
	
	/**
	 * Generate a trial for the initial variable values without any associated algorithm
	 * @return a trial without any associated algorithm
	 */
	private Trial generateInitialTrial() {
		return new Trial( this, generateInitialTrialPoint() );
	}
	
	
	/**
	 * Generate a new trial point based on the initial values of the variables.
	 * @return a new trial point
	 */
	public TrialPoint generateInitialTrialPoint() {
		final MutableTrialPoint trialPoint = new MutableTrialPoint( _variables.size() );
		
		for ( final Variable variable : _variables ) {
			final double value = variable.getInitialValue();
			trialPoint.setValue( variable, value );
		}			 
		
		return trialPoint.getTrialPoint();
	}
	
	
	/**
	 * Validate the trial.
	 * @param trial  The trial to be validated.
	 * @return A trial veto if any of the constraints vetos the trial or null if there is no veto.
	 */
	protected TrialVeto validate( final Trial trial ) {
		updateValueReference( trial );
		for ( final Constraint constraint : _constraints ) {
			final TrialVeto veto = constraint.validate( trial );
			if ( veto != null ) {
				return veto;
			}
		}
		return null;
	}
	
	
	/**
	 * Evaluate the initial point as specified by the initial values of the variables
	 * @return the evaluated trial
	 */
	public Trial evaluateInitialPoint() {
		final Trial trial = generateInitialTrial();
		evaluate( trial );
		return trial;
	}
	
	
	/**
	 * Evaluate the trial
	 * @param trial the trial to evaluate
	 * @return true if the evaluation was successful and false if it was vetoed
	 */
	public boolean evaluate( final Trial trial ) {
		final TrialVeto veto = validate( trial );
		if ( veto != null ) {
			trial.vetoTrial( veto );
			return false;
		};
		_evaluator.evaluate( trial );
		return true;
	}
	
	
	/**
	 * Update the value reference to synchronize with the user's model.
	 * @param trial  the trial with which to update the reference
	 */
	private void updateValueReference( final Trial trial ) {
		for ( final Variable variable : _variables ) {
			final ValueRef referenceVariable = getValueReference( variable );
			referenceVariable.setValue( trial.getTrialPoint().getValue( variable ) );
		}
	}
}

