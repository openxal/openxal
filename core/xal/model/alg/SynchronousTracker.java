/*
 * Created on Jun 8, 2004
 *
 * Copyright SNS/LANL, 2004
 */
package xal.model.alg;

import xal.model.IElement;
import xal.model.IProbe;
import xal.model.ModelException;
import xal.model.probe.SynchronousProbe;

/**
 * Algorithm for propagating a <code>SynchronousParticle</code> probe object 
 * through any modeling element that exposes the <code>IComponent</code> 
 * interface.
 * 
 * @author Christopher K. Allen
 *
 */
public class SynchronousTracker extends Tracker {




    /*
     *  Global Attributes
     */

    /** string type identifier for this algorithm */
    public static final String      s_strTypeId = SynchronousTracker.class.getName();
    
    /** current version of this algorithm */
    public static final int         s_intVersion = 1;

    /** probe type recognized by this algorithm */
    public static final Class<SynchronousProbe>       s_clsProbeType = SynchronousProbe.class;
    

    /*
     *  Local Attributes
     */
     
    
    
    
    /*
     * Initialization
     */
     
    /**
     * Default contructor for a <code>SynchronousTracker</code> objects.  These
     * objects have no internal state information.
     * 
     */
    public SynchronousTracker() {
        super(s_strTypeId, s_intVersion, s_clsProbeType);
    }


    /*
     * Tracker Protocol
     */
     
     
    /**
     * Perform the actual probe propagation through the the modeling element.
     * 
     * @param probe     interface to <code>SynchronousProbe</code> to be advanced
     * @param elem      interface to modeling element through which to advance probe
     * 
     * @throws ModelException   error during propagation
     * 
     * @see gov.sns.xal.model.alg.Tracker#doPropagation(gov.sns.xal.model.IProbe, gov.sns.xal.model.IElement)
     */
    @Override
    public void doPropagation(IProbe probe, IElement elem)
        throws ModelException 
    {
    
    }

}
