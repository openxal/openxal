/*
 * EnsembleTracker.java
 *
 *  Created on October 22, 2002, 1:11 PM
 *  Modified:
 *      1/24/03 - CKA
 */

package xal.model.alg;



import xal.model.IElement;
import xal.model.IProbe;
import xal.model.ModelException;
import  xal.model.probe.EnsembleProbe;



/**
 *  Implements the tracking mechanism for EnsembleProbe objects.
 *
 * TODO not finished!
 * 
 * @author  Christopher Allen
 * @author Craig McChesney
 */
public class EnsembleTracker extends Tracker {


    /*
     *  Global Attributes
     */

    
    /** string type identifier for algorithm */
    public static final String      s_strTypeId = EnsembleTracker.class.getName();
    
    /** current algorithm version */
    public static final int         s_intVersion = 1;

    /** probe type recognized by this algorithm */
    public static final Class<EnsembleProbe>       s_clsProbeType = EnsembleProbe.class;
    
    /** maximum distance to advance probe before applying space charge kick */
    private static final double     s_dblMaxStepSize = 0.01;  
       
    
       
       
   /*
    * Initialization
    */
    
    
    /** 
     *  Creates a new instance of EnsembleTracker 
     */
    public EnsembleTracker() {
        super(s_strTypeId, s_intVersion, s_clsProbeType);
    }
    
    /**
     * Copy constructor for EnsembleTracker
     *
     * @param       sourceTracker   Tracker that is being copied
     */
    public EnsembleTracker( EnsembleTracker sourceTracker ) {
        super( sourceTracker );
    }
    
    /**
     * Create a deep copy of EnsembleTracker
     */
    @Override
    public EnsembleTracker copy() {
        return new EnsembleTracker( this );
    }
    /*
     * Accessing 
     */

     
    /**
     * Returns the maximum element subsection length (in meters) that the probe 
     * may be advanced before applying a space charge kick.
     */
    private double getMaxStepSize() {
    	return s_dblMaxStepSize;
    }    
    
    
    
    /*
     *  Tracker Abstract Protocol
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
        
      int nSteps     = this.compStepCount(elem);
      double dlbStep = elem.getLength() / nSteps;
      for (int i=0 ; i<nSteps ; i++) {
        this.advanceState(probe, elem, dlbStep);
        this.advanceProbe(probe, elem, dlbStep);
        probe.update();
      }
    }
    


    /*
     * Internal Support
     */

    /**
     * Returns the number of subsections to break the specified element in to
     * for propagation.
     * 
     * Currently returns the ceiling of the element length divided by the space
     * charge interval.  This should be re-evaluated when the ensemble algorithm
     * is fleshed out.
     * 
     * @param elem Element currently acting on probe
     * 
     * @return ceiling of element length divided by space charge interval
     */
    protected int compStepCount(IElement elem) {
    	return (int) Math.ceil(elem.getLength() / getMaxStepSize());
    }
    
//    /**
//     * Returns the length in meters of the next element subsection to advance
//     * the probe through.
//     * 
//     * This currently returns the full length of the element, should be changed
//     * when the algorithm is fleshed out.
//     * 
//     * @param elem Element currently acting on probe
//     * @param pos Position of Probe within elem starting from 0
//     * 
//     * @return double indicating size of next element subsection to advance probe through
//     */ 
//    protected double nextIntervalFrom(IElement elem, double pos) {
//    	return elem.getLength();
//    }
    
    protected void advanceState(IProbe probe, IElement elem, double dblLen) 
    		throws ModelException    {
    }
}
