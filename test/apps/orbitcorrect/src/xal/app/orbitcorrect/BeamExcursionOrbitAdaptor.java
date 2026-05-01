//
//  BeamExcursionOrbitAdaptor.java
//  xal
//
//  Created by Tom Pelaia on 1/10/07.
//  Copyright 2007 Oak Ridge National Lab. All rights reserved.
//

package xal.app.orbitcorrect;

import java.util.*;

import xal.smf.*;
import xal.model.alg.*;
import xal.model.probe.*;
import xal.sim.scenario.*;
import xal.model.probe.traj.*;
import xal.smf.impl.*;


/** generate beam excursions from orbits */
public class BeamExcursionOrbitAdaptor {
	/** sequence for which to map orbits to beam excursions */
	protected AcceleratorSeq _sequence;
	
	/** the BPM agents which provide the BPM readings */
	protected List<BpmAgent> _bpmAgents;
	
	/** list of coordinate transfers */
	protected List<CoordinateTransfer> _coordinateTransfers;
	
	/** indicates whether the translation map needs updating */
	protected boolean _needsUpdate;
	
	
	/** Constructor */
	public BeamExcursionOrbitAdaptor( final AcceleratorSeq sequence, final List<BpmAgent> bpmAgents ) {
		setSequence( sequence );
		setBPMAgents( bpmAgents );
	}
	
	
	/** get the accelerator sequence */
	public AcceleratorSeq getSequence() {
		return _sequence;
	}
	
	
	/** set a new accelerator sequence */
	public void setSequence( final AcceleratorSeq sequence ) {
		if ( sequence != _sequence ) {
			_sequence = sequence;
			_needsUpdate = true;
		}
	}
	
	
	/** get the BPM agents */
	public List<BpmAgent> getBPMAgents() {
		return _bpmAgents;
	}
	
	
	/** set the BPM agents */
	public void setBPMAgents( final List<BpmAgent> bpmAgents ) {
		_bpmAgents = bpmAgents;
		_needsUpdate = true;
	}
	
	
	/** update the map with the specified scenario trajectory */
	public void update( final Trajectory<?> trajectory ) {
		try {
			final AcceleratorSeq sequence = _sequence;
			
			if ( sequence != null ) {
				final List<BpmAgent> bpmAgents = _bpmAgents;			
				final List<AcceleratorNode> quadrupoles = sequence.getNodesOfType( Quadrupole.s_strType, true );
				final List<BeamMarker<?>> beamMarkers = mergeNodes( sequence, bpmAgents, quadrupoles );
				
				final List<CoordinateTransfer> coordinateTransfers = getCoordinateTransfers( beamMarkers, bpmAgents );
				for ( final CoordinateTransfer transfer : coordinateTransfers ) {
					transfer.generateTransferMap( trajectory, sequence );
				}
				_coordinateTransfers = coordinateTransfers;
				
				_needsUpdate = false;
			}
		}
		catch( Exception exception ) {
			throw new RuntimeException( "Exception running the online model to generate a beam excursion map.", exception );
		}
	}
	
	
	/** run the online model */
	protected void generateMap() {
		try {
			final AcceleratorSeq sequence = _sequence;
			
			if ( sequence != null ) {
                
                final Probe<?> probe;
                
                probe = (sequence instanceof Ring) ? ProbeFactory.getTransferMapProbe( sequence, AlgorithmFactory.createTransferMapTracker(sequence)) : ProbeFactory.getEnvelopeProbe( sequence, AlgorithmFactory.createEnvTrackerAdapt(sequence));

            // the transfer matrices that matter are the ones for the coherent bunch motion
				if ( probe instanceof BunchProbe<?> ) {
                    //Do not need anymore ((BunchProbe)probe).setBeamCharge( 0.0 );
					((BunchProbe<?>)probe).setBeamCurrent( 0.0 );
				}
				final Scenario scenario = Scenario.newScenarioFor( _sequence );
				scenario.setProbe( probe );
				scenario.setSynchronizationMode( Scenario.SYNC_MODE_RF_DESIGN );
				scenario.resync();
				scenario.run();
				update( probe.getTrajectory() );
			}
		}
		catch( Exception exception ) {
			throw new RuntimeException( "Exception running the online model to generate a beam excursion map.", exception );
		}
	}
	
	
	/** determine beam marker clusters */
	private List<CoordinateTransfer> getCoordinateTransfers( final List<BeamMarker<?>> beamMarkers, final List<BpmAgent> bpmAgents ) {
		final AcceleratorSeq sequence = _sequence;
		
		final List<CoordinateTransfer> coordinateTransfers = new ArrayList<CoordinateTransfer>( beamMarkers.size() );
		
		// the following logic is really only optimal for the linac, but for now we can also live with it for the Ring
		int lastBpmIndex = -1;
		final int bpmCount = bpmAgents.size();
		final int count = beamMarkers.size();
		for ( int index = 0 ; index < count ; index++ ) {
			final BeamMarker<?> marker = beamMarkers.get( index );
			if ( marker instanceof BpmAgent ) {
				lastBpmIndex += 1;
				final CoordinateTransfer transfer = CoordinateTransfer.getInstance( marker, null );
				coordinateTransfers.add( transfer );
			}
			else {
				final BeamMarkerCluster cluster = new BeamMarkerCluster( marker );
				final int startBPMIndex = Math.max( 0, lastBpmIndex - 1 );
				final int endBPMIndex = Math.min( bpmCount, lastBpmIndex + 3 );
				for ( int bpmIndex = startBPMIndex ; bpmIndex < endBPMIndex ; bpmIndex++ ) {
					final BpmAgent bpmAgent = bpmAgents.get( bpmIndex );
					cluster.considerBPM( bpmAgent, sequence );
				}
				final CoordinateTransfer transfer = CoordinateTransfer.getInstance( marker, cluster.getBPMAgentList() );
				coordinateTransfers.add( transfer );
			}
		}
		
		return coordinateTransfers;
	}
	
	
	/** merge BPMs and quadrupoles into a single node list sorted by position */
	static protected List<BeamMarker<?>> mergeNodes( final AcceleratorSeq sequence, final List<BpmAgent> bpms, final List<AcceleratorNode> quadrupoles ) {
		final LinkedList<AcceleratorNode> quadrupoleStack = new LinkedList<AcceleratorNode>( quadrupoles );
		final LinkedList<BpmAgent> bpmStack = new LinkedList<BpmAgent>( bpms );
		final List<BeamMarker<?>> markers = new ArrayList<>();
		
		final int bpmCount = bpms.size();
		if ( bpmCount > 1 ) {	// we need at least one pair of BPMs to calculate positions at other nodes
			while( true ) {
				final BpmAgent bpm = bpmStack.size() > 0 ? bpmStack.getFirst() : null;
				final AcceleratorNode quadrupole = quadrupoleStack.size() > 0 ? quadrupoleStack.getFirst() : null;
				if ( bpm != null && quadrupole != null ) {
					final double bpmPosition = bpm.getPositionIn( sequence );
					final double quadrupolePosition = sequence.getPosition( quadrupole );
					if ( quadrupolePosition == bpmPosition ) {	// just toss the quadrupole since the BPM has it covered
						quadrupoleStack.removeFirst();
						bpmStack.removeFirst();
						markers.add( bpm );
					}
					else if ( quadrupolePosition < bpmPosition ) {
						quadrupoleStack.removeFirst();
						markers.add( new BeamMarker<>( quadrupole ) );
					}
					else {
						bpmStack.removeFirst();
						markers.add( bpm );
					}
				}
				else if ( bpm == null && quadrupole == null ) {
					break;
				}
				else if ( bpm == null ) {
					for ( final AcceleratorNode node : quadrupoleStack ) {
						markers.add( new BeamMarker<>( node ) );
					}
					quadrupoleStack.clear();
					break;
				}
				else if ( quadrupole == null ) {
					markers.addAll( bpmStack );
					bpmStack.clear();
					break;
				}
			}
		}
		else {
			markers.addAll( bpms );
		}
		
		return markers;
	}
	
	
	/** get a beam excursion from an orbit */
	public BeamExcursion getBeamExcursion( final Orbit orbit ) {
		if ( _sequence != null ) {
			if ( _needsUpdate ) {
				generateMap();
			}
			
			final MutableBeamExcursion beamExcursion = new MutableBeamExcursion( _sequence );
			final List<CoordinateTransfer> coordinateTransfers = _coordinateTransfers;
			for ( final CoordinateTransfer transfer : coordinateTransfers ) {
				final BeamMarkerRecord<?> record = transfer.getBeamMarkerRecord( orbit );
				if ( record != null ) {
					beamExcursion.addRecord( record );
				}
			}
			
			return new BeamExcursion( beamExcursion );
		}
		else {
			return null;
		}
	}
	
	
	
