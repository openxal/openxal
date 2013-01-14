/*
 * Ring.java
 *
 * Created on Mon May 17 16:46:20 EDT 2004
 *
 * Copyright (c) 2004 Spallation Neutron Source
 * Oak Ridge National Laboratory
 * Oak Ridge, TN 37830
 */

package xal.smf;

import xal.tools.data.*;

import java.util.*;


/**
 * Ring is a subclass of combo sequence that is intended to support the special needs of a Ring.
 * A ring is made up of ring segments.
 *
 * @author  tap
 */
public class Ring extends AcceleratorSeqCombo {
    /** node type */
    public static final String    s_strType = "Ring";
	
	
	/**
	 * Primary Constructor
	 */
    public Ring( final String strID, final List<AcceleratorSeq> segments) {
		super( strID, segments );
	}
	
	
    /** Constructor */
    public Ring( final String strID, final Accelerator accelerator, final DataAdaptor adaptor ) {
        this( strID, getSequences(accelerator, adaptor) );
    }
    
    
    /**
     * Identify whether the sequence is within a linear section.  This helps 
     * us to determine whether it is meaningful to identify one node as being 
     * downstream from another.
	 * @return false since by definition the ring is not a linear section
     */
    public boolean isLinear() {
        return false;
    }
	
	
	/**
	 * Convert the sequence position to a position relative to the specified reference node.
	 * @param position the position of a location relative to the sequence's start
	 * @param referenceNode the node relative to which we wish to get the position
	 */
	public double getRelativePosition( final double position, final AcceleratorNode referenceNode ) {
		final double relativePosition = position - getPosition( referenceNode );
		return ( relativePosition >= 0 ) ? relativePosition : getLength() + relativePosition;
	}
	
	
	/**
	 * Get the shortest relative postion of one node with respect to a reference node.  This is really useful for ring sequences.
	 * @param node the node whose relative position is sought
	 * @param referenceNode the reference node relative to which the node's position is calculated
	 * @return the distance (positive or negative) of the node with respect to the reference node whose magnitude is shortest 
	 */
	public double getShortestRelativePosition( final AcceleratorNode node, final AcceleratorNode referenceNode ) {
		final double distanceTo = Math.abs( getDistanceBetween( node, referenceNode ) );
		final double distanceFrom = Math.abs( getDistanceBetween( referenceNode, node ) );
		return distanceTo < distanceFrom ? - distanceTo : distanceFrom;
	}
}

