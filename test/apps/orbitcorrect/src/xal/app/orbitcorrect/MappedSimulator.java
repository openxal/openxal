/*
 * MappedSimulator.java
 *
 * Created on Mon Oct 18 10:01:18 EDT 2004
 *
 * Copyright (c) 2004 Spallation Neutron Source
 * Oak Ridge National Laboratory
 * Oak Ridge, TN 37830
 */

package xal.app.orbitcorrect;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import xal.smf.AcceleratorSeq;
import xal.tools.ArrayMath;
import xal.tools.data.DataAdaptor;


/**
 * MappedSimulator
 * @author  tap
 * @since Oct 18, 2004
 */
abstract public class MappedSimulator extends MachineSimulator {
	/** data label */
	static public String DATA_LABEL = "MappedSimulator";
	
	/** Map of x-axis detector/corrector responses keyed by corrector and BPM */
	protected ResponseMap _xResponseMap;
	
	/** Map of y-axis detector/corrector responses keyed by corrector and BPM */
	protected ResponseMap _yResponseMap;
	
	/** table of x-axis orbit array response to corrector changes keyed by BPM */
	protected Map<BpmAgent, double[]> _xResponses;

	/** table of y-axis orbit array response to corrector changes keyed by BPM */
	protected Map<BpmAgent, double[]> _yResponses;
	
	/** inidicates if the response needs to be updated */
	protected boolean _responseNeedsUpdate;


	/**
	 * Primary Constructor
	 * @param modificationStore the store of modifications
	 * @param sequence    The sequence over which the simulation is made.
	 * @param bpms        The BPMs to use in the simulation.
	 * @param supplies  The corrector supplies to use in the simulation.
	 */
	public MappedSimulator( final ModificationStore modificationStore, final AcceleratorSeq sequence, final List<BpmAgent> bpms, final List<CorrectorSupply> supplies ) {
		super( modificationStore, sequence, bpms, supplies );
		_responseNeedsUpdate = true;
	}


	/**
	 * Constructor
	 * @param orbitModel    The orbit model.
	 */
	public MappedSimulator( final OrbitModel orbitModel ) {
		this( orbitModel.getModificationStore(), orbitModel.getSequence(), orbitModel.getBPMAgents(), orbitModel.getCorrectorSupplies() );
	}
	
	
	/** 
	* Provides the name used to identify the class in an external data source.
	* @return a tag that identifies the receiver's type
	*/
    public String dataLabel() {
		return DATA_LABEL;
	}
    
    
    /**
	 * Update the data based on the information provided by the data provider.
     * @param adaptor The adaptor from which to update the data
     */
    public void update( final DataAdaptor adaptor ) {
        for (final DataAdaptor responseMapAdaptor : adaptor.childAdaptors(  ResponseMap.DATA_LABEL )){
			final int axis = responseMapAdaptor.intValue( ResponseMap.AXIS_KEY );
			
			switch ( axis ) {
				case ResponseMap.X_AXIS:
					_xResponseMap.update( axis, responseMapAdaptor );
					break;
				case ResponseMap.Y_AXIS:
					_yResponseMap.update( axis, responseMapAdaptor );
					break;
				default:
					break;
			}
		}
		
		_responseNeedsUpdate = true;
	}
    
    
    /**
	 * Write data to the data adaptor for storage.
     * @param adaptor The adaptor to which the receiver's data is written
     */
    public void write( final DataAdaptor adaptor ) {
		adaptor.setValue( "type", getSimulatorType() );
		adaptor.writeNode( _xResponseMap.generateDataListener( ResponseMap.X_AXIS ) );
		adaptor.writeNode( _yResponseMap.generateDataListener( ResponseMap.Y_AXIS ) );
	}
	
	
	/** Reset in preparation for the next simulation. */ 
	 public void reset() {
		 _responseNeedsUpdate = true;
	 }

	
	/** Clear cached data. */ 
	public void clear() {
		_xResponseMap.clear();
		_yResponseMap.clear();
		reset();
	}


	/**
	 * Initialize the simulator
	 * @param sequence    The sequence over which the orbit is measured.
	 * @param bpmAgents   The BPM agents used to measure the orbit.
	 * @param supplies  The corrector supplies used to correct the orbit.
	 */
	protected void setup( final AcceleratorSeq sequence, final List<BpmAgent> bpmAgents, final List<CorrectorSupply> supplies ) {
		_xResponseMap = new ResponseMap();
		_yResponseMap = new ResponseMap();
		_xResponses = new HashMap<BpmAgent, double[]>();
		_yResponses = new HashMap<BpmAgent, double[]>();
	}


