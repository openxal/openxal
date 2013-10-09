//
//  IdealMagSextupole.java
//  xal
//
//  Created by Thomas Pelaia on 10/12/05.
//  Copyright 2005 Oak Ridge National Lab. All rights reserved.
//

package xal.model.elem;

import xal.model.IProbe;
import xal.model.PropagationException;
import xal.tools.beam.PhaseMap;
import xal.tools.beam.PhaseMatrix;



/** Sextupole magnets element */
public class IdealMagSextupole extends ThickElement {
    /** type string identifier for all sextople elements */
    public static final String s_strType = "IdealMagSextupole";
	
	
	/** 
	 * Constructor
	 * @param  strID identifier of this object
	 * @param length magnetic length of the magnet
	 */
    public IdealMagSextupole( final String strID, final double length ) {
        super( s_strType, strID, length );
    }
    
	
    /** Constructor */
    public IdealMagSextupole() {
        super( s_strType );
    }
    
    
    /**
	 * Determine the time taken for the probe to propagate through element.
	 * @param probe probe at the entrance to this element
	 * @param length of the magnet
     * @return         the value zero 
     */
    public double elapsedTime( final IProbe probe, final double length )  {
        return super.compDriftingTime( probe, length );
    }

    
    /**
	 * Determine energy gain which is zero.
	 * @param probe probe at the entrance to this element
	 * @param length of the magnet
     * @return         returns zero
     */
    public double energyGain( final IProbe probe, final double length ) { return 0.0;  }
	
    
    /**  
	 * Determine the transfer map of this element which is simply treated as a drift.
	 * @param probe probe at the entrance to this element
	 * @param length of the magnet
	 * @return transfer map
	 * @exception  PropagationException  this should not occur
	 */
    public PhaseMap transferMap( final IProbe probe, final double length ) throws PropagationException { 
        // Build transfer matrix
        PhaseMatrix  matPhi  = new PhaseMatrix();
        
		// one dimensional drift matrix
        double mat0[][] = new double [][] { { 1.0, length }, { 0.0, 1.0 } };
		
		// set the block diagonals to the one dimensional drift matricies
        matPhi.setSubMatrix( 0, 1, 0, 1, mat0 );
        matPhi.setSubMatrix( 2, 3, 2 ,3, mat0 );
        matPhi.setSubMatrix( 4, 5, 4, 5, mat0 );
        matPhi.setElem( 6, 6, 1.0 );	// inhomogeneous term is always 1.0
		
        return new PhaseMap( matPhi );
	}
}
