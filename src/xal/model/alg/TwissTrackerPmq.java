/*
 * TwissTrackerPmq.java
 *
 *  Created : December, 2006
 *  Author  : Christopher K. Allen
 *  
 *  Modified:
 *
 */
 
package xal.model.alg;


import xal.model.IElement;
import xal.model.IProbe;
import xal.model.ModelException;

import xal.model.elem.IdealDrift;
import xal.model.elem.IdealPermMagQuad;

import xal.model.probe.TwissProbe;


/**
 * <p>
 * This class is a super class of the <code>TwissTracker</code> class meant to 
 * handle the special case of <code>IdealPermMagQuad</code> elements.  There really
 * is not much extra to do here, just look for the exception element and adjust the
 * number of integration steps accordingly.
 * </p>
 *<p>
 * See the <code>TwissTracker</code> class for a complete description of the
 * functionality
 * </p>
 * 
 * @see xal.model.alg.TwissTracker 
 *
 * @author Christopher K. Allen
 * @since  December, 2006
 */
public class TwissTrackerPmq extends TwissTracker {

    
    /*
     * Global Constants
     */
    
    
    /*
     *  Global Attributes
     */
    
    /** string type identifier for algorithm */
    public static final String      s_strTypeId = TwissTrackerPmq.class.getName();
    
    /** current algorithm version */
    public static final int         s_intVersion = 1;
    
    /** probe type recognized by this algorithm */
    public static final Class<TwissProbe>       s_clsProbeType = TwissProbe.class;
    
    
    
    /*
     *  Local Attributes
     */
     
     
     
    
    /*
     * Initialization
     */

    /** 
     *  Creates a new instance of TwissTrackerPmq 
     */
    public TwissTrackerPmq() { 
        super(s_strTypeId, s_intVersion, s_clsProbeType);
    };
    
    /**
     * Copy constructor for TwissTrackerPmq
     *
     * @param       sourceTracker   Tracker that is being copied
     */
    public TwissTrackerPmq(TwissTrackerPmq sourceTracker) {
        super( sourceTracker );
    }
    

    /**
     * Creates a deep copy of TwissTrackerPmq
     */
    @Override
    public TwissTrackerPmq copy() {
        return new TwissTrackerPmq( this );
    }
    
    /*
     *  Tracker Abstract Methods
     */
    
    /**
     * Propagates the probe through the element.
     *
     *  @param  probe   probe to propagate
     *  @param  elem    element acting on probe
     *
     *  @exception  ModelException  invalid probe type or error in advancing probe
     */
    @Override
    public void doPropagation(IProbe probe, IElement elem) throws ModelException {
        
        int     nSteps;
        double  dblSize;


        // Determine the number of steps through the element
        if (elem instanceof IdealPermMagQuad)   {
            nSteps = (int)Math.max(Math.ceil(elem.getLength() / getStepSize()), 1);
            
        } else if (elem instanceof IdealDrift) {
            nSteps = (int)Math.max(Math.ceil(elem.getLength() / getStepSize()), 1);
            
        }else if(this.getSpaceChargeFlag()) {
            nSteps = (int) Math.max(Math.ceil(elem.getLength() / getStepSize()), 1);
        
        } else { 
            nSteps = 1;
            
        }
        
        dblSize = elem.getLength() / nSteps;

        for (int i=0 ; i<nSteps ; i++) {
            this.advanceState(probe, elem, dblSize);
            this.advanceProbe(probe, elem, dblSize);
        }
    }
    

}
 
