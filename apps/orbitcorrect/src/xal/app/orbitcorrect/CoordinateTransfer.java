//
//  CoordinateTransfer.java
//  xal
//
//  Created by Tom Pelaia on 1/12/07.
//  Copyright 2007 Oak Ridge National Lab. All rights reserved.
//

package xal.app.orbitcorrect;

import java.util.*;

import xal.smf.*;
import xal.model.probe.traj.*;
import xal.tools.beam.PhaseMatrix;


/** represent the transfer of coordinate information from two BPMs to another node in the sequence */
abstract public class CoordinateTransfer {
	/** get the appropriate instance of the coordinate transfer */
	static public CoordinateTransfer getInstance( final BeamMarker<?> beamMarker, final List<BpmAgent> bpmReferences ) {
		return beamMarker instanceof BpmAgent ? new BPMCoordinateTransfer( (BpmAgent)beamMarker ) : new GenericMarkerCoordinateTransfer( beamMarker, bpmReferences );
	}
	
	
	/** get a beam marker record given the BPM inputs */
	abstract public BeamMarkerRecord<?> getBeamMarkerRecord( final Orbit orbit );
	
	
	/** generate the transfer map from the trajectory */
	abstract public void generateTransferMap( final Trajectory<?> trajectory, final AcceleratorSeq sequence );
}



/** BPM Coordinate Transfer */
class BPMCoordinateTransfer extends CoordinateTransfer {
	/** the BPM agent for which to get the records */
	final protected BpmAgent BPM_AGENT;
	
	
	/** Constructor */
	public BPMCoordinateTransfer( final BpmAgent bpmAgent ) {
		BPM_AGENT = bpmAgent;
	}
	
	
	/** get a beam marker record given the BPM inputs */
	public BeamMarkerRecord<?> getBeamMarkerRecord( final Orbit orbit ) {
		return orbit.getRecord( BPM_AGENT );
	}
	
	
	/** generate the transfer map from the trajectory */
	public void generateTransferMap( final Trajectory<?> trajectory, final AcceleratorSeq sequence ) {}	
}



/** Generic marker coordinate transfer */
class GenericMarkerCoordinateTransfer extends CoordinateTransfer {
	/** first BPM reference */
	final protected BpmAgent BPM_REF_A;
	
	/** second BPM reference */
	final protected BpmAgent BPM_REF_B;
	
	/** the element to which to transfer coordinate information */
	final protected BeamMarker<?> BEAM_MARKER;
	
	/** horizontal transfer map */
	final protected double[] X_TRANSFER_MAP;
	
	/** vertical transfer map */
	final protected double[] Y_TRANSFER_MAP;
	
	
	/** Constructor */
	public GenericMarkerCoordinateTransfer( final BeamMarker<?> beamMarker, final List<BpmAgent> bpmReferences ) {
		BEAM_MARKER = beamMarker;
		BPM_REF_A = bpmReferences.get( 0 );
		BPM_REF_B = bpmReferences.get( 1 );
		
		X_TRANSFER_MAP = new double[] { 0.5, 0.5, 0.0 };
		Y_TRANSFER_MAP = new double[] { 0.5, 0.5, 0.0 };
	}
	
	
	/** get a beam marker record given the BPM inputs */
	public BeamMarkerRecord<?> getBeamMarkerRecord( final Orbit orbit ) {
		final BpmRecord recordA = orbit.getRecord( BPM_REF_A );
		final BpmRecord recordB = orbit.getRecord( BPM_REF_B );
		
		if ( recordA != null && recordB != null ) {
			final Date timeStamp = recordA.getTimestamp();
			final double amplitude = ( recordA.getAmpAvg() + recordB.getAmpAvg() ) / 2.0;
			final double x = X_TRANSFER_MAP[0] * recordA.getXAvg() + X_TRANSFER_MAP[1] * recordB.getXAvg() + X_TRANSFER_MAP[2];
			final double y = Y_TRANSFER_MAP[0] * recordA.getYAvg() + Y_TRANSFER_MAP[1] * recordB.getYAvg() + Y_TRANSFER_MAP[2];
			
			return new BeamMarkerRecord<>( BEAM_MARKER, timeStamp, x, y, amplitude );
		}
		else {
			return null;
		}
	}
	
	
	/** generate the transfer map from the trajectory */
	public void generateTransferMap( final Trajectory<?> trajectory, final AcceleratorSeq sequence ) {
		// get the transfer matrix from BPM A to BPM B
		final PhaseMatrix transferAB = getTransferMatrix( trajectory, BPM_REF_A.getBPM(), BPM_REF_B.getBPM() );
		
		// get the transfer matrix from BPM A to the beam marker
		final PhaseMatrix transferAMarker = getTransferMatrix( trajectory, BPM_REF_A.getBPM(), BEAM_MARKER.getNode() );
		
		generateXTransferMap( transferAB, transferAMarker );
		generateYTransferMap( transferAB, transferAMarker );
	}
	
	
	/** generate the horizontal transfer map */
	protected void generateXTransferMap( final PhaseMatrix transferAB, final PhaseMatrix transferAMarker ) {
		// get the transfer matrix elements for the transfer between BPM A and the beam marker
		final double r11 = transferAMarker.getElem( PhaseMatrix.IND_X, PhaseMatrix.IND_X );
		final double r12 = transferAMarker.getElem( PhaseMatrix.IND_X, PhaseMatrix.IND_XP );
		final double r13 = transferAMarker.getElem( PhaseMatrix.IND_X, PhaseMatrix.IND_HOM );
		
		// get the transfer matrix elements for the transfer between BPM A and BPM B
		final double t11 = transferAB.getElem( PhaseMatrix.IND_X, PhaseMatrix.IND_X );
		final double t12 = transferAB.getElem( PhaseMatrix.IND_X, PhaseMatrix.IND_XP );
		final double t13 = transferAB.getElem( PhaseMatrix.IND_X, PhaseMatrix.IND_HOM );
		
		X_TRANSFER_MAP[0] = r11 - r12 * t11 / t12;
		X_TRANSFER_MAP[1] = r12 / t12;
		X_TRANSFER_MAP[2] = 1000 * ( r13 - r12 * t13 / t12 );
	}
	
	
	/** generate the vertical transfer map */
	protected void generateYTransferMap( final PhaseMatrix transferAB, final PhaseMatrix transferAMarker ) {
		// get the transfer matrix elements for the transfer between BPM A and the beam marker
		final double r11 = transferAMarker.getElem( PhaseMatrix.IND_Y, PhaseMatrix.IND_Y );
		final double r12 = transferAMarker.getElem( PhaseMatrix.IND_Y, PhaseMatrix.IND_YP );
		final double r13 = transferAMarker.getElem( PhaseMatrix.IND_Y, PhaseMatrix.IND_HOM );
		
		// get the transfer matrix elements for the transfer between BPM A and BPM B
		final double t11 = transferAB.getElem( PhaseMatrix.IND_Y, PhaseMatrix.IND_Y );
		final double t12 = transferAB.getElem( PhaseMatrix.IND_Y, PhaseMatrix.IND_YP );
		final double t13 = transferAB.getElem( PhaseMatrix.IND_Y, PhaseMatrix.IND_HOM );
		
		Y_TRANSFER_MAP[0] = r11 - r12 * t11 / t12;
		Y_TRANSFER_MAP[1] = r12 / t12;
		Y_TRANSFER_MAP[2] = 1000 * ( r13 - r12 * t13 / t12 );
	}
	
	
	/** get the transfer matrix from the transfer map trajectory */
	
