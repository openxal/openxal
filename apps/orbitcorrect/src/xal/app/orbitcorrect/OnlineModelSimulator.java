/*
 *  OnlineModelSimulator.java
 *
 *  Created on Tue Sep 07 14:40:21 EDT 2004
 *
 *  Copyright (c) 2004 Spallation Neutron Source
 *  Oak Ridge National Laboratory
 *  Oak Ridge, TN 37830
 */
package xal.app.orbitcorrect;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import xal.model.alg.ParticleTracker;
import xal.model.alg.TransferMapTracker;
import xal.model.probe.Probe;
import xal.model.probe.traj.ICoordinateState;
import xal.model.probe.traj.ProbeState;
import xal.model.probe.traj.Trajectory;
import xal.sim.scenario.AlgorithmFactory;
import xal.sim.scenario.ProbeFactory;
import xal.sim.scenario.Scenario;
import xal.smf.Ring;
import xal.smf.proxy.ElectromagnetPropertyAccessor;
import xal.tools.beam.PhaseVector;
import xal.tools.beam.calc.SimpleSimResultsAdaptor;


/**
 * OnlineModelSimulator
 * @author   tap
 * @since    Sep 07, 2004
 */
public class OnlineModelSimulator extends MappedSimulator {
    
//    /*
//     * Internal Classes
//     */
//    
//    private static class SimDataProcessor implements ISimLocResults<ProbeState> {
//
//        /*
//         * Local Attributes
//         */
//        
//        /** The simulation data */
//        private final Trajectory                        traj;
//        
//        /** The particle trajectory data processor */
//        private final ISimLocResults<? extends ProbeState>  engResults;
//        
//        
//        
//        /**
//         * @param supplies
//         * @param bpmAgents
//         * @return
//         *
//         * @author Christopher K. Allen
//         * @since  Nov 15, 2013
//         */
//        public SimDataProcessor(Trajectory traj) {
//            this.traj = traj;
//            
//            if (traj instanceof TransferMapTrajectory) {
//                TransferMapTrajectory trjXferMap = (TransferMapTrajectory)traj;
//                
//                this.engResults = new CalculationsOnMachines(trjXferMap);
//                
//            } else if (traj instanceof ParticleTrajectory) {
//                ParticleTrajectory  trjPart = (ParticleTrajectory)traj;
//                
//                this.engResults = new CalculationsOnParticles(trjPart);
//                
//            } else {
//                
//                throw new IllegalArgumentException("Unknown trajectory type " + traj.getClass());
//            }
//            
//        }
//        
//        /*
//         * ISimLocResults Interface
//         */
//        
//        /**
//         *
//         * @see xal.tools.beam.calc.ISimLocResults#computeCoordinatePosition(xal.model.probe.traj.ProbeState)
//         *
//         * @author Christopher K. Allen
//         * @since  Nov 15, 2013
//         */
//        @Override
//        public PhaseVector computeCoordinatePosition(ProbeState state) {
//            if (state instanceof TransferMapState)
//            return this.engResults.computeCoordinatePosition((TransferMapState)state);
//        }
//
//        /**
//         *
//         * @see xal.tools.beam.calc.ISimLocResults#computeFixedOrbit(xal.model.probe.traj.ProbeState)
//         *
//         * @author Christopher K. Allen
//         * @since  Nov 15, 2013
//         */
//        @Override
//        public PhaseVector computeFixedOrbit(ProbeState state) {
//            // TODO Auto-generated method stub
//            return null;
//        }
//
//        /**
//         *
//         * @see xal.tools.beam.calc.ISimLocResults#computeChromAberration(xal.model.probe.traj.ProbeState)
//         *
//         * @author Christopher K. Allen
//         * @since  Nov 15, 2013
//         */
//        @Override
//        public PhaseVector computeChromAberration(ProbeState state) {
//            // TODO Auto-generated method stub
//            return null;
//        }
//        
//    }
    
    
	/**
	 * Constructor
	 * @param orbitModel    The orbit model.
	 */
	public OnlineModelSimulator( final OrbitModel orbitModel ) {
		super( orbitModel.getModificationStore(), orbitModel.getSequence(), orbitModel.getBPMAgents(), orbitModel.getCorrectorSupplies() );
		_responseNeedsUpdate = true;
	}
	
	
	/**
	 * Get the unique simulator type.
	 * @return a unique string identifying this simulator class
	 */
	static public String getType() {
		return OnlineModelSimulator.class.toString();
	}
	
	
	/** 
	 * Calculate the corrector-BPM response matrix.
	 * @return true if updated and false if interrupted
	 */
	protected boolean calculateResponse( final List<CorrectorSupply> supplies, final List<BpmAgent> bpmAgents ) {
		final int supplyCount = supplies.size();
		final int bpmCount = bpmAgents.size();
		
		final double TRIAL_FIELD_EXCURSION = 0.001;
		final String FIELD_PROPERTY = ElectromagnetPropertyAccessor.PROPERTY_FIELD;
		
		try {

		    // CKA - Nov 25, 2013
		    // Create the probe according to the sequence type then run an online model
//            final Probe probe = (_sequence instanceof Ring) ? ProbeFactory.getTransferMapProbe( _sequence, AlgorithmFactory.createTransferMapTracker(_sequence) ) : ProbeFactory.createParticleProbe(_sequence, AlgorithmFactory.createParticleTracker(_sequence));
		
		    final Probe probe;
		    if (_sequence instanceof Ring) {
		        TransferMapTracker    algXferMap = AlgorithmFactory.createTransferMapTracker(_sequence);
		        probe = ProbeFactory.getTransferMapProbe(_sequence, algXferMap);
		                
		    } else {
		        ParticleTracker       algPart = AlgorithmFactory.createParticleTracker(_sequence);
		        probe = ProbeFactory.createParticleProbe(_sequence, algPart);
		        
		    }

			final Scenario scenario = Scenario.newScenarioFor( _sequence );
			scenario.setProbe( probe );
			scenario.setSynchronizationMode( Scenario.SYNC_MODE_RF_DESIGN );
			scenario.resync();
			scenario.run();
			final Trajectory initialTrajectory = probe.getTrajectory();
			
            // CKA - Nov 25, 2013
			SimpleSimResultsAdaptor  cmpCalcEngine = new SimpleSimResultsAdaptor(initialTrajectory);

			double[] xInitial = new double[bpmCount];
			double[] yInitial = new double[bpmCount];

			for ( int bpmIndex = 0 ; bpmIndex < bpmCount ; bpmIndex++ ) {
				final BpmAgent bpmAgent = bpmAgents.get( bpmIndex );
				
	            // CKA - Nov 25, 2013
//				ICoordinateState state = (ICoordinateState)initialTrajectory.stateForElement( bpmAgent.getID() );
//				PhaseVector coordinates = state.getFixedOrbit();
                ProbeState  state = initialTrajectory.stateForElement( bpmAgent.getID() );
                PhaseVector coordinates = cmpCalcEngine.computeFixedOrbit(state);
				
				xInitial[bpmIndex] = coordinates.getx();
				yInitial[bpmIndex] = coordinates.gety();
			}
			
			final boolean isRing = !_sequence.isLinear();
			for ( int supplyIndex = 0 ; supplyIndex < supplyCount ; supplyIndex++ ) {
				if ( _shouldStopPreparing )  return false;
				
				_fractionPrepared = ((double)supplyIndex) / supplyCount;
								
				final CorrectorSupply supply = supplies.get( supplyIndex );
				final double firstCorrectorPosition = supply.getFirstCorrectorPositionIn( _sequence );
				
				if ( _xResponseMap.hasResponse( supply, bpmAgents ) && _yResponseMap.hasResponse( supply, bpmAgents )  ) {
					continue;
				}
				
				final double initialField = supply.getFieldSetting();
				//System.out.println( supply.getID() + " initial field: " + initialField );
				
				final List<CorrectorAgent> correctorAgents = supply.getCorrectors();
				final double trialField = initialField + TRIAL_FIELD_EXCURSION;
				for ( CorrectorAgent correctorAgent : correctorAgents ) {
					final double magnetField = correctorAgent.getCorrector().toFieldFromCA( trialField );
					//System.out.println( "Corrector:  " + correctorAgent + ", Magnet Field:  " + magnetField + ", Supply Field:  " + trialField );
					scenario.setModelInput( correctorAgent.getCorrector(), FIELD_PROPERTY, magnetField );
				}
				probe.reset();
				scenario.resyncFromCache();
				scenario.run();
				
				final Trajectory trajectory = probe.getTrajectory();
				
				// CKA - Nov 25, 2013
				final SimpleSimResultsAdaptor cmpCalcEngineResp = new SimpleSimResultsAdaptor(trajectory);
	
				for ( int bpmIndex = 0 ; bpmIndex < bpmCount ; bpmIndex++ ) {
					final BpmAgent bpmAgent = bpmAgents.get( bpmIndex );
					
					if ( _xResponseMap.hasResponse( supply, bpmAgent ) && _yResponseMap.hasResponse( supply, bpmAgent ) ) {
						continue;
					}
					
					// verify that the BPM is downstream of the supply (which is always true for a Ring)
					if ( isRing || ( bpmAgent.getPositionIn( _sequence ) > firstCorrectorPosition ) ) {

					    // CKA - Nov 25, 2013
//						final ICoordinateState state = (ICoordinateState)trajectory.stateForElement( bpmAgent.getID() );
//						final PhaseVector coordinates = state.getFixedOrbit();
					    final ProbeState  state       = trajectory.stateForElement( bpmAgent.getID() );
					    final PhaseVector coordinates = cmpCalcEngineResp.computeFixedOrbit(state);
						
						// need factor of 1000 to convert from meters to mm
						final double xResponse = 1000 * ( coordinates.getx() - xInitial[bpmIndex] ) / TRIAL_FIELD_EXCURSION;
						//System.out.println( "BPM:  " + bpmAgent.getID() + ", response: " + xResponse );
						_xResponseMap.setResponse( supply, bpmAgent, xResponse );
						
						final double yResponse = 1000 * ( coordinates.gety() - yInitial[bpmIndex] ) / TRIAL_FIELD_EXCURSION;
						_yResponseMap.setResponse( supply, bpmAgent, yResponse );						
					}
					else {		// the response of upstream BPMs must be 0
						_xResponseMap.setResponse( supply, bpmAgent, 0.0 );
						_yResponseMap.setResponse( supply, bpmAgent, 0.0 );						
					}
				}
				
				for ( CorrectorAgent correctorAgent : correctorAgents ) {
					scenario.removeModelInput( correctorAgent.getCorrector(), FIELD_PROPERTY );
				}
			}
			probe.reset();
			scenario.resyncFromCache();
		}
		catch( Exception exception ) {
			Logger.getLogger(Logger.GLOBAL_LOGGER_NAME).log( Level.SEVERE, "Exception updating the response matrix.", exception );
			exception.printStackTrace();
			throw new RuntimeException( "Online Model calibration exception.", exception );
		}
		
		System.out.println( "Response calculation is complete..." );
		
		return true;
	}
}


