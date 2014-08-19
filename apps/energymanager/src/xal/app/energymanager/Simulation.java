//
//  Simulation.java
//  xal
//
//  Created by Thomas Pelaia on 6/14/05.
//  Copyright 2005 Oak Ridge National Lab. All rights reserved.
//


package xal.app.energymanager;

import java.util.Iterator;
import java.util.List;

import xal.model.probe.Probe;
import xal.model.probe.traj.ProbeState;
import xal.model.probe.traj.Trajectory;
import xal.smf.AcceleratorNode;
import xal.tools.beam.PhaseMatrix.IND;
import xal.tools.beam.PhaseVector;
import xal.tools.beam.Twiss;
import xal.tools.beam.calc.ISimulationResults.ISimEnvResults;
import xal.tools.beam.calc.SimpleSimResultsAdaptor;


/** 
 * <p>
 * A simulation result.
 * </p>
 * <p>
 * <h4>CKA NOTES:</h4>
 * - The probe states had been cast to <code>EnvelopeProbeState</code> when I took over
 * the refactoring of this class.  Originally they were treated as <code>PhaseState</code>
 * (or <code>IPhaseState</code>) interfaces.
 * <br/>
 * <br/>
 * - Some methods calling <code>{@link #getStates()}</code> still expected the 
 * <code>IPhaseState</code> interface.
 * <br/>
 * <br/>
 * - In practice the probe states are either <code>TransferMapState</code> objects or
 * <code>EnvelopeProbeState</code> objects, depending upon the simulation type 
 * (i.e., the <code>Probe</code> type).
 * <br/>
 * <br/>
 * - I have refactored this class to make the simulation data processing depending upon
 * the type of simulation data offered (transfer maps or envelope data).
 * </p>
 * 
 * @author Thomas Pelaia
 * @author Christopher K. Allen
 * @since  6/14/05.
 * @version Nov 7, 2013
 */
public class Simulation {
    
    /*
     * Global Constants
     */
    
    /** conversion factor to convert eV to MeV */
    final static private double CONVERT_EV_TO_MEV = 1.0e-6;
    
//	/** index of the X coordinate result */
//	static final public int X_INDEX = IPhaseState.X;
//	
//	/** index of the Y coordinate result */
//	static final public int Y_INDEX = IPhaseState.Y;
//	
//	/** index of the Z coordinate result */
//	static final public int Z_INDEX = IPhaseState.Z;
    /** index of the X coordinate result */
    static final public int X_INDEX = 0;
    
    /** index of the Y coordinate result */
    static final public int Y_INDEX = 1;
    
    /** index of the Z coordinate result */
    static final public int Z_INDEX = 2;
	
	
	/*
	 * Local Attributes
	 */
	
	/** Evaluation nodes */
	final protected List<AcceleratorNode> _evaluationNodes;
	
	/** trajectory */
	final protected Trajectory<?> _trajectory;
	
	/** kinetic energy */
	final protected double _outputKineticEnergy;
	
	/** beta error indexed by axis and position */
	protected double[][] _percentBetaError;
	
	/** worst Beta error */
	protected double[] _worstBetaError;
	
	/** mean Beta error */
	protected double[] _meanBetaError;
	
	/** Beta min */
	protected double[] _betaMin;
	
	/** Beta max */
	protected double[] _betaMax;
	
	/** chromatic dispersion error indexed by axis and position */
	protected double[][] _percentEtaError;
	
	/** worst chromatic dispersion error */
	protected double[] _worstEtaError;
	
	/** mean chromatic dispersion error */
	protected double[] _meanEtaError;
	
	/** chromatic dispersion min */
	protected double[] _etaMin;
	
	/** chromatic dispersion max */
	protected double[] _etaMax;
	
	/** positions in meters */
	protected double[] _positions;
	
	/** the phase states */
	protected ProbeState<?>[] _states;
	
