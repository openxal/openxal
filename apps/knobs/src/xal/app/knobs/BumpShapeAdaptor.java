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
import xal.tools.beam.calc.SimpleSimResultsAdaptor;



/** adaptor for generating bumps of specific shapes */
abstract public class BumpShapeAdaptor {


    /*
     * Global Attributes
     */
    
    /** bump offset adaptor */
	static protected BumpOffsetAdaptor _bumpOffsetAdaptor;
	
	/** bump angle adaptor */
	static protected BumpAngleAdaptor _bumpAngleAdaptor;
	
	
	/*
	 * Global Methods
	 */
	
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
	
	
    /*
     * Abstract Methods
     */
    
    /** get the label */
    abstract public String getLabel();
    
    
    /** get the minimum number of elements for the particular shape */
    abstract public int getMinimumElementCount();
    
    
    /** get the amplitude scale of the distortion per unit of knob */
    abstract public double getAmplitudeScale();
    
    
    /** get the orbit */
//    abstract public double[] getOrbit( final PlaneAdaptor planeAdaptor, final IPhaseState bumpState, final IPhaseState endState, final int elementCount );
    abstract public double[] getOrbit( final PlaneAdaptor planeAdaptor, final ProbeState<?> bumpState, final ProbeState<?> endState, final int elementCount );


    /*
     * Local Attributes
     */
    
    /** the simulation data processor */
    protected SimpleSimResultsAdaptor prcSimData;
    
    
	/*
	 * Local Methods
	 */
	
    /** get the orbit size for the specified element count */
    public int getOrbitSize( final int elementCount ) {
        return Math.min( elementCount, 4 );
    }
    
    /**
     * Every time a new set of simulation results is going to be
     * processed, this method must first be called to reset the
     * simulation processor.
     * 
     * @param traj  simulation data to be processed using the methods of this object
     *
     * @author Christopher K. Allen
     * @since  Nov 12, 2013
     */
    public void resetTrajectory(Trajectory<?> traj) {
        this.prcSimData = new SimpleSimResultsAdaptor(traj);
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
	
	
//	/** get the orbit */
//	public double[] getOrbit( final PlaneAdaptor planeAdaptor, final IPhaseState bumpState, final IPhaseState endState, final int elementCount ) {
//		final int orbitSize = getOrbitSize( elementCount );
//		final double[] orbit = new double[orbitSize];	// bump offset, end offset and end angle and possibly the bump angle
//		
//		orbit[0] = planeAdaptor.getOffset( bumpState.getFixedOrbit() );
//		orbit[1] = planeAdaptor.getOffset( endState.getFixedOrbit() );
//		orbit[2] = planeAdaptor.getAngle( endState.getFixedOrbit() );
//		
//		if ( orbitSize > 3 ) {
//			orbit[3] = planeAdaptor.getAngle( bumpState.getFixedOrbit() );
//		}
//		
//		return orbit;
//	}

    /** get the orbit */
    public double[] getOrbit( final PlaneAdaptor planeAdaptor, final ProbeState<?> bumpState, final ProbeState<?> endState, final int elementCount ) {
        final int orbitSize = getOrbitSize( elementCount );
        final double[] orbit = new double[orbitSize];   // bump offset, end offset and end angle and possibly the bump angle
        
        orbit[0] = planeAdaptor.getOffset( this.prcSimData.computeFixedOrbit(bumpState) );
        orbit[1] = planeAdaptor.getOffset( this.prcSimData.computeFixedOrbit(endState) );
        orbit[2] = planeAdaptor.getAngle( this.prcSimData.computeFixedOrbit(endState) );
        
        if ( orbitSize > 3 ) {
            orbit[3] = planeAdaptor.getAngle( this.prcSimData.computeFixedOrbit(bumpState) );
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
	
	
//	/** get the orbit */
//	public double[] getOrbit( final PlaneAdaptor planeAdaptor, final IPhaseState bumpState, final IPhaseState endState, final int elementCount ) {
//		final int orbitSize = getOrbitSize( elementCount );
//		final double[] orbit = new double[orbitSize];	// bump angle, end offset and end angle and possibly bump offset
//		
//		orbit[0] = planeAdaptor.getAngle( bumpState.getFixedOrbit() );
//		orbit[1] = planeAdaptor.getOffset( endState.getFixedOrbit() );
//		orbit[2] = planeAdaptor.getAngle( endState.getFixedOrbit() );
//		
//		if ( orbitSize > 3 ) {
//			orbit[3] = planeAdaptor.getOffset( bumpState.getFixedOrbit() );
//		}
//		
//		return orbit;
//	}

    /** get the orbit */
    public double[] getOrbit( final PlaneAdaptor planeAdaptor, final ProbeState<?> bumpState, final ProbeState<?> endState, final int elementCount ) {
        final int orbitSize = getOrbitSize( elementCount );
        final double[] orbit = new double[orbitSize];   // bump angle, end offset and end angle and possibly bump offset
        
        orbit[0] = planeAdaptor.getAngle( this.prcSimData.computeFixedOrbit(bumpState) );
        orbit[1] = planeAdaptor.getOffset( this.prcSimData.computeFixedOrbit(endState) );
        orbit[2] = planeAdaptor.getAngle( this.prcSimData.computeFixedOrbit(endState) );
        
        if ( orbitSize > 3 ) {
            orbit[3] = planeAdaptor.getOffset( this.prcSimData.computeFixedOrbit(bumpState) );
        }
        
        return orbit;
    }
}