	/**
	 * Set the sequence to the specified value along with the BPMs and correctors to use in the sequence.
	 * @param sequence    The sequence over which the simulation is made.
	 * @param bpmAgents   The new BPM agents to use in the simulation.
	 * @param supplies  The new corrector supplies to use in the simulation.
	 */
	public void setSequence( final AcceleratorSeq sequence, final List<BpmAgent> bpmAgents, final List<CorrectorSupply> supplies ) {
		super.setSequence( sequence, bpmAgents, supplies );
		
		if ( sequence == null )  return;

		_responseNeedsUpdate = true;
	}


	/**
	 * Set the BPM agents to the specified values.
	 * @param bpmAgents  The new list of BPM agents to use.
	 */
	public void setBPMAgents( final List<BpmAgent> bpmAgents ) {
		super.setBPMAgents( bpmAgents );
		_responseNeedsUpdate = true;
	}
	
	
	/**
	 * Event indicating that the bpm enable has changed.
	 * @param bpmAgent  the BPM agent whose enable status changed
	 */
	public void bpmFlattenEnableChanged( final BpmAgent bpmAgent ) {
		super.bpmFlattenEnableChanged( bpmAgent );
		_responseNeedsUpdate = true;
	}


	/**
	 * Set the corrector supplies to use in the simulation.
	 * @param supplies  The new list of corrector supplies to use in the simulation.
	 */
	public void setCorrectorSupplies( final List<CorrectorSupply> supplies ) {
		super.setCorrectorSupplies( supplies );
		_responseNeedsUpdate = true;
	}


	/**
	 * Add the corrector supply to the list of corrector supplies to use in the simulation.
	 * @param supply  The corrector supply to add to the simulation.
	 */
	public void setCorrectorSupplyEnable( final CorrectorSupply supply, final boolean enable ) {
		super.setCorrectorSupplyEnable( supply, enable );
		_responseNeedsUpdate = true;	
	}


	/** 
	 * Update the corrector-BPM response matrix.
	 * @return true if updated and false if interrupted
	 */
	protected boolean updateResponse() {
		final List<CorrectorSupply> supplies = new ArrayList<CorrectorSupply>();
		final List<BpmAgent> enabledBPMAgents = getEnabledBPMAgents();
		
		for ( CorrectorSupply supply : _correctorSuppliesToSimulate ) {
			supplies.add( supply );
		}
		
		if ( !_xResponseMap.hasResponse( supplies, enabledBPMAgents ) || !_yResponseMap.hasResponse( supplies, enabledBPMAgents ) ) {
			if ( !calculateResponse( supplies, enabledBPMAgents ) )  return false;
		}
		
		validateCorrectors();
		updateResponseArrays();
		
		_fractionPrepared = 1.0;
		
		return true;
	}
	
	
	/** validate correctors by checking that they have non-zero effect and disabling them if they don't */
	protected void validateCorrectors() {
		final List<BpmAgent> enabledBPMAgents = getEnabledBPMAgents();
		
		final List<CorrectorSupply> supplies = new ArrayList<CorrectorSupply>( _correctorSuppliesToSimulate );
		for ( CorrectorSupply supply : supplies ) {
			if ( !_xResponseMap.hasNonZeroEffect( supply, enabledBPMAgents ) && !_yResponseMap.hasNonZeroEffect( supply, enabledBPMAgents ) ) {
				setCorrectorSupplyEnable( supply, false );		// don't include any correctors which have zero effect on the BPMs
				supply.setEnabled( false );
				_correctorSuppliesToSimulate.remove( supply );
			}
		}
	}
	
	
	
	/** Update the response arrays based on the response maps and the enabled correctors and bpms. */
	protected void updateResponseArrays() {
		final List<CorrectorSupply> supplies = _correctorSuppliesToSimulate; 
		final List<BpmAgent> enabledBPMAgents = getEnabledBPMAgents();
		final int supplyCount = supplies.size();
		final int bpmCount = enabledBPMAgents.size();
		
		final boolean isRing = !_sequence.isLinear();
		double[][] xResponse = new double[bpmCount][supplyCount];
		double[][] yResponse = new double[bpmCount][supplyCount];
		for ( int supplyIndex = 0 ; supplyIndex < supplyCount ; supplyIndex++ ) {
			final CorrectorSupply supply = supplies.get( supplyIndex );
			final double firstCorrectorPosition = supply.getFirstCorrectorPositionIn( _sequence );
			for ( int bpmIndex = 0 ; bpmIndex < bpmCount ; bpmIndex++ ) {
				final BpmAgent bpmAgent = enabledBPMAgents.get( bpmIndex );
				
				if ( isRing || bpmAgent.getPositionIn( _sequence ) > firstCorrectorPosition ) {
					xResponse[bpmIndex][supplyIndex] = _xResponseMap.getResponse( supply, bpmAgent );
					yResponse[bpmIndex][supplyIndex] = _yResponseMap.getResponse( supply, bpmAgent );					
				}
				else {
					xResponse[bpmIndex][supplyIndex] = 0;
					yResponse[bpmIndex][supplyIndex] = 0;
				}				
			}
		}
		
		for ( int bpmIndex = 0 ; bpmIndex < bpmCount ; bpmIndex++ ) {
			final BpmAgent bpmAgent = enabledBPMAgents.get( bpmIndex );
			_xResponses.put( bpmAgent, xResponse[bpmIndex] );
			_yResponses.put( bpmAgent, yResponse[bpmIndex] );
		}
		
		_modificationStore.postModification( this );
		_responseNeedsUpdate = false;
	}


