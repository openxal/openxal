//
//  OrbitMatcher.java
//  xal
//
//  Created by Tom Pelaia on 6/21/07.
//  Copyright 2007 Oak Ridge National Lab. All rights reserved.
//

package xal.extension.orbit;

import java.util.ArrayList;
import java.util.List;

import Jama.Matrix;
import xal.model.probe.traj.Trajectory;
import xal.model.probe.traj.TransferMapState;
import xal.smf.AcceleratorNode;
import xal.tools.beam.PhaseMatrix;


/** using the online model (ignoring coupling), determines a beam position and momentum at an element which best matches the measured positions at a series of specified elements */
public class OrbitMatcher {
	/** node for which we wish to determine the matching beam position and momentum */
	final AcceleratorNode TARGET_NODE;
	
	/** list of nodes for which we have measured beam positions */
	final List<? extends AcceleratorNode> MEASURED_NODES;
	
	/** trajectory from which to get the transfer matrices */
	protected Trajectory<TransferMapState> _trajectory;

	/** horizontal beam position transform */
	protected BeamPositionTransform _xBeamPositionTransform;
	
	/** vertical beam position transform */
	protected BeamPositionTransform _yBeamPositionTransform;


	/** Constructor */
	public OrbitMatcher( final AcceleratorNode targetNode, final List<? extends AcceleratorNode> measuredNodes, final Trajectory<TransferMapState> trajectory ) {
		TARGET_NODE = targetNode;
		MEASURED_NODES = measuredNodes;
	}

	
	/** get the best matching horizontal beam position in mm at the target node based on the beam position measurements in mm at the measurement nodes */
	public double getHorizontalTargetBeamPosition( final double[] measuredBeamPositions ) {
		return _xBeamPositionTransform.getTargetBeamPosition( measuredBeamPositions );
	}
	
	
	/** get the best matching vertical beam position in mm at the target node based on the beam position measurements in mm at the measurement nodes */
	public double getVerticalTargetBeamPosition( final double[] measuredBeamPositions ) {
		return _yBeamPositionTransform.getTargetBeamPosition( measuredBeamPositions );
	}
	
	
	/** set the trajectory */
	public void setTrajectory( final Trajectory<TransferMapState> trajectory ) {
		_trajectory = trajectory;
		
		final List<TransferRow> xTransferRows = new ArrayList<TransferRow>( MEASURED_NODES.size() );
		final List<TransferRow> yTransferRows = new ArrayList<TransferRow>( MEASURED_NODES.size() );

		for ( final AcceleratorNode node : MEASURED_NODES ) {
			// we need to get the transfer matrix from the target node to the measurement node (see the equations)
			final PhaseMatrix transferMatrix = getTransferMatrix( TARGET_NODE, node );
			xTransferRows.add( extractHorizontalSubMatrix( transferMatrix ) );
			yTransferRows.add( extractVerticalSubMatrix( transferMatrix ) );
		}
		_xBeamPositionTransform = new BeamPositionTransform( xTransferRows );
		_yBeamPositionTransform = new BeamPositionTransform( yTransferRows );
	}
	
	
	/** extract the horizontal sub matrix */
	static protected TransferRow extractHorizontalSubMatrix( final PhaseMatrix transferMatrix ) {
		final double t11 = transferMatrix.getElem( PhaseMatrix.IND_X, PhaseMatrix.IND_X );
		final double t12 = transferMatrix.getElem( PhaseMatrix.IND_X, PhaseMatrix.IND_XP );
		final double t13 = 1000 * transferMatrix.getElem( PhaseMatrix.IND_X, PhaseMatrix.IND_HOM );
		
		return new TransferRow( t11, t12, t13 );
	}
	
	
	/** extract the vertical sub matrix */
	static protected TransferRow extractVerticalSubMatrix( final PhaseMatrix transferMatrix ) {
		final double t11 = transferMatrix.getElem( PhaseMatrix.IND_Y, PhaseMatrix.IND_Y );
		final double t12 = transferMatrix.getElem( PhaseMatrix.IND_Y, PhaseMatrix.IND_YP );
		final double t13 = 1000 * transferMatrix.getElem( PhaseMatrix.IND_Y, PhaseMatrix.IND_HOM );
		
		return new TransferRow( t11, t12, t13 );
	}
	
	
	/** get the transfer matrix from the transfer map trajectory */
	protected PhaseMatrix getTransferMatrix( final AcceleratorNode fromNode, final AcceleratorNode toNode ) {
//		return _trajectory.getTransferMatrix( fromNode.getId(), toNode.getId() );
//		return _trajectory.getTransferMatrix( toNode.getId(), fromNode.getId() );
	    TransferMapState   S1 = this._trajectory.stateForElement(fromNode.getId());
	    TransferMapState   S2 = this._trajectory.stateForElement(toNode.getId() );
	    
	    PhaseMatrix    matPhi1 = S1.getTransferMap().getFirstOrder();
	    PhaseMatrix    matPhi2 = S2.getTransferMap().getFirstOrder();
	    
	    PhaseMatrix    matPhi1inv = matPhi1.inverse();
	    PhaseMatrix    matPhi21   = matPhi2.times( matPhi1inv );
	    
	    return matPhi21;
	}
}



/** best fit transform (in one plane) from a set of beam positions to a beam position at a specified point */
class BeamPositionTransform {
	final protected Matrix KICK_TRANSFORM;
	final protected Matrix PROJECTION_TRANSFORM;
	
	
	/** Constructor */
	public BeamPositionTransform( final List<TransferRow> transferRows ) {
		final int rowCount = transferRows.size();
		
		final Matrix kickTransform = new Matrix( rowCount, 1 );
		final Matrix phaseTransform = new Matrix( rowCount, 2 );
		
		int row = 0;
		for ( final TransferRow transferRow : transferRows ) {
			phaseTransform.set( row, 0, transferRow.T11 );
			phaseTransform.set( row, 1, transferRow.T12 );
			kickTransform.set( row, 0, transferRow.T13 );
			++row;
		}
		
		KICK_TRANSFORM = kickTransform;
		
		// projection transform:  (A<sup>T</sup> A)<sup>-1</sup> A<sup>T</sup>
		final Matrix phaseTransformTranspose = phaseTransform.transpose();
		PROJECTION_TRANSFORM = phaseTransformTranspose.times( phaseTransform ).inverse().times( phaseTransformTranspose );
	}
	
	
	/** get the best matching beam position in mm at the target node based on the beam position measurements in mm at the measurement nodes */
	public double getTargetBeamPosition( final double[] measuredBeamPositions ) {
		final Matrix beamPositionVector = new Matrix( measuredBeamPositions.length, 1 );
		
		for ( int row = 0 ; row < measuredBeamPositions.length ; row++ ) {
			beamPositionVector.set( row, 0, measuredBeamPositions[row] );
		}
		
		return PROJECTION_TRANSFORM.times( beamPositionVector.minus( KICK_TRANSFORM ) ).get( 0, 0 );
	}
}



/** three elements of the transfer matrix */
class TransferRow {
	final public double T11;
	final public double T12;
	final public double T13;
	
	
	/** Constructor */
	public TransferRow( final double t11, final double t12, final double t13 ) {
		T11 = t11;
		T12 = t12;
		T13 = t13;
	}
}



