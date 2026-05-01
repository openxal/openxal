//
//  CoordinateMap.java
//  xal
//
//  Created by Tom Pelaia on 5/9/07.
//  Copyright 2007 Oak Ridge National Lab. All rights reserved.
//

package xal.extension.orbit;

import java.util.*;

import xal.smf.*;
import xal.model.IElement;
import xal.model.alg.*;
import xal.model.probe.*;
import xal.sim.scenario.*;
import xal.model.probe.traj.*;
import xal.smf.impl.*;
import xal.tools.beam.PhaseMatrix;
import xal.tools.beam.calc.*;


/** maps coordinates from two nodes to a third node */
public class CoordinateMap {
	/** Accelerator sequence */
	final protected AcceleratorSeq SEQUENCE;

	/** first reference node */
	final protected AcceleratorNode NODE_A;

	/** second reference node */
	final protected AcceleratorNode NODE_B;

	/** the element to which to transfer coordinate information */
	final protected AcceleratorNode TARGET_NODE;

	/** horizontal transfer map */
	final protected double[] X_TRANSFER_MAP;

	/** vertical transfer map */
	final protected double[] Y_TRANSFER_MAP;

	/** horizontal angle transfer map */
	final protected double[] XP_TRANSFER_MAP;

	/** vertical angle transfer map */
	final protected double[] YP_TRANSFER_MAP;

	/** scenario used to get the node to element map */
	//private Scenario _scenario;	// Hopefully this can be removed if the mapping can be done internally in Trajectory


	/**
	 * Primary Constructor
	 * @param targetNode accelerator node for which we wish to get the coordinates
	 * @param nodeA first accelerator node for which we have a position
	 * @param nodeB second accelerator node for which we have a position
	 * @param trajectory initial online model trajectory to use for generating the maps to the target node
	 * @param sequence accelerator sequence which is necessary to handle the ring correctly
	 */
	public CoordinateMap( final AcceleratorNode targetNode, final AcceleratorNode nodeA, final AcceleratorNode nodeB, final Trajectory<TransferMapState> trajectory, final AcceleratorSeq sequence ) {
		SEQUENCE = sequence;

		TARGET_NODE = targetNode;
		NODE_A = nodeA;
		NODE_B = nodeB;

		X_TRANSFER_MAP = new double[] { 0.5, 0.5, 0.0 };
		Y_TRANSFER_MAP = new double[] { 0.5, 0.5, 0.0 };
		XP_TRANSFER_MAP = new double[] { 0.0, 0.0, 0.0 };
		YP_TRANSFER_MAP = new double[] { 0.0, 0.0, 0.0 };

		if ( trajectory != null ) {
			setTrajectory( trajectory );
		}
	}


	/**
	 * Constructor suitable for a linear sequence
	 * @param targetNode accelerator node for which we wish to get the coordinates
	 * @param nodeA first accelerator node for which we have a position
	 * @param nodeB second accelerator node for which we have a position
	 * @param trajectory online model trajectory to use for generating the maps to the target node
	 */
	public CoordinateMap( final AcceleratorNode targetNode, final AcceleratorNode nodeA, final AcceleratorNode nodeB, final Trajectory<TransferMapState> trajectory ) {
		this( targetNode, nodeA, nodeB, trajectory, null );
	}


	/**
	 * Get a new instance of this class without setting the trajectory
	 * @param targetNode accelerator node for which we wish to get the coordinates
	 * @param nodeA first accelerator node for which we have a position
	 * @param nodeB second accelerator node for which we have a position
	 * @param sequence accelerator sequence which is necessary to handle the ring correctly
	 */
	static public CoordinateMap getInstance( final AcceleratorNode targetNode, final AcceleratorNode nodeA, final AcceleratorNode nodeB, final AcceleratorSeq sequence ) {
		return new CoordinateMap( targetNode, nodeA, nodeB, null, sequence );
	}


	/**
	 * get the horizontal coordinate in millimeters at the target node based on coordinates at the two reference nodes
	 * @param x1 horizontal coordinate at node A in millimeters
	 * @param x2 horizontal coordinate at node B in millimeters
	 * @return horizontal coordinate at the target in millimeters
	 */
	public double getX( final double x1, final double x2 ) {
		return getCoordinate( X_TRANSFER_MAP, x1, x2 );
	}


	/**
	 * get the vertical coordinate in millimeters at the target node based on coordinates at the two reference nodes
	 * @param y1 vertical coordinate at node A in millimeters
	 * @param y2 vertical coordinate at node B in millimeters
	 * @return vertical coordinate at the target in millimeters
	 */
	public double getY( final double y1, final double y2 ) {
		return getCoordinate( Y_TRANSFER_MAP, y1, y2 );
	}