	/** element IDs */
	protected String[] _evaluationElementIDs;
	
	/** node IDs */
	protected String[] _evaluationNodeIDs;
	
	/** alpha array where first index is the axis index and the second index is the position index */
	protected double[][] _alpha;
	
	/** beta array where first index is the axis index and the second index is the position index */
	protected double[][] _beta;
	
	/** emittance array where first index is the axis index and the second index is the position index */
	protected double[][] _emittance;
	
	/** chromatic dispersion array where the first index is the axis index and the second index is the position index */
	protected double[][] _eta;
	
	/** output kinetic energy (MeV) array as a function of each element's position */
	protected double[] _kineticEnergy;
    
    /** rest energy (MeV) array as a function of each element's position */
    private double[] _restEnergy;
    
    /** species charge (positive electron units) array as a function of each element's position */
    private double[] _speciesCharge;
	
	/** the state finder for finding the state that matches a specified node ID */
	protected StateFinder _stateFinder;
	
	
	/** The machine parameter calculation engine */
    private ISimEnvResults<ProbeState<?>>     _modelStatesAdaptor;
	
    
    /*
     * Initialization 
     */
    
	/** 
	 * Constructor 
	 * 
	 * @throws IllegalArgumentException    the probe is not a recognized type
	 */
	public Simulation( final Probe<?> probe, final List<AcceleratorNode> evaluationNodes ) 
	        throws IllegalArgumentException
	{
		_evaluationNodes = evaluationNodes;
		_trajectory = probe.getTrajectory();
		
		_outputKineticEnergy = CONVERT_EV_TO_MEV * probe.getKineticEnergy();
		
		_stateFinder = newFirstStateFinder();
		
		// Create the machine parameter calculation engine according to the
		//    type of probe we are given
		_modelStatesAdaptor = new SimpleSimResultsAdaptor(_trajectory);
		
//        if (probe instanceof TransferMapProbe) {
//            TransferMapTrajectory   traj = (TransferMapTrajectory)probe.getTrajectory();
//
//            this.cmpMchParams = new CalculationsOnRings(traj);
//            
//        } else if (probe instanceof EnvelopeProbe) {
//            EnvelopeTrajectory traj = (EnvelopeTrajectory)probe.getTrajectory();
//            
//            this.cmpMchParams = new CalculationsOnBeams(traj);
//            
//        } else {
//            
//            throw new IllegalArgumentException("Unknown probe type " + probe.getClass().getName());
//        }
		
	}


	/*
	 * Object Overrides
	 */
	