	/** 
	 * Calculate the corrector-BPM response matrix.
	 * @return true if updated and false if interrupted
	 */
	abstract protected boolean calculateResponse( final List<CorrectorSupply> supplies, final List<BpmAgent> bpmAgents );


	/**
	 * Get the array of corrector dependence on the X position of the specified BPM.
	 * @param bpmAgent  The BPM agent for which to get the corrector dependence vector
	 * @return     The corrector dependence vector for the specified BPM in the X coordinate
	 */
	protected double[] getXResponse( final BpmAgent bpmAgent ) {
		return getResponse( bpmAgent, _xResponses );
	}


	/**
	 * Get the array of corrector dependence on the Y position of the specified BPM.
	 * @param bpmAgent  The BPM agent for which to get the corrector dependence vector
	 * @return     The corrector dependence vector for the specified BPM in the Y coordinate
	 */
	protected double[] getYResponse( final BpmAgent bpmAgent ) {
		return getResponse( bpmAgent, _yResponses );
	}


	/**
	 * Get the corrector dependence vector for the specified BPM from the specified response table
	 * where the response table corresponds to either the X or Y coordinate.
	 * @param bpmAgent    The BPM agent for which to get the corrector dependence vector
	 * @param table  The table (X or Y coordinate) from which to retrieve the corrector dependence
	 * @return       The corrector dependence vector for the specified BPM and coordinate
	 */
	private double[] getResponse( final BpmAgent bpmAgent, final Map< BpmAgent , double[]> table ) {
		if ( _responseNeedsUpdate )  updateResponse();
		
		return table.get( bpmAgent );
	}
	
	
	/**
	 * Prepare for the simulation runs.
	 * @return true if successful and false if not.
	 */
	public boolean prepare() {
		if ( !_responseNeedsUpdate ) {
			_fractionPrepared = 1.0;
			return true;		// we are prepared
		}
		else {
			_fractionPrepared = 0.0;
			_shouldStopPreparing = false;
			return updateResponse();
		}
	}


	/**
	 * Calculate and get the predicted orbit given the initial orbit, initial corrector strengths and the final corrector strengths.
	 * @param initialOrbit                  The initial orbit
	 * @param initialCorrectorDistribution  The initial corrector strengths
	 * @param trialCorrectorDistribution    The trial corrector strengths
	 * @return                              The predicted Orbit
	 */
	public Orbit predictOrbit( final Orbit initialOrbit, final CorrectorDistribution initialCorrectorDistribution, final CorrectorDistribution trialCorrectorDistribution ) {
		final MutableOrbit orbit = new MutableOrbit( _sequence );
		final double[] initialStrengths = initialCorrectorDistribution.getFields( _correctorSuppliesToSimulate );
		final double[] trialStrengths = trialCorrectorDistribution.getFields( _correctorSuppliesToSimulate );
		final double[] correction = ArrayMath.subtract( trialStrengths, initialStrengths );
		
		final Date timestamp = new Date();
		
		final List<BpmAgent> enabledBPMAgents = getEnabledBPMAgents();
		for ( BpmAgent bpmAgent : enabledBPMAgents ) {
			final BpmRecord record = initialOrbit.getRecord( bpmAgent );
			if ( record == null ) {
				continue;
			}
		
			final double[] xResponse = getXResponse( bpmAgent );
			final double[] yResponse = getYResponse( bpmAgent );
			
			final double x = record.getXAvg() + ArrayMath.scalarProduct( xResponse, correction );
			final double y = record.getYAvg() + ArrayMath.scalarProduct( yResponse, correction );
			
			final BpmRecord newRecord = new BpmRecord( bpmAgent, timestamp, x, y );
			orbit.addRecord( newRecord );
		}
		
		return orbit.getOrbit();
	}
}