	/**
	 * get the horizontal angle in millimeters at the target node based on coordinates at the two reference nodes
	 * @param x1 horizontal coordinate at node A in millimeters
	 * @param x2 horizontal coordinate at node B in millimeters
	 * @return horizontal angle at the target in milliradians
	 */
	public double getXAngle( final double x1, final double x2 ) {
		return getCoordinate( XP_TRANSFER_MAP, x1,  x2 );
	}


	/**
	 * get the vertical angle in millimeters at the target node based on coordinates at the two reference nodes
	 * @param y1 vertical coordinate at node A in millimeters
	 * @param y2 vertical coordinate at node B in millimeters
	 * @return vertical angle at the target in milliradians
	 */
	public double getYAngle( final double y1, final double y2 ) {
		return getCoordinate( YP_TRANSFER_MAP, y1,  y2 );
	}


	/** get the target coordinate from the coordinates in the two reference nodes */
	static private double getCoordinate( final double[] transferMap, final double q1, final double q2 ) {
		return q1 * transferMap[0] + q2 * transferMap[1] + transferMap[2];
	}


	/** set the trajectory */
	public void setTrajectory( final Trajectory<TransferMapState> trajectory ) {
		// get the transfer matrix from A to B
		final PhaseMatrix transferAB = getTransferMatrix( trajectory, NODE_A, NODE_B );

		// get the transfer matrix from A to the target
		final PhaseMatrix transferATarget = getTransferMatrix( trajectory, NODE_A, TARGET_NODE );

		generateXTransferMap( transferAB, transferATarget );
		generateYTransferMap( transferAB, transferATarget );
		generateXPTransferMap( transferAB, transferATarget );
		generateYPTransferMap( transferAB, transferATarget );
	}


	/** Set the scenario used to get the node to element map. Hopefully this method can rbe removed if the mapping takes place internally in Trajectory. */
//	public void setScenario( final Scenario scenario ) {
//		_scenario = scenario;
//	}


	/** generate the horizontal transfer map */
	private void generateXTransferMap( final PhaseMatrix transferAB, final PhaseMatrix transferATarget ) {
		// get the transfer matrix elements for the transfer between A and the target
		final double r11 = transferATarget.getElem( PhaseMatrix.IND_X, PhaseMatrix.IND_X );
		final double r12 = transferATarget.getElem( PhaseMatrix.IND_X, PhaseMatrix.IND_XP );
		final double r13 = transferATarget.getElem( PhaseMatrix.IND_X, PhaseMatrix.IND_HOM );

		// get the transfer matrix elements for the transfer between A and B
		final double t11 = transferAB.getElem( PhaseMatrix.IND_X, PhaseMatrix.IND_X );
		final double t12 = transferAB.getElem( PhaseMatrix.IND_X, PhaseMatrix.IND_XP );
		final double t13 = transferAB.getElem( PhaseMatrix.IND_X, PhaseMatrix.IND_HOM );

		X_TRANSFER_MAP[0] = r11 - r12 * t11 / t12;
		X_TRANSFER_MAP[1] = r12 / t12;
		X_TRANSFER_MAP[2] = 1000 * ( r13 - r12 * t13 / t12 );
	}


	/** generate the vertical transfer map */
	private void generateYTransferMap( final PhaseMatrix transferAB, final PhaseMatrix transferATarget ) {
		// get the transfer matrix elements for the transfer between A and the target
		final double r11 = transferATarget.getElem( PhaseMatrix.IND_Y, PhaseMatrix.IND_Y );
		final double r12 = transferATarget.getElem( PhaseMatrix.IND_Y, PhaseMatrix.IND_YP );
		final double r13 = transferATarget.getElem( PhaseMatrix.IND_Y, PhaseMatrix.IND_HOM );

		// get the transfer matrix elements for the transfer between A and B
		final double t11 = transferAB.getElem( PhaseMatrix.IND_Y, PhaseMatrix.IND_Y );
		final double t12 = transferAB.getElem( PhaseMatrix.IND_Y, PhaseMatrix.IND_YP );
		final double t13 = transferAB.getElem( PhaseMatrix.IND_Y, PhaseMatrix.IND_HOM );

		Y_TRANSFER_MAP[0] = r11 - r12 * t11 / t12;
		Y_TRANSFER_MAP[1] = r12 / t12;
		Y_TRANSFER_MAP[2] = 1000 * ( r13 - r12 * t13 / t12 );
	}


