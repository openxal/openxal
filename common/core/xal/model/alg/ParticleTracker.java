/*
 * ParticleTracker.java
 *
 * Created on September 9, 2002, 11:16 AM
 */

package xal.model.alg;


import xal.tools.beam.PhaseMap;
import xal.tools.beam.PhaseVector;
import xal.model.IElement;
import xal.model.IProbe;
import xal.model.ModelException;
import xal.model.probe.ParticleProbe;



/**
 * Algorithm for tracking a single particle, represented by the class
 * <code>ParticleProbe</code> through a XAL modeling element, represented
 * by an object exposing the <code>IComponent</code> interface.
 *
 * @author  Christopher K. Allen
 * @author Craig McChesney
 */


public class ParticleTracker extends Tracker {

    /*
     *  Global Attributes
     */

    /** string type identifier for this algorithm */
    public static final String      s_strTypeId = ParticleTracker.class.getName();
    
    /** current version of this algorithm */
    public static final int         s_intVersion = 1;

    /** probe type recognized by this algorithm */
    public static final Class<ParticleProbe>       s_clsProbeType = ParticleProbe.class;
    
//    /** default value - maximum distance to advance probe before saving state */
//    private static final double     s_dblDefMaxStep = .01;  // one cm


    /*
     *  Local Attributes
     */
     
//    /** maximum distance to advance probe before saving state */
//    private double      m_dblMaxStep = s_dblDefMaxStep;
    
    
    
    /*
     * Initialization
     */
     
    
    /** 
     *  Creates a new instance of ParticleTracker 
     */
    public ParticleTracker() { 
        super(s_strTypeId, s_intVersion, s_clsProbeType);
    }; 
    
    


    /*
     *  Data Queries
     */    
    
//    /**
//     * Returns the maximum element subsection length (in meters) that the probe 
//     * may be advanced before saving particle state.
//     */
//    private double getMaxStepSize() {
//        return this.m_dblMaxStep;
//    }
    
    
    

    /*
     *  Tracker Abstract Protocol
     */


    /**
     * Propagates the probe through the element.
     *
     *  @param  iProbe  probe to propagate
     *  @param  elem    element acting on probe
     *
     *  @exception  ModelException  invalid probe type or error in advancing probe
     */
    @Override
    public void doPropagation(IProbe iProbe, IElement elem) throws ModelException {
        
        if (!this.validProbe(iProbe))
            throw new ModelException("ParticleTracker::propagate() - cannot propagate, invalid probe type.");
        ParticleProbe probe = (ParticleProbe)iProbe;

//        probe.setCurrentElement(elem.getId());
      
        double    dblLen = elem.getLength();
        
        this.advanceState(probe, elem, dblLen);
        this.advanceProbe(probe, elem, dblLen);

            
//      // take snapshot at beginning of element
//      probe.update();
//        
//      int nSteps = compStepCount(elem);
//      double sectionSize = elem.getLength() / nSteps;
//      for (int i=0 ; i<nSteps ; i++) {
//        this.advanceState(probe, elem, sectionSize);
//        this.advanceProbe(probe, elem, sectionSize);
//        probe.update();
//      }
    };
  


    /*
     * Support Methods
     */
  
//    /**
//     * Returns the number of subsections to break the specified element into for
//     * propagation. Always one for a particle tracker.
//     * 
//     * @param elem Element currently acting on probe
//     * 
//     * @return one
//     */
//    protected int compStepCount(IElement elem) {
//        double dblSecs = Math.ceil(elem.getLength() / getMaxStepSize());
//        int nSecs      = (int) Math.max(dblSecs, 1.0);
//        
//        return nSecs;
//    }
        
    /** 
     * Advances the probe state through the element.  
     *
     *  @param  probe       probe being modified
     *  @param  elem        element acting on probe
     *  @param  dblLen      length of element to advance
     *
     *  @exception ModelException     bad element transfer matrix/corrupt probe state
     */
    protected void advanceState(ParticleProbe probe, IElement ifcElem, double dblLen)
    		throws ModelException {
        
        // Properties of the element
        PhaseMap  mapPhi = ifcElem.transferMap(probe, dblLen);
        
        // Advance state vector
        PhaseVector  z0 = probe.phaseCoordinates();
        PhaseVector  z1 = mapPhi.apply(z0);
        
        probe.setPhaseCoordinates(z1);
    }



}
