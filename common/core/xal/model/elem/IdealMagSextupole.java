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



/** 
 * Sextupole magnets element
 *  
 *  Created by Thomas Pelaia on 10/12/05.
 *  Copyright 2005 Oak Ridge National Lab. All rights reserved.
 */
public class IdealMagSextupole extends ThinElement {
    /** type string identifier for all sextople elements */
    public static final String s_strType = "IdealMagSextupole";
    
    /** identity phase map */
    private static final PhaseMap IDENTITY_PHASE_MAP = PhaseMap.identity();
	
	
	/** 
	 *  Constructor
	 *  @param  strId   identifier of this object
	 */
    public IdealMagSextupole( final String strId ) {
        super( s_strType, strId );
    }
    
	
    /** Constructor */
    public IdealMagSextupole() {
        super( s_strType );
    }
    
    
    /**
	 * Returns the time taken for the probe to propagate through element.
     *  @param  probe   propagating probe
     *  @return         the value zero 
     */
    @Override
    public double elapsedTime( final IProbe probe )  {
        return 0.0;
    }

    
    /**
	 *  Returns energy gain which is zero.
     *  @param  probe   dummy argument
     *  @return         returns zero
     */
    @Override
    public double energyGain(IProbe probe) { return 0.0;  }
	
    
    /**  
	 *  Returns the transfer map of this element which is the identity.
	 *  @param  probe   dummy argument
	 *  @return         the identity phase map
	 *  @exception  PropagationException  this should not occur
	 */
    @Override
    protected PhaseMap transferMap(IProbe probe) throws PropagationException { return IDENTITY_PHASE_MAP; }
}
