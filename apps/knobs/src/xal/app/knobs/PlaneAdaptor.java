//
//  PlaneAdaptor.java
//  xal
//
//  Created by Thomas Pelaia on 3/9/06.
//  Copyright 2006 Oak Ridge National Lab. All rights reserved.
//

package xal.app.knobs;

import xal.tools.beam.PhaseVector;
import xal.smf.AcceleratorSeq;
import xal.smf.AcceleratorNode;
import xal.smf.impl.*;

import java.util.List;
import java.util.ArrayList;


/** Adaptor for horizontal or vertical operations */
abstract public class PlaneAdaptor {
	protected static PlaneAdaptor HORIZONTAL_ADAPTOR;
	protected static PlaneAdaptor VERTICAL_ADAPTOR;
	
	
	/** get the horizontal plane adaptor */
	static public PlaneAdaptor getHorizontalAdaptor() {
		if ( HORIZONTAL_ADAPTOR == null ) {
			HORIZONTAL_ADAPTOR = new HorizontalAdaptor();
		}
		
		return HORIZONTAL_ADAPTOR;
	}
	
	
	/** get the vertical plane adaptor */
	static public PlaneAdaptor getVerticalAdaptor() {
		if ( VERTICAL_ADAPTOR == null ) {
			VERTICAL_ADAPTOR = new VerticalAdaptor();
		}
		
		return VERTICAL_ADAPTOR;
	}
	
	
	/** get the name for the plane */
	abstract public String planeName();
	
	
	/** get the short name for the plane */
	abstract public String shortPlaneName();
	
	
	/** get the offset from the phase vector */
	abstract public double getOffset( final PhaseVector phaseVector );
	
	
	/** get the angle from the phase vector */
	abstract public double getAngle( final PhaseVector phaseVector );
	
	
	/** get list of correctors from the sequence */
	abstract public List<Dipole> getCorrectors( final AcceleratorSeq sequence );
	
	
	
	/** adaptor for the horizontal plane */
	static protected class HorizontalAdaptor extends PlaneAdaptor {
		/** Get the name for this plane. */
		public String planeName() {
			return "Horizontal";
		}
		
		
		/** Get the short name for this plane */
		public String shortPlaneName() {
			return "H";
		}
				
		
		/** get the offset from the phase vector */
		public double getOffset( final PhaseVector phaseVector ) {
			return phaseVector.getx();
		}
		
		
		/** get the angle from the phase vector */
		public double getAngle( final PhaseVector phaseVector ) {
			return phaseVector.getxp();
		}
		
		
		/** get list of correctors from the sequence */
		public List<Dipole> getCorrectors( final AcceleratorSeq sequence ) {
			final List<AcceleratorNode> nodes = sequence.getNodesOfType( HDipoleCorr.s_strType, true );
			final List<Dipole> correctors = new ArrayList<Dipole>( nodes.size() );
			for ( AcceleratorNode node : nodes ) {
				correctors.add( (Dipole)node );
			}
			return correctors;
		}
	}
	
	
	/** adaptor for the vertical plane */
	static protected class VerticalAdaptor extends PlaneAdaptor {
		/** Get the name for this plane. */
		public String planeName() {
			return "Vertical";
		}
		
		
		/** Get the short name for this plane */
		public String shortPlaneName() {
			return "V";
		}
		
		
		/** get the offset from the phase vector */
		public double getOffset( final PhaseVector phaseVector ) {
			return phaseVector.gety();
		}
		
		
		/** get the angle from the phase vector */
		public double getAngle( final PhaseVector phaseVector ) {
			return phaseVector.getyp();
		}
		
		
		/** get list of correctors from the sequence */
		public List<Dipole> getCorrectors( final AcceleratorSeq sequence ) {
			final List<AcceleratorNode> nodes = sequence.getNodesOfType( VDipoleCorr.s_strType, true );
			final List<Dipole> correctors = new ArrayList<Dipole>( nodes.size() );
			for ( AcceleratorNode node : nodes ) {
				correctors.add( (Dipole)node );
			}
			return correctors;
		}
	}
}