	/** 
	 * These getTransferMatrix functions had to be rewritten to implement the generic Trajectory
	 * and could be simplified from multiple functions, to just the two.  I left the old functions
	 * commented out.
	 * 
	 * - Jonathan M. Freed
	 * 
	 */
	protected PhaseMatrix getTransferMatrix( final Trajectory<?> trajectory, final AcceleratorNode fromNode, final AcceleratorNode toNode ) {
		if ( (trajectory.getStateClass()).isInstance(EnvelopeProbeState.class) ) {
			//return getTransferMatrix( trajectory, fromNode, toNode );
			final PhaseMatrix fromMatrix = ((EnvelopeProbeState)trajectory.stateForElement( fromNode.getId() )).getResponseMatrix();
			final PhaseMatrix toMatrix = ((EnvelopeProbeState)trajectory.stateForElement( toNode.getId() )).getResponseMatrix();
			return getTransferMatrix( fromMatrix, toMatrix );
		}
		else if ( (trajectory.getStateClass()).isInstance(TransferMapState.class) ) {
			//return getTransferMatrix( trajectory, fromNode, toNode );
			final PhaseMatrix fromMatrix = ((TransferMapState)trajectory.stateForElement( fromNode.getId() )).getTransferMap().getFirstOrder();
			final PhaseMatrix toMatrix = ((TransferMapState)trajectory.stateForElement( toNode.getId() )).getTransferMap().getFirstOrder();
			return getTransferMatrix( fromMatrix, toMatrix );
		}
		else {
			throw new RuntimeException( "Trajectory must be either TransferMap or Envelope, but instead it is:  " + trajectory.getClass() );
		}
	}
	
	
//	/** get the transfer matrix from the transfer map trajectory */
//	protected PhaseMatrix getTransferMatrix( final Trajectory<EnvelopeProbeState> trajectory, final AcceleratorNode fromNode, final AcceleratorNode toNode ) {
//		final PhaseMatrix fromMatrix = ((EnvelopeProbeState)trajectory.stateForElement( fromNode.getId() )).getResponseMatrix();
//		final PhaseMatrix toMatrix = ((EnvelopeProbeState)trajectory.stateForElement( toNode.getId() )).getResponseMatrix();
//		return getTransferMatrix( fromMatrix, toMatrix );
//	}
//	
//	
//	/** get the transfer matrix from the transfer map trajectory */
//	protected PhaseMatrix getTransferMatrix( final TransferMapTrajectory trajectory, final AcceleratorNode fromNode, final AcceleratorNode toNode ) {
//		final PhaseMatrix fromMatrix = ((TransferMapState)trajectory.stateForElement( fromNode.getId() )).getTransferMap().getFirstOrder();
//		final PhaseMatrix toMatrix = ((TransferMapState)trajectory.stateForElement( toNode.getId() )).getTransferMap().getFirstOrder();
//		return getTransferMatrix( fromMatrix, toMatrix );
//	}
//	
	
	/** get the transfer matrix between the two response matricies */
	static protected PhaseMatrix getTransferMatrix( final PhaseMatrix fromMatrix, final PhaseMatrix toMatrix ) {
		return toMatrix.times( fromMatrix.inverse() );
	}	
}