	/** cluster of two closest BPMs in proximity to a beam marker */
	private class BeamMarkerCluster {
		/** beam marker about which the BPMs are clustered */
		final protected BeamMarker<?> BEAM_MARKER;
		
		/** closest two BPM agents */
		protected BpmAgent[] bpmAgents;
		
		/** distances to the two closest BPM agents */
		protected double[] distances;
		
		
		/** Constructor */
		public BeamMarkerCluster( final BeamMarker<?> beamMarker ) {
			BEAM_MARKER = beamMarker;
			bpmAgents = new BpmAgent[2];
			distances = new double[2];
		}
		
		
		/** get the BPM Agents closest to the beam marker 
		 * 
		 * CKA - Never used 
		 */
		public BpmAgent[] getBPMAgents() {
			return bpmAgents;
		}
		
		
		/** get the BPM Agents closest to the beam marker as a list */
		public List<BpmAgent> getBPMAgentList() {
			final List<BpmAgent> list = new ArrayList<BpmAgent>(2);
			for ( int index = 0 ; index < bpmAgents.length ; index++ ) {
				list.add( bpmAgents[index] );
			}
			return list;
		}
		
		
		/** consider the BPM agents as candidates for this cluster 
		 * 
		 * CKA - Never used
		 */
		public void considerBPMs( final List<BpmAgent> bpmAgents, final AcceleratorSeq sequence ) {
			for ( final BpmAgent bpmAgent : bpmAgents ) {
				considerBPM( bpmAgent, sequence );
			}
		}
		
		
		/** consider the BPM agent as a candidate for this cluster */
		public void considerBPM( final BpmAgent bpmAgent, final AcceleratorSeq sequence ) {
			final double distance = Math.abs( sequence.getShortestRelativePosition( bpmAgent.getNode(), BEAM_MARKER.getNode() ) );
			if ( bpmAgents[0] == null ) {
				bpmAgents[0] = bpmAgent;
				distances[0] = distance;
			}
			else if ( bpmAgents[1] == null ) {
				bpmAgents[1] = bpmAgent;
				distances[1] = distance;
			}
			else if ( distance < distances[0] ) {
				bpmAgents[1] = bpmAgents[0];
				distances[1] = distances[0];
				bpmAgents[0] = bpmAgent;
				distances[0] = distance;
			}
			else if ( distance < distances[1] ) {
				bpmAgents[1] = bpmAgent;
				distances[1] = distance;
			}
		}
	}
}