	/** Provide a string representation of the global parameters of this Simulation */
	@Override
    public String toString() {
		final StringBuffer buffer = new StringBuffer();
		buffer.append( "Output Kinetic Energy: " + getOutputKineticEnergy() );
		buffer.append( ", Beta Min: [ " + getBetaMin()[X_INDEX] + ", " + getBetaMin()[Y_INDEX] + " ]" );
		buffer.append( ", Beta Max: [ " + getBetaMax()[X_INDEX] + ", " + getBetaMax()[Y_INDEX] + " ]" );
		buffer.append( ", Eta Min: [ " + getEtaMin()[X_INDEX] + ", " + getEtaMin()[Y_INDEX] + " ]" );
		buffer.append( ", Eta Max: [ " + getEtaMax()[X_INDEX] + ", " + getEtaMax()[Y_INDEX] + " ]" );
		return buffer.toString();
	}
	
	
	/**
	 * Get the output kinetic energy.
	 * @return the output kinetic energy in MeV.
	 */
	public double getOutputKineticEnergy() {
		return _outputKineticEnergy;
	}
	
	
	/**
	 * Get the trajectory.
	 * @return the trajectory.
	 */
	public Trajectory<?> getTrajectory() {
		return _trajectory;
	}
	
	
	/**
	 * Get the array of beta minimum values.
	 * @return the array of beta minimum values.
	 */
	public double[] getBetaMin() {
		if ( _betaMin == null ) {
			findBetaExtrema();
		}
		
		return _betaMin;
	}
	
	
	/**
	 * Get the array of beta maximum values.
	 * @return the array of beta maximum values.
	 */
	public double[] getBetaMax() {
		if ( _betaMax == null ) {
			findBetaExtrema();
		}
		
		return _betaMax;
	}
	
	
	/**
	 * Get the array of beta over the position indices.
	 * @return beta array where first index is the axis index (X, Y, Z) and the second index is the position index
	 */
	public double[][] getBeta() {
		if ( _beta == null ) {
			populateBeta();
		}
		
		return _beta;
	}
	
	
	/**
	 * Get the array of alpha over the position indices.
	 * @return alpha array where first index is the axis index (X, Y, Z) and the second index is the position index
	 */
	public double[][] getAlpha() {
		if ( _alpha == null ) {
			populateAlpha();
		}
		
		return _alpha;
	}
	
	
	/**
	 * Get the array of emittance over the position indices.
	 * @return emittance array where first index is the axis index (X, Y, Z) and the second index is the position index
	 */
	public double[][] getEmittance() {
		if ( _emittance == null ) {
			populateEmittance();
		}
		
		return _emittance;
	}
	
	
	/**
	 * Get the array of chromatic dispersion minimum values.
	 * @return the array of chromatic dispersion minimum values.
	 */
	public double[] getEtaMin() {
		if ( _etaMin == null ) {
			findChromaticDispersionExtrema();
		}
		
		return _etaMin;
	}
	
	
	/**
	 * Get the array of chromatic dispersion maximum values.
	 * @return the array of chromatic dispersion maximum values.
	 */
	public double[] getEtaMax() {
		if ( _etaMax == null ) {
			findChromaticDispersionExtrema();
		}
		
		return _etaMax;
	}
	
	
	/**
	 * Get the array of chromatic dispersion over the position indices.
	 * @return chromatic dispersion array where first index is the axis index (X, Y) and the second index is the position index
	 */
	public double[][] getEta() {
		if ( _eta == null ) {
			populateChromaticDispersion();
		}
		
		return _eta;
	}
	
	
	/**
	 * Get the beta error (percent discrepancy) relative to a base simulation
	 */
	public double[][] getPercentBetaError( final Simulation base ) {
		if ( _percentBetaError == null ) {
			populateBetaErrors( base );
		}
		
		return _percentBetaError;
	}
	
	
	/**
	 * Get the mean beta error (fractional discrepancy) relative to a base simulation
	 */
	public double[] getMeanBetaError( final Simulation base ) {
		if ( _meanBetaError == null ) {
			populateBetaErrors( base );
		}
		
		return _meanBetaError;
	}
	
	
	/**
	 * Get the array of worst beta errors (fractional discrepancy) relative to the base simulation.
	 */
	public double[] getWorstBetaError( final Simulation base ) {
		if ( _worstBetaError == null ) {
			populateBetaErrors( base );
		}
		
		return _worstBetaError;
	}
	
	
	/**
	 * Get the kinetic energy array corresponding to the evaluation nodes.
	 * @return the kinetic energy array in MeV
	 */
	public double[] getKineticEnergy() {
		if ( _kineticEnergy == null ) {
			populateChargeAndEnergy();
		}
		
		return _kineticEnergy;
	}
    
    
	/**
	 * Get the rest energy array corresponding to the evaluation nodes.
	 * @return rest energy array in MeV
	 */
    public double[] getRestEnergy() {
        if ( _restEnergy == null ) {
            populateChargeAndEnergy();
        }
        
        return _restEnergy;
    }
    
    
	/**
	 * Get the species charge array corresponding to the evaluation nodes.
	 * @return species charge array in units of positive electon charge
	 */
    public double[] getSpeciesCharge() {
        if ( _speciesCharge == null ) {
            populateChargeAndEnergy();
        }
        
        return _speciesCharge;
    }
	
	
	/**
	 * Get the array of positions corresponding to the evaluation nodes.
	 * @return the array of positions in meters.
	 */
	public double[] getPositions() {
		if ( _positions == null ) {
			populatePositions();
		}
		
		return _positions;
	}
	
	
	/**
	 * Get the phase states array corresponding to the evaluation nodes.
	 * @return the array of phase states
	 */
	public ProbeState<?>[] getStates() {
    	if ( _states == null ) {
			populateStates();
		}
		
		return _states;
	}
	
	
	/**
	 * Get the array of evaluation node IDs with a correspondence to the position array.
	 * @return the array of evaluation node IDs
	 */
	public String[] getEvaluationNodeIDs() {
		if ( _evaluationNodeIDs == null ) {
			populateEvaluationElementAndNodeIDs();
		}
		
		return _evaluationNodeIDs;
	}
	
	
	/**
	 * Get the array of evaluation element IDs with a correspondence to the position array.
	 * @return the array of evaluation element IDs
	 */
	public String[] getEvaluationElementIDs() {
		if ( _evaluationElementIDs == null ) {
			populateEvaluationElementAndNodeIDs();
		}
		
		return _evaluationElementIDs;
	}
	
	
	/** populate positions */
	protected void populatePositions() {
		final ProbeState<?>[] states = getStates();
		final double[] positions = new double[ states.length ];
		
		for ( int index = 0 ; index < states.length ; index++ ) {
			positions[ index ] = states[ index ].getPosition();
		}
		
		_positions = positions;
	}
	
	
	/** populate evaluation element and node IDs */
	protected void populateEvaluationElementAndNodeIDs() {
		final ProbeState<?>[] states = getStates();
        //System.out.println( "State count: " + states.length );
		final int numNodes = _evaluationNodes.size();
		
		_evaluationNodeIDs = new String[ numNodes ];
		_evaluationElementIDs = new String[ numNodes ];
		
		for ( int index = 0 ; index < numNodes ; index++ ) {
            final ProbeState<?> state = states[index];
			_evaluationNodeIDs[ index ] = _evaluationNodes.get( index ).getId();
            //System.out.println( "index: " + index + ", node: " + _evaluationNodeIDs[ index ] + ", state: " + state );
            if ( state != null ) {
                _evaluationElementIDs[ index ] = states[ index ].getElementId();
            }
		}
	}
	
	
	/** populate alpha */
	protected void populateAlpha() {
		final ProbeState<?>[] states = getStates();
		final double[][] alpha = new double[ Z_INDEX + 1 ][ states.length ];
		
		for ( int index = 0 ; index < states.length ; index++ ) {
		    ProbeState<?>    state = states[index];
		    
		    
//			final Twiss[] twiss = states[ index ].getTwiss();
            final Twiss[] twiss = _modelStatesAdaptor.computeTwissParameters(state);
		    
			for ( int axis = 0 ; axis <= Z_INDEX ; axis++ ) {
				alpha[ axis ][ index ] = twiss[ axis ].getAlpha();
			}			
		}
		
		_alpha = alpha;
	}
	
	
	/** populate Beta */
	protected void populateBeta() {
		final ProbeState<?>[] states = getStates();
		final double[][] beta = new double[ Z_INDEX + 1 ][ states.length ];
		
		for ( int index = 0 ; index < states.length ; index++ ) {
//			final Twiss[] arrTwiss = states[ index ].getTwiss();
			ProbeState<?>    state    = states[ index ];
			final Twiss[] arrTwiss = _modelStatesAdaptor.computeTwissParameters(state);
			
			for ( int axis = 0 ; axis <= Z_INDEX ; axis++ ) {
				beta[ axis ][ index ] = arrTwiss[ axis ].getBeta();
			}			
		}
		
		_beta = beta;
	}
	
	
	/** populate emittance */
	protected void populateEmittance() {
		final ProbeState<?>[] states = getStates();
		final double[][] emittance = new double[ Z_INDEX + 1 ][ states.length ];
		
		for ( int index = 0 ; index < states.length ; index++ ) {
//			final Twiss[] twiss = states[ index ].getTwiss();
		    ProbeState<?>    state = states[ index ];
		    final Twiss[] twiss = _modelStatesAdaptor.computeTwissParameters(state);
		    
			for ( int axis = 0 ; axis <= Z_INDEX ; axis++ ) {
				emittance[ axis ][ index ] = twiss[ axis ].getEmittance();
			}			
		}
		
		_emittance = emittance;
	}
	
	
	/** populate chromatic dispersion */
	protected void populateChromaticDispersion() {
		final ProbeState<?>[] states = getStates();
		final double[][] eta = new double[ Z_INDEX + 1 ][ states.length ];
		
		for ( int index = 0 ; index < states.length ; index++ ) {
		    ProbeState<?>    state   = states[ index ];
		    PhaseVector   vecDisp = _modelStatesAdaptor.computeChromDispersion(state);
		    
		    eta[X_INDEX][index] = vecDisp.getElem(IND.X);
		    eta[Y_INDEX][index] = vecDisp.getElem(IND.Y);
            eta[Z_INDEX][index] = 0.0;
		    
//			eta[X_INDEX][index] = states[ index ].getChromDispersionX();
//			eta[Y_INDEX][index] = states[ index ].getChromDispersionY();
//			eta[Z_INDEX][index] = 0.0;
		}
		
		_eta = eta;
	}
	
	
	/** populate the kinetic energy array */
	private void populateChargeAndEnergy() {
//		final IPhaseState[] states = getStates();

	    final ProbeState<?>[] states = getStates();
		final double[] kineticEnergy = new double[ states.length ];
        final double[] restEnergy = new double[ states.length ];
        final double[] speciesCharge = new double[ states.length ];
		
		for ( int index = 0 ; index < states.length ; index++ ) {
//            final IPhaseState state = states[ index ];
            final ProbeState<?> state = states[ index ];
            
			kineticEnergy[ index  ] = CONVERT_EV_TO_MEV * state.getKineticEnergy();	// kinetic energy in MeV
            restEnergy[ index ] = CONVERT_EV_TO_MEV * state.getSpeciesRestEnergy();   // rest energy in MeV
            speciesCharge[ index ] = state.getSpeciesCharge();  // species charge in positive electron charge units
		}
		
		_kineticEnergy = kineticEnergy;
        _restEnergy = restEnergy;
        _speciesCharge = speciesCharge;
	}
	
	
	/** Calculate the Beta min/max values. */
	protected void findBetaExtrema() {
		final double[] minBeta = new double[] { Double.MAX_VALUE, Double.MAX_VALUE, Double.MAX_VALUE };
		final double[] maxBeta = new double[] { 0, 0, 0 };
		
		final ProbeState<?>[] states = getStates();
		
		for ( int index = 0 ; index < states.length ; index++ ) {
//			final Twiss[] twiss = states[ index ].getTwiss();
			final Twiss[] twiss = _modelStatesAdaptor.computeTwissParameters( states[index] );
			
			for ( int axis = 0 ; axis <= Z_INDEX ; axis++ ) {
				final double beta = twiss[ axis ].getBeta();
				if ( Double.isNaN( beta ) ) {
					minBeta[ axis ] = Double.NaN;
					maxBeta[ axis ] = Double.NaN;
					_betaMin = minBeta;
					_betaMax = maxBeta;
					return;
				}
				else {
					if ( beta < minBeta[ axis ] )  minBeta[ axis ] = beta;
					if ( beta > maxBeta[ axis ] )  maxBeta[ axis ] = beta;
				}
			}			
		}
		
		_betaMin = minBeta;
		_betaMax = maxBeta;
	}
	
	
	/** Calculate the chromatic dispersion min/max values. */
	protected void findChromaticDispersionExtrema() {
		final double[] minEta = new double[] { Double.MAX_VALUE, Double.MAX_VALUE, 0.0 };
		final double[] maxEta = new double[] { Double.MIN_VALUE, Double.MIN_VALUE, 0.0 };
		
		final double[][] dispersion = getEta();
		final ProbeState<?>[] states = getStates();
		
		for ( int index = 0 ; index < states.length ; index++ ) {
			for ( int axis = 0 ; axis < Z_INDEX ; axis++ ) {
				final double eta = dispersion[ axis ][ index ];
				if ( Double.isNaN( eta ) ) {
					minEta[ axis ] = Double.NaN;
					maxEta[ axis ] = Double.NaN;
					_etaMin = minEta;
					_etaMax = maxEta;
					return;
				}
				else {
					if ( eta < minEta[ axis ] )  minEta[ axis ] = eta;
					if ( eta > maxEta[ axis ] )  maxEta[ axis ] = eta;
				}
			}
		}
		
		_etaMin = minEta;
		_etaMax = maxEta;
	}
	
	
	/**
	 * Calculate the Beta errors (fractional discrepancy) relative to a base simulation.
	 * @param base the reference simulation
	 */
	protected void populateBetaErrors( final Simulation base ) {
		final double[] worstError = new double[ Z_INDEX + 1 ];
		final double[] meanError = new double[ Z_INDEX + 1 ];
		final double[][] percentBetaError = new double[ Z_INDEX + 1 ][];
		final double[][] beta = getBeta();
		final double[][] baseBeta = base.getBeta();
		
		for ( int axis = 0 ; axis <= Z_INDEX ; axis++ ) {
			percentBetaError[ axis ] = new double[ beta[axis].length ];
			for ( int index = 0 ; index < beta[axis].length ; index++ ) {
				final double error = Math.abs( beta[axis][index] - baseBeta[axis][index] ) / baseBeta[axis][index];
				percentBetaError[axis][index] = 100 * error;
				meanError[axis] += error;
				if ( error > worstError[axis] )  worstError[axis] = error;
			}
			meanError[axis] = ( beta[axis].length > 0 ) ? meanError[axis] / beta[axis].length : 0.0;
		}
		
		_percentBetaError = percentBetaError;
		_meanBetaError = meanError;
		_worstBetaError = worstError;
	}
	
	
	/** populate phase states */
	protected void populateStates() {
		_states = _stateFinder.findStates( _evaluationNodes, _trajectory );
	}
	
	
	
