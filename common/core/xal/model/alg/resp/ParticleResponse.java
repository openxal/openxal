/*
 * ParticleResponse.java
 *
 * Created on March 25, 2003, 9:35 AM
 */

package xal.model.alg.resp;


import xal.tools.beam.PhaseMap;
import xal.tools.beam.PhaseMatrix;

import xal.model.IElement;
import xal.model.IProbe;
import xal.model.ModelException;
import xal.model.alg.Tracker;
import xal.model.probe.resp.ParticlePerturb;


/**
 *
 * @author  Christopher Allen
 * @author Craig McChesney
 */
@SuppressWarnings("deprecation")
public class ParticleResponse extends Tracker {
    
        
    /*
     *  Global Attributes
     */

    /** string type identifier for this algorithm */
    public static final String      s_strTypeId = "ParticleResponse";
    
    /** current version of this algorithm */
    public static final int         s_intVersion = 1;

    /** probe type recognized by this algorithm */
    public static final Class<ParticlePerturb>       s_clsProbeType = ParticlePerturb.class;




    /*
     * Initialization
     */	
    
    /** 
     *  Creates a new instance of ParticleTracker 
     */
    public ParticleResponse() { 
        super(s_strTypeId, s_intVersion, s_clsProbeType);
    }; 




    /*
     * Tracker Abstract Protocol
     */	
	
//    /**  All derived algorithms must implement this method for computing the first-order response
//     *  around the trajectory of a tracking probes.
//     *  This method advances the state of the probe through the provided element.
//     *
//     *  @param  probe       response probe to compute response
//     *  @param  elem        element that acts on probe
//     *
//     *  @exception  ModelException    unable to advance probe state
//     */
//    protected void trackResponse(IProbe probe, IElement elem) throws ModelException {
//    }
        

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
        
        int nSteps = compStepCount(elem);
        double dblStepSize = elem.getLength() / nSteps;
        for (int i=0 ; i<nSteps ; i++) {
            this.advanceState(probe, elem, dblStepSize);
            this.advanceProbe(probe, elem, dblStepSize);
            probe.update();
        }
    }


    /*
     * Support Methods
     */

    /**
     * Returns the number of subsections to break the specified element into for
     * propagation. Always one for a particle response.
     * 
     * @param elem Element currently acting on probe
     * 
     * @return one
     */
    protected int compStepCount(IElement elem) {
        return 1;
    }
        
    /** 
     * Advances the probe state through the element.  Always advances the full
     * length of the element, so the length parameter is ignored.
     *
     *  @param  ifcProbe    interface to probe being modified
     *  @param  ifcElem     interface to element acting on probe
     *  @param  dblLen      element subsection lengh, ignored
     *
     *  @exception ModelException     bad element transfer matrix/corrupt probe state
     */
    protected void advanceState(IProbe ifcProbe, IElement ifcElem, double dblLen)
    		throws ModelException {   
		ParticlePerturb probe = (ParticlePerturb) ifcProbe;
		PhaseMap elemTransferMap = ifcElem.transferMap(probe, dblLen);
		PhaseMatrix elemFirstOrder = elemTransferMap.getFirstOrder();
		PhaseMatrix probeTransferMatrix = elemFirstOrder.times(probe.getTransferMatrix());
		probe.setTransferMatrix(probeTransferMatrix);							
	}

}