	/** generate the horizontal angle transfer map */
	private void generateXPTransferMap( final PhaseMatrix transferAB, final PhaseMatrix transferATarget ) {
		// get the transfer matrix elements for the transfer between A and the target
		final double r21 = transferATarget.getElem( PhaseMatrix.IND_XP, PhaseMatrix.IND_X );
		final double r22 = transferATarget.getElem( PhaseMatrix.IND_XP, PhaseMatrix.IND_XP );
		final double r23 = transferATarget.getElem( PhaseMatrix.IND_XP, PhaseMatrix.IND_HOM );

		// get the transfer matrix elements for the transfer between A and B
		final double t11 = transferAB.getElem( PhaseMatrix.IND_X, PhaseMatrix.IND_X );
		final double t12 = transferAB.getElem( PhaseMatrix.IND_X, PhaseMatrix.IND_XP );
		final double t13 = transferAB.getElem( PhaseMatrix.IND_X, PhaseMatrix.IND_HOM );

		XP_TRANSFER_MAP[0] = r21 - r22 * t11 / t12;
		XP_TRANSFER_MAP[1] = r22 / t12;
		XP_TRANSFER_MAP[2] = 1000 * ( r23 - r22 * t13 / t12 );
	}


	/** generate the vertical transfer map */
	private void generateYPTransferMap( final PhaseMatrix transferAB, final PhaseMatrix transferATarget ) {
		// get the transfer matrix elements for the transfer between A and the target
		final double r21 = transferATarget.getElem( PhaseMatrix.IND_YP, PhaseMatrix.IND_Y );
		final double r22 = transferATarget.getElem( PhaseMatrix.IND_YP, PhaseMatrix.IND_YP );
		final double r23 = transferATarget.getElem( PhaseMatrix.IND_YP, PhaseMatrix.IND_HOM );

		// get the transfer matrix elements for the transfer between A and B
		final double t11 = transferAB.getElem( PhaseMatrix.IND_Y, PhaseMatrix.IND_Y );
		final double t12 = transferAB.getElem( PhaseMatrix.IND_Y, PhaseMatrix.IND_YP );
		final double t13 = transferAB.getElem( PhaseMatrix.IND_Y, PhaseMatrix.IND_HOM );

		YP_TRANSFER_MAP[0] = r21 - r22 * t11 / t12;
		YP_TRANSFER_MAP[1] = r22 / t12;
		YP_TRANSFER_MAP[2] = 1000 * ( r23 - r22 * t13 / t12 );
	}


	/** get the transfer matrix from the transfer map trajectory */
	private PhaseMatrix getTransferMatrix( final Trajectory<TransferMapState> trajectory, final AcceleratorNode fromNode, final AcceleratorNode toNode ) {
		if ( SEQUENCE.isLinear() ) {
			return getLinearTransferMatrix( trajectory, fromNode, toNode );
		}
		else {
			return getRingTransferMatrix( trajectory, fromNode, toNode );
		}
	}


	/** Get the element ID corresponding to the specified node */
	private String getElementIdForNode( final AcceleratorNode node ) {
//		if ( _scenario != null ) {
//			final List<IElement> elements = _scenario.elementsMappedTo( node );
//			if ( elements != null && elements.size() > 0 ) {
//				return elements.get( 0 ).getId();
//			}
//			else {
//				return null;
//			}
//		}
//		else {
//			return node.getId();
//		}
		return node.getId();
	}


	/** get the envelope probe state in the trajectory for the specified node */
	private TransferMapState getProbeState( final Trajectory<TransferMapState> trajectory, final AcceleratorNode node ) {
//		final Iterator<TransferMapState> stateIter = trajectory.stateIterator();
//		while( stateIter.hasNext() ) {
//			TransferMapState state = stateIter.next();
//			System.out.println( "state ID: " + state.getElementId() );
//		}

		final String elementID = getElementIdForNode( node );
//		System.out.println( "Getting state for element: " + elementID );
//		System.out.println( "State for element: " + elementID + " = " + trajectory.stateForElement( elementID ) );
		final TransferMapState state = elementID != null ? trajectory.stateForElement( elementID ) : null;
		return state;

//		if ( elementID != null ) {
//			final ProbeState[] states = trajectory.statesForElement_OLD( elementID );
//			final TransferMapState state = (TransferMapState)states[0];
//			return state;
//		}
//		else {
//			return null;
//		}
	}