	/** Find the node's corresponding state from the state iterator */
	static protected interface StateFinder {
		/** find the trajectory's states corresponding to the specified nodes */
		public ProbeState<?>[] findStates( final List<AcceleratorNode> nodes, final Trajectory<?> trajectory );
	}
	
	
	/**
	 * Instantiate a state finder that finds the first state that matches the specified node.
	 * @return a new state finder
	 */
	static protected StateFinder newFirstStateFinder() {
		return new StateFinder() {
			@Override
            public ProbeState<?>[] findStates( final List<AcceleratorNode> nodes, final Trajectory<?> trajectory ) {
				final Iterator<? extends ProbeState<?>> stateIter = trajectory.stateIterator();
				final ProbeState<?>[] states = new ProbeState[ nodes.size() ];
				
				int index = 0;
				for ( final AcceleratorNode node : nodes ) {
					final String nodeID = node.getId();
                    //System.out.println( "Searching for node: " + nodeID );
					while ( stateIter.hasNext() ) {
						final ProbeState<?> state = stateIter.next();
                        //System.out.println( "Testing state: " + state.getElementId() );
						if ( state.getElementId().startsWith( nodeID ) ) {
							states[ index++ ] = state;
							break;
						}
					}
				}
				
				return states;
			}
		};
	}
}










