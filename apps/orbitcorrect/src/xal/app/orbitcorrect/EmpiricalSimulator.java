/*
 * EmpiricalSimulator.java
 *
 * Created on Mon Oct 11 15:32:58 EDT 2004
 *
 * Copyright (c) 2004 Spallation Neutron Source
 * Oak Ridge National Laboratory
 * Oak Ridge, TN 37830
 */

package xal.app.orbitcorrect;

import xal.smf.*;
import xal.smf.impl.*;
import xal.tools.*;
import xal.extension.fit.*;
import xal.tools.statistics.*;

import java.util.*;
import java.util.logging.*;


/**
 * EmpiricalSimulator
 * @author  tap
 * @since Oct 11, 2004
 */
public class EmpiricalSimulator extends MappedSimulator {	
	/** time in milliseconds for the beam to settle */
	protected long _beamSettleTime;
	
	/** number of corrector calibration trials to run per corrector */
	protected int _correctorCalibrationTrials;
	
	/** the corrector field excursion as a fraction of corrector range */
	protected double _correctorSampleExcursion;
	
	/** live orbit source */
	protected LiveOrbitSource _orbitSource;


	/**
	 * Primary Constructor
	 * @param modificationStore the store of modifications
	 * @param sequence    The sequence over which the orbit is measured.
	 * @param bpmAgents   The BPMs used to measure the orbit.
	 * @param supplies  The corrector supplies used to correct the orbit.
	 */
	public EmpiricalSimulator( final ModificationStore modificationStore, final AcceleratorSeq sequence, final List<BpmAgent> bpmAgents, final List<CorrectorSupply> supplies ) {
		super( modificationStore, sequence, bpmAgents, supplies );
		_responseNeedsUpdate = true;
		setBeamSettleTime( 4000 );
		setCorrectorCalibrationTrials( 5 );
		setCorrectorSampleExcursion( 0.1 );		// default to 10% field excursion
		_orbitSource = new LiveOrbitSource( "calibration", sequence, bpmAgents, true );
	}


	/**
	 * Constructor
	 * @param orbitModel    The orbit model.
	 */
	public EmpiricalSimulator( final OrbitModel orbitModel ) {
		this( orbitModel.getModificationStore(), orbitModel.getSequence(), orbitModel.getBPMAgents(), orbitModel.getCorrectorSupplies() );
	}
	
	
	/**
	 * Get the unique simulator type.
	 * @return a unique string identifying this simulator class
	 */
	static public String getType() {
		return EmpiricalSimulator.class.toString();
	}
	
	
	/**
	 * Get the time to wait for the beam to settle after changing a corrector.
	 * @return the time in milliseconds to wait for the beam to settle 
	 */
	public long getBeamSettleTime() {
		return _beamSettleTime;
	}
	
	
	/**
	 * Set the time to wait for the beam to settle after changing a corrector.
	 * @param delay the time in milliseconds to wait for the beam to settle
	 */
	public void setBeamSettleTime( final long delay ) {
		_beamSettleTime = ( delay > 0 ) ? delay : _beamSettleTime;
	}
	
	
	/**
	 * Set the number of corrector calibrations trials to run per corrector.
	 * @param count the number of trials to run per corrector during calibration
	 */
	public void setCorrectorCalibrationTrials( final int count ) {
		_correctorCalibrationTrials = ( count >= 2 ) ? count : _correctorCalibrationTrials;
	}
	
	
	/**
	 * Get the number of calibration trials to run for each corrector.
	 * @return the number of calibration trials to run per corrector
	 */
	public int getCorrectorCalibrationTrials() {
		return _correctorCalibrationTrials;
	}
	
	
	/**
	 * Set the sample corrector excursion (as a fraction of corrector limits) to be used during corrector calibration.
	 * @param excursion the corrector field excursion as a fraction of corrector field limits
	 */
	public void setCorrectorSampleExcursion( final double excursion ) {
		_correctorSampleExcursion = ( excursion > 0.0 && excursion < 1.0 ) ? excursion : _correctorSampleExcursion;
	}
	
	
	/**
	 * Get the corrector field excursion to be used during corrector calibration samples.
	 * @return the field excursion as a fraction of corrector limits
	 */
	public double getCorrectorSampleExcursion() {
		return _correctorSampleExcursion;
	}