	/** get the transfer matrix from the  envelope trajectory */
	private PhaseMatrix getLinearTransferMatrix( final Trajectory<TransferMapState> trajectory, final AcceleratorNode fromNode, final AcceleratorNode toNode ) {
		final TransferMapState fromState = getProbeState( trajectory, fromNode );
		final TransferMapState toState = getProbeState( trajectory, toNode );

		return CalculationsOnMachines.computeTransferMatrix( fromState, toState	);
	}


	/**
	 * Get the transfer matrix from the transfer map trajectory along the shortest path along the ring between the two nodes. Care must be taken to
	 * make sure that the coordinates are consistent at the location accounting for winding number (not just absolute position within the ring).
	 * @param trajectory online model trajectory to use for generating the maps to the target node
	 * @param fromNode starting node
	 * @param toNode ending node
	 */
	private PhaseMatrix getRingTransferMatrix( final Trajectory<TransferMapState> trajectory, final AcceleratorNode fromNode, final AcceleratorNode toNode ) {
		final PhaseMatrix fromMatrix = getProbeState( trajectory, fromNode ).getTransferMap().getFirstOrder();
		final PhaseMatrix toMatrix = getProbeState( trajectory, toNode ).getTransferMap().getFirstOrder();

		// shortest distance on the ring between "to" and "from" nodes where it is positive if the "from" node position is greater than the "to" node position and negative otherwise
		final double distance = SEQUENCE.getShortestRelativePosition( fromNode, toNode );	// reference node is the "to" node
		final double toLocation = SEQUENCE.getPosition( toNode );	// postion of the "to" node with respect to the sequence origin
		final double fromPath = toLocation + distance;	// path to the "from" node relative to origin along the shortest path to the "to" node

		// Xo:  coordinate at the origin during the current turn
		// Xp:  coordinate at the origin during the previous turn
		// Xf:  coordinate at the "from" node
		// Xt:  coordinate at the "to" node
		// F:   full turn map at the origin
		// Tf:  Transfer matrix from the origin to the "from" node
		// Tt:  Transfer matrix from the origin to the "to" node
		// Tft: Transfer matrix (what we want) from the "from" node to the "to" node along the shortest path between them
		// Xf = Tf * Xo, Xt = Tt * Xo
		if ( fromPath < 0.0 ) {	// "from" node is across the origin near the end of the sequence, and the "to" node is near the front of the sequence
			// Xo = F * Xp, Xf = Tf * Xp, Xt = Tt * Xo  ->  Xp = Tf^-1 * Xf  ->  Xo = F * Tf^-1 * Xf  ->  Tft = Tt * F * Tf^-1
			final PhaseMatrix fullTurnOriginMatrix = new CalculationsOnRings( trajectory ).getFullTransferMap().getFirstOrder();
			//final PhaseMatrix fullTurnOriginMatrix = trajectory.getFullTurnMapAtOrigin().getFirstOrder();
			return toMatrix.times( fullTurnOriginMatrix ).times( fromMatrix.inverse() );
		}
		else if ( fromPath > SEQUENCE.getLength() ) { // "from" node is across the origin near the front of the sequence, and the "to" node is near the end of the sequence
			// Xo = F * Xp, Xf = Tf * Xo, Xt = Tt * Xp  ->  Xf = Tf * F * Xp  ->  Xp = (Tf * F)^-1 * Xf  ->  Xt = Tt * (Tf * F)^-1 * Xf  ->  Tft = Tt * (Tf * F)^-1
			final TransferMapState originState = trajectory.initialState();
			final PhaseMatrix fullTurnOriginMatrix = new CalculationsOnRings( trajectory ).getFullTransferMap().getFirstOrder();
			//final PhaseMatrix fullTurnOriginMatrix = trajectory.getFullTurnMapAtOrigin().getFirstOrder();
			return toMatrix.times( fromMatrix.times( fullTurnOriginMatrix ).inverse() );
		}
		else {  // "from" and "to" nodes are on the same side of the origin (i.e. shortest path between them along the ring does not cross the origin of the sequence)
			// Xo = Tf^-1 * Xf  ->  Xt = Tt * Tf^-1 * Xf  ->  Tft = Tt * Tf^-1
			return getTransferMatrix( fromMatrix, toMatrix );
		}
	}


	/** get the transfer matrix between the two response matricies */
	static private PhaseMatrix getTransferMatrix( final PhaseMatrix fromMatrix, final PhaseMatrix toMatrix ) {
		return toMatrix.times( fromMatrix.inverse() );
	}
}
