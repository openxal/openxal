//
//  BumpShapeAdaptor.java
//  xal
//
//  Created by Thomas Pelaia on 3/15/06.
//  Copyright 2006 Oak Ridge National Lab. All rights reserved.
//

package xal.app.knobs;

import xal.model.probe.traj.ProbeState;
import xal.model.probe.traj.Trajectory;
import xal.tools.beam.calc.SimResultsAdaptor;



/** adaptor for generating bumps of specific shapes */
abstract public class BumpShapeAdaptor {
    /** bump offset adaptor */
	static protected BumpOffsetAdaptor _bumpOffsetAdaptor;
	
	/** bump angle adaptor */
	static protected BumpAngleAdaptor _bumpAngleAdaptor;


	/** get the bump offset adaptor instance */
	static public BumpOffsetAdaptor getBumpOffsetAdaptor() {
		if ( _bumpOffsetAdaptor == null ) {
			_bumpOffsetAdaptor = new BumpOffsetAdaptor();
		}
		
		return _bumpOffsetAdaptor;
	}
	
	/** get the bump angle adaptor instance */
	static public BumpAngleAdaptor getBumpAngleAdaptor() {
		if ( _bumpAngleAdaptor == null ) {
			_bumpAngleAdaptor = new BumpAngleAdaptor();
		}
		
		return _bumpAngleAdaptor;
	}
	
	
    /** get the label */
    abstract public String getLabel();
    
    
    /** get the minimum number of elements for the particular shape */
    abstract public int getMinimumElementCount();
    
    
    /** get the amplitude scale of the distortion per unit of knob */
    abstract public double getAmplitudeScale();
    
    
    /** get the orbit */
    abstract public double[] getOrbit( final SimResultsAdaptor simulator, final PlaneAdaptor planeAdaptor, final ProbeState<?> bumpState, final ProbeState<?> endState, final int elementCount );

    
    /** get the orbit size for the specified element count */
    public int getOrbitSize( final int elementCount ) {
        return Math.min( elementCount, 4 );
    }
}



/** adaptor for generating bumps with an offset at the bump node */
class BumpOffsetAdaptor extends BumpShapeAdaptor {
	/** get the label */
	public String getLabel() {
		return "Offset";
	}
	
	
	/** get the minimum number of elements for the particular shape */
	public int getMinimumElementCount() {
		return 3;
	}
	
	
	/** get the amplitude scale of the distortion per unit of knob */
	public double getAmplitudeScale() {
		return 0.001;	// 1 millimeter per unit of knob
	}
	
	
    /** get the orbit */
    public double[] getOrbit( final SimResultsAdaptor simulator, final PlaneAdaptor planeAdaptor, final ProbeState<?> bumpState, final ProbeState<?> endState, final int elementCount ) {
        final int orbitSize = getOrbitSize( elementCount );
        final double[] orbit = new double[orbitSize];   // bump offset, end offset and end angle and possibly the bump angle
        
        orbit[0] = planeAdaptor.getOffset( simulator.computeFixedOrbit( bumpState ) );
        orbit[1] = planeAdaptor.getOffset( simulator.computeFixedOrbit( endState ) );
        orbit[2] = planeAdaptor.getAngle( simulator.computeFixedOrbit( endState ) );
        
        if ( orbitSize > 3 ) {
            orbit[3] = planeAdaptor.getAngle( simulator.computeFixedOrbit( bumpState ) );
        }
        
        return orbit;
    }
}



/** adaptor for generating bumps with an angle at the bump node */
class BumpAngleAdaptor extends BumpShapeAdaptor {
	/** get the label */
	public String getLabel() {
		return "Angle";
	}
	
	
	/** get the minimum number of elements for the particular shape */
	public int getMinimumElementCount() {
		return 5;
	}
	
	
	/** get the amplitude scale of the distortion per unit of knob */
	public double getAmplitudeScale() {
		return 0.001;	// 1 milliradian per unit of knob
	}
	
	
    /** get the orbit */
    public double[] getOrbit( final SimResultsAdaptor simulator, final PlaneAdaptor planeAdaptor, final ProbeState<?> bumpState, final ProbeState<?> endState, final int elementCount ) {
        final int orbitSize = getOrbitSize( elementCount );
        final double[] orbit = new double[orbitSize];   // bump angle, end offset and end angle and possibly bump offset
        
        orbit[0] = planeAdaptor.getAngle( simulator.computeFixedOrbit( bumpState ) );
        orbit[1] = planeAdaptor.getOffset( simulator.computeFixedOrbit( endState ) );
        orbit[2] = planeAdaptor.getAngle( simulator.computeFixedOrbit( endState ) );
        
        if ( orbitSize > 3 ) {
            orbit[3] = planeAdaptor.getOffset( simulator.computeFixedOrbit( bumpState ) );
        }
        
        return orbit;
    }
}