	/** 
	 * Update the corrector-BPM response matrix.
	 * @param supplies the corrector supplies for which to calculate the corrector-BPM response matrix
	 * @param bpmAgents the BPM agents for which to calculate the corrector-BPM response matrix
	 * @return true if updated and false if interrupted
	 */
	protected boolean calculateResponse( final List<CorrectorSupply> supplies, final List<BpmAgent> bpmAgents ) {
		final int supplyCount = supplies.size();
		final int bpmCount = bpmAgents.size();
		
		try {
			_orbitSource.setSequence( _sequence, bpmAgents );
			for ( int supplyIndex = 0 ; supplyIndex < supplyCount ; supplyIndex++ ) {
				if ( _shouldStopPreparing )  return false;
								
				_fractionPrepared = ((double)supplyIndex) / supplyCount;
				
				final CorrectorSupply supply = supplies.get( supplyIndex );

				if ( _xResponseMap.hasResponse( supply, bpmAgents ) && _yResponseMap.hasResponse( supply, bpmAgents )  ) {
					continue;
				}
				try {
					measureResponse( supply, supplyCount, bpmAgents );
				}
				catch ( Exception exception ) {
					exception.printStackTrace();
					disableCorrectorSupply( supply );
				}
			}
		}
		catch( Exception exception ) {
			Logger.getLogger(Logger.GLOBAL_LOGGER_NAME).log( Level.SEVERE, "Error updating the response matrix.", exception );
			exception.printStackTrace();
			System.exit( -1 );
		}
		finally {
			sleep( _beamSettleTime );	// wait for the orbit to settle			
		}
		
		return true;
	}
	
	
	/**
	 * Measure the response of the detectors to the specified corrector supply.
	 * @param supply the corrector supply to calibrate
	 * @param supplyCount the total number of supplies to calibrate
	 * @param bpmAgents the BPM agents whose response will be measured against the corrector field excursions
	 */
	private void measureResponse( final CorrectorSupply supply, final double supplyCount, final List<BpmAgent> bpmAgents ) throws Exception {
		final long beamSettleTime = _beamSettleTime;
		final int bpmCount = bpmAgents.size();
		
		final LinearFit[] xFit = new LinearFit[bpmCount];
		final LinearFit[] yFit = new LinearFit[bpmCount];
		
		final double fieldRange = supply.getUpperFieldLimit() - supply.getLowerFieldLimit();
		final double TRIAL_FIELD_EXCURSION = fieldRange < 1.0 ? _correctorSampleExcursion * fieldRange : .002;
		final int NUM_STEPS = _correctorCalibrationTrials;
		final double FIELD_STEP = 2 * TRIAL_FIELD_EXCURSION / ( NUM_STEPS - 1 );
		
		final double initialField = supply.getFieldSetting();
		final double startField = initialField - TRIAL_FIELD_EXCURSION;
		
		for ( int bpmIndex = 0 ; bpmIndex < bpmCount ; bpmIndex++ ) {
			xFit[bpmIndex] = new LinearFit();
			yFit[bpmIndex] = new LinearFit();
		}
		
		final double BASE_FRACTION_PREPARED = _fractionPrepared;
		for ( int stepIndex = 0 ; stepIndex < NUM_STEPS ; stepIndex ++ ) {
			try {
				_fractionPrepared = BASE_FRACTION_PREPARED + ((double)stepIndex) / ( supplyCount * NUM_STEPS );
				final double trialField = startField + stepIndex * FIELD_STEP;
				supply.setField( trialField );
				sleep( _beamSettleTime );
				
				Orbit orbit = null;
				while ( !_shouldStopPreparing && (orbit = _orbitSource.getOrbit()) == null )  sleep( 100 );
				if ( _shouldStopPreparing ) {
					resetCorrectorSupply( supply, initialField );
					return;
				}
				if ( orbit == null ) {
					disableCorrectorSupply( supply, initialField );
					return;
				}
				final double trialFieldRead = supply.getFieldSetting();
				for ( int bpmIndex = 0 ; bpmIndex < bpmCount ; bpmIndex++ ) {
					final BpmAgent bpmAgent = bpmAgents.get( bpmIndex );
					final BpmRecord record = orbit.getRecord( bpmAgent );
					if ( record == null )  continue;
					if ( !_xResponseMap.hasResponse( supply, bpmAgent ) ) {
						xFit[ bpmIndex ].addSample( trialFieldRead, record.getXAvg() );
					}
					if ( !_yResponseMap.hasResponse( supply, bpmAgent ) ) {
						yFit[ bpmIndex ].addSample( trialFieldRead, record.getYAvg() );
					}
				}
			}
			catch( Exception exception ) {
				exception.printStackTrace();
			}
		}
		
		System.out.println( "Calibration for corrector:  " + supply.getID() );
		for ( int bpmIndex = 0 ; bpmIndex < bpmCount ; bpmIndex++ ) {
			final BpmAgent bpmAgent = bpmAgents.get( bpmIndex );
			
			if ( _sequence.isLinear() && supply.getFirstCorrectorPositionIn( _sequence ) > bpmAgent.getPositionIn( _sequence ) ) {
				_xResponseMap.setResponse( supply, bpmAgent, 0.0 );
				_yResponseMap.setResponse( supply, bpmAgent, 0.0 );
				continue;
			}
			
			if ( !_xResponseMap.hasResponse( supply, bpmAgent ) ) {
				if ( supply.isVertical() ) {
					_xResponseMap.setResponse( supply, bpmAgent, 0.0 );
				}
				else {
					final double correlation = Math.abs( xFit[bpmIndex].getCorrelationCoefficient() );
					double xResponse = xFit[bpmIndex].getSlope();
					
					if ( Double.isNaN( xResponse ) || Double.isInfinite( xResponse ) ) {
						disableCorrectorSupply( supply, initialField );
						return;
					}
					else if ( correlation < 0.6 || Double.isNaN( correlation ) || Double.isInfinite( correlation ) ) {
						if ( Math.abs( TRIAL_FIELD_EXCURSION * xResponse ) < 0.5 ) {	// .1 mm effect is negligible
							_xResponseMap.setResponse( supply, bpmAgent, 0.0 );
						}
						else {
							disableCorrectorSupply( supply, initialField );
							return;
						}
					}
					else {
						_xResponseMap.setResponse( supply, bpmAgent, xResponse );					
					}
				}
			}
			if ( !_yResponseMap.hasResponse( supply, bpmAgent ) ) {
				if ( supply.isHorizontal() ) {
					_yResponseMap.setResponse( supply, bpmAgent, 0.0 );
				}
				else {
					final double correlation = Math.abs( yFit[bpmIndex].getCorrelationCoefficient() );
					double yResponse = yFit[bpmIndex].getSlope();
					
					if ( Double.isNaN( yResponse ) || Double.isInfinite( yResponse ) ) {
						disableCorrectorSupply( supply, initialField );
						return;
					}
					else if ( correlation < 0.7 || Double.isNaN( correlation ) || Double.isInfinite( correlation ) ) {
						//System.out.println( "y effect (mm):  " + TRIAL_FIELD_EXCURSION * yResponse );
						if ( Math.abs( TRIAL_FIELD_EXCURSION * yResponse ) < 0.1 ) {	// .1 mm effect is negligible
							_yResponseMap.setResponse( supply, bpmAgent, 0.0 );
						}
						else {
							disableCorrectorSupply( supply, initialField );
							return;
						}
					}
					else {
						_yResponseMap.setResponse( supply, bpmAgent, yResponse );					
					}
				}
			}
		}
		
		resetCorrectorSupply( supply, initialField );
	}
	
	
	/**
	 * Disable the specified corrector from being used in the correction.
	 * @param supply the corrector supply to disable
	 */
	protected void disableCorrectorSupply( final CorrectorSupply supply ) {
		System.out.println( "Disabling corrector supply: " + supply.getID() );
		setCorrectorSupplyEnable( supply, false );
		_xResponseMap.removeResponses( supply );
		_yResponseMap.removeResponses( supply );
	}
	
	
	/**
	 * Disable the specified corrector from being used in the correction and reset its value to the initial field.
	 * @param supply corrector supply to disable
	 * @param initialField the field to which to set the corrector supply
	 */
	protected void disableCorrectorSupply( final CorrectorSupply supply, final double initialField ) {
		disableCorrectorSupply( supply );
		resetCorrectorSupply( supply, initialField );
	}
	
	
	/**
	 * Attempt to restore the initial corrector supply field
	 * @param supply the corrector supply to reset
	 * @param initialField the field to which to reset the corrector
	 */
	static protected void resetCorrectorSupply( final CorrectorSupply supply, final double initialField ) {
		try {
			supply.setField( initialField );			
		}
		catch ( Exception exception ) {
			exception.printStackTrace();
		}
	}
	
	
	/** sleep for the specified number of milliseconds */
	static protected void sleep( final long duration ) {
		final long startTime = new Date().getTime();
		while ( new Date().getTime() - startTime < duration ) {}
	}
}




